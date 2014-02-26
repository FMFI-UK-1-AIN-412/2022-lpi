/**
 * Toto je ukazkovy program, ktory ukazuje ako vytvorit vstup pre SAT solver,
 * spustit ho a precitat a rozparsovat jeho vystup. Mozete ho skludom pouzit
 * ako kostru vasho riesenia.
 *
 * Tento program predpoklada, ze minisat / minisat.exe
 * sa nachadza
 * - Linux: v adresari, kam ukazuje PATH
 * - Windows: v adresari, kam ukazuje PATH, alebo v akt. adresari
 */
#include <iostream>
#include <fstream>
#include <string>
#include <cstdlib>

const int Kim = 1;
const int Jim = 2;
const int Sarah = 3;
const auto Everyone = { Kim, Jim, Sarah };
const char *IntToName[] = {"", "Kim", "Jim", "Sarah"};


/**
 * Pomocna funkcia na vypisovanie implikacii.
 */
void impl(std::ostream &s, int a, int b)
{
	s << -a << " " << b << " 0" << std::endl;
}

bool zapisProblem(const std::string &menoSuboru)
{
	// otvorime subor, do ktoreho zapiseme vstup pre sat solver
	std::ofstream o;
	o.open(menoSuboru);
	if (!o) {
		return false;
	}

	// zapiseme nas problem
	impl(o, Kim, -Sarah);
	impl(o, Jim, Kim);
	impl(o, Sarah, Jim);

	for(auto i : Everyone) {
		o << i << " ";
	}
	o << "0" << std::endl;

	// o (ofstream) tu (v destruktore) zavrie subor, co je dolezite,
	// lebo inac by v nom minisat este nmusel vidiet vsetko co sme zapisali
	return true;
}

bool vypisRiesenie(const std::string &menoSuboru)
{
	std::ifstream ifs;
	ifs.open(menoSuboru);
	if (!ifs) {
		return false;
	}

	// prvy riadok je SAT alebo UNSAT
	std::string sat;
	ifs >> sat;
	if (sat == "SAT") {
		std::cout << "Riesenie:" << std::endl;
		int v;
		// nacitame vsetky cisla/premenne
		while (ifs >> v) {
			// okrem poslednej ukoncovacej nuly
			if (v) {
				std::cout
					<< IntToName[abs(v)]
					<< (v<0 ? " ne" : " ")
					<< "pojde na party"
					<< std::endl;
			}
		}
	}
	else {
		std::cout << "Ziadne riesenie" << std::endl;
	}
	return true;
}

int main()
{
	// Normalne by sme tu mozno nieco nacitavali zo standartneho vstupu
	// i.e.  cin >> n;  a podobne, ale tato uloha nema ziadny vsetup.

	if (!zapisProblem("vstup.txt"))
	{
		std::cerr << "Chyba pri otvarani vstupneho suboru" << std::endl;
	}

	// spustime SAT solver
	system("minisat vstup.txt vystup.txt");

	if (!vypisRiesenie("vystup.txt"))
	{
		std::cerr << "Chyba pri otvarani vystupneho suboru" << std::endl;
	}

	return 0;
}
