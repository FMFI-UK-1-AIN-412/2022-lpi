#! /usr/bin/env python3
#
# Testovaci program pre sudoku kniznicu.
#
# Kniznica musi byt v subore sudoku.py a musi implementovat triedu
# SudokuSolver s metodou solve.
#

import sys
import copy
from SudokuSolver import SudokuSolver


class Tester(object):
    def __init__(self):
        self.case = 0
        self.tested = 0
        self.passed = 0

    def fail(self, msg):
        print('ERROR: %s' % msg)
        return False;
    def checkList(self, l, msg):
        if len(l) != 9:
            return self.fail('Wrong result format %s!' % (msg,))
        s = sorted(list(set(l)))
        if len(s) !=9:
            return self.fail('Duplicate number %s!' % (msg,))
        if s != list(range(1,10)):
            return self.fail('Wrong numbers %s!' % (msg,))
        return True

    def checkInput(self, i, s):
        for r in range(9):
            for c in range(9):
                if i[r][c] != 0:
                    if i[r][c] != s[r][c]:
                        return self.fail('does not match input at %d,%d!' % (r,c))
        return True

    def checkGood(self, i, s):
        self.tested += 4
        ret = True
        if self.checkInput(i, s):
            self.passed += 1
        else:
            ret = False

        rret = True
        for row,r in zip(s,range(9)):
            if not self.checkList(row, 'in row %d: %s' % (r,repr(row))):
                rret = False
                break
        if rret:
            self.passed += 1
        else:
            ret = False

        rret = True
        for c in range(9):
            col = [ row[c] for row in s ]
            if not self.checkList(col, 'in col %d: %s' % (c,repr(col))):
                rret = False
                break
        if rret:
            self.passed += 1
        else:
            ret = False

        rret = True
        for sr in range(3):
            for sc in range(3):
                a = sc*3
                b = sc*3 + 3
                l = s[sr*3][a:b] + s[sr*3+1][a:b] + s[sr*3+2][a:b]
                if not self.checkList(l, 'in square %d,%d: %s' % (sr,sc,repr(l))):
                    rret = False
                    break
        if rret:
            self.passed += 1
        else:
            ret = False

        return ret

    def checkBad(self, s):
        self.tested += 4
        for r in s:
            for c in r:
                if c:
                    print('ERROR: Nonzero in bad sudoku!')
                    return False
        self.passed += 4
        return True

    def check(self, i, good, s):
        if good:
            return self.checkGood(i, s)
        else:
            return self.checkBad(s)


    def test(self, i, good, s):
        self.case += 1
        sys.stdout.write('Case %d:  ' % (self.case,))
        if self.check(i, good, s):
            print('PASSED')
        else:
            print('')
            print('{:^20}    {:^20}'.format('INPUT', 'OUTPUT'))
            for ri,rs in zip(i,s):
                print('{:<20}    {:<20}'.format(
                    ' '.join([str(x) for x in ri]),
                    ' '.join([str(x) for x in rs]),
                ))
            print('')

    def status(self):
        print("self.tested %d" % (self.tested,))
        print("self.passed %d" % (self.passed,))
        if self.tested == self.passed:
            print("OK")
            return True
        else:
            print("ERROR")
            return False


t = Tester()

i = [
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9],
]
t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)))

i = [
    [1, 2, 3, 4, 5, 6, 7, 8, 9],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
]
t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)))

i = [
    [1, 1, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
]
t.test(i, False, SudokuSolver().solve(copy.deepcopy(i)))

i = [
    [0, 9, 5, 0, 0, 3, 6, 0, 0],
    [0, 6, 0, 0, 5, 1, 0, 3, 8],
    [1, 8, 0, 0, 4, 6, 7, 0, 9],
    [5, 0, 4, 0, 2, 0, 0, 0, 6],
    [6, 1, 0, 4, 8, 0, 0, 2, 0],
    [8, 3, 0, 0, 0, 0, 0, 7, 0],
    [9, 5, 0, 7, 3, 4, 0, 6, 0],
    [0, 0, 6, 0, 0, 0, 4, 0, 0],
    [7, 0, 0, 0, 0, 2, 5, 9, 3],
]
t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)))

