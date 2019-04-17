import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Variable {
    private final String name;
    private final Literal tLit, fLit;
    private Boolean val; // this is tri-state: null == unset

    /**
     * Create a new variable with the two corresponding literals.
     *
     * @param name name of the variable
     */
    private Variable(String name) {
        this.name = name;
        tLit = new Literal(this, true);
        fLit = new Literal(this, false);
        val = null;
    }

    /**
     * @return the name of the variable
     */
    public String name() {
        return name;
    }

    /**
     * Return the positive or negative literal for this variable.
     */
    public Literal lit(boolean sign) {
        return sign ? tLit : fLit;
    }

    /**
     * Set the variable to either true or false.
     * Note: do not use this method directly.
     * Use `setTrue` on the corresponding literal!
     */
    void set(boolean val) {
        this.val = val;
    }

    /**
     * Unset the variable
     */
    void unset() {
        val = null;
    }

    /**
     * @return true if the variable is assigned a value
     */
    public boolean isSet() {
        return val != null;
    }

    /**
     * Return the valuation of this variable.
     *
     * @return null if the variable is unset, true/false otherwise
     */
    public Boolean val() {
        return val;
    }

    @Override
    public String toString() {
        return "Var<" + name() +":" + (isSet() ? val.toString() : "unset") + ">";
    }

    public static Variable fromString(String name, Map<String,Variable> vars) {
        if (vars.containsKey(name))
            return vars.get(name);
        Variable v = new Variable(name);
        vars.put(name, v);
        return v;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Variable)) return false;
        Variable v = (Variable)other;
        return name().equals(v.name());
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }
}

class Literal {
    private final Variable variable;
    private final boolean sign;
    private final Set<Clause> watchedIn = new HashSet<Clause>();

    /**
     * Create a new literal.
     * Should be called only from Variable's constructor!
     */
    Literal(Variable variable, boolean sign) {
        this.variable = variable;
        this.sign = sign;
    }
    public Variable variable() {
        return variable;
    }
    public String name() {
        return variable.name();
    }
    public boolean sign() {
        return sign;
    }

    @Override
    public String toString() {
        return (sign() ? "" : "-") + name();
    }

    public String toString(Literal[] watched) {
        String w = "";
        if (watched[0] == this) w += "(1)";
        if (watched[1] == this) w += "(2)";
        return toString() + w;
    }

    public static Literal fromString(String s, Map<String,Variable> vars) {
        s = s.trim();
        boolean sign = true;
        if (s.charAt(0) == '-' || s.charAt(0) == '¬') {
            s = s.substring(1);
            sign = false;
        }
        return Variable.fromString(s, vars).lit(sign);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Literal)) return false;
        Literal l = (Literal)other;
        return (name().equals(l.name())) && (sign() == l.sign());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Change the assignment of the variable so that this literal becomes true.
     * I.e. if this is a negative literal, the variable becomes false.
     */
    void setTrue() {
        variable.set(sign);
    }

    /**
     * Unset the literal / variable.
     */
    void unset() {
        variable.unset();
    }

    /**
     * @return true if the literal / variable is assigned a value
     */
    boolean isSet() {
        return variable.isSet();
    }

    /**
     * @return true if the variable is assigned a value that makes the literal true
     */
    boolean isTrue() {
        return variable.isSet() && sign == variable.val();
    }

    /**
     * @return true if the variable is assigned a value that makes the literal false
     */
    boolean isFalse() {
        return variable.isSet() && sign != variable.val();
    }

    /**
     * @return the complementary literal to this one
     */
    public Literal not() {
        return variable.lit(!sign);
    }

    /**
     * References to clauses where this literal is being watched.
     *
     * When the watched literals in a clase change, this needs to be
     * updated / kept in sync!
     */
    Set<Clause> watchedIn() { return watchedIn; }
}

class Clause extends HashSet<Literal> {
    Literal[] watched = {null, null};

    private Clause(Collection<? extends Literal> lits) {
        super(lits);
    }

    /**
     * References to the literals being watched in this clause.
     * Can be null if a literal is not watched!
     */
    Literal[] watched() { return watched; }

