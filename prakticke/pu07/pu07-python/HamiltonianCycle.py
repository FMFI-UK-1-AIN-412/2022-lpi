#!/usr/bin/env python3

import os
import sys
sys.path[0:0] = [os.path.join(sys.path[0], os.path.join('..', '..', '..', 'examples', 'sat'))]
import sat

class HamiltonianCycle(object):
    def find(self, edges):
        """ Finds a hamiltonian cycle in the oriented graph given by 'edges' or
            returns an empty list if there is none.

            @param edges an incidence matrix, edges[i][j] is True if there
            is an edge from i to j
        """

        n = len(edges) # number of vertices in the graph

        return []

# vim: set sw=4 ts=4 sts=4 et :
