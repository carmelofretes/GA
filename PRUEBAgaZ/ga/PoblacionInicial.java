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

    public static void main(String [] args) throws IOException {
        String pathInicial = "C:\\Users\\ysapymimbi\\IdeaProjects\\tesis\\src\\ga\\archivos\\nsf\\";
        List<List<Solucion>> todosLosConjuntos = new ArrayList<>();
        List<List<Boolean>> topologia = AGHelper.leerTopologia(pathInicial);
        int cantLlamadasGA = AGHelper.leerParametro(pathInicial, "cantidad de corridas independientes");
        int cantSolucionesIniciales = AGHelper.leerParametro(pathInicial, "cantidad de cromosomas");
        int totalRanuras = AGHelper.leerParametro(pathInicial, "cantidad de longitudes de onda por fibra");
        int criterioDeParada = AGHelper.leerParametro(pathInicial, "criterio de parada");
        double probabMutacion = AGHelper.leerProbabMutacion(pathInicial, "probabilidad de mutacion");

        List<Integer> cantDemandas = new ArrayList<>();
        cantDemandas.add(50);
        cantDemandas.add(100);
        cantDemandas.add(150);
        cantDemandas.add(200);
        int k = 6;
        int cantCantidadDeDemandas = 4;
        List<DemandaInfo> demandaInfoList = new ArrayList<>();
        String archivoDeMaximos;
        String pathActual;
        Calendar cal = Calendar.getInstance();
        int horaInicial;
        int minutoInicial;
        int segundoInicial;

        for (int a = 2; a < k; a++) {
            for (int d = 0; d < cantCantidadDeDemandas; d++) {
                pathActual = pathInicial + "k" + (a+1) + "\\cantSolicitada" + cantDemandas.get(d) + "\\";
                archivoDeMaximos = pathActual + "maximos.txt";
                demandaInfoList.addAll(llenarDemandInfo(pathActual + "ga.txt"));
                saltoMayor = getSaltoMayorDeLaRed(demandaInfoList, a + 1);
                costoMayor = (cantDemandas.get(d) + 1) * saltoMayor; // el 1 es agregado para banda guarda
                espectroMayor = Long.valueOf(totalRanuras);
                guardarMaximos(archivoDeMaximos);

                for (int i = 0; i < cantLlamadasGA; i++) {
                    cal = Calendar.getInstance();
                    horaInicial = cal.get(Calendar.HOUR);
                    minutoInicial = cal.get(Calendar.MINUTE);
                    segundoInicial = cal.get(Calendar.SECOND);
                    todosLosConjuntos.add(ga(topologia, cantSolucionesIniciales, totalRanuras, criterioDeParada, probabMutacion, demandaInfoList));
                    // guardar tambien las rutas y ranuras elegidas
                    guardarEnArchivo(pathActual, todosLosConjuntos.get(i), i, horaInicial, minutoInicial, segundoInicial);

                    long minRunningMemory = (1024*1024);

                    Runtime runtime = Runtime.getRuntime();

                    if(runtime.freeMemory()<minRunningMemory)
                        System.gc();
                }
                demandaInfoList = new ArrayList<>();
                todosLosConjuntos = new ArrayList<>();
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
        String demandaNro;
        boolean bloqueado;

        // selecciono demanda por demanda en el orden ya establecido del 30% y 70%
        // de cada demanda, traigo la ruta mas corta de demandasInfo e intento asignarle la cantidad de ranuras que quiere
        // si no puede con esa ruta, intenta con la siguiente ruta mas corta, etc
        // y si no puede asignar las ranuras con ninguna de sus rutas, suma una demanda bloqueada a solucion
        for (DemandaInfo demanda : mayores) {

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
        solucion.setCosto(solucion.getSaltos() * demandasInfo.get(0).getTraf());
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
        for (int i = 0; i < solucion.getEnlaces().size(); i++) {
            if (solucion.getEnlaces().get(i).getInicio() == nodoPrimero && solucion.getEnlaces().get(i).getFin() == nodoSegundo){
                ranuras = solucion.getEnlaces().get(i).getRanuras();
                break;
            }
        }

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

    }

    public static void agregarRanurasASolucion(Solucion solucion, int rutaNro, int primeraRanura, List<DemandaInfo> demandasInfo) {

        DemandaInfo demandaInfo = demandasInfo.get(rutaNro);
        int ranurasSolicitadas = demandaInfo.getTraf();
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

            for (j = primeraRanura; j < ranurasSolicitadas + primeraRanura; j++) {
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

    public static List<Solucion> ga(List<List<Boolean>> topologia, int cantSolucionesIniciales, int totalRanuras, int criterioDeParada, double probabilidaMutacion, List<DemandaInfo> demandasInfo) throws FileNotFoundException {

        int j, reproductor1, reproductor2, candidato1 = -1, candidato2 = -1;
        Solucion hijo1, hijo2; // inicializar con enlaces con ranuras vacias

        List<Solucion> poblacionInicial = generarPoblacionInicial(topologia, cantSolucionesIniciales, totalRanuras, demandasInfo);
        List<Solucion> poblacionActual = new ArrayList<>();
        poblacionActual.addAll(poblacionInicial);
        List<Solucion> poblacionNueva;
        List<Solucion> aux ;
        Random rnd = new Random();

        Calendar cal = Calendar.getInstance();
        int minutoActual = cal.get(Calendar.MINUTE);
        int horaMaxima;
        int minutoMaximo = cal.get(Calendar.MINUTE) + criterioDeParada;

        if (minutoActual > 55) {
            horaMaxima = cal.get(Calendar.HOUR) + 1;
            minutoMaximo = (minutoActual + criterioDeParada) - 60;
        } else {
            horaMaxima = cal.get(Calendar.HOUR);
            minutoMaximo = minutoActual + criterioDeParada;
        }

        while ((minutoActual > 55 && (cal.get(Calendar.HOUR) < horaMaxima && cal.get(Calendar.MINUTE) > minutoMaximo) ||
                (cal.get(Calendar.HOUR) == horaMaxima && cal.get(Calendar.MINUTE) < minutoMaximo)) ||
                (minutoActual <= 55 && cal.get(Calendar.HOUR) == horaMaxima && cal.get(Calendar.MINUTE) < minutoMaximo)
                ) {
            aux = new ArrayList<>();
            poblacionNueva = new ArrayList<>();

            for (j = 0; j < poblacionInicial.size()/4; j++){

                hijo1 = new Solucion();
                hijo2 = new Solucion();

                // elegir reproductores aleatoriamente
                // elegir 2 candidatos aleatoriamente, de los 2 candidatos tomar el de mejor fitness para ser un reproductor
                while(candidato1 == candidato2){
                    candidato1 = (int)(rnd.nextDouble() * poblacionActual.size());
                    candidato2 = (int)(rnd.nextDouble() * poblacionActual.size());
                }

                if (poblacionActual.get(candidato1).getFitness() > poblacionActual.get(candidato2).getFitness()) {
                    reproductor1 = candidato2;
                } else {
                    reproductor1 = candidato1;
                }

                while(candidato1 == candidato2 || candidato1 == reproductor1 || candidato2 == reproductor1){
                    candidato1 = (int)(rnd.nextDouble() * poblacionActual.size());
                    candidato2 = (int)(rnd.nextDouble() * poblacionActual.size());
                }

                if (poblacionActual.get(candidato1).getFitness() > poblacionActual.get(candidato2).getFitness()) {
                    reproductor2 = candidato2;
                } else {
                    reproductor2 = candidato1;
                }

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

                mutacion (hijo1, probabilidaMutacion, topologia, totalRanuras, demandasInfo);
                mutacion (hijo2, probabilidaMutacion, topologia, totalRanuras, demandasInfo);

                poblacionNueva.add(hijo1);
                poblacionNueva.add(hijo2);

                candidato1 = -1;
                candidato2 = -1;

            }

            // de la poblacion vieja elegir solo las 2 primeras soluciones, las mejores y copiar a nueva poblacion
            // para el resto de mi poblacion nueva, cruzar el resto de las soluciones de la poblacion vieja
            elegirMejores (poblacionActual, aux, poblacionNueva);

            cal = Calendar.getInstance();
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

        int puntoDeCruce1 = rnd.nextInt(reproductor1.getRuteos().size() - 3) + 0;
        int puntoDeCruce2 = rnd.nextInt((reproductor1.getRuteos().size() - 2) - (puntoDeCruce1 + 1) + 1) + (puntoDeCruce1 + 1);

        for (int i = 0; i <= puntoDeCruce1; i++) {
            ruteo = new Ruteo();
            reproductor1.getRuteos().get(i).clonar(ruteo);
            ruteo1.add(ruteo);
            ruteo = new Ruteo();
            reproductor2.getRuteos().get(i).clonar(ruteo);
            ruteo2.add(ruteo);
        }

        for (int i = puntoDeCruce1 + 1; i <= puntoDeCruce2; i++) {
            ruteo = new Ruteo();
            reproductor2.getRuteos().get(i).clonar(ruteo);
            ruteo1.add(ruteo);
            ruteo = new Ruteo();
            reproductor1.getRuteos().get(i).clonar(ruteo);
            ruteo2.add(ruteo);
        }

        for (int i = puntoDeCruce2 + 1; i < reproductor1.getRuteos().size(); i++) {
            ruteo = new Ruteo();
            reproductor1.getRuteos().get(i).clonar(ruteo);
            ruteo1.add(ruteo);
            ruteo = new Ruteo();
            reproductor2.getRuteos().get(i).clonar(ruteo);
            ruteo2.add(ruteo);
        }

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
        hijo1.setCosto(hijo1.getSaltos() * demandasInfo.get(0).getTraf());
        hijo1.setFitness(hijo1.getCosto()/costoMayor + hijo1.getSaltos()/saltoMayor + hijo1.getEspectro()/espectroMayor);

//        hijo2.setSaltos(getSaltoMayor(hijo2, demandasInfo)/saltoMayor);
        hijo2.setSaltos(getSaltoMayor(hijo2, demandasInfo));
//        hijo2.setEspectro(getEspectroMayor(hijo2)/espectroMayor);
        hijo2.setEspectro(getEspectroMayor(hijo2));
//        hijo2.setCosto(hijo2.getFitness()/costoMayor);
        hijo2.setCosto(hijo2.getSaltos() * demandasInfo.get(0).getTraf());

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
        solucion.setCosto(solucion.getSaltos() * demandasInfo.get(0).getTraf());

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

        solucionesCompletas.addAll(poblacionActual);
        solucionesCompletas.addAll(poblacionNueva);
        ordenarPorBloqueados(solucionesCompletas);

        poblacionActual = new ArrayList<>();
        poblacionActual.addAll(solucionesCompletas);

        ordenarPorFitness (poblacionActual);

        for (int i = 0; i < cantSoluciones; i++) {
            poblacionResultado.add(poblacionActual.get(i));
        }

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
        List<Boolean> ranuras = new ArrayList<>();

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
                distancia = distancia + demandasInfo.get(solucion.getRuteos().get(i).getRutaNro()).getSaltos();
            }
        }

        return distancia;
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

    public static void guardarEnArchivo(String path, List<Solucion> conjuntoSoluciones, int corridaNumero, int horaInicial, int minutoInicial, int segundoInicial) throws IOException {

        int i, j, k, solucionNumero;
        DemandaInfo demandaInfo;
        int corridaNro = corridaNumero + 1;
        File f = new File(path + "corridaNro_" + corridaNro + ".txt");
        FileWriter fw = new FileWriter(f);
        int tiempoTotal = 0;

        try{
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter wr = new PrintWriter(bw);

            for (i = 0; i < conjuntoSoluciones.size(); i++) {
                solucionNumero = i + 1;
                wr.write("\nSolucion numero: " + solucionNumero);
                wr.write("\nCantidad de bloqueados: " + conjuntoSoluciones.get(i).getCantBloq());
                wr.write("\nCosto: " + conjuntoSoluciones.get(i).getCosto());
                wr.write("\nDistancia: " + conjuntoSoluciones.get(i).getSaltos());
                wr.write("\nEspectro Mayor: " + conjuntoSoluciones.get(i).getEspectro());
                wr.write("\nFitness: " + conjuntoSoluciones.get(i).getFitness());

                wr.write("\n\n");

            }
            Calendar cal = Calendar.getInstance();
            int horaFinal = cal.get(Calendar.HOUR);
            int minutoFinal = cal.get(Calendar.MINUTE);
            int segundoFinal = cal.get(Calendar.SECOND);

            wr.write("\nTiempo inicial: " + horaInicial + ":" + minutoInicial + ":" + segundoInicial);
            wr.write("\nTiempo final: " + horaFinal + ":" + minutoFinal + ":" + segundoFinal);
            wr.write("\nDiferencia: " + (horaFinal-horaInicial) + ":" + (minutoFinal-minutoInicial) + ":" + (segundoFinal-segundoInicial));
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

}
