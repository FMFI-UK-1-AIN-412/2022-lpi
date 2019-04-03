#pragma once
#include <string>
#include <unordered_map>
#include <unordered_set>

using Valuation = std::unordered_map<std::string, bool>;

// hash codes for Literal and Clause, so those can be used in hashed containers
class Literal;
class Clause;
namespace std
{
	template<> struct hash<Literal> { std::size_t operator()(const Literal &l) const noexcept; };
	template<> struct hash<Clause> { std::size_t operator()(const Clause &c) const noexcept; };
}


class Literal {
public:
	Literal(const std::string &name, bool neg = false);
	Literal(const char *name, bool neg = false);

	static Literal Lit(const std::string &name);
	static Literal Not(const std::string &name);
	static Literal Not(const Literal &lit);
	static Literal fromString(const std::string &s);

	std::string name() const { return m_name; }
	bool neg() const { return m_neg; }
	bool isSatisfied(const Valuation &v) const;
	std::string toString() const;

	Literal operator!() const;
	Literal operator-() const { return ! *this; }
	bool operator==(const Literal &other) const;
	bool operator!=(const Literal &other) const { return !(*this == other); }
	bool operator<(const Literal &other) const { return make_pair(name(), neg()) < make_pair(name(), neg()); }

private:
	std::string m_name;
	bool m_neg;
};

class Clause : public std::unordered_set<Literal> {
public:
	using unordered_set::unordered_set;

	static Clause fromString(const std::string &s);
	bool isSatisfied(const Valuation &v) const;
	std::string toString() const;
};

class Cnf : public std::unordered_set<Clause> {
public:
	using unordered_set::unordered_set;

	static Cnf fromString(const std::string &s);
	bool isSatisfied(const Valuation &v) const;
	std::string toString() const;
};

std::ostream& operator<< (std::ostream& os, const Literal &l);
std::ostream& operator<< (std::ostream& os, const std::unordered_set<Literal> &c);
std::ostream& operator<< (std::ostream& os, const std::unordered_set<Clause> &cnf);
