Cvičenie 11
===========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 12.5. 23:59:59.**

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre pu11](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Apu11).

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`pu11.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu11.zip).

Plánovanie
----------

Vyriešte úlohu o gazdovi, koze, kapuste a vlku
([Wikipedia](https://en.wikipedia.org/w/index.php?title=Wolf,_goat_and_cabbage_puzzle),
[XKCD #1134](https://xkcd.com/1134/))
**pomocu SAT solvera**.
Implementujte metódu `vyries` triedy `VlkKozaKapusta`, ktorá dostane
ako argumenty počiatočný stav a počet krokov, koľko má mať nájdené riešenie – _plán_.

Okrem vyriešenia zaujímavej hádanky na tomto príklade uvidíme, ako sa
pomocou logiky kóduje a rieši celá trieda ťažkých praktických problémov
_plánovania_, teda hľadania postupnosti akcií, ktorá nás dovedie
z daného počiatočného stavu do želaného cieľového stavu.

Počiatočný stav je reprezentovaný ako slovník tvaru

```python
{ "vlk": "vlavo", "koza": "vlavo", "kapusta": "vpravo", "gazda": "vlavo" }
```
Môžete predpokladať, že počiatočný stav je korektný vzhľadom na podmienky úlohy.

Vaša metóda vráti zoznam akcií (reťazcov), ktoré hovoria, koho má gazda previezť.
`"vlk"` znamená že prevezie vlka atď., `"gazda"` znamená, že sa prevezie sám.
Ak sa úloha nedá vyriešiť na daný počet krokov, metóda vráti prázdny zoznam.

### Reprezentácia

Potrebujeme nejak reprezentovať stavy v jednotlivých krokoch, možné akcie a to, v ktorom
kroku sa ktorá akcia vykonala.

**Stavy** by sme mohli reprezentovať 4 stavovými atómami (`vlavo(vlk)`,
`vlavo(koza)`, `vlavo(kapusta)` a `vlavo(gazda)`). Keďže sa ale stavy menia
z kroku na krok, budú ešte parametrizované číslom kroku:

- `vlavo(X,K)` bude pravdivý, ak `X` je na ľavom brehu v kroku `K`.

Podobne budeme reprezentovať **akcie**: máme 8 akcií (
`prevez(dolava,vlk)`, `prevez(dolava,koza)`, `prevez(dolava,kapusta)`, `prevez(dolava,gazda)`
`prevez(doprava,vlk)`, `prevez(doprava,koza)`, `prevez(doprava,kapusta)`, `prevez(doprava,gazda)`
), ale keďže potrebujeme reprezentovať, v ktorom kroku
ktorú z nich vykonáme, atómy budú znovu mať číslo kroku ako parameter:

- `prevez(S,X,K)` bude pravdivá, ak v `K`-tom kroku prevezie gazda objekt `X` v smere `S`
  (`prevez(S,gazda,K)` znamená, že sa preváža sám).

Poznámka: stačili by nám aj 4 atómy tvaru `prevez(X,K)`, akurát by sa trochu
komplikovanejšie písali formuly pre ich podmienky a efekty.

Všimnite si, že pre plán dlhý `N` krokov máme `N` akcií (`0` až `N-1`)
a `N+1` stavov (`0` až `N`):
```
stav_0 --akcia_0->  stav_1 --akcia_1-> ... stav_N-1 --akcia_N-1-> stav_N
```

Vo vstupe pre SAT solver sú premenné reprezentované číslami, takže pomôžu
správne pomocné funkcie a konštanty, ktoré uľahčia preklad z mien stavových
atómov a akcií do čísel a naopak.
Ak hľadáme plán dĺžky `N`,
potrebujeme `4*(N+1)` výrokových premenných pre stavové atómy
tvaru `vlavo(X,K)` pre 0 ≤ `K` ≤ `N`
a `2*4*N` premenných pre akcie
tvaru `prevez(S,X,K)` pre 0 ≤ `K` < `N`.

Zadanie pre SAT solver bude zložené z formúl popisujúcich:
- počiatočný stav,
- koncový stav,
- vykonanie práve jednej akcie v každom kroku,
- podmienky a efekty akcií,
- zotrvačnosť stavových atómov (frame problem) nezmenených akciou,
- obmedzenia úlohy.

### Počiatočný a koncový stav

**Počiatočný stav** popíšeme ako konjunkciu hodnôt stavových atómov v 0-tom kroku.
Napríklad počiatočný stav z vyššie uvedeného pythonovského slovníka
popisuje formula:
```
vlavo(vlk, 0) ∧ vlavo(koza, 0) ∧ ¬vlavo(kapusta, 0) ∧ vlavo(gazda, 0)
```
Podobne popíšeme želaný **koncový stav** (všetci sú na pravom brehu)
ako konjunkciu hodnôt stavových atómov v `N`-tom kroku:
```
¬vlavo(vlk, N) ∧ ¬vlavo(koza, N) ∧ ¬vlavo(kapusta, N) ∧ ¬vlavo(gazda, N)
```

### Vykonanie práve jednej akcie

Ak by sme mali predikát `objekt`, ktorý bude pravdivý pre prevážané objekty
(`∀X(objekt(X) ↔︎ (X = vlk ∨ X = koza ∨ X = kapusta ∨ X = gazda))`),
predikát `krok`, pravdivý pre možné poradové čísla krokov plánu (`0`…`N-1`),
a predikát `smer` pre smery prevozu (`∀S(smer(S) ↔︎ (S = dolava ∨ S = doprava))`),
vykonanie **práve jednej akcie** v každom kroku by sme zabezpečili dvoma
formulami:
```
∀K ( krok(K) →
  ∃S ( smer(S) ∧
    ∃X( objekt(X) ∧
      prevez(S,X,K) ) ) )

