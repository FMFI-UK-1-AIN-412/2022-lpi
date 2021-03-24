Cvičenie 6
==========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 31.3. 23:59:59.**

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre pu06](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Apu06).

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`pu06.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu06.zip).

Tablový algoritmus
------------------

Implementujte _tablový algoritmus_, teda algoritmus na konštrukciu úplného
tabla pre konečnú množinu označených formúl. Jeho nedeterministickú verziu
sme opísali na prednáške v dôkaze
[lemy o existencii úplného tabla](https://fmfi-uk-1-ain-412.github.io/lpi/prednasky/poznamky-z-prednasok.pdf#theorem.5.25).

### Bonus

Nižšie popisovaný jednoduchý algoritmus nie je ideálny vzhľadom na dobu behu
a veľkosť výsledného tabla. Ak implementujete zaujímavé optimalizácie
(výber formúl na rozpísanie), môžete získať až 2 bonusové body.
Pozor: algoritmus, ktorý vyrobí trochu menšie tablo, ale trvá mu to oveľa
dlhšie, nie je nutne lepší.

V pull requeste presne popíšte vaše optimalizácie a ideálne tiež uveďte
porovnanie s naivným (nižšie uvedeným) algoritmom vzhľadom na dobu behu
a veľkosť tabla.

### Tablo

Tablo, ktoré vytvorí metóda `TableauBuilder::build`, bude reprezentované
pomocou tried `Tableau` a `Node` z minulého cvičenia.

„Vstupné“ formuly treba do tabla vložiť pomocou metódy `addInitial`.
Pozor: vytvorené uzly, ktoré metóda vráti, treba ešte samozrejme spracovať,
ak ich napríklad treba zavrieť (viď nižšie).

Tablo sa potom ďalej rozširuje správnym volaním metód `extendAlpha` a `extendBeta`.

### Uzavrenie vetvy

Keď pridáme uzol s formulou, ktorá uzatvára vetvu, treba ho navyše „označiť“ volaním metódy
`close`, ktorá má ako parameter referenciu na uzol, ktorého formula má
opačné znamienko ako formula nového uzla.

Jednoduchý príklad na vygenerovanie uzavretého tabla a výsledné tablo:

```java
Tableau tab = new Tableau();
List<Node> initial = tab.addInitial(new SignedFormula[] {
    SignedFormula.F(new Implication(new Variable("a"), new Variable("a")))
}));

Node last = initial.get(initial.size() - 1);        // F (a->a)
Node ta = tab.extendAlpha(last, initial.get(0), 0); // T a
Node fa = tab.extendAlpha(ta, initial.get(0), 1);   // F a

// mark fa as closed against ta
fa.close(ta);

System.stdout.write(tab.toString());
```

```
(1) F (p(a)->p(a))
==================
  (2) T p(a) (1)  
  (3) F p(a) (1)  
     * [3,2]      
```

### Algoritmus

Najjednoduchší algoritmus na vytvorenie tabla je v skratke:

  * Inicializácia:
    1. Vložiť do tabla všetky vstupné formuly.
    2. Ak je tablo už uzavreté, označiť správny uzol ako uzavretý a skončiť.
    3. Inak spustiť jeho expanziu.
  * Expanzia:
    1. Rozpísať *všetky* formuly typu &alpha;.
    2. Ak už žiadna formula typu &alpha; nie je, rozpísať *nejakú* formulu
      typu &beta;.
    3. Opakovať, kým tablo nie je uzavreté a máme nerozpísané formuly.

_Pozor:_ Formula môže byť rozpísaná v nejakej vetve, ale ešte nerozpísaná
v inej vetve.

Keďže tablo je strom, systematicky ho môžete budovať do šírky alebo do hĺbky.
Pri budovaní do šírky pracujete s frontom listov (ku každému sa hodí pamätať
si pomocné dáta, viď nižšie). Budovanie do hĺbky sa dá naprogramovať
rekurziou alebo kombináciou cyklu a rekurzie. List, v ktorom tablo
expandujete, a pomocné dáta si môžete posielať ako argumenty rekurzívnej
metódy. Pretože zásobník riadiaci rekurziu za vás obhospodaruje programovací
jazyk, budovanie do hĺbky sa programuje ľahšie a časovo aj pamäťovo je
v priemere rovnako efektívne ako budovanie do šírky.

Pri implementácii si dajte pozor na „jazykové“ problémy: iterovanie
cez zoznam, ktorý zároveň meníte (pridávate / odoberáte prvky); ujasnite si,
kedy potrebujete novú kópiu zoznamu / štruktúry a kedy chcete meniť pôvodnú
atď.

### Pomocné dátové štruktúry

Aby ste počas budovania rýchlo zistili, či môžete vetvu uzavrieť, oplatí sa
pamätať si jej uzly v pomocnej dátovej štruktúre `branch`. Môže to byť
napríklad zoznam (`List<Node>`). Algoritmus sa dá zefektívniť výberom tejto
štruktúry tak, aby sa v nej ľahko vyhľadávali uzly na uzavretie vetvy.

Podobne je dobré sledovať uzly so zatiaľ nerozpísanými formulami
v samostatných štruktúrach `alphas` a `betas`. Môžu to byť napríklad fronty
(`Dequeue<Node>`), ale na bonusové riešenie budete pravdepodobne potrebovať
iný typ.

Na správne rozšírenie týchto štruktúr po pridaní nového uzla do tabla
a kontrolu uzavretia je dobré napísať si pomocnú metódu
```java
boolean processNode(Node n, TypeOfAlphas alphas, TypeOfBetas betas, TypeOfBranch branch)
```
Táto metóda by mala zistiť, či nový uzol uzatvára vetvu, a v tom prípade ho
uzavrieť a vrátiť `true`. Ak uzol vetvu neuzatvára, metóda ho pridá do vetvy
a zaradí ho medzi alfy alebo bety podľa toho, akú formulu obsahuje.

Metódu `processNode` potom budete volať z hlavnej metódy `build()` pri
inicializácii tabla a následne aj pri jeho expanzii do hĺbky/šírky.
Rekurzívna metóda na expanziu do hĺbky bude potrebovať nasledujúce
parametre:
```java
void expand(Tableau t, Node leaf, TypeOfAlphas alphas, TypeOfBetas betas, TypeOfBranch branch)
```

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu06` v adresári `prakticke/pu06`.
Odovzdávajte (modifikujte) súbory `pu06-java/Formula.java`,
`pu06-java/Tableau.java` a `pu06-java/TableauBuilder.java`.
Program [`pu06-java/TableauTest.java`](pu06-java/TableauTest.java) musí korektne
zbehnúť s vašou knižnicou.

Súbor `pu06-java/Tableau.java` upravte pridaním svojich implementácií metód
`addInitial`, `extendAlpha`, `extendBeta` z minulého cvičenia.
**Neprepíšte** celý súbor minulotýždňovou verziou, obsahuje nové metódy
potrebné pri testovaní.

Súbor `pu06-java/Formula.java` nahraďte svojou implementáciou z minulého cvičenia.

Odovzdávanie riešení v iných jazykoch konzultujte s cvičiacimi.
