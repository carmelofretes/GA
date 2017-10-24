/*********************************************
 * OPL 12.6.0.0 Model
 * Author: ACER
 * Creation Date: 11/08/2016 at 08:18:20
 *********************************************/

 // constantes
 
 int K = ...;
 int SD = ...;
 int G = ...;
 int E = ...;

 //variables
 
 range p = 1..K;
 range sd = 1.. SD;
 range sdp = 1..K*SD;

 range l = 1..E;
 
 int alfa[sd][p]=...;

 int R[sdp][l]=...;
 
 dvar int x[sd][p] in 0..1;

 dvar int+ c;

 minimize c;

 subject to {
 	
 	forall(e in l) {
 		c >= sum(r in sd, a in p) R[r*K-K+a][e]*x[r][a]*(alfa[r][a] + G); 	
 	}; 
 	
 	forall(r in sd) {
 		sum(k in p) x[r][k] == 1;
	};	
 	
}