i = [
    [5, 3, 4, 6, 7, 8, 9, 1, 0],
    [6, 7, 2, 1, 9, 5, 3, 4, 0],
    [1, 9, 8, 3, 4, 2, 5, 6, 0],
    [8, 5, 9, 7, 6, 1, 4, 2, 0],
    [4, 2, 6, 8, 5, 3, 7, 9, 0],
    [7, 1, 3, 9, 2, 4, 8, 5, 0],
    [9, 6, 1, 5, 3, 7, 2, 8, 0],
    [2, 8, 7, 4, 1, 9, 6, 3, 0],
    [3, 4, 5, 2, 8, 6, 1, 7, 0],
]
t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)))

i = [
    [5, 3, 4, 6, 7, 8, 9, 1, 2],
    [6, 7, 2, 1, 9, 5, 3, 4, 8],
    [1, 9, 8, 3, 4, 2, 5, 6, 7],
    [8, 5, 9, 7, 6, 1, 4, 2, 3],
    [4, 2, 6, 8, 5, 3, 7, 9, 1],
    [7, 1, 3, 9, 2, 4, 8, 5, 6],
    [9, 6, 1, 5, 3, 7, 2, 8, 4],
    [2, 8, 7, 4, 1, 9, 6, 3, 5],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
]

t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)))

i = [
    [0, 0, 5, 7, 0, 9, 8, 0, 0],
    [0, 7, 0, 0, 8, 0, 0, 9, 0],
    [0, 8, 0, 4, 0, 2, 0, 3, 0],
    [0, 6, 4, 0, 5, 0, 3, 1, 0],
    [8, 0, 0, 0, 9, 0, 0, 0, 2],
    [7, 0, 0, 0, 0, 0, 0, 0, 9],
    [0, 0, 7, 6, 0, 4, 2, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [3, 0, 0, 0, 2, 0, 0, 0, 6],
]
t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)));

i = [
    [0, 0, 5, 7, 0, 9, 8, 0, 0],
    [0, 7, 0, 0, 8, 0, 0, 9, 0],
    [0, 8, 0, 4, 0, 2, 0, 3, 0],
    [0, 6, 4, 0, 5, 0, 3, 1, 0],
    [8, 0, 0, 0, 9, 0, 0, 0, 2],
    [7, 0, 0, 0, 0, 0, 6, 0, 9],
    [0, 0, 7, 6, 0, 4, 2, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [3, 0, 0, 0, 2, 0, 0, 0, 6],
]
t.test(i, False, SudokuSolver().solve(copy.deepcopy(i)));

i = [
    [0, 0, 6, 0, 4, 0, 7, 0, 0],
    [0, 0, 0, 1, 0, 2, 0, 0, 0],
    [2, 0, 0, 0, 3, 0, 0, 0, 4],
    [3, 0, 0, 0, 0, 0, 0, 0, 6],
    [0, 0, 8, 3, 0, 4, 5, 0, 0],
    [0, 2, 0, 0, 5, 0, 0, 7, 0],
    [0, 1, 0, 0, 9, 0, 0, 4, 0],
    [0, 5, 0, 0, 0, 0, 0, 9, 0],
    [8, 0, 4, 7, 0, 5, 2, 0, 3],
]
t.test(i, True, SudokuSolver().solve(copy.deepcopy(i)));

i = [
    [0, 0, 6, 0, 4, 0, 7, 0, 0],
    [0, 0, 0, 1, 0, 2, 0, 0, 0],
    [2, 0, 0, 0, 3, 0, 0, 0, 4],
    [3, 0, 0, 8, 0, 0, 0, 0, 6],
    [0, 0, 8, 3, 0, 4, 5, 0, 0],
    [0, 2, 0, 0, 5, 0, 0, 7, 0],
    [0, 1, 0, 0, 9, 0, 0, 4, 0],
    [0, 5, 0, 0, 0, 0, 0, 9, 0],
    [8, 0, 4, 7, 0, 5, 2, 0, 3],
]
t.test(i, False, SudokuSolver().solve(copy.deepcopy(i)));

sys.exit(0 if t.status() else 1)

# vim: set sw=4 ts=4 sts=4 et :