    @Override
    public String toString() {
        return stream()
            .map(l -> l.toString(watched()))
            .collect(Collectors.joining(" "))
        ;
    }

    public static Clause fromString(String s, Map<String, Variable> vars) {
        s = s.trim().replaceFirst("^\\(", "").replaceFirst("\\)$", "").trim();
        if (s.isEmpty())
            return new Clause(Collections.emptyList());
        return new Clause(
            Pattern.compile("[ ∨]+").splitAsStream(s)
                .map(cs -> Literal.fromString(cs, vars))
                .collect(Collectors.toList())
        );
    }

    /**
     * Set the watched literal for the given index (0,1) to be `lit`.
     * This method must maintain correct watchedIn links in Literal-s.
     *
     * @param index which watched literal to set / change
     * @param lit the new literal to be watched
     */
    void setWatch(int index, Literal lit) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Find a new watched literal to replace old.
     *
     * @param old the old watched literal to replace
     *        (usually when it became false)
     * @return true if new watched literal was found,
     *         false if no acceptable literal was found (old won't be changed)
     */
    public boolean findNewWatch(Literal old) {
        throw new RuntimeException("Not implemented");
    }
}

/**
 * A helper class to reference unit clauses.
 *
 * A unit clause is one that has one unset literal
 * and all other are set to false.
 *
 * It contains a reference to the last unset literal,
 * so it can be accessed directly without searching thorugh
 * the clause again.
 */
class UnitClause {
    private final Clause clause;
    private final Literal unsetLiteral;
    public UnitClause(Clause clause, Literal unsetLiteral) {
        this.clause = clause;
        this.unsetLiteral = unsetLiteral;
    }
    /**
     * @return the  (unit) clause
     */
    public Clause clause() { return clause; }
    /**
     * @return the (last) literal that is inset in this unit clause
     */
    public Literal unsetLiteral() { return unsetLiteral; }

    @Override
    public String toString() { return "Unit<" + clause + "," + unsetLiteral + ">"; }

    @Override
    public int hashCode() { return clause.hashCode() * 31 + unsetLiteral.hashCode(); }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UnitClause)) return false;
        UnitClause uc = (UnitClause)other;
        return (clause.equals(uc.clause)) && (unsetLiteral.equals(uc.unsetLiteral));
    }
}

class Cnf extends HashSet<Clause> {
    Cnf() {
        super();
    }
    Cnf(Clause... cls) {
        super(Arrays.asList(cls));
    }
    Cnf(Collection<? extends Clause> cls) {
        super(cls);
    }
    @Override
    public String toString() {
        return stream()
            .map(Clause::toString)
            .map(s -> s + "\n")
            .collect(Collectors.joining(""))
        ;
    }
}

class Theory {
    private final Cnf cnf = new Cnf();
    private final Map<String,Variable> vars = new HashMap<String,Variable>();
    private final Deque<Literal> assignedLits = new ArrayDeque<Literal>();
    public Theory(String... cls) {
        for (String s : cls) {
            cnf.add(Clause.fromString(s, vars));
        }
    }

    public Cnf cnf() { return cnf; }
    public Map<String, Variable> vars() { return vars; }
    public int nAssigned() { return assignedLits.size(); }

    /**
     * Initialize the watched literals in each clause,
     * so that two appropriate literals are watched for each.
     *
     * If a clause contains only one literal, it should be watched
     * and the clause should be added to the list of unit clauses
     * `units`.
     *
     * @param units a set where unit clauses should be added.
     * @return false if an empty clause was encountered (i.e.
     *         the theory is already unsatisfiable), true otherwise.
     */
    public boolean initWatched(Set<UnitClause> units) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Set the literal to be true and adjust any watches
     * on the opposite literal accordingly.
     *
     * Also add any clauses that became "unit" clauses
     * (all literals are false except one that is unset)
     * into the set referenced by units.
     *
     * @param l the literal to become true
     * @return false if a clause becomes unsatisfied (all literals became/are false)
     *         or if an already set literal is requsted to be set the wrong way,
     *         true otherwise
     */
    public boolean setLiteral(Literal l, Set<UnitClause> units) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Unset the last assigned literal/variable.
     */
    public void unsetLiteral() {
        throw new RuntimeException("Not implemented");
    }

}
