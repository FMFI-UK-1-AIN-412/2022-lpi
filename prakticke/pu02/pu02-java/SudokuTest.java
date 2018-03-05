import java.util.Arrays;

class Tester {
	private int casei = 0;
	private int tested = 0;
	private int passed = 0;

	public boolean fail(String msg, Object arg) {
		System.err.print("ERROR: ");
		System.err.print(msg);
		System.err.print(" ");
		System.err.println(toString(arg));

		return false;
	}

	public boolean fail(String msg, Object arg1, Object arg2) {
		System.err.print("ERROR: ");
		System.err.print(msg);
		System.err.print(" ");
		System.err.print(toString(arg1));
		System.err.print(" ");
		System.err.println(toString(arg2));

		return false;
	}

	String toString(Object obj) {
		if (obj instanceof int[]) {
			return Arrays.toString((int[])obj);
		} else {
			return obj.toString();
		}
	}

	public boolean checkSize(int[][] sudoku) {
		if (sudoku.length != 9) {
			return fail("Wrong number of rows in result", "");
		}

		for (int i = 0; i < sudoku.length; ++i) {
			if (sudoku[i].length != 9) {
				return fail("Wrong number of numbers in row", i);
			}
		}

		return true;
	}

	public boolean checkList(int[] l, String msg) {
		// should not happen
		if (l.length != 9)
				return fail("Wrong result format ", msg, l);

		int x = 0;
		for(int i : l)
				x |= (1 << i);
		if (x != (1023 - 1))
				return fail("Wrong numbers ", msg, l);

		return true;
	}

	public boolean checkInput(int[][] i, int[][] s) {
		for(int r = 0; r < i.length; ++r) {
			for(int c = 0; c < i[r].length; ++c) {
				if (i[r][c] != 0) {
					if (i[r][c] != s[r][c]) {
						return fail("Result does not match input", r, c);
					}
				}
			}
		}
		return true;
	}

	public boolean checkRows(int[][] s) {
		for (int i = 0; i < s.length; ++i) {
			if (checkList(s[i], "in row") == false) {
				return false;
			}
		}

		return true;
	}

	public boolean checkCols(int[][] s) {
		for(int c = 0; c < 9; ++c) {
			int[] l = new int[s.length];
			for(int i = 0; i < s.length; ++i) {
				l[i] = s[i][c];
			}

			if (!checkList(l, "in col"))
				return false;
		}
		return true;
	}

	public boolean checkSquares(int [][]s) {
		for(int sr=0; sr < 3; ++sr) {
			for(int sc = 0; sc < 3; ++sc) {
				int[] l = new int[9];
				int i = 0;
				for(int r = 0; r < 3; ++r) {
					for(int c = 0; c < 3; ++c) {
						l[i] = s[sr*3 + r][sc*3 + c];
						++i;
					}
				}

				if (!checkList(l, "in square"))
						return false;
			}
		}
		return true;
	}

	public boolean checkTests(boolean[] tests)
	{
		tested += tests.length;
		boolean ret = true;
		for(boolean r : tests) {
				if (r) passed++;
				ret = ret && r;
		}
		return ret;
	}

	public boolean checkGood(int[][] i, int[][] s) {
		if (!checkSize(s)) {
			tested += 4;
			return false;
		}

		return checkTests(new boolean[] {
			checkInput(i,s),
			checkRows(s),
			checkCols(s),
			checkSquares(s),
			});
	}

	public boolean checkBad(int[][] s)
	{
		tested += 4;
		if (!checkSize(s)) {
				return false;
		}

		for(int[] row: s) {
			for(int c: row) {
				if (c != 0) {
						return fail("Nonzero in bad sudoku!", "");
				}
			}
		}
		passed += 4;
		return true;
	}

	public boolean check(int [][]i, boolean good, int[][] s)
	{
		if (good)
			return checkGood(i, s);
		else
			return checkBad(s);
	}

	public void test(int[][] i, boolean good, int[][]s)
	{
		casei++;
		System.err.println("");
		System.err.print("Case ");
		System.err.print(casei);
		System.err.print(": ");

		if (check(i, good, s)) {
			System.err.println("PASSED");
		} else {
			System.err.println("          INPUT              OUTPUT");
			for(int r = 0; r < Math.max(i.length, s.length); ++r) {
				if (r<i.length) {
					System.err.print(Arrays.toString(i[r]));
				}
				else {
					System.err.println("                    ");
				}
				System.err.print("    ");
				if (r<s.length) {
					System.err.print(Arrays.toString(s[r]));
				}
				System.err.println();
			}
		}
	}

	public boolean status() {
		System.err.println("");
		System.err.print("TESTED ");
		System.err.println(tested);

		System.err.print("PASSED ");
		System.err.println(passed);

		System.err.println(tested == passed ? "OK" : "ERROR" );
		return tested == passed;
	}
}

public class SudokuTest {

