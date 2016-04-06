Bonus 1
=======

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 28.4. 23:59:59.**

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`bonus01.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/bonus01.zip).

## Kto zabil Agátu (2b)

Someone in Dreadsbury Mansion killed Aunt Agatha. Agatha, the butler, and
Charles live in Dreadsbury Mansion, and are the only ones to live there.
A killer always hates, and is no richer than his victim. Charles hates no one that
Agatha hates. Agatha hates everybody except the butler. The butler hates
everyone not richer than Aunt Agatha. The butler hates everyone whom Agatha
hates. No one hates everyone. Who killed Agatha?

Niekto v Dreadsburskom panstve zabil Agátu. Agáta, komorník a Karol bývajú
v Dreadsburskom panstve a nikto iný okrem nich tam nebýva. Vrah vždy nenávidí
svoje obete a nie je od nich bohatší.
Karol neprechováva nenávisť k nikomu, koho nenávidí Agáta.
Agáta nenávidí každého okrem komorníka.
Komorník nenávidí každého, kto nie je bohatší ako Agáta. Komorník nenávidí
každého, koho nenávidí Agáta. Niet toho, kto by nenávidel všetkých. Kto zabil
Agátu?

Zistite a **dokážte** kto zabil Agátu.

### Formalizácia

Najprv treba jednotlivé tvrdenia zo zadania prepísať do logiky. Napríklad:

    The butler hates everyone whom Agatha hates.

    ∀X ( hates(Agatha,X) → hates(butler,X) )

Toto samozrejme nie je výroková logika, takže budeme potrebovať program, ktorý
vyrobí inštancie pravidiel pre správne doplnené konštanty za všeobecne
kvantifikované premenné.


### Hľadanie riešenia a dôkaz

Takto sformulovaný problém prerobíme na vstup pre SAT solver. Keď ho
len tak pustíme, nájde nám jedno z možných riešení, v ktorom sa môžeme pozrieť,
kto by mohol byť vrahom. Potrebujeme však ukázať, že to nie je pravda len náhodou
v tom jednom *modeli* (ohodnotení spĺňajúcom všetky formuly), ale že to naozaj
*vyplýva* z našej *teórie* (množiny formúl), teda je to pravdivé vo
všetkých jej modeloch. Takže musíme pridať negáciu toho tvrdenia k teórii.
Ak bude toto rozšírenie nesplniteľné, tak naše tvrdenie (kto bol vrahom)
vyplýva z teórie, teda je pravdivé vo všetkých modeloch našej teórie.

**Pozor**: Keďže naším cieľom je dokázať čosi o konkrétnej úlohe (a nie len
všeobecné vyplývanie nejakých formúl), je dôležité najskôr (pred pridaním
negácie toho, čo chceme dokázať) naozaj overiť, že naša teória je *splniteľná*
(má aspoň nejaký model). Keby sme niečo pokazili a vyrobili nekonzistentnú
(nesplniteľnú) teóriu, tak by z nej samozrejme vyplývalo všetko, vrátane nášho
cieľa (ale napríklad aj jeho negácie).

Najlepší spôsob je, keď celú teóriu zapíšete do nejakej funkcie, aby ste ju
(vstup pre SAT solver) mohli ľahko zapísať dvakrát:

1. najskôr samotnú, pustiť SAT solver a overiť, že má nejaké riešenie;
2. prejsť všetkých možných vrahov v tomto riešení (t.j. také `X`, pre ktoré je
   `killed(X,Agatha)` pravdivé);
3. pre každého možného vraha znovu zapísať teóriu ale aj spolu s negáciou
   dokazovaného tvrdenia a znovu pustiť SAT solver.

### Kódovanie

Podobne ako v predchádzajúcich úlohách potrebujeme zakódovať atómy na čísla.
Máme 3 druhy atómov, zodpovedajúce binárnym predikátom `killed(X,Y)`,
`hates(X,Y)` a `richer(X,Y)`.

Keďže máme 3 binárne predikáty, ktoré majú ako parametre jedného z troch ľudí,
môžeme atómy zakódovať ako čísla v trojkovej sústave
<var>R</var><var>X</var><var>Y</var><sub>3</sub> + 1,
kde <var>R</var> je číslo predikátu
a <var>X</var> a <var>Y</var> sú čísla osôb od 0 do 2
(teda v desiatkovej sústave to bude `R*3*3 + X*3 + Y + 1`).

Ak predikát `killed` a Agáta budú mať číslo 0 a keď vymeníte poradie parametrov
v predikáte `killed` (aby vrah bola najnižšia cifra), tak potom prvé
3 atómy (1, 2, 3) budú zodpovedať možnostiam toho, kto zabil Agátu.
(Dobré je vymeniť poradie parametrov len vo
výpočte čísla atómu, nie poradie argumentov vo funkcii ;)

Veľmi pomôže spraviť si pár pomocných funkcií podobne ako
v predchádzajúcich úlohách:

```python
P = 3 # pocet ludi
Agatha = 0
Butler = 1
Charles = 2
Persons = [Agatha, Butler, Charles]
P = len(Persons)

def killed(p1, p2):
    # p1 a p2 su vymenene,
    # aby killed(X,Agatha) zodpovedalo 1, 2, 3
    return 0 * P * P + p2 * P + p1 + 1

def hates(p1, p2):
    return 1 * P * P + p1 * P + p2 + 1

def richer(p1, p2):
    return 2 * P * P + p1 * P + p2 + 1
```

Formuly môžeme potom vygenerovať takto (`writeImpl`
je metóda pomocnej triedy `DimacsWriter`, ktorá rovno správne zapíše
implikáciu):
```python
# The butler hates everyone whom Agatha hates.
# ∀X ( hates(Agatha,X) → hates(butler,X) )
for p in Persons:
    w.writeImpl( hates(Agatha, p), hates(Butler, p) )
```

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `bonus01` v adresári `prakticke/bonus01/python`.
Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre bonus01](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Abonus01).

Odovzdávajte súbor
[`MurderMystery.py`](bonus01-python/MurderMystery.py),
ktorý korektne zbehne s priloženým testom
[`murderMysteryTest.py`](bonus01-python/murderMysteryTest.py).

Implementujte funkcie `killed`, `hates`, `richer` a metódy `writeTheory`
a `prove`.

Vaše riešenie by malo v metóde `prove` najskôr zakódovať iba teóriu a pustiť na
ňu SAT solver, aby ste sa ubezpečili, že vaša teória má model (riešenie)
a našli možných vrahov.
Potom by malo pre každého možného vraha znovu zakódovať teóriu spolu s tvrdením,
ktoré chcete dokázať, a znovu zavolať SAT solver (a skontrolovať, či dá
očakávaný výstup). Metóda solve má vrátiť zoznam všetkých osôb o ktorých
sa podarilo dokázať, že sú vrahom.

**Testovač kontroluje iba či máte dobre implementovanú metódu `writeTheory`,
ale nie, či máte správne dokazovanie v metóde `prove`.**

Do komentárov v programe napíšte tvrdenia / formuly, ktoré ste
sformalizovali.
