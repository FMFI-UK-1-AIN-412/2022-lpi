import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

class BadTableauException extends Exception {
    Tableau t;
    BadTableauException(Tableau t, String msg) { super(msg); this.t = t; }
}

public class TableauTest {
    int size = 0;
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

            SignedFormula.Type typeT = f.signedType(true);
            SignedFormula.Type typeF = f.signedType(false);
            List<SignedFormula> sfTs = f.signedSubf(true);
            List<SignedFormula> sfFs = f.signedSubf(false);

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

    int size(Node node) {
        return 1 + node.children().stream().mapToInt(n->size(n)).sum();
    }

    int size(Tableau t) {
        return size(t.root());
    }

    String openClosed(boolean isClosed) {
        return isClosed ? "closed" : "open";
    }

    void testTableauStructure(
        Tableau t,
        Set<SignedFormula> initials
    ) throws BadTableauException {
        if (t.root() == null)
            throw new BadTableauException(t, "Tableau is empty!");
        testTableauStructure(t, t.root(), new ArrayDeque<Node>(), initials);
    }

    void testTableauStructure(
        Tableau t,
        Node node,
        Deque<Node> ancestors,
        Set<SignedFormula> initials
    ) throws BadTableauException {
        Node src = node.source();
        if (src == null) {
            if (!initials.contains(node.sf()))
                throw new BadTableauException(t, String.format(
                    "Node (%d) has no source and is not one of initial formulas: ",
                    node.number()));
        }
        else if (!ancestors.contains(src)) {
            throw new BadTableauException(t, String.format(
                "Node (%d) has source (%d) which is not it's ancestor.",
                node.number(), src.number()));
        }
        else {
            Node parent = ancestors.peek();
            if (!src.sf().subf().contains(node.sf()))
                throw new BadTableauException(t, String.format(
                    "Node (%d) doesn't contain a subformula of it's source (%d)."
                    + "\n [ node.sf: %s , source.sf: %s ]",
                    node.number(), src.number(), node.sf().toString(), src.sf().subf().toString()));

            SignedFormula.Type type = src.sf().type();
            if (type == Alpha && parent.children().size() != 1)
                throw new BadTableauException(t, String.format(
                    "Node (%d) is a result of Alpha rule for (%d): it must not have siblings.",
                    node.number(), src.number()));

            if (type == Beta) {
                if (parent.children().size() != src.sf().subf().size())
                    throw new BadTableauException(t, String.format(
                        "Node (%d) should have %d siblings because it's a result of Beta rule for (%d).",
                        node.number(), src.sf().subf().size(), src.number()));
                Set<SignedFormula> srcBetas = new HashSet<SignedFormula>(
                    src.sf().subf());
                Set<SignedFormula> realBetas = parent.children().stream()
                    .map(n -> n.sf()).collect(toSet());
                if (!srcBetas.equals(realBetas))
                    throw new BadTableauException(t, String.format(
                        "Children of (%d) do not match the Beta rule for (%d): %s  vs %s",
                        parent.number(), src.number(),
                        realBetas.toString(), srcBetas.toString()));
            }
        }

        Node closedFrom = node.closedFrom();
        if (closedFrom != null) {
            // This is a 'closing' node
            if (!ancestors.contains(closedFrom))
                throw new BadTableauException(t, String.format(
                    "Close pair (%d) for node (%d) is not it's ancestor.",
                    closedFrom.number(), node.number()));
            if (node.sf().sign() != !closedFrom.sf().sign())
                throw new BadTableauException(t, String.format(
                    "Close pair formula signs (T/F) are from (%d, %d).",
                    node.number(), closedFrom.number()));
            if (!node.sf().f().equals(closedFrom.sf().f()))
                throw new BadTableauException(t, String.format(
                    "Close pair formulas do not match (%d, %d).",
                    node.number(), closedFrom.number()));
        }

        ancestors.push(node);

        if (node.children().isEmpty()) {
            if (ancestors.stream().allMatch(n -> n.closedFrom() == null)) {
                // child on an open branch
                // open branch should be complete
                Set<SignedFormula> branch = ancestors
                    .stream()
                    .map(n->n.sf())
                    .collect(toSet())
                ;
                // need to go over nodes to have reference numbers
                for (Node nd : ancestors) {
                    SignedFormula.Type type = nd.sf().type();
                    if (type == Alpha) {
                        List<SignedFormula> missing =
                            nd.sf().subf().stream()
                            .filter(ssf->!branch.contains(ssf))
                            .collect(toList())
                        ;
                        if (!missing.isEmpty())
                            throw new BadTableauException(t, String.format(
                                "Branch ending at (%d) is open but not complete"
                                + " -- (%d) (Alpha) is missing subformula(s) %s.",
                                node.number(), nd.number(), missing.toString()));
                    }
                    else if (type == Beta) {
                        boolean have =
                            nd.sf().subf().stream()
                            .anyMatch(ssf->branch.contains(ssf))
                        ;
                        if (!have)
                            throw new BadTableauException(t, String.format(
                                "Branch ending at (%d) is open but not complete"
                                + " -- (%d) (Beta) is missing at least one of its subformulas %s.",
                                node.number(), nd.number(),Arrays.asList(nd.sf().subf())));
                    }
                }
            }
        } else {
            for (Node child : node.children())
                testTableauStructure(t, child, ancestors, initials);
        }
        ancestors.pop();
    }

