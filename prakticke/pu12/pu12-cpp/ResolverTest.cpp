#include "Cnf.h"

#include "Resolver.h"
#include <chrono>
#include <iostream>

Literal operator "" _l(const char *s, std::size_t len) { return Literal(std::string(s, len)); }


using Clock = std::chrono::steady_clock;
using millis = std::chrono::duration<float, std::milli>;

class Tester {
	int m_case = 0;
	int m_tested = 0;
	int m_passed = 0;

public:
	Clock::duration m_time = Clock::duration::zero();

	template<typename T>
	bool compare(const T &result, const T &expected, const std::string &msg)
	{
		m_tested++;
		if (result == expected) {
			m_passed++;
			return true;
		}
		else {
			std::cerr << "    Failed: " << msg << ":" << std::endl;
			std::cerr << "      got " << result << " expected: " << expected << std::endl;
			return false;
		}
	}

	template<typename T>
	void fail(const std::string &msg, const T &arg)
	{
		std::cerr << "ERROR: " << msg << " " << arg << std::endl;
	}

	template<typename T1, typename T2>
	void fail(const std::string &msg, const T1 &arg1, const T2 &arg2)
	{
		std::cerr << "ERROR: " << msg << " " << arg1 << " " << arg2 << std::endl;
	}

	template<typename... Args>
	void startCase(Args... args) {
		std::cerr << "CASE " << ++m_case << ": ";
		std::begin({ (std::cerr << args, 0)... });
		std::cerr << std::endl;
	}

	template<typename Clauses>
	std::unordered_set<Clause> asSet(const Clauses &cs) {
		return {std::begin(cs), std::end(cs)};
	}

	void testResolve(const Clause &a, const Clause &b, const std::unordered_set<Clause> &expected) {

		startCase("Resolve ", a, "; ", b, " |- ", expected);

		auto start = Clock::now();
		std::unordered_set<Clause> resolved = Resolver::resolve(a, b);
		auto duration = Clock::now() - start;
		m_time += duration;

		compare(resolved, expected, "resolved clauses");
	}

	void testResolve(const std::string &a, const std::string &b, const std::string &expected) {
		testResolve(Clause::fromString(a), Clause::fromString(b),
			Cnf::fromString(expected)
		);
	}
	void testResolve2(const std::string &a, const std::string &b, const std::string &expected) {
		testResolve(a, b, expected);
		testResolve(b, a, expected);
	}

	void testIsSatisfiable(bool expected, const Cnf &theory, const std::string &description)
	{
		startCase(description);
		auto start = Clock::now();
		bool isSat = Resolver::isSatisfiable(theory);
		auto duration = Clock::now() - start;
		m_time += duration;

		if (compare(isSat, expected,"Resolver::isSatisfiable"))
			std::cerr << "PASSED in " << millis(duration).count() << "ms\n";
	}

	void testIsSatisfiable(bool expected, const Cnf &theory)
	{
		testIsSatisfiable(expected, theory, theory.toString());
	}

	bool status() {
		std::cerr << std::endl;
		std::cerr << "TESTED " << m_tested << std::endl;
		std::cerr << "PASSED " << m_passed << std::endl;
		std::cerr << "SUM(time) " << millis(m_time).count() << "ms" << std::endl;
		std::cerr << ( m_tested == m_passed ? "OK" : "ERROR" ) << std::endl;
		return m_tested == m_passed;
	}

};


Cnf T(const std::string &theory) { return Cnf::fromString(theory); }
Literal L(int i) { return Literal::fromString(std::to_string(i)); }
Clause C(int a, int b) { return {L(a), L(b)}; }
Clause C(int a, int b, int c) { return {L(a), L(b), L(c)}; }
Cnf trueChain(int n)
{
	Cnf cnf;
	cnf.insert(C(1, 2));
	cnf.insert(C(-n, -1));
	for (int i = 0; i < n; ++i)
		cnf.insert(C(-i, i + 1));
	return cnf;
}

