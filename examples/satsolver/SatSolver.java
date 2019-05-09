import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

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

    public static Theory loadDimacs(String name) throws FileNotFoundException {
        try (Scanner sc = new Scanner(new File(name))) {
            return new Theory(sc);
        }
    }

    public static void writeDimacsResult(String name, Result result) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(name))) {
            if (result.sat) {
                System.err.println("SAT");
                w.write("SAT\n");
                int max = result.valuation.keySet().stream().mapToInt(Integer::parseInt).max().orElse(-1);
                Iterator<String> i = IntStream.range(1, max+1).mapToObj(Integer::toString).iterator();
                while (i.hasNext()) {
                    String v = i.next();
                    if (result.valuation.getOrDefault(v, true) == false)
                        w.write("-");
                    w.write(v);
                    w.write(" ");
                }
                w.write("0\n");
            } else {
                System.err.println("UNSAT");
                w.write("UNSAT\n");
            }
        }
    }

    public Result solve(Theory t) {
        // TODO implement me!
        return Result.unsat();
    }

    public static void main(String[] args) {
        try {
            Theory t = loadDimacs(args[0]);
            SatSolver s = new SatSolver();
            Result res = s.solve(t);
            try {
                writeDimacsResult(args[1], res);
            }
            catch (IOException e) {
                System.err.println("Can't write output file `" + args[1] + "'");
                System.exit(1);
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("Can't read input file `" + args[0] + "'");
            System.exit(1);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Need two arguments: input file and output file");
            System.exit(1);
        }
    }
}
