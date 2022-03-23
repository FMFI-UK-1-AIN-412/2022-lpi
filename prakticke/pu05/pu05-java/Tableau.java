import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.lang.StringBuilder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.joining;
import static java.lang.Math.max;

class SignedFormula {
    private Formula f;
    private boolean sign;

    public enum Type {
        Alpha,
        Beta
    }

    public SignedFormula(boolean sign, Formula f) {
        this.f = f;
        this.sign = sign;
    }

    public static SignedFormula T(Formula f) {
        return new SignedFormula(true, f);
    }

    public static SignedFormula F(Formula f) {
        return new SignedFormula(false, f);
    }

    public SignedFormula neg() {
        return new SignedFormula(!sign(), f());
    }

    public Formula f() {
        return f;
    }

    public boolean sign() {
        return sign;
    }

    public Type type() {
        return f.signedType(sign);
    }

    public List<SignedFormula> subf() {
        return f.signedSubf(sign);
    }

    public String signString() {
        return sign ? "T" : "F";
    }

    @Override
    public String toString() {
        return signString() + " " + f();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object other)
    {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        SignedFormula otherSf = (SignedFormula) other;
        return f().equals(otherSf.f()) && sign == otherSf.sign;
    }
}

class Node {
    private SignedFormula sf;
    private Node source ;
    private Node closedFrom = null;
    private List<Node> children = new ArrayList<Node>();
    private Tableau tableau = null;
    private int number = 0;
    static private int NUM = 0;

    /**
     * Creates a new tableau node.
     *
     * @param sf the signed formula in this node
     * @param source reference to the node containing the original
     *               formula that this node was created from
     *               (null for "input" formulas at the top of the tableau)
     */
    public Node(SignedFormula sf, Node source) {
        this.sf = sf;
        this.source = source;
        this.number = ++NUM;
    }

    public SignedFormula sf() { return sf; }
    public Tableau tableau() { return tableau; }
    public List<Node> children() { return Collections.unmodifiableList(children); }
    public int number() { return number; }
    public Node source() { return source; }


    /**
     * Closes this node (and this branch) because the formula
     * in this node  is coplementary to `closedFrom`.
     *
     * @param closedFrom a node on this branch with the complementary formula
     */
    public void close(Node closedFrom) {
        this.closedFrom = closedFrom;
    }

    /**
     * @return true if this node is closed
     */
    public boolean isClosed() {
        if (closedFrom != null)
            return true;

        for (Node child : children)
            if (!child.isClosed())
                return false;
        return true;
    }

    /**
     * Removes the node and its children from the tableau it belongs to.
     */
    public void disown() {
        for (Node child : children)
            child.disown();
        tableau = null;
    }

    /**
     * Add a child to this node.
     * Used by Tableau.
     */
    public void addChild(Node child) {
        children.add(child);
    }

    /**
     * Add this node to a tableau.
     * This sets the tableu it belongs to and a sequential number inside that tableau.
     * Used by Tableau.
     */
    public void addToTableau(Tableau t, int number)
    {
        this.tableau = t;
        this.number = number;
    }

    @Override
    public String toString() {
        return label() + closedString();
    }

    public String label() {
        String lbl = "(" + number + ") " + sf;
        if (source != null)
            lbl = lbl + " (" + source.number +  ")";
        return lbl;
    }

    private String closedString() {
        if (closedFrom != null)
            return "* [" + closedFrom.number + "]";
        else
            return "";
    }

    public String tree() {
        return String.join("\n", treeLines());
    }

    private final String separator = " | ";

    private int treeWidth() {
        int thisWd = label().length();
        int childrenWd = 0;
        for (Node child : children)
            childrenWd += child.treeWidth();
        childrenWd += separator.length() * max(0, children.size() - 1);
        return max(thisWd, childrenWd);
    }

