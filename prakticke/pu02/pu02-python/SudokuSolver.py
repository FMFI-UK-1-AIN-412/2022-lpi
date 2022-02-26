#! /bin/env python

from typing import Sequence
import sys
import os
sys.path[0:0] = [os.path.join(sys.path[0], '../../../examples/sat')]

import sat


Sudoku = Sequence[Sequence[int]]


class SudokuSolver:
    def s(self, x: int, y: int, n: int) -> int:
        return x*100+y*10+n

    def solve(self, sudoku: Sudoku) -> Sudoku:
        solver = sat.SatSolver()
        w = sat.DimacsWriter('sudoku_in.txt')

        for x in range(9):
            for y in range(9):
                for n in range(1, 10):
                    w.writeLiteral(self.s(x, y, n))
                w.finishClause()

        for n in range(1, 10):
            for x in range(9):
                for y1 in range(9):
                    for y2 in range(y1):
                        w.writeImpl(self.s(x, y1, n), -self.s(x, y2, n))

        for n in range(1, 10):
            for y in range(9):
                for x1 in range(9):
                    for x2 in range(x1):
                        w.writeImpl(self.s(x1, y, n), -self.s(x2, y, n))

        for n in range(1, 10):
            for i in range(3):
                for j in range(3):
                    for x1 in range(3):
                        for x2 in range(3):
                            for y1 in range(3):
                                for y2 in range(3):
                                    if not (x1 == x2 and y1 == y2):
                                        w.writeImpl(self.s(x1+3*i, y1+3*j, n), -self.s(x2+3*i, y2+3*j, n))

        for y in range(len(sudoku)):
            for x in range(len(sudoku[y])):
                if sudoku[y][x] != 0:
                    w.writeLiteral(self.s(x, y, sudoku[y][x]))
                    w.finishClause()

        w.close()
        ok, sol = solver.solve(w, 'sudoku_out.txt')
        result = list(9 * [0] for _ in range(9))

        if not ok:
            return result

        for i in sol:
            if i > 0:
                x = (i%1000)//100
                y = (i%100)//10
                n = (i%10)
                result[y][x] = n
        return result
