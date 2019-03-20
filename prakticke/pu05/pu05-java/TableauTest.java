import java.lang.RuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;

public class TableauTest {
    Tester t = new Tester();
    private SignedFormula.Type cType(SignedFormula.Type t) {
        return t == SignedFormula.Type.Alpha
            ? SignedFormula.Type.Beta
            : SignedFormula.Type.Alpha
        ;
    }

    public void testSignedForm(
        Formula f,
        SignedFormula.Type expTypeT,
        Set<SignedFormula> expSfsT
    ) {
        t.testCase("signedType/signedSubf " + f.toString(), s -> {

            long start = System.nanoTime();
            SignedFormula.Type typeT = f.signedType(true);
            SignedFormula.Type typeF = f.signedType(false);
            List<SignedFormula> sfTs = f.signedSubf(true);
            List<SignedFormula> sfFs = f.signedSubf(false);
            long end = System.nanoTime();
            long duration = (end - start) / 1000;

            Set<SignedFormula> sfsT = new HashSet<SignedFormula>(sfTs);
            Set<SignedFormula> sfsF = new HashSet<SignedFormula>(sfFs);
            Set<SignedFormula> expSfsF = new HashSet<SignedFormula>();
            for (SignedFormula sf : expSfsT) {
                expSfsF.add(sf.neg());
            }

            if (expTypeT != null) {
                t.compare(typeT, expTypeT,
                    String.format("%s.signedType(true)", f.toString()));
                t.compare(typeF, cType(expTypeT),
                    String.format("%s.signedType(false)", f.toString()));
            }
            t.compare(sfsT, expSfsT,
                String.format("%s.signedSubf(true)", f.toString()));
            t.compare(sfsF, expSfsF,
                String.format("%s.signedSubf(false)", f.toString()));
        });
    }

    static Constant C(String c) { return new Constant(c); }
    static PredicateAtom PA(String p, Constant... ts) { return new PredicateAtom(p, Arrays.asList(ts)); }
    static PredicateAtom PA(String p, String... ts) { return new PredicateAtom(p, Arrays.stream(ts).map(s -> C(s)).collect(Collectors.toList())); }
    static PredicateAtom p(Constant... ts) { return new PredicateAtom("p", Arrays.asList(ts)); }
    static PredicateAtom p(String... ts) { return new PredicateAtom("p", Arrays.stream(ts).map(s -> C(s)).collect(Collectors.toList())); }
    static Negation Not(Formula f) { return new Negation(f); }
    static Conjunction And(Formula... fs) { return new Conjunction(Arrays.asList(fs)); }
    static Disjunction Or(Formula... fs) { return new Disjunction(Arrays.asList(fs)); }
    static Implication Impl(Formula l, Formula r) { return new Implication(l, r); }
    static Equivalence Eq(Formula l, Formula r) { return new Equivalence(l, r); }

    static SignedFormula T(Formula f) { return SignedFormula.T(f); }
    static SignedFormula F(Formula f) { return SignedFormula.F(f); }
    static Set<SignedFormula> SFs(SignedFormula... sfs) {
        return new HashSet<SignedFormula>(Arrays.asList(sfs));
    }
    static SignedFormula[] SFS(SignedFormula... sfs) { return sfs; }
    static List<SignedFormula> LSF(SignedFormula... sfs) { return Arrays.asList(sfs); }
    static final SignedFormula.Type Alpha = SignedFormula.Type.Alpha;
    static final SignedFormula.Type Beta = SignedFormula.Type.Beta;

    public boolean runSignedTests() {
        t.catchAny(() -> {
            PredicateAtom a = p("a");
            PredicateAtom b = p("b");
            PredicateAtom c = p("c");
            PredicateAtom d = p("d");

            testSignedForm(a, null, SFs());
            testSignedForm(Not(a), null, SFs(F(a)));

            testSignedForm(
                And(a, b),
                Alpha,
                SFs( T(a), T(b) )
            );

            testSignedForm(
                Or(a, b),
                Beta,
                SFs( T(a), T(b) )
            );

            testSignedForm(
                And(a, b, c, d),
                Alpha,
                SFs( T(a), T(b), T(c), T(d) )
            );

            testSignedForm(
                Or(a, b, c, d),
                Beta,
                SFs( T(a), T(b), T(c), T(d) )
            );

            testSignedForm(
                Or(a, Not(b), And(c, d)),
                Beta,
                SFs( T(a), T(Not(b)), T(And(c, d)) )
            );

            testSignedForm(
                Impl(a, b),
                Beta,
                SFs( F(a), T(b) )
            );

            testSignedForm(
                Eq(a, b),
                Alpha,
                SFs( T(Impl(a,b)), T(Impl(b,a)) )
            );
        });
        return t.tested == t.passed;
    }

