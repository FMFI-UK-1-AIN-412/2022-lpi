#!/usr/bin/env python3

import os
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '..', '..', '..', 'examples', 'sat')]
import sat

Agatha = 0
Butler = 1
Charles = 2
People = [ Agatha, Butler, Charles ]
PeopleNames = ['Agatha', 'Butler', 'Charles']
P = len(People)

def killed(p1 : int, p2 : int) -> int:
    pass

def hates(p1 : int, p2 : int) -> int:
    pass

def richer(p1 : int, p2 : int) -> int:
    pass

class MurderMystery(object):
    def writeTheory(self, w : sat.DimacsWriter) -> None:
        """Zapise teoriu do DimacsWriter-a w."""

        #Someone in Dreadsbury Mansion killed Aunt Agatha.
        # Ex killed(x,Agatha)
        # t.j.  ( killed(Agatha,Agatha) v killed(Butler,Agatha) v ...)
        w.writeClause(killed(x,Agatha) for x in People)

        #Agatha, the butler, and Charles live in Dreadsbury Mansion, and are the only ones to live there.
        #  -- toto nezapisujeme, hovori nam to koho dosadzujeme

        #A killer always hates, and is no richer than his victim.
        #Charles hates noone that Agatha hates.
        #Agatha hates everybody except the butler.
        #The butler hates everyone not richer than Aunt Agatha.
        #The butler hates everyone whom Agatha hates.
        #Noone hates everyone.
        #Who killed Agatha?

    def prove(self) -> str:
        return ''

if __name__ == "__main__":
    mm = MurderMystery()
    print(mm.prove())
