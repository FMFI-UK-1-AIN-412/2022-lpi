Cvičenie 8
==========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 21.4. 23:59:59.**

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`pu08.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu08.zip).

Príprava na SAT solver
----------------------

Na tomto a nasledujúcom cvičení naprogramujete SAT solver, ktorý zisťuje, či je
vstupná formula (v konjunktívnej normálnej forme) splniteľná.

Na prednáške ste videli základnú kostru metódy DPLL, ktorej hlavnou ideou je
propagácia klauzúl s iba jedným atómom (_jednotková klauzula_,
<i lang="en">unit clause</i>). Tá ale hovorí o veciach ako _vymazávanie
literálov_ z klauzúl a _vymazávanie klauzúl_, čo sú veci, ktoré nie je také
ľahké efektívne (či už časovo alebo pamäťovo) implementovať, hlavne ak počas
<i lang="en">backtrack</i>ovania treba zmazané literály resp. klauzuly správne
naspäť obnovovať.

Na tomto cvičení si preto naprogramujeme techniku _sledovaných literálov_,
ktorá výrazne zjednodušuje „menežment“ literálov, klauzúl a dátových štruktúr.
Na budúcom cvičení ju použijeme pri implementácii samotnej metódy DPLL.

<i>Terminologická poznámka.</i> Vo výrokovej logike sa na atómy často
pozerá, akoby nemali žiadnu štruktúru. Hovorí sa im potom <dfn>výrokovologické
premenné</dfn> (<dfn lang="en">propositional variables</dfn>) alebo iba
**<dfn>premenné</dfn>** (<dfn lang="en">variables</dfn>). Zaujíma nás totiž
iba to, akú pravdivostnú hodnotu im priraďuje ohodnotenie. V tomto
a nasledujúcom praktickom cvičení budeme používať tento pojem.

## Sledované literály (<i lang="en">watched literals</i>)

Základným problémom pri DPLL metóde je vedieť povedať, či klauzula:
*   už obsahuje nejaký literál ohodnotený `true` (a teda je už _pravdivá_), alebo
*   obsahuje práve jeden neohodnotený literál (a teda je _jednotková_), alebo
*   už má všetky literály ohodnotené `false` (a teda je _nepravdivá_ a treba
    <i lang="en">backtrack</i>ovať).

Namiesto mazania / obnovovania literálov a klauzúl budeme v každej klauzule
_sledovať_ (označíme si) dva jej literály (<i lang="en">watched literals</i>), pričom budeme požadovať (pokiaľ je to možné), aby každý z nich
- buď ešte nemal priradenú hodnotu,
- alebo mal priradenú hodnotu `true`.

Ak nejaký literál počas prehľadávania nastavíme na `true`, tak očividne
nemusíme nič meniť. Ak ho nastavíme na `false` (lebo sme napríklad jeho
komplement (negáciu) nastavili na `true`), tak pre každú klauzulu, v ktorej je
sledovaný, musíme nájsť nový literál, ktorý spĺňa horeuvedené podmienky. Môžu
nastať nasledovné možnosti:
- našli sme iný literál, ktorý je buď nenastavený, alebo je `true`, odteraz
  sledujeme ten,
- nenašli sme už literál, ktorý by spĺňal naše podmienky (všetky ostatné sú
 `false`):
    - ak druhý sledovaný literál je `true`, tak to nevadí (klauzula je aj tak pravdivá),
    - ale ak je druhý literál _nenastavený_, tak nám práve vznikla jednotková klauzula, a mali by sme ho
      nastaviť na `true`.
    - podľa toho, ako presne implementujeme propagáciu, sa nám môže stať, že sa
      dostaneme do momentu, že aj druhý sledovaný literál sa práve stal `false`,
      v tom prípade sme práve našli nepravdivú klauzulu a musíme <i lang="en">backtrack</i>ovať.

Bonus navyše: ak <i lang="en">backtrack</i>ujeme (meníme nejaký `true` alebo `false` literál naspäť
na nenastavený), tak nemusíme vôbec nič robiť (so sledovanými literálmi v klauzulách;
samotný literál / premennú samozrejme musíme korektne „odnastaviť“).

### Príklad

Majme klauzulu `a -b c -d` a zatiaľ žiadne premenné / literály nie sú nastavené.
Na začiatku v nej začneme teda sledovať napríklad prvé dva literály
(`?` - nenastavený, `1` - `true`, `0` - `false`, `^` - sledovaný):

```
?  ? ?  ?
a -b c -d
^  ^
```

Teraz nastavíme (z nejakého dôvodu) `-a` na `true`, takže `a` sa stane `false`. Tým
sa nám porušila podmienka, že sledované literály sú `true` alebo nenastavené, takže
musíme nájsť nový namiesto `a`, t.j. napríklad prejsť ostatné a nájsť nový pravdivý alebo
nenastavený (a iný ako ten druhý sledovaný!).

```
0  ? ?  ?
a -b c -d
   ^ ^
