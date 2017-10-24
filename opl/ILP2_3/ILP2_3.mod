/*********************************************
 * OPL 12.6.0.0 Model
 * Author: ACER
 * Creation Date: 11/08/2016 at 08:18:20
 *********************************************/

 // constantes
 int G= ...;
 int K = ...;
 int SD = ...;
 int E = ...;
 int cost_max=...;
 int spectrum_max=...;
 int dist_max=...;

 //variables
 
 range p = 1..K;
 range sd = 1.. SD;
 range sdp = 1..K*SD;
 range e = 1..E;
 int l[sdp][sdp]=...;
 
 int alfa[sd][p]=...;

 int R[sdp][e]=...;
 int dist[sdp]=...;
 
 dvar int x[sd][p] in 0..1;
 dvar int+ f[sd];
 
 dvar int delta[sd][sd] in 0..1;
 dvar float+ f1;
 dvar float+ f2;
 dvar float+ f3;
 dvar float+ fo;

 minimize fo;

 subject to {
 
 	fo==(1/cost_max)*f1+(1/dist_max)*f2+(1/spectrum_max)*f3; 
 
 	
 	f1 == sum(r in sd, a in p) (alfa[r][a]+G)*dist[r*K-K+a]*x[r][a];
 	
 	forall(r in sd, a in p){
 	 	f2 >= sum(r in sd, a in p) (dist[r*K-K+a]*x[r][a]);
 	}
 	//1.si el espectro es la suma osea uso efectivo de espectro
 	//forall(e in e) {
 	//	f3 >= sum(r in sd, a in p) R[r*K-K+a][e]*x[r][a]*(alfa[r][a]+G); 	
 	//}; 
 	
 	//2.el maximo espectro o sea la ranura de frecuencia maxima usada
 	forall(r in sd) {
 		f3 >= f[r] + alfa[r][1]; 	
 	};
 	
 	forall(r in sd) {
 		sum(k in p) x[r][k] == 1;
	};	

	forall(t in sd){
		forall(u in sd){
			forall(a in p){
				forall(b in p){
 					if((l[t*K-K+a][u*K-K+b] == 1) && (t != u)) 
 		 			{
 		 				delta[t][u] + delta[u][t] == 1;
 		 				f[u]-f[t] <= spectrum_max * delta[t][u];
 		 				f[t]-f[u] <= spectrum_max * delta[u][t]; 		 		
 		 			}
    			} 		 		
 			}
 		} 		 	
	}; 	
	
	forall(t in sd){
		forall(u in sd){
			forall(a in p){
				forall(b in p){
					if((l[t*K-K+a][u*K-K+b] == 1) && (t != u)) 
	 				{	
	 					f[t] + alfa[t][a] * x[t][a] + G - f[u] <= 
	 						(spectrum_max + G) * (1 - delta[t][u] + 2 - x[t][a] - x[u][b]);
						f[u] + alfa[u][b] * x[u][b] + G - f[t] <= 
							(spectrum_max + G) * (1 - delta[u][t] + 2 - x[t][a] - x[u][b]);     
					}			
				}	
 			}					 					
		}
	};		
 	
}