    private String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; ++i)
            sb.append(s);
        return sb.toString();
    }
    private String center(String s, int n) {
        int ss = s.length();
        if (n <= ss) return s;
        int left = (n - ss) / 2;
        int right = n - ss - left;
        return repeat(" ", left) + s + repeat(" ", right);
    }

    private List<String> treeLines() {
        int width = treeWidth();
        List<String> lines = new ArrayList();

        lines.add(label());

        if (closedFrom != null)
            lines.add("* [" + number + "," + closedFrom.number + "]");

        if (children.size() > 0) {
            if (source == null && children.get(0).source != null) {
                // last "input" node
                lines.add(repeat("=", width));
            }
            else if (children.size() > 1) {
                lines.add(repeat("-", width));
            }
            lines.addAll(treeMergeChildLines());
        }

        return lines
            .stream()
            .map(l -> center(l, width))
            .collect(toList())
        ;
    }

    private List<String> treeMergeChildLines() {
        List<List<String>> chLines =
            children.stream().map(c -> c.treeLines()).collect(toList());
        List<Integer> chWidths =
            children.stream().map(c -> c.treeWidth()).collect(toList());

        List<String> lines = new ArrayList<String>();
        int l = 0;
        boolean allEmpty = false;

        for (; !allEmpty; ++l) {
            allEmpty = true;
            List<String> lineParts = new ArrayList<String>();
            for (int i = 0; i < children.size(); ++i) {
                if (l < chLines.get(i).size()) {
                    // use i-th child line
                    lineParts.add(chLines.get(i).get(l));
                    allEmpty = false;
                } else {
                    // no more lines in i-th child, pad with spaces
                    lineParts.add(repeat(" ", chWidths.get(i)));
                }
            }
            if (!allEmpty)
                lines.add(lineParts.stream().collect(joining(separator)));
        }
        return lines;
    }
}

/**
 * A (propositional) tableau that consists of Node-s.
 */
class Tableau {
    private Node root = null;
    private int number = 0;

    /**
     * @return true if the tableau is closed (i.e. all branches are closed).
     */
    public boolean isClosed() {
        return root != null && root.isClosed();
    }

    /**
     * @return the root node of the tableau
     */
    public Node root() {
        return root;
    }

    /**
     * @return the size of the tableau (number of nodes)
     */
    public int size() {
        return number;
    }

    /**
     * @return a string representing the tableau as a plain text tree
     */
    public String toString() {
        if (root == null)
            return "EMPTY";
        return root.tree();
    }


    /**
     * Add signed formulas as the "input" of a tableau.
     *
     * Note: this can be called only once on an empty tableau.
     *
     * @param sfs  signed formulas to add at the start of the tableau
     *             as input
     * @return A list of nodes that were created.
     */
    public List<Node>  addInitial(SignedFormula[] sfs) {
        ArrayList<Node> added = new ArrayList<>();
        if (sfs.length == 0){
            return new ArrayList<>();
        }
        Node parent = new Node(sfs[0], null);
        root = parent;
        addNode(null, parent);
        added.add(parent);
        for (int i = 1; i < sfs.length; i++) {
            Node node = new Node(sfs[i], null);
            addNode(parent, node);
            parent = node;
            added.add(parent);
        }
        return added;
    }

    /**
     * Extend the tableau at `leaf` using the `index`-th
     * alpha-subformula of `from` node.
     *
     * @param leaf the (leaf) node to add the new node/formula to
     * @param from the node with an alpha formula which's subformula
     *             will be added
     * @param index which alfa-subformula to append
     *
     * @return reference to the added node.
     *
     * TODO `SignedFormula sf` instead of index?
     */
    public Node extendAlpha(Node leaf, Node from, int index)
    {
        Node node = new Node(from.sf().subf().get(index),from);
        addNode(leaf,node);
        return node;
    }

    /**
     * Extend the tableau at `leaf` by adding the (beta) subformulas
     * of the beta Node `from`.
     *
     * @param leaf the (leaf) node to add the new node/formula to
     * @param from the node with a beta formula which's subformulas
     *             will be added
     * @return list of references to the added nodes
     */
    public List<Node> extendBeta(Node leaf, Node from)
    {
        ArrayList<Node> a = new ArrayList<>();
        for (SignedFormula sf : from.sf().subf()) {
            Node node = new Node(sf, from);
            addNode(leaf,node);
            a.add(node);
        }
        return a;
    }

    /**
     * Internal helper to add a Node to the tableau.
     *
     * Calls `addChild` on parent (if appropriate)
     * and `addToTableau` with appropriate number.
     *
     * @param parent parent to put the new node under, null to insert a root
     * @param node the node to insert
     */
    private void addNode(Node parent, Node node) {
        if(parent == null){
            root = node;
        } else {
            parent.addChild(node);
        }
        number++;
        node.addToTableau(this, number);
    }

}

