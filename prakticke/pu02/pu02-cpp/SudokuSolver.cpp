#include "SudokuSolver.h"

std::vector<std::vector<int> > SudokuSolver::solve(const std::vector<std::vector<int> > &sudoku) {
    // all zeroes -> no solution
    return {9, std::vector<int>(9,0)};
}
