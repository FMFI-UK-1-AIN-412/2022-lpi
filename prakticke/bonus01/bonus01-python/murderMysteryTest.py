#!/usr/bin/env python3

import unittest
import MurderMystery as MM
import models
from typing import Set, Iterable, Generator
import os
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '../../../examples/sat')]
import sat

ti = 'testinput.txt'
def predString(pred : str, x : int, y : int) -> str:
    return '%s(%s,%s)' % (pred, MM.PeopleNames[x], MM.PeopleNames[y])

def numbersFromFile(fname : str) ->  Iterable[int]:
    with open(fname, "r") as inf:
        for l in inf:
            for i in l.split():
                yield int(i)

def parseCnf(numbers : Iterable[int]) -> Set[Set[int]]:
    cnf = set()
    cls = set()
    for n in numbers:
        if n == 0:
            cnf.add(frozenset(cls))
            cls = set()
        else:
            cls.add(n)
    return cnf

def readCnf(fname : str) -> Set[Set[int]]:
    return parseCnf(numbersFromFile(fname))

def formatModel(m : Set[str]) -> str:
    if len(m) == 0:
        return "\nEmpty model (perhaps an error in cnf / empty cnf...)"
    return '\n' + '\n'.join(sorted(m))

def predicates():
    for x in MM.People:
        for y in MM.People:
            for pred, predName in ((MM.killed, 'killed'),
                    (MM.hates, 'hates'), (MM.richer, 'richer')):
                yield ((pred, predName), x, y)


class MurderMysteryTest(unittest.TestCase):
    def test_1_predicates(self):
        """Tests that predicates are mapped to unique numbers.
        """
        values = {}
        for ((pred, predName), x, y) in predicates():
            pxy = pred(x,y)
            self.assertIsNotNone(pxy)
            values[predString(predName,x,y)] = pxy
        self.assertEqual(len(frozenset(values.values())), 27,
                'Wrong (duplicate) variable numbers: %s' % (repr(values),))
        self.assertTrue(0 not in values.values(),
                '0 in variables: %s' % (repr(values)))

    def models(self):
        while True:
            s, sol = sat.SatSolver().solve(ti, 'testoutput.txt')
            if not s:
                break
            yield sol
            w = sat.DimacsWriter(ti, 'a')
            w.writeClause(-x for x in sol)
            w.close()

    def test_2_theory(self):
        """Tests that the theory itself is correct.
        """
        solver = sat.SatSolver()
        w = sat.DimacsWriter(ti)

        MM.MurderMystery().writeTheory(w)
        w.close()

        cnf = readCnf(ti)

        rev = {}
        for ((pred, predName), x, y) in predicates():
            rev[pred(x,y)] = predString(predName, x, y)
            rev[-pred(x,y)] = '-' + predString(predName, x, y)
        def revModel(m : Set[int]) -> Set[str]:
            return frozenset(rev[x] for x in m)
        M = set(frozenset(m) for m in models.M)
        for m in self.models():
            rm = revModel(m)
            if rm not in M:
                self.fail("Wrong model:\n %s" % (formatModel(rm),))
            M.remove(rm)
        if len(M) > 0:
            self.fail("Some models are missing, for example:\n %s" %
                    (formatModel(M.pop()),))

    def test_3_prove(self):
        """Tests that `prove` returns a murderer (but not that it proves it correctly).
        """
        murderer = MM.MurderMystery().prove()
        self.assertIn(murderer, MM.PeopleNames, "Unknow murderer!")
        killerPredicate = "%s(%s,%s)" % ("killed", murderer, MM.PeopleNames[MM.Agatha])
        for model in models.M:
            self.assertIn(killerPredicate, model,
                "\n\n%s did not kill Agatha in this model: %s"
                % (murderer, formatModel(frozenset(model))))

if __name__ == "__main__":
    unittest.main(verbosity=2)

