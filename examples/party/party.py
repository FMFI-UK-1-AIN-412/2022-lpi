# Toto je ukazkovy program, ktory ukazuje ako vytvorit vstup pre SAT solver,
# spustit ho a precitat a rozparsovat jeho vystup. Mozete ho skludom pouzit
# ako kostru vasho riesenia.

import os

# Nastavime cestu (PATH) k minisat programu.
# Toto by malo fungovat na linuxe aj windows (ta druha cesta sa odignoruje).
os.environ["PATH"] += os.pathsep + os.path.join("..","..","tools","lin")
os.environ["PATH"] += os.pathsep + os.path.join("..","..","tools","win")


Kim = 1
Jim = 2
Sarah = 3
intToName = ["", "Kim", "Jim", "Sarah"]


# Pomocna funkcia na zapis implikacie do suboru
def impl(subor, a, b):
    subor.write( "{0:d} {1:d} 0\n".format(-a, b) )
    
# Funkcia zapisujuca problem do vstupneho suboru SAT solvera v spravnom formate
def zapis_problem(subor):
    # implikacie - poziadavky moznych ucastnikov
    impl(subor, Kim, -Sarah)
    impl(subor, Jim, Kim)
    impl(subor, Sarah, Jim)
    # disjunkcia - niekto sa zucastni
    for osoba in range(Kim, Sarah+1):
        subor.write( "{0:d} ".format(osoba) )
    subor.write("0\n")

# Funkcia vypisujuca riesenie najdene SAT solverom z jeho vystupneho suboru
def vypis_riesenie(ries):
    # rozbijeme riesenie na cisla/premenne
    vs = ries.split()
    # zahodime ukoncovaciu 0
    vs = vs[0:-1]
    # vypiseme vyznam riesenia
    for v in vs:
        v = int(v)
        print("{0} {1}pojde na party".format(
                intToName[abs(v)],
                "ne" if v < 0 else ""))

def main():
    # Normalne by sme tu mozno nieco nacitavali zo standardneho vstupu,
    # ale tato uloha nema ziadny vstup.

    # otvorime subor, do ktoreho zapiseme vstup pre sat solver
    try:
        with open("vstup.txt", "w") as o:
            # zapiseme nas problem
            zapis_problem(o)
    except IOError as e:
        print("Chyba pri vytvarani vstupneho suboru ({0}): {1}".format(
                e.errno, e.strerror))
        return 1

    # spustime SAT solver
    os.system("minisat vstup.txt vystup.txt");

    # nacitame jeho vystup
    try:
        with open("vystup.txt", "r") as i:
            # prvy riadok je SAT alebo UNSAT
            sat = i.readline()
            if sat == "SAT\n":
                print("Riesenie:")
                # druhy riadok je riesenie
                ries = i.readline()
                vypis_riesenie(ries)
            else:
                print("Ziadne riesenie")
    except IOError as e:
        print("Chyba pri nacitavani vystupneho suboru ({0}): {1}".format(
                e.errno, e.strerror))
        return 1

    return 0

if __name__ == "__main__":
    main()
