import java.io.FileWriter;
import java.io.IOException;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.cplex.IloCplex;

public class UC_solve {
	void play_UC(int UC_play_bin_type, String pathAndFilename, int time, int t_time, int L, Output4EachCase output)
			throws IOException, IloException {
		//-----------------
		readdataUC ucdata = new readdataUC(pathAndFilename, time, t_time);
		
//		ucdata.sub_system(3, 3);
		
		String output_path = pathAndFilename.replace("UC_AF/", "out_put/");
		output_path = output_path.replace(".mod", "_");
		
		if  (UC_play_bin_type == 2) {
			Projection projection = new Projection();
			ucdata = projection.ProjectreaddataUC(ucdata);
		}

		IloCplex cplex = new IloCplex();
		
//		if  (UC_play_bin_type == 1) {
//			cplex.setParam(IloCplex.IntParam.RootAlg, 4);
//		}
		
		
		cplex.setParam( IloCplex.DoubleParam.EpGap, 0.005);
//		cplex.setParam(IloCplex.IntParam.Threads, 1);
		cplex.setParam(IloCplex.DoubleParam.TiLim, 5*3600);    // cplex terminate if time limit hit 3600s
		
		int tuning = 0;    // = 1 turn on Cplex tuning, =0 turn off CPLEX tuning
		char turn_type = 'a';   // can be 'a' average,  or be 'm' minimize
		
		
//		cplex.setParam(IloCplex.Param.MIP.Display, 5);
		//cplex.setParam(IloCplex.Param.MIP.Interval, 50);
		
		// turn off all the presolve reduce procedures to see the original models
		int preprocessing_swith = 0;
		if (preprocessing_swith == 0) {
			cplex.setParam(IloCplex.Param.Preprocessing.Presolve, false);
			cplex.setParam(IloCplex.Param.Preprocessing.Reduce, 0);
			cplex.setParam(IloCplex.Param.Preprocessing.RepeatPresolve, 0);
			cplex.setParam(IloCplex.IntParam.Reduce, 0);
			cplex.setParam(IloCplex.IntParam.RelaxPreInd, 0);
		}
		
		cplex.setParam(IloCplex.LongParam.HeurFreq, -1);   // turn of MIP heuristic
		int cuts_switch = 1;
		if (cuts_switch == 0){
			cplex.setParam(IloCplex.IntParam.Cliques, -1);
			cplex.setParam(IloCplex.IntParam.Covers, -1);
			cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
			cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
			cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
			cplex.setParam(IloCplex.IntParam.FracCuts, -1);
			cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
			cplex.setParam(IloCplex.IntParam.ImplBd, -1);
			cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
			cplex.setParam(IloCplex.IntParam.MCFCuts, -1);
			cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
		}

		


		Prob_set prob_set = new Prob_set(ucdata, cplex);
		FileWriter fileWriter = null;
		IloLPMatrix lp = null;
		if (UC_play_bin_type == 1) {	
			lp = prob_set.bin_1_populateByRow(L);
			cplex.exportModel(output_path+"UC_1_bin_model.lp");
			
			if(tuning == 1) {TuneTest.tuning(output_path+"UC_1_bin_model.lp", turn_type, null);}
			
			fileWriter = new FileWriter(output_path+"UC_1_bin_model_results.txt", true);
		}	

		if (UC_play_bin_type == 2) {

			lp = prob_set.bin_2_populateByRow(L);
			cplex.exportModel(output_path+"UC_2_bin_model.lp");
			
			if(tuning == 1) {TuneTest.tuning(output_path+"UC_2_bin_model.lp", turn_type, null);}
			
			fileWriter = new FileWriter(output_path+"UC_2_bin_model_results.txt", true);
		}	
		if (UC_play_bin_type == 3) {
			lp = prob_set.bin_3_populateByRow(L);
			cplex.exportModel(output_path+"UC_3_bin_model.lp");
			
			
			if(tuning == 1) { TuneTest.tuning(output_path+"UC_3_bin_model.lp", turn_type, null);}
			
			fileWriter = new FileWriter(output_path+"UC_3_bin_model_results.txt", true);
		}
				

		
//		output.rows = cplex.getNrows();
//		output.nozeros = cplex.getNNZs();
//		output.bin = cplex.getNbinVars();
//		output.col = cplex.getNcols();
		
		long start = System.currentTimeMillis();
		
		
		
		
		
		
		//if (true) {
		if (cplex.solve()) {
//		if (false) {
			long end = System.currentTimeMillis();
			//System.out.println("\n Run time:" + (end - start) + " ms");
			//fileWriter.write("\r\n Run time:" + (end - start) + " ms\r\n");
			
			output.saveinfo(cplex,ucdata,lp);
			output.total_time = end - start;
			
			//double[] x = {1,2,3};
			double[] x = cplex.getValues(lp);

			System.out.println("Solution status = " + cplex.getStatus());
			System.out.println("Solution value  = " + cplex.getObjValue());
			//fileWriter.write("\r\n Solution value  = " + cplex.getObjValue() + "\r\n");

			//------------- print all the result for 3 models
			int print_all = 0;
			if (print_all == 1){
				if (UC_play_bin_type == 1) {

					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n unit_u：" + String.format("% 4d", (i + 1)));  //-------------------
						fileWriter.write("\r\n unit_u：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + String.format("%3d", Math.round(x[i + t * ucdata.unit])));
							fileWriter.write(" | " + String.format("%3d", Math.round(x[i + t * ucdata.unit])));
						}
					}
					System.out.print("\n\n\n");
					fileWriter.write("\r\n");


					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n unit_p：" + (i + 1));
						fileWriter.write("\r\n unit_p：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + String.format("%3d", Math.round(x[i + t * ucdata.unit + ucdata.unit * ucdata.T0])));
							fileWriter.write(" | " + String.format("%3d", Math.round(x[i + t * ucdata.unit + ucdata.unit * ucdata.T0])));
						}
					}
					System.out.print("\n\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n unit_c：" + (i + 1));
						fileWriter.write("\r\n unit_c：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + String.format("%3d", Math.round(x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0])));
							fileWriter.write(" | " + String.format("%3d", Math.round(x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0])));
						}
					}
					System.out.print("\n\n");
					fileWriter.write("\r\n");
				}


				if (UC_play_bin_type == 2) {
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组u：" + (i + 1));
						fileWriter.write("\r\n 机组u：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit]));

						}
					}
					System.out.print("\n\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组p：" + (i + 1));
						fileWriter.write("\r\n 机组p：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(
									x[i + t * ucdata.unit + ucdata.unit * ucdata.T0] * (ucdata.p_up0[i] - ucdata.p_low0[i])
									+ x[i + t * ucdata.unit] * ucdata.p_low0[i]));
							fileWriter.write(" | " + Math.round(
									x[i + t * ucdata.unit + ucdata.unit * ucdata.T0] * (ucdata.p_up0[i] - ucdata.p_low0[i])
									+ x[i + t * ucdata.unit] * ucdata.p_low0[i]));

						}
					}
					System.out.print("\n\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组c：" + (i + 1));
						fileWriter.write("\r\n 机组c：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | "
									+ Math.round(ucdata.Hot_cost0[i] * x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0]
											+ x[i + t * ucdata.unit + 3 * ucdata.unit * ucdata.T0]));
							fileWriter.write(" | "
									+ Math.round(ucdata.Hot_cost0[i] * x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0]
											+ x[i + t * ucdata.unit + 3 * ucdata.unit * ucdata.T0]));
						}
					}
					System.out.print("\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组s：" + (i + 1));
						fileWriter.write("\r\n 机组s：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0]));
						}
					}

					fileWriter.write("\r\n");

				}



				if (UC_play_bin_type == 3) {

					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组u：" + (i + 1));
						fileWriter.write("\r\n 机组u：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit]));
						}
					}
					System.out.print("\n\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组p：" + (i + 1));
						fileWriter.write("\r\n 机组p：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit + ucdata.unit * ucdata.T0]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit + ucdata.unit * ucdata.T0]));
						}
					}
					System.out.print("\n\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组c：" + (i + 1));
						fileWriter.write("\r\n 机组c：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit + 4 * ucdata.unit * ucdata.T0]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit + 4 * ucdata.unit * ucdata.T0]));
						}
					}
					System.out.print("\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组s：" + (i + 1));
						fileWriter.write("\r\n 机组s：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit + 2 * ucdata.unit * ucdata.T0]));
						}
					}

					System.out.print("\n\n");
					fileWriter.write("\r\n");
					for (int i = 0; i < ucdata.unit; i++) {
						System.out.print("\n 机组d：" + (i + 1));
						fileWriter.write("\r\n 机组d：" + (i + 1));
						for (int t = 0; t < ucdata.T0; t++) {
							System.out.print(" | " + Math.round(x[i + t * ucdata.unit + 3 * ucdata.unit * ucdata.T0]));
							fileWriter.write(" | " + Math.round(x[i + t * ucdata.unit + 3 * ucdata.unit * ucdata.T0]));
						}
					}
					System.out.print("\n\n");
					fileWriter.write("\r\n");

				}
			}
			//-------------end print all the result for 3 models			


			cplex.end();
			fileWriter.flush();
			fileWriter.close();

		}   //end if (cplex.solve()) {

	}
}

