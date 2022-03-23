import javax.swing.plaf.basic.BasicLookAndFeel;
import java.util.*;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class Constant {
    String name;
    public Constant(String name) {
        this.name = name;
    }
    public String name() {
        return this.name;
    }
    public String eval(Structure m) {
        return m.iC(name());
    }
    @Override
    public String toString() {
        return name();
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        Constant otherC = (Constant) other;
        return name().equals(otherC.name());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

class Formula {
    List<Formula> podformuly;

    public List<Formula> subfs() {
        return podformuly;
    }

    @Override
    public String toString() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isTrue(Structure m) {
        throw new RuntimeException("Not implemented");
    }

    public int deg() {
        int n = 1;
        for (int i = 0; i < podformuly.size(); i++){
            n += podformuly.get(i).deg();
        }
        return n;
    }

    public Set<AtomicFormula> atoms() {
        HashSet<AtomicFormula> atoms = new HashSet<>();
        for (Formula f : podformuly) {
            atoms.addAll(f.atoms());
        }
        return atoms;
    }

    public Set<String> constants() {
        HashSet<String> constants = new HashSet<>();
        for (Formula f : podformuly) {
            constants.addAll(f.constants());
        }
        return constants;
    }

    public Set<String> predicates() {
        HashSet<String> predicates = new HashSet<>();
        for (Formula f : podformuly) {
            predicates.addAll(f.predicates());
        }
        return predicates;
    }


    /**
     * Return the type (of a signed formula),
     * as if this was a signed formula signed with sign.
     *
     * @param sign the sign of the signed formula to be considered
     * @return SignedFormula.Type.Alpha or SignedFormula.Type.Beta
     */
    public SignedFormula.Type signedType(boolean sign) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Return a list of signed sub-formulas of this
     * formula, if this formula was signed with sign
     *
     * @param sign the sign of the signed formula to be considered
     * @return list of signed sub-formulas
     */
    public List<SignedFormula> signedSubf(boolean sign) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

class AtomicFormula extends Formula {
    AtomicFormula() {

    }

    @Override
    public int deg() {
        return 0;
    }
}

class PredicateAtom extends AtomicFormula {
    String name;
    List<Constant> constants;

    PredicateAtom(String name, List<Constant> args) {
        this.name = name;
        this.constants = args;
    }

    String name() {
        return name;
    }

    List<Constant> arguments() {
        return constants;
    }

    @Override
    public List<Formula> subfs(){
        return new ArrayList<>();
    }

    @Override
    public Set<AtomicFormula> atoms(){
        return Set.of(this);
    }

    @Override
    public Set<String> constants(){
        HashSet<String> cs = new HashSet<>();
        for (Constant c:constants) {
            cs.add(c.name());
        }
        return cs;
    }

    @Override
    public Set<String> predicates(){
        return Set.of(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("(");
        for (int i = 0; i < constants.size(); i++) {
            sb.append(constants.get(i).name);
            if (i == constants.size() - 1) continue;
            sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isTrue(Structure m) {
        return m.iP(name).contains(constantsCoding(m));
    }

    private List<String> constantsCoding(Structure m){
        List<String> conStrings = constantsStrings();
        List<String> conCoding = new ArrayList<>();
        for (String conString : conStrings) {
            conCoding.add(m.iC(conString));
        }
        return conCoding;
    }

    private List<String> constantsStrings(){
        ArrayList<String> a = new ArrayList<>();
        for (Constant c : constants){
            a.add(c.name());
        }
        return a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredicateAtom that = (PredicateAtom) o;
        return Objects.equals(name, that.name()) && Objects.equals(constantsStrings(), that.constantsStrings());
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        return new ArrayList<>();
    }
}

class Negation extends Formula {
    Negation(Formula originalFormula) {
        podformuly = List.of(originalFormula);
    }

    public Formula originalFormula() {
        return podformuly.get(0);
    }

    @Override
    public String toString() {
        return "-" + originalFormula().toString();
    }

    @Override
    public boolean isTrue(Structure m) {
        return !originalFormula().isTrue(m);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Negation that = (Negation) o;
        return Objects.equals(originalFormula(), that.originalFormula());
    }

    @Override
    public int deg() {
        return originalFormula().deg() + 1;
    }


    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        return List.of(new SignedFormula(!sign,originalFormula()));
    }
}

class Disjunction extends Formula {
    Disjunction(List<Formula> disjuncts) {
        podformuly = disjuncts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < podformuly.size(); i++) {
            sb.append(podformuly.get(i).toString());
            if (i == podformuly.size() - 1) continue;
            sb.append("|");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isTrue(Structure m) {
        boolean isTrue = false;
        for (Formula f : podformuly) {
            if (f.isTrue(m))
                isTrue = true;
        }
        return isTrue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disjunction that = (Disjunction) o;
        return Objects.equals(podformuly,that.podformuly);
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? (SignedFormula.Type.Beta) : (SignedFormula.Type.Alpha);
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        ArrayList<SignedFormula> a = new ArrayList<>();
        for (Formula f:podformuly) {
            a.add(new SignedFormula(sign,f));
        }
        return a;
    }
}

class Conjunction extends Formula {
    Conjunction(List<Formula> conjuncts) {
        podformuly = conjuncts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < podformuly.size(); i++) {
            sb.append(podformuly.get(i).toString());
            if (i == podformuly.size() - 1) continue;
            sb.append("&");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isTrue(Structure m) {
        boolean isTrue = true;
        for (Formula f : podformuly) {
            if (!f.isTrue(m))
                isTrue = false;
        }
        return isTrue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conjunction that = (Conjunction) o;
        return Objects.equals(podformuly, that.podformuly);
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? (SignedFormula.Type.Alpha) : (SignedFormula.Type.Beta);
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        ArrayList<SignedFormula> a = new ArrayList<>();
        for (Formula f:podformuly) {
            a.add(new SignedFormula(sign,f));
        }
        return a;
    }
}

class BinaryFormula extends Formula {
    BinaryFormula(Formula left, Formula right) {
        podformuly = new ArrayList<>();
        podformuly.add(left);
        podformuly.add(right);
    }

    public Formula leftSide() {
        return podformuly.get(0);
    }

    public Formula rightSide() {
        return podformuly.get(1);
    }
}

class Implication extends BinaryFormula {
    Implication(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public String toString() {
        return "(" + leftSide().toString() + "->" + rightSide().toString() + ")";
    }

    @Override
    public boolean isTrue(Structure m) {
        return !leftSide().isTrue(m) || rightSide().isTrue(m);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Implication that = (Implication) o;
        return Objects.equals(leftSide(), that.leftSide()) && Objects.equals(rightSide(), that.rightSide());
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? (SignedFormula.Type.Beta) : (SignedFormula.Type.Alpha);
    }


    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        ArrayList<SignedFormula> a = new ArrayList<>();
        a.add(new SignedFormula(!sign,leftSide()));
        a.add(new SignedFormula(sign,rightSide()));
        return a;
    }
}

class Equivalence extends BinaryFormula {
    Equivalence(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public String toString() {
        return "(" + leftSide().toString() + "<->" + rightSide().toString() + ")";
    }

    @Override
    public boolean isTrue(Structure m) {
        return leftSide().isTrue(m) == rightSide().isTrue(m);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equivalence that = (Equivalence) o;
        return Objects.equals(leftSide(), that.leftSide()) && Objects.equals(rightSide(), that.rightSide()) || Objects.equals(leftSide(), that.rightSide()) && Objects.equals(rightSide(), that.leftSide());
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? (SignedFormula.Type.Alpha) : (SignedFormula.Type.Beta);
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        ArrayList<SignedFormula> a = new ArrayList<>();
        a.add(new SignedFormula(sign,new Implication(leftSide(),rightSide())));
        a.add(new SignedFormula(sign,new Implication(rightSide(),leftSide())));
        return a;
    }
}
