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
    private static String algoritmo = "moga1";
    private static String topologia = "tipoRed";
    private static List<String> tiposDeCarga = new ArrayList<>();

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
            int k = 2;   //Esta es la cantidad de caminos que tiene la demanda.
            int cantCantidadDeDemandas = 3;
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
        demandasMayores.addAll(definirOrdenDemandas(demandaInfoList));

        for (int i = 0; i < cantSolucionesIniciales; i++) {
            poblacionInicial.add(generarSolucion(totalRanuras, topologia, demandasMayores, demandaInfoList));
        }

        return poblacionInicial;
    }

    public static List<DemandaInfo> definirOrdenDemandas (List<DemandaInfo> demandasInfo) {
        int i, r;

        List<DemandaInfo> mayores = new ArrayList<DemandaInfo>();
        List<DemandaInfo> aux;
        List<DemandaInfo> infoCopy = new ArrayList<DemandaInfo>();

        // traer la lista de demandasInfo con las filas que tienen la mayor X de cada demanda
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

    public static Solucion generarSolucion(int cantRanuras, List<List<Boolean>> topologia, List<DemandaInfo> mayores, List<DemandaInfo> demandasInfo) {
        List<Enlace> enlacesIniciales = generarListaInicialRanuras (cantRanuras, topologia);
        Solucion solucion = new Solucion(enlacesIniciales); // inicializar con una lista de enlaces en la cual todas sus ranuras estan libres
        int j;
        boolean bloqueado;

        // selecciono demanda por demanda en el orden ya establecido del 30% y 70%
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

        // recorrer todos los enlaces, y guardar en una lista de listas las ranuras que correspondan a los enlaces de mi ruta.
        // en la lista lo que voy a guardar es si mi ranura R == 1, elimino la posicion que contenga
        // si es R == 0, agrego a mi map un
//        for (int i = 0; i < solucion.getEnlaces().size(); i++) {
//            if (solucion.getEnlaces().get(i).getInicio() == nodoPrimero && solucion.getEnlaces().get(i).getFin() == nodoSegundo){
//                ranuras = solucion.getEnlaces().get(i).getRanuras();
//                break;
//            }
//        }

        // ESTO aplica para GA 1
        /*
        // Una vez que tenga la lista, del primer enlace busco su primera ranura libre, verifico que las X siguientes esten libres.
        // Si es asi, agrego esa primera ranura a una una lista de posibles soluciones. Aplico esto para todas las siguientes ranuras libres.
        // Para el siguiente enlace, obtengo de la lista de posibles soluciones la primera ranura, verifico si esa ranura existe en mi lista de ranuras,

        obtenerRanurasLibres(ranuras, posicionesLibres);
        cantRanurasLibres = posicionesLibres.size();

        for (int i = 0; i < cantRanurasLibres; i++) {
            r = (int)(Math.random() * posicionesLibres.size());
            aplica = aplicaRanuras(posicionesLibres.get(r), rutaNro, solucion.getEnlaces(), demandasInfo);

            if (aplica){
                agregarRanurasASolucion(solucion, rutaNro, posicionesLibres.get(r), demandasInfo);
                return true;
            }
            posicionesLibres.remove(r);
            cantRanurasLibres--;
        }

        return false;
        */

        List<Integer> ranurasDisponibles = new ArrayList<>();
        ranurasDisponibles.addAll(obtenerRanurasDisponiblesParaRuta(solucion, demandaInfo));
        // elegir randomicamente una posicion e ranurasDisponibles
        // asignar esa ranura a la solucion para esta demanda

        if (ranurasDisponibles.isEmpty())
            return false;
        else {
            if ("moga1".equals(algoritmo)) {
                // elegir enlaces aleatoriamente
                int ranuraElegida = (int) (Math.random()*(ranurasDisponibles.size()-1));
                agregarRanurasASolucion(solucion, rutaNro, demandaInfo, ranurasDisponibles.get(ranuraElegida));

            } else if ("moga2".equals(algoritmo)) {
                // elegir ranuras con first fit
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

        for (int i = 0; i < solucion.getEnlaces().size(); i++) {
            for (int j = 0; j < (demandaInfo.getRuta().size()-1); j++) {
                origen = demandaInfo.getRuta().get(j);
                destino = demandaInfo.getRuta().get(j + 1);

                if (solucion.getEnlaces().get(i).getInicio() == origen &&
                        solucion.getEnlaces().get(i).getFin() == destino) {
                    enlaces.add(solucion.getEnlaces().get(i));
                }
            }
        }

        List<Integer> indiceDeRanurasLibres = new ArrayList<>();

        // obtener ranuras libres del primer enlace
        for (int i = 0; i < enlaces.get(0).getRanuras().size(); i++) {
            if (ranuraEsSolucion(enlaces.get(0).getRanuras(), i, demandaInfo.getTraf() + nivelDeModulacion)) {
                indiceDeRanurasLibres.add(i);
            }
        }

        for (int i = 1; i < enlaces.size(); i++) {
            List<Integer> copiaIndiceDeRanurasLibres = new ArrayList<>();
            copiaIndiceDeRanurasLibres.addAll(indiceDeRanurasLibres);
            for (int j = 0; j < indiceDeRanurasLibres.size(); j++) {
                if (!ranuraEsSolucion(enlaces.get(i).getRanuras(), indiceDeRanurasLibres.get(j), demandaInfo.getTraf() + nivelDeModulacion)) {
                    copiaIndiceDeRanurasLibres.remove(indiceDeRanurasLibres.get(j));
                }
            }
            indiceDeRanurasLibres = new ArrayList<>();
            indiceDeRanurasLibres.addAll(copiaIndiceDeRanurasLibres);
        }

        return indiceDeRanurasLibres;
    }

    public static boolean ranuraEsSolucion(List<Boolean> ranuras, int ranura, int cantSolicitada) {
        if (!ranuras.get(ranura) && ranuras.size() > (ranura + cantSolicitada)) {
            for (int i = (ranura + 1); i < (ranura + cantSolicitada); i++) {
                if (ranuras.get(i)) {
                    return false;
                }
            }
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

            for (j = primeraRanura; j < (ranurasSolicitadas + primeraRanura); j++) {
                solucion.getEnlaces().get(ubicacion).getRanuras().set(j, true);
            }
        }

        // crea el objeto que cubre esta demanda y agrega a solucion
        for (int i = 0; i < solucion.getRuteos().size(); i++) {
            if (solucion.getRuteos().get(i).getOriden() == demandaInfo.getOrigen() &&
                    solucion.getRuteos().get(i).getDestino() == demandaInfo.getDestino()) {
                existe = true;
                posicion = i;
                break;
            }
        }

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
        //Aqui se genera la población inicial
        List<Solucion> poblacionInicial = generarPoblacionInicial(topologia, cantSolucionesIniciales, totalRanuras, demandasInfo);
        //----------------------------------------------
        List<Solucion> poblacionActual = new ArrayList<>();
        poblacionActual.addAll(poblacionInicial);
        List<Solucion> poblacionNueva;
        List<Solucion> aux ;
        Random rnd = new Random();
        Long TInicio;
        Long TFin; //Variables para determinar el tiempo de ejecución

        TInicio = System.currentTimeMillis();

        if (k == 1) {
            rankeoPareto(poblacionActual);
            return poblacionActual;
            //return poblacionInicial;
        }

        int v = 1;
        TFin = System.currentTimeMillis();
        //int tiempoLimite = criterioDeParada * 60 * 1000;    //ESTE ES EL ANTERIOR
        int tiempoLimite = criterioDeParada * 60 * 1000;

        while ((TFin - TInicio) < tiempoLimite) {

//            System.out.println(v + ". Inicio: " + TInicio + ", Fin: " + TFin + ", Dif: " + (TFin - TInicio) + ", sigue: " + ((TFin - TInicio) < (240000)));
            aux = new ArrayList<>();
            poblacionNueva = new ArrayList<>();

            for (j = 0; j < poblacionInicial.size()/4; j++){  //PORQUE DIVIDE ENTRE 4

                hijo1 = new Solucion();
                hijo2 = new Solucion();

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
                
//AQUI SE APLICA EL CRUCE
                cruce (poblacionActual.get(reproductor1), poblacionActual.get(reproductor2), hijo1, hijo2, topologia, totalRanuras, demandasInfo);

                // mantener poblacion actual para comparar con los hijos al final
                // pero eliminar los reproductores ya elegidos para no repetir
                aux.add(poblacionActual.get(reproductor1));
                aux.add(poblacionActual.get(reproductor2));

                if (reproductor1 < reproductor2){
                    poblacionActual.remove(reproductor2);
                    poblacionActual.remove(reproductor1);
                } else {
                    poblacionActual.remove(reproductor1);
                    poblacionActual.remove(reproductor2);
                }
                
//AQUI SE REALIZA LA MUTACIÓN
                mutacion (hijo1, probabilidaMutacion, topologia, totalRanuras, demandasInfo);
                mutacion (hijo2, probabilidaMutacion, topologia, totalRanuras, demandasInfo);

                poblacionNueva.add(hijo1);
                poblacionNueva.add(hijo2);

                candidato1 = -1;
                candidato2 = -1;

            }

            // de la poblacion vieja elegir solo las 2 primeras soluciones, las mejores y copiar a nueva poblacion
            // para el resto de mi poblacion nueva, cruzar el resto de las soluciones de la poblacion vieja
            // aqui se envían, la poblacionActual, los dos individuos mejores de la poblacionActual que se eligieron
            // para el cruce, mutación y por último la poblacionNueva (resultado del cruce, mutación)
            //elegirMejores (poblacionActual, aux, poblacionNueva);
            //AQUI ARMO R = P U Q
            poblacionActual.addAll(aux);
            poblacionActual.addAll(poblacionNueva);
                 
            rankeoPareto(poblacionActual); //AQUI SE SE EL CONJUNTO DE SOLUCIONES QUIEN ES DOMINADO Y QUIEN NO
            Collections.sort(poblacionActual);  //AQUI SE ORDENA EL COJUNTO ANTERIOR
            
            List<Solucion> auxPoblacionActual = new ArrayList<>();
            auxPoblacionActual.addAll(poblacionActual);
            
            poblacionActual = new ArrayList<>();
            
            //EN ESTE FOR LO QUE HACEMOS ES SELECCIONAR LOS cantSolucionesIniciales DEL RANKEO POR PARETO
            for (int l = 0; l < cantSolucionesIniciales; l++) {
                poblacionActual.add(auxPoblacionActual.get(l));
            }
            
            TFin = System.currentTimeMillis();
            v++;
        }

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

    public static void elegirMejores (List<Solucion> poblacionResultado, List<Solucion> poblacionActual, List<Solucion> poblacionNueva) {
        int cantSoluciones = poblacionActual.size();
        List<Solucion> solucionesCompletas = new ArrayList<>();

        
//AQUI ARMO MI CONJUNTO DE SOLUCIONES R = P u Q       
        solucionesCompletas.addAll(poblacionActual);
        solucionesCompletas.addAll(poblacionNueva);
        solucionesCompletas.addAll(poblacionResultado);
        
        poblacionActual = new ArrayList<>();
        poblacionActual.addAll(solucionesCompletas);
        poblacionResultado = new ArrayList<>();
        poblacionResultado.addAll(poblacionActual);
        
//ESTO YO NO VOY A USAR///////////////////////////////////////////        
//       ordenarPorBloqueados(solucionesCompletas);
//        ordenarPorFitness (poblacionActual);
//       for (int i = 0; i < cantSoluciones; i++) {
//            poblacionResultado.add(poblacionActual.get(i));
//        }
////////////////////////////////////////////////////////////////

//AQUI APLICAR EL RANKEO POR DOMINANCIA DE PARETO

            /////////////////////////////////////
            //seleccionar los mejores por dominancia
            /////////////////////////////////////
        //1- clasificar  solucion por dominancia en su conjunto no dominado (R0 < R1 < R2 ... Rn
        //conjuntoPareto(poblacionResultado);
        
        
        //2- Clasificacion por distancia de crowding
        //Pa calcular fmaxm, fminm        m= 1,2,3 de Ri
        // Pb Aplicar la formula de distancia de crowding
        
    }

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

        for (i = 0; i < topologia.get(0).size(); i++ ) {
            for (j = 0; j < topologia.get(0).size(); j++ ) {
                if (topologia.get(i).get(j)){
                    enlace = new Enlace();
                    ranuras = new ArrayList<>();
                    for (k = 0; k < cantRanuras; k++) {
                        ranuras.add(false);
                    }

                    enlace.setInicio(i);
                    enlace.setFin(j);
                    enlace.setRanuras(ranuras);

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
                
                for (int m = 0; m < conjuntoSoluciones.get(m).getRuteos().size(); m++) {
                    wr.write("\nRutas Seleccionadas: " + conjuntoSoluciones.get(m).getRuteos().get(m).getOriden() + 
                            "-" + conjuntoSoluciones.get(m).getRuteos().get(m).getDestino());
                }
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

        List<Solucion> solucionesPareto = new ArrayList<>();
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
                        poblacionActual.add(solucionesParetoAux.get(i));
                        indiceSolucionNoDominada = i ;
                        //AQUI SE APLICA EL RANKEO DE LAS SOLUCIONES
                        //RANKEAR
                        //i = solucionesParetoAux.size();
                    }
                } //endfor i  
                //aqui ver la manera que una vez que se encuentre en primer grupo del frente de pareto, se excluya
                // los que forman parte del primer frente
                //if (solucionDominada == false) {
                    solucionesParetoAux.remove(indiceSolucionNoDominada);
                    grupoPareto = grupoPareto + 1;
                //} 
            } else { 
                //aqui se inserta el ultimo grupo del conjunto de pareto que sobra
                //if (solucionDominada == true) {
                    indiceSolucionNoDominada = solucionesParetoAux.size()-1 ;
                    solucionesParetoAux.get(solucionesParetoAux.size()-1).setPareto(grupoPareto);
                    poblacionActual.add(solucionesParetoAux.get(solucionesParetoAux.size()-1)); 
                    solucionesParetoAux.remove(indiceSolucionNoDominada);
                //}
            } 
        }
    }
}

