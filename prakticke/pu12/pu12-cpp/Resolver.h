#include "Cnf.h"

class Resolver
{
public:
	static std::unordered_set<Clause> resolve(const Clause &a, const Clause &b);
	static bool isSatisfiable(const Cnf &theory);
};
