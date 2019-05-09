Súťaž o najrýchlejší SAT solver
===============================

**Súťaž končí v nedeľu 29.5. 23:59:59. Riešenia po tomto termíne už nebudú
akceptované.**

Cieľom je naprogramovať čo najrýchlejší SAT solver.

[Technické požiadavky](#technické-detaily-riešenia) sú trochu iné ako
v [úlohe 9](../pu09), najmä:
- implementujte commandline program, ktorý dostane dva argumenty: meno vstupného a výstupného súboru
  (podobne ako minisat);
- vstupný súbor vo formáte DIMACS (s korektnou hlavičkou, môže obsahovať komentáre),
  max. 2048 premenných;
- výstupný súbor obsahuje na prvom riadku reťazec `SAT` alebo `UNSAT`, v prvom
  prípade obsahuje druhý riadok čísla s absolútnymi hodnotami 1, 2, … až
  najväčšia premenná, **ukončené nulou**.

## Odovzdávanie

Riešenie odovzdávajte do vetvy, ktorej názov začína na `sat` (napríklad do
vetvy `sat`, ktorú už v repozitároch máte vytvorenú) do adresára `prakticke/sat`
a vytvorte pull request **oproti vetve `sat`**.

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre `sat`](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Asat).

Môžete odovzdať viacero riešení (napríklad s rôznymi optimalizáciami) do
rôznych vetiev (začínajúcich na `sat`). Vo výsledkoch sa ukážu všetky
a hodnotiť vás budeme podľa najlepšie umiestneného z nich. V každej vetve
nahrávajte súbory normálne do adresára `prakticke/sat` a nezabudnite pre každú
vytvoriť pull request **oproti vetve `sat`**. Rôzne riešenia môžu byť v rôznych
jazykoch. Môžete napríklad odovzdať 4 rôzne riešenia do vetiev `sat`, `sat2`,
`sat-optimalizacia1` a `satRandom` (a na každú z nich vytvoriť pull request),
kde prvé tri budú mať v adresári `prakticke/sat` súbor `satsolver.py` a posledné
v adresári `prakticke/sat` súbory `satsolver.cpp` a `satsolver.h`.

V rámci jednej vetvy bude hodnotená naposledy odovzdaná verzia (commit). Ak
chcete aktualizovať riešenie vo vetve, ktorej ste už vytvorili pull request,
stačí do nej iba nahrať novú verziu riešenia (nový commit). Toto riešenie
bude potom pretestované s touto najnovšou verziou.

Odovzdávanie riešení v iných jazykoch ako python, C++ a Java konzultujte s cvičiacimi.

## Hodnotenie

SAT solvery budú vyhodnotené formou súťaže a budú usporiadané nasledovne:

1. Pre každú dvojicu (<var>solver</var>, <var>vstup</var>) sa vypočíta, koľko solverov
   vyriešilo daný <var>vstup</var> rýchlejšie. Ak <var>solver</var> nerieši <var>vstup</var> korektne, tak
   všetky solvery, ktoré ho riešia korektne, sú považované, že ho vyriešili
   rýchlejšie.
1. Pre každý solver sa vypočíta súčet cez všetky vstupy, koľko solverov bolo
   rýchlejších.
1. Solvery sa usporiadajú podľa súčtu z bodu 2.
1. Poradie študentov sa určí podľa umiestnenia ich najlepšieho solvera.
1. Študent na <var>i</var>-tom mieste získa max(0, 6 − <var>i</var>) bodov. Prvé miesto je najvyššie.

Riešenia budú hodnotené priebežne podľa časových možností ;) a výsledky
budú zverejňované na stránke https://dai.fmph.uniba.sk/courses/lpi/sat/.

**Súťaž končí v nedeľu 29.5. 23:59:59. Riešenia po tomto termíne už nebudú
akceptované.** Finálne výsledky a body budú zverejnené v krátkej dobe po tomto
termíne.

Riešenia budú kompilované a vyhodnocované na linuxe (64bit,
python aspoň 3.6, g++ aspoň 9, java OpenJDK aspoň 11).
Počítač, na ktorom sa bude vyhodnocovať, bude mať minimálne 4 jadrá.
Používajte ale iba veci, ktoré sú vo vašom jazyku štandardizované.
Nepoužívajte žiaden kód závislý na operačnom systéme, prípadne vašom
vývojovom prostredí.

## Technické detaily riešenia

Riešenie odovzdajte do vetvy začínajúcej sa na `sat` v adresári `prakticke/sat`.
Program [`test.py`](test.py) otestuje váš solver na rôznych vstupoch
(z adresára `testData`). Testovač by mal automaticky detegovať Python / C++ / Java
riešenia prítomné v adresári. Aby detekcia zafungovala správne, dbajte na správne pomenovanie
súborov (viď nižšie podľa jazyka) a tiež na veľké/malé písmenká.

Vaším riešením má byť konzolový program (žiadne GUI), ktorý dostane dva
argumenty: meno vstupného a výstupného súboru. Vstupný súbor bude v DIMACS
formáte s korektnou hlavičkou (môže obsahovať komentáre).

Do výstupného súboru váš program zapíše na prvý riadok buď `SAT` alebo `UNSAT`,
podľa toho, či je formula splniteľná. Ak je formula splniteľná, tak na druhý
riadok zapíše model (spĺňajúce ohodnotenie): medzerami oddelené čísla s
absolútnymi hodnotami 1, 2, … až najväčšia premenná. Kladné číslo znamená, že
premenná je nastavená na `true` a záporné, že je nastavená na `false`.

V [examples/satsolver](../../examples/satsolver) môžete nájsť kostru Java
riešenia, ktoré už má implementované korektné načítavanie a zápis riešenia
(možno nie najrýchlejšie `:-)`).

Za korektný beh programu sa považuje, iba keď váš program skončí s návratovou hodnotou 0,
nenulová hodnota sa považuje za chybu (Runtime Error). Toto je dôležité hlavne v C++
(`return 0;` na konci `main`), ak Python/Java korektne skončí, tak by mali vrátiť 0.

Môžete predpokladať, že počet premenných bude do 2048.

Automatické testy na Travis CI skontrolujú, či sa vaše riešenie dá skompilovať,
ale časy v nich nie sú veľmi smerodajné, vzhľadom na prostredie, v ktorom
tieto testy bežia.

### Python

Vaše riešenie musí obsahovať súbor `satsolver.py`.

### C++

Vaše riešenie musí obsahovať aspoň súbor `satsolver.cpp` a `CMakeLists.txt`,
pomocou ktorého sa kompiluje.

### Java

Vaše riešenie musí obsahovať súbor `SatSolver.java`, ktorý implementuje
triedu `SatSolver` s metódou `main`, a ideálne aj súbor `build.gradle`.
