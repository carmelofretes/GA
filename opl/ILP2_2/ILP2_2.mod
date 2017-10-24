/*********************************************
 * OPL 12.6.0.0 Model
 * Author: ACER
 * Creation Date: 11/08/2016 at 08:18:20
 *********************************************/

 // constantes
 
 int K = ...;
 int SD = ...;
 float Ftotal = ...;
 int G = ...;
 //int s2d2 = ...;
 
 //variables

 range sd = 1.. SD;
 //range S2D2 = 1 .. s2d2;
 int l[sd][sd] = ...;
 
 int alfa[sd]=...;
 //int l[sdp][sdp] = ...;
 
 dvar int+ f[sd];
 
 dvar int delta[sd][sd] in 0..1;

 
 dvar int+ c;
 
 
 
 minimize c;
 
 
 subject to {
 	forall(r in sd) {
 	 	c >= f[r] + alfa[r]; 	 	
 	};
 	
 	/*forall(e in l) {
 		c >= sum(r in sd, a in p) R[r*K-K+a][e]*x[r][a]*(alfa[r][a] + G); 	
 	};*/ 
 	/*
 	forall(r in sd) {
 		sum(k in p) x[r][k] == 1;
	};	
 	*/
	
	forall(t in sd){
		forall(u in sd){
			if((l[t][u] == 1) && (t != u)) 
 		 	{
 		 		delta[t][u] + delta[u][t] == 1;
 		 		f[u]-f[t] <= Ftotal * delta[t][u];
 		 		f[t]-f[u] <= Ftotal * delta[u][t];
 		 	}			 		
 		} 		 	
	}; 
	
	forall(t in sd){
		forall(u in sd){
			if((l[t][u] == 1) && (t != u)) 
	 		{
	 			f[t] + alfa[t] + G - f[u] <= 
	 						(Ftotal + G) * (1 - delta[t][u]);
				f[u] + alfa[u] + G - f[t] <= 
							(Ftotal + G) * (1 - delta[u][t]);     
			}							 					
		}
	};
	
	forall(t in sd){
			f[t] <= Ftotal-1;
	}
}