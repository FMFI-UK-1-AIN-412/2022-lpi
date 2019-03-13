import java.lang.System;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class StructCase {
    Structure m;
    boolean result;

    StructCase(Structure m, boolean result) {
        this.m = m;
        this.result = result;
    }
}

class Case {
    Formula formula;
    String string;
    int deg;
    Set<String> constants;
    Set<String> predicates;
    Set<AtomicFormula> atoms;
    List<StructCase> structs;

    Case(Formula f, String s, int d, Set<String> cons, Set<String> preds, Set<AtomicFormula> atoms, List<StructCase> structs) {
        this.formula = f;
        this.string = s;
        this.deg = d;
        this.constants = cons;
        this.predicates = preds;
        this.atoms = atoms;
        this.structs = structs;
    }
}

class Struct implements Structure {
    public Set<String> domain;
    public Map<String, String> iC;
    public Map<String, Set<List<String>>> iP;
    Struct() {
        domain = new HashSet<>();
        iC = new HashMap<>();
        iP = new HashMap<>();
    }
    Struct(Set<String> domain, Map<String, String> iC, Map<String, Set<List<String>>> iP) {
        this.domain = domain; this.iC = iC; this.iP = iP;
    }
    Struct(Map<String, String> iC, Map<String, Set<List<String>>> iP) {
        this.domain = new HashSet<>(); this.iC = iC; this.iP = iP;
        md();
    }
    public void md() {
        domain.addAll(iC.values());
        for (Set<List<String>> ps : iP.values()) {
            for (List<String> l : ps) {
                domain.addAll(l);
            }
        }
    }
    @Override
    public Set<String> domain() { return domain; }
    @Override
    public String iC(String name) { return iC.get(name); }
    @Override
    public Set<List<String>> iP(String name) { return iP.get(name); }

    @Override
    public String toString() {
        return "{ D=" + domain + "; iC=" + iC + "; iP=" + iP + "; }";
    }
}
public class FormulaTest {

    static Constant C(String c) { return new Constant(c); }
    static PredicateAtom PA(String p, Constant... ts) { return new PredicateAtom(p, Arrays.asList(ts)); }
    static PredicateAtom PA(String p, String... ts) { return new PredicateAtom(p, Arrays.stream(ts).map(s -> C(s)).collect(Collectors.toList())); }
    static PredicateAtom p(Constant... ts) { return new PredicateAtom("Ppp", Arrays.asList(ts)); }
    static PredicateAtom p(String... ts) { return new PredicateAtom("Ppp", Arrays.stream(ts).map(s -> C(s)).collect(Collectors.toList())); }
    static Negation Neg(Formula f) { return new Negation(f); }
    static Conjunction And(Formula... fs) { return new Conjunction(Arrays.asList(fs)); }
    static Disjunction Or(Formula... fs) { return new Disjunction(Arrays.asList(fs)); }
    static Implication Impl(Formula l, Formula r) { return new Implication(l, r); }
    static Equivalence Eq(Formula l, Formula r) { return new Equivalence(l, r); }
    static Case CS(Formula f, String s, int d, Set<String> cons, Set<String> preds, Set<AtomicFormula> atoms, List<StructCase> structs) {
        return new Case(f, s, d, cons, preds, atoms, structs);
    }
    static StructCase SC(Structure m, boolean r) { return new StructCase(m, r); }
    @SafeVarargs
    static <T> List<T> L(T... ts) { return Arrays.asList(ts); }
    @SafeVarargs
    static <T> Set<T> S(T... ts) { return new HashSet<T>(Arrays.asList(ts)); }
    static <K,V> Map<K,V> M() { return Map.of(); }
    static <K,V> Map<K,V> M(K k1, V v1) {return Map.of(k1, v1); }
    static <K,V> Map<K,V> M(K k1, V v1, K k2, V v2) { return Map.of(k1, v1, k2, v2); }
    static <K,V> Map<K,V> M(K k1, V v1, K k2, V v2, K k3, V v3) { return Map.of(k1, v1, k2, v2, k3, v3); }


