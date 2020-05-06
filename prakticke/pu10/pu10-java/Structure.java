import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Structure<D> {
    /**
     * The domain of this structure.
     */
    Set<D> domain();

    /**
     * Interpretation function for constants.
     * @return value from the domain for the constant `constName`
     */
    public D iC(String constName);
    /**
     * Interpretation function for function symbols.
     * @return a mapping from (lists of) funcion arguments to function results for function `funcName`
     */
    public Map<List<D>,D> iF(String funcName);
    /**
     * Interpretation function for predicate symbols.
     * @return a set of (lists of) predicate argumets for which the predicate `predName` is satisfied
     */
    public Set<List<D>> iP(String predName);
}
