import java.util.Vector;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

//refer to TuneSet.java in Cplex/examples

public class TuneTest {

	public static void tuning(String prob, char tune_type, String fixedfile){

		try {
			IloCplex cplex = new IloCplex();
			int            tunemeasure = 0;
			boolean        mset = false;
			Vector<String> filenames = new Vector<String>();

			filenames.add(prob);

			switch ( tune_type ) {
			case 'a':
				tunemeasure = 1;
				mset = true;
				break;
			case 'm':
				tunemeasure = 2;
				mset = true;
				break;
			}

			System.out.println("Problem Name:");
			System.out.println("  " + prob);


			if ( mset )
				cplex.setParam(IloCplex.Param.Tune.Measure, tunemeasure);

			IloCplex.ParameterSet paramset = null;


     		cplex.setParam( IloCplex.DoubleParam.EpGap, 0.005);
//    		cplex.setParam(IloCplex.IntParam.Threads, 1);
            cplex.setParam(IloCplex.DoubleParam.TiLim, 3600); 
//            cplex.setParam(IloCplex.Param.MIP.Display, 2); 
            paramset = cplex.getParameterSet();
			
            cplex.setDefaults();
            
	         if ( fixedfile != null ) {
	             cplex.readParam(fixedfile);
	             

	             cplex.setDefaults();
	          }
		
			
			int tunestat = cplex.tuneParam(filenames.toArray(new String[0]), paramset);

			if      ( tunestat == IloCplex.TuningStatus.Complete)
				System.out.println("Tuning complete.");
			else if ( tunestat == IloCplex.TuningStatus.Abort)
				System.out.println("Tuning abort.");
			else if ( tunestat == IloCplex.TuningStatus.TimeLim)
				System.out.println("Tuning time limit.");
			else
				System.out.println("Tuning status unknown.");

			
			String tunedfile = prob.replace(".lp", ".PRM");
			
            cplex.writeParam(tunedfile);
            System.out.println("Tuned parameters written to file '" +
                               tunedfile + "'");
			

			cplex.end();
		}
		catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

}