∀K ( krok(K) →
  ∀S1 ( smer(S1) →
    ∀S2 ( smer(S2) →
      ∀X1 ( objekt(X1) →
        ∀X2 ( objekt(X2) →
              ( (¬S1 = S2 ∨ ¬X1 = X2) →
                  ¬( prevez(S1,X1,K) ∧ prevez(S2,X2,K) ) ) ) ) ) ) )
```
Prvá hovorí, že sa v každom kroku vykoná aspoň jedna akcia – gazda prevezie
v aspoň jednom smere aspoň jeden objekt.
Druhá formula hovorí, že sa vykoná najviac jedna akcia.
Presnejšie hovorí, že sa v žiadnom kroku nestane,
že by gazda previezol niečo v rôznych smeroch,
ani že by gazda previezol rôzne objekty bez ohľadu na smer
(tieto kombinácie vyjadruje podmienka `(¬S1 = S2 ∨ ¬X1 = X2)`).

Do výrokovej logiky musíme tieto podmienky prepísať tak, že **v cykloch
cez možné hodnoty premenných** podľa podmienok „rozvinieme“ všeobecné
kvantifikátory na konjunkcie a existenčné na disjunkcie. Tomuto procesu
sa hovorí <i lang="en">grounding</i>.

Dostaneme tak tieto výrokové formuly pre každý krok `K` ∈ {0, …, `N-1`}:
- aspoň jedna akcia v `K`-tom kroku:

    ```
    prevez(dolava,vlk,K) ∨ ... ∨ prevez(dolava,gazda,K)
      ∨ prevez(doprava,vlk,K) ∨ ... ∨ prevez(doprava,gazda,K)
    ```

- najviac jedna akcia v `K`-tom kroku:
  klauzuly
    ```
    ¬prevez(S1,X1,K) ∨ ¬prevez(S2,X2,K)
    ```
  pre všetky páry
  navzájom rôznych dvojíc smerov `S1`, `S2` ∈ {`dolava`, `doprava`}
  a prevážaných objektov `X1`, `X2` ∈ {`vlk`, …, `gazda`}.

Všimnite si, že predikáty `krok`, `smer`, `objekt` vo výsledných klauzulách
nevystupujú a netreba ich teda kódovať. Hovoria nám iba to, cez aké hodnoty
prebiehame v cykloch, ktorými klauzuly generujeme.

### Podmienky a efekty akcií

**Podmienky**, za ktorých možno akciu vykonať,
sú jej **nutnými** podmienkami –
ak sa akcia vykoná, museli pred jej vykonaním platiť.
Vyjadríme to implikáciami `akcia(K) → (podmienka_1(K) ∧ ... ∧ podmienka_p(K))`.
(Čo by znamenala opačná implikácia, teda ak by sme podmienky zapísali
ako postačujúce?)
Napríklad podmienkou prevezenia kapusty doprava v `K`-tom kroku je,
že sa kapusta v `K`-tom kroku nachádza vľavo a nachádza sa tam aj gazda
(kapusta sa sama neprevezie).
Zapíšeme ju teda
```
prevez(doprava,kapusta,K) → (vlavo(kapusta,K) ∧ vlavo(gazda,K))
```
Podmienky akcií musíme zapísať pre každý krok `K` od 0 po `N-1`.

V našom prípade sú všetky podmienky akcií
jednoduchými variáciami uvedeného príkladu
a prvorádovo by sme ich zapísali:
```
∀K ( krok(K) →
  ∀X ( objekt(X) →
       ( (prevez(doprava,X,K) → (vlavo(X,K) ∧ vlavo(gazda,K))) ∧
         (prevez(dolava,X,K) → (¬vlavo(X,K) ∧ ¬vlavo(gazda,K))) ) ) )
