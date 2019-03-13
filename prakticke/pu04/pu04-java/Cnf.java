import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class Literal {
	private AtomicFormula atom;
    private boolean neg;
    Literal(AtomicFormula atom) {
        this.atom = atom;
        this.neg = false;
    }
    Literal(AtomicFormula atom, boolean neg) {
        this.atom = atom;
        this.neg = neg;
    }
    public AtomicFormula atom() {
        return atom;
    }
    public boolean neg() {
        return neg;
    }
    public static Literal Lit(AtomicFormula atom) {
        return new Literal(atom);
    }
    public static Literal Not(AtomicFormula atom) {
        return new Literal(atom, true);
    }
    public static Literal Not(Literal lit) {
        return new Literal(lit.atom(), !lit.neg());
    }
    public boolean isTrue(Structure m) {
        return neg ^ atom.isTrue(m);
    }
    public String toString() {
        return (neg() ? "-" : "") + atom();
    }
}

class Clause extends ArrayList<Literal> {
    Clause(Literal... lits) {
        super(Arrays.asList(lits));
    }
    Clause(Collection<? extends Literal> lits) {
        super(lits);
    }
    public boolean isTrue(Structure m) {
        for (Literal lit : this)
            if (lit.isTrue(m))
                return true;
        return false;
    }
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> vs = new HashSet<AtomicFormula>();
        for (Literal lit : this)
            vs.add(lit.atom());
        return vs;
    }
    public String toString() {
        return stream()
            .map( l -> l.toString() )
            .collect(Collectors.joining(" "))
        ;
    }
}

class Cnf extends ArrayList<Clause> {
    Cnf(Clause... cls) {
        super(Arrays.asList(cls));
    }
    Cnf(Collection<? extends Clause> cls) {
        super(cls);
    }
    public boolean isTrue(Structure m) {
        for(Clause c : this)
            if (!c.isTrue(m))
                return false;
        return true;
    }
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> vs = new HashSet<AtomicFormula>();
        for (Clause cls : this)
            vs.addAll(cls.atoms());
        return vs;
    }
    public String toString() {
        return stream()
            .map( cls -> cls.toString() + "\n" )
            .collect(Collectors.joining(""))
        ;
    }
}
