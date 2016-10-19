import java.util.LinkedList;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLQNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.concert.IloRange;

public class Prob_set {
	readdataUC ucdata;
    IloCplex model; 
    int ramp;
    int CR;
    int s_it_CR;
	Prob_set(readdataUC ucdata,IloCplex cplex){
		this.ucdata=ucdata;
		this.model=cplex;
	    this.ramp = 1;	 // ramp = 0 noramp,      =1 with ramp
	    this.CR = 0;   // CR = 1  continuous relaxation solved     CR = 0 MIP solved.
	    this.s_it_CR = 1;  // s_it_CR = 1  var s_it is setted as continuous var     CR = 0  s_it is setted as integer var.
	    
	}
	//1_bin_uc
	 IloLPMatrix bin_1_populateByRow(int L) throws IloException {
	      IloLPMatrix lp = model.addLPMatrix();
	      double[]    lb = new double[4*ucdata.unit*ucdata.T0];
	      double[]    ub = new double[4*ucdata.unit*ucdata.T0];
	      IloNumVarType[] xt =  new IloNumVarType[4*ucdata.unit*ucdata.T0];
	      
	      for (int i=0;i<4*ucdata.unit*ucdata.T0;i++){
	    	  if (i<ucdata.unit*ucdata.T0)
	    	  {
	    		  lb[i]=0.0; ub[i]=1.0; 
	    		  xt[i]=IloNumVarType.Bool;
	    		  if(CR == 1) xt[i] = IloNumVarType.Float;
	    	  }else{
	    		  lb[i]=0.0; ub[i]=Double.MAX_VALUE;xt[i]=IloNumVarType.Float;
	    	  }   		  
	      }
	      
	      IloNumVar[] x  = model.numVarArray(model.columnArray(lp, 4*ucdata.unit*ucdata.T0), lb, ub,xt);

	      
	      
	      //Unit generation capacity limits constrains	      
	      IloRange[] A_b_low_up_con = new IloRange[2 * ucdata.T0 * ucdata.unit]; 	      
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++)
	    	  { 	
	    		  A_b_low_up_con[ t*ucdata.unit + i] = model.le( model.diff(model.prod(ucdata.p_low0[i],x[i+t*ucdata.unit]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]),0.0);
	    		  A_b_low_up_con[ ucdata.T0 * ucdata.unit + t*ucdata.unit + i ] =model.ge( model.diff(model.prod(ucdata.p_up0[i],x[i+t*ucdata.unit]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]),0.0);

//	    		  lp.addRow(model.le( model.diff(model.prod(ucdata.p_low0[i],x[i+t*ucdata.unit]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]),0.0));
//	    	      lp.addRow(model.ge( model.diff(model.prod(ucdata.p_up0[i],x[i+t*ucdata.unit]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]),0.0));
	    	      }
	      }
	      lp.addRows(A_b_low_up_con);
	   
	      //Power balance constrains
	      IloRange[] A_b_balance = new IloRange[ucdata.T0]; 	 
	      IloNumExpr[] cbt =new IloNumExpr[ucdata.unit];		  	   
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++){
	    		   cbt[i]=x[i+t*ucdata.unit+ucdata.unit*ucdata.T0];	  
	    	  }
	    	  A_b_balance[t] = model.eq(model.sum(cbt),ucdata.PD0[t]);
//	         lp.addRow(model.eq(model.sum(cbt),ucdata.PD0[t]));		         
	      }
	      lp.addRows(A_b_balance);
	      
	      //System spinning constrains
	      IloRange[] A_b_spin = new IloRange[ucdata.T0]; 
	      IloNumExpr[] css =new IloNumExpr[ucdata.unit];		  	   
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++){
	    		  //css[i]=model.prod(-ucdata.p_up0[i],x[i+t*ucdata.unit]);	    		 
	    		   css[i]=model.prod(ucdata.p_up0[i],x[i+t*ucdata.unit]);
	    	  } 		    			      
//	    	  lp.addRow(model.ge(model.sum(css), (ucdata.PD0[t]+ucdata.spin[t])));
	    	  A_b_spin[t] = model.ge(model.sum(css), (ucdata.PD0[t]+ucdata.spin[t]));
	      }
	      lp.addRows(A_b_spin);
	      
	      //Ramp rate limits constrains
	      if(ramp == 1){
	    	  IloRange[] A_b_ramp = new IloRange[2 * ucdata.T0 * ucdata.unit]; 
		      for(int t=0;t<ucdata.T0;t++){
		    	  for(int i=0;i<ucdata.unit;i++)
		    	  { 		    			   	 
		    		 if(t==0){
		    			 A_b_ramp[ t*ucdata.unit + i] = model.le(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod((ucdata.p_up0[i]-ucdata.Pstart[i]),x[i+t*ucdata.unit])),ucdata.p_up0[i]+ucdata.p_initial[i]+ucdata.u0[i]*(ucdata.RU0[i]-ucdata.Pstart[i]));
		    			 A_b_ramp[ ucdata.T0 * ucdata.unit + t*ucdata.unit + i] = model.le(model.sum(model.prod(ucdata.Pshut[i]-ucdata.RD0[i],x[i+t*ucdata.unit]),model.prod(-1.0,x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0])),-ucdata.p_initial[i]+(ucdata.Pshut[i]-ucdata.p_up0[i])*ucdata.u0[i]+ucdata.p_up0[i]);		 
//		    		  lp.addRow(model.le(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod((ucdata.p_up0[i]-ucdata.Pstart[i]),x[i+t*ucdata.unit])),ucdata.p_up0[i]+ucdata.p_initial[i]+ucdata.u0[i]*(ucdata.RU0[i]-ucdata.Pstart[i])));		    	  
//		    		  lp.addRow(model.le(model.sum(model.prod(ucdata.Pshut[i]-ucdata.RD0[i],x[i+t*ucdata.unit]),model.prod(-1.0,x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0])),-ucdata.p_initial[i]+(ucdata.Pshut[i]-ucdata.p_up0[i])*ucdata.u0[i]+ucdata.p_up0[i]));
		    		 }else{
		    			 A_b_ramp[ t*ucdata.unit + i] = model.le(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0]), model.prod((ucdata.Pstart[i]-ucdata.RU0[i]),x[i+(t-1)*ucdata.unit]), model.prod(-ucdata.Pstart[i]+ucdata.p_up0[i],x[i+t*ucdata.unit])),ucdata.p_up0[i]);
		    			 A_b_ramp[ ucdata.T0 * ucdata.unit + t*ucdata.unit + i] = model.le(model.sum(x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0],model.prod(-1.0,x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0]), model.prod(ucdata.Pshut[i]-ucdata.RD0[i],x[i+(t)*ucdata.unit]), model.prod(-ucdata.Pshut[i]+ucdata.p_up0[i],x[i+(t-1)*ucdata.unit])),ucdata.p_up0[i]);
