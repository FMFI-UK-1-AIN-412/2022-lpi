from typing import Sequence


Sudoku = Sequence[Sequence[int]]


class SudokuSolver:
    def solve(self, sudoku: Sudoku) -> Sudoku:
        # all zeroes -> no solution
        return list(9 * [0] for _ in range(9))
