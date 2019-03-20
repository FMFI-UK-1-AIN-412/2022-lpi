import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

class Tester {
    int tested = 0;
    int passed = 0;
    int ncase = 0;
    int time = 0;
    Deque<Scope> scopes = new ArrayDeque<Scope>();
    Scope lastScope = null;
    boolean quiet = false;

    void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

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
        System.err.println();
        scopes.descendingIterator().forEachRemaining(s -> s.print(prefix + "  "));
    }

    public boolean compare(Object result, Object expected, String msg) {
        tested++;
        if (expected.equals(result)) {
            passed++;
            if (!quiet)
                System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("  ✕ Failed: " + msg + ":");
            System.err.println("     "
                    + " got " + (result == null ? "null" : result.toString())
                    + " expected " + expected
            );
            return false;
        }
    }

    public <T> boolean compareRef(T result, T expected, String msg) {
        tested++;
        if (result == expected) {
            passed++;
            if (!quiet)
                System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("   ✕ Failed: " + msg + ":");
            System.err.println("     "
                    + " got " + (result == null ? "null" : result.toString())
                    + " expected " + (expected == null ? "null" : expected.toString())
            );
            return false;
        }
    }

    public boolean testNotEqual(Object a, Object b, String msg) {
        return compare(!a.equals(b), true, msg);
    }

    public boolean testNotEqual(Object a, Object b) {
        return compare(!a.equals(b), true, a + " != " + b);
    }

    public void pass(String msg) {
        tested++;
        passed++;
        if (!quiet)
            System.err.println("  ✓ " + msg);
    }
    public void fail(String msg) {
        tested++;
        printScopes("  ");
        System.err.println("   ✕ Failed: " + msg);
    }

    public void startCase(String s) {
        System.err.println("");
        System.err.println(String.format("CASE %d: %s", ++ncase, s));
    }

    public void catchAny(Runnable r) {
        try {
            r.run();
        }
        catch (Throwable e) {
            fail("Exception: " + e.toString());
            e.printStackTrace();
        }
    }
    public void scope(String name, Consumer<Scope> c) {
        try (Tester.Scope s = scope(name)) {
            catchAny(() -> { c.accept(s); });
        }
    }

    public void scope(Consumer<Scope> c) {
        scope("", c);
    }

    public void testCase(String name, Consumer<Scope> c) {
        startCase(name);
        scope(name, c);
    }

    public static class TimedResult<T> {
        T result;
        long duration;
        public TimedResult(T result, long duration) {
            this.result = result;
            this.duration = duration;
        }
    }
    public <T> TimedResult<T> timed(Supplier<T> s) {
        long start = System.nanoTime();
        T ret = s.get();
        long end = System.nanoTime();
        long duration = (end - start) / 1000;

        time += duration;
        return new TimedResult<T>(ret, duration);
    }

    public boolean status() {
        System.err.println("");
        System.err.println("TESTED " + tested);
        System.err.println("PASSED " + passed);
        System.err.println("SUM(time) " + (time / 1000.0) + "ms");

        return tested == passed;
    }

}