```


Ak by sme nastavili nejaký nesledovaný, tak sa nič nedeje:

```
0  ? ?  0
a -b c -d
   ^ ^
```

Keď ale teraz nastavíme napr. `b` na `true`, tak musíme zmeniť ten prvý sledovaný,
ale nemáme ho už kam presunúť (nevieme nájsť iný nenastavený alebo pravdivý)!
(*Poznámka: `findNewWatch` má v tomto prípade vrátiť false a nepresunúť nič*).
Tým vieme, že okrem toho druhého sledované, sú všetky ostatné určite `false`.
Ak je ten druhý sledovaný literál nenastavený, tak máme jednotkovú klauzulu:

```
0  0 ?  0
a -b c -d
   ^ ^
```

Kvôli tomu, že v tomto prípade nepresunieme sledovaný literál (a na jednotkových
klauzulách sme teda „porušili“ tú podmienku pre ne), tak sa môžeme pri postupnom
nastavovaní literálov z jednotkových klauzúl dostať aj do situácie, že ten druhú
sledovaný literál nie je ešte nenastavený, ale je už false:

```
0  0 0  0
a -b c -d
   ^ ^
```

V tomto prípade sme práve našli „spor“ (nepravdivú klauzulu) a mali by sme
<i lang="en">backtrack</i>ovať (alebo ukončiť hľadanie s `UNSAT`, ak už
nemáme ďalšie možnosti na <i lang="en">backtrack</i>ovanie).

## Implementácia

Sledovanie literálov a jeho zmeny pri zmenách čiastočného ohodnotenia
premenných doimplementujeme do tried na reprezentáciu formúl v CNF [z minulých
cvičení](../pu04) a do novej triedy `Theory`, ktorá predstavuje stav behu algoritmu DPLL,
hlavne formulu v `Cnf` (metóda `cnf()`), s ktorou algoritmus pracuje.
Tieto triedy spolu tvoria knižnicu [Theory.java](java/Theory.java).

### Premenné a literály

Každú premennú _jednoznačne_ reprezentuje objekt triedy `Variable`.
Čiastočné ohodnotenie premenných, s ktorým algoritmus DPLL pracuje, nie je
reprezentované samostatným objektom. Každá premenná si priamo pamätá,
či má priradenú pravdivostnú hodnotu (teda či je „nastavená“) a akú.

Premenná odkazuje na dva `Literal`y, pozitívny a negatívny, ktoré k nej
prislúchajú. Tieto literály sú potom prvkami klauzúl (`Clause`) a tie zase
prvkami `Cnf`.

Algoritmus DPLL pracuje s literálmi a klauzulami. Aby sme ľahko zistili
pravdivostnú hodnotu literálu a či je vôbec nastavená, má `Literal` metódy
`isSet()`, `isTrue()` a `isFalse()`, ktoré vrátia správnu hodnotu podľa toho,
či a ako je ohodnotená príslušná premenná a aké je znamienko literálu.
Prostredníctvom metód `Literal`u `setTrue()` a `unset()` môžeme ohodnotenie aj
meniť. K opačnému literálu sa dostaneme metódou `not()`.

```java
Literal l = someLiteral();   // ak l je "-a"
Literal nl = l.not()         // tak toto je "a"

