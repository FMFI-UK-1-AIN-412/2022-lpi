Cvičenie 10
===========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 5.5. 23:59:59.**

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre pu10](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Apu10).

Všetky ukážkové a testovacie súbory k tomuto cvičeniu si môžete stiahnuť
ako jeden zip súbor
[pu10.zip](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu10.zip).

## FolFormula

Vytvorte objektovú hierarchiu na reprezentáciu prvorádových formúl.
Zadefinujte základné triedy `Term` a `Formula` a od nich odvodené triedy pre
jednotlivé typy termov a formúl.

Všetky triedy naprogramujte ako knižnicu podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia).

*Poznámka: korektná implementácia `substitute` je za 0.5b. Ak implementujete
korektne všetko okrem `substitute`, dostanete 1.5b.*

```
Term
 │  constructor(...)
 │  name() -> String              // vrati nazov termu (i.e. premennej, konst., funkcie)
 │  toString() -> String          // vrati retazcovu reprezentaciu termu
 │  equals(Term other) -> Bool    // vrati true, ak je tento term rovnaky ako other
 │  variables() -> Set of String  // vrati mnozinu mien premennych
 │  constants() -> Set of String  // vrati mnozinu mien konstant
 │  functions() -> Set of String  // vrati mnozinu mien funkcii
 │  eval(Structure m, Valuation e) -> m.domain  // vrati hodnotu termu v m pri e
 │  substitute(String var, Term t) -> Term      // substituuje term t za vsetky vyskyty
 │                                              // premennej var v tomto terme
 │
 ├─ Variable
 │      constructor(String name)
 │
 ├─ Constant
 │      constructor(String name)
 │
 └─ FunctionApplication
        constructor(String name, Array of Term subts))
        subts() -> Array of Term      // vrati vsetky "priame" podtermy

Formula
 │  constructor()
 │  subfs() -> Array of Formula   // vrati vsetky priame podformuly ako pole
 │  toString() -> String          // vrati retazcovu reprezentaciu formuly
 │  equals(Formula other) -> Bool // vrati true, ak je tato formula rovnaka ako other
 │  variables() -> Set of String  // vrati mnozinu mien premennych
 │  constants() -> Set of String  // vrati mnozinu mien konstant
 │  functions() -> Set of String  // vrati mnozinu mien funkcii
 │  predicates() -> Set of String // vrati mnozinu mien predikatov
 │  isSatisfied(Structure m, Valuation e) -> Bool  // vrati true, ak je formula
 │                                                 // splnena v m pri e
 │  freeVariables() -> Set of String   // vrati mnozinu vsetkych volnych premennych
 │                                     // v tejto formule
 │  substitute(String var, Term t) -> Formula // substituuje term t za vsetky volne vyskyty
 │                                            // premennej var; vyhodi vynimku, ak substitucia
 │                                            // nie je aplikovatelna
 │
 ├─ AtomicFormula
 │   │  subts() -> Array of Term  // vrati termy, ktore su argumentmi predikatu/rovnosti
 │   │
 │   ├─ PredicateAtom
 │   │      constructor(String name, Array of Term subts)
 │   │      name() -> String          // vrati meno predikatu
 │   │
 │   └─ EqualityAtom
 │          constructor(Term leftTerm, Term rightTerm)
 │          leftTerm() -> Term
 │          rightTerm() -> Term
 │
 ├─ Negation
 │      constructor(Formula originalFormula)
 │      originalFormula() -> Formula // vrati povodnu formulu
 │                                   // (jedinu priamu podformulu)
 │
 ├─ Disjunction
 │      constructor(Array of Formula disjuncts)
 │
 ├─ Conjunction
 │      constructor(Array of Formula conjuncts)
 │
 ├─ BinaryFormula
 │   │  constructor(Formula leftSide, Formula rightSide)
 │   │  Formula leftSide()    // vrati lavu priamu podformulu
 │   │  Formula rightSide()   // vrati pravu priamu podformulu
 │   │
 │   ├─ Implication
 │   │
 │   └─ Equivalence
 │
 └─ QuantifiedFormula
     │  constructor(String qvar, Formula originalFormula)
     │  originalFormula() -> Formula  // vrati povodnu formulu
     │  qvar() -> String              // vrati meno kvantifikovanej premennej
     │
     ├─ ForAll
     │
     └─ Exists
```

Samozrejme použite syntax a základné typy programovacieho jazyka, v ktorom
úlohu riešite (viď príklady použitia knižnice na konci).

Metóda `toString` vráti reťazcovú reprezentáciu termu / formuly podľa
nasledovných pravidiel:
- `Variable`: reťazec `x`, kde `x` je meno premennej (môže byť
  viacpísmenkové).
- `Constant`: reťazec `c`, kde `c` je meno konštanty (môže byť
  viacpísmenkové).
- `FunctionApplication`:  reťazec `funcName(T1,T2,T3...)`, kde `funcName` je meno funkcie.
  a `T1`, `T2`, `T3`, ... sú  reprezentácie argumentov (termov) funkcie
  (funkcia s nula argumentami ma reprezentáciu `funcName()`).
- `PredicateAtom`:  reťazec `predName(T1,T2,T3...)`, kde `predName` je meno predikátu
  a `T1`, `T2`, `T3`, ... sú  reprezentácie argumentov (termov) predikátu
  (predikát s nula argumentami ma reprezentáciu `predName()`).
- `EqualityAtom`: reťazec `T1=T2`, kde `T1` a `T2` sú reprezentácie argumentov
  (termov) rovnostného atómu.
- `ForAll`: reťazec `∀x F` kde `x` je meno kvantifikovanej premennej
  a `F` je reprezentácia podformuly.
