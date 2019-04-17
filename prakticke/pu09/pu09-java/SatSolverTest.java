import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;



public class SatSolverTest {
    Tester tt = new Tester();
    static String C(int... ns) { return Arrays.stream(ns).mapToObj(Integer::toString).collect(Collectors.joining(" ")); }
    public static Theory T(String... cls) { return new Theory(cls); }

    public static String toString(Clause c) {
        if (c.isEmpty()) return "()";
        return c.stream()
            .map(l -> l.toString())
            .collect(Collectors.joining(" "))
        ;
    }
    public static String toString(Cnf cnf) {
        return cnf.stream()
            .map(c -> toString(c))
            .collect(Collectors.joining("; "))
        ;
    }

    public static boolean isSatisfied(Literal l, Map<String,Boolean> v) {
        return l.sign() == v.get(l.name());
    }

    public static boolean isSatisfied(Clause c, Map<String, Boolean> v) {
        return c.stream().anyMatch(l -> isSatisfied(l, v));
    }

    public void test(boolean expSat, String name, Theory t) {
        tt.testCase(name, s -> {
            Tester.TimedResult<SatSolver.Result> timedResult = tt.timed(() -> {
                return (new SatSolver()).solve(t);

            });
            SatSolver.Result res = timedResult.result;
            tt.message("ran in " + (timedResult.duration / 1000.0) + "ms");

            tt.message("expSat: " + expSat);
            tt.message("res.sat: " + res.sat);
            if (!tt.compare(res.sat, expSat, "bad sat/unsat result"))
                return;

            if (res.sat) {
                tt.message("res.valuation: " + res.valuation);

                if (res.valuation == null) {
                    tt.fail("returned sat == true, but valuation == null");
                    return;
               }

                for (String v : res.valuation.keySet())
                    if (res.valuation.get(v) == null) {
                        tt.fail("Variable " + v + " is unset");
                        return;
                    }

                for (Clause cls : t.cnf())
                    if (!isSatisfied(cls, res.valuation)) {
                        tt.fail("Clause " + toString(cls) + " is not satisfied");
                        return;
                    }
            }

            tt.passVerbose("PASSED in " + (timedResult.duration / 1000.0) + "ms");
        });
    }

    public void test(boolean expSat, Theory t) {
        test(expSat, toString(t.cnf()), t);
    }



    static Theory trueChain(int n) {
        List<String> cnf = new ArrayList<String>();
        cnf.add(C(1, 2));
        cnf.add(C(-n, -1));
        for (int i = 0; i < n; ++i)
            cnf.add(C(-i, i + 1));
        return T(cnf.toArray(new String[0]));
    }

    static Theory falseChain(int n) {
        List<String> cnf = new ArrayList<String>();
        cnf.add(C(1, 2));
        cnf.add(C(-n, -2));
        for (int i = 0; i < n; ++i)
            cnf.add(C(-i, i + 1));
        return T(cnf.toArray(new String[0]));
    }

    void run() {
        // Let's not include the first run in timings
        try { (new SatSolver()).solve(T()); } catch (Throwable e) {}

        test(true, "empty theory", T());
        test(false, "empty clause", T(""));
        test(false, T("", "p"));

        test(true, T("a"));
        test(true, T("-a"));
        test(false, T("a", "-a"));
        test(true, T("a b"));
        test(true, T("a", "b"));
        test(true, "chain", T("-a b", "-b c", "-c d", "-d e", "a"));
        test(false, "chainUnsat", T("-a b", "-b c", "-c d", "-d e", "a", "-e"));

        test(true, T("p q r s t u v", "-p", "-q", "-r", "-s", "-t", "-u"));
        test(false, T("p q r s t u v", "-p", "-q", "-r", "-s", "-t", "-u", "-v"));

        test(true, "kim jim sara", T(
            "-kim -sarah", "-jim kim", "-sarah jim", "kim jim sarah"
        ));
        test(false, "kim jim sara |= kim", T(
            "-kim -sarah", "-jim kim", "-sarah jim", "kim jim sarah", "-kim"
        ));
        test(false, "kim jim sara |= -sarah", T(
            "-kim -sarah", "-jim kim", "-sarah jim", "kim jim sarah", "sarah"
        ));
        test(true, "kim jim sara |≠ jim", T(
            "-kim -sarah", "-jim kim", "-sarah jim", "kim jim sarah", "-jim"
        ));
        test(true, "kim jim sara |≠ -jim", T(
            "-kim -sarah", "-jim kim", "-sarah jim", "kim jim sarah", "jim"
        ));

        test(true, "SAT chain 4", trueChain(4));
        test(false, "UNSAT chain 4", falseChain(4));
        test(true, "SAT chain 20", trueChain(20));
        test(false, "UNSAT chain 20", falseChain(20));

        test(false, "nqueens3", T(
            C(1,2,3), C(4,5,6), C(7,8,9), C(-2,-1), C(-3,-1), C(-3,-2), C(-5,-4), C(-6,-4),
            C(-6,-5), C(-8,-7), C(-9,-7), C(-9,-8), C(-4,-1), C(-7,-1), C(-7,-4),
            C(-5,-2), C(-8,-2), C(-8,-5), C(-6,-3), C(-9,-3), C(-9,-6), C(-1,-5),
            C(-4,-2), C(-4,-8), C(-7,-5), C(-1,-9), C(-7,-3), C(-2,-4), C(-5,-1),
            C(-5,-7), C(-8,-4), C(-2,-6), C(-5,-3), C(-5,-9), C(-8,-6), C(-3,-7),
            C(-9,-1), C(-3,-5), C(-6,-2), C(-6,-8), C(-9,-5)
            ));
        test(true, "nqueens4", TestData.theory_009_q4());
        test(true, "uf20_04130", TestData.theory_200_uf20_0413());
        test(true, "uf50_0500", TestData.theory_200_uf50_0500());
        test(true, "flat100_22", TestData.theory_300_flat100_22());
        test(false, "unsat uuf50_0992", TestData.theory_unsat_200_uuf50_0992());

        System.exit(tt.status() ? 0 : 1);
    }

    public static void main(String[] args) {
        SatSolverTest sst = new SatSolverTest();
        sst.run();
    }
}
