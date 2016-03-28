Cvičenie 4
==========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 17.3. 23:59:59.**

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre pu04](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Apu04).

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`pu04.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu04.zip).

NNF a CNF
---------

Do tried na reprezentáciu formúl z [cvičenia 3](../pu03/) bez rovnostných atómov
doimplementujte metódy
`toNnf()`, ktorá vráti ekvivalentnú alebo ekvisplniteľnú formulu
v negačnom normálnom tvare (NNF),
a `nnfToCnf()`, ktorá vráti ekvivalentnú alebo ekvisplniteľnú formulu
v konjunktívnom normálnom tvare
(CNF, viď [Reprezentácia CNF](#reprezentácia-cnf)).

Na konci [prednášky](https://fmfi-uk-1-ain-412.github.io/lpi/prednasky/pr04.pdf#page.47)
sme spomenuli jeden z možných prístupov na transformáciu formuly do CNF:
Formulu najprv ekvivalentne upravíme do NNF a následne z NNF ďalšími
ekvivalentnými úpravami vyrobíme CNF. Oba tieto kroky implementujeme
v každej z našich predchádzajúcich tried virtuálnymi metódami `toNnf()`
a `nnfToCnf()`.

### toNnf

Metóda `toNnf` vyrobí z formuly novú formulu, ktorá je s ňou ekvivalentná
(alebo aspoň
[ekvisplniteľná](https://en.wikipedia.org/wiki/Equisatisfiability)), ale
neobsahuje implikácie ani ekvivalencie a negované sú iba atómy. Dá sa to
dosiahnuť kombináciou rekurzívneho volania `toNnf` na podformulách
a použitím de Morganovych pravidiel (a nahradením implikácií a ekvivalencií
disjunkciami, negáciami a konjunkciami).

Predikátovému atómu stačí jednoducho vrátiť svoju kópiu.

Konjunkcia a disjunkcia zavolajú `toNnf` svojich priamych podformúl
a spoja výsledky do novej konjunkcie resp. disjunkcie.

Implikácia a ekvivalencia sa najľahšie preložia do NNF tak, že sa (najprv)
prekonvertujú do ekvivalentných formúl zložených iba z konjunkcií,
disjunkcií a negácií a tie potom nechajú skonvertovať sa do NNF.
Teda napríklad `(A → B)` sa najprv môže preložiť na `(¬A ∨ B)` a následne
zavolať `toNnf` tejto formuly. Ľahko si všimneme, že disjunkcia sa
konverziou do NNF nezmení, takže vlastne stačí `toNnf` zavolať na `¬A`
(prečo nie na `A`?) a `B` a výsledky spojiť disjunkciou,
schematicky: `((¬A).toNnf() ∨ B.toNnf())`, pseudokód:

```c++
Formula Implication::toNnf() {​​​​​
  return Disjunction([
    Negation(leftSide()).toNnf(),
    rightSide().toNnf()
  ]);
}
```

Negácia má v metóde `toNnf` najzložitejšiu úlohu, pretože ak jej podformula
nie je atomická, musia sa negácie posunúť „o úroveň nižšie“ podľa de
Morganovych zákonov. Najjednoduchšie na predstavu asi je, že sa negácia
rozhodne podľa druhu svojej priamej podformuly:

 *  Ak je priama podformula atóm, negácia iba vráti svoju kópiu.
 *  Ak je priamou podformulou tiež negácia `¬A`, podľa zákona o dvojitej
    negácii je s celou formulou ekvivalentná `A`. Tá však nemusí byť v NNF,
    takže musíme zavolať `A.toNnf()`.
 *  Ak je priamou podformulou negácie konjunkcia `(A1 ∧ … ∧ An)`, negácia ju
    môže najprv podľa de Morganovho zákona skonvertovať na `(¬A1 ∨ … ∨ ¬An)`
    a na tejto formule zavolať `toNnf`. Podobne ako pri implikácii vyššie
    ale môžeme najprv zavolať `toNnf` na negáciách konjunktov a výsledky
    spojiť disjunkciou: `((¬A1).toNnf() ∨ … ∨ (¬An).toNnf())`.
 *  Pre ďalšie druhy priamych podformúl negácie je postup podobný ako pre
    konjunkciu.

Takáto implementácia, okrem toho že nie je veľmi pekná a vyžaduje manuálne
pretypovávanie, ale popiera
[open-closed princíp](https://en.wikipedia.org/wiki/Open%E2%80%93closed_principle)
zo [SOLID princípov OOP](https://en.wikipedia.org/wiki/SOLID), pretože na pridanie
novej spojky by nestačilo pridať novú triedu, ale museli by sme modifikovať
aj implementáciu negácie.

Na čistejšie riešenie potrebujeme presunúť postup, ako sa ktorá spojka
(formula) neguje, do implementácie (triedy) pre túto spojku. To sa dá napríklad
jednou z týchto techník:
 *  `toNnf()` vo `Formula` použije pomocnú virtuálnu metódu
    `toNnf(boolean isNegated)`, ktorú zavolá s argumentom `false`.
    V podtriedach budete implementovať iba `toNnf(boolean isNegated)`.
    Napríklad v `Conjunction` zavolá `negToNnf(isNegated)` svojich
    priamych podformúl a výsledky spojí konjunkciou alebo disjunkciou podľa
    hodnoty `isNegated`.
 *  V každej podtriede triedy `Formula` implementujete pomocnú virtuálnu
    metódu `negToNnf`. Metóda `toNnf` pre negáciu zavolá `negToNnf`
    svojej priamej podformuly a napríklad `negToNnf` v `Conjunction` môže
    zavolať `negToNnf` svojich priamych podformúl a výsledky spojiť do
    disjunkcie: `(A1.negToNnf() ∨ … ∨ An.negToNnf())`.

### nnfToCnf

Formulu, ktorá je v NNF, môžeme skonvertovať do CNF vo [výslednej
reprezentácii](#reprezentácia-cnf) rekurzívnym prechodom virtuálnou metódou
`nnfToCnf`.

Predikátový atóm jednoducho vráti (`Cnf` obsahujúcu) jednu `Clause`, v ktorej
je jeden (nenegovaný) `Literal` so správnym menom a argumentmi.

Negácia môže očakávať, že jej priama podformula je atóm, a vráti (Cnf
obsahujúcu) jednu `Clause`, v ktorej je jeden negovaný `Literal` so správnym
menom a argumentmi. Môže to urobiť priamo alebo zavolať `nnfToCnf` svojho
atómu a upraviť jeho `Cnf`. Ak priamou podformulou nie je atóm, negácia by
mala hodiť výnimku.

Konjunkcia zavolá `nnfToCnf` na jednotlivé konjunkty, dostane niekoľko `Cnf`
(zoznamov klauzúl) a vyrobí z nich jednu tak, že jednoducho dá dokopy všetky
klauzuly do jedného zoznamu.

Disjunkcia je zložitejšia, pretože čiastočné CNF pre jednotlivé disjunkty
musíme podľa distributívneho zákona medzi sebou „roznásobiť“, pričom
roznásobujeme ľubovoľne veľa disjunktov, ktoré môžu obsahovať ľubovoľne veľa
klauzúl.

Príklad pre 2 disjunkty:

```
a.toCnf() = (a11 ∨ a12 ∨ a13) ∧ (a21 ∨ a22) ∧ (a31)
b.toCnf() = (b11 ∨ b12) ∧ (b21)

