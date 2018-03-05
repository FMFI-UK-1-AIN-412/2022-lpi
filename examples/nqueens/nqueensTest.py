#!/bin/env python

import nqueens
import unittest

class NQueensTest(unittest.TestCase):
    def __init__(self, N):
        unittest.TestCase.__init__(self, 'test_nqueens')
        self.N = N

    def compareRowColumn(self, q1, q2):
        r1, c1 = q1
        r2, c2 = q2
        self.assertNotEqual(r1, r2, "queens in same row %s %s" % (repr(q1), repr(q2)))
        self.assertNotEqual(c1, c2, "queens in same column %s %s" % (repr(q1), repr(q2)))

    def compareDiagonals(self, q1, q2):
        r1, c1 = q1
        r2, c2 = q2
        self.assertFalse(r1 + c2 == r2 + c1,
                "queens on same diagonal %s %s" % (repr(q1), repr(q2)))
        self.assertFalse(r1 + c1 == r2 + c2,
                "queens on same diagonal %s %s" % (repr(q1), repr(q2)))

    def test_nqueens(self):
        "test queens"
        self.queens = None
        queens = nqueens.NQueens().solve(self.N)
        self.queens = queens

        self.assertIsInstance(queens, list, "solve method must return a list")

        if self.N in (2,3):
            self.assertEqual(queens, [], "There is no solution for %d" % (self.N,))
        else:
            self.assertEqual(len(queens), self.N, "Wrong number of queens: %s" % (repr(queens),))
            for q in queens:
                self.assertIsInstance(q, tuple, "Queen coordinates must be a tuple (pair)")
                self.assertEqual(len(q), 2, "Queen coordinates must be a pair: %s" % (repr(q),))
                for x in q:
                    self.assertIsInstance(x, int,
                            "Queen coordinates must be numbers: %s" % (repr(q),))
                r,c = q
                self.assertGreaterEqual(r, 0, "Wrong queen coordinate: %s" % (repr(q),))
                self.assertGreaterEqual(c, 0, "Wrong queen coordinate: %s" % (repr(q),))
                self.assertLess(r, self.N, "Wrong queen coordinate: %s" % (repr(q),))
                self.assertLess(c, self.N, "Wrong queen coordinate: %s" % (repr(q),))
            for i in range(len(queens)):
                for j in range(i+1,len(queens)):
                    self.compareRowColumn(queens[i], queens[j])
            for i in range(len(queens)):
                for j in range(i+1,len(queens)):
                    self.compareDiagonals(queens[i], queens[j])

    def shortDescription(self):
        return "N=%d, queens=%s" % (self.N, repr(self.queens))

def load_tests(loader, tests, pattern):
    return unittest.TestSuite(
        NQueensTest(N) for N in range(9)
    )

if __name__ == '__main__':
    unittest.main()

# vim: set sw=4 ts=4 sts=4 et :