    static class IsTrueCase {
        public Formula f;
        public boolean isTrue;
        IsTrueCase(Formula f, boolean isTrue) { this.f = f; this.isTrue = isTrue; }
    };
    static IsTrueCase ITC(Formula f, boolean isTrue) { return new IsTrueCase(f, isTrue); }

    static void testIsTrue(Tester t, Structure m, List<IsTrueCase> cases) {
        t.scope("isTrue m: " + m.toString(), s -> {
            for (IsTrueCase c : cases) {
                t.compare(c.f.isTrue(m), c.isTrue, "isTrue " + c.f.toString() + " m: " + m.toString());
            }
        });
    }
    static void testIsTrue(Tester t, Structure m, Formula f, boolean isTrue) {
        testIsTrue(t, m, L(ITC(f, isTrue)));
    }
    public static boolean test() {
        Tester t = new Tester();
        t.setQuiet(true);

        try {
            String cn = "ccc", dn = "ddd", pn = "Ppp";
            Constant c = C(cn), d = C(dn);
            Map<String, String> iC = M(cn, "1", dn, "2");

            t.testCase("Constant", s -> {
                t.compare(c.name(), cn, "name");
                t.compare(c.toString(), cn, "toString");

                t.compare(c.equals(c), true, "c.equals(c)");
                t.compare(c.equals(C("ccc")), true, "c.equals(Constant(ccc))");
                t.compare(c.equals(C("bbb")), false, "!c.equals(Constant(bbb))");
            });

            t.testCase("PredicateAtom", s -> {
                PredicateAtom pcd = p(c, d);

                t.compare(pcd.name(), pn, "name");
                t.compare(pcd.arguments(), L(c, d), "arguments");
                t.compare(pcd.subfs(), Collections.emptyList(), "subfs");
                t.compare(pcd.toString(), String.format("%s(%s,%s)", pn, cn, dn), "toString");

                t.compare(pcd.equals(pcd), true, "p.equals(pcd)");
                t.compare(pcd.equals(PA("Ppp", "ccc", "ddd")), true, "p.equals(Ppp(ccc,ddd))");
                t.compare(pcd.equals(PA("Qqq", "ccc", "ddd")), false, "!p.equals(Qqq(ccc,ddd))");
                t.compare(pcd.equals(PA("Ppp", "ddd", "ddd")), false, "!p.equals(Ppp(ddd,ddd))");
                t.compare(pcd.equals(PA("Ppp", "ccc", "ccc")), false, "!p.equals(Ppp(ccc,ccc))");
                t.compare(pcd.equals(PA("Ppp", "ccc")), false, "!p.equals(Ppp(ccc))");
                t.compare(pcd.equals(PA("Ppp", "ddd")), false, "!p.equals(Ppp(ddd))");
                t.compare(p(c).equals(p(c)), true, "p(c).equals(p(c))");

                t.compare(PA("P", "a,b", "c").equals(PA("P","a", "b,c")), false,
                    "P('a,b',c) equals P(a,'b,c')");

                t.compare(pcd.atoms(), S(pcd), "atoms");
            });

            t.testCase("Negation", s -> {
                Formula pc = p(c);
                Negation n = Neg(pc);

                t.compareRef(n.originalFormula(), pc, "originalFormula");
                t.compare(n.toString(), String.format("-%s(%s)", pn, cn), "toString");

                t.compare(n.equals(n), true, "n.equals(n)");
                t.compare(n.equals(Neg(pc)), true, "n.equals(neg(pc))");
                t.compare(n.equals(Neg(p(c))), true, "n.equals(neg(p(c)))");
                t.compare(n.equals(Neg(p(d))), false, "!n.equals(neg(p(d)))");
                t.compare(n.equals(p(c)), false, "!n.equals(p(c))");

                t.compare(n.equals(PA("-Ppp", "ccc")), false, "-P(c) equals '-P'(c)");

                t.compare(n.subfs(), L(pc), "subfs"); // TODO compare refs inside list
                t.compare(n.atoms(), S(pc), "atoms");

            });

            t.testCase("Disjunction2", s -> {
                PredicateAtom pc = p(c);
                PredicateAtom pd = p(d);
                Disjunction dis = Or(pc, pd);

                t.compare(dis.toString(), String.format("(%s(%s)|%s(%s))",pn,cn,pn,dn), "toString");

                t.compare(dis.equals(dis), true, "dis.equals(d)");
                t.compare(dis.equals(Or(pc, pd)), true, "eq1");
                t.compare(dis.equals(Or(p(c), pd)), true, "eq2");
                t.compare(dis.equals(Or(pc, p(d))), true, "eq3");
                t.compare(dis.equals(Or(p(c), p(d))), true, "eq4");
                t.compare(dis.equals(Or(pc, p(c))), false, "dis.equals(or(pc, pc))");
                t.compare(dis.equals(Or()), false, "dis.equals(or())");
                t.compare(dis.equals(Or(pc, pd, pc)), false, "dis.equals(or(pc, pd, pc))");

                t.compare(dis.subfs(), L(pc, pd), "subfs");
                t.compare(dis.atoms(), S(pc, pd), "atoms");

            });

            t.testCase("Disjunction1", s -> {
                PredicateAtom pc = p(c);
                Disjunction dis = Or(pc);

                t.compare(dis.toString(), String.format("(%s(%s))",pn,cn), "toString");

                t.compare(dis.equals(dis), true, "dis.equals(d)");
                t.compare(dis.equals(Or(pc)), true, "eq1");
                t.compare(dis.equals(Or(p(c))), true, "eq2");
                t.compare(dis.equals(Or(pc, p(d))), false, "eq3");
                t.compare(dis.equals(Or(p(c), p(d))), false, "eq4");

                t.compare(dis.subfs(), L(pc), "subfs");
                t.compare(dis.atoms(), S(pc), "atoms");
            });

            t.testCase("Disjunction0", s -> {
                PredicateAtom pc = p(c);
                Disjunction dis = Or();

                t.compare(dis.toString(), "()", "toString");

                t.compare(dis.equals(dis), true, "dis.equals(d)");
                t.compare(dis.equals(Or()), true, "eq1");
                t.compare(dis.equals(Or(pc)), false, "eq1");
                t.compare(dis.equals(Or(pc, p(d))), false, "eq3");
                t.compare(dis.equals(Or(p(c), p(d))), false, "eq4");
                t.compare(Or().equals(And()), false, "Disjunction() equals Conjuction()");

                t.compare(dis.subfs(), L(), "subfs");
                t.compare(dis.atoms(), S(), "atoms");
            });

            t.testCase("Conjunction2", s -> {
                PredicateAtom pc = p(c);
                PredicateAtom pd = p(d);
                Conjunction con = And(pc, pd);

                t.compare(con.toString(), String.format("(%s(%s)&%s(%s))",pn,cn,pn,dn), "toString");

                t.compare(con.equals(con), true, "con.equals(d)");
                t.compare(con.equals(And(pc, pd)), true, "eq1");
                t.compare(con.equals(And(p(c), pd)), true, "eq2");
                t.compare(con.equals(And(pc, p(d))), true, "eq3");
                t.compare(con.equals(And(p(c), p(d))), true, "eq4");
                t.compare(con.equals(And(pc, p(c))), false, "con.equals(and(pc, pc))");
                t.compare(con.equals(And()), false, "con.equals(and())");
                t.compare(con.equals(And(pc, pd, pc)), false, "con.equals(and(pc, pd, pc))");

                t.compare(con.subfs(), L(pc, pd), "subfs");
                t.compare(con.atoms(), S(pc, pd), "atoms");
            });

            t.testCase("Conjunction1", s -> {
                PredicateAtom pc = p(c);
                Conjunction con = And(pc);

                t.compare(con.toString(), String.format("(%s(%s))",pn,cn), "toString");

                t.compare(con.equals(con), true, "con.equals(d)");
                t.compare(con.equals(And(pc)), true, "eq1");
                t.compare(con.equals(And(p(c))), true, "eq2");
                t.compare(con.equals(And(pc, p(d))), false, "eq3");
                t.compare(con.equals(And(p(c), p(d))), false, "eq4");

                t.compare(con.subfs(), L(pc), "subfs");
                t.compare(con.atoms(), S(pc), "atoms");
            });

            t.testCase("Conjunction0", s -> {
                Conjunction con = And();

                t.compare(con.toString(), "()", "toString");

                t.compare(con.equals(con), true, "con.equals(d)");
                t.compare(con.equals(And(p(c))), false, "eq1");
                t.compare(con.equals(And(p(c), p(d))), false, "eq2");
                t.compare(And().equals(Or()), false, "Conjunction() equals Disjuction()");

                t.compare(con.subfs(), L(), "subfs");
                t.compare(con.atoms(), S(), "atoms");
            });

            t.testCase("Implication", s -> {
                PredicateAtom pc = p(c);
                PredicateAtom pd = p(d);
                Implication impl = Impl(pc, pd);

                t.compareRef(impl.leftSide(), pc, "leftSide");
                t.compareRef(impl.rightSide(), pd, "rightSide");

                t.compare(impl.toString(), String.format("(%s(%s)->%s(%s))",pn,cn,pn,dn), "toString");

                t.compare(impl.equals(impl), true, "impl.equals(d)");
                t.compare(impl.equals(Impl(pc, pd)), true, "eq1");
                t.compare(impl.equals(Impl(p(c), pd)), true, "eq2");
                t.compare(impl.equals(Impl(pc, p(d))), true, "eq3");
                t.compare(impl.equals(Impl(p(c), p(d))), true, "eq4");
                t.compare(impl.equals(Impl(pc, p(c))), false, "impl.equals(and(pc, pc))");
                t.compare(impl.equals(And(PA("Ppp", "ccc)->Ppp(ddd"))), false,
                    "(P(c)->p(d)) equals  Conj(P('c->p(d')");

                t.compare(impl.subfs(), L(pc, pd), "subfs");
                t.compare(impl.atoms(), S(pc, pd), "atoms");
            });

            t.testCase("Equivalence", s -> {
                PredicateAtom pc = p(c);
                PredicateAtom pd = p(d);
                Equivalence eq = Eq(pc, pd);

                t.compareRef(eq.leftSide(), pc, "leftSide");
                t.compareRef(eq.rightSide(), pd, "rightSide");

                t.compare(eq.toString(), String.format("(%s(%s)<->%s(%s))",pn,cn,pn,dn), "toString");

                t.compare(eq.equals(eq), true, "eq.equals(d)");
                t.compare(eq.equals(Eq(pc, pd)), true, "eq1");
                t.compare(eq.equals(Eq(p(c), pd)), true, "eq2");
                t.compare(eq.equals(Eq(pc, p(d))), true, "eq3");
                t.compare(eq.equals(Eq(p(c), p(d))), true, "eq4");
                t.compare(eq.equals(Eq(pc, p(c))), false, "eq.equals(eq(pc, pc))");
                t.compare(eq.equals(And(PA("Ppp", "ccc)<->Ppp(ddd"))), false,
                    "(P(c)<->P(d)) equals  Conj(P('c->P(d')");

                t.compare(eq.subfs(), L(pc, pd), "subfs");
                t.compare(eq.atoms(), S(pc, pd), "atoms");
            });

            testFormulas(t);
        }
        catch (Throwable e) {
            t.fail("Exception: " + e.toString());
            e.printStackTrace();
        }
        return t.status();
    }