(a∨b).nnfToCnf() =
=  ((a11 ∨ a12 ∨ a13) ∧ (a21 ∨ a22) ∧ (a31)) ∨ ((b11 ∨ b12) ∧ (b21))
    --------a1-------   -----a2----   --a3-     ----b1-----   --b2-

=  (a1 ∧ a2 ∧ a3) ∨ (b1 ∧ b2)
=  (a1 ∨ b1) ∧ (a2 ∨ b1) ∧ (a3 ∨ b1) ∧ (a1 ∨ b2) ∧ (a2 ∨ b2) ∧ (a3 ∨ b2)

    ------a1-------   ---b1----     ---a2----   ---b1----
=  (a11 ∨ a12 ∨ a13 ∨ b11 ∨ b12) ∧ (a21 ∨ a22 ∨ b11 ∨ b12) ∧ ...
```

Implikácia a ekvivalencia sa v NNF nemôžu vyskytovať a ich metóda `nnfToCnf`
by mala hodiť výnimku (resp. mala by to urobiť `nnfToCnf` vo `Formula`
a implikácia a ekvivalencia by túto metódu ani nemali implementovať).

## Reprezentácia CNF

Triedy, ktoré sme vyrobili v [3. praktickej úlohe](../pu03/), nie sú z viacerých dôvodov
veľmi vhodné na reprezentáciu formúl v CNF:
- kedykoľvek by sme očakávali formulu v CNF tvare,  museli by sme vždy
  kontrolovať, či naozaj má správny tvar;
- je trošku neefektívna ( `Negation(PredicateAtom("x", ...))`) a ťažkopádnejšia
  na použite (musíme zisťovať akého typu je podformula v `Disjunction` atď.);
- chceme pridať niekoľko metód, ktoré majú zmysel len pre CNF formulu.

Najpriamočiarejší spôsob, ako sa týmto problémom vyhnúť, je reprezentovať CNF
formulu jednoducho ako pole (resp. zoznam) klauzúl, pričom každá klauzula je pole
literálov. Literál by mohol byť reprezentovaný ako dvojica: meno
a boolovský flag hovoriaci, či je negovaný.
Operácie s takto reprezentovanými CNF formulami by ale potom nemohli byť
implementované ako ich metódy.

Obidve výhody dosiahneme tak,
že vytvoríme triedy `Cnf` a `Clause`, ktoré oddedíme od poľa (resp. zoznamu)
a pridáme im navyše potrebné metódy.
Na reprezentáciu literálov vytvoríme triedu `Literal`.
Ďalšou výhodou takéhoto prístupu je aj to, že vieme písať kód,
ktorý sa oveľa ľahšie číta:
namiesto hromady hranatých zátvoriek vidíme, či vytvárame klauzulu
alebo celú CNF formulu.

V súbore [`pu04-java/Cnf.java`](pu04-java/Cnf.java) nájdete hotové definície tried `Literal`,
`Clause` a `Cnf`, ktoré máte použiť na reprezentáciu literálov, klauzúl a CNF
formúl. Vaše metódy `toCnf` teda majú vždy vracať inštanciu triedy `Cnf`.

Trieda `Literal` má konštruktor ktorý akceptuje `AtomicFormula` a `boolean`
flag, či je negovaný, ale odporúčame ich vyrábať cez pomocné konštruktory
`Literal.Lit(nejakyAtom)` a `Literal.Not(nejakyAtom)`.

Triedy `Cnf` a `Clause` majú konštruktory, ktoré akceptujú ľubovoľnú
`Collection` adekvátnych objektov, alebo aj len jednoducho klauzuly resp.
literály ako samostatné argumenty. Keďže sú oddedené od `ArrayList`-u, môžete
tiež používať metódy na modifikáciu zoznamov ako `add`, či `addAll`.

```java
// Cnf obsahujuca jednu klauzulu, ktora obsahuje jeden nenegovany literal
Cnf jedenLiteral = new Cnf(new Clause(Literal.Lit(nejakyAtom)));

