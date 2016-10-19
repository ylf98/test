// This class read UC data from pathAndFilename file.
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class readdataUC {
	int unit=0,T0=0;
	String pathAndFilename=null;
	double Pstart[],Pshut[],u0[],PD0[],	spin[],	alpha0[],	beta0[],	gamma0[],	p_low0[],	p_up0[],time_on_off_ini0[],time_min_on0[],time_min_off0[],
	       fixedCost4startup0[],RU0[],RD0[],p_initial[],index_tmp[], alpha_e0[], beta_e0[], gamma_e0[], 
	       Cold_hour0[],Cold_cost0[],Hot_cost0[];
	
	readdataUC(String path,int time,int t_time) throws IOException	{
		this.pathAndFilename=path;
		System.out.println(this.pathAndFilename);
		FileInputStream inputStream = null;
		Scanner sc = null;
		try{
		  inputStream=new FileInputStream(path);
		  sc = new Scanner(inputStream, "UTF-8");
		  sc.nextLine(); //忽略第一行
		  sc.next(); T0=t_time*sc.nextInt(); //获得运行时间
		  sc.next(); unit=time*sc.nextInt();  // total number of units
		  for(int i=0;i<=7;i++){ sc.nextLine();} //忽略7行
		  
		  PD0 =new double[T0];spin=new double[T0]; //分配内存
		  alpha0= new double[unit];	beta0=new double[unit];	gamma0= new double[unit];	p_low0= new double[unit];	p_up0= new double[unit];
		  time_on_off_ini0= new double[unit];time_min_on0= new double[unit];time_min_off0= new double[unit];
	      fixedCost4startup0= new double[unit];RU0= new double[unit];RD0= new double[unit];p_initial= new double[unit];
	      index_tmp= new double[unit]; alpha_e0= new double[unit]; beta_e0= new double[unit]; gamma_e0= new double[unit];u0=new double[unit];
	      Pstart = new double[unit];Pshut = new double[unit];
	      Cold_hour0 = new double[unit];
	      
	    		  
	      for(int i=0;i<T0;i=i+t_time){PD0[i]=time*sc.nextDouble();if(t_time>1){for(int j=1;j<t_time;j++){PD0[i+j]=PD0[i];}}} sc.nextLine();sc.nextLine();
	      for(int i=0;i<T0;i=i+t_time){spin[i]=time*sc.nextDouble();if(t_time>1){for(int j=1;j<t_time;j++){spin[i+j]=spin[i];}}} sc.nextLine();sc.nextLine();
	      //String[] u=new String[unit];	      
          for(int i=0;i<unit;i=i+time){
          sc.next();
          gamma0[i]=sc.nextDouble();    beta0[i]=sc.nextDouble();alpha0[i]=sc.nextDouble();		 	
    	  p_low0[i]=sc.nextDouble(); 	p_up0[i]=sc.nextDouble();
    	  time_on_off_ini0[i]=sc.nextDouble();time_min_on0[i]=sc.nextDouble();time_min_off0[i]=sc.nextDouble();
    	  
    	  Pstart[i]= p_low0[i];Pshut[i]=p_low0[i];
    	  
		  sc.next();sc.next();sc.next();sc.next();
		  
	      fixedCost4startup0[i]=sc.nextDouble(); sc.next();p_initial[i]=sc.nextDouble(); 
	      
	      if(pathAndFilename.contains("std")) {  	 Cold_hour0[i]=sc.nextDouble(); 
	      }
	      
	      sc.next();
	      RU0[i]=sc.nextDouble();RD0[i]=sc.nextDouble();
	      //System.out.println(RU0[i]+" "+RD0[i]+"\n");
	      if(time>1){
	    	  for(int j=1;j<time;j++){
	    		  gamma0[i+j]=gamma0[i];beta0[i+j]=beta0[i];alpha0[i+j]=alpha0[i];
	    		  p_low0[i+j]=p_low0[i];Pstart[i+j]=Pstart[i];p_up0[i+j]=p_up0[i];
	    		  Pshut[i+j]=Pshut[i];time_on_off_ini0[i+j]=time_on_off_ini0[i];time_min_on0[i+j]=time_min_on0[i];
	    		  time_min_off0[i+j]=time_min_off0[i];fixedCost4startup0[i+j]=fixedCost4startup0[i];p_initial[i+j]=p_initial[i];
	    		  RU0[i+j]=RU0[i];RD0[i+j]=RD0[i];
	    		  Cold_hour0[i+j]=Cold_hour0[i];
	    		  }
	      }
          }
          for(int i=0;i<unit;i++){
        	  if(p_initial[i]>0){
        		  u0[i]=1.0;
        	  }else{
        		  u0[i]=0.0;
        	  }  
        	  //System.out.print(+" ");
          }
	      if(pathAndFilename.contains("std")) 
	      {
	    	  Cold_cost0 = new double[unit];
	    	  Hot_cost0 =new double[unit];
	    	  for(int i=0;i<unit;i++)
	    	  {
	    		  Cold_cost0[i]=2*fixedCost4startup0[i];
	    		  Hot_cost0[i]=fixedCost4startup0[i];
	    		  //System.out.println(Hot_cost0[i]+" "+Cold_cost0[i]+" "+ Cold_hour0[i]+" "+gamma0[i]+" "+beta0[i]+" "+alpha0[i]+" "+p_low0[i]+" "+Pstart[i]+" "+p_up0[i]+" "+Pshut[i]+" "+time_on_off_ini0[i]+" "+time_min_on0[i]+" "+time_min_off0[i]+" "+fixedCost4startup0[i]+" "+p_initial[i]+" "+RU0[i]+" "+RD0[i]);
	    	  }

	      }else{
	    	  Cold_cost0 = new double[unit];
	    	  Hot_cost0 =new double[unit];
	    	  for(int i=0;i<unit;i++)
	    	  {
	    		  Cold_hour0[i]=0.0;
	    		  Cold_cost0[i]=fixedCost4startup0[i];
	    		  Hot_cost0[i]=fixedCost4startup0[i];
	    	  }
	      }
	      
			if (pathAndFilename.contains("8_std")) {
				double[] proportion = { 0.71, 0.65, 0.62, 0.60, 0.58, 0.58, 0.60, 0.64, 0.73, 0.80, 0.82, 0.83, 0.82,
										0.80, 0.79, 0.79, 0.83, 0.91, 0.90, 0.88, 0.85, 0.84, 0.79, 0.74 };
				double sum_all_p_up = 0;
				for(int ii = 0; ii<unit; ii++){ sum_all_p_up = sum_all_p_up + p_up0[ii]; }
				
				for (int i = 0; i < T0; i = i + t_time) {
					PD0[i] = sum_all_p_up * proportion[i];
					spin[i] = PD0[i] * 0.03;
					if (t_time > 1) {
						for (int j = 1; j < t_time; j++) {
							PD0[i + j] = PD0[i]; spin[i + j] = spin[i];
						}
					}
				}
			}
	      
	      
	      //System.out.println(sc.nextInt());	         
	      //for(int i=0;i<unit;i++)
	      //{
	      //  System.out.println( gamma0[i]+" "+ alpha0[i]+" "+beta0[i]+" "+p_low0[i]+" "+p_up0[i]+" "+ time_on_off_ini0[i]+" "+time_min_on0[i]+" "+time_min_off0[i]+" "+fixedCost4startup0[i]+" "+RU0[i]+" "+RD0[i]);	    	    	  
	      //}
	      //System.out.println(this.pathAndFilename);
		  //System.out.println(T0+" "+unit+"\n"+" "+u0+"\n"+" "+u9);		  
		  //while(sc.hasNextLine()){			  
		  //System.out.println(sc.nextInt());	  
		  //}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			if(inputStream!=null){
				inputStream.close();
			}
			if(sc!=null){
				sc.close();
			}
		}
	}
	
	public void sub_system(int N, int T){
		this.unit = N;
		this.T0 = T;
		
		double[] tmp;
		
		tmp = this.alpha0;
		this.alpha0 = new double[N];
		for(int i = 0;i<N;i++){
			this.alpha0[i] = tmp[i];
		}
		
		tmp = this.beta0;
		this.beta0 = new double[N];
		for(int i = 0;i<N;i++){
			this.beta0[i] = tmp[i];
		}
		
		tmp = this.gamma0;
		this.gamma0 = new double[N];
		for(int i = 0;i<N;i++){
			this.gamma0[i] = tmp[i];
		}
		
		tmp = this.Pstart;
		this.Pstart = new double[N];
		for(int i = 0;i<N;i++){
			this.Pstart[i] = tmp[i];
		}
		
		tmp = this.Pshut;
		this.Pshut = new double[N];
		for(int i = 0;i<N;i++){
			this.Pshut[i] = tmp[i];
		}
		
		tmp = this.u0;
		this.u0 = new double[N];
		for(int i = 0;i<N;i++){
			this.u0[i] = tmp[i];
		}
		
		tmp = this.p_low0;
		this.p_low0 = new double[N];
		for(int i = 0;i<N;i++){
			this.p_low0[i] = tmp[i];
		}
		
		tmp = this.p_up0;
		this.p_up0 = new double[N];
		for(int i = 0;i<N;i++){
			this.p_up0[i] = tmp[i];
		}
		
		tmp = this.time_on_off_ini0;
		this.time_on_off_ini0 = new double[N];
		for(int i = 0;i<N;i++){
			this.time_on_off_ini0[i] = tmp[i];
		}
		
		tmp = this.time_min_on0;
		this.time_min_on0 = new double[N];
		for(int i = 0;i<N;i++){
			this.time_min_on0[i] = tmp[i];
		}
		
		tmp = this.time_min_off0;
		this.time_min_off0 = new double[N];
		for(int i = 0;i<N;i++){
			this.time_min_off0[i] = tmp[i];
		}
		
		tmp = this.fixedCost4startup0;
		this.fixedCost4startup0 = new double[N];
		for(int i = 0;i<N;i++){
			this.fixedCost4startup0[i] = tmp[i];
		}
		
		tmp = this.RU0;
		this.RU0 = new double[N];
		for(int i = 0;i<N;i++){
			this.RU0[i] = tmp[i];
		}
		
		tmp = this.RD0;
		this.RD0 = new double[N];
		for(int i = 0;i<N;i++){
			this.RD0[i] = tmp[i];
		}
		
		tmp = this.p_initial;
		this.p_initial = new double[N];
		for(int i = 0;i<N;i++){
			this.p_initial[i] = tmp[i];
		}
		
		tmp = this.index_tmp;
		this.index_tmp = new double[N];
		for(int i = 0;i<N;i++){
			this.index_tmp[i] = tmp[i];
		}
		
		tmp = this.PD0 ;
		this.PD0 = new double[N];
		for(int t = 0;t<T;t++){
			this.PD0[t] = tmp[t] * T/24 ;
		}
		
		tmp = this.spin;
		this.spin = new double[N];
		for(int t = 0;t<T;t++){
			this.spin[t] = tmp[t] * T/24;
		}
		
		
		
	}

}