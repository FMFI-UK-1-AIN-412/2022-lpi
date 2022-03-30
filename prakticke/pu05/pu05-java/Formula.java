import java.util.*;

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
        return new ArrayList<Formula>();
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


    public SignedFormula.Type signedType(boolean sign) {
        throw new RuntimeException("Not implemented");
    }

    public List<SignedFormula> signedSubf(boolean sign) {
        throw new RuntimeException("Not implemented");
    }
}

class AtomicFormula extends Formula {
    AtomicFormula() {

    }

    @Override
    public int deg() {
        return 0;
    }

    @Override
    public List<Formula> subfs() {
        return new ArrayList<Formula>();
    }
}

class PredicateAtom extends AtomicFormula {
    String name;
    List<Constant> args;

    PredicateAtom(String name, List<Constant> args) {
        this.name = name;
        this.args = args;
    }

    String name() {
        return name;
    }


    List<Constant> arguments() {
        return args;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(name);
        s.append('(');
        for (int i = 0; i < args.size(); i++) {
            s.append(args.get(i));
            if (i != args.size() - 1) {
                s.append(',');
            }
        }
        s.append(')');

        return s.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        PredicateAtom p2 = (PredicateAtom) other;
        if (!this.name.equals(((PredicateAtom) other).name)) {
            return false;
        }
        if (!this.args.equals(p2.args)) {
            return false;
        }

        return true;
    }

    @Override
    public Set<String> constants() {
        Set<String> result = new HashSet<String>();
        for (Constant arg : args) {
            result.add(arg.name);

        }
        return result;
    }

    @Override
    public Set<String> predicates() {
        Set<String> result = new HashSet<String>();
        result.add(name);
        return result;
    }