//		    		  lp.addRow(model.le(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0]), model.prod((ucdata.Pstart[i]-ucdata.RU0[i]),x[i+(t-1)*ucdata.unit]), model.prod(-ucdata.Pstart[i]+ucdata.p_up0[i],x[i+t*ucdata.unit])),ucdata.p_up0[i]));
//		    		  lp.addRow(model.le(model.sum(x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0],model.prod(-1.0,x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0]), model.prod(ucdata.Pshut[i]-ucdata.RD0[i],x[i+(t)*ucdata.unit]), model.prod(-ucdata.Pshut[i]+ucdata.p_up0[i],x[i+(t-1)*ucdata.unit])),ucdata.p_up0[i]));
		 		    	
		    		 }	
		    	  }
		      }
		      lp.addRows(A_b_ramp);  
	      }
	      
	    //Minimum on/off time limits constrains
	      double[] U_i = new double[ucdata.unit];
	      double[] L_i = new double[ucdata.unit];
	      for(int i=0;i<ucdata.unit;i++){
	    	 double temp1= ucdata.u0[i]*(ucdata.time_min_on0[i]-ucdata.time_on_off_ini0[i]);
	    	 double temp2= (1-ucdata.u0[i])*(ucdata.time_min_off0[i]+ucdata.time_on_off_ini0[i]); 
	    	 if(ucdata.T0>temp1){
	    		 if(temp1>0){
	    			 U_i[i]=temp1;
	    		 }else{
	    			 U_i[i]=0.0;
	    		 }	    		 
	    	 }else{
	    		 if(ucdata.T0>0){
	    			 U_i[i]=ucdata.T0;
	    		 }else{
	    			 U_i[i]=0.0;
	    		 }	
	    	 }
	    	 if(ucdata.T0>temp2){
	    		 if(temp2>0){
	    			 L_i[i]=temp2;
	    		 }else{
	    			 L_i[i]=0.0;
	    		 }	    		 
	    	 }else{
	    		 if(ucdata.T0>0){
	    			 L_i[i]=ucdata.T0;
	    		 }else{
	    			 L_i[i]=0.0;
	    		 }	
	    	 }
	    	 //System.out.println(ucdata.u0[i]+" "+L_i[i]);	
	      }
	      //U(i,t)=U(i,0)
	      int tmp = 0;
	      for(int i=0;i<ucdata.unit;i++) { tmp = tmp + (int)(U_i[i]+L_i[i]);}
	      IloRange[] A_b_u0 = new IloRange[tmp]; 
	      int k= 0;
	      for (int i=0;i<ucdata.unit;i++){
	    	  double temp=U_i[i]+L_i[i];
	    	        for(int t=0;t<temp;t++){ 
	    	        	A_b_u0[k] = model.eq(x[i+t*(ucdata.unit)], ucdata.u0[i]);  k++;
//	    	        	lp.addRow(model.eq(x[i+t*(ucdata.unit)], ucdata.u0[i]));
                       }			    	        
	      }
	      lp.addRows(A_b_u0);
	
	      LinkedList <IloRange> A_b_min_on_off_list = new LinkedList ();
	      for(int i=0;i<ucdata.unit;i++){
	    	  for(int t=0;t<ucdata.T0;t++){
	    		  if(t==0)
	    		  {
	    			  //Minimum on time limits constrains
		    		  int tt_on= (int)Math.min(t+1+ucdata.time_min_on0[i]-1, ucdata.T0);
		    		  for(int tt=t+1;tt<tt_on;tt++){
		    			  A_b_min_on_off_list.add(model.le(model.sum(x[i+t*ucdata.unit],model.prod(-1.0,x[i+tt*ucdata.unit])),ucdata.u0[i]));
//		    			  lp.addRow(model.le(model.sum(x[i+t*ucdata.unit],model.prod(-1.0,x[i+tt*ucdata.unit])),ucdata.u0[i]));
		    		  }
		    		  //Minimum off time limits constrains
		    		  int tt_off=(int)Math.min(t+1+ucdata.time_min_off0[i]-1, ucdata.T0);
		    		  for(int tt=t+1;tt<tt_off;tt++){
		    			  A_b_min_on_off_list.add(model.le(model.sum(model.prod(-1.0,x[i+t*ucdata.unit]), model.prod(1.0,x[i+tt*ucdata.unit])),1.0-ucdata.u0[i]));
//		    			  lp.addRow(model.le(model.sum(model.prod(-1.0,x[i+t*ucdata.unit]), model.prod(1.0,x[i+tt*ucdata.unit])),1.0-ucdata.u0[i]));
		    		  }
	    		  }else{
	    		  //Minimum on time limits constrains
	    		  int tt_on= (int)Math.min(t+1+ucdata.time_min_on0[i]-1, ucdata.T0);
	    		  for(int tt=t+1;tt<tt_on;tt++){
	    			  A_b_min_on_off_list.add(model.le(model.sum(x[i+t*ucdata.unit], model.prod(-1.0,x[i+(t-1)*ucdata.unit]), model.prod(-1.0,x[i+tt*ucdata.unit])),0.0));
//	    			  lp.addRow(model.le(model.sum(x[i+t*ucdata.unit], model.prod(-1.0,x[i+(t-1)*ucdata.unit]), model.prod(-1.0,x[i+tt*ucdata.unit])),0.0));
	    		  }
	    		  //Minimum off time limits constrains
	    		  int tt_off=(int)Math.min(t+1+ucdata.time_min_off0[i]-1, ucdata.T0);
	    		  for(int tt=t+1;tt<tt_off;tt++){
	    			  A_b_min_on_off_list.add(model.le(model.sum(x[i+(t-1)*ucdata.unit], model.prod(-1.0,x[i+t*ucdata.unit]), model.prod(1.0,x[i+tt*ucdata.unit])),1.0));
//	    			  lp.addRow(model.le(model.sum(x[i+(t-1)*ucdata.unit], model.prod(-1.0,x[i+t*ucdata.unit]), model.prod(1.0,x[i+tt*ucdata.unit])),1.0));
	    		  }
	    		  }
	    	  }
	      }
	      lp.addRows(A_b_min_on_off_list.toArray(new IloRange[0]));
	      
	      /*for(int i=0;i<ucdata.unit;i++){
	    	  for(int t=1;t<ucdata.T0;t++){
	    		  //Minimum on time limits constrains
	    		  int tt_on=ucdata.T0;
	    		  int tt_off=ucdata.T0;
	    		  if(Math.min(t+ucdata.time_min_on0[i]-1, ucdata.T0)<ucdata.T0){tt_on=(int)Math.min(t+ucdata.time_min_on0[i]-1, ucdata.T0)+1;}
	    		 
	    		  for(int tt=t+1;tt<tt_on;tt++){
	    			  lp.addRow(model.le(model.sum(x[i+t*ucdata.unit], model.prod(-1.0,x[i+(t-1)*ucdata.unit]), model.prod(-1.0,x[i+tt*ucdata.unit])),0.0));
	    		  }
	    		  //Minimum off time limits constrains
	    		  if(Math.min(t+ucdata.time_min_off0[i]-1, ucdata.T0)<ucdata.T0){tt_off=(int)Math.min(t+ucdata.time_min_off0[i]-1, ucdata.T0)+1;}
		    		 
	    		  for(int tt=t+1;tt<tt_off;tt++){
	    			  lp.addRow(model.le(model.sum(x[i+(t-1)*ucdata.unit], model.prod(-1.0,x[i+t*ucdata.unit]), model.prod(1.0,x[i+tt*ucdata.unit])),1.0));
	    		  }
	    	  }
	      }*/
	      
	      
	      //Startup hot costs constraints
         double[]   Ndi = new double[ucdata.unit];
         int U0_length=Math.abs((int)ucdata.time_on_off_ini0[0]); //8
         double[] Init_on_off =new double[ucdata.unit];
         for(int i=0;i<ucdata.unit;i++){Init_on_off[i]=Math.abs(ucdata.time_on_off_ini0[i]);U0_length=Math.max(U0_length,Math.abs((int)ucdata.time_on_off_ini0[i]));}
         double[][] Init_U0 =new double[ucdata.unit][U0_length];
         for(int i=0;i<ucdata.unit;i++){
       		  if(ucdata.time_on_off_ini0[i]>0){
       			  for(int j=0;j<ucdata.time_on_off_ini0[i];j++){
       				  Init_U0[i][j]=1.0;
       			  }
       			  for(int j=(int)ucdata.time_on_off_ini0[i];j<U0_length;j++){
       				  Init_U0[i][j]=0.0;
       			  }
       		  }else{
       			  for(int j=0;j<Math.abs(ucdata.time_on_off_ini0[i]);j++){
       				  Init_U0[i][j]=0.0;
       			  }
       			  for(int j=Math.abs((int)ucdata.time_on_off_ini0[i]);j<U0_length;j++){
       				  Init_U0[i][j]=1.0;
       			  }
       		  }
         }
         /*for(int i=0;i<10;i++){
       	  for(int j=0;j<8;j++)
       	  {
       		  System.out.print(Init_U0[i][j]+" ");
       	  }
       	  System.out.print("| "+Init_on_off[i]);
       	  System.out.println();
         }*/
         for(int i=0;i<ucdata.unit;i++){Ndi[i]=ucdata.time_min_off0[i]+ucdata.Cold_hour0[i]+1;}
         
         
         LinkedList <IloRange> A_b_startCost_list = new LinkedList ();
         
        for(int  i=0;i<ucdata.unit;i++){
       	  for(int t=0;t<ucdata.T0;t++){
       		  for(int c=0, taonum=0; taonum<2; c=(int)Ndi[i]-1,taonum++){
     		  //for(int c=0;c<Ndi[i];c++){
       			  if(c<Ndi[i]-1){     //hot cost
       				 
       				  double temp_u=0.0;
       				  if(t-c<1){      //think init
       					  //if(ucdata.u0[i]>0)          //Init start
       					 // {
       					  IloNumExpr[] csu =new IloNumExpr[t]; //wrong
       					   for(int tt=t-1,j=0;j<=c;j++,tt--){
       						  if(tt<0){
       							  if((Math.abs(tt)-1<U0_length)){
       							    temp_u =temp_u+Init_U0[i][Math.abs(tt)-1];
       							  }
       						  }else{
       							  csu[j]=x[i+tt*ucdata.unit];
       						  }
       					  }
       					  //}else{}
       					A_b_startCost_list.add(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Hot_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Hot_cost0[i],model.sum(csu))),-temp_u*ucdata.Hot_cost0[i]));
//       					  lp.addRow(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Hot_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Hot_cost0[i],model.sum(csu))),-temp_u*ucdata.Hot_cost0[i]));
       				  }else{  
       					  // no think init
       			     IloNumExpr[] csu =new IloNumExpr[c+1]; //wrong
       				for(int tt=t-1,jj=0;(tt>=0)&&(tt>=(t-c-1));tt--,jj++){
       							  csu[jj]=x[i+tt*ucdata.unit];
       					  }
       				A_b_startCost_list.add(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Hot_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Hot_cost0[i],model.sum(csu))),-temp_u*ucdata.Hot_cost0[i]));
