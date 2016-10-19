//This Class project the parameters of UC to 2-bin model's parameters.
public class Projection {
	readdataUC ProjectreaddataUC(readdataUC ucdata){
		//DecimalFormat    df   = new DecimalFormat("#.0000"); 
		for(int i=0;i<ucdata.unit;i++){
			ucdata.RU0[i]=(ucdata.RU0[i])/(ucdata.p_up0[i]-ucdata.p_low0[i]);
			//ucdata.RU0[i]=Double.valueOf(df.format(ucdata.RU0[i])); 			
			ucdata.RD0[i]=(ucdata.RD0[i])/(ucdata.p_up0[i]-ucdata.p_low0[i]);			
			ucdata.Pstart[i]=(ucdata.Pstart[i]-ucdata.p_low0[i])/(ucdata.p_up0[i]-ucdata.p_low0[i]);			
			ucdata.Pshut[i]=(ucdata.Pshut[i]-ucdata.p_low0[i])/(ucdata.p_up0[i]-ucdata.p_low0[i]);			
			ucdata.alpha0[i]=ucdata.alpha0[i]+ucdata.beta0[i]*ucdata.p_low0[i]+ ucdata.gamma0[i]*(ucdata.p_low0[i]*ucdata.p_low0[i]);			
            ucdata.beta0[i]=(ucdata.p_up0[i]-ucdata.p_low0[i])*(ucdata.beta0[i]+2.0* ucdata.gamma0[i]*ucdata.p_low0[i]);          
            ucdata.gamma0[i]=ucdata.gamma0[i]*(ucdata.p_up0[i]-ucdata.p_low0[i])*(ucdata.p_up0[i]-ucdata.p_low0[i]);            
            ucdata.p_initial[i]=(ucdata.p_initial[i]-ucdata.u0[i]*ucdata.p_low0[i])/(ucdata.p_up0[i]-ucdata.p_low0[i]);
		}			
		return ucdata;
		}
}
