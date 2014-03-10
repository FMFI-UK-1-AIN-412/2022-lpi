import subprocess
import os
import sys

class DimacsWriter(object):
    """ A helper class that writes clauses to a DIMACS format file. """
    def __init__(self, filename, mode = 'w'):
        """ Create a new writer that writes to *filename*.

            You can use ``mode='a'`` to append to an existing file
            instead of rewriting it.
        """
        self.fn = filename
        self.f = open(filename, mode)

    def filename(self):
        """ Returns the filename that this writer writes to as a string."""
        return self.fn

    def writeLiteral(self, lit):
        """ Writes a single literal (positive or negative integer).

            Use finishClause to finis this clause (write a zero).
        """
        self.f.write('{} '.format(lit))

    def finishClause(self):
        """" Finishes current clause (writes a zero).

            Note that if no clause was started (through *writeLiteral*),
            it will create an empty clause, which is always false!
        """
        self.f.write(' 0\n')
        self.f.flush()

    def writeClause(self, clause):
        """ Writes a single clause.

            *clause* must be a list of literals (positive or negative integers).
        """
        for l in clause:
            self.writeLiteral(l)
        self.finishClause()

    def writeImpl(self, left, right):
        """ Writes an implication *left* => *right*. """
        self.writeClause([-left, right])

    def writeComment(self, comment):
        """ Writes a comment.

            Note that this does not work inside an unfinished clause!
        """
        for line in comment.split('\n'):
            self.f.write('c {}\n'.format(line))

    def closed(self):
        """ Returs True if the output file has been already closed. """
        return self.f.closed

    def close(self):
        """ Closes the output file. """
        self.f.close()


class SatSolver(object):
    """ A helper class that manages SAT solver invocation. """

    def __init__(self, solverPath = None):
        """ Creates a new SAT solver.

            Use *solverPath* to specify an optional location where to look
            for SAT solver binary (it will be looked for in a set of default
            locations).
        """

        self.paths = []
        if solverPath:
            self.paths.append(solverPath)

        if sys.platform.startswith('linux'):
            self.paths += [
                    './minisat',
                    '../tools/lin/minisat',
                    '../../tools/lin/minisat',
                    '../../../tools/lin/minisat'
                    '../../../../tools/lin/minisat'
                ]
        elif sys.platform.startswith('darwin'):
            self.paths += [
                    './minisat',
                    '../tools/mac/minisat',
                    '../../tools/mac/minisat',
                    '../../../tools/mac/minisat'
                    '../../../../tools/mac/minisat'
                ]
        elif sys.platform.startswith('win'):
            self.paths += [
                    'minisat.exe',
                    '..\\tools\\win\\minisat.exe',
                    '..\\..\\tools\\win\\minisat.exe',
                    '..\\..\\..\\tools\\win\\minisat.exe',
                    '..\\..\\..\\..\\tools\\win\\minisat.exe',
                ]
        else:
            pass # empty solver paths will fall back to try 'minisat'

        # default fall for all
        self.paths.append('minisat')

    def getSolverPath(self):
        """ Returns the path to solver binary. """
        for fn in self.paths:
            try:
                subprocess.check_output([fn, '--help'], stderr = subprocess.STDOUT)
                #sys.stderr.write('using sat solver:  "%s"\n' % fn)
                return fn
            except OSError:
                pass
        raise IOError('Solver executable not found!')

    def solve(self, theory, output):
        """ Use SAT solver to solve a theory, which is either the name
            of a file (in DIMACS format) or an instance of DimacsWriter.

            Writes the SAT solvers output to a file named *output*.

            Returns a tuple (sat, solution), where sat is True or False
            and solution is a list of positive or negative integers
            (an empty list if sat is False).
        """
        if isinstance(theory, DimacsWriter):
            if not theory.closed():
                theory.close()
            theory = theory.filename()

        try:
            self.output = subprocess.check_output(
                    [self.getSolverPath(), theory, output],
                    stderr = subprocess.STDOUT,

                    )
        except subprocess.CalledProcessError:
            # minisat has weird return codes
            pass

        with open(output) as f:
            sat = f.readline()
            if sat.strip() == 'SAT':
                sol = f.readline()
                return (
                        True,
                        [int(x) for x in sol.split()][:-1]
                )
            else:
                return (False, [])


# vim: set sw=4 ts=4 sts=4 et :
