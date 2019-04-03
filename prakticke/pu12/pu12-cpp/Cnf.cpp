#include "Cnf.h"

#include <algorithm>
#include <iostream>
#include <regex>
#include <sstream>

Literal::Literal(const std::string &name, bool neg)
	: m_name(name)
	, m_neg(neg)
{
}

Literal::Literal(const char *name, bool neg)
	: m_name(name)
	, m_neg(neg)
{
}

Literal Literal::Lit(const std::string &name)
{
	return Literal(name);
}

Literal Literal::Not(const std::string &name)
{
	return Literal(name, true);
}

Literal Literal::Not(const Literal &lit)
{
	return !lit;
}

Literal Literal::fromString(const std::string &s)
{
	static std::string neg{"¬"};
	if (s.size() && (s[0] == '-'))
		return Literal(s.substr(1), true);
	else if (!s.compare(0, neg.size(), neg))
		return Literal(s.substr(neg.size()), true);
	else
		return Literal(s);
}

Literal Literal::operator!() const
{
	return Literal(name(), !neg());
}

bool Literal::isSatisfied(const Valuation &v) const
{
	return v.at(name());
}

std::string Literal::toString() const
{
	return std::string(neg() ? "-" : "") + name();
}

bool Literal::operator==(const Literal &other) const
{
	return name() == other.name() && neg() == other.neg();
}

bool Clause::isSatisfied(const Valuation &v) const
{
	return std::any_of(cbegin(), cend(), [&](const auto &l) { return l.isSatisfied(v); });
}

std::string Clause::toString() const
{
	std::stringstream ss;
	ss << *this;
	return ss.str();
}

Clause Clause::fromString(const std::string &s)
{
	auto trimLeft = std::find_if_not(s.begin(), s.end(),
		[](char c) { return std::isspace(c) || c == '('; } );
	auto trimRight = std::find_if_not(s.rbegin(), s.rend(),
		[](char c) { return std::isspace(c) || c == ')'; }).base();

	if (trimLeft >= trimRight)
		return {};

	std::string trimmed{trimLeft, trimRight};

	Clause c;
	std::regex re{"(\\s|∨)+"};
	std::transform(
		std::sregex_token_iterator{trimmed.begin(), trimmed.end(), re, -1},
		std::sregex_token_iterator{},
		std::inserter(c, c.end()),
		&Literal::fromString
	);
	return c;
}

bool Cnf::isSatisfied(const Valuation &v) const
{
	return std::all_of(cbegin(), cend(), [&](const auto &l) { return l.isSatisfied(v); });
}

std::string Cnf::toString() const
{
	std::stringstream ss;
	ss << *this;
	return ss.str();
}

Cnf Cnf::fromString(const std::string &s)
{
	if (s.empty()) return {};
	Cnf cnf;
	std::regex re("[,;\n]|∧");
	std::transform(
		std::sregex_token_iterator{s.begin(), s.end(), re, -1},
		std::sregex_token_iterator{},
		std::inserter(cnf, cnf.end()),
		&Clause::fromString
	);
	return cnf;
}

// hash codes for Literal and Clause, so those can be used in hashed containers
namespace std
{
std::size_t hash<Literal>::operator()(const Literal &l) const noexcept
{
	return std::hash<std::string>{}(l.name()) + l.neg();
}
std::size_t hash<Clause>::operator()(const Clause &c) const noexcept
{
	size_t h = 0;
	for (const auto &l : c)
		h ^= std::hash<Literal>{}(l);
	return h;
}
}

// ostream printing support for Literal, Clause, Cnf
std::ostream& operator<< (std::ostream& os, const Literal &l)
{
	os <<(l.neg() ? "-" : "") << l.name();
	return os;
}

template<typename Range>
std::ostream& printRange(
	std::ostream& os,
	const Range &range,
	const std::string &delim = ", ",
	const std::string &left = "",
	const std::string &right = ""
) {
	os << left;
	if (!range.empty()) {
		auto i = range.cbegin();
		os <<  *i;
		for (++i; i != range.cend(); ++i)
			os << delim << *i;
	}
	os << right;
	return os;
}

std::ostream& operator<< (std::ostream& os, const std::unordered_set<Literal> &c)
{
	return printRange(os, c, " ", "(", ")");
}

std::ostream& operator<< (std::ostream& os, const std::unordered_set<Clause> &cnf)
{
	return printRange(os, cnf, "; ");
}
