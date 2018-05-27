package ga;
import ga.objects.Enlace;
import ga.objects.Ruteo;
import ga.objects.Solucion;

import java.io.*;
import java.util.*;

import static java.lang.Math.floor;

/**
 * Created by ysapy on 23/02/16.
 */
public class PoblacionInicial {

    private static Long costoMayor;
    private static Long saltoMayor;
    private static Long espectroMayor;
    private static int nivelDeModulacion = 1;
    private static String algoritmo = "exactFit";
    private static String topologia = "tipoRed";
    private static List<String> tiposDeCarga = new ArrayList<>();
//
    public static void main(String [] args) throws IOException {
        tiposDeCarga.add("Uniforme");
        tiposDeCarga.add("Aleatoria");
        for (String tipoDeCarga : tiposDeCarga) {
            //La anterior línea
            //String pathInicial = "G:\\Genetico\\ga\\src\\ga\\archivos\\" + topologia + "\\" + 
            //    algoritmo + "\\carga" + tipoDeCarga + "\\";
            String pathInicial = "G:\\Genetico\\ga\\src\\ga\\archivos\\" + topologia + "\\" + 
                algoritmo + "\\";

            List<List<Solucion>> todosLosConjuntos = new ArrayList<>();
            List<List<Boolean>> topologia = AGHelper.leerTopologia(pathInicial);
            int cantLlamadasGA = AGHelper.leerParametro(pathInicial, "cantidad de corridas independientes");
            int cantSolucionesIniciales = AGHelper.leerParametro(pathInicial, "cantidad de cromosomas");
            int totalRanuras = AGHelper.leerParametro(pathInicial, "cantidad de longitudes de onda por fibra");
            int criterioDeParada = AGHelper.leerParametro(pathInicial, "criterio de parada");
            double probabMutacion = AGHelper.leerProbabMutacion(pathInicial, "probabilidad de mutacion");
        /*
        *  1. MOGA spectrum allocation random
        *  2. MOGA spectrum allocation first fit
        */

            List<Integer> cantDemandas = new ArrayList<>();
            cantDemandas.add(50); //escenarios de demandas
            cantDemandas.add(35);
            cantDemandas.add(100);
            //cantDemandas.add(200);
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            int k = 2;   //ATENCION!!!  ESTO CAMBIAR DE ACUERDO A LOS CAMINOS.
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            int cantCantidadDeDemandas = 3; // estos son los escenarios
            List<DemandaInfo> demandaInfoList = new ArrayList<>();
            String archivoDeMaximos;
            String pathActual;
            long TInicio, TFin; //Variables para determinar el tiempo de ejecución

            for (int a = 0; a < k; a++) {
                for (int d = 0; d < cantCantidadDeDemandas; d++) {
                    pathActual = pathInicial + "k" + (a + 1) + "\\cantSolicitada" + cantDemandas.get(d) + "\\";
                    archivoDeMaximos = pathActual + "maximos.txt";
                    demandaInfoList.addAll(llenarDemandInfo(pathActual + "ga.txt"));
                    saltoMayor = getSaltoMayorDeLaRed(demandaInfoList, a + 1);
                    costoMayor = (getCostoMayorDeLaRed(demandaInfoList) + 1) * saltoMayor; // el 1 es agregado para banda guarda
                    espectroMayor = Long.valueOf(totalRanuras);

                    String sFichero = pathActual + "corridaNro_1.txt";
                    File fichero = new File(sFichero);
                    if (!fichero.exists()) {

                        guardarMaximos(archivoDeMaximos);

                        for (int i = 0; i < cantLlamadasGA; i++) {

                            TInicio = System.currentTimeMillis();
                            todosLosConjuntos.add(ga(topologia, cantSolucionesIniciales, totalRanuras, criterioDeParada, probabMutacion, demandaInfoList, (a + 1)));

                            TFin = System.currentTimeMillis();

                            // guardar tambien las rutas y ranuras elegidas
                            guardarEnArchivo(pathActual, todosLosConjuntos.get(i), i, (((TFin - TInicio) / 1000) / 60));

                            long minRunningMemory = (1024 * 1024);

                            Runtime runtime = Runtime.getRuntime();

                            if (runtime.freeMemory() < minRunningMemory)
                                System.gc();
                        }
                    }
                    demandaInfoList = new ArrayList<>();
                    todosLosConjuntos = new ArrayList<>();
                }
            }
        }
    }

    public static List<Solucion> generarPoblacionInicial (List<List<Boolean>> topologia, Integer cantSolucionesIniciales, Integer totalRanuras, List<DemandaInfo> demandaInfoList) throws FileNotFoundException {

        List<Solucion> poblacionInicial = new ArrayList<>();

        List<DemandaInfo> demandasMayores = new ArrayList<>();
        /////////////////////////////////////////////////////////////////////
        //AQUI SE GENERA LA MATRIZ DE CARGA Y SE DEVIDE EN 70%(ALEATORIO) Y 30%(LOS MAYORES)
        /////////////////////////////////////////////////////////////////////
        demandasMayores.addAll(definirOrdenDemandas(demandaInfoList));
        /////////////////////////////////////////////////////////////////////
        List<Enlace> enlacesUsados = new ArrayList<>();
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        //AQUI SE GENERAN LAS SOLUCIONES  DE LA POBLACIÓN INICIAL, SE LE ASGINAN LAS RANURAS CORRESPONDIENTES
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        
        for (int i = 0; i < cantSolucionesIniciales; i++) {
            if (!poblacionInicial.isEmpty()) {
                enlacesUsados.addAll(poblacionInicial.get(i-1).getEnlaces()); 
            }
            poblacionInicial.add(generarSolucion(totalRanuras, topologia, demandasMayores, demandaInfoList, enlacesUsados));            

        }

        return poblacionInicial;
    }

    public static List<DemandaInfo> definirOrdenDemandas (List<DemandaInfo> demandasInfo) {
        int i, r;

        List<DemandaInfo> mayores = new ArrayList<DemandaInfo>();
        List<DemandaInfo> aux;
        List<DemandaInfo> infoCopy = new ArrayList<DemandaInfo>();

        //traer la lista de demandasInfo con las filas que tienen la mayor X de cada demanda
        //ESTO SE APLICA CUANDO LA DEMANDA TENGA  MAS DE UN CAMINO
        aux = obtenerDemandasMayores(demandasInfo);

        // ordenar aux de mayor a menor
        ordenar(aux);
        infoCopy.addAll(aux);

        // copio el 30% de los mayores a mayores
        for (i = 0; i < floor(aux.size() * 0.3); i++){
            mayores.add(aux.get(i));
            infoCopy.remove(aux.get(i));
        }

        // copio el 70% restante en orden aleatorio
        for (i = 0; i < aux.size() - floor(aux.size() * 0.3); i++){
            r = (int)(Math.random()*infoCopy.size());
            mayores.add(infoCopy.get(r));
            infoCopy.remove(r);
        }

        return mayores;
    }

    public static Solucion generarSolucion(int cantRanuras, List<List<Boolean>> topologia, List<DemandaInfo> mayores, List<DemandaInfo> demandasInfo, List<Enlace> enlacesUsados) {
        List<Enlace> auxEnlacesUsados = enlacesUsados;
        /////////////////////////////////////////////////////////////////////
        //AQUI SE GENERAN LOS ENLACES Y LAS RANURAS PARA TODOS LOS NODOS DE LA TOPOLOGIA
        /////////////////////////////////////////////////////////////////////    
        List<Enlace> enlacesIniciales = generarListaInicialRanuras (cantRanuras, topologia);

        for (int eu = 0; eu < auxEnlacesUsados.size(); eu++) {
            if (auxEnlacesUsados.get(eu).getInicio() == enlacesIniciales.get(eu).getInicio()) {
                enlacesIniciales.get(eu).setRanurasUsadas(auxEnlacesUsados.get(eu).getRanurasUsadas());
            }
        }
        //////////////////////////////////////////////////////////////////////////////////
        // AQUI SE GENERA LA SOLUCION Y SE DEBE 
        //inicializar con una lista de enlaces en la cual todas sus ranuras estan libres
        //////////////////////////////////////////////////////////////////////////////////
        Solucion solucion = new Solucion(enlacesIniciales); 
        int j;
        boolean bloqueado;

        // selecciono demanda por demanda en el orden ya establecido del 30% y 70% (MAYORES)
        // de cada demanda, traigo la ruta mas corta de demandasInfo e intento asignarle la cantidad de ranuras que quiere
        // si no puede con esa ruta, intenta con la siguiente ruta mas corta, etc
        // y si no puede asignar las ranuras con ninguna de sus rutas, suma una demanda bloqueada a solucion
        for (int m = 0; m < mayores.size(); m++) {

            DemandaInfo demanda = mayores.get(m);
            bloqueado = true;

            for (j = 0; j < demandasInfo.size(); j++){
                if (demandasInfo.get(j).getOrigen() == demanda.getOrigen() &&
                demandasInfo.get(j).getDestino() == demanda.getDestino()){
                    if (asignarRanuras(solucion, j, demandasInfo)){
                        j = demandasInfo.size();
                        bloqueado = false;
                    }
                }
            }

            if (bloqueado){
                agregarBloqueado(solucion, demanda);
                solucion.setCantBloq(solucion.getCantBloq() + 1);
            }
        }

        // elegir rutas aleatoriamente
//        for (DemandaInfo demanda : mayores) {
//            bloqueado = true;
//
//            for (j = 0; j < demandasInfo.size(); j++){
//                if (demandasInfo.get(j).getOrigen() == demanda.getOrigen()
//                        && demandasInfo.get(j).getDestino() == demanda.getDestino()){
//                    int o = j - 1;
//                    for (int r = 0; r < k; r++) {
//                        o++;
//                        rutasDisponibles.add(o);
//                    }
//                    while (!rutasDisponibles.isEmpty() && bloqueado) {
//                        int rutaElegida = rnd.nextInt(rutasDisponibles.size() - 1) + 0;
//                        rutasDisponibles.remove(rutaElegida);
//                        if (asignarRanuras(solucion, rutaElegida, demandasInfo)) {
//                            bloqueado = false;
//                        }
//                    }
//                    j = demandasInfo.size();
//                }
//            }
//
//            if (bloqueado){
//                agregarBloqueado(solucion, demanda);
//                solucion.setCantBloq(solucion.getCantBloq() + 1);
//            }
//        }




//        solucion.setSaltos(getSaltoMayor(solucion, demandasInfo)/saltoMayor);
        solucion.setSaltos(getSaltoMayor(solucion, demandasInfo));
//        solucion.setEspectro(getEspectroMayor(solucion)/espectroMayor);
        solucion.setEspectro(getEspectroMayor(solucion));
//        solucion.setCosto(getCostoMayor(solucion, demandasInfo)/costoMayor);
        solucion.setCosto(getCostoDeLaSolucion(solucion, demandasInfo));
        solucion.setFitness(solucion.getCosto()/costoMayor + solucion.getSaltos()/saltoMayor + solucion.getEspectro()/espectroMayor);
        return solucion;

    }