	public static void main(String[] args) {
		Tester t = new Tester();

	int[][] i;
	i = new int[][]{
		{5, 3, 0, 0, 7, 0, 0, 0, 0},
		{6, 0, 0, 1, 9, 5, 0, 0, 0},
		{0, 9, 8, 0, 0, 0, 0, 6, 0},
		{8, 0, 0, 0, 6, 0, 0, 0, 3},
		{4, 0, 0, 8, 0, 3, 0, 0, 1},
		{7, 0, 0, 0, 2, 0, 0, 0, 6},
		{0, 6, 0, 0, 0, 0, 2, 8, 0},
		{0, 0, 0, 4, 1, 9, 0, 0, 5},
		{0, 0, 0, 0, 8, 0, 0, 7, 9},
	};
	t.test(i, true, new SudokuSolver().solve(i));

	i = new int[][]{
		{1, 2, 3, 4, 5, 6, 7, 8, 9},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
	};
	t.test(i, true, new SudokuSolver().solve(i));

	i = new int[][]{
		{1, 1, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
	};
	t.test(i, false, new SudokuSolver().solve(i));

	i = new int[][]{
		{0, 9, 5, 0, 0, 3, 6, 0, 0},
		{0, 6, 0, 0, 5, 1, 0, 3, 8},
		{1, 8, 0, 0, 4, 6, 7, 0, 9},
		{5, 0, 4, 0, 2, 0, 0, 0, 6},
		{6, 1, 0, 4, 8, 0, 0, 2, 0},
		{8, 3, 0, 0, 0, 0, 0, 7, 0},
		{9, 5, 0, 7, 3, 4, 0, 6, 0},
		{0, 0, 6, 0, 0, 0, 4, 0, 0},
		{7, 0, 0, 0, 0, 2, 5, 9, 3},
	};
	t.test(i, true, new SudokuSolver().solve(i));

	i = new int[][]{
		{5, 3, 4, 6, 7, 8, 9, 1, 0},
		{6, 7, 2, 1, 9, 5, 3, 4, 0},
		{1, 9, 8, 3, 4, 2, 5, 6, 0},
		{8, 5, 9, 7, 6, 1, 4, 2, 0},
		{4, 2, 6, 8, 5, 3, 7, 9, 0},
		{7, 1, 3, 9, 2, 4, 8, 5, 0},
		{9, 6, 1, 5, 3, 7, 2, 8, 0},
		{2, 8, 7, 4, 1, 9, 6, 3, 0},
		{3, 4, 5, 2, 8, 6, 1, 7, 0},
	};
	t.test(i, true, new SudokuSolver().solve(i));

	i = new int[][]{
		{5, 3, 4, 6, 7, 8, 9, 1, 2},
		{6, 7, 2, 1, 9, 5, 3, 4, 8},
		{1, 9, 8, 3, 4, 2, 5, 6, 7},
		{8, 5, 9, 7, 6, 1, 4, 2, 3},
		{4, 2, 6, 8, 5, 3, 7, 9, 1},
		{7, 1, 3, 9, 2, 4, 8, 5, 6},
		{9, 6, 1, 5, 3, 7, 2, 8, 4},
		{2, 8, 7, 4, 1, 9, 6, 3, 5},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
	};

	t.test(i, true, new SudokuSolver().solve(i));


	i = new int[][]{
		{0, 0, 5, 7, 0, 9, 8, 0, 0},
		{0, 7, 0, 0, 8, 0, 0, 9, 0},
		{0, 8, 0, 4, 0, 2, 0, 3, 0},
		{0, 6, 4, 0, 5, 0, 3, 1, 0},
		{8, 0, 0, 0, 9, 0, 0, 0, 2},
		{7, 0, 0, 0, 0, 0, 0, 0, 9},
		{0, 0, 7, 6, 0, 4, 2, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{3, 0, 0, 0, 2, 0, 0, 0, 6},
	};
	t.test(i, true, new SudokuSolver().solve(i));

	i = new int[][]{
		{0, 0, 5, 7, 0, 9, 8, 0, 0},
		{0, 7, 0, 0, 8, 0, 0, 9, 0},
		{0, 8, 0, 4, 0, 2, 0, 3, 0},
		{0, 6, 4, 0, 5, 0, 3, 1, 0},
		{8, 0, 0, 0, 9, 0, 0, 0, 2},
		{7, 0, 0, 0, 0, 0, 6, 0, 9},
		{0, 0, 7, 6, 0, 4, 2, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{3, 0, 0, 0, 2, 0, 0, 0, 6},
	};
	t.test(i, false, new SudokuSolver().solve(i));

	i = new int[][]{
		{0, 0, 6, 0, 4, 0, 7, 0, 0},
		{0, 0, 0, 1, 0, 2, 0, 0, 0},
		{2, 0, 0, 0, 3, 0, 0, 0, 4},
		{3, 0, 0, 0, 0, 0, 0, 0, 6},
		{0, 0, 8, 3, 0, 4, 5, 0, 0},
		{0, 2, 0, 0, 5, 0, 0, 7, 0},
		{0, 1, 0, 0, 9, 0, 0, 4, 0},
		{0, 5, 0, 0, 0, 0, 0, 9, 0},
		{8, 0, 4, 7, 0, 5, 2, 0, 3},
	};
	t.test(i, true, new SudokuSolver().solve(i));

	i = new int[][]{
		{0, 0, 6, 0, 4, 0, 7, 0, 0},
		{0, 0, 0, 1, 0, 2, 0, 0, 0},
		{2, 0, 0, 0, 3, 0, 0, 0, 4},
		{3, 0, 0, 8, 0, 0, 0, 0, 6},
		{0, 0, 8, 3, 0, 4, 5, 0, 0},
		{0, 2, 0, 0, 5, 0, 0, 7, 0},
		{0, 1, 0, 0, 9, 0, 0, 4, 0},
		{0, 5, 0, 0, 0, 0, 0, 9, 0},
		{8, 0, 4, 7, 0, 5, 2, 0, 3},
	};
	t.test(i, false, new SudokuSolver().solve(i));

	System.exit(t.status() ? 0 : 1);

	}

}
