import java.util.List;
import java.util.Set;

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
    public List<Formula> subfs() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isTrue(Structure m) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean equals(Object other) {
        throw new RuntimeException("Not implemented");
    }

    public int deg() {
        throw new RuntimeException("Not implemented");
    }

    public Set<AtomicFormula> atoms() {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> constants() {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> predicates() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

class AtomicFormula extends Formula {
    AtomicFormula() {
        throw new RuntimeException("Not implemented");
    }
}

class PredicateAtom extends AtomicFormula {
    PredicateAtom(String name, List<Constant> args) {
        throw new RuntimeException("Not implemented");
    }

    String name() {
        throw new RuntimeException("Not implemented");
    }

    List<Constant> arguments() {
        throw new RuntimeException("Not implemented");
    }
}

class EqualityAtom extends AtomicFormula {
    EqualityAtom(Constant left, Constant right) {
        throw new RuntimeException("Not implemented");
    }

    Constant left() {
        throw new RuntimeException("Not implemented");
    }

    Constant right() {
        throw new RuntimeException("Not implemented");
    }
}

class Negation extends Formula {
    Negation(Formula originalFormula) {
        throw new RuntimeException("Not implemented");
    }

    public Formula originalFormula() {
        throw new RuntimeException("Not implemented");
    }
}

class Disjunction extends Formula {
    Disjunction(List<Formula> disjuncts) {
        throw new RuntimeException("Not implemented");
    }
}

class Conjunction extends Formula {
    Conjunction(List<Formula> conjuncts) {
        throw new RuntimeException("Not implemented");
    }
}

class BinaryFormula extends Formula {
    BinaryFormula(Formula left, Formula right) {
        throw new RuntimeException("Not implemented");
    }

    public Formula leftSide() {
        throw new RuntimeException("Not implemented");
    }

    public Formula rightSide() {
        throw new RuntimeException("Not implemented");
    }
}

class Implication extends BinaryFormula {
    Implication(Formula left, Formula right) {
        super(left, right);
        throw new RuntimeException("Not implemented");
    }
}

class Equivalence extends BinaryFormula {
    Equivalence(Formula left, Formula right) {
        super(left, right);
        throw new RuntimeException("Not implemented");
    }
}