Cnf falseChain(int n)
{
	Cnf cnf;
	cnf.insert(C(1, 2));
	cnf.insert(C(-n, -2));
	for (int i = 0; i < n; ++i)
		cnf.insert(C(-i, i + 1));
	return cnf;
}

int main()
{
	Tester t;

	t.testResolve2("p", "q", {});
	t.testResolve2("p", "-p", "()");
	t.testResolve2("-p q", "p -q","q -q; -p p");
	t.testResolve2("p -p", "p", "p");
	t.testResolve2("p -p", "-p", "-p");
	t.testResolve2("p -p", "p -p", "p -p");

	t.testResolve2("(-jim kim)", "(-sarah -kim)", "-jim -sarah");

	std::cerr << std::endl;

	t.testIsSatisfiable(true, {}, "empty theory");
	t.testIsSatisfiable(false, T("()"), "only empty clause");
	t.testIsSatisfiable(false, T(";p"));

	t.testIsSatisfiable(true, T("p;p"));
	t.testIsSatisfiable(true, T("p;q"));
	t.testIsSatisfiable(true, T("p -p"));
	t.testIsSatisfiable(true, T("p -p; p"));
	t.testIsSatisfiable(true, T("p -p; -p"));
	t.testIsSatisfiable(false, T("p;-p"));
	t.testIsSatisfiable(true, T("p q r s t u v; -p; -q; -r; -s; -t; -u"));
	t.testIsSatisfiable(false, T("p q r s t u v; -p; -q; -r; -s; -t; -u; -v"));
	t.testIsSatisfiable(false, T("p q r s t u v; p q r s t u v; -p; -q; -r; -s; -t; -u; -v"));
	t.testIsSatisfiable(true, T("v u t s r q p; -p; -q; -r; -s; -t; -u"));
	t.testIsSatisfiable(false, T("v u t s r q p; v u t s r q p; -p; -q; -r; -s; -t; -u; -v"));

	t.testIsSatisfiable(true, T(
		"¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah"
	), "kim jim sara");
	t.testIsSatisfiable(false, T(
		"¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; ¬kim"
	), "kim jim sara |= kim");
	t.testIsSatisfiable(false, T(
		"¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; sarah"
	), "kim jim sara |= ¬sarah");
	t.testIsSatisfiable(true, T(
		"¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; ¬jim"
	), "kim jim sara |≠ jim");
	t.testIsSatisfiable(true, T(
		"¬kim ∨ ¬sarah; ¬jim ∨ kim; ¬sarah ∨ jim; kim ∨ jim ∨ sarah; jim"
	), "kim jim sara |≠ ¬jim");

	t.testIsSatisfiable(true, trueChain(4), "SAT chain 4");
	t.testIsSatisfiable(false, falseChain(4), "UNSAT chain 4");
	t.testIsSatisfiable(true, trueChain(20), "SAT chain 20");
	t.testIsSatisfiable(false, falseChain(20), "UNSAT chain 20");

	/*
	t.testIsSatisfiable(false, Cnf{
		C(1,2,3), C(4,5,6), C(7,8,9), C(-2,-1), C(-3,-1), C(-3,-2), C(-5,-4), C(-6,-4),
		C(-6,-5), C(-8,-7), C(-9,-7), C(-9,-8), C(-4,-1), C(-7,-1), C(-7,-4),
		C(-5,-2), C(-8,-2), C(-8,-5), C(-6,-3), C(-9,-3), C(-9,-6), C(-1,-5),
		C(-4,-2), C(-4,-8), C(-7,-5), C(-1,-9), C(-7,-3), C(-2,-4), C(-5,-1),
		C(-5,-7), C(-8,-4), C(-2,-6), C(-5,-3), C(-5,-9), C(-8,-6), C(-3,-7),
		C(-9,-1), C(-3,-5), C(-6,-2), C(-6,-8), C(-9,-5)
		}, "q3");
	*/

	return t.status() ? 0 : 1;
}