    @Override
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> result = new HashSet<AtomicFormula>();
        result.add(this);
        return result;
    }

    @Override
    public boolean isTrue(Structure m) {
        if (!m.iP(this.name).isEmpty()) {
            List<String> zoz = new ArrayList<String>();
            for (Constant arg : args) {
                zoz.add(arg.name);
            }
            List<String> interpr = new ArrayList<String>();
            for (String s : zoz) {
                interpr.add(m.iC(s));
            }
            for (List<String> x : m.iP(name)) {
                if (interpr.equals(x)) {
                    return true;
                }

            }
            return m.iP(name).contains(interpr);

        }

        return false;
    }

    @Override
    public List<Formula> subfs() {
        return new ArrayList<Formula>();
    }

    @Override
    public int deg() {
        return 0;
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

class EqualityAtom extends AtomicFormula {
    Constant left;
    Constant right;

    EqualityAtom(Constant left, Constant right) {
        this.left = left;
        this.right = right;
    }

    Constant left() {
        return left;
    }

    Constant right() {
        return right;
    }

    public String toString() {
        return left.toString() + '=' + right.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (this == null || other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        EqualityAtom p2 = (EqualityAtom) other;
        if (!this.left.equals(((EqualityAtom) other).left)) {
            return false;
        }
        if (!this.right.equals(((EqualityAtom) other).right)) {
            return false;
        }


        return true;
    }

    @Override
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> result = new HashSet<AtomicFormula>();
        result.add(this);
        return result;
    }

    @Override
    public Set<String> constants() {
        Set<String> result = new HashSet<String>();
        result.add(left.name);
        result.add(right.name);
        return result;
    }

    @Override
    public Set<String> predicates() {
        return new HashSet<String>();
    }

    @Override
    public boolean isTrue(Structure m) {
        if (m.iC(left.name).equals(m.iC(right.name))) {
            return true;
        }
        return false;
    }
}

class Negation extends Formula {
    Formula orig;

    Negation(Formula originalFormula) {
        this.orig = originalFormula;
    }

    public Formula originalFormula() {
        return orig;
    }

    @Override
    public String toString() {
        return "-" + orig.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Negation negation = (Negation) o;
        return Objects.equals(orig, negation.orig);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public List<Formula> subfs() {
        List<Formula> res = new ArrayList<Formula>();
        res.add(orig);
        return res;

    }

    @Override
    public int deg() {
        return 1 + orig.deg();
    }

    @Override
    public Set<AtomicFormula> atoms() {
        return orig.atoms();
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        List<SignedFormula> result = new ArrayList<>();
        if (sign ) {
            result.add(new SignedFormula(false, originalFormula()));
        }
        else  {
            result.add(new SignedFormula(true, originalFormula()));
        }
        return result;
    }

    @Override
    public Set<String> predicates() {
        return orig.predicates();
    }

    @Override
    public Set<String> constants() {
        return orig.constants();
    }

    @Override
    public boolean isTrue(Structure m) {
        return !orig.isTrue(m);
    }


}

class Disjunction extends Formula {
    List<Formula> disj;

    Disjunction(List<Formula> disjuncts) {
        this.disj = disjuncts;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(");
        for (Formula f : disj) {
            s.append(f.toString());
            s.append('|');
        }
        if (s.length() > 1) {
            s.deleteCharAt(s.length() - 1);
        }

        s.append(')');
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disjunction that = (Disjunction) o;
        return Objects.equals(disj, that.disj);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public List<Formula> subfs() {
        return disj;
    }

    @Override
    public int deg() {
        if (disj.isEmpty() || disj.size() == 1) {
            return 1;
        }
        int degree = 0;
        String s = toString();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '-') {
                degree++;
            }
            if (s.charAt(i) == '|') {
                degree++;
            }
            if (s.charAt(i) == '&') {
                degree++;
            }
        }


        return degree;
    }

    @Override
    public Set<String> constants() {
        Set<String> result = new HashSet<String>();
        for (Formula f : disj) {
            result.addAll(f.constants());
        }
        return result;
    }

    @Override
    public Set<String> predicates() {
        Set<String> result = new HashSet<String>();
        for (Formula f : disj) {
            result.addAll(f.predicates());
        }
        return result;
    }

    @Override
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> result = new HashSet<AtomicFormula>();
        for (Formula f : disj) {
            result.addAll(f.atoms());
        }
        return result;
    }

    @Override
    public boolean isTrue(Structure m) {
        for (Formula f : disj) {
            if (f.isTrue(m)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        if (sign) {
            return SignedFormula.Type.Beta;
        }
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        List<SignedFormula> result = new ArrayList<>();
        for (Formula f :disj) {
            result.add(new SignedFormula(sign, f));

        }
        return result;
    }
}

class Conjunction extends Formula {
    List<Formula> conj;

    Conjunction(List<Formula> conjuncts) {
        this.conj = conjuncts;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(");
        for (Formula f : conj) {
            s.append(f.toString());
            s.append('&');
        }
        if (s.length() > 1) {
            s.deleteCharAt(s.length() - 1);
        }

        s.append(')');
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conjunction that = (Conjunction) o;
        return Objects.equals(conj, that.conj);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public List<Formula> subfs() {
        return conj;
    }

    @Override
    public int deg() {
        if (conj.isEmpty() || conj.size() == 1) {
            return 1;
        }

        int degree = 0;
        String s = toString();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '-') {
                degree++;
            }
            if (s.charAt(i) == '|') {
                degree++;
            }
            if (s.charAt(i) == '&') {
                degree++;
            }
        }


        return degree;
    }

    @Override
    public Set<String> constants() {
        Set<String> result = new HashSet<String>();
        for (Formula f : conj) {
            result.addAll(f.constants());
        }
        return result;
    }

    @Override
    public Set<String> predicates() {
        Set<String> result = new HashSet<String>();
        for (Formula f : conj) {
            result.addAll(f.predicates());
        }
        return result;
    }

    @Override
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> result = new HashSet<AtomicFormula>();
        for (Formula f : conj) {
            result.addAll(f.atoms());
        }
        return result;
    }

    @Override
    public boolean isTrue(Structure m) {
        for (Formula f : conj) {
            if (!f.isTrue(m)) {
                return false;
            }

        }
        return true;
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        if (sign) {
            return SignedFormula.Type.Alpha;
        }
        return SignedFormula.Type.Beta;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        List<SignedFormula> result = new ArrayList<>();
        for (Formula f :conj) {
            result.add(new SignedFormula(sign, f));

        }
        return result;
    }
}

class BinaryFormula extends Formula {
    Formula left;
    Formula right;

    BinaryFormula(Formula left, Formula right) {
        this.left = left;
        this.right = right;

    }

    public Formula leftSide() {
        return left;
    }

    public Formula rightSide() {
        return right;
    }
}

class Implication extends BinaryFormula {
    Implication(Formula left, Formula right) {
        super(left, right);

    }

    @Override
    public String toString() {
        return "(" + left.toString() + "->" + right.toString() + ')';
    }

    @Override
    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> resltt = new HashSet<AtomicFormula>(left.atoms());
        resltt.addAll(right.atoms());
        return resltt;
    }

    @Override
    public Set<String> constants() {
        Set<String> res = new HashSet<String>(left.constants());
        res.addAll(right.constants());
        return res;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (this == null || other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        Implication o = (Implication) other;
        if (((Implication) other).right.equals(this.right) && ((Implication) other).left.equals(this.left)) {
            return true;
        }
        return false;

    }

    @Override
    public Set<String> predicates() {
        Set<String> res = new HashSet<String>(left.predicates());
        res.addAll(right.predicates());
        return res;
    }

    @Override
    public int deg() {
        int degree = 0;
        String s = toString();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '-') {
                degree++;
            }
            if (s.charAt(i) == '|') {
                degree++;
            }
            if (s.charAt(i) == '&') {
                degree++;
            }
        }


        return degree;
    }

    @Override
    public List<Formula> subfs() {
        List<Formula> res = new ArrayList<Formula>();
        res.add(left);
        res.add(right);
        return res;
    }

    @Override
    public boolean isTrue(Structure m) {
        if (left.isTrue(m) && !right.isTrue(m)) {
            return false;


        }
        return true;
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        if (sign) {
            return SignedFormula.Type.Beta;
        }
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        List<SignedFormula> result = new ArrayList<>();
        if (sign) {
            result.add(new SignedFormula(false, leftSide()));
            result.add(new SignedFormula(true, rightSide()));
        }
        else  {
            result.add(new SignedFormula(true, leftSide()));
            result.add(new SignedFormula(false, rightSide()));
        }
        return result;
    }
}

