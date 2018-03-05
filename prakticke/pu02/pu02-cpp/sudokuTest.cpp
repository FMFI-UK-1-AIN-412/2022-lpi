/**
 * Testovaci program pre sudoku kniznicu.
 * Kniznica musi byt v suboroch SudokuSover.h a SudokuSolver.cpp, musi implementovat
 * triedu SudokuSolver, ktora ma defaultny konstruktor a implementuje
 * metodu solve so spravnou signaturou (vid zadanie).
 *
 * Ak vasa kniznica pouziva (iba) triedy DimacsWriter / SatSolver
 * z ../examples/sat/, tento testovaci program by sa mal dat
 * skompilovat nasledovnym prikazom:
 *
 * g++ -std=c++11 -o sudokuTest sudokuTestest.cpp SudokuSolver.cpp ../../examples/sat/sat.cpp -I../../examples/sat/
 *
 */

#include <vector>
#include <string>
#include <iostream>
#include <algorithm>
#include "SudokuSolver.h"


typedef std::vector<int> Row;
typedef std::vector<Row> Sudoku;
struct SudokuPair {
	Sudoku i;
	Sudoku o;
};

std::ostream& operator<< (std::ostream& os, const Row &row)
{
	os << "( ";
	for(const auto &i : row)
		os << i << " ";
	os << ")";
	return os;
}

std::ostream& operator<< (std::ostream& os, const SudokuPair &s)
{
	std::cerr << "          INPUT              OUTPUT" << std::endl;
	for(unsigned int r = 0; r < std::max(s.i.size(), s.o.size()); ++r) {
		if (r<s.i.size()) {
			std::cerr << s.i[r];
		}
		else {
			std::cerr << "                    ";
		}
		std::cerr << "    ";
		if (r<s.o.size()) {
			std::cerr << s.o[r];
		}
		std::cerr << std::endl;
	}
	return os;
}

class Tester {
	int m_case = 0;
	int m_tested = 0;
	int m_passed = 0;
public:

	template<typename T>
	bool fail(const std::string &msg, const T &arg)
	{
		std::cerr << "ERROR: " << msg << " " << arg << std::endl;
		return false;
	}
	template<typename T1, typename T2>
	bool fail(const std::string &msg, const T1 &arg1, const T2 &arg2)
	{
		std::cerr << "ERROR: " << msg << " " << arg1 << " " << arg2 << std::endl;
		return false;
	}

	bool checkSize(const Sudoku &s)
	{
		if (s.size() != 9)
			return fail("Wrong number of rows in result", "");
		for(unsigned int r = 0; r < s.size(); ++r) {
			if (s[r].size() != 9)
				return fail("Wrong number of numbers in row", r);
		}
		return true;
	}

	bool checkList(const Row &l, const std::string &msg)
	{
		// should not happen
		if (l.size() != 9)
			return fail("Wrong result format ", msg, l);

		int x = 0;
		for(auto i : l)
			x |= (1 << i);
		if (x != (1023 - 1))
			return fail("Wrong numbers ", msg, l);

		return true;
	}

	bool checkInput(const Sudoku &i, const Sudoku &s)
	{
		for(unsigned int r = 0; r < i.size(); ++r)
			for(unsigned int c = 0; c < i[r].size(); ++c)
				if (i[r][c] != 0)
					if (i[r][c] != s[r][c])
						return fail("Result does not match input", r, c);
		return true;
	}

	bool checkRows(const Sudoku &s)
	{
		return std::all_of(s.begin(), s.end(),
				[this](const Row &r){ return checkList(r, "in row"); });
	}
	bool checkCols(const Sudoku &s)
	{
		for(unsigned int c = 0; c < 9; ++c) {
			Row l;
			for(const auto &row : s)
				l.push_back(row[c]);
			if (!checkList(l, "in col"))
				return false;
		}
		return true;
	}
	bool checkSquares(const Sudoku &s)
	{
		for(int sr=0; sr < 3; ++sr)
			for(int sc = 0; sc < 3; ++sc) {
				Row l;
				for(int r = 0; r < 3; ++r)
					for(int c = 0; c < 3; ++c)
						l.push_back(s[sr*3 + r][sc*3 + c]);
				if (!checkList(l, "in square"))
					return false;
			}
		return true;
	}

	bool checkTests(std::initializer_list<bool> tests)
	{
		m_tested += tests.size();
		bool ret = true;
		for(auto r : tests) {
			if (r) m_passed++;
			ret = ret && r;
		}
		return ret;
	}

	bool checkGood(const Sudoku &i, const Sudoku &s)
	{
		if (!checkSize(s)) {
			m_tested += 4;
			return false;
		}

		return checkTests({
				checkInput(i,s),
				checkRows(s),
				checkCols(s),
				checkSquares(s),
				});
	}

	bool checkBad(const Sudoku &s)
	{
		m_tested += 4;
		if (!checkSize(s)) {
			return false;
		}

		for(const auto &row: s)
			for(auto c: row)
				if (c)
					return fail("Nonzero in bad sudoku!", "");
		m_passed += 4;
		return true;
	}

	bool check(const Sudoku &i, bool good, const Sudoku &s)
	{
		if (good)
			return checkGood(i, s);
		else
			return checkBad(s);
	}

	void test(const Sudoku &i, bool good, const Sudoku &s)
	{
		m_case++;
		std::cerr << "Case " << m_case << ": ";
		if (check(i, good, s)) {
			std::cerr << "PASSED" << std::endl;
		}
		else {
			std::cerr << SudokuPair{i,s};
		}
	}

