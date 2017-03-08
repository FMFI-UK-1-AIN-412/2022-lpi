import java.util.List;
import java.util.Set;

public interface Structure {
    /**
     * The domain of this structure.
     */
    public Set<String> domain();

    /**
     * Interpretation function for constants.
     * @return value from the domain for the constant `constName`
     */
    public String iC(String constName);
    /**
     * Interpretation function for predicate symbols.
     * @return a set of (lists of) predicate argumets for which the predicate `predName` is satisfied
     */
    public Set<List<String>> iP(String predName);
}
