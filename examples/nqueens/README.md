# <var>N</var>-queens

Pomocou SAT solvera vyriešte problém <var>N</var>-dám:

Máme šachovnicu rozmerov <code>N&times;N</code>. Na ňu chceme umiestniť `N` šachových dám
tak, aby sa navzájom neohrozovali. Ohrozovanie dám je v zmysle
štandardných šachových pravidiel:

-  žiadne dve dámy nemôžu byť v rovnakom riadku,
-  žiadne dve dámy nemôžu byť v rovnakom stĺpci,
-  žiadne dve dámy nemôžu byť na tej istej uhlopriečke.

Riešenie implementujte ako triedu `NQueens`, ktorá má metódu `solve`. Metóda
`solve` má jediný argument `N` (číslo – počet dám) a vracia zoznam dvojíc čísel
(súradnice dám). Priložené testy by mali s vašou triedou zbehnúť!

## Riešenie

Použite atómy `q(i, j)`, <code>0 &le; i,j &lt; N</code>,
ktorých pravdivostná hodnota bude hovoriť, či je alebo nie je na pozícii `i,j`
umiestnená dáma.

Pre SAT solver musíme atómy `q(i, j)` zakódovať na čísla.
Keďže platí <code>0 &le; i, j &lt; N</code>, atóm `q(i, j)` môžete zakódovať ako číslo
`N*i + j + 1`. **Napíšte si na to funkciu! Ideálne s názvom `q`. Jednoduchšie
sa vám budú opravovať chyby a ľahšie sa to číta / opravuje.**

Nemusíme počítať počet dám. Stačí požadovať, že v každom riadku
musí byť nejaká dáma (určite nemôžu byť dve dámy v tom istom riadku, keďže ich
má byť `N`, musí byť v každom riadku práve jedna).

Ostatné podmienky vyjadrujte vo forme jednoduchých implikácií:<br/>
<code>q(X, Y) &rarr; &not;q(X, Z)</code> pre <code>X,Y,Z &isin; &lt;0,N), Y&ne;Z</code>
(ak je v riadku `X` dáma na pozícii `Y`, tak nemôže byť iná dáma v tom istom
riadku), atď.

V priečinku [examples/party](../../examples/party) je ukážkový program
(c++ a python), ktorý môžete použiť ako kostru vášho riešenia.
V priečinku [examples/sat](../../examples/sat) môžete nájsť knižnicu s dvoma
pomocnými triedami `DimacsWriter` a `SatSolver`, ktoré vám môžu uľahčiť prácu
so SAT solverom.