    public boolean runTableauTests() {
        t.catchAny(() -> {
            PredicateAtom a = p("a");
            PredicateAtom b = p("b");
            PredicateAtom c = p("c");

/*
            Node na = new Node(F(a), null);
            Node nb = new Node(T(p("asdfasdfasdf")), na);

            na.addChild(nb);
            na.addChild(new Node(T(c),na));

            System.err.println(na);
            System.err.println(nb);

            System.err.println();
            System.err.println(na.tree());
*/

            t.testCase("addInitial", s -> {
                Tableau T = new Tableau();
                List<Node> added = T.addInitial(SFS( F(Impl(a,b)), T(Impl(a,c))));
                t.compare(
                    added.stream().map(n -> n.sf()).collect(toList()),
                    LSF( F(Impl(a,b)), T(Impl(a,c)) ),
                    "addInitial return"
                );
                t.compareRef(
                    T.root(),
                    added.get(0),
                    "addInitial root node"
                );
                t.compareRef(
                    T.root().children().get(0),
                    added.get(1),
                    "addInitial child"
                );
                t.compare(
                    T.root().sf(), F(Impl(a,b)), "addInitial root node"
                );
                t.compare(
                    T.root().children().stream()
                        .map(n -> n.sf()).collect(toList()),
                    LSF( T(Impl(a, c)) ),
                    "addInitial child(ren) of root node"
                );
                t.compare(
                    T.root().number(),
                    1, "addInitial root number"
                );
                t.compare(
                    T.root().children().get(0).number(),
                    2, "addInitial child number"
                );
                t.compareRef(
                    T.root().source(),
                    null, "addInitial root source"
                );
                t.compareRef(
                    T.root().children().get(0).source(),
                    null, "addInitial second source"
                );
                t.compareRef(
                    T.root().tableau(),
                    T, "addInitial added root to tableau"
                );
                t.compareRef(
                    T.root().children().get(0).tableau(),
                    T, "addInitial added second to tableau"
                );
            });

            t.testCase("extendAlpha", s -> {
                Tableau T = new Tableau();
                List<Node> addedI = T.addInitial(SFS( F(Impl(a,b)), T(Impl(a,c))));

                Node added1 = T.extendAlpha(
                    addedI.get(1),
                    addedI.get(0),
                    0);
                t.compare(added1.sf(), T(a), "extendAlpha first");
                t.compareRef(
                    T.root().children().get(0).children().get(0),
                    added1,
                    "extendAlpha returns added node");

                Node added2 = T.extendAlpha(
                    added1,
                    addedI.get(0),
                    1);
                t.compare(added2.sf(), F(b), "extendAlpha second");
                t.compareRef(
                    T.root().children().get(0).children().get(0).children().get(0),
                    added2,
                    "extendAlpha returns added node");
                t.compare(
                    T.root().children().get(0).children().get(0).number(),
                    3, "extendAlpha first number"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).children().get(0).number(),
                    4, "extendAlpha second number"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).source(),
                    T.root(), "extendAlpha first source"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).children().get(0).source(),
                    T.root(), "extendAlpha second source"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).tableau(),
                    T, "extendAlpha first added to tableau"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).children().get(0).tableau(),
                    T, "extendAlpha second added to tableau"
                );
            });

            t.testCase("extendBeta", s -> {
                Tableau T = new Tableau();
                List<Node> addedI = T.addInitial(SFS( F(Impl(a,b)), T(Impl(a,c))));

                List<Node> added = T.extendBeta(
                    addedI.get(1),
                    addedI.get(1)
                );

                t.compare(
                    added.stream().map(n->n.sf()).collect(toList()),
                    LSF( F(a), T(c) ),
                    "extendBeta children");
                t.compare(
                    T.root().children().get(0).children().size(),
                    2, "number of added children"
                );
                t.compareRef(
                    T.root()
                        .children().get(0)
                        .children().get(0),
                    added.get(0), "extendBeta returns added first child"
                );
                t.compareRef(
                    T.root()
                        .children().get(0)
                        .children().get(1),
                    added.get(1), "extendBeta returns added first child"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).number(),
                    3, "extendBeta first number"
                );
                t.compare(
                    T.root().children().get(0).children().get(1).number(),
                    4, "extendBeta second number"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).source(),
                    T.root().children().get(0), "extendBeta first source"
                );
                t.compare(
                    T.root().children().get(0).children().get(1).source(),
                    T.root().children().get(0), "extendBeta second source"
                );
                t.compare(
                    T.root().children().get(0).children().get(0).tableau(),
                    T, "extendBeta first added to tableau"
                );
                t.compare(
                    T.root().children().get(0).children().get(1).tableau(),
                    T, "extendBeta second added to tableau"
                );
            });
        });

        return t.status();
    }

    public static void main(String[] args) {
        System.err.println("Testing relevant Formula implementations");
        if (FormulaTest.test()) {
            System.err.println("Formula OK");
            System.err.println();
        } else {
            System.err.println();
            System.err.println("Basic Formula operations (toString, equals, subfs) don't work correctly.");
            System.err.println("It doesn't make sense to run Tableau / SignedFormula tests.");
            System.err.println();
            System.exit(2);
        }

        System.err.println("Testing signed formulas / tableau");
        TableauTest tt = new TableauTest();
        boolean signedOk = tt.runSignedTests();
        boolean allOk = tt.runTableauTests();

        if (!signedOk) {
            System.err.println();
            System.err.println("    There were errors in signed formula implementation");
            System.err.println("    Tableau methods tests (addInitial/extendAlpha/Beta) are not relevant");
            System.err.println();
        }
        System.exit(allOk ? 0 : 1);
    }
}
