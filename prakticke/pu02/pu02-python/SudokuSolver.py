from typing import Sequence
import os.path
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '../../examples/sat')]

import sat

Sudoku = Sequence[Sequence[int]]


class SudokuSolver:

    def encode(self, x, y, num):
        return (9*9*x) + (9*y) + num

    def decode(self, num):
        num -= 1
        n = num % 9 + 1
        num //= 9
        j = num % 9
        num //= 9
        i = num % 9
        return ((i, j, n))

    def solve(self, sudoku):
        solver = sat.SatSolver()
        file = sat.DimacsWriter("result.txt")

        #cisla v predpripravenom sudoku (2D pole) + ulozenie ich pozicii
        for i in range(9):
            for j in range(9):
                val = sudoku[i][j]
                if (val != 0):
                    file.writeClause([self.encode(i,j,val)])

        #zadefinovat ze na kazdom policku moze byt iba jedno cislo
        for i in range(9):
            for j in range(9):
                for x in range(1, 10):
                    for y in range(1, 10):
                        if (x != y):
                            file.writeImpl(self.encode(i,j,x), -self.encode(i,j,y))

        #zabranit opakovanie v riadku/stlpci
        for num in range(1, 10):
            for x in range(9):
                for y in range(9):
                    file.writeLiteral(self.encode(x, y, num))
                file.finishClause()

                for y in range(9):
                    file.writeLiteral(self.encode(y, x, num))
                file.finishClause()

        #3x3 stvorceky
        for x in (0,3,6):
            for y in (0,3,6):
                for num in range(1, 10):
                    for addX in range(3):
                        for addY in range(3):
                            file.writeLiteral(self.encode(x+addX, y+addY, num))
                    file.finishClause()

        passed, res = solver.solve(file, "result.txt")

        if not passed:
            return list(9 * [0] for _ in range(9))

        solved = [[0 for __ in range(9)] for _ in range(9)]
        for num in res:
            if (0 < num):
                x, y, val = self.decode(num)
                solved[x][y] = val

        return solved

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
# s = SudokuSolver()
# res = s.solve(i)
# for _ in res:
#     print(_)

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
s = SudokuSolver()
res = s.solve(i)

for _ in res:
    print(_)