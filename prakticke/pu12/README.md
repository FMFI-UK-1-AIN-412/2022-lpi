Cvičenie 12
===========

**Riešenie odovzdávajte podľa
[pokynov na konci tohoto zadania](#technické-detaily-riešenia)
do štvrtka 19.5. 23:59:59.**

Toto cvičenie má [bonusovú časť](#bonus),
za ktorú môžete získať ďalšie body.

Či ste správne vytvorili pull request si môžete overiť
v [tomto zozname PR pre pu12](https://github.com/pulls?utf8=%E2%9C%93&q=is%3Aopen+is%3Apr+user%3AFMFI-UK-1-AIN-412+base%3Apu12).

Súbory potrebné pre toto cvičenie si môžete stiahnuť ako jeden zip
[`pu12.zip`](https://github.com/FMFI-UK-1-AIN-412/lpi/archive/pu12.zip).

## Rezolver

Naprogramujte výrokovologický dokazovač založený na rezolvencii. V triede
`Resolver` implementujte metódy `resolve` a `isSatisfiable`.

Metóda `Set<Clause> resolve(Clause a, Clause b)` vráti množinu všetkých
klauzúl, ktoré môžu vzniknúť rezolvenciou klauzúl <var>`a`</var>
a <var>`b`</var>.

Metóda `boolean isSatisfiable(Cnf theory)` zistí pomocou rezolvencie, či je
klauzálna teória (formula v CNF) <var>`theory`</var> splniteľná.

Vieme, že teória je nesplniteľná vtt existuje jej zamietnutie (teda rezolvenčné
odvodenie končiace prázdnou klauzulou). To však nehovorí, kedy môžeme
zamietnutie prestať hľadať. Platí však, že teória je splniteľná, ak existuje
rezolvenčné odvodenie obsahujúce celú teóriu, ktoré s každou dvojicu klauzúl
obsahuje aj všetky ich rezolventy, ale neobsahuje prázdnu klauzulu.

### Hodnotenie

Hodnotí sa hlavne korektnosť a čiastočne efektívnosť. Riešenia s horšou
zložitosťou ako nižšie popísané
[„priamočiare“ riešenie](#priamočiare-riešenie) môžu dostať menej bodov.

### „Priamočiare“ riešenie

„Priamočiare“ riešenie prejde všetky dvojice klauzúl (vstupných aj
rezolvent) a každú dvojicu skúsi rezolvovať na každej výrokovej premennej
(atóme) z týchto klauzúl.

Jedna (ale nie jediná) z možností, ako zabezpečiť, aby sa prešli naozaj
naozaj všetky dvojice, je prechádzať klauzuly v poradí, ako boli pridávané,
a skúšať ich rezolvovať vždy so všetkými, ktoré boli pridané / spracované
pred nimi.

Treba si tiež dať pozor na „zacyklenie“ a nepridávať rezolventy, ktoré sa už
vyskytli na vstupe alebo vznikli skôr.
Napríklad `(a -a)` a `(a)` sa rezolvujú znovu na `(a)`.
Na plný počet bodov treba nájsť rozumnejšie riešenie ako zakaždým prechádzať
zoznamom všetkých klauzúl, aby sme zistili, či ju už nemáme.

Rezolvovať klauzuly na konkrétnej premennej sa dá lineárne od ich veľkosti.
Keď sú klauzuly reprezentované ako množiny literálov, tak sa v lineárnom
čase dá zistiť aj to, podľa ktorých premenných sa dajú rezolvovať
(aj keď pre utriedené množiny treba trošku iný prístup ako pre hashované).
Množinová reprezentácia klauzúl eliminuje potrebu pravidla idempotencie.
Pripravili sme vám základné triedy tejto reprezentácie v C++ a Jave
([technické detaily](#technické-detaily-riešenia) nižšie).

### Literatúra

Krátky popis
[rezolvencie](http://en.wikipedia.org/wiki/Resolution_(logic))
na wikipédii alebo v prvej kapitole
[Handbook of Knowledge Representation](http://ii.fmph.uniba.sk/~sefranek/kri/handbook/)
(časť 1.3.1.).

## Technické detaily riešenia

Riešenie odovzdajte do vetvy `pu12` v adresári `prakticke/pu12`.

### C++
Odovzdávajte (modifikujte) súbor(y)
[`Resolver.cpp`](pu12-cpp/Resolver.cpp)/[`.h`](pu12-cpp/Resolver.h).
Program [`ResolverTest.cpp`](pu12-cpp/ResolverTest.cpp) musí korektne
zbehnúť s vašou knižnicou.
Použite reprezentáciu literálov a množinové reprezentácie klauzúl a CNF
z modulu [`Cnf.h`](pu12-cpp/Cnf.h)/[`.cpp`](pu12-cpp/Cnf.cpp).

### Java
Odovzdávajte (modifikujte) súbor [`Resolver.java`](pu12-java/Resolver.java).
Program [`ResolverTest.java`](pu12-java/ResolverTest.java) musí korektne
zbehnúť s vašou knižnicou.
Použite reprezentáciu literálov a množinové reprezentácie klauzúl a CNF
z modulu [`Cnf.java`](pu12-java/Cnf.java).

## Bonus

Uvedené [„priamočiare“ riešenie](#priamočiare-riešenie) nevyhľadáva
rezolvovateľné dvojice klauzúl veľmi efektívnym spôsobom,
ani sa obzvlášť nezaujíma o poradie rezolvovania.

Ak implementujete nejakú efektívnejšiu (vzhľadom na čas behu) metódu výberu
alebo usporiadania rezolvovaných dvojíc klauzúl, uveďte to v pull requeste
s krátkym popisom vášho algoritmu (odkaz na nejaký internetový zdroj je OK,
implementácia ale musí byť vaša vlastná) a môžete získať **až 2 bonusové
body**.