//       					  lp.addRow(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Hot_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Hot_cost0[i],model.sum(csu))),-temp_u*ucdata.Hot_cost0[i]));
       				
       				  }
       			  }else{              //cold cost
       				  
       				  double temp_u=0.0;
       				  if(t-c<1){      //think init
       					  IloNumExpr[] csu =new IloNumExpr[t]; //wrong
       					  for(int tt=t-1,j=0;j<=c;j++,tt--){
       						  if(tt<0){
       							  if((Math.abs(tt)-1<U0_length)){
       							    temp_u =temp_u+Init_U0[i][Math.abs(tt)-1];
       							  
       							  }
       						  }else{
       							  csu[j]=x[i+tt*ucdata.unit];
       						  }
       					  }
       					A_b_startCost_list.add(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Cold_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Cold_cost0[i],model.sum(csu))),-temp_u*ucdata.Cold_cost0[i]));
//       					  lp.addRow(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Cold_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Cold_cost0[i],model.sum(csu))),-temp_u*ucdata.Cold_cost0[i]));
       				  }else{  
       					           // no think init
       					  IloNumExpr[] csu =new IloNumExpr[c+1]; //wrong
       				     for(int tt=t-1,jj=0;(tt>=(t-c-1));tt--,jj++){
       							  csu[jj]=x[i+tt*ucdata.unit];
       					  }
       				  A_b_startCost_list.add(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Cold_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Cold_cost0[i],model.sum(csu))),-temp_u*ucdata.Cold_cost0[i]));
