
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.LinkedList ;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloModel;
import ilog.cplex.IloCplex;

public class Main {
	public static void main(String args[]) throws IOException, IloException {
		setprintfile("132binMIQP0.005_1threads_s_it_int_Frongio_20units_nohueristic_nopreproccessing_5hour.txt");
//		setprintfile("tmp.txt");
		LinkedList <Output4EachCase> outs = new LinkedList ();
		
		
		int time = 1; // time * N = total number of units
		int t_time = 1; // times of T
		int L = -1; // for L=-1 MIQP,  L = any int ,then linealize into L segments.
		
		int UC_play_bin_type = 2; // chose 1,2 or 3 bin model
				
		String[] allFilename = {  //"UC_AF/10_std.mod",
//				"UC_AF/10_0_1_w.mod", "UC_AF/10_0_2_w.mod", "UC_AF/10_0_3_w.mod", "UC_AF/10_0_4_w.mod", "UC_AF/10_0_5_w.mod",
				"UC_AF/20_0_1_w.mod", "UC_AF/20_0_2_w.mod", "UC_AF/20_0_3_w.mod", "UC_AF/20_0_4_w.mod", "UC_AF/20_0_5_w.mod",
				"UC_AF/50_0_1_w.mod", "UC_AF/50_0_2_w.mod", "UC_AF/50_0_3_w.mod", "UC_AF/50_0_4_w.mod", "UC_AF/50_0_5_w.mod",
				"UC_AF/75_0_1_w.mod", "UC_AF/75_0_2_w.mod", "UC_AF/75_0_3_w.mod", "UC_AF/75_0_4_w.mod", "UC_AF/75_0_5_w.mod",
				"UC_AF/100_0_1_w.mod", "UC_AF/100_0_2_w.mod", "UC_AF/100_0_3_w.mod", "UC_AF/100_0_4_w.mod", "UC_AF/100_0_5_w.mod",
				"UC_AF/150_0_1_w.mod", "UC_AF/150_0_2_w.mod", "UC_AF/150_0_3_w.mod", "UC_AF/150_0_4_w.mod", "UC_AF/150_0_5_w.mod",
				"UC_AF/200_0_1_w.mod", "UC_AF/200_0_2_w.mod", "UC_AF/200_0_3_w.mod", "UC_AF/200_0_4_w.mod", "UC_AF/200_0_5_w.mod",  "UC_AF/200_0_6_w.mod",
				"UC_AF/200_0_7_w.mod", "UC_AF/200_0_8_w.mod", "UC_AF/200_0_9_w.mod", "UC_AF/200_0_10_w.mod", "UC_AF/200_0_11_w.mod", "UC_AF/200_0_12_w.mod",
		};
		
		
//		String[] allFilename = {   //"UC_AF/8_std.mod",
//						"UC_AF/c1_28_based_8_std.mod", "UC_AF/c2_35_based_8_std.mod",
//						"UC_AF/c3_44_based_8_std.mod", "UC_AF/c4_45_based_8_std.mod",
//						"UC_AF/c5_49_based_8_std.mod", "UC_AF/c6_50_based_8_std.mod",
//						"UC_AF/c7_51_based_8_std.mod", "UC_AF/c8_51_based_8_std.mod",
//						"UC_AF/c9_52_based_8_std.mod", "UC_AF/c10_54_based_8_std.mod",
//						
//						"UC_AF/c11_132_based_8_std.mod", "UC_AF/c12_156_based_8_std.mod",
//						"UC_AF/c13_156_based_8_std.mod", "UC_AF/c14_165_based_8_std.mod",
//						"UC_AF/c15_167_based_8_std.mod", "UC_AF/c16_172_based_8_std.mod",
//						"UC_AF/c17_182_based_8_std.mod", "UC_AF/c18_182_based_8_std.mod",
//						"UC_AF/c19_183_based_8_std.mod", "UC_AF/c20_187_based_8_std.mod",
//		};
		
		
		String pathAndFilename;
		
		int casenum = 3;   //  =0 solve No. 1 case, =43 solve all the cases.
		
		for(int i=2; i<casenum; i++){
			pathAndFilename = allFilename[i];
			
			Output4EachCase output = new Output4EachCase();

			UC_solve uc_play = new UC_solve();
			uc_play.play_UC(UC_play_bin_type=1, pathAndFilename, time, t_time, L, output);
			
			outs.add(output);
					
		}
		
//		while(!outs.isEmpty()){
//			System.out.println(outs.removeFirst());
//		}
		

		
		
		for(int i=2; i<casenum; i++){
			pathAndFilename = allFilename[i];
			
			Output4EachCase output = new Output4EachCase();

			UC_solve uc_play = new UC_solve();
			uc_play.play_UC(UC_play_bin_type=3, pathAndFilename, time, t_time, L, output);
			
			outs.add(output);
					
		}
		
//		while(!outs.isEmpty()){
//			System.out.println(outs.removeFirst());
//		}
		
		
		
//		for(int i=0; i<casenum; i++){
//			pathAndFilename = allFilename[i];
//			
//			Output4EachCase output = new Output4EachCase();
//
//			UC_solve uc_play = new UC_solve();
//			uc_play.play_UC(UC_play_bin_type=2, pathAndFilename, time, t_time, L, output);
//			
//			outs.add(output);
//					
//		}
		
		while(!outs.isEmpty()){
			System.out.println(outs.removeFirst());
		}
				

	}
	
	
	
	
	
	
	public static void setprintfile(String fileName){
		try {
		    // 创建一个文件流
		    FileOutputStream fos = new FileOutputStream(fileName);
		    // 先保存原来的标准输出
		    OutputStream cos = System.out;
		    // 创建一个分发流分发到文件流和标准输出
		    DistributOutputStream osc = new DistributOutputStream(new OutputStream[] { fos, cos });
		    // 分发流的打印方式
		    PrintStream ps = new PrintStream(osc);
		    // 设置到Err和Out
		    System.setErr(ps);
		    System.setOut(ps);
		} catch (Exception e) {
		    e.printStackTrace();
		    return;
		}
		
	}
}
