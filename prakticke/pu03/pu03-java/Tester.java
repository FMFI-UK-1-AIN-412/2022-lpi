import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

class Tester {
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
            System.err.println("  ✓ " + msg);
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
            System.err.println("  ✓ " + msg);
            return true;
        } else {
            printScopes("  ");
            System.err.println("   ✕ Failed: " + msg + ":");
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

    public void fail(String msg) {
        tested++;
        printScopes("  ");
        System.err.println("   ✕ Failed: " + msg);
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

    public boolean status() {
        System.err.println("");
        System.err.println("TESTED " + tested);
        System.err.println("PASSED " + passed);
        System.err.println("SUM(time) " + (time / 1000.0) + "ms");

        System.err.println(tested == passed ? "OK" : "ERROR" );
        return tested == passed;
    }

}
