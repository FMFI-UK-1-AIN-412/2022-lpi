#include "sat.h"

#include <stdexcept>
#include <algorithm>
#include <iterator>
#include <sstream>
#include <string>

// for fileExists
#ifdef _WIN32
   #include <io.h>
   #define access    _access_s
#else
   #include <unistd.h>
#endif

namespace {
	bool fileExists(const char *fname) {
		return access(fname, 0) == 0;
	}
}

namespace sat {

DimacsWriter::DimacsWriter(const std::string &name, std::ios_base::open_mode mode)
{
	m_filename = name;
	m_f.open(m_filename);
	if (!m_f.good())
		throw std::runtime_error("Failed to open output file");
}

void DimacsWriter::writeLiteral(int lit)
{
	m_f << lit << " ";
}

void DimacsWriter::finishClause()
{
	m_f << "0" << std::endl;
}

void DimacsWriter::writeClause(const std::vector<int> &clause)
{
	for(auto lit : clause) {
		writeLiteral(lit);
	}
	finishClause();
}

void DimacsWriter::writeClause(const std::initializer_list<int> &clause)
{
	for(auto lit : clause) {
		writeLiteral(lit);
	}
	finishClause();
}

void DimacsWriter::writeImpl(int left, int right)
{
	writeClause({-left, right});
}

void DimacsWriter::writeComment(const std::string &comment)
{
	std::istringstream iss(comment);
	std::string line;
	while (std::getline(iss,line)) {
		m_f << "c " << line << std::endl;
	}
}

bool DimacsWriter::closed()
{
	return !m_f.is_open();
}

void DimacsWriter::close()
{
	m_f.close();
}



SatSolver::SatSolver(const std::string &solverPath)
{
	// TODO solverPath
}

std::string SatSolver::getSolverPath()
{
	static const char* paths[] = {
		"minisat",
#if defined(__linux__)
		"./minisat",
		"../tools/lin/minisat",
		"../../tools/lin/minisat",
		"../../../tools/lin/minisat",
		"../../../../tools/lin/minisat",
#elif defined(__APPLE__) && defined(__MACH__)
        "./minisat",
		"../tools/mac/minisat",
		"../../tools/mac/minisat",
		"../../../tools/mac/minisat",
		"../../../../tools/mac/minisat",
#elif defined(_WIN32)
		"minisat.exe",
		"..\\..\\tools\\win\\minisat.exe",
		"..\\..\\..\\tools\\win\\minisat.exe",
		"..\\..\\..\\..\\tools\\win\\minisat.exe",
#endif
	};
	for (const auto path : paths) {
		if (fileExists(path))
			return path;
	}
	return "minisat";
}

SatSolver::Result SatSolver::solve(const std::string &theoryFile, const std::string &outputFile)
{
	system((getSolverPath()
				+ " " + theoryFile
				+ " " + outputFile
				+ " >solver_out.txt 2>solver_err.txt").c_str());

	Result res;

	std::ifstream fi;
	fi.open(outputFile);
	if (!fi.good()) {
		return res;
	}

	std::string sat;
	fi >> sat;

	if (sat == "SAT") {
		res.sat = true;
		std::copy(std::istream_iterator<int>(fi),
				std::istream_iterator<int>(),
				std::back_inserter<std::vector<int> >(res.vars));
		// get rid of the 0 at the end
		if (res.vars.back() == 0) {
			res.vars.pop_back();
		}
	}

	return res;
}

SatSolver::Result SatSolver::solve(DimacsWriter *theoryWriter, const std::string &outputFile)
{
	if (!theoryWriter->closed())
	{
		theoryWriter->close();
	}
	return solve(theoryWriter->filename(), outputFile);
}

} //namespace sat