l.setTrue();        // "-a" nastavime na true
print(l.variable().isSet()); // true, premenna "a" ma nastavenu hodnotu
print(l.variable().val());   // false (ma nastavenu hodnotu na false)
print(l.isSet())    // true, literal -a ma nastavenu hodnotu (lebo premenna...)
print(l.isTrue())   // true, literal -a je pravdivy, lebo premenna "a" je nepravdiva
print(nl.isSet())   // true
print(nl.isTrue())  // false, literal "a" je nepravdivy
```

*Poznámka*: V kóde by ste mali pracovať hlavne s literálmi
(či sú nastavené a či sú `true`), nie s premennými.

### Sledované literály

Samotné sledovanie literálov v klauzulách implementujte v triede `Clause`
v metódach `setWatch` a `findNewWatch`. Metóda `watched()` vráti dvojprvkové
pole sledovaných literálov klauzuly. Naopak `Literal` má metódu `watchedIn()`,
ktorá vráti množinu klauzúl, v ktorých je sledovaný.

Metóda `void setWatch(int index, Literal lit)` v triede `Clause` nastaví
`index`-tý prvok poľa `watched()` na literál `lit`, ktorý sa v klauzule musí
vyskytovať. Ďalej literálu `lit` pridá túto klauzulu do množiny klauzúl,
v ktorých je sledovaný. Ak navyše klauzula predtým nejaký literál s týmto
`index`-om sledovala, `setWatch` ju ostráni z jeho množiny sledujúcich klauzúl.

Metóda `boolean findNewWatch(Literal old)` nahradí doteraz sledovaný literál
`old`, ak je to potrebné, teda ak je literál `old` nastavený a nepravdivý.
V tom prípade sa pokúsi nájsť nový pravdivý alebo nenastavený literál,
ktorý ešte nie je sledovaný v tejto klauzule a pomocou metódy `setWatch` ním
nahradí literál `old` a vráti `true`. Ak sa jej to podarí, alebo literál `old`
netreba nahrádzať, metóda `findNewWatch` vráti `true`. Inak (všetky literály
sú nepravdivé alebo už sledované) vráti `false`.

### Inicializácia

Pri inicializácii algoritmu musíme zabezpečiť, aby v každej klauzule boli
sledované (podľa možnosti) dva jej literály. To je úlohou metódy
`boolean initWatched(Set<UnitClause> units)` v triede `Theory`. Pre klauzuly
s dvoma a viacerými literálmi vyberie na sledovanie ľubovoľné dva z nich.
Jednotkovým klauzulám priradí ich jediný literál ako nultý aj prvý sledovaný
a zároveň ich vloží do množiny `units` (použijeme ju potom na propagáciu
jednotkových klauzúl). Ak sa pri inicializácii nájde prázdna klauzula (vstupná
teória je evidentne nesplniteľná), metóda `initWatched` to signalizuje vrátením
`false`. Inak vráti `true`.

Množina `units` neobsahuje jednotkové klauzuly priamo, ale „obalené“ do objektu
triedy `UnitClause`, ktorý okrem klauzuly obsahuje aj referenciu na jej jediný
literál. Túto triedu použijeme aj počas behu algoritmu na obalenie klauzúl,
ktoré sú „v podstate jednotkové“ – majú viac literálov, ale iba jeden z nich je
nenastavený.

### Vplyv nastavovania literálov na sledovanie

Na zabezpečenie potrebných zmien v sledovaní literálov pri zmenách ohodnotenia,
teda pri nastavení pravdivostnej hodnoty literálu a jej „odnastavení“
implementujeme v triede `Theory` metódy `setLiteral` a `unsetLiteral`.

Metóda `boolean setLiteral(Literal l, Set<UnitClause> units)` nastaví
literál `l` na pravdivý a vo všetkých klauzulách, ktoré sledujú jeho opačný
literál `l.not()`, sa pokúsi nájsť nový literál na sledovanie. Všetky klauzuly,
o ktorých zistí, že už majú iba jeden nenastavený literál, pridá do množiny
jednotkových klauzúl `units` (obalené spolu s týmto literálom do objektu vyššie
spomenutej triedy `UnitClause`). Ak má niektorá klauzula už všetky literály
nastavené na nepravdivé, metóda `setLiteral` vráti `false`. Inak vráti `true`.

Nezabudnite, že klauzula má na hľadanie nových sledovaných literálov metódu.
Podľa jej návratovej hodnoty a sledovaných literálov ľahko rozhodnete, či
v klauzule ostal jediný nenastavený literál aj či sú všetky nepravdivé.

Metóda `void unsetLiteral()` „odnastaví“ naposledy nastavený literál.
To, samozrejme, vyžaduje, aby si `Theory` pamätala poradie, v ktorom sa
literály nastavujú.

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu08` v adresári `prakticke/pu08`.
Odovzdávajte knižnicu [`Theory.java`](pu08-java/Theory.java).
Program [`WatchedLiteralsTest.java`](pu08-java/WatchedLiteralsTest.java)
musí korektne zbehnúť s vašou knižnicou.

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre pu08](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Apu08).

Odovzdávanie riešení v iných jazykoch konzultujte s cvičiacimi.
