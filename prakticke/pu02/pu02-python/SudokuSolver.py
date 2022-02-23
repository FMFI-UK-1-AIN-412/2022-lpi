from typing import Sequence

import sys
import os
sys.path[0:0] = [os.path.join(sys.path[0], '../../../examples/sat')]
import sat


Sudoku = Sequence[Sequence[int]]


class SudokuSolver:
    def code(self, r, s, n):
        return 9 * 9 * r + 9 * s + n

    def decode(self, code):
        code -= 1
        n = code % 9
        code /= 9
        s = code % 9
        code /= 9
        r = code % 9
        return r, s, n + 1

    def solve(self, sudoku: Sudoku) -> Sudoku:
        solver = sat.SatSolver()
        w = sat.DimacsWriter('sudoku_cnf_in.txt')

        for i in range(len(sudoku)):
            for j in range(len(sudoku)):
                if (sudoku[i][j] != 0):
                    w.writeLiteral(self.code(i, j, sudoku[i][j]))
                    w.finishClause()


        # v kazdom policku 1 az 9
        for i in range(9):
            for j in range(9):
                for n in range(1, 10):
                    w.writeLiteral(self.code(i, j, n))
                w.finishClause()



        # v policku len 1 cislo
        for i in range(9):
            for j in range(9):
                for n1 in range(1, 10):
                    for n2 in range(1, 10):
                        if n1 != n2:
                            w.writeImpl(self.code(i, j, n1),
                                        -self.code(i, j, n2))
                            #print(i, j, n1, ' ->  -', i, j, n2)

        # aby nebolo to iste cislo viackrat v jednom riadku
        for i in range(9):
            for j1 in range(9):
                for j2 in range(9):
                    if j1 != j2:
                        for n in range(1, 10):
                            w.writeImpl(self.code(i, j1, n),
                                        -self.code(i, j2, n))

        # aby nebolo to iste cislo viackrat v jednom stlpci
        for i1 in range(9):
            for i2 in range(9):
                if i1 != i2:
                    for j in range(9):
                        for n in range(1, 10):
                            #print(i1, j, i2, j)
                            w.writeImpl(self.code(i1, j, n),
                                        -self.code(i2, j, n))

        # aby nebolo to iste cislo viackrat v jednom stvorci
        for i1 in range(3):
            for i2 in range(3):
                for j1 in range(3):
                    for j2 in range(3):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                #print(i1, j1, i2, j2)
                                w.writeImpl(self.code(i1, j1, n),
                                           -self.code(i2, j2, n))
        # aby nebolo to iste cislo viackrat v jednom stvorci
        for i1 in range(3):
            for i2 in range(3):
                for j1 in range(3, 6):
                    for j2 in range(3, 6):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                #print(i1, j1, i2, j2)
                                w.writeImpl(self.code(i1, j1, n),
                                           -self.code(i2, j2, n))
        # aby nebolo to iste cislo viackrat v jednom stvorci
        for i1 in range(3):
            for i2 in range(3):
                for j1 in range(6, 9):
                    for j2 in range(6, 9):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                #print(i1, j1, i2, j2)
                                w.writeImpl(self.code(i1, j1, n),
                                           -self.code(i2, j2, n))

        # aby nebolo to iste cislo viackrat v jednom stvorci
        for i1 in range(3, 6):
            for i2 in range(3, 6):
                for j1 in range(3):
                    for j2 in range(3):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                #print(i1, j1, i2, j2)
                                w.writeImpl(self.code(i1, j1, n),
                                           -self.code(i2, j2, n))



        # aby nebolo to iste cislo viackrat v jednom stvorci
        for i1 in range(3, 6):
            for i2 in range(3, 6):
                for j1 in range(3, 6):
                    for j2 in range(3, 6):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                #print(i1, j1, i2, j2)
                                w.writeImpl(self.code(i1, j1, n),
                                           -self.code(i2, j2, n))

        # aby nebolo to iste cislo viackrat v jednom stvorci
        for i1 in range(3, 6):
            for i2 in range(3, 6):
                for j1 in range(6, 9):
                    for j2 in range(6, 9):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                #print(i1, j1, i2, j2)
                                w.writeImpl(self.code(i1, j1, n),
                                           -self.code(i2, j2, n))
        for i1 in range(6, 9):
            for i2 in range(6, 9):
                for j1 in range(3):
                    for j2 in range(3):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                w.writeImpl(self.code(i1, j1, n),
                                            -self.code(i2, j2, n))

        for i1 in range(6, 9):
            for i2 in range(6, 9):
                for j1 in range(3, 6):
                    for j2 in range(3, 6):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                w.writeImpl(self.code(i1, j1, n),
                                            -self.code(i2, j2, n))

        for i1 in range(6, 9):
            for i2 in range(6, 9):
                for j1 in range(6, 9):
                    for j2 in range(6, 9):
                        if not (i1 == i2 and j1 == j2):
                            for n in range(1, 10):
                                w.writeImpl(self.code(i1, j1, n),
                                            -self.code(i2, j2, n))

   

        w.close()
        ok, sol = solver.solve(w, 'sudoku_cnf_out.txt')

        ret = [[]]
        i = 0
        j = 0
        count = 0
        if ok:
            for x in sol:
                if x > 0 :
                    r, s, n = self.decode(x)
                    count += 1
                    if j >= 9:
                        ret.append([])
                        i += 1
                        j = 0
                    ret[i].append(n)
                    j += 1
            return ret

        # all zeroes -> no solution
        return list(9 * [0] for _ in range(9))
