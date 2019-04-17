import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SatSolver {
    public static class Result {
        public boolean sat;
        public Map<String,Boolean> valuation;
        public Result(boolean sat, Map<String, Variable> vars) {
            this.sat = sat;
            valuation = new HashMap<String, Boolean>();
            for (String k : vars.keySet()) {
                valuation.put(k, vars.get(k).val());
            }
        }
        public static Result sat(Map<String, Variable> vars) { return new Result(true, vars); }
        public static Result unsat() { return new Result(false, Collections.emptyMap()); }
    }

    public Result solve(Theory t) {
        // TODO implement me!
        return Result.unsat();
    }
}
