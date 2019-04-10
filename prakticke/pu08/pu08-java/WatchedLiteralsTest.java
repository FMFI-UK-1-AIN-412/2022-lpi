import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class WatchedLiteralsTest {
    Tester tt = new Tester();

    public Set<UnitClause> units(Theory t) {
        Set<UnitClause> units = new HashSet<UnitClause>();
        for (Clause c : t.cnf()) {
            int size = c.size();
            int nFalse = 0;
            int nUnset = 0;
            Literal unset = null;
            for (Literal l : c) {
                if (l.isFalse()) ++nFalse;
                if (!l.isSet()) {
                    ++nUnset;
                    unset = l;
                }
            }
            if (nUnset == 1 && nFalse == size - 1)
                units.add(new UnitClause(c, unset));
        }
        return units;
    }

    public boolean checkWatches(Theory t, String msg) {
        boolean ok = true;
        for (Variable v : t.vars().values()) {
            for (Literal ll : Arrays.asList(v.lit(true), v.lit(false))) {
                Set<Clause> watchedIn = new HashSet<Clause>();
                for (Clause c : t.cnf()) {
                    if (ll == c.watched()[0] || ll == c.watched()[1]) {
                        watchedIn.add(c);
                    }
                }
                ok = ok && tt.compare(ll.watchedIn(), watchedIn, msg + ": bad watchedIn for literal " + ll);
            }
        }
        return ok;
    }

    public void checkWatchesAreSet(Theory t) {
        for (Clause c : t.cnf()) {
            tt.scope(s -> {
                tt.message("Clause: " + c);
                tt.message("Watched: " + c.watched()[0] + ", " + c.watched()[1]);
                if (c.size() > 0) {
                    tt.testNotEqual(c.watched()[0], null,
                        "first watched literal must be set for clause " + c);
                    tt.testNotEqual(c.watched()[1], null,
                        "second watched literal must be set for clause " + c);
                }
                if (c.size() > 2) {
                    tt.testNotEqual(c.watched()[0], c.watched()[1],
                        "watched literals must differ");
                }
            });
        }
    }

    public void testSetLiteral(boolean willBeSat, String assignStr, String name, Theory t) {
        List<Literal> assign = assignStr.isEmpty() ? Collections.emptyList() : Pattern.compile("[ ∨]+").splitAsStream(assignStr)
                .map(cs -> Literal.fromString(cs, t.vars()))
                .collect(toList())
        ;
        tt.testCase(name, s -> {
            Set<UnitClause> units = new HashSet<UnitClause>();
            boolean initRes = t.initWatched(units);
            tt.compare(initRes, assign.size() > 0 ? true : willBeSat, "initWatched should return whether it's unsatisfiable");
            if (initRes) {
                Set<UnitClause> realUnits = units(t);
                tt.compare(units, realUnits, "wrong unit clauses reported after initWatched!");
                checkWatchesAreSet(t);
                checkWatches(t, "After initWatched");
            }

            int toGo = assign.size();
            String assigned = "";
            for (Literal l : assign) {
                units = new HashSet<UnitClause>();
                Set<UnitClause> oldUnits = units(t);

                boolean ret = t.setLiteral(l, units);
                tt.message(l + " -> true " + "	" + "   vars: " + t.vars().values());

                assigned = assigned + " " + l;

                Set<UnitClause> newUnits = units(t);
                newUnits.removeAll(oldUnits);
                tt.compare(units, newUnits, "Wrong unit clauses reported after setting literals: " + assigned + " vars: " + t.vars().values());

                boolean expRet = (--toGo > 0) ? true : willBeSat;
                tt.compare(ret, expRet, "setLiteral returned wrong value after setting literals " + assigned);

                tt.compare(l.isTrue(), true, "literal was not set or set to wrong value! "  + l + ": " + l.variable());

                checkWatches(t, "After setting literals " + assigned);
            }

            Collections.reverse(assign);
            for (Literal l : assign) {
                t.unsetLiteral();
                tt.message("unsetLiteral()" + "	" + "   vars: " + t.vars().values());
                tt.compare(l.isSet(), false, "literal was not unset properly (expected to unset " + l + ") " + l + ".isSet()");
            }

        });
    }

    public static Theory T(String... cls) { return new Theory(cls); }
    public void run() {

        tt.testCase("setWatch", s -> {
            Theory T = T("a b c -e -f -g");
            Clause C = T.cnf().iterator().next();

            tt.compareRef(C.watched()[0], null, "Watch 0 is null at the beginning");
            tt.compareRef(C.watched()[1], null, "Watch 1 is null at the beginning");

            tt.message("set watch 0 to a");
            Literal a = T.vars().get("a").lit(true);
            C.setWatch(0, a);
            tt.compare(C.watched()[0], a, "Watch 0 set to a");
            checkWatches(T, "watchedIn after watch 0 set to a");

            tt.message("set watch 1 to -e");
            Literal ne = T.vars().get("e").lit(false);
            C.setWatch(1, ne);
            tt.compare(C.watched()[1], ne, "Watch 1 set to -e");
            checkWatches(T, "watchedIn after watch 1 set to -e");

            tt.message("change watch 0 from a to b");
            Literal b = T.vars().get("b").lit(true);
            C.setWatch(0, b);
            tt.compare(C.watched()[0], b, "Watch 0 after change from a to b");
            checkWatches(T, "watchedIn after watch 0 changed from a to b");
        });

        tt.testCase("fndNewWatch", s -> {
            Theory T = T("a b c -e -f -g");
            Clause C = T.cnf().iterator().next();
            Literal a = T.vars().get("a").lit(true);
            Literal ne = T.vars().get("e").lit(false);
            C.setWatch(0, a);
            tt.message("setWatch(0,a)" + "	" + C);
            C.setWatch(1, ne);
            tt.message("setWatch(1,ne)" + "	" + C);
            tt.compare(C.watched()[0], a, "Watch 0 set to a");
            tt.compare(C.watched()[1], ne, "Watch 1 set to -e");
            checkWatches(T, "watchedIn at the beginning");

            Literal w = null;
            boolean ret = false;
            boolean ok = true;

            for (int which : Arrays.asList(0, 1, 0, 1)) {
                if (!ok) break;
                w = C.watched()[which];
                w.not().setTrue();
                tt.message(w + " -> false" + "	" + C + "   vars: " + T.vars().values());
                ret = C.findNewWatch(w);
                tt.message("findNewWatch(" + w + ")"+ "	" + C + "   vars: " + T.vars().values());
                ok = ok && tt.compare(ret, true, "findNewWatch should return true");
                ok = ok && tt.compare(!C.watched()[which].isSet() || C.watched()[which].isTrue(), true, "new watched should be unset or true");
                ok = ok && checkWatches(T, "watchedIn after findNewWatch");
            }

            // now it's not possible to find new ones, the first time other will still be unset (unit clause), the second time it will be false
            for (int which : Arrays.asList(0, 1)) {
                if (!ok) break;
                w = C.watched()[which];
                w.not().setTrue();
                tt.message(w + " -> false" + "	" + C + "   vars: " + T.vars().values());
                ret = C.findNewWatch(w);
                tt.message("findNewWatch(" + w + ")"+ "	" + C + "   vars: " + T.vars().values());
                ok = ok && tt.compare(ret, false, "findNewWatch should return false");
                ok = ok && tt.compare(C.watched()[which], w, "watched literal should not have changed");
                ok = ok && checkWatches(T, "watchedIn after findNewWatch");
            }

        });


        tt.testCase("findNewWatch true", s -> {
            Theory T = T("a b c");
            Clause C = T.cnf().iterator().next();
            Literal a = T.vars().get("a").lit(true);
            Literal b = T.vars().get("b").lit(true);
            Literal c = T.vars().get("c").lit(true);
            C.setWatch(0, a);
            tt.message("setWatch(0,a)" + "	" + C);
            C.setWatch(1, b);
            tt.message("setWatch(1,b)" + "	" + C);
            tt.compare(C.watched()[0], a, "Watch 0 set to a");
            tt.compare(C.watched()[1], b, "Watch 1 set to b");

            c.setTrue();
            tt.message(c + " -> true " + "	" + C + "   vars: " + T.vars().values());
            a.not().setTrue();
            tt.message(a + " -> false" + "	" + C + "   vars: " + T.vars().values());

            boolean ret = C.findNewWatch(a);
            tt.message("findNewWatch(" + a + ")"+ "	" + C + "   vars: " + T.vars().values());
            tt.compare(ret, true, "findNewWatch should find the true literal");
            tt.compare(C.watched()[0], c, "findNewWatch should find the true literal");

        });


        testSetLiteral(true, "a -b", "setLiteral simple",
            T("a b c", "-b")
        );

        testSetLiteral(true, "-a", "setLiteral creates unit",
            T("a b")
        );

        testSetLiteral(false, "-a -b -c -d -e", "setLiteral unsat",
            T("a b c d e")
        );

        testSetLiteral(false, "", "empty clause", T(""));

        System.exit(tt.status() ? 0 : 1);
    }

    public static void main(String[] args) {
        WatchedLiteralsTest wlt = new WatchedLiteralsTest();
        wlt.run();
    }
}