Clause c = new Clause();
c.add(Literal.Not(inyAtom));
for (Clause cl : jedenLiteral) {
    c.addAll(cl);
}
// c je teraz (-inyAtom, nejakyAtom)
```

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu04` v adresári `prakticke/pu04`.  Odovzdávajte
(modifikujte) súbor (knižnicu) [`pu04-java/Formula.java`](pu04-java/Formula.java). Program
[`pu04-java/CnfTest.java`](pu04-java/CnfTest.java) musí korektne zbehnúť s vašou knižnicou.

Odovzdávanie riešení v iných jazykoch konzultujte s cvičiacimi.

## Bonus

Uvedená „jednoduchá“ metóda je veľmi neefektívna vzhľadom na veľkosť /
komplikovanosť výsledných formúl. Napríklad pre niektoré formuly
stupňa menšieho ako 10 vyrobíme CNF obsahujúce desaťtisíce až
stotisíce výskytov literálov.

Ak implementujete nejakú efektívnejšiu (vzhľadom na veľkosť výslednej
formuly, ale pozor aj na čas behu) metódu na konverziu do CNF, uveďte
to v pull requeste s krátkym popisom vášho algoritmu (odkaz na nejaký
[internetový zdroj](https://en.wikipedia.org/wiki/Tseytin_transformation)
je OK, implementácia ale musí byť vaša vlastná)
a môžete získať **až 2 bonusové body**.
