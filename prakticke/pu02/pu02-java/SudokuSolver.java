import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;



class SudokuSolver {
    public static int[][] solve(int[][] sudoku) {
	// all zeroes -> no solution
        /*for(int i=0; i<9; i++) {
            for(int j=0; j<9; j++) {
                System.out.printf("%d ", sudoku[i][j]);
            }

            System.out.println();
        }*/
        int[][] out = new int[9][9];
        try {
            FileWriter myWriter = new FileWriter("in.txt");
            for (int i=0; i<9; i++) {
                for (int j=0; j<9; j++) {
                    if (sudoku[i][j] != 0) {
                        myWriter.write(toNum(new int[]{i,j,sudoku[i][j]}) + " 0\n");
                    }
                }
            }

            for (int row = 0; row<=8; row++)
                for (int col = 0; col<=8; col++) {
                        for (int x = 1; x<=9; x++) myWriter.write(toNum(new int[]{row,col,x}) + " ");
                        myWriter.write("0 \n");
                }



            for (int row = 0; row<=8; row++)
                for (int col = 0; col<=8; col++)
                    //if (row<=col)
                        for (int x = 1; x<=9; x++)
                                            for (int y = 1; y<=9; y++) if (x != y) myWriter.write(-toNum(new int[]{row,col,x}) + " " + -toNum(new int[]{row,col,y}) + " 0\n");
            //myWriter.write("\n\n\n");
            for(int r=0; r<9; r++)
                for(int s1=0; s1<9; s1++)
                    for(int s2=0; s2<9; s2++)
                        if (s1 != s2) for(int c=1; c<=9; c++) myWriter.write(-toNum(new int[]{r,s1,c}) + " " + -toNum(new int[]{r,s2,c}) + " 0\n");

            //myWriter.write("\n\n\n");
            for(int r1=0; r1<9; r1++)
                for(int r2=0; r2<9; r2++)
                    if (r1 != r2) for(int s=0; s<9; s++)
                        for(int c=1; c<=9; c++) myWriter.write(-toNum(new int[]{r1,s,c}) + " " + -toNum(new int[]{r2,s,c}) + " 0\n");

            //myWriter.write("\n\n\n");
            for(int r1=0; r1<9; r1++)
                for(int r2=0; r2<9; r2++)
                    for(int s1=0; s1<9; s1++)
                        for(int s2=0; s2<9; s2++)
                        if (inSameSquare3x3(r1,s1,r2,s2) && !sameSquare(r1,s1,r2,s2))
                            for(int c=1; c<=9; c++) myWriter.write(-toNum(new int[]{r1,s1,c}) + " " + -toNum(new int[]{r2,s2,c}) + " 0\n");

            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
            ProcessBuilder pb = new ProcessBuilder();

            pb.command("minisat.exe", "in.txt", "out.txt");
            Process process = pb.start();

            BufferedReader stdInput
                    = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                //System.out.println(s);
            }

            File myObj2 = new File("out.txt");
            Scanner myReader = new Scanner(myObj2);


            if (!myReader.hasNext()){
                System.out.println("Error in out.txt file");
            }
            else {
                if(myReader.next().equals("SAT")) {
                    //System.out.println("Sudoku is satisfiable!");
                    int tmp = 0;
                    int count = 0;
                    int[] tmp_arr = new int[3];
                    while(myReader.hasNextInt()) {

                        tmp = myReader.nextInt();
                        if (tmp > 0) {
                            count++;

                            tmp_arr = new int[3];
                            tmp_arr = toTriplet(tmp);
                            //System.out.printf("%d:[%d,%d,%d];\n", tmp, tmp_arr[0], tmp_arr[1], tmp_arr[2]);

                            out[tmp_arr[0]][tmp_arr[1]] = tmp_arr[2];
                        }

                    }
                    //System.out.println("Count: " + count);
                }
                //else System.out.println("Sudoku is unsatisfiable!");
            }
            myReader.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        /*for(int i=0; i<9; i++) {
            for(int j=0; j<9; j++) {
                System.out.printf("%d ", out[i][j]);
            }

            System.out.println();
        }*/
        return out;
    }

    private static boolean inSameSquare3x3(int r1, int s1, int r2, int s2) {
        return (r1 / 3 == r2 / 3) && (s1 / 3 == s2 / 3);
    }

    private static boolean sameSquare(int r1, int s1, int r2, int s2) {
        return (r1 == r2) && (s1 == s2);
    }

    private static int[] toTriplet(int number) {
        number--;
        int[] num = new int[3];
        int tmp = 0;
        while (tmp < 3) {
            num[2-tmp] = number % 9;
            number = number / 9;
            tmp++;
        }

        //System.out.printf("triplet: %d%d%d; ", num[0], num[1], num[2] + 1);
        num[2]++;
        return num;

    }

    private static int toNum(int[] triplet) {
        return triplet[0]*9*9 + triplet[1]*9 + triplet[2];
    }
}