```
Výrokovú verziu vygenerujeme cyklami podobne ako v prípade vykonania práve
jednej akcie.

Akcia je **postačujúcou** podmienkou svojich **efektov**,
teda zmien stavových atómov
(nový stav určite nastane po vykonaní akcie,
ale môže nastať aj z iných príčin).
Zapíšeme ich teda implikáciami
`akcia(K) → (efekt_1(K+1) ∧ ... ∧ efekt_p(K+1))` –
všimnite si, že efekt sa prejaví v `K+1`-om kroku.
Napríklad efektom prevezenia kapusty doprava v `K`-tom kroku je,
že kapusta a gazda sa v `K+1`-om kroku nachádzajú vpravo:
```
prevez(doprava,kapusta,K) → (¬vlavo(kapusta,K+1) ∧ ¬vlavo(gazda,K+1))
```
Efekty akcií (rovnako ako ich podmienky) musíme zapísať pre
každý krok `K` od 0 po `N-1`.
V našej úlohe sa dajú generovať podobne jednoducho ako podmienky akcií.

### Zotrvačnosť stavových atómov

Pri popise efektov akcie je pohodlné zapisovať
iba zmeny stavových atómov.
SAT solveru však treba vysvetliť aj **zotrvačnosť stavových atómov**
([<i lang="en">frame problem</i>](https://en.wikipedia.org/wiki/Frame_problem)),
teda to, že stavové atómy neovplyvnené akciou
sa medzi `K`-tym a `K+1`-ým krokom nezmenia.
Môžeme to spraviť napríklad tak, že pre každú akciu a každý
krok priamo popíšeme, že hodnoty akciou neovplyvnených atómov
sa prenášajú do ďalšieho kroku.

Zvyčajne je ale stručnejšie zapísať,
že **nutnou** podmienkou **zmeny** stavového atómu medzi
`K`-tym a `K+1`-ým krokom je vykonanie niektorej z akcií,
ktorá má túto zmenu ako efekt (tieto formuly sa nazývajú
<dfn lang="en">explanatory axioms</dfn>, lebo vysvetľujú, ktoré akcie zapríčinili efekt).
Napríklad kapusta sa premiestni zľava doprava
iba vykonaním akcie jej prevozu doprava:
```
(vlavo(kapusta,K) ∧ ¬vlavo(kapusta,K+1)) → prevez(doprava,kapusta,K)
```
Nezabudnite, že treba opísať aj opačné prevozy:
```
(¬vlavo(kapusta,K) ∧ vlavo(kapusta,K+1)) → prevez(dolava,kapusta,K)
```
Ak by niektorú zmenu spôsobovalo viacero akcií
(napríklad gazda sa presunie pri každej akcii),
spojíme ich v konzekvente (na pravej strane) implikácie
disjunkciou (niektorá z nich sa musí vykonať, ale nie všetky).

### Obmedzenia úlohy

**Obmedzenia úlohy** (čo nemôže ostať bez gazdu na tom istom brehu)
sa dajú buď zahrnúť do predpokladov akcií,
ale tiež ľahko napísať pre každý stav `K`.
Napríklad formula

```
∀K ( krok(K) →
  ¬(vlavo(vlk,K) ∧ vlavo(koza,K) ∧ ¬vlavo(gazda,K) ) )
```

hovorí, že sa nesmie stať, aby vlk a koza boli v `K`-tom kroku
vľavo bez gazdu.

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu11` v správnom adresári.


### Python
Odovzdávajte (modifikujte) súbor [`VlkKozaKapusta.py`](pu11-python/VlkKozaKapusta.py).
Program [`vlkKozaKapustaTest.py`](pu11-python/vlkKozaKapustaTest.py)
musí s vašou knižnicou korektne zbehnúť.

### Java
Odovzdávajte (modifikujte) súbor [`VlkKozaKapusta.java`](pu11-java/VlkKozaKapusta.java).
Program [`VlkKozaKapustaTest.java`](pu11-java/VlkKozaKapustaTest.java)
musí s vašou knižnicou korektne zbehnúť.

Odovzdávanie riešení v iných jazykoch konzultujte s cvičiacimi.
