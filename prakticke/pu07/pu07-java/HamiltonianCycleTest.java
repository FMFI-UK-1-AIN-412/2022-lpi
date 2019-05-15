import java.lang.Math;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Tester {
    int tested = 0;
    int passed = 0;
    int ncase = 0;
    long time = 0;
    Deque<Scope> scopes = new ArrayDeque<Scope>();
    Scope lastScope = null;

    class Scope implements AutoCloseable{
        private final Tester t;
        public final String name;
        public final Deque<String> messages = new ArrayDeque<String>();
        Scope(Tester t, String name) {
            this.t = t;
            this.name = name;
            t.scopes.push(this);
        }
        public void close() {
            t.scopes.pop();
            t.lastScope = t.scopes.peek();
        }
        public void message(String msg) {
            messages.push(msg);
        }
        public void print(String prefix) {
            if (!name.isEmpty())
                System.err.println(prefix + "In " + name);
            messages.descendingIterator().forEachRemaining(msg -> System.err.println(prefix + "  " + msg));
        }
    }

    public Scope scope(String msg) {
        lastScope = new Scope(this, msg);
        return lastScope;
    }
    public void message(String msg) {
        // throws if there isn't a scope
        lastScope.message(msg);
    }
    public void printScopes(String prefix) {
        scopes.descendingIterator().forEachRemaining(s -> s.print(prefix + "  "));
    }

    public boolean compare(Object result, Object expected, String msg) {
        tested++;
        if (expected.equals(result)) {
            passed++;
//            System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("  ✕ Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public <T> boolean compareRef(T result, T expected, String msg) {
        tested++;
        if (result == expected) {
            passed++;
//            System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("  ✕ Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public boolean testNotEqual(Object a, Object b, String msg) {
        return compare(!a.equals(b), true, msg);
    }

    public boolean testNotEqual(Object a, Object b) {
        return compare(!a.equals(b), true, a + " != " + b);
    }

    public boolean verify(boolean cond, String msg) {
        tested++;
        if (cond) {
            passed++;
            return true;
        } else {
            printScopes("  ");
            System.err.println("  ✕ Failed: " + msg);
            return false;
        }
    }

    public void fail(String msg) {
        tested++;
        printScopes("  ");
        System.err.println("  ✕ Failed: " + msg);
    }

    public void startCase(String s) {
        System.err.println(String.format("CASE %d: %s", ++ncase, s));
    }

    public void scope(String name, Consumer<Scope> c) {
        try (Tester.Scope s = scope(name)) {
            try {
                c.accept(s);
            }
            catch (Throwable e) {
                fail("Exception: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void scope(Consumer<Scope> c) {
        scope("", c);
    }

    public void testCase(String name, Consumer<Scope> c) {
        startCase(name);
        scope(name, c);
    }

    public boolean checkGood(boolean[][] m, List<Integer> r) {
        int N = m.length;
        if (!compare(r.size(), N, "Wrong result length"))
            return false;
        int last = r.get(r.size() - 1);
        if (!verify(last < N, "Wrong vertex number in output: " + last))
            return false;
        for (int v : r) {
            if (!verify(v < N, "Wrong vertex number in output: " + v))
                return false;
            if (!verify(m[last][v], String.format("No edge %d -> %d", last, v)))
                return false;
            last = v;
        }
        Set<Integer> vs = new HashSet<>(r);
        for (int v = 0 ; v < N; ++v) {
            if (!verify(vs.contains(v), "Missing vertex " + v))
                return false;
        }
        return true;
    }

    public boolean checkBad(boolean[][] m, List<Integer> r) {
        return verify(r.size() == 0, "non-empty result when there should be no cycle");
    }
    public boolean check(boolean[][] m, boolean good, List<Integer> r) {
        tested++;
        if (good) {
            passed++;
            return checkGood(m, r);
        } else {
            return checkBad(m, r);
        }
    }

    public static String toString(boolean[] l) {
        return IntStream.range(0, l.length)
            .mapToObj(i -> l[i] ? "1" : "0")
            .collect(Collectors.joining(" "));
    }

    public static String toString(boolean[][] m) {
        return Arrays.stream(m)
            .map(l -> toString(l))
            .collect(Collectors.joining("\n        "))
        ;
    }

    public void test(String desc, boolean[][] m, boolean good) {
        testCase(desc, s -> {
            long start = System.nanoTime();
            List<Integer> r = (new HamiltonianCycle()).find(m);
            long duration = (System.nanoTime() - start) / 1000;
            time += duration;

            message("m: \n        " + toString(m) );
            message("result: " + r);
            if (check(m, good, r))
                System.err.println("  ✓ PASSED in " + (duration / 1000.0) + "ms");
        });
    }

    public boolean status() {
        System.err.println("");
        System.err.println("TESTED " + tested);
        System.err.println("PASSED " + passed);
        System.err.println("SUM(time) " + (time / 1000.0) + "ms");

        System.err.println(tested == passed ? "OK" : "ERROR" );
        return tested == passed;
    }

}


public class HamiltonianCycleTest {
    public static class Edge {
        int f, t;
        Edge(int f, int t) { this.f = f; this.t = t;}
    };
    public static Edge E(int f, int t) { return new Edge(f, t); }

    public static boolean[][] edgesToIncidenceMatrix(List<Edge> edges) {
        int max = 1 + edges.stream()
            .mapToInt(e -> Math.max(e.f, e.t))
            .max()
            .orElse(0)
        ;
        boolean[][] m = new boolean[max][max];
        for (Edge e : edges)
            m[e.f][e.t] = true;
        return m;
    }

    public static boolean[][] randomGoodInput(Random rng, int size) {
        boolean[][] m = new boolean[size][size];
        for (int r=0; r<size; ++r)
            for (int c=0; c<size; ++c)
                m[r][c] = rng.nextBoolean();

        List<Integer> path = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(path, rng);
        int last = path.get(path.size() - 1);
        for (int v : path) {
            m[last][v] = true;
            last = v;
        }
        return m;
    }

    public static void main(String[] args) {
        Tester t = new Tester();
        Random rng = new Random(47);
        {
            boolean[][] m = {{true, true}, {true, true}};
            t.test("K2", m, true);
        }
        {
            boolean[][] m = {{false, true}, {false, false}};
            t.test("2->1", m, false);
        }
        {
            boolean[][] m = edgesToIncidenceMatrix(Arrays.asList(
                    E(0, 3),
                    E(0, 4),
                    E(1, 0),
                    E(1, 3),
                    E(1, 4),
                    E(2, 1),
                    E(2, 5),
                    E(3, 0),
                    E(3, 4),
                    E(4, 1),
                    E(4, 2),
                    E(4, 5),
                    E(5, 2),
                    E(5, 4)
                ));

            t.test("SomeGraph", m, true);
        }
        {
            boolean[][] m = edgesToIncidenceMatrix(Arrays.asList(
                    E( 1,  2), E( 2,  1),
                    E( 1,  4), E( 4,  1),
                    E( 1,  5), E( 5,  1),
                    E( 2,  3), E( 3,  2),
                    E( 2,  6), E( 6,  2),
                    E( 2,  7), E( 7,  2),
                    E( 3,  4), E( 4,  3),
                    E( 3,  8), E( 8,  3),
                    E( 4,  9), E( 9,  4),
                    E( 4, 10), E(10,  4),
                    E( 5,  6), E( 6,  5),
                    E( 5, 10), E(10,  5),
                    E( 6,  0), E( 0,  6),
                    E( 7,  8), E( 8,  7),
                    E( 7,  0), E( 0,  7),
                    E( 8,  9), E( 9,  8),
                    E( 9,  0), E( 0,  9),
                    E(10,  0), E( 0, 10)
                ));

            t.test("Herschel graph", m, false);
        }

        t.test("random good 10.1", randomGoodInput(rng, 10), true);
        t.test("random good 10.2", randomGoodInput(rng, 10), true);
        t.test("random good 10.3", randomGoodInput(rng, 10), true);
        t.test("random good 10.4", randomGoodInput(rng, 10), true);

        t.test("random good 20", randomGoodInput(rng, 20), true);

        t.test("random good 50", randomGoodInput(rng, 50), true);

        {
            boolean[][] m = {
                {true, true, true, true, true, false, false, true, true, true, true, false, true, false, false},
                {false, true, false, false, false, true, false, false, true, true, true, true, false, true, true},
                {false, false, true, false, false, false, false, false, true, false, true, false, true, false, false},
                {false, false, false, true, false, true, false, false, true, false, true, false, true, false, true},
                {false, false, false, false, true, true, false, false, false, false, true, true, true, true, true},
                {false, false, false, false, false, true, false, false, true, false, true, true, true, true, true},
                {false, false, false, false, false, false, true, false, true, true, true, true, true, true, true},
                {false, false, false, false, false, false, false, true, false, true, true, true, true, true, true},
                {false, false, false, false, false, false, false, false, true, false, true, false, false, true, true},
                {false, false, false, false, true, true, false, true, false, true, true, true, true, true, true},
                {false, false, true, true, true, true, false, false, false, true, true, true, true, true, false},
                {false, false, true, false, false, true, false, true, true, true, false, true, false, false, false},
                {false, false, true, true, true, true, false, true, true, true, false, false, true, false, false},
                {true, false, true, true, true, true, true, true, true, true, false, false, false, true, false},
                {false, true, true, true, false, true, false, false, true, false, false, false, false, false, true}
            };
            t.test("BigFalse", m, false);
        }

        System.exit(t.status() ? 0 : 1);
    }
}