    public static List<DemandaInfo> llenarDemandInfo (String archivo) throws FileNotFoundException {

        List<DemandaInfo> demandasInfo = new ArrayList<DemandaInfo>();
        demandasInfo.addAll(AGHelper.leerDemandasInfo(archivo));

        return demandasInfo;

    }

    public static void ordenar(List<DemandaInfo> demandasInfo){

        DemandaInfo aux;
        int j;

        for (int i = 0; i < demandasInfo.size(); i++) {
            for (j = i + 1; j < demandasInfo.size(); j++) {
                if (demandasInfo.get(i).getX() < demandasInfo.get(j).getX()){
                    aux = demandasInfo.get(i);
                    demandasInfo.set(i, demandasInfo.get(j));
                    demandasInfo.set(j, aux);
                }
            }
        }

    }

    public static boolean asignarRanuras (Solucion solucion, int rutaNro, List<DemandaInfo> demandasInfo){

        boolean aplica = false;
        List<Boolean> ranuras = new ArrayList<>();
        
        
        List<Integer> posicionesLibres = new ArrayList<>();
        int r, cantRanurasLibres;

        DemandaInfo demandaInfo = demandasInfo.get(rutaNro);
        int nodoPrimero = demandaInfo.getRuta().get(0);
        int nodoSegundo = demandaInfo.getRuta().get(1);

        List<Integer> ranurasDisponibles = new ArrayList<>();
        
        //AQUI CREAMOS LAS PARTICIONES PARA LA ASGINACIÓN FIRST LAST FIT
        //ESTAS PARTICIONES SON IMPAR PARA ASIGNAR Y EN EL CASO DE NO PODER ASIGNAR EN IMPAR HACERLO EN EL PAR
        List<Integer> ranurasDisponiblesImpar = new ArrayList<>();
        List<Integer> ranurasDisponiblesPar = new ArrayList<>();

        //AQUI VEMOS LA DISPONIBILIDAD DE LAS RANURAS EN TODOS LOS ENLACES DE MANERA A CONTROLAR LA CONTINUIDAD
        //Y LA CONTIGUIDAD
        ranurasDisponibles.addAll(obtenerRanurasDisponiblesParaRuta(solucion, demandaInfo));
        ///////////////////////////////////////////////////////////////////////////////////////////
        //AQUI GENERO DOS PARTICIONES DE MANERA A DETERMINAR CUALES SON IMPARES Y PARES
        //PARA IMPLEMENTAR LA POLITICA FIRST-LAST FIT
        ///////////////////////////////////////////////////////////////////////////////////////////
        for (int rd = 0; rd < ranurasDisponibles.size(); rd++) {
           //Integer ranuraDisp = ranurasDisponibles.get(rd);
            if (esImpar(ranurasDisponibles.get(rd))) {
                //ES IMPAR
                ranurasDisponiblesImpar.add(rd);
            } else {
                //ES PAR
                ranurasDisponiblesPar.add(rd);
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////// 
        //HASTA AQUI
        ///////////////////////////////////////////////////////////////////////////////////////////
        
        
// elegir randomicamente una posicion de ranurasDisponibles
// asignar esa ranura a la solucion para esta demanda
        if (ranurasDisponibles.isEmpty())
            return false;
        else {
            if ("randomfit".equals(algoritmo)) {
                // elegir enlaces RANDOM FIT
                int ranuraElegida = (int) (Math.random()*(ranurasDisponibles.size()-1));
                agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(ranuraElegida));

            } else if ("firstFit".equals(algoritmo)) {
                // elegir ranuras con FIRST FIT
                agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(0));
                
            } else if ("lastFit".equals(algoritmo)) {
                //asignación de ranura LAST FIT
                agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(ranurasDisponibles.size()-1));

            } else if ("firstLastFit".equals(algoritmo)) {
                //asignación de ranura FIRST LAST FIT
                if (!ranurasDisponiblesImpar.isEmpty()) {
                    agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponiblesImpar.get(0));    
                } else {
                    agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponiblesPar.get(ranurasDisponiblesPar.size()-1));
                }
                
            } else if ("leastUsed".equals(algoritmo)) {
                //asignación de ranura LEAST USED  
                agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(0)); 
                
            } else if ("mostUsed".equals(algoritmo)) {
                //asignación de ranura MOST USED
                if (solucion.getRuteos().isEmpty()) {
                    agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(0));
                } else {
                    //int ranuraElegida = (int) (Math.random()*(ranurasDisponibles.size()-1));
                    agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(ranurasDisponibles.size()-1));    
                }
            } else if ("exactFit".equals(algoritmo)) {
                //asignación de ranura EXACT FIT
                agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(0));
            } else {
                return false;
            }            
            return true;
        }
    }

    public static List<Integer> obtenerRanurasDisponiblesParaRuta (Solucion solucion, DemandaInfo demandaInfo) {
        int origen;
        int destino;
        List<Enlace> enlaces = new ArrayList<>();
        int auxiliarRanura;
        int auxiliarUsado;

        //AQUI LO QUE HACEMOS ES ENCONTRAR LOS ENLACES PARA EL PEDIDO, DESPUES VEMOS LA DISPONIBILIDAD DE LAS RANURAS
        for (int i = 0; i < solucion.getEnlaces().size(); i++) {
            for (int j = 0; j < (demandaInfo.getRuta().size()-1); j++) {   //-1 PORQUE SON 2 ENLACES
                origen = demandaInfo.getRuta().get(j);
                destino = demandaInfo.getRuta().get(j + 1);

                if (solucion.getEnlaces().get(i).getInicio() == origen &&
                        solucion.getEnlaces().get(i).getFin() == destino) {
                    enlaces.add(solucion.getEnlaces().get(i));
                }
            }
        }

        List<Integer> indiceDeRanurasLibres = new ArrayList<>();
        List<Integer> indiceRanurasLibresMasUsadas = new ArrayList<>();
//        List<Integer> indiceRanurasLibresExactas = new ArrayList<>();

        // obtener ranuras libres del primer enlace
        for (int i = 0; i < enlaces.get(0).getRanuras().size(); i++) {
            if (ranuraEsSolucion(enlaces.get(0).getRanuras(), i, demandaInfo.getTraf() + nivelDeModulacion)) {
                indiceDeRanurasLibres.add(i);
                //O AQUI PUEDO PONER PARA CONTROLAR LA EXACTITUD DE LA RANURA PARA LA POLITICA EXACT FIT
                /*if ("exactFit".equals(algoritmo)) {
                    if (i == 0){
                        if (enlaces.get(0).getRanuras().get(i + demandaInfo.getTraf() + nivelDeModulacion)) {
                            //ENTONCES ES EXACTO
                            indiceRanurasLibresExactas.add(i);
                        }
                    } else {
                        if (enlaces.get(0).getRanuras().get(i-1) && enlaces.get(0).getRanuras().get(i + demandaInfo.getTraf() + nivelDeModulacion)) {
                            //ENTONCES ES EXACTO
                            indiceRanurasLibresExactas.add(i);                    
                        }
                    }
                }*/
                //HASTA AQUI LAS POLITICAS DE ASIGNACIÓN EXACT FIT
                
                //AQUI CREAR EL ARRAY CON SLOTS IMPARES Y PARES                  
            }
        }  
        
        //OBTENER RANURAS DE LOS ENLACES RESTANTES
        //AQUI SE CONTROLA QUE LAS RANURAS LIBRES QUE ANTERIORMENTE SE SELECCIONARON ESTEN LIBRES EN LAS DEMAS RUTAS
        //DE MANERA A CONTROLAR LA CONTINUIDAD Y CONTIGUIDAD EN LOS ENLACES
        //SI NO TIENEN CONTINUIDAD Y CONTIGUIDAD SE REMUEVE DE LA LISTA
        for (int i = 1; i < enlaces.size(); i++) {
            List<Integer> copiaIndiceDeRanurasLibres = new ArrayList<>();
//            List<Integer> copiaIndiceRanurasLibresExactas = new ArrayList<>();
            
            copiaIndiceDeRanurasLibres.addAll(indiceDeRanurasLibres);
//            copiaIndiceRanurasLibresExactas.addAll(indiceRanurasLibresExactas);
            
            for (int j = 0; j < indiceDeRanurasLibres.size(); j++) {
                if (!ranuraEsSolucion(enlaces.get(i).getRanuras(), indiceDeRanurasLibres.get(j), demandaInfo.getTraf() + nivelDeModulacion)) {
                    copiaIndiceDeRanurasLibres.remove(indiceDeRanurasLibres.get(j));
                    //copiaIndiceRanurasLibresExactas.remove(indiceRanurasLibresExactas.get(j)); //EN EL CASO DE EXACT FIT
                }
            }
            indiceDeRanurasLibres = new ArrayList<>();
//            indiceRanurasLibresExactas = new ArrayList<>();
            
            indiceDeRanurasLibres.addAll(copiaIndiceDeRanurasLibres);
            
        }

// ESTE PEDAZO DE CODIGO USA PARA CALCULAR LA RANURA MAS USADA  
//AQUI REVISAR PORQUE NO ORDENA DE MENOR A MAYOR LAS RANURAS USADAS
        if ("leastUsed".equals(algoritmo) || "mostUsed".equals(algoritmo)) {
            indiceRanurasLibresMasUsadas.addAll(indiceDeRanurasLibres);
            //AQUI CREAMOS EL ARRAY CON LAS RANURAS MAS Y MENOS USADAS PARA LA ASGINACIÓN LEAST Y MOST USED
            for (int i = 0; i < enlaces.size(); i++) {
                for (int ranUs = 0; ranUs < indiceRanurasLibresMasUsadas.size(); ranUs++) {
                    for (int ranUs2 = ranUs + 1; ranUs2 < indiceRanurasLibresMasUsadas.size(); ranUs2++) {
                        if (enlaces.get(i).getRanurasUsadas().get(ranUs) > enlaces.get(i).getRanurasUsadas().get(ranUs2)){
                            auxiliarRanura = indiceRanurasLibresMasUsadas.get(ranUs);
                            indiceRanurasLibresMasUsadas.set(ranUs, indiceRanurasLibresMasUsadas.get(ranUs2));
                            indiceRanurasLibresMasUsadas.set(ranUs2, auxiliarRanura);
                        }
                    }
                }              
            }
            indiceDeRanurasLibres = new ArrayList<>();
            indiceDeRanurasLibres.addAll(indiceRanurasLibresMasUsadas);  
            return indiceDeRanurasLibres;
///////////////////////////////////////////////////////////////
// ESTE PEDAZO DE CODIGO USA PARA CALCULAR LA RANURA EXACTA             
///////////////////////////////////////////////////////////////
        } else if ("exactFit".equals(algoritmo)) {
            List<Integer> indiceRanurasLibresExactas = new ArrayList<>();
            
//            if (indiceDeRanurasLibres.get(indiceDeRanurasLibres.size()) == indiceDeRanurasLibres.size()) {
//                return indiceDeRanurasLibres;
//            } else  
                
                for (int rl = 0; rl < indiceDeRanurasLibres.size()-1; rl++) {
                    //ME FALTA CONTROLAR QUE CUANDO LA PRIMERA RANURA SEA CERO NO HAGA RESTO DE - 1
                    //AUNQUE POSIBLEMENTE NUNCA OCURRA ESTO DEBIDO A LA ASGINACION FIRST FIT QUE SE HACE
                    //EN LA PRIMERA VEZ
                    //
                    boolean ranAnt;
                    boolean ranPos;
                            
                    if (rl == 0) {
                        ranAnt = false ;
                    } else { 
                        ranAnt = enlaces.get(0).getRanuras().get(rl-1) ;
                    }
                    
                    ranPos = enlaces.get(0).getRanuras().get(rl + demandaInfo.getTraf() + nivelDeModulacion);
 
                    if (!ranAnt && !ranPos) {
                        //ENTONCES ES EXACTO
                        indiceRanurasLibresExactas.add(rl);                    
                    }
                }            
            indiceDeRanurasLibres = new ArrayList<>();
            indiceDeRanurasLibres.addAll(indiceRanurasLibresExactas);
            return indiceDeRanurasLibres;
        } 
        return indiceDeRanurasLibres;
        
//HASTA AQUI
//        return indiceDeRanurasLibres;
    }

    //AQUI CREO QUE SE CALCULA SI LA RANURA ACTUAL TIENE DISPONIBLE LA BANDA GUARDA
    public static boolean ranuraEsSolucion(List<Boolean> ranuras, int ranura, int cantSolicitada) {
        //AQUI CONTROLA QUE EL TAMAÑO DE LA RANURA NO SUPERE LA CANTIDAD DE RANURAS TOTALES
        if (!ranuras.get(ranura) && ranuras.size() >= (ranura + cantSolicitada)) { //tiene que ser mayor o igual
            for (int i = (ranura + 1); i < (ranura + cantSolicitada); i++) { //+1 PARA VEER DISPONIBILIDAD DE LA BANDA GUARDA
                if (ranuras.get(i)) {                        
                    return false;
                }
            }
            //AQUI TENGO QUE VER SI LA RANURA SELECCINADA ES UNA RANURA EXACTA PARA LA POLITICA EXACT FIT
            return true;
        }
        return false;
    }

    public static void agregarRanurasASolucion(Solucion solucion, int rutaNro, DemandaInfo demandaInfo, int primeraRanura) {
        int ranurasSolicitadas = demandaInfo.getTraf() + nivelDeModulacion;
        int nodoInicio, nodoFin, ubicacion, j;
        boolean existe = false;
        int posicion = -1;

        Ruteo ruteo = new Ruteo();
        List<Integer> ranurasUsadas = new ArrayList<>();

        // crea una lista de la posicion de las ranuras que usa cada enlace
        for (int i = primeraRanura; i < ranurasSolicitadas + primeraRanura; i++) {
            ranurasUsadas.add(i);
        }

        // marca en la solucion cuales son las ranuras que van a ser ocupadas por esta demanda
        for (int i = 0; i < demandaInfo.getRuta().size() - 1; i++) {
            nodoInicio = demandaInfo.getRuta().get(i);
            nodoFin = demandaInfo.getRuta().get(i + 1);
            ubicacion = ubicarEnlace(nodoInicio, nodoFin, solucion.getEnlaces());
            /////////////////////////////////////////////////////////////////////////////
            //AQUI LE ASIGNA LAS RANURAS DE ACUERDO AL PEDIDO Y AL NIVEL DE MODULACION
            /////////////////////////////////////////////////////////////////////////////
            for (j = primeraRanura; j < (ranurasSolicitadas + primeraRanura); j++) {
                solucion.getEnlaces().get(ubicacion).getRanuras().set(j, true);
                //AQUI SE ACTUALIZA UN CONTADOR DE LAS RANURAS USADAS
                solucion.getEnlaces().get(ubicacion).getRanurasUsadas().set(j, solucion.getEnlaces().get(ubicacion).getRanurasUsadas().get(j)+1);
                
            }
        }

        // crea el objeto que cubre esta demanda y agrega a solucion
        for (int i = 0; i < solucion.getRuteos().size(); i++) {
            //SI YA EXISTE EL RUTEO NO HACE FALTA AGREGAR A LA LISTA DE RUTEOS DE SOLUCION
            if (solucion.getRuteos().get(i).getOriden() == demandaInfo.getOrigen() &&
                    solucion.getRuteos().get(i).getDestino() == demandaInfo.getDestino()) {
                existe = true;
                posicion = i;
                break;
            }
        }
        //SI EXISTE SE ACTUALIZA LAS RANURAS USADAS
        if (existe){
            solucion.getRuteos().get(posicion).setRanurasUsadas(ranurasUsadas);
        } else {
            ruteo.setDemandaId(demandaInfo.getDemandaId());
            ruteo.setOriden(demandaInfo.getOrigen());
            ruteo.setDestino(demandaInfo.getDestino());
            ruteo.setRanurasUsadas(ranurasUsadas);
            ruteo.setRutaNro(rutaNro);

            solucion.getRuteos().add(ruteo);
        }
    }

    public static boolean aplicaRanuras(int primeraRanura, int rutaNro, List<Enlace> enlaces, List<DemandaInfo> demandasInfo) {

        DemandaInfo demandaInfo = demandasInfo.get(rutaNro);
        int ranurasSolicitadas = demandaInfo.getTraf();
        int nodoInicio, nodoFin, posicion, j;
        int limite = 0;

        for (int i = 0; i < demandaInfo.getRuta().size() - 1; i++) {
            nodoInicio = demandaInfo.getRuta().get(i);
            nodoFin = demandaInfo.getRuta().get(i + 1);
            posicion = ubicarEnlace(nodoInicio, nodoFin, enlaces);
            limite = ranurasSolicitadas + primeraRanura;

            for (j = primeraRanura; j < limite; j++) {
                // si recibe como primera ranura una de las ultimas y sumando las ranuras solicitadas alcanza mas del total de ranuras
                // envia falso porque no aplica
                if (j == enlaces.get(posicion).getRanuras().size() || enlaces.get(posicion).getRanuras().get(j)){
                    return false;
                }
            }
        }

        return true;
    }

    public static int ubicarEnlace (int inicio, int fin, List<Enlace> enlaces) {

        for (int i = 0; i < enlaces.size(); i++) {
            if (enlaces.get(i).getInicio() == inicio && enlaces.get(i).getFin() == fin){
                return i;
            }
        }

        return -1;
    }



    /*
    * Aca empieza el purete
    *
    * */

    public static List<Solucion> ga(List<List<Boolean>> topologia, int cantSolucionesIniciales, int totalRanuras, int criterioDeParada, double probabilidaMutacion, List<DemandaInfo> demandasInfo, int k) throws FileNotFoundException {

        int j, reproductor1, reproductor2, candidato1 = -1, candidato2 = -1;
        Solucion hijo1, hijo2; // inicializar con enlaces con ranuras vacias

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //AQUI SE GENERA LA POBLACION INICIAL
        List<Solucion> poblacionInicial = generarPoblacionInicial(topologia, cantSolucionesIniciales, totalRanuras, demandasInfo);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        List<Solucion> poblacionActual = new ArrayList<>();
        poblacionActual.addAll(poblacionInicial);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //AQUI SE DETERMINA EL CONJUNTO DE PARETO DE LA POBLACIÓN INICIAL osea EL CONJUNTO P
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        rankeoPareto(poblacionActual); //AQUI EL CONJUNTO DE SOLUCIONES QUIEN ES DOMINADO Y QUIEN NO
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //HASTA AQUI
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        List<Solucion> poblacionNueva;
        List<Solucion> aux ;
        Random rnd = new Random();
        Long TInicio;
        Long TFin; //Variables para determinar el tiempo de ejecución

        TInicio = System.currentTimeMillis();

        if (k == 1) {
            rankeoPareto(poblacionActual);
            Collections.sort(poblacionActual);  //AQUI SE ORDENA EL COJUNTO ANTERIOR
            return poblacionActual;
            //return poblacionInicial;
        }

        int v = 1;
        TFin = System.currentTimeMillis();
        //int tiempoLimite = criterioDeParada * 60 * 1000;    //ESTE ES EL ANTERIOR
        int tiempoLimite = criterioDeParada * 60 * 1000;

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //DESDE AQUI SE HACE EL CRUZAMIENTO Y LA MUTACIÓN
        //DESDE AQUI SE REALIZA LA ITERACCION DEL AG PARA ENCONTRAR UNA MEJOR SOLUCION, EN ESTE CASO
        //COMO METODO DE PARADA ES EL TIEMPO
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        while ((TFin - TInicio) < tiempoLimite) {

//            System.out.println(v + ". Inicio: " + TInicio + ", Fin: " + TFin + ", Dif: " + (TFin - TInicio) + ", sigue: " + ((TFin - TInicio) < (240000)));
            aux = new ArrayList<>();
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //ESTE OBJETO SOLUCION, ES EL CONJUNTO "Q"
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            poblacionNueva = new ArrayList<>();

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //EN ESTE FOR LO QUE HAGO ES GENERAR EL CONJUNTO Q HASTA QUE SEA DE TAMAÑO IGUAL AL DE LA POBLACION ACTUAL
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //for (j = 0; j < poblacionInicial.size()/4; j++) {  //PORQUE DIVIDE ENTRE 4 - ESTE ES EL ANTERIOR
            for (j = 0; j < poblacionActual.size(); j++) { 
                
                hijo1 = new Solucion();
                hijo2 = new Solucion();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//AQUI SE HACE LA SELECCIÓN DE LOS PADRES PARA REPRODUCIRSE
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// elegir reproductores aleatoriamente
// elegir el índice de 2 candidatos aleatoriamente, de los 2 candidatos tomar el de mejor fitness para ser un reproductor
//PARA EL REPRODUCTOR 1 - elige aleatoriamente
                while(candidato1 == candidato2){
                    candidato1 = (int)(rnd.nextDouble() * poblacionActual.size());
                    candidato2 = (int)(rnd.nextDouble() * poblacionActual.size());
                }
//elegir el mejor candidato con el mejor fitnes para el reproductor 1
                if (poblacionActual.get(candidato1).getFitness() > poblacionActual.get(candidato2).getFitness()) {
                    reproductor1 = candidato2;
                } else {
                    reproductor1 = candidato1;
                }
//PARA EL REPRODUCTOR 2 - elige aleatoriamente
                while(candidato1 == candidato2 || candidato1 == reproductor1 || candidato2 == reproductor1){
                    candidato1 = (int)(rnd.nextDouble() * poblacionActual.size());
                    candidato2 = (int)(rnd.nextDouble() * poblacionActual.size());
                }
//elegir el mejor candidato con el mejor fitnes para el reproductor 2
                if (poblacionActual.get(candidato1).getFitness() > poblacionActual.get(candidato2).getFitness()) {
                    reproductor2 = candidato2;
                } else {
                    reproductor2 = candidato1;
                }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////              
//HASTA AQUI LA SELECCION
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//AQUI SE APLICA EL CRUCE
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                cruce (poblacionActual.get(reproductor1), poblacionActual.get(reproductor2), hijo1, hijo2, topologia, totalRanuras, demandasInfo);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//HASTA AQUI EL CRUCE
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//AQUI SE REALIZA LA MUTACIÓN
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                mutacion (hijo1, probabilidaMutacion, topologia, totalRanuras, demandasInfo);
                mutacion (hijo2, probabilidaMutacion, topologia, totalRanuras, demandasInfo);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//HASTA AQUI LA MUTACION
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////////////////////////////////////
                //AQUI SE AGREGAR EL HIJO DE LOS REPRODUCTORES A LA POBLACION NUEVA (Q), HASTA QUE EL TAMAÑO SEA
                //IGUAL A LA POBLACIÓN ACTUAL
                /////////////////////////////////////////////////////////////////////////////////////////////////
                poblacionNueva.add(hijo1);
                poblacionNueva.add(hijo2);

                candidato1 = -1;
                candidato2 = -1;
                
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//AQUI SE CONTROLA QUE SI LA POBLACION NUEVA LLEGO AL NUMERO DE POBLACIÓN ACTUAL ENTONCES YA NO HACE FALTA
//SEGUIR SELECCIONANDO, CRUZANDO Y MUTANDO
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                if (poblacionNueva.size() == poblacionActual.size()) {
                    j = poblacionActual.size();  //AQUI ASIGNAMOS EL TAMAÑO DE LA POBALCIÓN ACTUAL PARA QUE PUEDA SALIR DEL FOR
                } 

            }
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //AQUI SE HALLA EL CONJUNTO PARETO DEL CONJUNTO Q
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            rankeoPareto(poblacionNueva); //AQUI EL CONJUNTO DE SOLUCIONES QUIEN ES DOMINADO Y QUIEN NO
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //AQUI ARMO R = P U Q - YA RANKEADOS TODAS LAS SOLUCIONES
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            poblacionActual.addAll(poblacionNueva);
            Collections.sort(poblacionActual);  //AQUI SE ORDENA POR "PARETO" EL COJUNTO ANTERIOR
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //HASTA AQUI
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            List<Solucion> auxPoblacionActual = new ArrayList<>();
            auxPoblacionActual.addAll(poblacionActual);
            
            poblacionActual = new ArrayList<>();
            
            //AQUI APLICAR EL CALCULO DE LA DISTANCIA DE CROWDING, ANTES SELECCIONAR LOS MEJORES DEL PRIMER FRETNE DE PARETO
            elegirMejores (auxPoblacionActual, cantSolucionesIniciales, poblacionActual);
            
           
            
            TFin = System.currentTimeMillis();
            v++;
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //HASTA AQUI
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        return poblacionActual;

    }

    /**
     * Nuestro operador cruce utiliza el cruce de 2 puntos
     * @param reproductor1
     * @param reproductor2
     * @param hijo1
     * @param hijo2
     * @param topologia
     * @param totalRanuras
     * @param demandasInfo
     */
    public static void cruce (Solucion reproductor1, Solucion reproductor2, Solucion hijo1, Solucion hijo2, List<List<Boolean>> topologia, Integer totalRanuras, List<DemandaInfo> demandasInfo) {
        int cantRutas = reproductor1.getRuteos().size() <= reproductor2.getRuteos().size() ? reproductor1.getRuteos().size() : reproductor2.getRuteos().size();
        List<Ruteo> ruteo1 = new ArrayList<>();
        List<Ruteo> ruteo2 = new ArrayList<>();
        Ruteo ruteo = new Ruteo();
        Random rnd = new Random();

//AQUI TENGO PROBLEMAS PARA HALLAR EL PUNTO DE CRUCE
//      int puntoDeCruce1 = rnd.nextInt(reproductor1.getRuteos().size()- 3) + 0;
//      int puntoDeCruce2 = rnd.nextInt((reproductor1.getRuteos().size() - 2) - (puntoDeCruce1 + 1) + 1) + (puntoDeCruce1 + 1);
        
        //DESDE AQUI ES DE CARMELO //////////////////////////////////////////////////////////
        int puntoDeCruce1 = rnd.nextInt(reproductor1.getRuteos().size()-2); // -2 depende de la cantidad de rutas que haya
        int puntoDeCruce2 = rnd.nextInt((reproductor1.getRuteos().size() - 1) - (puntoDeCruce1 + 1) + 1) + (puntoDeCruce1 + 1) ;
        // HASTA AQUI /////////////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////////////////////////////////////////
        //Desde aqui agrego el conjunto de ruteo.         
        for (int i = 0; i <= puntoDeCruce1; i++) {
            ruteo = new Ruteo();
            //Al ruteo1 le agrego la ruta del reproductor1
            reproductor1.getRuteos().get(i).clonar(ruteo);
            ruteo1.add(ruteo);
            ruteo = new Ruteo();
            //Al ruteo2 le agrego la ruta del reproductor2
            reproductor2.getRuteos().get(i).clonar(ruteo);
            ruteo2.add(ruteo);
        }

//      for (int i = puntoDeCruce1 + 1; i <= puntoDeCruce2; i++) {    //este es el anterior, porque le suma 1 al punto de cruce 1
        for (int i = puntoDeCruce1 + 1; i <= puntoDeCruce2; i++) {  //se le suma uno porque tiene que ser la siguiente ruta a partir de pc1          
            ruteo = new Ruteo();
            //Al ruteo1 le agrego la ruta del reproductor2
            reproductor2.getRuteos().get(i).clonar(ruteo);
            ruteo1.add(ruteo);
            ruteo = new Ruteo();
            //Al ruteo2 le agrego la ruta del reproductor1
            reproductor1.getRuteos().get(i).clonar(ruteo);
            ruteo2.add(ruteo);
        }

//      for (int i = puntoDeCruce2 + 1; i < reproductor1.getRuteos().size(); i++) {
        for (int i = puntoDeCruce2 + 1; i < reproductor1.getRuteos().size(); i++) {
            ruteo = new Ruteo();
            reproductor1.getRuteos().get(i).clonar(ruteo);
            ruteo1.add(ruteo);
            ruteo = new Ruteo();
            reproductor2.getRuteos().get(i).clonar(ruteo);
            ruteo2.add(ruteo);
        }
        //Hasta aqui agrego las rutas
        //////////////////////////////////////////////////////////////////////////////////////
        hijo1.setRuteos(ruteo1);
        hijo2.setRuteos(ruteo2);

        hijo1.setEnlaces(generarListaInicialRanuras(totalRanuras, topologia));
        hijo2.setEnlaces(generarListaInicialRanuras(totalRanuras, topologia));

        recalcularRanuras (hijo1, demandasInfo);
        recalcularRanuras (hijo2, demandasInfo);

//        hijo1.setSaltos(getSaltoMayor(hijo1, demandasInfo)/saltoMayor);
        hijo1.setSaltos(getSaltoMayor(hijo1, demandasInfo));
//        hijo1.setEspectro(getEspectroMayor(hijo1)/espectroMayor);
        hijo1.setEspectro(getEspectroMayor(hijo1));
//        hijo1.setCosto(hijo1.getFitness()/costoMayor);
        hijo1.setCosto(getCostoDeLaSolucion(hijo1, demandasInfo));
        hijo1.setFitness(hijo1.getCosto()/costoMayor + hijo1.getSaltos()/saltoMayor + hijo1.getEspectro()/espectroMayor);

//        hijo2.setSaltos(getSaltoMayor(hijo2, demandasInfo)/saltoMayor);
        hijo2.setSaltos(getSaltoMayor(hijo2, demandasInfo));
//        hijo2.setEspectro(getEspectroMayor(hijo2)/espectroMayor);
        hijo2.setEspectro(getEspectroMayor(hijo2));
//        hijo2.setCosto(hijo2.getFitness()/costoMayor);
        hijo2.setCosto(getCostoDeLaSolucion(hijo2, demandasInfo));

        hijo2.setFitness(hijo2.getCosto()/costoMayor + hijo2.getSaltos()/saltoMayor + hijo2.getEspectro()/espectroMayor);

    }

    public static void mutacion (Solucion solucion, double probabilidadMutacion, List<List<Boolean>> topologia, Integer totalRanuras, List<DemandaInfo> demandasInfo) {
        List<Integer> posiciones;
        int j, r;

        for (int i = 0; i < solucion.getRuteos().size(); i++) {
            if ((Math.random()) <= probabilidadMutacion) {
                posiciones = new ArrayList<>();
                for (j = 0; j < demandasInfo.size(); j++) {
                    if (demandasInfo.get(j).getOrigen() == solucion.getRuteos().get(i).getOriden() &&
                            demandasInfo.get(j).getDestino() == solucion.getRuteos().get(i).getDestino()) {
                        posiciones.add(j);
                    }
                }

                r = (int)(Math.random() * posiciones.size());

                solucion.getRuteos().get(i).setRutaNro(posiciones.get(r));
            }
        }

        int cantEnlaces = solucion.getEnlaces().size();
        for (int i = 0; i < cantEnlaces; i++) {
            solucion.getEnlaces().remove(cantEnlaces - i - 1);
        }
        solucion.setEnlaces(generarListaInicialRanuras(totalRanuras, topologia));
        solucion.setCantBloq(0);
        solucion.setFitness(0.0);
        recalcularRanuras(solucion, demandasInfo);

//        solucion.setSaltos(getSaltoMayor(solucion, demandasInfo)/saltoMayor);
        solucion.setSaltos(getSaltoMayor(solucion, demandasInfo));
//        solucion.setEspectro(getEspectroMayor(solucion)/espectroMayor);
        solucion.setEspectro(getEspectroMayor(solucion));
//        solucion.setCosto(solucion.getFitness()/costoMayor);
        solucion.setCosto(getCostoDeLaSolucion(solucion, demandasInfo));

        solucion.setFitness(solucion.getCosto()/costoMayor + solucion.getSaltos()/saltoMayor + solucion.getEspectro()/espectroMayor);

    }

    public static void recalcularRanuras (Solucion solucion, List<DemandaInfo> demandasInfo) {
        int j, rutaNro;

        // vaciar las ranuras usadas por cada ruta
        for (int i = 0; i < solucion.getRuteos().size(); i++) {
            for (j = solucion.getRuteos().get(i).getRanurasUsadas().size() - 1; j >= 0; j-- ) {
                solucion.getRuteos().get(i).getRanurasUsadas().remove(j);
            }
        }

        for (int i = 0; i < solucion.getRuteos().size(); i++) {

            rutaNro = solucion.getRuteos().get(i).getRutaNro();
            if (rutaNro > -1) {
                if (!asignarRanuras(solucion, rutaNro, demandasInfo)) {
                    solucion.setCantBloq(solucion.getCantBloq() + 1);
                    solucion.getRuteos().get(i).setRutaNro(-1);
                }
            } else {
                solucion.setCantBloq(solucion.getCantBloq() + 1);
            }

        }
    }

//    public static void calcularFitness (Solucion solucion, List<DemandaInfo> demandasInfo) {
//        DemandaInfo demandaInfo;
//        Double fitness = 0.0;
//
//        for (int i = 0; i < solucion.getRuteos().size(); i++) {
//            if (solucion.getRuteos().get(i).getRutaNro() > -1) {
//                demandaInfo = demandasInfo.get(solucion.getRuteos().get(i).getRutaNro());
//                fitness = fitness + demandaInfo.getX();
//            }
//        }
//
//        solucion.setFitness(fitness);
//    }

    public static List<Solucion> elegirMejores (List<Solucion> auxPoblacionActual, int cantSolucionesIniciales, List<Solucion> poblacionActual) {
            int frente = 0 ;
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //AQUI GUARDO EN UN AUXILIAR LA POBLACION R=PuQ, PARA PODER ELEGIR LOS MEJORES EN EL SIGUIENTE FOR
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            List<Solucion> auxPoblacionR = new ArrayList<>();
            auxPoblacionR.addAll(auxPoblacionActual);
            auxPoblacionActual = new ArrayList<>();    //REINICIO LA POBLACION ACTUAL PARA GUARDAR LOS MEJORES    

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
        
            //CON ESTE FOR LO QUE HAGO ES COPIAR TODOS LOS INDIVIDUOS QUE ESTAN EN EL PRIMER FRENTE DE PARETO
            for (int l = 0; l < cantSolucionesIniciales; l++) {
                if (auxPoblacionR.get(l).getPareto() == frente) {
                    auxPoblacionActual.add(auxPoblacionR.get(l));
                    auxPoblacionR.get(l).setIndiceRankeo(-99);  // A LOS DEL PRIMER GRUPO SELECCIONADO LE ASIGNO 
                                                               // -99 PARA LUEGO BORRARLOS
                }    
            }
            //AQUI LO QUE HAGO ES BORRAR TODOS LOS INDIVIDUOS DEL PRIMER GRUPO DE PARETO
            Iterator<Solucion> it = auxPoblacionR.iterator();
            while (it.hasNext()) {
                if (it.next().getIndiceRankeo() == -99) {
                    it.remove();
                }    
            }             
            frente++;
        //AQUI PREGUNTO SI LA POBLACION ACTUAL ESTÁ COMPLETA DE ACUERDO A LA CANTIDAD DE SOLUCIONES DESEADA
        while (auxPoblacionActual.size() != cantSolucionesIniciales) {
        //LUEGO TENGO QUE SELECCIONAR EL MEJOR INDIVIDUO QUE TENGA MEJOR DISTANCIA DE CROWDING
        //////////////////////////////////////////////////////////////////////////////////////////////////
        //AQUI CALCULO LA DISTANCIA DE CROWDING DE LOS DEMAS FRENTES DE PARETO QUE NO SON EL PRIMER FRENTE
        //SOLO SE CALCULA LA DISTANCIA POR CADA FRENTE
        //////////////////////////////////////////////////////////////////////////////////////////////////
            calcularDistanciaCrowding(auxPoblacionR);  //AQUI ENVIO EN CONJUNTO DE SOLUCIONES EN GRUPO
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // HASTA AQUI
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //EN ESTE FOR LO QUE HAGO ES ELEGIR EL MEJOR CROWDING DEL SIGUIENTE FRENTE CON
            //LA MEJOR DISTANCIA DE CROWDING 
            for (int s = 0; s < auxPoblacionR.size(); s++) {
                if (auxPoblacionR.get(s).getPareto() == frente  ) {
                    auxPoblacionActual.add(auxPoblacionR.get(s)) ;
                    auxPoblacionR.get(s).setIndiceRankeo(-99);
                    if (auxPoblacionActual.size() == cantSolucionesIniciales) {
                        s = auxPoblacionR.size();
                    }
                }
            }        
        }
        auxPoblacionR = new ArrayList<>();
        poblacionActual.addAll(auxPoblacionActual);

        return poblacionActual ;
 
    }
    
    
    public static void calcularDistanciaCrowding (List<Solucion> auxPoblacionActual) {
        
        List<Solucion> auxPoblacionOrdenada = new ArrayList<>();  //ESTE ARRAY LO CREO PARA TENER UN AUXILIAR DE LA 
                                                                  //POBLACION ACTUAL
        List<Solucion> auxFrenteIndividual = new ArrayList<>();   //ESTE ARRAY DE SOLUCIONES LO CREO PARA QUE PUEDA 
                                                                  //TIRAR EL GRUPO DE UN MISMO CONJUNTO PARETO

        double fMinCos, fMaxCos, fMinSal, fMaxSal, fMinEs, fMaxEs ;
        double auxCalculoCos, auxCalculoSal, auxCalculoEs ;
        int grupoPareto = 1 ;
        
        auxPoblacionOrdenada.addAll(auxPoblacionActual);
        auxPoblacionActual = new ArrayList<>();
        
////////////////////////////////////////////////
//AQUI TENGO QUE RECORRER EL GRUPO SELECCIONADO     
        while (!auxPoblacionOrdenada.isEmpty()) {
            //EN ESTE FOR LO QUE HAGO ES APARTAR EL SIGUIENTE FRENTE DE PARETO PARA HALLAR
            //LA DISTANCIA DE CROWDING 
            for (int sFrente = 0; sFrente < auxPoblacionOrdenada.size(); sFrente++) {
                if (auxPoblacionOrdenada.get(sFrente).getPareto() == grupoPareto  ) {
                    auxFrenteIndividual.add(auxPoblacionOrdenada.get(sFrente)) ;
                    auxPoblacionOrdenada.get(sFrente).setIndiceRankeo(-99);
                }
            }
            //AQUI LO QUE HAGO ES BORRAR TODOS LOS INDIVIDUOS DEL GRUPO DE PARETO
            Iterator<Solucion> it = auxPoblacionOrdenada.iterator();
            while (it.hasNext()) {
                if (it.next().getIndiceRankeo() == -99) {
                    it.remove();
                }    
            }            
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //AQUI EMPIEZO EL CALCULO DE LA DISTANCIA DE CROWDING DE TODOS LA POBLACION, EN ESTE CASO PARA 3 OBJETIVOS
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////       
                for (int e = 0; e < auxFrenteIndividual.size(); e++) {
                    auxFrenteIndividual.get(e).setPerimetroCuboide(0.0);
                }

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
            //CALCUAR LA DISTANCIA PARA EL OBJETIVO COSTO - OBJETIVO 1
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
            //AQUI ORDENAR EL CONJUNTO DE INDIVIDUOS auxPoblacionOrdenada POR COSTO
                ordenarPorCosto(auxFrenteIndividual) ;
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
               
                //AQUI SE LE ASIGNA A LOS EXTREMOS EL MAXIMO VALOR DEL OBJETO Double PARA EL OBJETIVO COSTO
                auxFrenteIndividual.get(0).setPerimetroCuboide(Double.MAX_VALUE);
                auxFrenteIndividual.get(auxFrenteIndividual.size()-1).setPerimetroCuboide(Double.MAX_VALUE);  
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        
                fMinCos = auxFrenteIndividual.get(0).getCosto() ;
                fMaxCos = auxFrenteIndividual.get(auxFrenteIndividual.size()-1).getCosto() ;

                for (int indiceCos = 1; indiceCos < auxFrenteIndividual.size()-1; indiceCos++) {
                    auxCalculoCos = Math.abs(auxFrenteIndividual.get(indiceCos + 1).getCosto() - auxFrenteIndividual.get(indiceCos - 1).getCosto());
                    auxCalculoCos = auxCalculoCos / Math.abs(fMaxCos - fMinCos) ;
                    auxFrenteIndividual.get(indiceCos).setPerimetroCuboide(auxFrenteIndividual.get(indiceCos).getPerimetroCuboide() + auxCalculoCos) ;
                }        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //HASTA AQUI OBJETIVO COSTO
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //CALCUAR LA DISTANCIA PARA EL OBJETIVO SALTO - OBJETIVO 2
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //AQUI ORDENAR EL CONJUNTO DE INDIVIDUOS auxPoblacionOrdenada POR SALTOS
                ordenarPorSaltos(auxFrenteIndividual) ;
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
       //AQUI SE LE ASIGNA A LOS EXTREMOS EL MAXIMO VALOR DEL OBJETO Double PARA EL OBJETIVO SALTOS
                auxFrenteIndividual.get(0).setPerimetroCuboide(Double.MAX_VALUE);
                auxFrenteIndividual.get(auxFrenteIndividual.size()-1).setPerimetroCuboide(Double.MAX_VALUE);  
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                fMinSal = auxFrenteIndividual.get(0).getSaltos() ;
                fMaxSal = auxFrenteIndividual.get(auxFrenteIndividual.size()-1).getSaltos() ;

                for (int indiceSal = 1; indiceSal < auxFrenteIndividual.size()-1; indiceSal++) {
                    auxCalculoSal = Math.abs(auxFrenteIndividual.get(indiceSal + 1).getSaltos() - auxFrenteIndividual.get(indiceSal - 1).getSaltos());
                    auxCalculoSal = auxCalculoSal / Math.abs(fMaxSal - fMinSal) ;
                    auxFrenteIndividual.get(indiceSal).setPerimetroCuboide(auxFrenteIndividual.get(indiceSal).getPerimetroCuboide() + auxCalculoSal) ;
                }        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //HASTA AQUI OBJETIVO SALTOS
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //CALCUAR LA DISTANCIA PARA EL OBJETIVO ESPECTRO - OBJETIVO 3
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //AQUI ORDENAR EL CONJUNTO DE INDIVIDUOS auxPoblacionOrdenada POR ESPECTRO
                ordenarPorEspectro(auxFrenteIndividual) ;
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       //AQUI SE LE ASIGNA A LOS EXTREMOS EL MAXIMO VALOR DEL OBJETO Double PARA EL OBJETIVO ESPECTRO
                auxFrenteIndividual.get(0).setPerimetroCuboide(Double.MAX_VALUE);
                auxFrenteIndividual.get(auxFrenteIndividual.size()-1).setPerimetroCuboide(Double.MAX_VALUE);  
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////        
                fMinEs = auxFrenteIndividual.get(0).getEspectro() ;
                fMaxEs = auxFrenteIndividual.get(auxFrenteIndividual.size()-1).getEspectro() ;

                for (int indiceEs = 1; indiceEs < auxFrenteIndividual.size()-1; indiceEs++) {
                    auxCalculoEs = Math.abs(auxFrenteIndividual.get(indiceEs + 1).getEspectro() - auxFrenteIndividual.get(indiceEs - 1).getEspectro());
                    auxCalculoEs = auxCalculoEs / Math.abs(fMaxEs - fMinEs) ;
                    auxFrenteIndividual.get(indiceEs).setPerimetroCuboide(auxFrenteIndividual.get(indiceEs).getPerimetroCuboide() + auxCalculoEs) ;
                }
                grupoPareto++;
            
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////          
        //HASTA AQUI OBJETIVO ESPECTRO
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////               indice++ ;
            auxPoblacionActual.addAll(auxFrenteIndividual);
            
            auxFrenteIndividual = new ArrayList<>(); //CON ESTO REINICIO EL FRENTE PARA EL SIGUIENTE CALCULO CROWDING
//HASTA AQUI            
////////////////////////////////////      
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////
//ORDENAMIENTO DE LOS DISTINTOS OBJETIVOS, EN MI CASO SON 3 OBJETIVOS
///////////////////////////////////////////////////////////////////////////////////////////////////    
    public static void ordenarPorCosto (List<Solucion> auxPoblacionOrdenada) {
        Collections.sort(auxPoblacionOrdenada, new Comparator<Solucion> () {
            @Override
            public int compare(Solucion uno, Solucion dos) {
                return uno.getCosto().compareTo(dos.getCosto()) ;
            }
        });
    }

    public static void ordenarPorSaltos (List<Solucion> auxPoblacionOrdenada) {
        Collections.sort(auxPoblacionOrdenada, new Comparator<Solucion> () {
            @Override
            public int compare(Solucion uno, Solucion dos) {
                return uno.getSaltos().compareTo(dos.getSaltos()) ;
            }
        });
    }
        
    public static void ordenarPorEspectro (List<Solucion> auxPoblacionOrdenada) {
        Collections.sort(auxPoblacionOrdenada, new Comparator<Solucion> () {
            @Override
            public int compare(Solucion uno, Solucion dos) {
                return uno.getEspectro().compareTo(dos.getEspectro()) ;
            }
        });
    }
///////////////////////////////////////////////////////////////////////////////////////////////////
//HASTA AQUI EL ORDENAMIENTO
///////////////////////////////////////////////////////////////////////////////////////////////////    
    
    public static void ordenarPorBloqueados (List<Solucion> soluciones) {

        Solucion aux;
        int j;

        for (int i = 0; i < soluciones.size(); i++) {
            for (j = i + 1; j < soluciones.size(); j++) {
                if (soluciones.get(i).getCantBloq() > soluciones.get(j).getCantBloq()){
                    aux = soluciones.get(i);
                    soluciones.set(i, soluciones.get(j));
                    soluciones.set(j, aux);
                }
            }
        }

    }

    public static void ordenarPorFitness (List<Solucion> soluciones) {

        List<Solucion> gruposBloq = new ArrayList<Solucion>();
        Solucion aux;
        int j, cantidad = 0, inicio = -1;

        if (soluciones.get(soluciones.size()/2).getCantBloq() == soluciones.get((soluciones.size()/2) -1 ).getCantBloq()) {
            for (int i = 0; i < soluciones.size(); i++) {
                if (soluciones.get(soluciones.size()/2).getCantBloq() == soluciones.get(i).getCantBloq()) {
                    if (inicio < 0) {
                        inicio = i;
                    }
                    cantidad++;
                    gruposBloq.add(soluciones.get(i));
                }
            }
        }

        for (int i = 0; i < cantidad; i++) {
            for (j = i + 1; j < cantidad; j++) {
                if (gruposBloq.get(i).getFitness() > gruposBloq.get(j).getFitness()){
                    aux = gruposBloq.get(i);
                    gruposBloq.set(i, gruposBloq.get(j));
                    gruposBloq.set(j, aux);
                }
            }
        }

        for (int i = 0; i < cantidad; i++) {
            soluciones.set(i + inicio, gruposBloq.get(i));
        }

    }

    /*
    * Metodos auxiliares
    *
    * Inicializa todas las ranuras de todos los enlaces a false = no utilizado
    *
    * */

    public static List<Enlace> generarListaInicialRanuras (int cantRanuras, List<List<Boolean>> topologia) {
        int i, j, k;
        Enlace enlace;
        List<Enlace> enlaces = new ArrayList<>();
        List<Boolean> ranuras;
        List<Integer> ranurasUsadas;

        for (i = 0; i < topologia.get(0).size(); i++ ) {
            for (j = 0; j < topologia.get(0).size(); j++ ) {
                if (topologia.get(i).get(j)){
                    enlace = new Enlace();
                    ranuras = new ArrayList<>(); //LISTADO DE RANURAS PARA LA ASIGNACIÓN DE ESPECTRO
                    ranurasUsadas = new ArrayList<>(); //LISTADO DE LA CANTIDAD USADA POR CADA RANURA
                    for (k = 0; k < cantRanuras; k++) {
                        ranuras.add(false); 
                        ranurasUsadas.add(0); //ESTO GENERA E INICIALIZA EL CONTADOR DE USO DE LAS RANURAS PARA
                                              //CALCULAR EL MAS USADO Y MENOS USADO     
                    }

                    enlace.setInicio(i);
                    enlace.setFin(j);
                    enlace.setRanuras(ranuras);
                    if ("mostUsed".equals(algoritmo)) {
                        enlace.setRanurasUsadas(ranurasUsadas);                    
                    }
                    else { 
                        enlace.setRanurasUsadas(ranurasUsadas);
                    }
                    enlaces.add(enlace);
                }
            }
        }

        return enlaces;
    }

    public static List<DemandaInfo> obtenerDemandasMayores(List<DemandaInfo> demandasInfo) {
        List<DemandaInfo> aux = new ArrayList<>();
        DemandaInfo demandaInfo;
        int i = 0;

        while (i < demandasInfo.size()) {
            demandaInfo = demandasInfo.get(i);
            while (i < demandasInfo.size() &&
                    demandaInfo.getOrigen() == demandasInfo.get(i).getOrigen() &&
                    demandaInfo.getDestino() == demandasInfo.get(i).getDestino() ) {
                if (demandasInfo.get(i).getX() > demandaInfo.getX()) {
                    demandaInfo = demandasInfo.get(i);
                }
                i++;
            }
            aux.add(demandaInfo);
        }

        return aux;
    }

//    public static void guardarEnArchivo(List<Solucion> conjuntoSoluciones, int corridaNumero, List<DemandaInfo> demandasInfo, String archivo, Long tiempoInicial) throws IOException {
//
//        int i, j, k, solucionNumero;
//        DemandaInfo demandaInfo;
//        int corridaNro = corridaNumero + 1;
//        File f = new File(pathArchivo + archivo + "-corrida_" + corridaNro + ".txt");
//        FileWriter fw = new FileWriter(f);
//        int tiempoTotal = 0;
//
//        try{
//            BufferedWriter bw = new BufferedWriter(fw);
//            PrintWriter wr = new PrintWriter(bw);
//
//            for (i = 0; i < conjuntoSoluciones.size(); i++) {
//                solucionNumero = i + 1;
//                wr.write("\nSolucion numero: " + solucionNumero);
//                wr.write("\nCantidad de bloqueados: " + conjuntoSoluciones.get(i).getCantBloq());
//                wr.write("\nFitness: " + conjuntoSoluciones.get(i).getFitness());
//                wr.write("\nRanuras \tOrigen \tDestino \tRuta");
//
//                for (j = 0; j < conjuntoSoluciones.get(i).getRuteos().size(); j++) {
//                    wr.write("\n\t\t" + conjuntoSoluciones.get(i).getRuteos().get(j).getOriden());
//                    wr.write("\t" + conjuntoSoluciones.get(i).getRuteos().get(j).getDestino());
//
//                    if (conjuntoSoluciones.get(i).getRuteos().get(j).getRutaNro() > -1) {
//                        demandaInfo = demandasInfo.get(conjuntoSoluciones.get(i).getRuteos().get(j).getRutaNro());
//
//                        wr.write("\t\t" + demandaInfo.getRuta().get(0));
//                        for (k = 1; k < demandaInfo.getRuta().size(); k++) {
//                            wr.write(" - " + demandaInfo.getRuta().get(k));
//                        }
//
//                        wr.write("\n" + conjuntoSoluciones.get(i).getRuteos().get(j).getRanurasUsadas().get(0));
//                        for (k = 1; k < conjuntoSoluciones.get(i).getRuteos().get(j).getRanurasUsadas().size(); k++) {
//                            wr.write("\n" + conjuntoSoluciones.get(i).getRuteos().get(j).getRanurasUsadas().get(k));
//                        }
//                    } else {
//                        wr.write("\t\tBloqueado");
//                        wr.write("\t\tBloqueado");
//                    }
//
//                }
//
//                wr.write("\n\n");
//
//            }
//
//            tiempoTotal = (int) (((System.currentTimeMillis() - tiempoInicial) / 100) / 60);
//            wr.write("\nTiempo Total: " + tiempoTotal);
//            wr.close();
//            bw.close();
//        } catch (IOException e){
//
//        }
//
//
//    }

    public static void obtenerRanurasLibres (List<Boolean> ranuras, List<Integer> posicionesLibres) {
        for (int i = 0; i < ranuras.size(); i++ ) {
            if (!ranuras.get(i)) {
                posicionesLibres.add(i);
            }
        }

    }

    public static void agregarBloqueado (Solucion solucion, DemandaInfo demandaInfo) {
        Ruteo ruteo = new Ruteo();

        ruteo.setDemandaId(demandaInfo.getDemandaId());
        ruteo.setOriden(demandaInfo.getOrigen());
        ruteo.setDestino(demandaInfo.getDestino());
        ruteo.setRutaNro(-1);

        solucion.getRuteos().add(ruteo);
    }

    // Suma las distancias de las rutas utilizadas en esta solucion
    public static Double getSaltoMayor (Solucion solucion, List<DemandaInfo> demandasInfo) {
        Double distancia = 0.0;
        for (int i = 0; i < solucion.getRuteos().size(); i++) {
            if (solucion.getRuteos().get(i).getRutaNro() > -1) {
                distancia = distancia +
                        (demandasInfo.get(solucion.getRuteos().get(i).getRutaNro()).getSaltos());
            }
        }

        return distancia;
    }

    // Suma las distancias de las rutas utilizadas en esta solucion
    public static Double getCostoDeLaSolucion (Solucion solucion, List<DemandaInfo> demandasInfo) {
        Double costo = 0.0;
        for (int i = 0; i < solucion.getRuteos().size(); i++) {
            if (solucion.getRuteos().get(i).getRutaNro() > -1) {
                costo = costo +
                        (demandasInfo.get(solucion.getRuteos().get(i).getRutaNro()).getSaltos() *
                                (demandasInfo.get(solucion.getRuteos().get(i).getRutaNro()).getTraf() + nivelDeModulacion));
            }
        }

        return costo;
    }

    public static Double getEspectroMayor (Solucion solucion) {
        Double mayor = 0.0;
        int j;
        for (int i = 0; i < solucion.getEnlaces().size(); i++) {
            for (j = 0; j < solucion.getEnlaces().get(i).getRanuras().size(); j++) {
                if (solucion.getEnlaces().get(i).getRanuras().get(j) && j > mayor) {
                    mayor = Double.valueOf(j);
                }
            }
        }

        return mayor;
    }


    public static Double getCostoMayor(List<DemandaInfo> demandaInfoList) {
        Double mayor = 0.0;
        for (int i = 0; i < demandaInfoList.size(); i++) {
            if (demandaInfoList.get(i).getX() > mayor) {
                mayor = Double.valueOf(demandaInfoList.get(i).getX());
            }
        }
        return mayor;
    }

    public static Long getSaltoMayorDeLaRed(List<DemandaInfo> demandaInfos, int k) {
        Long mayor = Long.valueOf(0);
        Long mayorSalto = Long.valueOf(0);

        for (int i = 0; i < demandaInfos.size(); i++) {
            for (int j = i; j < k + i; j++) {
                if (demandaInfos.get(j).getSaltos() > mayor) {
                    mayor = demandaInfos.get(j).getSaltos().longValue();
                }
            }
            mayorSalto = mayorSalto + mayor;
            i = (i + k) - 1;
            mayor = Long.valueOf(0);
        }

        return mayorSalto;
    }

    public static Double getCostoMayor (Solucion solucion, List<DemandaInfo> demandaInfos) {
        return solucion.getSaltos() * solucion.getEspectro();
    }

    public static void guardarEnArchivo(String path, List<Solucion> conjuntoSoluciones, int corridaNumero, Long tiempoDeEjecucion) throws IOException {

        int i, solucionNumero;
        int corridaNro = corridaNumero + 1;
        File f = new File(path + "corridaNro_" + corridaNro + ".txt");
        FileWriter fw = new FileWriter(f);

        try{
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter wr = new PrintWriter(bw);

            //Collections.sort(conjuntoSoluciones);
            for (i = 0; i < conjuntoSoluciones.size(); i++) {
                solucionNumero = i + 1;
                wr.write("\nSolucion numero: " + solucionNumero);
                
                //for (int m = 0; m < conjuntoSoluciones.get(m).getRuteos().size(); m++) {
                //    wr.write("\nRutas Seleccionadas: " + conjuntoSoluciones.get(i).getRuteos().get(m).getOriden() + 
                //            "-" + conjuntoSoluciones.get(i).getRuteos().get(m).getDestino());
                //}
                wr.write("\nCantidad de bloqueados: " + conjuntoSoluciones.get(i).getCantBloq());
                wr.write("\nFitness: " + conjuntoSoluciones.get(i).getFitness());
                wr.write("\nCosto: " + conjuntoSoluciones.get(i).getCosto());
                wr.write("\nDistancia: " + conjuntoSoluciones.get(i).getSaltos());
                wr.write("\nEspectro Mayor: " + conjuntoSoluciones.get(i).getEspectro());
                wr.write("\nConjunto Pareto (R): " + conjuntoSoluciones.get(i).getPareto());
                

                wr.write("\n\n");
            }

            wr.write("\nTiempo total de ejecucion: " + tiempoDeEjecucion + " minutos.");
            wr.close();
            bw.close();
        } catch (IOException e){

        }


    }

    public static void guardarMaximos (String archivo) throws IOException {
        File f = new File(archivo);
        FileWriter fw = new FileWriter(f);

        try{
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter wr = new PrintWriter(bw);

            wr.write(costoMayor.toString() + "\n");
            wr.write(saltoMayor.toString() + "\n");
            wr.write(espectroMayor.toString());
            wr.close();
            bw.close();
        } catch (IOException e){

        }
    }

    public static int getCostoMayorDeLaRed(List<DemandaInfo> demandaInfoList) {
        int costoMayor = 0;

        for (int i = 0; i < demandaInfoList.size(); i++) {
            if ((demandaInfoList.get(i).getTraf() + nivelDeModulacion) > costoMayor) {
                costoMayor = demandaInfoList.get(i).getTraf() + nivelDeModulacion;
            }
        }

        return costoMayor;
    }

    private static void rankeoPareto(List<Solucion> poblacionActual) {

        List<Integer> solucionesNoDominadas = new ArrayList<>();
        List<Solucion> solucionesParetoAux = new ArrayList<>(poblacionActual);
        
        poblacionActual = new ArrayList<>();
        //poblacionActual.addAll(solucionesCompletas);
        
        int indiceSolucionNoDominada = -1;
        boolean solucionDominada = true ;
        int grupoPareto = 0;
    
        while (!solucionesParetoAux.isEmpty()) {
            if (solucionesParetoAux.size() != 1) {
                for (int i = 0; i < solucionesParetoAux.size(); i++) {  //aqui recorro mi población tomo 1 y comparo con el resto
                    //if (solucionesParetoAux.get(i).getCantBloq() == 0) { ;
                        solucionDominada = true;
                        for (int j = 0; j < solucionesParetoAux.size(); j++) { //aqui recorro y comparo con el anterior
                            if (i != j) {
                                Double vEspectroj = solucionesParetoAux.get(j).getEspectro();
                                Double vEspectroi = solucionesParetoAux.get(i).getEspectro();
                                if (solucionesParetoAux.get(j).getCosto() <= solucionesParetoAux.get(i).getCosto() &&
                                    solucionesParetoAux.get(j).getSaltos() <= solucionesParetoAux.get(i).getSaltos() &&
                                    solucionesParetoAux.get(j).getEspectro() <= solucionesParetoAux.get(i).getEspectro() &&
                                    (solucionesParetoAux.get(j).getCosto() < solucionesParetoAux.get(i).getCosto() ||
                                    solucionesParetoAux.get(j).getSaltos() < solucionesParetoAux.get(i).getSaltos() ||
                                    solucionesParetoAux.get(j).getEspectro() < solucionesParetoAux.get(i).getEspectro())) {
                                    //ENTONCES j le domina a i 
                                    solucionDominada = true;
                                    j = solucionesParetoAux.size();
                                }   else {
                                        solucionDominada = false;
                                }   
                            } //endif
                        } //endfor j
                    //}    
                    //aqui poner el codigo de asignación final del conjunto de soluciones pareto
                    if (solucionDominada == false) {                      
                        solucionesParetoAux.get(i).setPareto(grupoPareto);
                        solucionesParetoAux.get(i).setIndiceRankeo(i);
                        poblacionActual.add(solucionesParetoAux.get(i));
                        solucionesNoDominadas.add(i) ;
                    }
                } //endfor i  
                //aqui ver la manera que una vez que se encuentre en primer grupo del frente de pareto, se excluya
                // los que forman parte del primer frente
                    grupoPareto = grupoPareto + 1;

                    Iterator<Solucion> it = solucionesParetoAux.iterator();
                    while (it.hasNext()) {
                        if (it.next().getIndiceRankeo() >= 0) {
                            it.remove();
                        }    
                    } 
                    solucionesNoDominadas = new ArrayList<>(); 
                    //
                //} 
            } else { 
                //aqui se inserta el ultimo grupo del conjunto de pareto que sobra
                //if (solucionDominada == true) {
                    indiceSolucionNoDominada = solucionesParetoAux.size()-1 ;
                    
                    
                    solucionesParetoAux.get(solucionesParetoAux.size()-1).setPareto(grupoPareto);
                    solucionesParetoAux.get(solucionesParetoAux.size()-1).setIndiceRankeo(grupoPareto);
                    poblacionActual.add(solucionesParetoAux.get(solucionesParetoAux.size()-1)); 

                    solucionesNoDominadas.add(solucionesParetoAux.size()-1); // -1 porque el tamaño no es igual al índice
                    for (int p = 0; p < solucionesNoDominadas.size(); p++) { //aqui recorro y comparo con el anterior
                        solucionesParetoAux.remove(p);
                    }
                    solucionesNoDominadas = new ArrayList<>(); 
                //}
            } 
        }
    }
    
    public static boolean esImpar(Integer iNumero) {
        if (iNumero%2 != 0)
            return true;
        else
            return false;
    }
}