class Equivalence extends BinaryFormula {
    Equivalence(Formula left, Formula right) {
        super(left, right);

    }

    public Set<AtomicFormula> atoms() {
        Set<AtomicFormula> resltt = new HashSet<AtomicFormula>(left.atoms());
        resltt.addAll(right.atoms());
        return resltt;
    }

    @Override
    public Set<String> constants() {
        Set<String> res = new HashSet<String>(left.constants());
        res.addAll(right.constants());
        return res;
    }

    @Override
    public Set<String> predicates() {
        Set<String> res = new HashSet<String>(left.predicates());
        res.addAll(right.predicates());
        return res;
    }

    public String toString() {
        return "(" + left.toString() + "<->" + right.toString() + ')';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (this == null || other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        Equivalence o = (Equivalence) other;
        if (((Equivalence) other).right.equals(this.right) && ((Equivalence) other).left.equals(this.left)) {
            return true;
        }
        return false;

    }

    @Override
    public int deg() {
        String s = toString();
        int degree = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '-') {
                degree++;
            }
            if (s.charAt(i) == '|') {
                degree++;
            }
            if (s.charAt(i) == '&') {
                degree++;
            }
        }

        return degree;
    }

    @Override
    public List<Formula> subfs() {
        List<Formula> res = new ArrayList<Formula>();
        res.add(left);
        res.add(right);
        return res;
    }

    @Override
    public boolean isTrue(Structure m) {
        if (!left.isTrue(m) && !right.isTrue(m)) {
            return true;
        } else if (left.isTrue(m) && right.isTrue(m)) {
            return true;
        }
        return false;
    }

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        if (sign) {
            return SignedFormula.Type.Alpha;
        }
        return SignedFormula.Type.Beta;
    }

    @Override
    public List<SignedFormula> signedSubf(boolean sign) {
        List<SignedFormula> result = new ArrayList<>();
        result.add(new SignedFormula(sign, new Implication(left, right)));
        result.add(new SignedFormula(sign, new Implication( right, left)));
        return result;



    }
}

