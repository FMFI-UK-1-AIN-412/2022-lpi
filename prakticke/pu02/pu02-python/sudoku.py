#! /usr/bin/env python3
#
# Program, ktory nacita zadanie sudoku zo standardneho vstupu,
# vyriesi ho (pomocou kniznice SudokuSolver.py) a vypise riesenie na
# standardny vystup.
#
# V linuxe ho mozete pustit napriklad:
#
# ./sudoku.py < sudoku.in
#

import sys
import SudokuSolver

def die(msg):
    sys.stderr.write('%s\n' % msg)
    sys.exit(1)

s = []
try:
    for line in sys.stdin:
        if line.strip() != '':
            row = [ int(x) for x in line.split() ]
            if len(row) != 9:
                raise ValueError("Wrong line on input")
            s.append(row)
    if len(s) != 9:
        raise ValueError("Wrong number of lines")
except ValueError as e:
    die('Error reading input: %s' % (e,))


try:
    result = SudokuSolver.SudokuSolver().solve(s)
except:
    die('Error solving sudoku: %s' % (sys.exc_info()[1].message,))


for row in result:
    sys.stdout.write('%s\n' % ' '.join(map(str,row)))


# vim: set sw=4 ts=4 sts=4 et :