//       					  lp.addRow(model.ge(model.sum(x[i+t*ucdata.unit+2*ucdata.T0*ucdata.unit],model.prod(-ucdata.Cold_cost0[i],x[i+t*ucdata.unit]),model.prod(ucdata.Cold_cost0[i],model.sum(csu))),-temp_u*ucdata.Cold_cost0[i]));
       				
       				  }
       			  }
       		  }
       	  }
         }
        lp.addRows(A_b_startCost_list.toArray(new IloRange[0]));


        IloLQNumExpr objExpr = model.lqNumExpr();
        
		   if(L>=0){
			   IloRange[] A_b_pcf_linCon = new IloRange[(L+1) * ucdata.unit * ucdata.T0];
			   
			   //objective perspective constraints
			   double[][] Pil =new double[ucdata.unit][L+1];
			   for(int i=0;i<ucdata.unit;i++)
				   for(int l=0;l<=L;l++){
					   if(L ==0) Pil[i][l] = ucdata.p_low0[i];
					   else	Pil[i][l]=ucdata.p_low0[i]+l*(ucdata.p_up0[i]-ucdata.p_low0[i])/L;
				   }
			   int kk=0;
			   for(int i=0;i<ucdata.unit;i++){
				   for(int t=0;t<ucdata.T0;t++){
					   for(int l=0;l<=L;l++){
						   A_b_pcf_linCon[kk] = model.le(model.sum(model.prod((2*ucdata.gamma0[i]*Pil[i][l]+ucdata.beta0[i]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]), model.prod((ucdata.alpha0[i]-ucdata.gamma0[i]*Pil[i][l]*Pil[i][l]),x[i+t*ucdata.unit]),model.prod(-1.0,x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0])),0.0);
						   kk++;
//						   lp.addRow(model.le(model.sum(model.prod((2*ucdata.gamma0[i]*Pil[i][l]+ucdata.beta0[i]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]), model.prod((ucdata.alpha0[i]-ucdata.gamma0[i]*Pil[i][l]*Pil[i][l]),x[i+t*ucdata.unit]),model.prod(-1.0,x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0])),0.0));
					         
					   }
					 }
				   }
			   for(int i=0;i<ucdata.unit;i++){
			    	  //System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]); 
			    	  for(int t=0;t<ucdata.T0;t++){	    				    		  
			    		   objExpr.addTerm(1.0, x[i+t*ucdata.unit+3*ucdata.T0*ucdata.unit]);
			    		   objExpr.addTerm( 1.0, x[2*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
			   
			    	  }  
			      }
			   lp.addRows(A_b_pcf_linCon);
		   }else{
			   for(int i=0;i<ucdata.unit;i++){
			    	  //System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]); 
			    	  for(int t=0;t<ucdata.T0;t++){	    				    		  
			    		  objExpr.addTerm(ucdata.alpha0[i], x[i+t*ucdata.unit]);
			    		   objExpr.addTerm( ucdata.beta0[i], x[ucdata.T0*ucdata.unit+i+t*ucdata.unit]);		   		   
			    		   objExpr.addTerm( ucdata.gamma0[i], x[ucdata.T0*ucdata.unit+i+t*ucdata.unit],x[ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
			    		   objExpr.addTerm( 1.0, x[2*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
			    	  }  
			      }
		   }
	     

	      model.add(model.minimize(objExpr));		  
	      return (lp);
   }
	 //2_bin_uc   variables: u p_wan s c z(perspective cut var)   
	 IloLPMatrix bin_2_populateByRow(int L) throws IloException {
	      IloLPMatrix lp = model.addLPMatrix();
	      double[]    lb = new double[5*ucdata.unit*ucdata.T0];
	      double[]    ub = new double[5*ucdata.unit*ucdata.T0];
	      IloNumVarType[] xt =  new IloNumVarType[5*ucdata.unit*ucdata.T0];
	      
	      for (int i=0;i<5*ucdata.unit*ucdata.T0;i++){
	    	  if ((i<ucdata.unit*ucdata.T0)||((i>=2*ucdata.unit*ucdata.T0)&&(i<3*ucdata.unit*ucdata.T0)))
	    	  {
	    		  lb[i]=0.0; ub[i]=1.0;xt[i]=IloNumVarType.Bool; 
	    		  if(CR == 1) xt[i] = IloNumVarType.Float;
	    		  
	    		  // this sentence let s_it to be continuous variables. If commented it out, then s_it is integer.
	    		  if(s_it_CR == 1){
		    		  if (((i>=2*ucdata.unit*ucdata.T0)&&(i<3*ucdata.unit*ucdata.T0))) xt[i] = IloNumVarType.Float;  
	    		  }
		  
	    	  }else if((i>=1*ucdata.unit*ucdata.T0)&&(i<2*ucdata.unit*ucdata.T0)){
	    		  lb[i]=0.0; ub[i]=1.0;xt[i]=IloNumVarType.Float;
	    	  }else{
	    		  lb[i]=0.0; ub[i]=Double.MAX_VALUE;xt[i]=IloNumVarType.Float;
	    	  }   		  
	      }
	      
	      IloNumVar[] x  = model.numVarArray(model.columnArray(lp, 5*ucdata.unit*ucdata.T0), lb, ub,xt);

	      System.out.print("\n");
	     // for(int i=0;i<10;i++)System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]+" "+ucdata.RU0[i]+" "+ucdata.RD0[i]+" "+ucdata.Pstart[i]+" "+ucdata.Pshut[i]+" "+ucdata.p_initial[i]);
	      
	      // unit generation limits 
	      LinkedList <IloRange> A_b_low_up_con_list = new LinkedList ();
	      for(int t=0;t<ucdata.T0;t++){
	    	for(int i=0;i<ucdata.unit;i++)
	    	{ 		
	    		A_b_low_up_con_list.add(model.le(model.sum(x[i+t*ucdata.unit+ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+t*ucdata.unit])),0.0));
//	    	  lp.addRow(model.le(model.sum(x[i+t*ucdata.unit+ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+t*ucdata.unit])),0.0));
	           }
	      }
	      lp.addRows(A_b_low_up_con_list.toArray(new IloRange[0]));
	      
	      //Power balance constrains
	      LinkedList <IloRange> A_b_balance_list = new LinkedList ();
	      IloNumExpr[] cbt =new IloNumExpr[ucdata.unit];		  	   
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++){
	    		   //cbt[i]=x[i+t*ucdata.unit+ucdata.unit*ucdata.T0];	
	    		   cbt[i]=model.sum(model.prod((ucdata.p_up0[i]-ucdata.p_low0[i]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0] ),model.prod(ucdata.p_low0[i], x[i+t*ucdata.unit]));
	    		   
	    	  } 		    	
	    	  A_b_balance_list.add(model.eq(model.sum(cbt),ucdata.PD0[t]));	 
	      }
	      lp.addRows(A_b_balance_list.toArray(new IloRange[0]));
	      
	      //System spinning constrains
	      LinkedList <IloRange> A_b_spin_list = new LinkedList ();
	      IloNumExpr[] css =new IloNumExpr[ucdata.unit];		  	   
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++){
	    		   //css[i]=model.prod(-ucdata.p_up0[i],x[i+t*ucdata.unit]);	    		  
	    		   css[i]=model.prod(ucdata.p_up0[i],x[i+t*ucdata.unit]);
	    		  
	    	  } 		    			      
	         //lp.addRow(model.le(model.sum(css), -(ucdata.PD0[t]+ucdata.spin[t])));
	    	  A_b_spin_list.add(model.ge(model.sum(css), (ucdata.PD0[t]+ucdata.spin[t])));		    	 
	      }
	      lp.addRows(A_b_spin_list.toArray(new IloRange[0]));
	      
	    //Ramp rate limits constrains
	      if(ramp == 1){
	    	  LinkedList <IloRange> A_b_ramp_list = new LinkedList ();
		      for(int i=0;i<ucdata.unit;i++) {
		          for(int t=0;t<ucdata.T0;t++)
		          {
		    	  	 if(t==0){
		    	  		A_b_ramp_list.add(model.ge(model.sum(model.prod(ucdata.RU0[i], x[i+t*ucdata.unit]),model.prod(ucdata.Pstart[i]-ucdata.RU0[i], x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0]),model.prod(-1.0, x[i+t*ucdata.unit+ucdata.unit*ucdata.T0])), -1.0*ucdata.p_initial[i]));
		    		     
		    	  		A_b_ramp_list.add(model.ge(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0],model.prod(ucdata.Pshut[i]-ucdata.RD0[i], x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0]),model.prod(-ucdata.Pshut[i]+ucdata.RD0[i], x[i+t*ucdata.unit])), ucdata.p_initial[i]-ucdata.u0[i]*ucdata.Pshut[i]));
		    		 }else{
		    			 A_b_ramp_list.add(model.ge(model.sum(model.prod(ucdata.RU0[i], x[i+t*ucdata.unit]),model.prod(ucdata.Pstart[i]-ucdata.RU0[i], x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0]),x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0],model.prod(-1.0, x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0])), 0.0));
		    			 A_b_ramp_list.add(model.ge(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0],model.prod(-1.0, x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0]),model.prod(ucdata.Pshut[i], x[i+(t-1)*ucdata.unit]),model.prod(ucdata.Pshut[i]-ucdata.RD0[i], x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0]),model.prod(-ucdata.Pshut[i]+ucdata.RD0[i], x[i+t*ucdata.unit])), 0.0));
				  
		    		 }	
		    	  }
		      }
		      lp.addRows(A_b_ramp_list.toArray(new IloRange[0]));
	      }
	      
	      LinkedList <IloRange> A_b_all_others = new LinkedList ();
	    //Minimum on/off time limits constrains
	     double[] U_i = new double[ucdata.unit];
	      double[] L_i = new double[ucdata.unit];
	      for(int i=0;i<ucdata.unit;i++){
	    	 double temp1= ucdata.u0[i]*(ucdata.time_min_on0[i]-ucdata.time_on_off_ini0[i]);
	    	 double temp2= (1-ucdata.u0[i])*(ucdata.time_min_off0[i]+ucdata.time_on_off_ini0[i]); 
	    	 if(ucdata.T0>temp1){
	    		 if(temp1>0){
	    			 U_i[i]=temp1;
	    		 }else{
	    			 U_i[i]=0.0;
	    		 }	    		 
	    	 }else{
	    		 if(ucdata.T0>0){
	    			 U_i[i]=ucdata.T0;
	    		 }else{
	    			 U_i[i]=0.0;
	    		 }	
	    	 }
	    	 if(ucdata.T0>temp2){
	    		 if(temp2>0){
	    			 L_i[i]=temp2;
	    		 }else{
	    			 L_i[i]=0.0;
	    		 }	    		 
	    	 }else{
	    		 if(ucdata.T0>0){
	    			 L_i[i]=ucdata.T0;
	    		 }else{
	    			 L_i[i]=0.0;
	    		 }	
	    	 }
	    	 //System.out.println(ucdata.u0[i]+" "+L_i[i]+" "+U_i[i]);	
	      }
	      //U(i,t)=U(i,0)
	      for (int i=0;i<ucdata.unit;i++){
	    	  double temp=U_i[i]+L_i[i];
	    	        for(int t=0;t<temp;t++){ 
	    	        	A_b_all_others.add(model.eq(x[i+t*(ucdata.unit)], ucdata.u0[i]));
                       }			    	        
	      }
	      //U(i,t)-U(i,t-1)<=S(i,t)
	      for(int i=0;i<ucdata.unit;i++){
	    	  for(int t=0;t<ucdata.T0;t++){
	    		  if(t==0){
	    			  A_b_all_others.add(model.le(model.sum(x[i+t*ucdata.unit],model.prod(-1.0, x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0])), ucdata.u0[i]));
		    		  
	    		  }else{
	    			  A_b_all_others.add(model.le(model.sum(x[i+t*ucdata.unit],model.prod(-1.0, x[i+(t-1)*ucdata.unit]),model.prod(-1.0, x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0])), 0.0));
	    		  }
	    	  }
	      }
	      
	      
	    //Minimum on time limits
	    for(int i=0;i<ucdata.unit;i++){
	    	  double [] all_t= new double[((int)ucdata.T0-(int)U_i[i])]; 
	    	  double [] ini_w= new double[all_t.length];
	    	  for(int ii=0;ii<all_t.length;ii++){
	    		  all_t[ii]=U_i[i]+ii+1;//System.out.println(all_t[ii]);
	    		  double temp_w = all_t[ii]-ucdata.time_min_on0[i]+1;
	    		  if(temp_w>1.0){ 
	    			  ini_w[ii]=temp_w;
	    			  }else{
	    			  ini_w[ii]=1.0; 
	    				  }
	    		  //System.out.print(ini_w[ii]+" ");
	    		  }
	    	  //System.out.println("\n--------------------");
	    	  for(int t=(int)U_i[i],w_t0=0;t<ucdata.T0;w_t0++,t++){
	    		 // System.out.println("------------"+t);		    		  	    		 
	    		  //System.out.println(t+3-(int)ini_w[w_t0]+" ");
	    		 // System.out.println("");	
	    		  double[]   val = new double[t+3-(int)ini_w[w_t0]];
	    		  int[]      ind = new int[t+3-(int)ini_w[w_t0]];
	    		  int ttt= (int)ini_w[w_t0]-1;
	    		  for(int tt=(int)ini_w[w_t0]-1,iii=0;tt<t+1;tt++,iii++){
	    			 val[iii]=1.0;
	    			 ind[iii]=i+2*ucdata.T0*ucdata.unit+(iii+ttt)*ucdata.unit;		    		 
	    		  }
	    		  val[t+2-(int)ini_w[w_t0]]=-1.0;
	    		  ind[t+2-(int)ini_w[w_t0]]=i+t*ucdata.unit;
	    		  
	    		  
//	    		  IloNumExpr[] tmp =new IloNumExpr[ind.length];		  
//	    		  for(int kk =0;kk<ind.length;kk++){
//	    			  cbt[kk]=model.prod(val[kk], x[ind[kk]]);		
//	    		  }
//	    		  A_b_all_others.add(model.ge(0, model.sum(cbt)));
	    		  	    		  
	    		  lp.addRow(-Double.MAX_VALUE, 0.0, ind, val);  	  
	    	  }
	    	  //System.out.println("\n*********************"); 
	      }
	    

	    
	    //Minimum off time limits
	      for(int i=0;i<ucdata.unit;i++){
	    	  double [] all_t= new double[((int)ucdata.T0-(int)L_i[i])]; 
	    	  double [] ini_w= new double[all_t.length];
	    	  
	    	  for(int ii=0;ii<all_t.length;ii++){
	    		  all_t[ii]=L_i[i]+ii+1;//System.out.println(all_t[ii]);
	    		  double temp_w = all_t[ii]-ucdata.time_min_off0[i]+1;
	    		  if(temp_w>1.0){ 
	    			  ini_w[ii]=temp_w;
	    			  }else{
	    			  ini_w[ii]=1.0; 
	    				  }
	    		  //System.out.print(ini_w[ii]+" ");
	    		  }
	    	  
	    	  for(int t=(int)L_i[i],w_t0=0;t<ucdata.T0;t++,w_t0++){		    		  	    		  	    		 
	    		 	    		  
	    		  double[]   val = new double[t+3-(int)ini_w[w_t0]];
	    		  int[]      ind = new int[t+3-(int)ini_w[w_t0]];
	    		  int ttt= (int)ini_w[w_t0]-1;
	    		  for(int tt=(int)ini_w[w_t0]-1,iii=0;tt<t+1;tt++,iii++){
	    			 val[iii]=1.0;
	    			 ind[iii]=i+2*ucdata.T0*ucdata.unit+(iii+ttt)*ucdata.unit;
	    			 //System.out.println(i+tt+2*ucdata.T0*ucdata.unit+iii*ucdata.unit+" "+iii+" "+tt+" ");
		    		 
	    		  }
	    		  double lt=Math.max(0, t+1-ucdata.time_min_off0[i]);
	    		  if(lt==0){
	    			  val[t+2-(int)ini_w[w_t0]]=0.0;
	    			  ind[t+2-(int)ini_w[w_t0]]=i+(int)lt*ucdata.unit;	
	    			  lp.addRow(-Double.MAX_VALUE, (1.0-ucdata.u0[i]), ind, val);  
	    		  }else{
	    			  val[t+2-(int)ini_w[w_t0]]=1.0;
	    			  ind[t+2-(int)ini_w[w_t0]]=i+(int)(lt-1)*ucdata.unit;	
	    			  lp.addRow(-Double.MAX_VALUE, 1.0, ind, val);  
	    		  }
	    		  
	    		
	    		  	    		  
	    		  
	    	  }
	    	 
	      }
	      
	      //Startup cost constraints
	   for(int i=0;i<ucdata.unit;i++){
	    	  for(int t=0;t<ucdata.T0;t++){
	    		  double c=Math.max(1, (t-ucdata.time_min_off0[i]-ucdata.Cold_hour0[i]));
	    		  double[] Scval = new double[t-(int)c+3];
	              int[]    Scind = new int[t-(int)c+3];
	              Scval[0]=-1.0;
	              Scind[0]=3*ucdata.T0*ucdata.unit+i+t*ucdata.unit;
	              
	              Scval[1]=ucdata.Cold_cost0[i]-ucdata.Hot_cost0[i]; 
	              Scind[1]=2*ucdata.T0*ucdata.unit+i+t*ucdata.unit;
	             double finit=0.0;
	             if(((t-ucdata.time_min_off0[i]-ucdata.Cold_hour0[i])<=0)&&(Math.max(0, -ucdata.time_on_off_ini0[i])<(Math.abs(t-ucdata.time_min_off0[i]-ucdata.Cold_hour0[i])+1))){
	            	 finit=ucdata.Cold_cost0[i]-ucdata.Hot_cost0[i];
	             }else {
	            	 finit=0.0;
	             }
	              for(int tt=2,cc=(int)c-1;cc<t;tt++,cc++){
	            	  Scval[tt]=ucdata.Hot_cost0[i]-ucdata.Cold_cost0[i];
	            	  Scind[tt]=i+cc*ucdata.unit;
	              }
	              lp.addRow(-Double.MAX_VALUE, finit, Scind, Scval);
	    	  }
	      }
	   
	   IloLQNumExpr objExpr = model.lqNumExpr();
	   
	   if(L>=0){
		   //objective perspective constraints
		   double[][] Pil =new double[ucdata.unit][L+1];
		   for(int i=0;i<ucdata.unit;i++){
			   //System.out.print("\n");
			   for(double l=0;l<=L;l++){
				   if (L == 0) Pil[i][(int)l]=0;
				   else	Pil[i][(int)l]=l/L;
				   //System.out.print(Pil[i][(int)l]+" ");
			   }
		   }
		   for(int i=0;i<ucdata.unit;i++){
			   for(int t=0;t<ucdata.T0;t++){
				   for(int l=0;l<=L;l++){
					   
					   A_b_all_others.add(model.le(model.sum(model.prod((2*ucdata.gamma0[i]*Pil[i][l]+ucdata.beta0[i]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]), model.prod((ucdata.alpha0[i]-ucdata.gamma0[i]*Pil[i][l]*Pil[i][l]),x[i+t*ucdata.unit]),model.prod(-1.0,x[i+t*ucdata.unit+4*ucdata.unit*ucdata.T0])),0.0));
				         
				   }
				 }
			   }
		   for(int i=0;i<ucdata.unit;i++){
		    	  //System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]); 
		    	  for(int t=0;t<ucdata.T0;t++){	    				    		  
		    		   objExpr.addTerm(1.0, x[i+t*ucdata.unit+4*ucdata.T0*ucdata.unit]);
		    		   objExpr.addTerm( ucdata.Hot_cost0[i], x[2*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);	
		    		   objExpr.addTerm( 1.0, x[3*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
		   
		    	  }  
		      }
		   
	   }else{
		   //No objective perspective constraints
		   for(int i=0;i<ucdata.unit;i++){
		    	  //System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]); 
		    	  for(int t=0;t<ucdata.T0;t++){	    				    		  
		    		   objExpr.addTerm(ucdata.alpha0[i], x[i+t*ucdata.unit]);
		    		   objExpr.addTerm( ucdata.beta0[i], x[ucdata.T0*ucdata.unit+i+t*ucdata.unit]);		   		   
		    		   objExpr.addTerm( ucdata.gamma0[i], x[ucdata.T0*ucdata.unit+i+t*ucdata.unit],x[ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
		    		   objExpr.addTerm( ucdata.Hot_cost0[i], x[2*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);	
		    		   objExpr.addTerm( 1.0, x[3*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
		   
		    	  }  
		      }
	   }
	   
	    lp.addRows(A_b_all_others.toArray(new IloRange[0]));
	
	      model.add(model.minimize(objExpr));		  
	      return (lp);
}
	//3_bin_uc
	 IloLPMatrix bin_3_populateByRow(int L) throws IloException {
	      IloLPMatrix lp = model.addLPMatrix();
	      double[]    lb = new double[6*ucdata.unit*ucdata.T0];
	      double[]    ub = new double[6*ucdata.unit*ucdata.T0];
	      IloNumVarType[] xt =  new IloNumVarType[6*ucdata.unit*ucdata.T0];
	      
	      LinkedList <IloRange> A_b_all_list = new LinkedList ();
	      
	      
	      for (int i=0;i<6*ucdata.unit*ucdata.T0;i++){
	    	  if ((i<ucdata.unit*ucdata.T0)||((i>=2*ucdata.unit*ucdata.T0)&&(i<3*ucdata.unit*ucdata.T0))||((i>=3*ucdata.unit*ucdata.T0)&&(i<4*ucdata.unit*ucdata.T0)))
	    	  {
	    		  lb[i]=0.0; ub[i]=1.0;xt[i]=IloNumVarType.Bool; 
	    		  if(CR == 1) xt[i] = IloNumVarType.Float;
	    		  
	    		  // this sentence let s_it to be continuous variables. If commented it out, then s_it is integer.
	    		  if(s_it_CR == 1){
		    		  if (((i>=2*ucdata.unit*ucdata.T0)&&(i<4*ucdata.unit*ucdata.T0))) xt[i] = IloNumVarType.Float;  
	    		  }
	    		  
	    		  
	    	  }else{
	    		  lb[i]=0.0; ub[i]=Double.MAX_VALUE;xt[i]=IloNumVarType.Float;
	    	  }   		  
	      }
	      
	      IloNumVar[] x  = model.numVarArray(model.columnArray(lp, 6*ucdata.unit*ucdata.T0), lb, ub,xt);
	      
	   
	      //Power balance constrains
	      IloNumExpr[] cbt =new IloNumExpr[ucdata.unit];		  	   
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++){
	    		   cbt[i]=x[i+t*ucdata.unit+ucdata.unit*ucdata.T0];		    		  
	    		   //System.out.println("AAAAAAAAAAAAAA");
	    	  } 		    	
	    	  A_b_all_list.add(model.eq(model.sum(cbt),ucdata.PD0[t]));	
	         
	      }
	      
	      //Status variables constraints
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++)
	    	  { 		    			   	 
	    		 if(t==0){
	    			 System.out.print(ucdata.u0[i]+" ");
	    			 A_b_all_list.add(model.eq(model.sum(x[i+t*ucdata.unit],x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0])),ucdata.u0[i]));
	    		 }else{
	    			 A_b_all_list.add(model.eq(model.sum(x[i+t*ucdata.unit],x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+(t-1)*ucdata.unit]), model.prod(-1.0,x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0])),0.0));
	    		 }
	    		 //lp.addRow(model.le( model.sum(x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0],x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0]),1.0));
	    	  }
	      }
	      
	      //System spinning constrains
	      IloNumExpr[] css =new IloNumExpr[ucdata.unit];		  	   
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++){
	    		   //css[i]=model.prod(-ucdata.p_up0[i],x[i+t*ucdata.unit]);	    		  
	    		   css[i]=model.prod(ucdata.p_up0[i],x[i+t*ucdata.unit]);
	    		  
	    	  } 		    			      
	         //lp.addRow(model.le(model.sum(css), -(ucdata.PD0[t]+ucdata.spin[t])));
	    	  A_b_all_list.add(model.ge(model.sum(css), (ucdata.PD0[t]+ucdata.spin[t])));		    	 
	      }
	      
	      //Unit generation capacity limits constrains
	      for(int t=0;t<ucdata.T0;t++){
	    	  for(int i=0;i<ucdata.unit;i++)
	    	  { 	
	    		  //lp.addRow(model.le(x[i+t*ucdata.unit+ucdata.unit*ucdata.T0],ucdata.p_up0[i]));
	    	      //lp.addRow(model.ge(x[i+t*ucdata.unit+ucdata.unit*ucdata.T0],ucdata.p_low0[i]));
	    		  A_b_all_list.add(model.le( model.diff(model.prod(ucdata.p_low0[i],x[i+t*ucdata.unit]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]),0.0));
	    		  A_b_all_list.add(model.ge( model.diff(model.prod(ucdata.p_up0[i],x[i+t*ucdata.unit]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]),0.0));
	    	      }
	      }
	   
	      //Ramp rate limits constrains
	     if(ramp == 1){

		     for(int t=0;t<ucdata.T0;t++){
		    	  for(int i=0;i<ucdata.unit;i++)
		    	  { 		    			   	 
		    		 if(t==0){
		    			 A_b_all_list.add(model.le(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod(-(ucdata.RU0[i]+ucdata.p_low0[i]),x[i+t*ucdata.unit]),model.prod(-(ucdata.Pstart[i]-ucdata.RU0[i]-ucdata.p_low0[i]),x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0])),ucdata.p_initial[i]-ucdata.u0[i]*ucdata.p_low0[i]));
		    		  
		    			 A_b_all_list.add(model.le(model.sum(model.prod(-1.0,x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0]),model.prod(-(ucdata.Pshut[i]-ucdata.RD0[i]-ucdata.p_low0[i]),x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0]),model.prod(ucdata.p_low0[i],x[i+t*ucdata.unit])),-ucdata.p_initial[i]+ucdata.u0[i]*(ucdata.p_low0[i]+ucdata.RD0[i])));
		    		 }else{
		    			 A_b_all_list.add(model.le(model.sum(x[i+t*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0]), model.prod(-ucdata.RU0[i]-ucdata.p_low0[i],x[i+(t)*ucdata.unit]), model.prod(ucdata.p_low0[i],x[i+(t-1)*ucdata.unit]), model.prod(-(ucdata.Pstart[i]-ucdata.RU0[i]-ucdata.p_low0[i]),x[i+t*ucdata.unit+2*ucdata.unit*ucdata.T0])),0.0));
		    		     
		    			 A_b_all_list.add(model.le(model.sum(x[i+(t-1)*ucdata.unit+1*ucdata.unit*ucdata.T0], model.prod(-1.0,x[i+(t)*ucdata.unit+1*ucdata.unit*ucdata.T0]), model.prod(-ucdata.RD0[i]-ucdata.p_low0[i],x[i+(t-1)*ucdata.unit]), model.prod(ucdata.p_low0[i],x[i+(t)*ucdata.unit]), model.prod(-(ucdata.Pshut[i]-ucdata.RD0[i]-ucdata.p_low0[i]),x[i+t*ucdata.unit+3*ucdata.unit*ucdata.T0])),0.0));
		    			 
		    		     
		    		 }	
		    	  }
		      }
	      
	      }
	    
	      //Minimum on/off time limits constrains
	      double[] U_i = new double[ucdata.unit];
	      double[] L_i = new double[ucdata.unit];
	      for(int i=0;i<ucdata.unit;i++){
	    	 double temp1= ucdata.u0[i]*(ucdata.time_min_on0[i]-ucdata.time_on_off_ini0[i]);
	    	 double temp2= (1-ucdata.u0[i])*(ucdata.time_min_off0[i]+ucdata.time_on_off_ini0[i]); 
	    	 if(ucdata.T0>temp1){
	    		 if(temp1>0){
	    			 U_i[i]=temp1;
	    		 }else{
	    			 U_i[i]=0.0;
	    		 }	    		 
	    	 }else{
	    		 if(ucdata.T0>0){
	    			 U_i[i]=ucdata.T0;
	    		 }else{
	    			 U_i[i]=0.0;
	    		 }	
	    	 }
	    	 if(ucdata.T0>temp2){
	    		 if(temp2>0){
	    			 L_i[i]=temp2;
	    		 }else{
	    			 L_i[i]=0.0;
	    		 }	    		 
	    	 }else{
	    		 if(ucdata.T0>0){
	    			 L_i[i]=ucdata.T0;
	    		 }else{
	    			 L_i[i]=0.0;
	    		 }	
	    	 }
	    	 //System.out.println(ucdata.u0[i]+" "+L_i[i]);	
	      }
	      for (int i=0;i<ucdata.unit;i++){
	    	  double temp=U_i[i]+L_i[i];
	    	        for(int t=0;t<temp;t++){ 
	    	        	A_b_all_list.add(model.eq(x[i+t*(ucdata.unit)], ucdata.u0[i]));
                       }			    	        
	      }
	      //Minimum on time limits
	      for(int i=0;i<ucdata.unit;i++){
	    	  double [] all_t= new double[((int)ucdata.T0-(int)U_i[i])]; 
	    	  double [] ini_w= new double[all_t.length];
	    	  for(int ii=0;ii<all_t.length;ii++){
	    		  all_t[ii]=U_i[i]+ii+1;//System.out.println(all_t[ii]);
	    		  double temp_w = all_t[ii]-ucdata.time_min_on0[i]+1;
	    		  if(temp_w>1.0){ 
	    			  ini_w[ii]=temp_w;
	    			  }else{
	    			  ini_w[ii]=1.0; 
	    				  }
	    		  //System.out.print(ini_w[ii]+" ");
	    		  }
	    	  //System.out.println("\n--------------------");
	    	  for(int t=(int)U_i[i],w_t0=0;t<ucdata.T0;t++,w_t0++){
	    		  //System.out.println("------------"+t);		    		  	    		 
	    		   //System.out.print((int)ini_w[t]-1+" ");
	    		  
	    		  double[]   val = new double[t+3-(int)ini_w[w_t0]];
	    		  int[]      ind = new int[t+3-(int)ini_w[w_t0]];
	    		  int ttt= (int)ini_w[w_t0]-1;
	    		  for(int tt=(int)ini_w[w_t0]-1,iii=0;tt<t+1;tt++,iii++){
	    			 val[iii]=1.0;
	    			 ind[iii]=i+2*ucdata.T0*ucdata.unit+(iii+ttt)*ucdata.unit;		    		 
	    		  }
	    		  val[t+2-(int)ini_w[w_t0]]=-1.0;
	    		  ind[t+2-(int)ini_w[w_t0]]=i+t*ucdata.unit;
	    		  lp.addRow(-Double.MAX_VALUE, 0.0, ind, val);

	    	  
	    	  }
	    	  //System.out.println("\n*********************"); 
	      }
	      //Minimum off time limits
	      for(int i=0;i<ucdata.unit;i++){
	    	  double [] all_t= new double[((int)ucdata.T0-(int)L_i[i])]; 
	    	  double [] ini_w= new double[all_t.length];
	    	  for(int ii=0;ii<all_t.length;ii++){
	    		  all_t[ii]=L_i[i]+ii+1;//System.out.println(all_t[ii]);
	    		  double temp_w = all_t[ii]-ucdata.time_min_off0[i]+1;
	    		  if(temp_w>1.0){ 
	    			  ini_w[ii]=temp_w;
	    			  }else{
	    			  ini_w[ii]=1.0; 
	    				  }
	    		  //System.out.print(ini_w[ii]+" ");
	    		  }
	    	  
	    	  for(int t=(int)L_i[i],w_t0=0;t<ucdata.T0;t++,w_t0++){		    		  	    		  	    		 
	    		  //System.out.print((int)ini_w[t]-1+" ");		    		  
	    		  double[]   val = new double[t+3-(int)ini_w[w_t0]];
	    		  int[]      ind = new int[t+3-(int)ini_w[w_t0]];
	    		  int ttt= (int)ini_w[w_t0]-1;
	    		  for(int tt=(int)ini_w[w_t0]-1,iii=0;tt<t+1;tt++,iii++){
	    			 val[iii]=1.0;
	    			 ind[iii]=i+3*ucdata.T0*ucdata.unit+(iii+ttt)*ucdata.unit;
	    			 //System.out.println(i+tt+2*ucdata.T0*ucdata.unit+iii*ucdata.unit+" "+iii+" "+tt+" ");
		    		 
	    		  }
	    		  val[t+2-(int)ini_w[w_t0]]=1.0;
	    		  ind[t+2-(int)ini_w[w_t0]]=i+t*ucdata.unit;		    		  
	    		  lp.addRow(-Double.MAX_VALUE, 1.0, ind, val);  
	    	  }
	    	  //System.out.println("\n*********************"); 
	      }
	      
	      //Startup hot costs constraints

	      double[]   Slhs = new double[ucdata.T0*ucdata.unit];
	      double[]   Srhs = new double[ucdata.T0*ucdata.unit];
	      double[][] Sval = new double[ucdata.T0*ucdata.unit][2];
         int[][]    Sind = new int[ucdata.T0*ucdata.unit][2];
         int count = 0;
	      for(int t=0;t<ucdata.T0;t++){
	    	  for (int i=0;i<ucdata.unit;i++){
	    		  Slhs[count]=-Double.MAX_VALUE;
	    		  Srhs[count]=0.0;
	    		  Sval[count][0]=-1.0;
	    		  Sval[count][1]=ucdata.Hot_cost0[i];
	    		  Sind[count][0]=4*ucdata.T0*ucdata.unit+i+t*ucdata.unit;
	    		  Sind[count][1]=2*ucdata.T0*ucdata.unit+i+t*ucdata.unit;
	    		  count++;		    		  
	    	  }
	      }
	      lp.addRows(Slhs, Srhs, Sind, Sval);
	      //Startup cold costs constraints
	      double[] t_coldstart = new double[ucdata.unit];
	      for(int i=0;i<ucdata.unit;i++){
	    	  t_coldstart[i]=ucdata.time_min_off0[i]+ucdata.Cold_hour0[i]+1;
	    	  //System.out.print(t_coldstart[i]+" ");
	      }
	  
         // cold start
	      for(int i=0;i<ucdata.unit;i++){
	    	  for(int t=0;t<ucdata.T0;t++){
	    		  double c=Math.max(1, (t+1-ucdata.time_min_off0[i]-ucdata.Cold_hour0[i]));
	    		  double[] Scval = new double[t-(int)c+3];
	              int[]    Scind = new int[t-(int)c+3];
	              Scval[0]=-1.0;
	              Scind[0]=4*ucdata.T0*ucdata.unit+i+t*ucdata.unit;
	              
	              Scval[1]=ucdata.Cold_cost0[i]; 
	              Scind[1]=2*ucdata.T0*ucdata.unit+i+t*ucdata.unit;
	             double finit=0.0;
	             if(((t+1-ucdata.time_min_off0[i]-ucdata.Cold_hour0[i])<=0)&&(Math.max(0, -ucdata.time_on_off_ini0[i])<(Math.abs(t-ucdata.time_min_off0[i]-ucdata.Cold_hour0[i])+1))){
	            	 finit=ucdata.Cold_cost0[i];
	             }else {
	            	 finit=0.0;
	             }
	              for(int tt=2,cc=(int)c-1;cc<t;tt++,cc++){
	            	  Scval[tt]=-ucdata.Cold_cost0[i];
	            	  Scind[tt]=3*ucdata.T0*ucdata.unit+i+cc*ucdata.unit;
	              }
	              
	              lp.addRow(-Double.MAX_VALUE, finit, Scind, Scval);
	    	  }
	    	  
	     }
	      
	      
	      IloLQNumExpr objExpr = model.lqNumExpr();
	      
		   if(L>=0){
			   //objective perspective constraints
			   double[][] Pil =new double[ucdata.unit][L+1];
			   for(int i=0;i<ucdata.unit;i++)
				   for(int l=0;l<=L;l++){
					   if (L ==0) Pil[i][l]=ucdata.p_low0[i];
					   else Pil[i][l]=ucdata.p_low0[i]+l*(ucdata.p_up0[i]-ucdata.p_low0[i])/L;
				   }
			   for(int i=0;i<ucdata.unit;i++){
				   for(int t=0;t<ucdata.T0;t++){
					   for(int l=0;l<=L;l++){
						   A_b_all_list.add(model.le(model.sum(model.prod((2*ucdata.gamma0[i]*Pil[i][l]+ucdata.beta0[i]),x[i+t*ucdata.unit+ucdata.unit*ucdata.T0]), model.prod((ucdata.alpha0[i]-ucdata.gamma0[i]*Pil[i][l]*Pil[i][l]),x[i+t*ucdata.unit]),model.prod(-1.0,x[i+t*ucdata.unit+5*ucdata.unit*ucdata.T0])),0.0));
					         
					   }
					 }
				   }
			   for(int i=0;i<ucdata.unit;i++){
			    	  //System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]); 
			    	  for(int t=0;t<ucdata.T0;t++){	    				    		  
			    		   objExpr.addTerm(1.0, x[i+t*ucdata.unit+5*ucdata.T0*ucdata.unit]);
			    		   objExpr.addTerm( 1.0, x[4*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
			   
			    	  }  
			      }
		   }else{
			   for(int i=0;i<ucdata.unit;i++){
			    	  //System.out.println(ucdata.alpha0[i]+" "+ucdata.beta0[i]+" "+ucdata.gamma0[i]); 
			    	  for(int t=0;t<ucdata.T0;t++){	    				    		  
			    		   objExpr.addTerm(ucdata.alpha0[i], x[i+t*ucdata.unit]);
			    		   objExpr.addTerm( ucdata.beta0[i], x[ucdata.T0*ucdata.unit+i+t*ucdata.unit]);		   		   
			    		   objExpr.addTerm( ucdata.gamma0[i], x[ucdata.T0*ucdata.unit+i+t*ucdata.unit],x[ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
			    		   objExpr.addTerm( 1.0, x[4*ucdata.T0*ucdata.unit+i+t*ucdata.unit]);
			   
			    	  }  
			      }
		   }
	      
		   lp.addRows(A_b_all_list.toArray(new IloRange[0]));

	      model.add(model.minimize(objExpr));		  
	      return (lp);
	   }
	
}
