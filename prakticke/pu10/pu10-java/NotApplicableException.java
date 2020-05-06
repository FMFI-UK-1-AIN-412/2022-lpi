import java.lang.Exception;

/**
 * Thrown when the substitution given to `substitute` is not applicable for the
 * given formula.
 */
public class NotApplicableException extends Exception {
    /**
     * Constructs a new substitution not applicable exception.
     *
     * @param formula the (sub)formula where the substituiton is not applicable
     * @param variable the variable that is being substituted
     * @param replacemnt the term that is being substituted for `variable`
     */
    public NotApplicableException(Formula formula, String variable, Term replacement) {
        super(String.format("Substitution {%s -> %s} is not applicable to formula %s", variable, replacement, formula));
    }
}
