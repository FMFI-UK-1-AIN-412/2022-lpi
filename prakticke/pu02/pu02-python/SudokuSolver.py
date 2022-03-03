from typing import Sequence
import os.path
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '../../examples/sat')]

import sat

Sudoku = Sequence[Sequence[int]]


class SudokuSolver:
    def enkoduj(self, r, s ,c):
        return 81 * r + 9 * s + c

    def dekoduj(self, c):
        c -= 1
        cislo = c % 9 + 1
        c //= 9
        stlpec = c % 9
        c //= 9
        riadok = c % 9
        return ((riadok, stlpec, cislo))

    def solve(self, vstup):
        solver = sat.SatSolver()
        sub = sat.DimacsWriter('vysledok.txt')

        for i in range(9):
            for j in range(9):
                if vstup[i][j] != 0:
                    sub.writeClause([self.enkoduj(i, j, vstup[i][j])])

        for num1 in range(1, 10):
            for num2 in range(1, 10):
                for x in range(9):
                    for y in range(9):
                        if(num1 != num2):
                            sub.writeImpl(self.enkoduj(x,y,num1), -self.enkoduj(x,y,num2))

        for c in range(1, 10):
            for x in range(9):
                for y in range(9):
                    sub.writeLiteral(self.enkoduj(x, y, c))
                sub.finishClause()
                #
                for y in range(9):
                    sub.writeLiteral(self.enkoduj(y, x, c))
                sub.finishClause()

        for num in range(1, 10):
            for x in range(0,7,3):
                for y in range(0,7,3):
                    for addX in range(3):
                        for addY in range(3):
                            sub.writeLiteral(self.enkoduj(x+addX, y+addY, num))
                    sub.finishClause()

        solvable, finish = solver.solve(sub, "vysledok.txt")
        if not solvable:
            return [[0 for i in range(9)] for j in range(9)]

        solved = [[0 for i in range(9)] for j in range(9)]
        for n in finish:
            if(n > 0):
                x, y, cis = self.dekoduj(n)
                solved[x][y] = cis

        return solved

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

# i = [
#     [0, 9, 5, 0, 0, 3, 6, 0, 0],
#     [0, 6, 0, 0, 5, 1, 0, 3, 8],
#     [1, 8, 0, 0, 4, 6, 7, 0, 9],
#     [5, 0, 4, 0, 2, 0, 0, 0, 6],
#     [6, 1, 0, 4, 8, 0, 0, 2, 0],
#     [8, 3, 0, 0, 0, 0, 0, 7, 0],
#     [9, 5, 0, 7, 3, 4, 0, 6, 0],
#     [0, 0, 6, 0, 0, 0, 4, 0, 0],
#     [7, 0, 0, 0, 0, 2, 5, 9, 3],
# ]

s = SudokuSolver()
res = s.solve(i)
for _ in res:
    print(_)

    # def solve(self, sudoku: Sudoku) -> Sudoku:
    #     # all zeroes -> no solution
    #     return list(9 * [0] for _ in range(9))
