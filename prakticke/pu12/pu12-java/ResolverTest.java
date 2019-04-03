import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;


class Tester {
    int tested = 0;
    int passed = 0;
    int ncase = 0;
    int time = 0;

    public boolean compare(Object result, Object expected, String msg) {
        tested++;
        if (result.equals(expected)) {
            passed++;
            return true;
        } else {
            System.err.println("    Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public void fail(String msg) {
        tested++;
        System.err.println("FAILED: " + msg);
    }

    public void startCase(String s) {
        System.err.println(String.format("CASE %d: %s", ++ncase, s));
    }

    public Set<Clause> asSet(List<Clause> cs) {
        return new HashSet<Clause>(cs);
    }

    public void testResolve(Clause a, Clause b, Set<Clause> expected) {
        startCase(String.format("Resolve %s ; %s |- %s", a, b,
            expected.stream().map(Clause::toString).collect(joining("; "))
        ));

        try {
            long start = System.nanoTime();
            Set<Clause> resolved = Resolver.resolve(a, b);
            long duration = (System.nanoTime() - start) / 1000;
            time += duration;

            compare(resolved, expected, "resolved clauses");

        }
        catch (Throwable e) {
            fail("Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void testResolve(String a, String b, String expected) {
        testResolve(Clause.fromString(a), Clause.fromString(b),
            Cnf.fromString(expected)
        );
    }
    public void testResolve2(String a, String b, String expected) {
        testResolve(a, b, expected);
        testResolve(b, a, expected);
    }

    public void testIsSatisfiable(boolean expected, Cnf theory, String description)
    {
        startCase(description);
        try {
            long start = System.nanoTime();
            boolean isSat = Resolver.isSatisfiable(theory);
            long duration = (System.nanoTime() - start) / 1000;
            time += duration;

            if (compare(isSat, expected, description))
                System.err.println("PASSED in " + (duration / 1000.0) + "ms");
        }
        catch (Throwable e) {
            fail("Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void testIsSatisfiable(boolean expected, Cnf theory)
    {
        testIsSatisfiable(expected, theory, theory.toString());
    }

    public boolean status() {
        System.err.println("");
        System.err.println("TESTED " + tested);
        System.err.println("PASSED " + passed);
        System.err.println("SUM(time) " + (time / 1000.0) + "ms");

        System.err.println(tested == passed ? "OK" : "ERROR" );
        return tested == passed;
    }

}

public class ResolverTest {
    static Literal L(String n) { return Literal.Lit(n); }
    static Literal N(String n) { return Literal.Not(n); }
    static Clause C(Literal... lits) { return new Clause(lits); }
    static Clause C(String s) { return Clause.fromString(s); }
    static Clause C(int... ns) { return new Clause(Arrays.stream(ns).mapToObj(n -> Literal.fromString(Integer.toString(n))).collect(toSet()));}
    static List<Clause> E(Clause... cls) { return new ArrayList<Clause>(Arrays.asList(cls)); }
    static Cnf T(Clause... cls) { return new Cnf(cls); }
    static Cnf T(String s) { return Cnf.fromString(s); }

    static Cnf trueChain(int n) {
        Cnf cnf = new Cnf();
        cnf.add(C(1, 2));
        cnf.add(C(-n, -1));
        for (int i = 0; i < n; ++i)
            cnf.add(C(-i, i + 1));
        return cnf;
    }

    static Cnf falseChain(int n) {
        Cnf cnf = new Cnf();
        cnf.add(C(1, 2));
        cnf.add(C(-n, -2));
        for (int i = 0; i < n; ++i)
            cnf.add(C(-i, i + 1));
        return cnf;
    }

    public static void main(String[] args) {
        Tester t = new Tester();

        t.testResolve2("p", "q", "");
        t.testResolve2("p", "-p", "()");
        t.testResolve2("-p q", "p -q", "q -q; -p p");
        t.testResolve2("p -p", "p", "p");
        t.testResolve2("p -p", "-p", "-p");
        t.testResolve2("p -p", "p -p", "p -p");

        t.testResolve2("(-jim kim)", "(-sarah -kim)", "-jim -sarah");

        System.err.println();

        t.testIsSatisfiable(true, T(), "empty theory");
        t.testIsSatisfiable(false, T(C("")), "only empty clause");
        t.testIsSatisfiable(false, T(";p"));

        t.testIsSatisfiable(true, T("p;p"));
        t.testIsSatisfiable(true, T("p;q"));
        t.testIsSatisfiable(true, T("p -p"));
        t.testIsSatisfiable(true, T("p -p; p"));
        t.testIsSatisfiable(true, T("p -p; -p"));
        t.testIsSatisfiable(false, T("p;-p"));
        t.testIsSatisfiable(true, T("p q r s t u v; -p; -q; -r; -s; -t; -u"));
        t.testIsSatisfiable(true, T("v u t s r q p; -p; -q; -r; -s; -t; -u"));
        t.testIsSatisfiable(false, T("p q r s t u v; -p; -q; -r; -s; -t; -u; -v"));
        t.testIsSatisfiable(false, T("v u t s r q p; -p; -q; -r; -s; -t; -u; -v"));

        t.testIsSatisfiable(true, T(
            "¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah"
        ), "kim jim sara");
        t.testIsSatisfiable(false, T(
            "¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; ¬kim"
        ), "kim jim sara |= kim");
        t.testIsSatisfiable(false, T(
            "¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; sarah"
        ), "kim jim sara |= ¬sarah");
        t.testIsSatisfiable(true, T(
            "¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; ¬jim"
        ), "kim jim sara |≠ jim");
        t.testIsSatisfiable(true, T(
            "¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; jim"
        ), "kim jim sara |≠ ¬jim");

        t.testIsSatisfiable(true, trueChain(4), "SAT chain 4");
        t.testIsSatisfiable(false, falseChain(4), "UNSAT chain 4");
        t.testIsSatisfiable(true, trueChain(20), "SAT chain 20");
        t.testIsSatisfiable(false, falseChain(20), "UNSAT chain 20");

        t.testIsSatisfiable(false, T(
            C(1,2,3), C(4,5,6), C(7,8,9), C(-2,-1), C(-3,-1), C(-3,-2), C(-5,-4), C(-6,-4),
            C(-6,-5), C(-8,-7), C(-9,-7), C(-9,-8), C(-4,-1), C(-7,-1), C(-7,-4),
            C(-5,-2), C(-8,-2), C(-8,-5), C(-6,-3), C(-9,-3), C(-9,-6), C(-1,-5),
            C(-4,-2), C(-4,-8), C(-7,-5), C(-1,-9), C(-7,-3), C(-2,-4), C(-5,-1),
            C(-5,-7), C(-8,-4), C(-2,-6), C(-5,-3), C(-5,-9), C(-8,-6), C(-3,-7),
            C(-9,-1), C(-3,-5), C(-6,-2), C(-6,-8), C(-9,-5)
            ), "q3");

    System.exit(t.status() ? 0 : 1);
    }
}
