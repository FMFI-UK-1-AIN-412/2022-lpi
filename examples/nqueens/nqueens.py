#! /bin/env python

import os.path
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '../../examples/sat')]

import sat


class NQueens(object):
    def __init__(self):
        self.N = 0

    def q(self, r, c):
        return r * self.N + c + 1;

    def solve(self, N):
        self.N = N
        solver = sat.SatSolver()
        w = sat.DimacsWriter('nqueens_cnf_in.txt')

        # v kazdom riadku
        for r in range(N):
            # je aspon jedna dana
            for c in range(N):
                w.writeLiteral(self.q(r,c))
            w.finishClause()

        # v kazdom riadku
        for r in range(N):
            # na dvoch roznych poziciach
            for c1 in range(N):
                for c2 in range(c1):
                    # nie je na oboch dama
                    # q(r,c1) => -q(r,c2)
                    w.writeImpl(self.q(r,c1), -self.q(r,c2))

        # v kazdom stlpci
        for c in range(N):
            # na dvoch roznych miestach
            for r1 in range(N):
                for r2 in range(r1):
                    # nie je na oboch dama
                    w.writeImpl(self.q(r1,c), -self.q(r2,c))

        # uhlopriecky
        # trochu neefektivnejsie, ale rychlejsie na napisanie
        for c1 in range(N):
            for c2 in range(N):
                for r1 in range(N):
                    for r2 in range(N):
                        # rozne pozicie
                        if (self.q(r1,c1) != self.q(r2,c2)):
                            # r1,c1 a r2,c2 su na uhlopriecke
                            if (r1+c1 == r2+c2) or (r1+c2 == r2+c1):
                                w.writeImpl(self.q(r1,c1), -self.q(r2,c2))

        w.close()
        ok, sol = solver.solve(w, 'nqueens_cnf_out.txt')

        ret = []
        if ok:
            for x in sol:
                if x>0:
                    x -= 1
                    ret.append( (x // N, x % N) )
        return ret


if __name__ == '__main__':
    N = int(input())
    nq = NQueens()
    s = nq.solve(N)
    if len(s) == 0:
        print('Nema riesenie')
    else:
        for r,c in s:
            print('{} {}'.format(r,c))

# vim: set sw=4 ts=4 sts=4 et :
