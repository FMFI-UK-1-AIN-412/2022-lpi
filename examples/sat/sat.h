#include <vector>
#include <string>
#include <fstream>

namespace sat {

/**
 * A helper class that writes clauses to a DIMACS format file.
 */
class DimacsWriter
{
	std::string m_filename;
	std::ofstream m_f;

public:
	/**
	 * Create a new writer that writes to filename.
	 */
	DimacsWriter(const std::string &name, std::ios_base::open_mode mode = std::ios_base::out);

	/**
	 * Returns the filename that this writer writes to as a string.
	 */
	std::string filename() const { return m_filename; }

	/**
	 * Writes a single literal (positive or negative integer).
	 *
	 * Use finishClause to finis this clause (write a zero).
	 */
	void writeLiteral(int lit);

	/**
	 * Finishes current clause (writes a zero).
	 *
	 * Note that if no clause was started (through writeLiteral),
	 * it will create an empty clause, which is always false!
	 */
	void finishClause();

	/**
	 * Writes a single clause.
	 *
	 * *clause* must be a list of literals (positive or negative integers).
	 */
	void writeClause(const std::vector<int> &clause);

	/**
	 * Overloaded method.
	 */
	void writeClause(const std::initializer_list<int> &clause);

	/**
	 * Writes an implication left => right.
	 */
	void writeImpl(int left, int right);

	/**
	 * Writes a comment.
	 *
	 * Note that this does not work inside an unfinished clause!
	 */
	void writeComment(const std::string &comment);

	/**
	 * Returs True if the output file has been already closed.
	 */
	bool closed();

	/**
	 * Closes the output file.
	 */
	void close();
};

/**
 * A helper class that manages SAT solver invocation.
 */
class SatSolver
{
public:
	/**
	 * Result of a SAT solver invocation.
	 *
	 * vars contains a list of positive or negative
	 * integers, empty if sat is false
	 */
	struct Result {
		bool sat = false;
		std::vector<int> vars;
	};

	/**
	 * Creates a new SAT solver.
	 *
	 * Use *solverPath* to specify an optional location where to look
	 * for SAT solver binary (it will be looked for in a set of default
	 * locations).
	 */
	SatSolver(const std::string &solverPath = "");

	/**
	 * Returns the path to solver binary.
	 */
	std::string getSolverPath();

	/**
	 * Use SAT solver to solve a theory.
	 *
	 * Writes the SAT solvers output to a file named *output*.
	 *
	 * \param theoryFile name (path) of the CNF file that holds the theory
	 */
	Result solve(const std::string &theoryFile, const std::string &outputFile);

	/**
	 * Use SAT solver to solve a theory.
	 *
	 * Writes the SAT solvers output to a file named *output*.
	 *
	 * \param theoryWriter a DimacsWriter that was used to write the theory
	 */
	Result solve(DimacsWriter *theoryWriter, const std::string &outputFile);
};

} //namespace sat