	bool status()
	{
		std::cerr << "TESTED " << m_tested << std::endl;
		std::cerr << "PASSED " << m_passed << std::endl;
		std::cerr << ( m_tested == m_passed ? "OK" : "ERROR" ) << std::endl;
		return m_tested == m_passed;
	}
};

int main()
{
	Tester t;
	Sudoku i;

	i = {
		{5, 3, 0, 0, 7, 0, 0, 0, 0},
		{6, 0, 0, 1, 9, 5, 0, 0, 0},
		{0, 9, 8, 0, 0, 0, 0, 6, 0},
		{8, 0, 0, 0, 6, 0, 0, 0, 3},
		{4, 0, 0, 8, 0, 3, 0, 0, 1},
		{7, 0, 0, 0, 2, 0, 0, 0, 6},
		{0, 6, 0, 0, 0, 0, 2, 8, 0},
		{0, 0, 0, 4, 1, 9, 0, 0, 5},
		{0, 0, 0, 0, 8, 0, 0, 7, 9},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i = {
		{1, 2, 3, 4, 5, 6, 7, 8, 9},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i ={
		{1, 1, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
	};
	t.test(i, false, SudokuSolver().solve(i));

	i = {
		{0, 9, 5, 0, 0, 3, 6, 0, 0},
		{0, 6, 0, 0, 5, 1, 0, 3, 8},
		{1, 8, 0, 0, 4, 6, 7, 0, 9},
		{5, 0, 4, 0, 2, 0, 0, 0, 6},
		{6, 1, 0, 4, 8, 0, 0, 2, 0},
		{8, 3, 0, 0, 0, 0, 0, 7, 0},
		{9, 5, 0, 7, 3, 4, 0, 6, 0},
		{0, 0, 6, 0, 0, 0, 4, 0, 0},
		{7, 0, 0, 0, 0, 2, 5, 9, 3},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i = {
		{5, 3, 4, 6, 7, 8, 9, 1, 0},
		{6, 7, 2, 1, 9, 5, 3, 4, 0},
		{1, 9, 8, 3, 4, 2, 5, 6, 0},
		{8, 5, 9, 7, 6, 1, 4, 2, 0},
		{4, 2, 6, 8, 5, 3, 7, 9, 0},
		{7, 1, 3, 9, 2, 4, 8, 5, 0},
		{9, 6, 1, 5, 3, 7, 2, 8, 0},
		{2, 8, 7, 4, 1, 9, 6, 3, 0},
		{3, 4, 5, 2, 8, 6, 1, 7, 0},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i = {
		{5, 3, 4, 6, 7, 8, 9, 1, 2},
		{6, 7, 2, 1, 9, 5, 3, 4, 8},
		{1, 9, 8, 3, 4, 2, 5, 6, 7},
		{8, 5, 9, 7, 6, 1, 4, 2, 3},
		{4, 2, 6, 8, 5, 3, 7, 9, 1},
		{7, 1, 3, 9, 2, 4, 8, 5, 6},
		{9, 6, 1, 5, 3, 7, 2, 8, 4},
		{2, 8, 7, 4, 1, 9, 6, 3, 5},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i = {
		{0, 0, 5, 7, 0, 9, 8, 0, 0},
		{0, 7, 0, 0, 8, 0, 0, 9, 0},
		{0, 8, 0, 4, 0, 2, 0, 3, 0},
		{0, 6, 4, 0, 5, 0, 3, 1, 0},
		{8, 0, 0, 0, 9, 0, 0, 0, 2},
		{7, 0, 0, 0, 0, 0, 0, 0, 9},
		{0, 0, 7, 6, 0, 4, 2, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{3, 0, 0, 0, 2, 0, 0, 0, 6},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i = {
		{0, 0, 5, 7, 0, 9, 8, 0, 0},
		{0, 7, 0, 0, 8, 0, 0, 9, 0},
		{0, 8, 0, 4, 0, 2, 0, 3, 0},
		{0, 6, 4, 0, 5, 0, 3, 1, 0},
		{8, 0, 0, 0, 9, 0, 0, 0, 2},
		{7, 0, 0, 0, 0, 0, 6, 0, 9},
		{0, 0, 7, 6, 0, 4, 2, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{3, 0, 0, 0, 2, 0, 0, 0, 6},
	};
	t.test(i, false, SudokuSolver().solve(i));

	i = {
		{0, 0, 6, 0, 4, 0, 7, 0, 0},
		{0, 0, 0, 1, 0, 2, 0, 0, 0},
		{2, 0, 0, 0, 3, 0, 0, 0, 4},
		{3, 0, 0, 0, 0, 0, 0, 0, 6},
		{0, 0, 8, 3, 0, 4, 5, 0, 0},
		{0, 2, 0, 0, 5, 0, 0, 7, 0},
		{0, 1, 0, 0, 9, 0, 0, 4, 0},
		{0, 5, 0, 0, 0, 0, 0, 9, 0},
		{8, 0, 4, 7, 0, 5, 2, 0, 3},
	};
	t.test(i, true, SudokuSolver().solve(i));

	i = {
		{0, 0, 6, 0, 4, 0, 7, 0, 0},
		{0, 0, 0, 1, 0, 2, 0, 0, 0},
		{2, 0, 0, 0, 3, 0, 0, 0, 4},
		{3, 0, 0, 8, 0, 0, 0, 0, 6},
		{0, 0, 8, 3, 0, 4, 5, 0, 0},
		{0, 2, 0, 0, 5, 0, 0, 7, 0},
		{0, 1, 0, 0, 9, 0, 0, 4, 0},
		{0, 5, 0, 0, 0, 0, 0, 9, 0},
		{8, 0, 4, 7, 0, 5, 2, 0, 3},
	};
	t.test(i, false, SudokuSolver().solve(i));

	return t.status() ? 0 : 1;
}