    void testTableau(boolean expClosed, SignedFormula[] sfs) {
        t.testCase(Arrays.stream(sfs).map(sf->sf.toString()).collect(joining("; ")), s -> {
            TableauBuilder builder = new TableauBuilder();

            try {
                Tester.TimedResult<Tableau> tr = t.timed(() -> {
                    return builder.build(sfs);
                });
                Tableau tab = tr.result;

                testTableauStructure(tab, new HashSet<SignedFormula>(Arrays.asList(sfs)));
                int tSize = size(tab);
                size += tSize;

                if (tab.isClosed() != expClosed) {
                    throw new BadTableauException(tab,
                        String.format("FAILED: Tableau is %s, but should be %s",
                            openClosed(tab.isClosed()), openClosed(expClosed)));
                }

                t.pass(String.format("PASSED: time: %6d  tableau size: %3d  %s",
                    tr.duration, tSize, openClosed(expClosed)));
            } catch (BadTableauException e) {
                t.fail("FAIlED: Bad tableau: " + e.getMessage());
                System.err.println("=====");
                System.err.println(e.t);
                System.err.println("=====");
            }
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

        return t.tested == t.passed;
    }

    public boolean runBuilderTests() {
        t.catchAny(() -> {
            PredicateAtom a = p("a");
            PredicateAtom b = p("b");
            PredicateAtom c = p("c");
            testTableau(false, SFS(T(a)));

            testTableau(true, SFS(T(a), F(a)));

            testTableau(true, SFS( F(Impl(a, a)) ));

            testTableau(true, SFS( F(Impl(a, a)) ));

            testTableau(true, SFS( F(Or(a, Not(a))) ));

            testTableau(true, SFS( T(a), F(a) ));

            testTableau(true, SFS( T(a), F(a), T(a) ));

            testTableau(true, SFS( T(a), F(a), T(b) ));

            testTableau(false, SFS( T(Or(a,b)), F(a) ));

            testTableau(true, SFS( T(And(a,b)), F(a) ));

            Formula demorgan1 = Eq( Not( And( a, b ) ), Or( Not(a), Not(b) ) );
            testTableau(true, SFS( F(demorgan1) ));

            Formula demorgan2 = Eq( Not( Or( a, b ) ), And( Not(a), Not(b) ) );
            testTableau(true, SFS( F(demorgan2) ));

            Formula demorgan3 = Eq( Not( Or( a, b, c ) ),
                                     And( Not(a), Not(b), Not(c) ) );
            testTableau(true, SFS( F(demorgan3) ));

            Formula contraposition = Eq( Impl(a, b), Impl( Not(b), Not(a) ) );
            testTableau(true, SFS( F(contraposition) ));

            Formula impl_impl_distrib = Impl( Impl(a, Impl(b, c)),
                                      Impl( Impl(a, b), Impl(a, c) ) );
            testTableau(true, SFS( F(impl_impl_distrib) ));

            Formula impl_or = Eq( Impl(a, b), Or( Not(a), b ) );
            testTableau(true, SFS( F(impl_or) ));

            Formula impl_and = Eq( Impl(a, b), Not( And( a, Not(b) ) ) );
            testTableau(true, SFS( F(impl_and) ));

            Formula or_and_distrib = Eq( Or( a, And( b, c ) ),
                                          And( Or( a, b ), Or( a, c ) ) );
            testTableau(true, SFS( F(or_and_distrib) ));

            Formula bad_demorgan1 = Eq( Not( And( a, b ) ), Or( a, b ) );
            testTableau(false, SFS( F(bad_demorgan1) ));

            Formula bad_demorgan2 = Eq( Not( Or( a, b ) ), Or( Not(a), Not(b) ) );
            testTableau(false, SFS( F(bad_demorgan2) ));

            Formula bad_demorgan3 = Eq( Not( Or( a, b, c ) ),
                                     And( Not(a), b, Not(c) ) );
            testTableau(false, SFS( F(bad_demorgan3) ));

            Formula bad_contraposition = Eq( Impl(a, b), Impl( b, a ) );
            testTableau(false, SFS( F(bad_contraposition) ));

            Formula bad_impl_impl_distrib = Impl( Impl(a, Impl(b, c)),
                                      Impl( Impl(b, a), Impl(c, a) ) );
            testTableau(false, SFS( F(bad_impl_impl_distrib) ));

            Formula bad_impl_and = Eq( Impl(a, b), Not( And( Not(a), b ) ) );
            testTableau(false, SFS( F(bad_impl_and) ));

            Formula bad_or_and_distrib = Eq( Or( a, And( b, c ) ),
                                          Or( And( a, b ), And( a, c ) ) );
            testTableau(false, SFS( F(bad_or_and_distrib) ));

            {
                // Keď Katka nakreslí obrazok, je na ňom bud mačka alebo pes. Obrázok mačky
                // Katkin pes vždy hneď roztrhá. Ak jej pes roztrhá obrazok, Katka je
                // smutná. Dokážte, že ak Katka nakreslila obrázok a je šťastná, tak na jej
                // obrázku je pes.
                Formula ax1 = Impl(
                        p("obrazok"),
                        And(
                            Or(p("macka"),p("pes")),
                            Or(Not(p("macka")),Not(p("pes")))
                        )
                    );
                Formula ax2 = Impl(p("macka"), p("roztrha"));
                Formula ax3 = Impl(p("roztrha"), p("smutna"));
                Formula conclusion = Impl(
                                And(  p("obrazok"), Not(p("smutna"))  ),
                                p("pes")
                            );

                Formula cax1 = And( ax1, ax2, ax3 );
                testTableau(true, SFS( T(And(cax1, Not(conclusion))) ));
                testTableau(true, SFS( F(Impl(cax1, conclusion)) ));
                testTableau(true, SFS( T(cax1), F(conclusion) ));
                testTableau(true, SFS( T(ax1), T(ax2), T(ax3), F(conclusion) ));
                testTableau(false, SFS( T(cax1) ));
                testTableau(false, SFS( F(conclusion) ));
            }

            {
                // Bez práce nie sú koláče. Ak niekto nemá ani koláče, ani chleba, tak bude
                // hladný. Na chlieb treba múku. Dokážte, že ak niekto nemá múku a je
                // najedený (nie je hladný), tak pracoval.
                Formula ax1 = Impl(p("kolace"), p("praca"));

                Formula ax2 = Impl(
                        And(Not(p("kolace")),Not(p("chlieb"))),
                        p("hlad")
                    );
                Formula ax3 = Impl(p("chlieb"), p("muka"));

                Formula conclusion = Impl(
                                And(  Not(p("muka")), Not(p("hlad"))  ),
                                p("praca")
                            );

                Formula cax1 = And( ax1, ax2, ax3 );
                testTableau(true, SFS( T(And(cax1, Not(conclusion))) ));
                testTableau(true, SFS( F(Impl(cax1, conclusion)) ));
                testTableau(true, SFS( T(cax1), F(conclusion) ));
                testTableau(true, SFS( T(ax1), T(ax2), T(ax3), F(conclusion) ));
                testTableau(false, SFS( T(cax1) ));
                testTableau(false, SFS( F(conclusion) ));
            }
        });
        return t.status();
    }


    public void selfTest() {
        PredicateAtom a = p("a");
        try {
            SignedFormula[] init = SFS(
                T(Impl(a,a))
            );
            System.err.println();
            System.err.println("Testing " + Arrays.stream(init).map(sf->sf.toString()).collect(joining("; ")));

            Tableau T = new Tableau();
            List<Node> initials = T.addInitial(init);

            /*
            Node n1 = new Node(T(a), T.root());
            T.addNode(initials.get(0), n1);
            Node n2 = new Node(T(a), T.root());
            T.addNode(initials.get(0), n2);
            */
            T.extendBeta(initials.get(0), initials.get(0));


            testTableauStructure(T, new HashSet<SignedFormula>(Arrays.asList(init)));

        } catch (BadTableauException e) {
            System.err.println("FAIlED: Bad tableau: " + e.getMessage());
            System.err.println("=====");
            System.err.println(e.t);
            System.err.println("=====");
        }
    }

    public static void main(String[] args) {
        System.err.println("Testing relevant Formula implementations");
        if (FormulaTest.test()) {
            System.err.println("Formula OK");
            System.err.println();
        } else {
            System.err.println();
            System.err.println("Basic Formula operations (toString, equals, subfs) don't work correctly.");
            System.err.println("It doesn't make sense to run tableau tests.");
            System.err.println();
            System.exit(2);
        }

        System.err.println("Testing signed formulas / tableau");
        TableauTest tt = new TableauTest();
        tt.selfTest();
        boolean signedOk = tt.runSignedTests();
        boolean tableauOk = tt.runTableauTests();
        boolean allOk = tt.runBuilderTests();

        // !signedOk implies !tableauOk
        if (!tableauOk) {
            System.err.println();
            System.err.println("    There were errors in signed formula or tableau methods implementation!");
            System.err.println("    Builder methods tests results are not relevant.");
            System.err.println("    Any PASSED tableaux can be false positives!");
            System.err.println();
        }
        System.exit(allOk ? 0 : 1);
    }
}
