Cvičenie 7
==========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 7.4. 23:59:59.**

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`pu07.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu07.zip).

## Hamiltonovská kružnica (2b)

Pomocou SAT solveru nájdite
[hamiltonovskú kružnicu](https://en.wikipedia.org/wiki/Hamiltonian_cycle)
v orientovanom grafe.
Nájdenie hamiltonovskej kružnice patrí, podobne ako SAT, medzi NP-úplné
problémy: Je ľahké vytvoriť nedeterministický algoritmus (Turingov stroj),
ktorý nájde riešenie v polynomiálnom čase vzhľadom na veľkosť grafu, ale známe
deterministické algoritmy sú v najhoršom prípade exponenciálne. NP-úplné
problémy sa na seba dajú navzájom redukovať – po vhodnej polynomiálnej úprave
vstupu a výstupu sa algoritmus pre SAT sa dá použiť na hľadanie hamiltonovskej
kružnice (ale aj naopak). Vašou úlohou je naprogramovať túto redukciu.

Implementujte triedu `HamiltonianCycle` s metódou `find` s jediným argumentom
`edges`, maticou susednosti: dvojrozmerné pole `n` × `n` bool-ov popisujúce
hrany v grafe tak, že ak je v `i`-tom riadku na `j`-tom mieste `True`
(`edges[i][j] == True`), tak z `i` do `j` vedie hrana (vrcholy sú číslované
od 0). Metóda vráti ako výsledok pole `n` čísel, postupnosť vrcholov na
kružnici, ak kružnica existuje, alebo prázdne pole, ak kružnica neexistuje.

### Logická reprezentácia

Potrebujeme zistiť, či sa dajú vrcholy grafu usporiadať do takej postupnosti, že
vždy ide hrana z <var>k</var>-teho do (<var>k</var>+1)-teho vrcholu v tejto
postupnosti (a z posledného do prvého). Potrebujeme teda uhádnuť, ktorý vrchol
bude na ktorej pozícii v tejto postupnosti (zabezpečiť, že pre každú pozíciu
vyberieme práve jeden vrchol), a zabezpečiť, že za sebou idúce vrcholy sú
spojené hranou správnym smerom (ak nie je v grafe hrana z <var>a</var>
do <var>b</var>, nesmú byť <var>a</var> a <var>b</var> na nejakých pozíciách
<var>k</var> a <var>k</var>+1).

Jedna z možností je mať atómy `v(pos,i)`, ktoré budú
pravdivé práve vtedy, ak vrchol `i` je na pozícii `pos`. Potom stačí:

- zabezpečiť, že je to postupnosť neopakujúcich sa vrcholov (dĺžky `n`):
  - pre každé `pos` aspoň jedno z `v(pos,i)` je pravdivé
    (na každej pozícii je aspoň jeden vrchol),
  - pre každé `pos` nie sú dve rôzne `v(pos,i)` a `v(pos,j)` pravdivé naraz
    (nie sú dva vrcholy na tej istej pozícii),
  - pre každé `i` nie sú dve rôzne `v(pos1,i)` a `v(pos2,i)` pravdivé naraz
    (nie je jeden vrchol na dvoch rôznych pozíciách);
- zabezpečiť, že za sebou idúce vrcholy sú spojené hranou, teda, ak sú
  v postupnosti za sebou, tak musia byť spojené hranou, resp. (obmena) ak nie je
  hrana z `i` do `j`, tak nemôžu byť za sebou:
  - ak nie je v grafe hrana z `i` do `j`, tak pre každé `pos` nesmú platiť
    `v(pos,i)` a `v(pos+1,j)` (naraz), plus samozrejme aj pre posledný a prvý,
    aby to bola kružnica.

Trošku nepekná vec na tomto zakódovaní / riešení je, že potrebujeme generovať
klauzuly (alebo pomocné atómy) pre každú dvojicu vrcholov, medzi ktorými nie
je hrana. Plus netreba zabúdať, že pracujeme s orientovaným grafom.


## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu07` v adresári `prakticke/pu07/pu07-{python,java}`.

Odovzdajte súbor [`HamiltonianCycle.py`](pu07-python/HamiltonianCycle.py) /
[`HamiltonianCycle.java`](pu07-java/HamiltonianCycle.java)
v ktorom je implementovaná trieda `HamiltonianCycle`
s metódou `find`.

Ak chcete v pythone použiť knižnicu z [examples/sat](../../examples/sat), nemusíte
si ju kopírovať do aktuálneho adresára, stačí ak na začiatok svojej knižnice
pridáte:
```python
import os
import sys
sys.path[0:0] = [os.path.join(sys.path[0], '..', '..', '..', 'examples', 'sat')]
import sat
```

Odovzdávanie riešení v iných jazykoch konzultujte s cvičiacimi.
