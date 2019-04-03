import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Literal {
    private final String name;
    private final boolean neg;
    Literal(String name) {
        this.name = name;
        this.neg = false;
    }
    Literal(String name, boolean neg) {
        this.name = name;
        this.neg = neg;
    }
    public String name() {
        return name;
    }
    public boolean neg() {
        return neg;
    }
    public static Literal Lit(String name) {
        return new Literal(name);
    }
    public static Literal Not(String name) {
        return new Literal(name, true);
    }
    public static Literal Not(Literal lit) {
        return new Literal(lit.name(), !lit.neg());
    }
    /**
     * Parse a literal from string, ignoring whitespace and
     * recognizing possible '-' or '¬' at the beginning.
     * @throws on emtpy string.
     */
    public static Literal fromString(String s) {
        s = s.trim();
        if (s.charAt(0) == '-' || s.charAt(0) == '¬')
            return Not(s.substring(1));
        else
            return Lit(s);
    }
    public Literal not() {
        return new Literal(name(), !neg());
    }
    public boolean isSatisfied(Map<String,Boolean> v) {
        return neg ^ v.get(name);
    }
    public String toString() {
        return (neg() ? "-" : "") + name();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(this instanceof Literal)) return false;
        Literal l = (Literal)other;
        return (name().equals(l.name())) && (neg() == l.neg());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

class Clause extends HashSet<Literal> {
    Clause(Literal... lits) {
        super(Arrays.asList(lits));
    }
    Clause(Collection<? extends Literal> lits) {
        super(lits);
    }
    public static Clause fromString(String s) {
        s = s.trim().replaceFirst("^\\(", "").replaceFirst("\\)$", "").trim();
        if (s.isEmpty())
            return new Clause();
        return new Clause(
            Pattern.compile("[ ∨]+").splitAsStream(s)
                .map(Literal::fromString)
                .collect(Collectors.toList())
        );
    }
    public boolean isSatisfied(Map<String,Boolean> v) {
        return stream().anyMatch(l -> l.isSatisfied(v));
    }
    public String toString() {
        return "(" + stream()
            .map(Literal::toString)
            .collect(Collectors.joining(" "))
            + ")"
        ;
    }
}

class Cnf extends HashSet<Clause> {
    Cnf(Clause... cls) {
        super(Arrays.asList(cls));
    }
    Cnf(Collection<? extends Clause> cls) {
        super(cls);
    }
    public static Cnf fromString(String s) {
        if (s.trim().isEmpty())
            return new Cnf();
        return new Cnf(
            Pattern.compile("[,;∧]").splitAsStream(s)
                .map(Clause::fromString)
                .collect(Collectors.toList())
        );
    }
    public boolean isSatisfied(Map<String,Boolean> v) {
        return stream().allMatch(c -> c.isSatisfied(v));
    }
    @Override
    public String toString() {
        return stream()
            .map(Clause::toString)
            .collect(Collectors.joining(", "))
        ;
    }
}
