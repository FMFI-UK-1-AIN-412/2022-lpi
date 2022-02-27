from typing import Sequence
import sys
import os
sys.path[0:0] = [os.path.join(sys.path[0], '../../../examples/sat')]
import sat
Sudoku = Sequence[Sequence[int]]
class SudokuSolver:

    def __init__(self):
        self.N = 0

    def q(self, riadok, stlpec, cislo):
        return 9*9*riadok + 9*stlpec + cislo

    def solve(self, sudoku: Sudoku):
        solver = sat.SatSolver()
        w = sat.DimacsWriter('sudoku_out.txt')
        suradnice = {0, 1, 2, 3, 4, 5, 6, 7, 8}

        for cislo in range(1, 10, 1):

            # kde uz su cisla
            for riadok in range(9):
                for stlpec in range(9):
                    if (sudoku[riadok][stlpec] != 0):
                        w.writeClause([self.q(riadok, stlpec, sudoku[riadok][stlpec])])


            # stvorcek 0:0 - 2:2
            for riadok in range(0, 3, 1):
                for stlpec in range(0, 3, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 3:0 - 5:2
            for riadok in range(3, 6, 1):
                for stlpec in range(0, 3, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 6:0 - 8:2
            for riadok in range(6, 9, 1):
                for stlpec in range(0, 3, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 0:3 - 2:5
            for riadok in range(0, 3, 1):
                for stlpec in range(3, 6, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 3:3 - 5:5
            for riadok in range(3, 6, 1):
                for stlpec in range(3, 6, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 6:3 - 8:5
            for riadok in range(6, 9, 1):
                for stlpec in range(3, 6, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 0:6 - 2:8
            for riadok in range(0, 3, 1):
                for stlpec in range(6, 9, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 3:6 - 5:8
            for riadok in range(3, 6, 1):
                for stlpec in range(6, 9, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()

            # stvorcek 6:6 - 8:8
            for riadok in range(6, 9, 1):
                for stlpec in range(6, 9, 1):
                    w.writeLiteral(self.q(riadok, stlpec, cislo))
            w.finishClause()



            #nemoze byt v stlpci
            for riadok in range(0, 9, 1):
                for stlpec in range(0, 9, 1):
                    suradnice.remove(stlpec)
                    for j in suradnice:
                        w.writeImpl(self.q(riadok, stlpec, cislo), -self.q(riadok, j, cislo))
                    suradnice.add(stlpec)


            #nemoze byt v riadku
            for riadok in range(0, 9, 1):
                for stlpec in range(0, 9, 1):
                    suradnice.remove(riadok)
                    for j in suradnice:
                        w.writeImpl(self.q(riadok, stlpec, cislo), -self.q(j, stlpec, cislo))
                    suradnice.add(riadok)

            # na jednej pozicii len jedno cislo
            for i in range(9):
                for j in range(9):
                    for n2 in range(1, 10):
                        if cislo != n2:
                            w.writeImpl(self.q(i, j, cislo), -self.q(i, j, n2))



        w.close()
        ok, sol = solver.solve(w, 'sudoku_out.txt')
        res = list(9 * [0] for _ in range(9))
        if ok:
            for num in sol:
                if num > 0:
                    num -= 1
                    n = num % 9 + 1
                    num = num // 9
                    j = num % 9
                    num = num // 9
                    i = num % 9
                    res[i][j] = n
        return res



