import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Tester {
    private static final String Vpravo = VlkKozaKapusta.Vpravo;
    private static final List<String> Breh = VlkKozaKapusta.Breh;
    private static final String Vlk = VlkKozaKapusta.Vlk;
    private static final String Koza = VlkKozaKapusta.Koza;
    private static final String Kapusta = VlkKozaKapusta.Kapusta;
    private static final String Gazda = VlkKozaKapusta.Gazda;
    private static final List<String> Kto = VlkKozaKapusta.Kto;
    int tested = 0;
    int passed = 0;
    int ncase = 0;
    int time = 0;
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
            return true;
        } else {
            printScopes("  ");
            System.err.println("    Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public <T> boolean compareRef(T result, T expected, String msg) {
        tested++;
        if (result == expected) {
            passed++;
            return true;
        } else {
            printScopes("  ");
            System.err.println("    Failed: " + msg + ":");
            System.err.println("      got " + result + " expected " + expected);
            return false;
        }
    }

    public boolean fail(String msg) {
        tested++;
        printScopes("  ");
        System.err.println("    Failed: " + msg);
        return false;
    }

    public boolean pass(long duration) {
        tested++;
        passed++;
        System.err.println("PASSED in " + (duration / 1000.0) + "ms");
        return true;
    }

    public void startCase(String s) {
        System.err.println(String.format("CASE %d: %s", ++ncase, s));
    }

    boolean checkGood(Map<String, String> s0, int N, List<String> res) {
        if (N != res.size())
            return fail("Wrong result length!");

        int step = 0;
        Map<String, String> s = new HashMap<>(s0);
        message(String.format("%2d: %-7s %s", step, "", s));
        for (String a : res) {
            step++;
            if (!Kto.contains(a))
                return fail("Wrong action: " + a);
            if (!s.get(Gazda).equals(s.get(a)))
                return fail(a + " is on other side then " + Gazda);
            s.put(a, Breh.get(1 - Breh.indexOf(s.get(a))));
            s.put(Gazda, s.get(a));
            message(String.format("%2d: %-7s %s", step, a, s));

            for (List<String> bad : Arrays.asList( Arrays.asList(Vlk, Koza), Arrays.asList(Koza, Kapusta))) {
                String x = bad.get(0);
                String y = bad.get(1);
                if (s.get(x).equals(s.get(y)) && !s.get(x).equals(s.get(Gazda)))
                    return fail(String.format("%s and %s without %s", x, y, Gazda));
            }
        }

        for (Map.Entry<String, String> e: s.entrySet()) {
            if (!e.getValue().equals(Vpravo))
                return fail(e.getKey() + " didn't end on the right");
        }

        return true;
    }

    boolean checkBad(Map<String, String> s0, int N, List<String> res) {
        if (res.size() != 0)
            return fail("non-empty result when there should be no solution");
        return true;
    }

    public void test(Map<String, String> s0, int N, boolean solvable) {
        String name = s0.toString() + " (" + N + ")";
        startCase(name);
        try (Tester.Scope s = scope(name)) {
            try {
                long start = System.nanoTime();
                List<String> res = (new VlkKozaKapusta()).vyries(s0, N);
                long duration = (System.nanoTime() - start) / 1000;
                time += duration;
                message("ran in " + (duration / 1000.0) + "ms");
                message("result: " + res);

                boolean ok = solvable ? checkGood(s0, N, res) : checkBad(s0, N, res);
                if (ok)
                    pass(duration);
            }
            catch (Throwable e) {
                printScopes("  ");
                fail("Exception: " + e.toString());
                e.printStackTrace();
            }
        }
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

public class VlkKozaKapustaTest {
    private static final String Vlavo = VlkKozaKapusta.Vlavo;
    private static final String Vpravo = VlkKozaKapusta.Vpravo;
    private static final String Vlk = VlkKozaKapusta.Vlk;
    private static final String Koza = VlkKozaKapusta.Koza;
    private static final String Kapusta = VlkKozaKapusta.Kapusta;
    private static final String Gazda = VlkKozaKapusta.Gazda;
    static Map<String, String> makeState(String... s) {
        Map<String, String> m = new HashMap<String, String>();
        for (int i = 0; i + 1 < s.length; i += 2)
            m.put(s[i], s[i+1]);
        return m;
    }
    public static void main(String[] args) {
        Tester t = new Tester();
        Map<String, String> s0;

        s0 = makeState(
            Vlk, Vpravo,
            Koza, Vpravo,
            Kapusta, Vpravo,
            Gazda, Vpravo
        );
        t.test(s0, 0, true);

        s0 = makeState(
            Vlk, Vpravo,
            Koza, Vpravo,
            Kapusta, Vpravo,
            Gazda, Vpravo
        );
        t.test(s0, 1, false);

        s0 = makeState(
            Vlk, Vpravo,
            Koza, Vlavo,
            Kapusta, Vpravo,
            Gazda, Vlavo
        );
        t.test(s0, 1, true);

        s0 = makeState(
            Vlk, Vpravo,
            Koza, Vpravo,
            Kapusta, Vpravo,
            Gazda, Vpravo
        );
        t.test(s0, 2, true);

        s0 = makeState(
            Vlk, Vpravo,
            Koza, Vlavo,
            Kapusta, Vpravo,
            Gazda, Vpravo
        );
        t.test(s0, 2, true);

        s0 = makeState(
            Vlk, Vpravo,
            Koza, Vpravo,
            Kapusta, Vlavo,
            Gazda, Vpravo
        );
        t.test(s0, 2, false);

        s0 = makeState(
            Vlk, Vlavo,
            Koza, Vpravo,
            Kapusta, Vpravo,
            Gazda, Vpravo
        );
        t.test(s0, 2, false);

        s0 = makeState(
            Vlk, Vlavo,
            Koza, Vlavo,
            Kapusta, Vlavo,
            Gazda, Vlavo
        );
        t.test(s0, 7, true);

        s0 = makeState(
            Vlk, Vlavo,
            Koza, Vlavo,
            Kapusta, Vlavo,
            Gazda, Vlavo
        );
        t.test(s0, 9, true);

        System.exit(t.status() ? 0 : 1);
    }
}
