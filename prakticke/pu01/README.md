Cvičenie 1
==========

Vašou hlavnou úlohou na tomto cvičení je:
* vytvoriť si konto na https://github.com/ (ak ešte nemáte)
* vyplniť [registračný formulár](http://dai.fmph.uniba.sk/courses/lpi/register/),
  aby sme mohli spárovať vaše GitHub konto a vytvoriť vám repozitáre
* po vyriešení týchto cvík si **odložiť riešenie 2. príkladu**;
  na budúcich cvičeniach si ukážeme, ako sa odovzdá v github-e.

SAT solver
----------
SAT solverov je veľa, budeme používať [MiniSAT](http://minisat.se/).
Binárka pre Windows sa dá stiahnuť priamo na ich stránke, ale potrebuje ešte
2 knižnice (cygwin1.dll, cygz.dll). Všetky tri súbory sa nachádzajú
v adresári s [nástrojmi](../../tools/).

Všetky potrebné súbory k tomuto cvičeniu si môžete stiahnuť pohromade
ako jeden zip súbor
[pu01.zip](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu01.zip).

## 1. príklad

Chceme na párty pozvať aspoň niekoho z trojice Jim, Kim a Sára,
bohužiaľ každý z nich má nejaké svoje podmienky.

* Sára nepôjde na párty, ak pôjde Kim.
* Jim pôjde na párty, len ak pôjde Kim.
* Sára nepôjde bez Jima.

Zapísané v logike prvého rádu (v jazyku s indivíduovými konštantami `Kim`,
`Jim`, `Sarah` a predikátovým symbolom <code>pojde(<var>x</var>)</code>
s významom „<var>x</var> pôjde na párty“):
```
pojde(kim) → ¬pojde(sarah)
pojde(jim) → pojde(kim)
pojde(sarah) → pojde(jim)
pojde(kim) ∨ pojde(jim) ∨ pojde(sarah)
```

Prerobené do CNF (konjunktívnej normálnej formy):
```
¬pojde(kim) ∨ ¬pojde(sarah)
¬pojde(jim) ∨ pojde(kim)
¬pojde(sarah) ∨ pojde(jim)
pojde(kim) ∨ pojde(jim) ∨ pojde(sarah)
```

### DIMACS CNF formát ###

Keďže SAT solver nerozumie významu atómov a nepotrebuje teda ich textovú
reprezentáciu, pre efektívnejšiu prácu so vstupom (hlavne na súťažiach SAT
solverov `;-)`) sa používa číselný formát DIMACS, v ktorom sú atómy
reprezentované číslami, negované atómy zápornými číslami a klauzuly ako
postupnosti čísel ukončené nulou.

Prvý riadok môže obsahovať hlavičku s typom súboru (existuje variant, ktorý
reprezentuje disjunktívnu normálnu formu) a počtom premenných a klauzúl, ale
väčšina SAT solverov ju nepotrebuje a je lepšie ju vynechať (hlavne ak vopred
neviete koľko klauzúl bude / zabudnete ju upraviť keď meníte vstup).
Jednoriadkové komentáre sa píšu s písmenom `c` v prvom stĺpci (ako vo
FORTRANe).

```
p cnf VARS CLAUSES
1 2 -3 0
...
```
```
p cnf 3 4
c -pojde(kim) v -pojde(sarah)
-1 -3 0
c -pojde(jim) v pojde(kim)
-2 1 0
c -pojde(sarah) v pojde(jim)
-3 2 0
c pojde(kim) v pojde(jim) v pojde(sarah)
1 2 3 0
```

Aby bola práca s DIMACS CNF súbormi vo Windows jednoduchšia, budeme im dávať
príponu `.txt`, t.j. budeme sa tváriť, ako by to boli obyčajné textové súbory.

### SAT solver ###

Spustíme SAT solver, ako parameter dáme meno vstupného súboru. MiniSAT
normálne iba vypíše, či je vstup splniteľný. Ak chceme aj nejaký výstup, tak
dáme ešte meno výstupného súboru (MiniSAT ho vytvorí/prepíše.)
```
$ minisat party.txt party.out
...
SATISFIABLE
$ cat party.out
SAT
1 -2 -3 0
```

#### Hľadanie riešení ####

MiniSAT nájde len nejaké riešenie. Ak chceme nájsť ďalšie, môžeme mu
povedať, že toto konkrétne nechceme (nemajú byť naraz pravdivé resp.
nepravdivé tieto atómy). Toto riešenie je
`pojde(kim) ∧ ¬pojde(jim) ∧ ¬pojde(sarah)`, znegovaním dostaneme
`¬pojde(kim) ∨ pojde(jim) ∨ pojde(sarah)`, čo je priamo disjunktívna klauza
a môžeme ju pridať k zadaniu:

```
p cnf 3 5
-1 -3 0
-2 1 0
-3 2 0
1 2 3 0
c nechceme riesenie 1 -2 -3
-1 2 3 0
```
```
$ minisat party.txt party.out
...
SATISFIABLE
$ cat party.out
SAT
1 2 -3 0
```

Ak to zopakujeme ešte raz, nenájdeme už žiadne riešenie:
```
p cnf 3 6
-1 -3 0
-2 1 0
-3 2 0
1 2 3 0
c nechceme riesenie 1 -2 -3
-1 2 3 0
c nechceme riesenie 1 2 -3
-1 -2 3 0
```
```
$ minisat party.txt party.out
...
UNSATISFIABLE
$ cat party.out
UNSAT
```

#### Dokazovanie ####

Vidíme, že v obidvoch riešeniach sme mohli pozvať Kim. Mali by sme teda byť
schopní **dokázať**, že **logickým dôsledkom** našich predpokladov (vstupné
štyri tvrdenia) je, že Kim pôjde na párty (hovoríme aj, že toto tvrdenie
**vyplýva** z predpokladov).

So SAT solverom to môžeme spraviť tak, že sa ho spýtame, či existuje možnosť,
že naše predpoklady platia, ale Kim na párty nepôjde (t.j. platí negácia
tvrdenia, ktoré chceme dokázať). Ak SAT solver riešenie nájde, tak nám našiel
„protipríklad“: predpoklady platia, ale naše tvrdenie nie. Ak SAT solver
riešenie nenájde, tak to znamená, že vždy, keď platili predpoklady, tak platilo
aj naše tvrdenie.

```
p cnf 3 4
c nasa "teoria"
-1 -3 0
-2 1 0
-3 2 0
1 2 3 0

c chceme dokazat cielove tvrdenie "pojde(kim)"
c zapiseme jeho negaciu "¬pojde(kim)"
-1 0
```
```
$ minisat party.txt party.out
...
UNSATISFIABLE
$ cat party.out
UNSAT
```

*Poznámka:* Je dobré vždy najskôr napísať len „teóriu“ a overiť si, či je
**splniteľná** („bezosporná“), t.j. či má riešenie, a až potom pridať
negáciu dokazovaného tvrdenia.

*Otázka:* Je problém, že keď máme nesplniteľnú („spornú“) teóriu, tak SAT
solver vlastne povie, že z nej vyplýva čokoľvek?

## 2. Russian spy puzzle (2b)

> Máme tri osoby, ktoré sa volajú Stirlitz, Müller a Eismann.
> Vie sa, že práve jeden z nich je Rus,
> kým ostatní dvaja sú Nemci.
> Navyše každý Rus musí byť špión.
> 
> Keď Stirlitz stretne Müllera na chodbe, zavtipkuje:
> „Vieš, Müller, ty si taký Nemec, ako som ja Rus.“
> Je všeobecne známe, že Stirlitz vždy hovorí pravdu, keď vtipkuje.
> 
> Máme rozhodnúť, či Eismann nie je ruský špión.
> 
> — Andrei Voronkov, [www.voronkov.com](https://web.archive.org/web/20120715172242/http://www.voronkov.com/lics_doc.cgi?what=slides-&n=satisfiability)

*Na riešenie / testovanie tejto úlohy môžete použiť aj
[online editor / testovač](https://dai.fmph.uniba.sk/courses/lpi/russianSpy/).*

Prvá úloha pri zápise nejakého problému v logike je vždy vymyslieť,
ako budeme **reprezentovať** jednotlivé objekty, vlastnosti, vzťahy.
Musíme si dať pozor na to, aby naša reprezentácia:
  * nepripúšťala nejaké nečakané možnosti („Eismann
    nie je Rus ani Nemec“, „Eismann je zároveň Rus aj Nemec“), teda
    aby zohľadňovala naše intuitívne **znalosti na pozadí** (background
    knowledge), ale zároveň tiež, aby
  * nepredpokladala niečo, čo nemusí byť zrejmé priamo zo zadania,
    a teda nemusí byť pravda (byť špiónom nie je to isté, čo byť Rusom:
    dôkaz tejto úlohy by to zrovna nemalo ovplyvniť, ale to si nemôžeme
    byť vopred istí).

Na ukážku budeme používať tri indivíduové konštanty, jednu pre každého
z ľudí, a tri predikáty, ktoré hovoria, či je ich argument Rus, Nemec,
respektíve špión (keďže zo zadania je zrejmé, že každý je *buď Nemec alebo
Rus*, tak by nám mohol stačiť aj iba jeden predikát namiesto týchto dvoch).

|Konštanta| Význam  | Predikát                     | Význam                |
|---------|---------|------------------------------|-----------------------|
| `E`     | Eismann | <code>r(<var>X</var>)</code> | <var>X</var> je Rus   |
| `M`     | Müller  | <code>n(<var>X</var>)</code> | <var>X</var> je Nemec |
| `S`     | Stirlitz| <code>s(<var>X</var>)</code> | <var>X</var> je špión |

Potom napríklad atóm `s(S)` znamená „Stirlitz je špión“.

Táto reprezentácia umožňuje, aby niekto z nich nebol ani Nemec, ani Rus, alebo
bol oboje. Ako prvé teda napíšeme formuly, ktoré zabezpečia, že každý z nich je
buď Nemec, alebo Rus, ale nie oboje. Jednou z možností je napríklad tvrdenie
„<var>X</var> je Rus práve vtedy, keď <var>X</var> nie je Nemec“, čo zapíšeme
ako `r(X) ↔︎ ¬n(X)` (inými slovami: povedali sme, že <code>r(<var>X</var>)</code> je to isté čo
<code>¬n(<var>X</var>)</code>, a ak zvolíme, či platí jedno, určili sme aj druhé).

Teraz môžeme zapísať všetky podmienky zo zadania:

1. <var>X</var> je buď Rus alebo Nemec: `(r(S) ↔︎ ¬n(S)) ∧ (r(M) ↔︎ ¬n(M)) ∧ (r(E) ↔︎ ¬n(E))`
2. práve jeden je Rus, ostatní dvaja sú Nemci:
  `(r(S) ∧ n(M) ∧ n(E)) ∨ (n(S) ∧ r(M) ∧ n(E)) ∨ (n(S) ∧ n(M) ∧ r(E))`
3. každý Rus musí byť špión:
  `(r(S) → s(S)) ∧ (r(M) → s(M)) ∧ (r(E) → s(E))`
4. Stirlitz: „Müller, ty si taký Nemec, ako som ja Rus“:
  `n(M) ↔︎ r(S)`

Samozrejme, aby sme vyrobili vstup pre SAT solver, musíme všetky formuly upraviť
do konjunktívnej normálnej formy:
* Ekvivalencie sa len rozpíšu ako dve implikácie (spojené konjunkciou).
* Implikácie prepíšeme pomocou vzťahu `(A → B) ⇔ (¬A ∨ B)`
* Formulu z bodu 2 musíme „roznásobiť“, čím nám vznikne 27 disjunkcií s tromi
  atómami (z každej zátvorky jeden).

Na vyriešenie úlohy ale potrebujeme dokázať, že *Eismann nie je ruský špión*.
Potrebujeme teda ešte zapísať toto tvrdenie, znegovať ho, previesť do CNF.


**Zapísanú teóriu (so „symbolickými“, slovnými atómami, nie číslami) uložte do
súboru `spyTheory.txt` a cieľové tvrdenie do súboru `spyGoal.txt`. Obidva
súbory si odložte, na budúcich cvičeniach si ukážeme ako ich odovzdať.**

Príklad `spyTheory.txt`:
```
¬r(S) ∨ ¬n(S)
n(S) ∨ r(S)
¬r(M) ∨ ¬n(M)
n(M) ∨ r(M)
¬r(E) ∨ ¬n(E)
n(E) ∨ r(E)

...
```

Vaše riešenie si môžete odskúšať buď spustením programu [`test.py`](test.py) alebo v
[online editore / testovači](https://dai.fmph.uniba.sk/courses/lpi/russianSpy/).


### Test so SAT solverom

Keď budete chcieť vaše riešenie vyskúšať so SAT solverom, stačí len zmeniť
všetky atómy na čísla (search&replace je váš priateľ :-), negácie na mínus a
zmeniť disjunkcie na nulou ukončené postupnosti čísel.

Môžete použiť priložený pythonovský program [`text2dimacs.py`](text2dimacs.py),
ktorý vám takýto súbor „preloží“ na číselný vstup pre SAT solver.
Rovnako môžete použiť aj
[online konvertor](https://dai.fmph.uniba.sk/courses/lpi/text2dimacs/).

Keď pustíte SAT solver iba s teóriou, nájde možné riešenie, ako by to mohlo
byť. Ak SAT solver povie, že teória je nesplniteľná, tak je niekde problém.
Môžete použiť podobný postup ako v úlohe 1 a získať všetky možnosti (malo by
ich byť 8).

Na dokázanie požadovaného tvrdenia *Eismann nie je ruský špión*, treba negáciu
tohoto tvrdenia pridať k ostatným. Ak SAT solver povie, že teória je už
nesplniteľná, negácia nemôže platiť spolu s predpokladmi, a teda musí určite
platiť pôvodné, nenegované tvrdenie.
