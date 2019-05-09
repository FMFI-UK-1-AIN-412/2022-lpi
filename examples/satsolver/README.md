SatSolver
=========

Toto je kostra pre java verziu SAT solvera.

Oproti kostre v [úlohe 8](../../prakticke/pu08/) obsahuje `main` a metódy
potrebné na to, aby sa dal použiť ako samostatný solver, ktorý načítava zadanie
zo súboru v DIMACS formáte a tiež zapisuje výsledok do súboru (podľa
požiadaviek bonusovej súťaže o najrýchlejší SAT solver).

Priadané / zmenené metódy:

- `SatSolver.java`:
  - `loadDimacs`
  - `writeDimacsResult`
  - `main`
- `Theory.java`:
  - `private Clause(...)` -> `public Clause(...)`
  - `public Theory(Scanner s)`
