import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.function.Consumer;
import java.util.function.Function;

class Tester {
    int tested = 0;
    int passed = 0;
    int ncase = 0;
    int time = 0;
    Deque<Scope> scopes = new ArrayDeque<Scope>();
    Scope lastScope = null;

    class Scope implements AutoCloseable{
        private final Tester t;
        public final String name;
        public final Deque<String> messages = new ArrayDeque<String>();
        Scope(Tester t, String name) {
            this.t = t;
            this.name = name;
            t.scopes.push(this);
        }
        public void close() {
            t.scopes.pop();
            t.lastScope = t.scopes.peek();
        }
        public void message(String msg) {
            messages.push(msg);
        }
        public void print(String prefix) {
            if (!name.isEmpty())
                System.err.println(prefix + "In " + name);
            messages.descendingIterator().forEachRemaining(msg -> System.err.println(prefix + "  " + msg));
        }
    }

    public Scope scope(String msg) {
        lastScope = new Scope(this, msg);
        return lastScope;
    }
    public void message(String msg) {
        // throws if there isn't a scope
        lastScope.message(msg);
    }
    public void printScopes(String prefix) {
        scopes.descendingIterator().forEachRemaining(s -> s.print(prefix + "  "));
    }

    public boolean compare(Object result, Object expected, String msg) {
        tested++;
        if (expected.equals(result)) {
            passed++;
            System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("  ✕ Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public <T> boolean compareRef(T result, T expected, String msg) {
        tested++;
        if (result == expected) {
            passed++;
            System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("   ✕ Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public boolean testNotEqual(Object a, Object b, String msg) {
        return compare(!a.equals(b), true, msg);
    }

    public boolean testNotEqual(Object a, Object b) {
        return compare(!a.equals(b), true, a + " != " + b);
    }

    public void fail(String msg) {
        tested++;
        printScopes("  ");
        System.err.println("   ✕ Failed: " + msg);
    }

    public void startCase(String s) {
        System.err.println(String.format("CASE %d: %s", ++ncase, s));
    }

    public void scope(String name, Consumer<Scope> c) {
        try (Tester.Scope s = scope(name)) {
            try {
                c.accept(s);
            }
            catch (Throwable e) {
                fail("Exception: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void scope(Consumer<Scope> c) {
        scope("", c);
    }

    public void testCase(String name, Consumer<Scope> c) {
        startCase(name);
        scope(name, c);
    }

    public void testTermSubst(String desc, Term original, String var, Term replacement, Term expected) {
        Term result = original.substitute(var, replacement);
        if (!compare(result, expected, desc))
            return;
        // FIXME this test should check if a deep copy was made -- JKľ
        compare(
            result != original && result != replacement, true,
            "subst must create a new copy (of the original and of the replacement): " + desc
        );
        if (original instanceof FunctionApplication) {
            FunctionApplication oF = (FunctionApplication) original;
            FunctionApplication rF = (FunctionApplication) result;
            for (int i = 0; i < oF.subts().size(); ++i) {
                // FIXME this test should check if a deep copy was made
                compare(
                    rF.subts().get(i) != oF.subts().get(i) &&
                        rF.subts().get(i) != replacement,
                    true,
                    i + "-th subterm of FunctionApplication subst must be a new copy (of the original or of the replacement): " + desc
                );
            }
        }
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

class Struct<D> implements Structure<D> {
    public Set<D> domain;
    public Map<String, D> iC;
    public Map<String, Map<List<D>, D>> iF;
    public Map<String, Set<List<D>>> iP;
    Struct() {
        domain = new HashSet<>();
        iC = new HashMap<>();
        iF = new HashMap<>();
        iP = new HashMap<>();
    }
    Struct(Struct<D> original) {
        this.domain = new HashSet<>(original.domain);
        this.iC = new HashMap<>(original.iC);
        this.iF = new HashMap<>(original.iF);
        this.iP = new HashMap<>(original.iP);
    }
    Struct(Set<D> domain, Map<String, D> iC, Map<String, Map<List<D>, D>> iF, Map<String, Set<List<D>>> iP) {
        this.domain = domain; this.iC = iC; this.iF = iF; this.iP = iP;
    }
    Struct(Map<String, D> iC, Map<String, Map<List<D>, D>> iF, Map<String, Set<List<D>>> iP) {
        this.domain = new HashSet<>(); this.iC = iC; this.iF = iF; this.iP = iP;
        md();
    }
    public void md() {
        domain.addAll(iC.values());
        for (Map<List<D>, D> fs : iF.values()) {
            domain.addAll(fs.values());
            for (List<D> l : fs.keySet()) {
                domain.addAll(l);
            }
        }
        for (Set<List<D>> ps : iP.values()) {
            for (List<D> l : ps) {
                domain.addAll(l);
            }
        }
    }
    public Set<D> domain() { return domain; }
    public D iC(String name) { return iC.get(name); }
    public Map<List<D>, D> iF(String name) { return iF.get(name); }
    public Set<List<D>> iP(String name) { return iP.get(name); }

    @Override
    public String toString() {
        return "{ D=" + domain + "; iC=" + iC + "; iF=" + iF + "; iP=" + iP + "; }";
    }
}

public class FolFormulaTest {
    @SafeVarargs
    static <T> List<T> L(T... ts) { return Arrays.asList(ts); }
    @SafeVarargs
    static <T> Set<T> S(T... ts) { return new HashSet<T>(Arrays.asList(ts)); }
    static Variable V(String v) { return new Variable(v); }
    static Constant C(String c) { return new Constant(c); }
    static FunctionApplication F(String f, Term... ts) { return new FunctionApplication(f, Arrays.asList(ts)); }
    static PredicateAtom P(String p, Term... ts) { return new PredicateAtom(p, Arrays.asList(ts)); }
    static EqualityAtom Eq(Term l, Term r) { return new EqualityAtom(l, r); }
    static Negation Neg(Formula f) { return new Negation(f); }
    static Conjunction And(Formula... fs) { return new Conjunction(Arrays.asList(fs)); }
    static Disjunction Or(Formula... fs) { return new Disjunction(Arrays.asList(fs)); }
    static Implication Impl(Formula l, Formula r) { return new Implication(l, r); }
    static Equivalence Eq(Formula l, Formula r) { return new Equivalence(l, r); }
    static ForAll Fa(String v, Formula f) { return new ForAll(v, f); }
    static ForAll Fa(Variable v, Formula f) { return new ForAll(v.name(), f); }
    static Exists Ex(String v, Formula f) { return new Exists(v, f); }
    static Exists Ex(Variable v, Formula f) { return new Exists(v.name(), f); }
    static <K,V> Map<K,V> M() { return new HashMap<K,V>(); }
    static <K,V> Map<K,V> M(Map<K,V> orig) { return new HashMap<K,V>(orig); }
    static <K,V> Map<K,V> M(K k1, V v1) { Map<K,V> m = M(); m.put(k1, v1); return m; }
    static <K,V> Map<K,V> M(K k1, V v1, K k2, V v2) { Map<K,V> m = M(k1, v1); m.put(k2, v2); return m; }
    static <K,V> Map<K,V> M(List<? extends K> ks, List<? extends V> vs) {
        Map<K,V> m = M();
        IntStream.range(0, Math.min(ks.size(), vs.size())).forEach(i -> {
            m.put(ks.get(i), vs.get(i)); } );
        return m;
    }
    static <K,V> AbstractMap.SimpleImmutableEntry<K,V> KV(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }
    @SafeVarargs
    static <K,V> Map<K,V> M(AbstractMap.SimpleImmutableEntry<? extends K,? extends V>... es) {
        return Arrays.stream(es).collect(
            Collectors.toMap(
                e -> { return e.getKey(); },
                e  -> { return e.getValue(); }
            )
        );
    }
    static <K,V> Map<K,V> M(List<AbstractMap.SimpleImmutableEntry<? extends K,? extends V>> es) {
        return es.stream().collect(
            Collectors.toMap(
                e -> { return e.getKey(); },
                e  -> { return e.getValue(); }
            )
        );
    }

    static class FormulaStr {
        public final Formula f;
        public final String s;
        FormulaStr(Formula f, String s) {
            this.f = f;
            this.s = s;
        }
    };
    static FormulaStr FS(Formula f, String s) { return new FormulaStr(f, s); }

    public static void main(String[] args) {
        Tester t = new Tester();


        t.testCase("Variable aaa", s -> {
            Variable v = V("aaa");
            t.compare(v.name(), "aaa", "Variable name");
            t.compare(v.toString(), "aaa", "Variable.toString");
            t.compare(v.variables(), S("aaa"), "Variable.variables");
            t.compare(v.constants(), S(), "Variable.constants");
            t.compare(v.functions(), S(), "Variable.functions");
            Struct<Integer> m = new Struct<>(M("aaa", 1), M(), M());
            Map<String, Integer> e = M("aaa", 2);
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(v.eval(m, e), 2, "Variable.eval");
        });

        t.testCase("Constant aaa", s -> {
            Constant c = C("aaa");
            t.compare(c.name(), "aaa", "Constant.name");
            t.compare(c.toString(), "aaa", "Constant.toString");
            t.compare(c.variables(), S(), "Constant.variables");
            t.compare(c.constants(), S("aaa"), "Constant.constants");
            t.compare(c.functions(), S(), "Constant.functions");

            Struct<Integer> m = new Struct<>(M("aaa", 1), M(), M());
            Map<String, Integer> e = M("aaa", 2);
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(c.eval(m, e), 1, "Constant.eval");
        });

        t.testCase("FunctionApplication fff(ccc)", s -> {
            String cn = "ccc"; String fn = "fff";
            Constant c = C(cn);
            FunctionApplication f = F(fn, c);

            t.compare(f.name(), fn, "Function.name");
            t.compare(f.toString(), "fff(ccc)", "Function.toString");
            t.compare(f.subts(), L(c), "Function.subts");
            t.compare(f.variables(), S(), "Function.variables");
            t.compare(f.constants(), S(cn), "Function.constants");
            t.compare(f.functions(), S(fn), "Function.functions");

            Struct<Integer> m = new Struct<>(M("ccc", 1), M("fff", M(L(1), 2)), M());
            Map<String, Integer> e = M("ccc", 3);
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(f.eval(m, e), 2, "Function.eval");
        });

        t.testCase("FunctionApplication fff(ggg(ccc),xx)", s -> {
            String cn = "ccc"; String vn = "xx"; String fn = "fff"; String gn ="ggg";
            Term c = C(cn);
            Term v = V(vn);
            FunctionApplication g = F(gn, c);
            FunctionApplication f = F(fn, g, v);

            t.compare(f.name(), fn, "Function.name");
            t.compare(f.toString(), "fff(ggg(ccc),xx)", "Function.toString");
            t.compare(f.subts(), L(g, v), "Function.subts");
            t.compare(f.variables(), S(vn), "Function.variables");
            t.compare(f.constants(), S(cn), "Function.constants");
            t.compare(f.functions(), S(fn, gn), "Function.functions");

            Struct<Integer> m = new Struct<>(M("ccc", 1),
                M("fff", M(L(1,3), 4), "ggg", M(L(1), 1)), M());
            Map<String, Integer> e = M("xx", 3);
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(f.eval(m, e), 4, "Function.eval");
        });

        t.testCase("FunctionApplication fff(ggg(yyy),xx)", s -> {
            String vxn = "xx"; String vyn = "yyy"; String fn = "fff"; String gn ="ggg";
            Term vx = V(vxn);
            Term vy = V(vyn);
            FunctionApplication g = F(gn, vy);
            FunctionApplication f = F(fn, g, vx);

            t.compare(f.name(), fn, "Function.name");
            t.compare(f.toString(), "fff(ggg(yyy),xx)", "Function.toString");
            t.compare(f.subts(), L(g, vx), "Function.subts");
            t.compare(f.variables(), S(vxn, vyn), "Function.variables");
            t.compare(f.constants(), S(), "Function.constants");
            t.compare(f.functions(), S(fn, gn), "Function.functions");

            Struct<Integer> m = new Struct<>(M(),
                M("fff", M(L(1,3), 4), "ggg", M(L(1), 1)), M());
            Map<String, Integer> e = M("xx", 3, "yyy", 1);
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(f.eval(m, e), 4, "Function.eval");
        });

        t.testCase("FunctionApplication f(x,c1,g(y,h(i(j(u),k(c4),l(f(v,c5,w))),z,c3),c2))", s -> {
            Term x = V("x");
            Term c1 = C("c1");
            Term g = F("g",
                V("y"),
                F("h",
                    F("i", F("j", V("u")), F("k", C("c4")),
                        F("l", F("f", V("v"), C("c5"), V("w")))),
                    V("z"),
                    C("c3")
                ),
                C("c2")
            );
            FunctionApplication f = F("f", x, c1, g);

            t.compare(f.name(), "f", "Function.name");
            t.compare(f.toString(), "f(x,c1,g(y,h(i(j(u),k(c4),l(f(v,c5,w))),z,c3),c2))", "Function.toString");
            t.compare(f.subts(), L(x, c1, g), "Function.subts");
            t.compare(f.variables(), S("u", "v", "w", "x", "y", "z"), "Function.variables");
            t.compare(f.constants(), S("c1", "c2", "c3", "c4", "c5"), "Function.constants");
            t.compare(f.functions(), S("f", "g", "h", "i", "j", "k", "l"), "Function.functions");

            Struct<Integer> m = new Struct<>(
                M(
                    KV("c1", 11), KV("c2", 22), KV("c3", 33),
                    KV("c4", 44), KV("c5", 55)
                ),
                M(
                    KV("f", M(L(1,11,111), 42, L(5,55,6), 777)),
                    KV("g", M(L(2,222,22), 111)),
                    KV("h", M(L(333,3,33), 222)),
                    KV("i", M(L(444,555,666), 333)),
                    KV("j", M(L(4), 444)),
                    KV("k", M(L(44), 555)),
                    KV("l", M(L(777), 666))
                ),
                M());
            Map<String, Integer> e = M(
                KV("x", 1), KV("y", 2), KV("z", 3),
                KV("u", 4), KV("v", 5), KV("w", 6)
            );
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(f.eval(m, e), 42, "Function.eval");
        });

        t.testCase("TermEquals", s -> {
            t.compare(V("xxx"), V("xxx"), "Var xxx == Var xxx");
            t.compare(C("ccc"), C("ccc"), "Const ccc == Const ccc");
            t.compare(F("fff", V("xxx")), F("fff", V("xxx")), "fff(Var xxx) == fff(Var xxx)");
            t.compare(F("fff", C("ccc")), F("fff", C("ccc")), "fff(Const ccc) == fff(Const ccc)");
            t.compare(
                F("f", F("g", V("x"), C("c")), C("c")),
                F("f", F("g", V("x"), C("c")), C("c")),
                "f(g(x,c),c) == f(g(x,c),c)"
            );
            t.compare(
                F("f", F("g", V("x"), F("f", V("x"), C("c"))),
                       F("h", V("x"), C("c"), V("y"))),
                F("f", F("g", V("x"), F("f", V("x"), C("c"))),
                       F("h", V("x"), C("c"), V("y"))),
                "f(g(x,f(x,c)),h(x,c,y)) == f(g(x,f(x,c)),h(x,c,y))"
            );

            t.compare(V("xxx").equals(V("yyy")), false, "xxx != yyy");
            t.compare(C("aaa").equals(C("bbb")), false, "aaa != bbb");
            t.compare(V("xxx").equals(C("xxx")), false, "Var xxx != Const xxx");
            t.compare(C("xxx").equals(V("xxx")), false, "Const xxx != Var xxx");
            t.compare(F("fff", V("xxx")).equals(V("xxx")), false, "fff(xxx) != xxx");
            t.compare(F("fff", C("ccc")).equals(C("ccc")), false, "fff(ccc) != ccc");
            t.compare(V("xxx").equals(F("fff", V("xxx"))), false, "xxx != fff(xxx)");
            t.compare(C("ccc").equals(F("fff", C("ccc"))), false, "ccc != fff(ccc)");
            t.compare(F("fff", V("xxx")).equals(F("ggg", V("xxx"))),
                false, "fff(xxx) != ggg(xxx)");
            t.compare(F("fff", V("xxx")).equals(F("fff", V("xxx"), V("xxx"))),
                false, "fff(xxx) != fff(xxx,xxx)");
            t.compare(F("f", V("x")).equals(V("f(x)")), false, "f(x) != Var 'f(x)'");
            t.compare(F("f", V("c")).equals(C("f(c)")), false, "f(c) != Const 'f(c)'");
            t.compare(F("fff", V("xxx"), V("xxx")).equals(F("fff", V("xxx,xxx"))),
                false, "fff(xxx,xxx) != fff(Var 'xxx,xxx')");
            t.compare(F("fff", C("ccc"), C("ddd")).equals(F("fff", C("ccc,ddd"))),
                false, "fff(ccc,ddd) != fff(Const 'ccc,ddd')");
            t.compare(F("f", V("x")).equals(F("f", C("x"))), false, "f(Var 'x') != f(Const 'x')");
            t.compare(F("f", C("c"), V("x")).equals(F("f", V("x"), C("c"))),
                false, "f(c,x) != f(x,c)");
            t.compare(
                F("f", F("g", V("x"), F("f", V("x"), C("c"))), C("c"))
                .equals(
                F("f", F("g", V("x"), F("h", V("x"), C("c"))), C("c"))),
                false,
                "f(g(x,f(x,c)),c) != f(g(x,h(x,c)),d)"
            );
        });

        t.testCase("TermEval", s -> {
            int a=1, b=2, c=3, d=4, e=5, mm = 6, x = 7;
            Struct<Integer> m = new Struct<>();
            m.iC.put("Anicka", a);
            m.iC.put("Betka", b);
            m.iC.put("Cecilka", c);
            m.iC.put("Edo", e);

            m.iP.put("dievca", S(L(a), L(b), L(c), L(d)));
            m.iP.put("ma_rada", S(
                 L(a, b), L(a, c),
                 L(b, a), L(b, d), L(b, mm),
                 L(c, a), L(c, b), L(c, c), L(c, d), L(c, e), L(c, mm),
                 L(d, c), L(d, e),
                 L(e, b),
                 L(mm, mm), L(mm,x)
            ));
            Map<List<Integer>, Integer> bff = new HashMap<>();
            bff.put(L(a), b); bff.put(L(b), a); bff.put(L(c), d); bff.put(L(d), c);
            bff.put(L(e), x); bff.put(L(mm), x); bff.put(L(x), x);
            m.iF.put("bff", bff);
            m.md();

            t.scope(ss -> {
                Map<String, Integer> e1 = M("x", a, "y", d);
                t.message("m: " + m);
                t.message("e1: " + e1);
                t.compare(C("Anicka").eval(m, e1), a, "eval Anicka");
                t.compare(F("bff", C("Cecilka")).eval(m, e1), d, "eval bff(Cecilka)");
                t.compare(F("bff", F("bff", C("Betka"))).eval(m, e1), b, "eval bff(bff(Betka))");
                t.compare(V("x").eval(m, e1), a, "eval x");
                t.compare(F("bff", V("y")).eval(m, e1), c, "eval bff(y)");
                t.compare(F("bff", F("bff", V("x"))).eval(m, e1), a, "eval bff(bff(x))");
            });

            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", mm, "y", c);
                t.message("m: " + m);
                t.message("e2: " + e2);
                t.compare(C("Anicka").eval(m, e2), a, "eval Anicka");
                t.compare(F("bff", C("Cecilka")).eval(m, e2), d, "eval bff(Cecilka)");
                t.compare(F("bff", F("bff", C("Betka"))).eval(m, e2), b, "eval bff(bff(Betka))");
                t.compare(V("x").eval(m, e2), mm, "eval x");
                t.compare(F("bff", V("y")).eval(m, e2), d, "eval bff(y)");
                t.compare(F("bff", F("bff", V("x"))).eval(m, e2), x, "eval bff(bff(x))");
            });
        });


        t.testCase("PredicateAtom", s -> {
            String fn = "fff", vn = "xxx", cn = "ccc", pn = "Ppp";
            Variable v = V(vn);
            Constant c = C(cn);
            FunctionApplication f = F(fn, v);
            PredicateAtom p = P(pn, f, c);

            t.compare(p.name(), pn, "Predicate name");
            t.compare(p.subfs(), Collections.emptyList(), "Predicate subfs");
            t.compare(p.subts(), L(f, c), "Predicate subts");
            t.compare(p.toString(), String.format("%s(%s(%s),%s)", pn, fn, vn, cn), "Predicate toString");
            t.compare(p.variables(), S(vn), "Predicate variables");
            t.compare(p.constants(), S(cn), "Predicate constants");
            t.compare(p.functions(), S(fn), "Predicate functions");
            t.compare(p.predicates(), S(pn), "Predicate predicates");
            t.compare(p.freeVariables(), S(vn), "Predicate free variables");

            Struct<Integer> m = new Struct<>(M(cn, 1), M(fn, M(L(2), 3)), M(pn, S(L(3,1))));
            Map<String, Integer> e = M(vn, 2);
            t.message("m: " + m);
            t.message("e: " + e);
            t.compare(p.isSatisfied(m, e), true, "Predicate isSatisfied");
        });

        t.testCase("EqualityAtom", s -> {
            String fn = "fff", vn = "xxx", cn = "ccc";
            Variable v = V(vn);
            Constant c = C(cn);
            FunctionApplication f = F(fn, c);
            EqualityAtom eq = Eq(f, v);

            t.compare(eq.subfs(), L(), "Equality subfs");
            t.compare(eq.subts(), L(f, v), "Equality subts");
            t.compare(eq.toString(), String.format("%s(%s)=%s", fn, cn, vn), "Equality toString");
            t.compare(eq.variables(), S(vn), "Equality variables");
            t.compare(eq.constants(), S(cn), "Equality constants");
            t.compare(eq.functions(), S(fn), "Equality functions");
            t.compare(eq.predicates(), S(), "Equality predicates");
            t.compare(eq.freeVariables(), S(vn), "Equality free variables");
            t.compareRef(eq.leftTerm(), f, "Equality leftTerm");
            t.compareRef(eq.rightTerm(), v, "Equality rightTerm");

            Struct<Integer> m = new Struct<>(M(cn, 1), M(fn, M(L(1), 2)), M());
            t.scope(ss -> {
                Map<String, Integer> e1 = M(vn, 2);
                t.message("m: " + m);
                t.message("e: " + e1);
                t.compare(eq.isSatisfied(m, e1), true, "Equality isSatisfied");
            });

            t.scope(ss -> {
                Map<String, Integer> e2 = M(vn, 3);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(eq.isSatisfied(m, e2), false, "Equality not isSatisfied");
            });
        });

        t.testCase("Negation", s -> {
            Formula p = P("p", V("x"));
            Negation n = Neg(p);

            t.compare(n.subfs(), L(p), "Negation subfs");
            t.compare(n.originalFormula(), p, "Negation originalFormula");
            t.compare(n.toString(), "-p(x)", "Negation toString");
            t.compare(n.variables(), S("x"), "Negation variables");
            t.compare(n.constants(), S(), "Negation constants");
            t.compare(n.functions(), S(), "Negation functions");
            t.compare(n.predicates(), S("p"), "Negation predicates");
            t.compare(n.freeVariables(), S("x"), "Negation free variables");

            Struct<Integer> m = new Struct<>(M(), M(), M("p", S(L(1))));
            t.scope(ss -> {
                Map<String, Integer> e1 = M("x", 1);
                t.message("m: " + m);
                t.message("e: " + e1);
                t.compare(n.isSatisfied(m, e1), false, "Negation isSatisfied false");
            });

            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 2);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(n.isSatisfied(m, e2), true, "Negation isSatisfied true");
            });
        });

        t.testCase("Disjunction", s -> {
            Formula p = P("p", V("x"));
            Formula q = P("q", V("x"));
            Disjunction f = Or(p,q);

            t.compare(f.subfs(), L(p,q), "Disjunction subfs");
            t.compare(f.toString(), "(p(x)|q(x))", "Disjunction toString");
            t.compare(f.variables(), S("x"), "Disjunction variables");
            t.compare(f.constants(), S(), "Disjunction constants");
            t.compare(f.functions(), S(), "Disjunction functions");
            t.compare(f.predicates(), S("p","q"), "Disjunction predicates");
            t.compare(f.freeVariables(), S("x"), "Disjunction free variables");

            Struct<Integer> m = new Struct<>(M(), M(), M("p", S(L(1)), "q", S(L(2))));
            t.scope(ss -> {
                Map<String, Integer> e1 = M("x", 3);
                t.message("m: " + m);
                t.message("e: " + e1);
                t.compare(f.isSatisfied(m, e1), false, "Disjunction isSatisfied false");
            });

            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), true, "Disjunction isSatisfied true 1");
            });
            t.scope(ss -> {
                Map<String, Integer> e3 = M("x", 2);
                t.message("m: " + m);
                t.message("e: " + e3);
                t.compare(f.isSatisfied(m, e3), true, "Disjunction isSatisfied true 2");
            });
        });

        t.testCase("Conjunction", s -> {
            Formula p = P("p", V("x"));
            Formula q = P("q", V("y"));
            Conjunction f = And(p,q);

            t.compare(f.subfs(), L(p,q), "Conjunction subfs");
            t.compare(f.toString(), "(p(x)&q(y))", "Conjunction toString");
            t.compare(f.variables(), S("x", "y"), "Conjunction variables");
            t.compare(f.constants(), S(), "Conjunction constants");
            t.compare(f.functions(), S(), "Conjunction functions");
            t.compare(f.predicates(), S("p","q"), "Conjunction predicates");
            t.compare(f.freeVariables(), S("x", "y"), "Conjunction free variables");

            Struct<Integer> m = new Struct<>(M(), M(), M("p", S(L(1)), "q", S(L(1))));
            t.scope(ss -> {
                Map<String, Integer> e1 = M("x", 2, "y", 2);
                t.message("m: " + m);
                t.message("e: " + e1);
                t.compare(f.isSatisfied(m, e1), false, "Conjunction isSatisfied false both");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1, "y", 2);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), false, "Conjunction isSatisfied false y");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 2, "y", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), false, "Conjunction isSatisfied false x");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1, "y", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), true, "Conjunction isSatisfied true");
            });
        });

        t.testCase("Disjunction empty", s -> {
            Disjunction f = Or();

            t.compare(f.subfs(), L(), "Disjunction subfs");
            t.compare(f.toString(), "()", "Disjunction toString");
            t.compare(f.variables(), S(), "Disjunction variables");
            t.compare(f.constants(), S(), "Disjunction constants");
            t.compare(f.functions(), S(), "Disjunction functions");
            t.compare(f.predicates(), S(), "Disjunction predicates");
            t.compare(f.freeVariables(), S(), "Disjunction free variables");

            Struct<Integer> m = new Struct<>(M(), M(), M());
            Map<String, Integer> e1 = M();
            t.compare(f.isSatisfied(m, e1), false, "empty disjunction isSatisfied ");
        });

        t.testCase("Conjunction empty", s -> {
            Conjunction f = And();

            t.compare(f.subfs(), L(), "Conjunction subfs");
            t.compare(f.toString(), "()", "Conjunction toString");
            t.compare(f.variables(), S(), "Conjunction variables");
            t.compare(f.constants(), S(), "Conjunction constants");
            t.compare(f.functions(), S(), "Conjunction functions");
            t.compare(f.predicates(), S(), "Conjunction predicates");
            t.compare(f.freeVariables(), S(), "Conjunction free variables");

            Struct<Integer> m = new Struct<>(M(), M(), M());
            Map<String, Integer> e1 = M();
            t.compare(f.isSatisfied(m, e1), true, "empty disjunction isSatisfied ");
        });

        t.testCase("Implication", s -> {
            Formula p = P("p", F("f", V("x"), C("c")));
            Formula q = P("q", V("y"));
            Implication f = Impl(p,q);

            t.compare(f.subfs(), L(p,q), "Implication subfs");
            t.compare(f.leftSide(), p, "Implication leftSide");
            t.compare(f.rightSide(), q, "Implication rightSide");
            t.compare(f.toString(), "(p(f(x,c))->q(y))", "Implication toString");
            t.compare(f.variables(), S("x", "y"), "Implication variables");
            t.compare(f.constants(), S("c"), "Implication constants");
            t.compare(f.functions(), S("f"), "Implication functions");
            t.compare(f.predicates(), S("p","q"), "Implication predicates");
            t.compare(f.freeVariables(), S("x", "y"), "Implication free variables");

            Struct<Integer> m = new Struct<>(M("c", 1), M("f", M(L(1,1), 1, L(2,1), 2)), M("p", S(L(1)), "q", S(L(1))));
            t.scope(ss -> {
                Map<String, Integer> e1 = M("x", 2, "y", 2);
                t.message("m: " + m);
                t.message("e: " + e1);
                t.compare(f.isSatisfied(m, e1), true, "Implication isSatisfied false false");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1, "y", 2);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), false, "Implication isSatisfied true false");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 2, "y", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), true, "Implication isSatisfied false true");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1, "y", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), true, "Implication isSatisfied true true");
            });
        });

        t.testCase("Equivalence", s -> {
            Formula p = P("p", F("f", V("x"), C("c")));
            Formula q = P("q", V("y"));
            Equivalence f = Eq(p,q);

            t.compare(f.subfs(), L(p,q), "Equivalence subfs");
            t.compare(f.leftSide(), p, "Equivalence leftSide");
            t.compare(f.rightSide(), q, "Equivalence rightSide");
            t.compare(f.toString(), "(p(f(x,c))<->q(y))", "Equivalence toString");
            t.compare(f.variables(), S("x", "y"), "Equivalence variables");
            t.compare(f.constants(), S("c"), "Equivalence constants");
            t.compare(f.functions(), S("f"), "Equivalence functions");
            t.compare(f.predicates(), S("p","q"), "Equivalence predicates");
            t.compare(f.freeVariables(), S("x", "y"), "Equivalence free variables");

            Struct<Integer> m = new Struct<>(M("c", 1), M("f", M(L(1,1), 1, L(2,1), 2)), M("p", S(L(1)), "q", S(L(1))));
            t.scope(ss -> {
                Map<String, Integer> e1 = M("x", 2, "y", 2);
                t.message("m: " + m);
                t.message("e: " + e1);
                t.compare(f.isSatisfied(m, e1), true, "Equivalence isSatisfied false false");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1, "y", 2);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), false, "Equivalence isSatisfied true false");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 2, "y", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), false, "Equivalence isSatisfied false true");
            });
            t.scope(ss -> {
                Map<String, Integer> e2 = M("x", 1, "y", 1);
                t.message("m: " + m);
                t.message("e: " + e2);
                t.compare(f.isSatisfied(m, e2), true, "Equivalence isSatisfied true true");
            });
        });

        t.testCase("ForAll", s -> {
            Formula p = P("p", F("f", V("x"), C("c")));
            ForAll f = Fa(V("x"), p);

            t.compare(f.subfs(), L(p), "ForAll subfs");
            t.compare(f.originalFormula(), p, "ForAll originalFormula");
            t.compare(f.qvar(), "x", "ForAll qvar");
            t.compare(f.toString(), "∀x p(f(x,c))", "ForAll toString");
            t.compare(f.variables(), S("x"), "ForAll variables");
            t.compare(Fa(V("y"), p).variables(), S("x", "y"), "ForAll variables including the bound one");
            t.compare(f.constants(), S("c"), "ForAll constants");
            t.compare(f.functions(), S("f"), "ForAll functions");
            t.compare(f.predicates(), S("p"), "ForAll predicates");
            t.compare(f.freeVariables(), S(), "ForAll free variables");

            t.scope(ss -> {
                Struct<Integer> m1 = new Struct<>(M("c", 1), M("f", M(L(1,1), 1, L(2,1), 2)), M("p", S(L(1))));
                Map<String, Integer> e1 = M("x", 1);
                t.message("m1: " + m1);
                t.message("e1: " + e1);
                t.compare(f.isSatisfied(m1, e1), false, "ForAll isSatisfied some");
            });
            t.scope(ss -> {
                Struct<Integer> m2 = new Struct<>(M("c", 1), M("f", M(L(1,1), 1, L(2,1), 2)), M("p", S(L(1), L(2))));
                Map<String, Integer> e2 = M("x", 1);
                t.message("m2: " + m2);
                t.message("e2: " + e2);
                t.compare(f.isSatisfied(m2, e2), true, "ForAll isSatisfied all");
            });
            t.scope(ss -> {
                Struct<String> m3 = new Struct<>(S("!", "@", "#", "$", "%"),M("c", "!"), M("f", M(L("!","!"), "!", L("@","!"), "@")), M("p", S(L("!"))));
                Map<String, String> e3orig = M();
                Map<String, String> e3 = M(e3orig);
                t.message("m3: " + m3);
                t.message("e3: " + e3);
                t.compare(f.isSatisfied(m3, e3), false, "ForAll isSatisfied some in string domain under empty valuation");
                t.compare(
                    e3, e3orig,
                    "Forall isSatisfied some valuation unchanged"
                );
            });
            t.scope(ss -> {
                Struct<String> m4 = new Struct<>(M("c", "!"), M("f", M(KV(L("!","!"), "!"), KV(L("@","!"), "@"), KV(L("#","!"), "#"))), M("p", S(L("!"), L("@"), L("#"))));
                Map<String, String> e4orig = M();
                Map<String, String> e4 = M(e4orig);
                t.message("m4: " + m4);
                t.message("e4: " + e4);
                t.compare(f.isSatisfied(m4, e4), true, "ForAll isSatisfied all in string domain under empty valuation");
                t.compare(
                    e4, e4orig,
                    "Forall isSatisfied all valuation unchanged"
                );
            });
        });

        t.testCase("Exists", s -> {
            Formula p = P("p", F("f", V("x"), C("c")));
            Exists f = Ex(V("x"), p);

            t.compare(f.subfs(), L(p), "Exists subfs");
            t.compare(f.originalFormula(), p, "Exists originalFormula");
            t.compare(f.qvar(), "x", "Exists qvar");
            t.compare(f.toString(), "∃x p(f(x,c))", "Exists toString");
            t.compare(f.variables(), S("x"), "Exists variables");
            t.compare(Ex(V("y"), p).variables(), S("x", "y"), "Exists variables including the bound one");
            t.compare(f.constants(), S("c"), "Exists constants");
            t.compare(f.functions(), S("f"), "Exists functions");
            t.compare(f.predicates(), S("p"), "Exists predicates");
            t.compare(f.freeVariables(), S(), "Exists free variables");

            t.scope(ss -> {
                Struct<Integer> m1 = new Struct<>(M("c", 1), M("f", M(L(1,1), 1, L(2,1), 2)), M("p", S()));
                Map<String, Integer> e1 = M("x", 1);
                t.message("m1: " + m1);
                t.message("e1: " + e1);
                t.compare(f.isSatisfied(m1, e1), false, "Exists isSatisfied none");
            });
            t.scope(ss -> {
                Struct<Integer> m2 = new Struct<>(M("c", 1), M("f", M(L(1,1), 1, L(2,1), 2)), M("p", S(L(1))));
                Map<String, Integer> e2 = M("x", 1);
                t.message("m2: " + m2);
                t.message("e2: " + e2);
                t.compare(f.isSatisfied(m2, e2), true, "Exists isSatisfied some");
            });
            t.scope(ss -> {
                Struct<String> m3 = new Struct<>(S("_1", "_2", "_3", "_4", "_5"), M("c", "_1"), M("f", M(L("_1","_1"), "_1", L("_2","_1"), "_2")), M("p", S()));
                Map<String, String> e3orig = M();
                Map<String, String> e3 = M(e3orig);
                t.message("m3: " + m3);
                t.message("e3: " + e3);
                t.compare(
                    f.isSatisfied(m3, e3), false,
                    "Exists isSatisfied none in string domain under empty valuation"
                );
                t.compare(
                    e3, e3orig,
                    "Exists isSatisfied none valuation unchanged"
                );
            });
            t.scope(ss -> {
                Struct<String> m4 = new Struct<>(S("!", "@", "#", "$", "%"), M("c", "!"), M("f", M(L("!","!"), "!", L("@","!"), "@")), M("p", S(L("!"))));
                Map<String, String> e4orig = M();
                Map<String, String> e4 = M(e4orig);
                t.message("m4: " + m4);
                t.message("e4: " + e4);
                t.compare(f.isSatisfied(m4, e4), true,
                    "Exists isSatisfied some in string domain under empty valuation");
                t.compare(
                    e4, e4orig,
                    "Exists isSatisfied some valuation unchanged"
                );
            });
        });


        t.testCase("QuantifiedSubfs", s -> {
            Formula a = Fa("x",
                P("p", C("c"), F("f", V("x"), V("y")), V("w"))
            );
            Formula b = Ex("y",
                P("q", V("y"), C("d"), V("z"))
            );
            List<Formula> fs = L(
                Neg(a), Fa("y", a), Ex("w", a),
                And(a,b), Or(a,b), Impl(a,b), Eq(a,b)
            );

            t.compare(
                fs.stream().map(f -> f.variables())
                    .collect(Collectors.toList()),
                Stream.concat(
                    Collections.nCopies(3, S("w", "x", "y")).stream(),
                    Collections.nCopies(4, S("w", "x", "y", "z")).stream()
                ).collect(Collectors.toList()),
                "variables in formulas with quantified subfs"
            );
            t.compare(
                fs.stream().map(f -> f.constants())
                    .collect(Collectors.toList()),
                Stream.concat(
                    Stream.generate(() -> S("c")).limit(3),
                    Stream.generate(() -> S("c", "d")).limit(4)
                ).collect(Collectors.toList()),
                "constants in formulas with quantified subfs"
            );
            t.compare(
                fs.stream().map(f -> f.functions())
                    .collect(Collectors.toList()),
                Stream.generate(() -> S("f")).limit(fs.size())
                    .collect(Collectors.toList()),
                "functions in formulas with quantified subfs"
            );
            t.compare(
                fs.stream().map(f -> f.predicates())
                    .collect(Collectors.toList()),
                Stream.concat(
                    Stream.generate(() -> S("p")).limit(3),
                    Stream.generate(() -> S("p", "q")).limit(4)
                ).collect(Collectors.toList()),
                "predicates in formulas with quantified subfs"
            );
        });

        t.testCase("FormulaEquals", s -> {
            t.compare(P("p", V("x")), P("p", V("x")), "p(x) == p(x)");
            t.compare(P("p", C("c"), V("x")), P("p", C("c"), V("x")), "p(c,x) == p(c,x)");
            t.compare(Eq(C("c"), V("x")), Eq(C("c"), V("x")), "c=x == c=x");
            t.compare(Eq(F("f",C("c"), F("g",V("x"))),V("y")), Eq(F("f",C("c"), F("g",V("x"))),V("y")), "f(c,g(x))=y == f(c,g(x))=y");
            t.compare(Neg(P("p", V("x"))), Neg(P("p", V("x"))), "-p(x) == -p(x)");
            t.compare(
                And(P("p", V("x")), P("q", C("c"))),
                And(P("p", V("x")), P("q", C("c"))),
                "p(x)&q(c) == p(x)&q(c)"
            );
            t.compare(
                Or(P("p", V("x")), P("q", C("c"))),
                Or(P("p", V("x")), P("q", C("c"))),
                "p(x)|q(c) == p(x)|q(c)"
            );
            t.compare(
                Impl(P("p", V("x")), P("q", C("c"))),
                Impl(P("p", V("x")), P("q", C("c"))),
                "p(x)->q(c) == p(x)->q(c)"
            );
            t.compare(
                Eq(P("p", V("x")), P("q", C("c"))),
                Eq(P("p", V("x")), P("q", C("c"))),
                "p(x)<->q(c) == p(x)<->q(c)"
            );
            t.compare(
                Fa("x", P("p", V("x"))),
                Fa("x", P("p", V("x"))),
                "Vx p(x) == Vx p(x)"
            );
            t.compare(
                Ex("x", P("p", V("x"))),
                Ex("x", P("p", V("x"))),
                "Ex p(x) == Ex p(x)"
            );

            t.testNotEqual(P("p", V("x")), P("p", V("y")));
            t.testNotEqual(P("p", V("x")), P("q", V("x")));
            t.testNotEqual(P("p", V("x")), P("p", C("c")));
            t.testNotEqual(P("p", V("x"), V("y")), P("p", V("x")));
            t.testNotEqual(P("p", V("x"), V("y")), P("p", V("y"), V("x")));
            t.testNotEqual(Eq(C("c"), V("x")), P("p", C("c"), V("x")));
            t.testNotEqual(Eq(C("c"), V("x")), Eq(V("x"), C("c")));
            t.testNotEqual(Neg(P("p", V("x"))), P("p", V("x")));
            t.testNotEqual(
                And(P("p", V("x")), P("q", C("c"))),
                And(P("q", C("c")), P("p", V("x")))
            );
            t.testNotEqual(
                And(P("p", V("x")), P("q", C("c"))),
                Or( P("p", V("x")), P("q", C("c")))
            );
            t.testNotEqual(
                Fa("x", P("p", V("x"))),
                Ex("x", P("p", V("x")))
            );
            t.testNotEqual(
                Fa("x", P("p", V("x"))),
                Ex("y", P("p", V("x")))
            );
            t.testNotEqual(
                Fa("x", P("p", V("x"))),
                Ex("x", P("p", V("y")))
            );
            t.testNotEqual(
                Fa("x", P("p", V("x"))),
                Ex("y", P("p", V("x")))
            );
            t.testNotEqual(
                Fa("x", P("p", V("x"))),
                Ex("x", P("p", V("y")))
            );
            t.testNotEqual(And(), Or());
            t.testNotEqual(Neg(And()), Neg(Or()));
            t.testNotEqual(
                P("p", V("x"), V("y")),
                P("p", V("x,y"))
            );
            t.testNotEqual(
                Ex("x", Neg(P("p", V("x"), V("y")))),
                Ex("x", Neg(P("p", V("x,y"))))
            );
            t.testNotEqual(
                Fa("x", Neg(And(P("p", V("x")),P("p", V("y")),P("p", V("z"))))),
                Fa("x", Neg(And(P("p", V("x)&p(y")),P("p", V("z")))))
            );
            t.testNotEqual(
                Fa("x", Neg(And(P("p", V("x")),P("p", V("y)&p(z"))))),
                Fa("x", Neg(And(P("p", V("x)&p(y")),P("p", V("z")))))
            );
        });

        t.testCase("FormulaToString", s -> {
            Formula a = Fa("x", P("p", C("c"), F("f", V("x"), V("y")), V("w")));
            Formula b = Ex("y", P("q", V("y"), C("d"), V("z")));
            List<FormulaStr> formulas = Arrays.asList(
                FS(
                    Neg(P("p", F("f", V("x"), C("c")))), "-p(f(x,c))"
                ),
                FS(
                    Neg(Eq(F("f", V("x"), C("c")),F("g", V("y")))), "-f(x,c)=g(y)"
                ),

                FS(
                    And(  P("p", F("f", V("x"), C("c"))), P("p", F("f", C("c"), V("x")))  ),
                    "(p(f(x,c))&p(f(c,x)))"
                ),
                FS(
                    Or(  P("p", F("f", V("x"), C("c"))), P("p", F("f", C("c"), V("x")))  ),
                    "(p(f(x,c))|p(f(c,x)))"
                ),
                FS(
                    Impl( P("p", F("f", V("x"), C("c"))), P("p", F("f", C("c"), V("x"))) ),
                    "(p(f(x,c))->p(f(c,x)))"
                ),
                FS(
                    Eq( P("p", F("f", V("x"), C("c"))), P("p", F("f", C("c"), V("x"))) ),
                    "(p(f(x,c))<->p(f(c,x)))"
                ),
                FS(
                    Or(
                        Neg(Impl(P("p", F("f", V("x"), C("c"))),P("p", F("f", C("c"), V("x"))))),
                        Neg(Impl(P("p", F("f", C("c"), V("x"))),P("p", F("f", V("x"), C("c")))))
                    ),
                    "(-(p(f(x,c))->p(f(c,x)))|-(p(f(c,x))->p(f(x,c))))"
                ),
                FS(
                    And(
                        Impl(P("p", F("f", V("x"), C("c"))),P("p", F("f", C("c"), V("x")))),
                        Impl(Neg(P("p", F("f", V("x"), C("c")))), P("q", V("z")))
                    ),
                    "((p(f(x,c))->p(f(c,x)))&(-p(f(x,c))->q(z)))"
                ),
                FS(
                    Eq(
                        And(
                            P("p", F("f", V("x"), C("c"))),
                            Neg(P("p", F("f", C("c"), V("x"))))
                        ),
                        Or(
                            P("p", F("f", V("x"), C("c"))),
                            Impl(
                                P("p", F("f", C("c"), V("x"))),
                                P("p", F("f", V("x"), C("c")))
                            )
                        )
                    ),
                    "((p(f(x,c))&-p(f(c,x)))<->(p(f(x,c))|(p(f(c,x))->p(f(x,c)))))"
                ),
                FS( Fa("x", P("p", V("x"))), "∀x p(x)"),
                FS( Ex("x", P("p", V("x"))), "∃x p(x)"),
                FS( Fa("x", And(P("p", V("x")), P("q"))), "∀x (p(x)&q())"),
                FS( Ex("x", And(P("p", V("x")), P("q"))), "∃x (p(x)&q())"),
                FS( Neg(a), "-∀x p(c,f(x,y),w)"),
                FS( And(a,b), "(∀x p(c,f(x,y),w)&∃y q(y,d,z))"),
                FS( Or(a,b), "(∀x p(c,f(x,y),w)|∃y q(y,d,z))"),
                FS( Impl(a,b), "(∀x p(c,f(x,y),w)->∃y q(y,d,z))"),
                FS( Eq(a,b), "(∀x p(c,f(x,y),w)<->∃y q(y,d,z))"),
                FS( Fa("y", a), "∀y ∀x p(c,f(x,y),w)"),
                FS( Ex("w", a), "∃w ∀x p(c,f(x,y),w)")
            );
            for (FormulaStr fs : formulas) {
                t.compare(fs.f.toString(), fs.s, fs.s);
            }
        });

        t.testCase("FormulaIsSatisfied", new Consumer<Tester.Scope>() {
            int a=1, b=2, c=3, d=4, e=5, mm = 6, x = 7;
            Struct<Integer> m = new Struct<>();
            Map<String, Integer> e1 = M("x", b, "y", c);

            Term bff(Term x) { return F("bff", x); }
            Formula dievca(Term x) { return P("dievca", x); }
            Formula ma_rada(Term x, Term y) { return P("ma_rada", x,y); }
            void testIsSat(boolean sat, Formula f, String s) {
                // FIXME: added protection against m and e1 modification,
                // but this should be explicitly checked
                t.compare(
                    f.isSatisfied(new Struct<>(m), new HashMap<>(e1)),
                    sat, "isSatisfied " + s
                );
            }
            public void accept(Tester.Scope s) {
                m.iC.put("Anicka", a);
                m.iC.put("Betka", b);
                m.iC.put("Cecilka", c);
                m.iC.put("Edo", e);

                m.iP.put("dievca", S(L(a), L(b), L(c), L(d)));
                m.iP.put("ma_rada", S(
                     L(a, b), L(a, c),
                     L(b, a), L(b, d), L(b, mm),
                     L(c, a), L(c, b), L(c, c), L(c, d), L(c, e), L(c, mm),
                     L(d, c), L(d, e),
                     L(e, b),
                     L(mm, mm), L(mm,x)
                ));
                Map<List<Integer>, Integer> bff = new HashMap<>();
                bff.put(L(a), b); bff.put(L(b), a); bff.put(L(c), d); bff.put(L(d), c);
                bff.put(L(e), x); bff.put(L(mm), x); bff.put(L(x), x);
                m.iF.put("bff", bff);
                m.md();
                t.message("m: " + m);
                t.message("e1: " + e1);

                Term Anicka = C("Anicka");
                Term Betka = C("Betka");
                Term Cecilka = C("Cecilka");
                Term Edo = C("Edo");
                Term vx = V("x");
                Term vy = V("y");

                testIsSat( true, dievca(Anicka), "dievca(Anicka)");
                testIsSat(false, dievca(Edo), "dievca(Edo)");
                testIsSat( true, dievca(vx), "dievca(x)");
                testIsSat( true, ma_rada(Anicka, Betka), "ma_rada(Anicka,Betka)");

                testIsSat( true, Eq(Betka, vx), "Betka=x");
                testIsSat(false, Eq(Anicka, Edo), "Anicka=Edo");
                testIsSat(false, Eq(vy,bff(Anicka)), "y=bff(Anicka)");
                testIsSat( true, Eq(bff(Cecilka), bff(bff(bff(vy)))), "bff(Cecilka)=bff(bff(bff(y)))");

                testIsSat(false, Neg(dievca(Anicka)), "-dievca(Anicka)");
                testIsSat( true, Neg(dievca(Edo)), "-dievca(Edo)");
                testIsSat(false, Neg(Neg(Eq(bff(Betka), vy))), "--bff(Betka)=y");

                testIsSat( true, And(dievca(Anicka), ma_rada(Anicka, Betka), ma_rada(Anicka, Cecilka)),
                        "(dievca(Anicka)&ma_rada(Anicka,Betka)&ma_rada(Anicka,Cecilka))");
                testIsSat(false, And(dievca(Anicka), ma_rada(Anicka, Betka), ma_rada(Anicka, Edo)),
                        "(dievca(Anicka)&ma_rada(Anicka,Betka)&ma_rada(Anicka,Edo))");

                testIsSat(true, Or(dievca(Anicka), ma_rada(Anicka, Betka), ma_rada(Anicka, Edo)),
                        "(dievca(Anicka)|ma_rada(Anicka,Betka)|ma_rada(Anicka,Edo))");
                testIsSat(false, Or(dievca(Edo), ma_rada(Anicka, Edo)),
                        "(dievca(Edo)|ma_rada(Anicka,Edo))");

                testIsSat( true, Impl(ma_rada(Anicka, Edo), dievca(Edo)), "(ma_rada(Anicka,Edo)->dievca(Edo))");
                testIsSat( true, Impl(dievca(Anicka), dievca(Betka)), "(dievca(Anicka)->dievca(Betka))");
                testIsSat(false, Impl(dievca(Anicka), dievca(Edo)), "(dievca(Anicka)->dievca(Edo))");

                testIsSat( true, Eq(dievca(Anicka), dievca(Betka)), "(dievca(Anicka)<->dievca(Betka))");
                testIsSat(false, Eq(dievca(Anicka), dievca(Edo)), "(dievca(Anicka)<->dievca(Edo))");

                testIsSat(false, ma_rada(Anicka, Edo), "ma_rada(Anicka,Edo)");
                testIsSat( true, And(ma_rada(Anicka, vx), dievca(vx)), "(ma_rada(Anicka,x)&dievca(x))");
                testIsSat(false, Fa("x", ma_rada(vy, vx)), "∀x ma_rada(y,x)");
                testIsSat( true, Ex("y", And(ma_rada(Betka, vy), Neg(dievca(vy)))), "∃y (ma_rada(Betka,y)&-dievca(y))");
                testIsSat( true, Fa("x", Impl(dievca(vx), ma_rada(vx, bff(vx)))), "∀x (dievca(x)->ma_rada(x,bff(x)))");
                testIsSat( true, Fa("x", Impl(dievca(vx), Ex("y", ma_rada(vx, vy)))), "∀x (dievca(x)->∃y ma_rada(x,y))");
                testIsSat(false, Ex("x", And(dievca(vx), Fa("y", Neg(ma_rada(vx, vy))))), "∃x (dievca(x)&∀y -ma_rada(x,y))");
                testIsSat( true, And(dievca(vx),Ex("x",Neg(dievca(vx)))), "(dievca(x)&∃x -dievca(x))");
                testIsSat( true, And(Ex("x",Neg(dievca(vx))), dievca(vx)), "(∃x -dievca(x)&dievca(x))");
                testIsSat( true, And(dievca(vx),Fa("x",Eq(vx,vx))), "(dievca(x)&∀x x = x)");
                testIsSat( true, And(Fa("x",Eq(vx,vx)), dievca(vx)), "(∀x x = x&dievca(x))");
            }
        });

        t.testCase("FormulaFreeVariables",  new Consumer<Tester.Scope>() {
            Formula p(Term... args) { return P("p", args); }
            Term f(Term... args) { return F("f", args); }
            boolean testFree(Formula f, Set<String> expected) {
                return t.compare(f.freeVariables(), expected, f.toString());
            }
            public void accept(Tester.Scope s) {
                String xn = "x", yn = "y", zn = "z";
                Term c = C("c");

                testFree(p(C("c")), S());
                testFree(p(V(xn)), S(xn));
                testFree(p(V(xn), f(V(zn), C("c"))), S(xn, zn));

                testFree(Fa("x", p(V(xn), V(yn), V(zn))), S(yn, zn));
                testFree(
                    Neg(Fa("x", p(V(xn), V(yn), V(zn)))), S(yn, zn)
                );
                testFree(
                    And(Fa("x", p(V(xn), V(yn), V(zn))), p(V(yn)), p(c)), S(yn, zn)
                );
                testFree(
                    And(Fa("x", p(V(xn), V(yn), V(zn))), p(V(xn)), p(c)), S(xn, yn, zn)
                );
                testFree(
                    Or(Fa("x", p(V(xn), V(yn), V(zn))), p(V(yn)), p(c)), S(yn, zn)
                );
                testFree(
                    Or(Fa("x", p(V(xn), V(yn), V(zn))), p(V(xn)), p(c)), S(xn, yn, zn)
                );
                testFree(
                    Impl(Fa("x", p(V(xn), V(yn), V(zn))), p(V(yn))), S(yn, zn)
                );
                testFree(
                    Impl(Fa("x", p(V(xn), V(yn), V(zn))), p(V(xn))), S(xn, yn, zn)
                );
                testFree(
                    Eq(Fa("x", p(V(xn), V(yn), V(zn))), p(V(yn))), S(yn, zn)
                );
                testFree(
                    Eq(Fa("x", p(V(xn), V(yn), V(zn))), p(V(xn))), S(xn, yn, zn)
                );

                testFree(Ex("x", p(V(xn), V(yn), V(zn))), S(yn, zn));
                testFree(
                    Neg(Ex("x", p(V(xn), V(yn), V(zn)))), S(yn, zn)
                );
                testFree(
                    And(Ex("x", p(V(xn), V(yn), V(zn))), p(V(yn)), p(c)), S(yn, zn)
                );
                testFree(
                    Or(Ex("x", p(V(xn), V(yn), V(zn))), p(V(yn)), p(c)), S(yn, zn)
                );
                testFree(
                    Impl(Ex("x", p(V(xn), V(yn), V(zn))), p(V(yn))), S(yn, zn)
                );
                testFree(
                    Eq(Ex("x", p(V(xn), V(yn), V(zn))), p(V(yn))), S(yn, zn)
                );

                testFree(Fa("x", Ex("y", p(V(xn),V(yn),V(zn)))), S(zn));
                testFree(Ex("x", Fa("y", p(V(xn),V(yn),V(zn)))), S(zn));
                testFree(
                    Fa("x", And(Ex("y", p(V(xn),V(yn),V(zn))), p(V(yn)))),
                    S(yn, zn)
                );
                testFree(
                    Ex("x", And(Fa("y", p(V(xn),V(yn),V(zn))), p(V(yn)))),
                    S(yn, zn)
                );
                testFree(
                    Fa("x", And(Ex("y", p(V(xn),V(yn),V(zn))),
                                Fa("y", p(V(xn),V(yn),V(zn))))),
                    S(zn)
                );
                testFree(
                    Fa("x", And(Ex("y", p(V(xn),V(yn),V(zn))),
                                Fa("y", p(V(xn),V(yn),V(zn))))),
                    S(zn)
                );
                testFree(
                    Fa("x", And(Ex("y", p(V(xn),V(yn),V(zn))),
                                Fa("z", p(V(xn),V(yn),V(zn))))),
                    S(yn, zn)
                );
                testFree(
                    Ex("x", And(Fa("y", p(V(xn),V(yn),V(zn))),
                                Ex("z", p(V(xn),V(yn),V(zn))))),
                    S(yn, zn)
                );
                testFree(
                    And(
                        Neg(Or(
                            Fa("x", Ex("y", p(V(xn),V(yn),V(zn)))),
                            Ex("x", Fa("y", p(V(xn),V(yn),V(zn))))
                        )),
                        Neg(Impl(
                            Fa("y", Fa("y", p(V(xn),V(yn),V(zn)))),
                            Ex("y", Ex("y", p(V(xn),V(yn),V(zn))))
                        )),
                        Fa("y", Eq(
                            Fa("x", p(V(xn),V(yn),V(zn))),
                            Ex("x", p(V(xn),V(yn),V(zn)))
                        ))
                    ),
                    S(xn, zn)
                );
            }
        });

        t.testCase("TermSubstitute", s -> {
            t.testTermSubst("x {x -> a} = a", V("x"), "x", C("a"), C("a"));
            t.testTermSubst("y {x -> a} = y", V("y"), "x", C("a"), V("y"));
            t.testTermSubst("a {x -> x} = a", C("a"), "x", V("x"), C("a"));
            t.testTermSubst("a {x -> x} = a", C("a"), "x", V("x"), C("a"));
            t.testTermSubst("a {a -> b} = a", C("a"), "a", C("b"), C("a"));

            t.testTermSubst("f(y) {y -> g(c)} = f(g(c))",
                F("f", V("y")),
                "y", F("g", C("c")),
                F("f", F("g", C("c")))
            );
            t.testTermSubst("f(y) {y -> g(c)} = f(g(c))",
                F("f", V("y")),
                "f", C("c"),
                F("f", V("y"))
            );
            t.testTermSubst("f(x,y) {x -> a} = f(a,y)",
                F("f", V("x"), V("y")),
                "x", C("a"),
                F("f", C("a"), V("y"))
            );
            t.testTermSubst("f(x,x) {x -> a} = f(a,a)",
                F("f", V("x"), V("x")),
                "x", C("a"),
                F("f", C("a"), C("a"))
            );
            t.testTermSubst("f(x,x) {x -> f(x,y)} = f(f(x,y),f(x,y))",
                F("f", V("x"), V("x")),
                "x", F("f", V("x"), V("y")),
                F("f", F("f", V("x"), V("y")), F("f", V("x"), V("y")))
            );
            t.testTermSubst("f(x,f(g(x),f(y,x))) {x -> a} = f(a,f(g(a),f(y,a)))",
                F("f", V("x"),
                    F("f", F("g", V("x")), F("f", V("y"), V("x")))),
                "x", C("a"),
                F("f", C("a"),
                    F("f", F("g", C("a")), F("f", V("y"), C("a"))))
            );
            t.testTermSubst("f(c) {c -> g(x)} = f(c)",
                F("f", C("c")),
                "c", F("g", C("x")),
                F("f", C("c"))
            );
            t.testTermSubst("f(c) {'f(c)' -> g(x)} = f(c)",
                F("f", C("c")),
                "f(c)", F("g", C("x")),
                F("f", C("c"))
            );
            Term bigterm = F("f",
                V("x"), C("c1"),
                F("g",
                    V("y"),
                    F("h",
                        F("i",
                            F("j", V("u")), F("k", C("c4")),
                            F("l", V("v"))),
                        V("z"), C("c3")
                    ),
                    C("c2")
                )
            );
            Term bigterm_result = F("f",
                V("x"), C("c1"),
                F("g",
                    V("y"),
                    F("h",
                        F("i",
                            F("j", V("u")), F("k", C("c4")),
                            F("l", F("m", V("w")))),
                        V("z"), C("c3")
                    ),
                    C("c2")
                )
            );
            t.testTermSubst("f(x,c1,g(y,h(i(j(u),k(c4),l(v)),z,c3),c2)) " +
                    "{v -> m(w)} = " +
                    "f(x,c1,g(y,h(i(j(u),k(c4),l(m(w)))),z,c3),c2))",
                bigterm,
                "v", F("m", V("w")),
                bigterm_result
            );
            t.compare(
                bigterm.substitute("v", F("m", V("w"))).toString(),
                bigterm_result.toString(),
                "f(x,c1,g(y,h(i(j(u),k(c4),l(v)),z,c3),c2)) " +
                    "{v -> m(w)} = " +
                    "f(x,c1,g(y,h(i(j(u),k(c4),l(m(w)))),z,c3),c2)) " +
                    "(toString compared)"
            );
        });

        t.testCase("FormulaSubstitute",  new Consumer<Tester.Scope>() {
            Formula p(Term... args) { return P("p", args); }
            Term f(Term... args) { return F("f", args); }
            Term g(Term... args) { return F("g", args); }
            Set<String> notCopied(Formula a, Formula b) {
                if (a == b) return Collections.singleton(a.toString());
                Set<String> same = new HashSet<>();
                for (int i = 0; i < a.subfs().size(); ++i)
                    same.addAll(notCopied(a.subfs().get(i), b.subfs().get(i)));
                return same;
            }
            void testSubst(Formula original, Variable var, Term replacement, Formula expected) {
                t.scope(s -> {
                    t.message(String.format("substitution: %s {%s -> %s} = %s", original, var, replacement, expected));
                    Formula result;
                    try {
                        result = original.substitute(var.name(), replacement);
                    }
                    catch (NotApplicableException e) {
                        t.fail("NotApplicableException thrown when substitution is applicable");
                        e.printStackTrace();
                        return;
                    }

                    if (!t.compare(result, expected, String.format("%s {%s -> %s} = %s", original, var, replacement, expected)))
                        return;
                    Set<String> same = notCopied(original, result);
                    if (!same.isEmpty()) {
                        t.fail("formulas were not copied in substitution: " + same);
                    }
                });
            }
            public void accept(Tester.Scope s) {
                Constant c = C("c");
                Variable x = V("x"), y = V("y"), z = V("z");

                testSubst(
                    p(f(x,y)), x, c,
                    p(f(c,y))
                );
                testSubst(
                    Eq(f(x,y),f(c,x)), x, c,
                    Eq(f(c,y),f(c,c))
                );
                testSubst(
                    Neg(p(x,y)), x, c,
                    Neg(p(c,y))
                );
                testSubst(
                    And(p(x), p(y), p(z), p(f(x))), x, c,
                    And(p(c), p(y), p(z), p(f(c)))
                );
                testSubst(
                    Or(p(x), p(y), p(z), p(f(x))), x, c,
                    Or(p(c), p(y), p(z), p(f(c)))
                );
                testSubst(
                    Impl(p(x), p(f(x,y))), x, c,
                    Impl(p(c), p(f(c,y)))
                );
                testSubst(
                    Eq(p(x), p(f(x,y))), x, c,
                    Eq(p(c), p(f(c,y)))
                );
                testSubst(
                    Fa(x, Impl(p(x), p(y))), x, c,
                    Fa(x, Impl(p(x), p(y)))
                );
                testSubst(
                    Fa(x, Impl(p(x), p(y))), y, c,
                    Fa(x, Impl(p(x), p(c)))
                );
                testSubst(
                    Ex(x, Impl(p(x), p(y))), x, c,
                    Ex(x, Impl(p(x), p(y)))
                );
                testSubst(
                    Ex(x, Impl(p(x), p(y))), y, c,
                    Ex(x, Impl(p(x), p(c)))
                );
                testSubst(
                    Fa(x, Impl(p(y), Ex(y, p(y,x)))), y, c,
                    Fa(x, Impl(p(c), Ex(y, p(y,x))))
                );
                testSubst(
                    Ex(x, Impl(p(y), Fa(y, p(y,x)))), y, c,
                    Ex(x, Impl(p(c), Fa(y, p(y,x))))
                );

                testSubst(
                    And(p(x,y), Ex(x, p(x,y))), x, c,
                    And(p(c,y), Ex(x, p(x,y)))
                );

                // this is applicable
                testSubst(Fa(y, Fa(x, p(x))), x, y, Fa(y, Fa(x, p(x))));
                testSubst(Fa(y, Ex(x, p(x))), x, y, Fa(y, Ex(x, p(x))));
                testSubst(Ex(y, Fa(x, p(x))), x, y, Ex(y, Fa(x, p(x))));
                testSubst(Ex(y, Ex(x, p(x))), x, y, Ex(y, Ex(x, p(x))));
                testSubst(
                    Fa(y, Fa(x, p(x,y))), x, y,
                    Fa(y, Fa(x, p(x,y)))
                );
                testSubst(
                    Fa(y, Ex(x, p(x))), x, f(y),
                    Fa(y, Ex(x, p(x)))
                );
                testSubst(
                    Fa(y, Ex(x, p(x,y))), x, f(f(y)),
                    Fa(y, Ex(x, p(x,y)))
                );

                // a more complex case
                testSubst(
                    Ex(y, And(
                        p(g(f(x), x)),
                        Fa(x, And(p(c), p(x)))
                    )),
                    x, g(f(x), c),
                    Ex(y, And(
                        p(g(f(g(f(x),c)), g(f(x),c))),
                        Fa(x, And(p(c), p(x)))
                    ))
                );
            }
        });

        t.testCase("FormulaApplicableSubstitute",  new Consumer<Tester.Scope>() {
            Formula p(Term... args) { return P("p", args); }
            Term f(Term... args) { return F("f", args); }
            Term g(Term... args) { return F("g", args); }
            Formula rodic(Term... args) { return P("rodic", args); }
            void testNonApplicable(Formula original, Variable var, Term replacement) {
                t.scope(s -> {
                    boolean gotException = false;
                    Formula result;
                    try {
                        result = original.substitute(var.name(), replacement);
                        t.message("substitute returned result: " + result);
                    }
                    catch (NotApplicableException e) {
                        gotException = true;
                    }
                    catch (Exception e) {
                        t.message(String.format("substitution: %s {%s -> %s}", original, var, replacement));
                        t.fail("Wrong exception thrown when substitution is not applicable");
                        e.printStackTrace();
                        return;
                    }
                    t.compare(gotException, true,
                        String.format("Substitution {%s -> %s} is not applicable to %s, substitute should throw",
                            var, replacement, original));
                });
            }

            public void accept(Tester.Scope s) {
                Variable x = V("x"), y = V("y");
                testNonApplicable(Ex(y, And(rodic(y, x), Neg(Eq(x, y)))), x, y);
                testNonApplicable(Fa(y, And(rodic(y, x), Neg(Eq(x, y)))), x, y);
                testNonApplicable(Fa(y, And(p(f(x)), Fa(x, p(x)))), x, f(y));
                testNonApplicable(Fa(y, And(p(f(x)), Ex(x, p(x)))), x, f(y));
                testNonApplicable(Ex(y, And(p(f(x)), Fa(x, p(x)))), x, f(y));
                testNonApplicable(Ex(y, And(p(f(x)), Ex(x, p(x)))), x, f(y));
                testNonApplicable(Ex(y, And(p(g(f(x),x)), Ex(x, p(x)))), x, g(f(y), x));
            }
        });

        System.exit(t.status() ? 0 : 1);
    }
}