    static Case[] cases() {
        Map<String, String> iC1 = M("a", "a");
        Map<String, String> iC2 = M("a", "a", "b", "b");
        Map<String, String> iC3 = M("a", "a", "b", "b", "c", "c");
        Struct m1_ = new Struct(iC1, M("p", S()));
        Struct m1a = new Struct(iC1, M("p", S(L("a"))));
        Struct m2__ = new Struct(iC2, M("p", S()));
        Struct m2a_ = new Struct(iC2, M("p", S(L("a"))));
        Struct m2_b = new Struct(iC2, M("p", S(L("b"))));
        Struct m2ab = new Struct(iC2, M("p", S(L("a"), L("b"))));
        Struct m3___ = new Struct(iC3, M("p", S()));
        Struct m3_b_ = new Struct(iC3, M("p", S(L("b"))));
        Struct m3__c = new Struct(iC3, M("p", S(L("c"))));
        Struct m3_bc = new Struct(iC3, M("p", S(L("b"),L("c"))));
        Struct m3a__ = new Struct(iC3, M("p", S(L("a"))));
        Struct m3ab_ = new Struct(iC3, M("p", S(L("a"),L("b"))));
        Struct m3a_c = new Struct(iC3, M("p", S(L("a"),L("c"))));
        Struct m3abc = new Struct(iC3, M("p", S(L("a"),L("b"),L("c"))));

        PredicateAtom a = PA("p", "a");
        PredicateAtom b = PA("p", "b");
        PredicateAtom c = PA("p", "c");
        return new Case[] {
            CS(a, "p(a)", 0, S("a"), S("p"), S(a), L(
                SC(m1_, false),
                SC(m1a, true)
            )),

            CS(new Negation(a), "-p(a)", 1, S("a"), S("p"), S(a),
                L(SC(m1_, true), SC(m1a, false))
            ),
            CS(new Conjunction( L(a, b) ), "(p(a)&p(b))", 1, S("a", "b"), S("p"), S(a,b), L(
                SC(m2__, false),
                SC(m2a_, false),
                SC(m2_b, false),
                SC(m2ab, true)
            )),
            CS(new Disjunction( L(a, b) ), "(p(a)|p(b))", 1, S("a", "b"), S("p"), S(a,b), L(
                SC(m2__, false),
                SC(m2a_, true),
                SC(m2_b, true),
                SC(m2ab, true)
            )),
            CS(new Implication( a, b ), "(p(a)->p(b))", 1, S("a", "b"), S("p"), S(a,b), L(
                SC(m2__, true),
                SC(m2a_, false),
                SC(m2_b, true),
                SC(m2ab, true)
            )),
            CS(new Equivalence( a, b ), "(p(a)<->p(b))", 1, S("a", "b"), S("p"), S(a,b), L(
                SC(m2__, true),
                SC(m2a_, false),
                SC(m2_b, false),
                SC(m2ab, true)
            )),
            CS(new Disjunction(L(
                    new Negation(new Implication(a, b)),
                    new Negation(new Implication(b, a))
                )), "(-(p(a)->p(b))|-(p(b)->p(a)))", 5, S("a", "b"), S("p"), S(a,b), L(
                SC(m2__, false),
                SC(m2a_, true),
                SC(m2_b, true),
                SC(m2ab, false)
            )),
            CS(new Conjunction(L(
                    new Implication(a, b),
                    new Implication(new Negation(a), c)
                )), "((p(a)->p(b))&(-p(a)->p(c)))", 4, S("a", "b", "c"), S("p"), S(a,b,c), L(
                SC(m3___, false),
                SC(m3a__, false),
                SC(m3_b_, false),
                SC(m3ab_, true),
                SC(m3__c, true),
                SC(m3a_c, false),
                SC(m3_bc, true),
                SC(m3abc, true)
            )),
            CS(new Equivalence(
                    new Conjunction(L( a, new Negation(b) )),
                    new Disjunction(L( a, new Implication( b, a) ))
                ), "((p(a)&-p(b))<->(p(a)|(p(b)->p(a))))", 5, S("a", "b"), S("p"), S(a,b), L(
                    SC(m2__, false),
                    SC(m2a_, true),
                    SC(m2_b, true),
                    SC(m2ab, false)
            )),
        };
    }

    static void testFormulas(Tester t) {
        Case[] cases = cases();

        t.testCase("toString", s -> {
            for (Case c : cases) {
                t.compare(c.formula.toString(), c.string, c.string);
            }
        });

        t.testCase("atoms", s -> {
            for (Case c : cases) {
                t.compare(c.formula.atoms(), c.atoms, c.string);
            }
        });
    }
}
