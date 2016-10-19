import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.cplex.IloCplex;

public class Output4EachCase {
	
	int rows, col, nozeros, bin;
	int reduced_rows, reduced_col , reduced_nozeros, reduced_bin;
	double Egap, total_time, Fc;
	int nodes, nodes_left, iterations;
	int CliqueCover,Cover, Disj, FlowCover, FlowPath, Frac, GUBCover, ImplBd, LiftProj, LocalCover, MCF, MIR, ObjDisj,  SolnPool,  Table,  Tighten,  ZeroHalf;
	int num_cuts;
	int no_int_v, int_v;
	
	public String toString(){
		String s =  Egap + "   " + total_time + "   " + Fc + "   "  + iterations  + "    " + nodes  + "   "  +  nodes_left  +  "     " +  num_cuts + "     " + CliqueCover + "   " + Cover
				+ "    " + FlowCover + "    " + ImplBd + "   " + MIR  + "  " +  ZeroHalf + "    " + Frac  +   "    " + rows +  "   " + nozeros  + "   " + bin + "   "+ col;
		return s;
		
	}
	public void saveinfo(IloCplex cplex, readdataUC ucdata, IloLPMatrix lp){

		double[] x;
		try {
			
			rows = cplex.getNrows();
			nozeros = cplex.getNNZs();
			bin = cplex.getNbinVars();
			col = cplex.getNcols();	
			
			
			int CR = 0;
			if(CR == 0){
			
				nodes = cplex.getNnodes();
				nodes_left = cplex.getNnodesLeft();		

				CliqueCover = cplex.getNcuts(IloCplex.CutType.CliqueCover);
				Cover = cplex.getNcuts(IloCplex.CutType.Cover);
				Disj = cplex.getNcuts(IloCplex.CutType.Disj);
				FlowCover = cplex.getNcuts(IloCplex.CutType.FlowCover);
				FlowPath = cplex.getNcuts(IloCplex.CutType.FlowPath);
				Frac = cplex.getNcuts(IloCplex.CutType.Frac);
				GUBCover = cplex.getNcuts(IloCplex.CutType.GUBCover);
				ImplBd = cplex.getNcuts(IloCplex.CutType.ImplBd);
				LiftProj = cplex.getNcuts(IloCplex.CutType.LiftProj);
				LocalCover = cplex.getNcuts(IloCplex.CutType.LocalCover);
				MCF = cplex.getNcuts(IloCplex.CutType.MCF);
				MIR = cplex.getNcuts(IloCplex.CutType.MIR);
				ObjDisj = cplex.getNcuts(IloCplex.CutType.ObjDisj);
				SolnPool = cplex.getNcuts(IloCplex.CutType.SolnPool);
				Table = cplex.getNcuts(IloCplex.CutType.Table);
				Tighten = cplex.getNcuts(IloCplex.CutType.Tighten);
				ZeroHalf = cplex.getNcuts(IloCplex.CutType.ZeroHalf);			
				num_cuts = CliqueCover+Cover+ Disj+ FlowCover+ FlowPath+ Frac+ GUBCover+ ImplBd+ LiftProj+ LocalCover+ MCF+ MIR+ ObjDisj+  SolnPool+  Table+  Tighten+  ZeroHalf;
				Egap = cplex.getMIPRelativeGap();
			}
			
			
			Fc = cplex.getObjValue();
			iterations = cplex.getNiterations();
			
//			x = cplex.getValues(lp);
//			
//			for(int i = 0; i<ucdata.T0*ucdata.unit; i++){
//				if (x[i]<=0.01 || x[i]>=0.99) int_v = int_v + 1;
//				else no_int_v = no_int_v + 1;
//				System.out.println(x[i]);
//			}
			

		} catch (IloException e) {
			e.printStackTrace();
		}	
		
	
		
	
		
	}
	
}
