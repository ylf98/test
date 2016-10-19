import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Generate_8_std_cases {

	public static void main(String[] args) throws IOException {		
		int[][] Case_matrix = { { 12, 11, 0, 0, 1, 4, 0, 0 }, 
				{ 13, 15, 2, 0, 4, 0, 0, 1 },
				{ 15, 13, 2, 6, 3, 1, 1, 3 }, 
				{ 15, 11, 0, 1, 4, 5, 6, 3 }, 
				{ 15, 13, 3, 7, 5, 3, 2, 1 },
				{ 10, 10, 2, 5, 7, 5, 6, 5 }, 
				{ 17, 16, 1, 3, 1, 7, 2, 4 }, 
				{ 17, 10, 6, 5, 2, 1, 3, 7 },
				{ 12, 17, 4, 7, 5, 2, 0, 5 }, 
				{ 13, 12, 5, 7, 2, 5, 4, 6 }, 
				{ 46, 45, 8, 0, 5, 0, 12, 16 },
				{ 40, 54, 14, 8, 3, 15, 9, 13 }, 
				{ 50, 41, 19, 11, 4, 4, 12, 15 }, 
				{ 51, 58, 17, 19, 16, 1, 2, 1 },
				{ 43, 46, 17, 15, 13, 15, 6, 12 }, 
				{ 50, 59, 8, 15, 1, 18, 4, 17 }, 
				{ 53, 50, 17, 15, 16, 5, 14, 12 },
				{ 45, 57, 19, 7, 19, 19, 5, 11 }, 
				{ 58, 50, 15, 7, 16, 18, 7, 12 },
				{ 55, 48, 18, 5, 18, 17, 15, 11 }, };
		int[] total_units = { 28, 35, 44, 45, 49, 50, 51, 51, 52, 54, 132, 156, 156, 165, 167, 172, 182, 182, 183,	187 };

		String pathAndFilename = "UC_AF/8_std.mod";
		System.out.println(pathAndFilename);

		FileInputStream inputStream = null;
		Scanner sc = null;

		BufferedWriter bufferwriter = null;

		String outFileName = "";

		String tmp_line;
		
		for (int c = 0; c < 20; c++) {

			outFileName = "UC_AF/c" + (c + 1) + "_" + total_units[c] + "_based_8_std.mod";

			inputStream = new FileInputStream(pathAndFilename);

			bufferwriter = new BufferedWriter(new FileWriter(outFileName));

			sc = new Scanner(inputStream, "UTF-8");

			bufferwriter.write(sc.nextLine()+"\n"); // copy first line;
			bufferwriter.write(sc.nextLine()+"\n"); // copy second line;

			tmp_line = sc.nextLine();
			tmp_line = tmp_line.replace(" 8", " " + total_units[c]);

			bufferwriter.write(tmp_line+"\n"); // copy line 3

			for (int i = 0; i < 11; i++) {
				bufferwriter.write(sc.nextLine()+"\n"); // copy 4-14 lines
			}
			
			for (int i = 0; i < 8; i++) {
				String s1 = sc.nextLine();
				String s2 = sc.nextLine();

				for (int j = 0; j < Case_matrix[c][i]; j++) {
					bufferwriter.write(s1+"\n");
					bufferwriter.write(s2+"\n");
				}

			}

			bufferwriter.write(sc.nextLine()+"\n"); // copy last 2 lines;
			bufferwriter.write(sc.nextLine()+"\n");
			
			inputStream.close();
			bufferwriter.close();
		}

	}

}