- `Exists`: reťazec `∃x F` kde `x` je meno kvantifikovanej premennej
  a `F` je reprezentácia podformuly.

Ostatné typy formúl majú rovnakú reprezentáciu ako v [cvičení 3](../pu03/).

Metóda `eval` vráti hodnotu termu v danej štruktúre pri danom ohodnotení premenných.
Ak sa stane, že ohodnotenie neobsahuje nejakú premennú, ktorá sa vyskytne
v terme alebo štruktúra neobsahuje interpretáciu niektorého symbolu, tak môžete
buď vygenerovať chybu / výnimku alebo vrátiť ľubovoľnú hodnotu.

Metóda `isSatisfied` vráti `true` alebo `false` podľa toho, či je formula splnená
v danej štruktúre pri danom ohodnotení premenných. Ak sa stane, že ohodnotenie
neobsahuje nejakú premennú, ktorá sa vyskytne vo formule alebo štruktúra
neobsahuje interpretáciu niektorého symbolu, tak môžete buď vygenerovať chybu /
výnimku alebo ju považovať za `false`.

Metóda `freeVariables` vráti množinu mien všetkých voľných premenných vo formule.
Voľné premenné sú také, ktoré nie sú viazané žiadnym kvantifikátorom, t.j. vo formule

```
(P(x,x) -> ∀x∃y(Q(x,z)-> P(z,y)))
```
sú voľné premenné `z` a `x` (ale iba jej prvé dva výskyty mimo `∀x`).

Metóda `substitute` vráti **kópiu** formuly, v ktorej je každý voľný výskyt danej
premennej nahradený **kópiou** daného termu. Napríklad volanie
```java
substitute("x",
  new FunctionApplication("f", Arrays.asList(new Constant("c"), new Variable("x"))));
```
na predchádzajúcej formule vráti úplne novú formulu
```
(P(f(c,x),f(c,x)) -> ∀x∃y(Q(x,z)-> P(z,y))).
```

Všimnite si, že ani tretí výskyt `x` (viazaný kvantifikátorom `∀x`), ani „nové“
výskyty `x` už neboli ďalej substituované.

Ak substitúcia nie je aplikovateľná, metóda `substitute` vyhodí výnimku.

## Štruktúra a ohodnotenie premenných

Štruktúra je objekt obsahujúci nasledovné atribúty:

- `domain` - množina prvkov domény. Použite vhodnú štruktúru jazyka, ktorý používate.
  Mala by umožňovať iterovať cez všetky prvky a testovať príslušnosť.
- `iC` - interpretácia indivíduových konštánt. Mapa z mien konštánt na prvky z `domain`.
- `iF` - interpretácia funkčných symbolov. Mapa z mien funkcií na zobrazenia (mapy), ktoré
  zobrazujú zoznam/`n`-ticu so správny počtom argumentov na hodnotu z `domain`.
- `iP` - interpretácia predikátových symbolov. Mapa z mien predikátov na množinu
  zoznamov/`n`-tíc prvkov z `domain` (kde `n` je arita príslušného symbolu).

*Poznámka: Štruktúra štruktúry (pun intended) je navrhnutá tak, aby čo najviac
zodpovedala teórii z prednášky (teda skoro, mala by to byť jedna mapa spolu).
V praxi môže byť praktickejšie (again) použiť iné reprezentácie. Interpretácie
funkčných a predikátových symbolov môžu tiež byť reprezentované ako funkcie
použitého programovacieho jazyka (s návratovou hodnotou z domény resp. bool),
čo môže byť pamäťovo efektívnejšie.
Alternatívne môžu byť interpretácie funkčných symbolov reprezentované ako
množiny `n+1`-tíc (`(arg1, arg2, ..., vysledok)`). Všimnite si, že ak by sme
navyše brali interpretácie konštánt ako jednoprvkové množiny 1-tíc z `domain`,
tak v tomto prípade budú všetky interpretácie reprezentované ako množiny `n`-tíc
z `domain`.*

Ohodnotenie premenných (angl. <i>valuation</i>) je mapa z reťazcov (mien premenných)
na hodnoty z `domain` (pre danú štruktúru).

Na prednáškach ste používali označenie `e(x/v)` pre <em>nové</em> ohodnotenie, ktoré je
rovnaké ako `e` ale premennej `x` priraďuje hodnotu `v`. Je dobré si spraviť
podobnú funkciu (napr. s názvom `set`).

### Java

Pre reprezentáciu štruktúr je definovaný interface
[`Structure<D>`](pu10-java/Structure.java), kde `D` je typ hodnôt z domény. Dá
sa používať nasledovne:

```java
Structure<Integer> m = ...; // struktura m, hodoty m.domain() su cisla
Map<String,Integer> e = ...; // ohodnotenie premennych

// hodnota termu f(g(c), x) v strukture m pri ohodnoteni e
int h = m.iF("f") // interpretacia funkcneho symbolu f: zobrazenie z List<Integer> na Integer
    .get(Arrays.asList( // vyrobime zoznam "argumentov" pre funkciu
      m.iF("g").get(Arrays.asList(    // to iste pre funkcny symbol g
        m.iC("c") // interpretacia konstanty c
      )),
      e.get("x")      // hodnota premennej x v ohodnoteni e
    ))
;

// ci je P(f(g(c),x), z) splnena v strukture m pri ohodnoteni e
boolean isSatisfied = m.iP("P").contains(Arrays.asList(h, e.get("z")));
```

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu10` v adresári `prakticke/pu10`. Modifikujte
a odovzdávajte knižnicu [Formula.java](pu10-java/Formula.java). Program
[FolFormulaTest.java](pu10-java/FolFormulaTest.java) musí korektne zbehnúť s vašou
knižnicou.
