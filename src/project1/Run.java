package project1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Run {

	public static void main(String[] arg) {
		int students = 600;
		int courses = 18;
		int semesters = 12;
		int classesToGraduate = 12;
		int coursesPerSemester = 2;
		int[] fallOnlyOfferings = new int[] { 1, 7, 11, 15, 17 };
		int[] springOnlyOfferings = new int[] { 5, 10, 14, 16, 18 };
		int[][] prereqs = new int[][] { { 4, 16 }, { 12, 1 }, { 9, 13 },
				{ 3, 7 } };
		int[][] courseMatrix = new int[students][semesters];
		BufferedReader br = null;
		FileReader fr = null;
		try {
			File file = new File("student_schedule.txt");
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;
			int n = 0;
			while ((line = br.readLine()) != null) {
				if (line.trim() != null && line.trim().length() > 0
						&& Character.isDigit(line.trim().charAt(0))) {

					String[] tokens = line.replaceAll("\\s+", "").split("\\.");
					int m = 0;

					for (String s : tokens) {

						if (!s.equals("")) {

							courseMatrix[n][m] = Integer.valueOf(s);
							m++;
						}
					}
					if (n > 600) {
						System.out.println("ERROR");
						return;
					}
					n++;
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {

			File file = new File("studentSchedule.lp");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			// open file
			// write header
			/*
			 * minimize x subject to
			 */
			bw.write("minimize x:x");
			bw.newLine();
			bw.write("subject to");
			bw.newLine();
			/***************** constraints ******************************/

			// max student per course
			for (int j = 1; j <= courses; j++) {
				for (int k = 1; k <= semesters; k++) {

					boolean hasWrite = false;
					for (int i = 1; i <= students; i++) {
						if (!hasWrite) {
							hasWrite = true;
							bw.write("y_" + i + "_" + j + "_" + k);
						} else {
							hasWrite = true;
							bw.write(" + y_" + i + "_" + j + "_" + k);
						}

					}
					if (hasWrite) {
						bw.write(" - x <= 0");
						bw.newLine();
					}

				}
			}
			// max courses per semester
			for (int i = 1; i <= students; i++) {
				for (int k = 1; k <= semesters; k++) {

					boolean hasWrite = false;
					for (int j = 1; j <= courses; j++) {

						if (!hasWrite) {
							hasWrite = true;
							bw.write("y_" + i + "_" + j + "_" + k);
						} else {
							hasWrite = true;
							bw.write(" + y_" + i + "_" + j + "_" + k);

						}
					}

					if (hasWrite) {
						bw.write(" <= " + coursesPerSemester);
						bw.newLine();
					}

				}
			}
			// course offerings
			for (int j = 1; j <= courses; j++) {
				for (int k = 1; k <= semesters; k++) {
					boolean hasWrite = false;
					for (int i = 1; i <= students; i++) {
						if (!isCourseOffered(k, j, fallOnlyOfferings,
								springOnlyOfferings)) {
							if (!hasWrite) {
								hasWrite = true;
								bw.write("y_" + i + "_" + j + "_" + k);
							} else {
								hasWrite = true;
								bw.write(" + y_" + i + "_" + j + "_" + k);

							}
						}
					}
					if (hasWrite) {
						bw.write(" = " + 0);
						bw.newLine();
					}

				}
			}

			// prereqs for courses
			for (int i = 1; i <= students; i++) {
				for (int[] j : prereqs) {
					int j0 = j[0];
					int j1 = j[1];
					boolean hasWrite = false;
					boolean isStudentTakingPrereq1 = false;
					boolean isStudentTakingPrereq2 = false;
					for (int y : courseMatrix[i - 1]) {
						if (j0 == y) {
							isStudentTakingPrereq1 = true;
						}
						if (j1 == y) {
							isStudentTakingPrereq1 = true;
						}
					}
					if (isStudentTakingPrereq1 && isStudentTakingPrereq2) {
						for (int k = 1; k <= classesToGraduate; k++) {
							if (isCourseOffered(k, j0, fallOnlyOfferings,
									springOnlyOfferings)
									&& isCourseOffered(k, j1,
											fallOnlyOfferings,
											springOnlyOfferings)) {
								if (!hasWrite) {
									hasWrite = true;

									bw.write(k + " y_" + i + "_" + j0 + "_" + k
											+ " - " + k + " y_" + i + "_"
											+ (j1) + "_" + k);
								} else {
									bw.write(" + " + k + " y_" + i + "_" + j0
											+ "_" + k + " - " + k + " y_" + i
											+ "_" + (j1) + "_" + k);
								}

							}

						}
						if (hasWrite) {
							bw.write(" <= -1");
							bw.newLine();
						}
					}
				}

			}
			// each student take courses only once
			for (int i = 1; i <= students; i++) {
				for (int j = 1; j <= courses; j++) {
					for (int studentSelection : courseMatrix[i - 1]) {

						if (j == studentSelection) {
							boolean hasWrite = false;
							for (int k = 1; k <= semesters; k++) {
								if (!hasWrite) {
									hasWrite = true;
									bw.write("y_" + i + "_" + j + "_" + k);
								} else {
									hasWrite = true;
									bw.write(" + y_" + i + "_" + j + "_" + k);

								}
							}
							if (hasWrite) {
								bw.write(" = " + 1);
								bw.newLine();
							}
						}
					}

				}
			}
			/*************************** binary variables *********************************/
			bw.newLine();
			bw.write("binary");
			bw.newLine();
			for (int i = 1; i <= students; i++) {
				for (int j = 1; j <= courses; j++) {
					for (int k = 1; k <= semesters; k++) {
						// write

						bw.write("y_" + i + "_" + j + "_" + k);
						bw.newLine();

					}
				}
			}
			bw.write("end");
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		
		try {
			Runtime.getRuntime().exec("gurobi_cl ResultFile=studentSchedule.sol studentSchedule.lp");
			 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done! :)");
	}

	public static boolean isCourseOffered(int k, int j, int[] fall, int[] spring) {

		if ((k + 2) % 3 == 0) {
			for (int courseOffering : spring) {
				if (courseOffering == j) {
					return false;

				}
			}
		}
		if ((k + 1) % 3 == 0) {
			for (int courseOffering : fall) {
				if (courseOffering == j) {
					return false;

				}
			}

		}
		if (k % 3 == 0) {
			for (int courseOffering : fall) {
				if (courseOffering == j) {
					return false;

				}
			}
			for (int courseOffering : spring) {
				if (courseOffering == j) {
					return false;

				}
			}
		}
		return true;

	}
}
