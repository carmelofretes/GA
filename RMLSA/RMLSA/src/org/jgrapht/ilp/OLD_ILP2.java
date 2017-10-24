/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jgrapht.ilp;

/**
 *
 * @author Ivan
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.*;

public class OLD_ILP2 {

    public static void main(String args[]) throws IOException {
        FileReader fr;


        //Configuracion config = leerConfiguraciones();
        // Definimos e instanciamos el grafo con pesos
        SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph = new SimpleDirectedGraph(DefaultWeightedEdge.class);
        
        int cantEnlaces = cargarGrafo(directedGraph);
        
        Integer cantSolicitudes = 3;
        //Aqui se crea una lista con las cantidades solicitadas y se agregan 
        List<Integer> cantSolicitadas = new ArrayList<>();
        cantSolicitadas.add(50);
        cantSolicitadas.add(35);
        cantSolicitadas.add(100);
       
//        hacer el doble for para las k y solicitudes
        String alfa = "";
        int kTotales = 1;
        for (int i = 0; i < kTotales; i++) {
            for (int j = 0; j < cantSolicitadas.size(); j++) {
                //Aqui se hace una lectura del archivo de solicitudes de conexion
                String path = "G:\\Genetico\\ga\\src\\ga\\archivos\\tipoRed\\k" + (i + 1) + "\\" + 
                        "cantSolicitada" + (cantSolicitadas.get(j)) + "\\";
                String solicitudesPath = path + "solicitudes.txt";
                
                // esto es para los parametros del ag
                String salidaPath = "G:\\Genetico\\ga\\src\\ga\\archivos\\tipoRed\\k" + (i+1) + 
                                    "\\cantSolicitada" + (50 * (j+1)) + "\\ga.txt";
                
                 
                alfa = leerSolicitudes(directedGraph, i + 1, solicitudesPath, path, salidaPath);
                String saltos = leerSaltos(path); //lee la distancia
                List<String> maximos = leerMaximos(path);
                
                //el anterior
                //String pathILP= "G:\\Genetico\\opl\\ILP2_2\\tipoRed\\cargaUniforme\\k" + (i + 1) + "\\cantSolicitada" + (cantSolicitadas.get(j));
                String pathILP= "G:\\Genetico\\opl\\ILP2_3";
                FileWriter fw = new FileWriter(pathILP + "\\ILP2_3.dat");

                BufferedWriter bw = new BufferedWriter(fw);
                try (PrintWriter salida = new PrintWriter(bw)) {
                    System.out.println("K = " + (i + 1) + ";"); //esta variable son las k solicitudes
                    salida.println("K = " + (i + 1) + ";");
                    System.out.println("SD = " + cantSolicitudes + ";");//variable de cantidad de solicitudes
                    salida.println("SD = " + cantSolicitudes + ";");
                    System.out.println("E = " + cantEnlaces + ";"); //esta variable es la cantidad de enlaces del grafo
                    salida.println("E = " + cantEnlaces + ";");
                    System.out.println("G = " + 1 + ";");
                    salida.println("G = " + 1 + ";");
                    
                    //del archivo G:\Genetico\ga\src\ga\archivos\tipoRed\k1\cantSolicitada35\maximos.txt
                    System.out.println("cost_max = " + maximos.get(0) + ";"); // la primera fila
                    salida.println("cost_max = " + maximos.get(0) + ";");

                    System.out.println("spectrum_max = " + maximos.get(2) + ";"); //la tercera fila
                    salida.println("spectrum_max = " + maximos.get(2) + ";");

                    System.out.println("dist_max = " + maximos.get(1) + ";"); //la segunda fila
                    salida.println("dist_max = " + maximos.get(1) + ";");

                    System.out.println("alfa = " + alfa);
                    salida.println("alfa = " + alfa);
                    
                    //del archivo G:\Genetico\ga\src\ga\archivos\tipoRed\k1\cantSolicitada35\saltos.txt
                    System.out.println("dist = " + saltos);
                    salida.println("dist = " + saltos);

//                     System.out.println("Longitud del vector saltos es " + saltos.length());

                    ArrayList<ArrayList<String>> listaTotalCaminos = leerTotalCaminos();
                    Set<DefaultWeightedEdge> listaTotalEnlaces = directedGraph.edgeSet();
//                    ArrayList<ArrayList<String>> listaTotalCaminos = directedGraph.edgeSet();

//                            System.out.println("l = [");
                            salida.println("l = [");
                            for (Iterator<ArrayList<String>> it = listaTotalCaminos.iterator(); it.hasNext();) {
                                ArrayList<String> camino = it.next();
//                                System.out.print("[");
                                salida.print("[");
                                for (Iterator<ArrayList<String>> it4 = listaTotalCaminos.iterator(); it4.hasNext();) {
                                    ArrayList<String> caminoComparador = it4.next();
                                    String result = "0";
                                    for (String enlace : camino) {

                                        if (caminoComparador.contains(enlace)) {
                                            result = "1";
                                            break;
                                        }

                                    }
                                    if (it4.hasNext()) {
//                                        System.out.print(result + ",");
                                        salida.print(result + ",");
                                    } else {
//                                        System.out.print(result);
                                        salida.print(result);
                                    }
                                }
                                if (it.hasNext()) {
//                                    System.out.println("],");
                                    salida.println("],");
                                } else {
//                                    System.out.println("]];");
                                    salida.println("]];");
                                }
                            }


//                    System.out.println("R = [");
                    salida.println("R = [");


                    for (Iterator<ArrayList<String>> it4 = listaTotalCaminos.iterator(); it4.hasNext();) {
                        String caminoComparador = it4.next().toString();
                        //System.out.println("caminoComparador = " + caminoComparador + "\n");
//                        System.out.print("[");
                        salida.print("[");
                        for (Iterator<DefaultWeightedEdge> it = listaTotalEnlaces.iterator(); it.hasNext();) {
                            String enlaceAux = it.next().toString();
                            String enlace = enlaceAux.substring(1, enlaceAux.length() - 1);
//                            System.out.println("enlace = " + enlace + "\n");

                            String result;
                            if (caminoComparador.toString().contains(enlace.toString())) {
                                result = "1";
                            } else {
                                result = "0";
                            }
                            /*for (String enlace : camino) {
                             //System.out.println("enlace = " + enlace + "\n");
                             if (caminoComparador.contains(enlace)) 
                             result = "1";
                             else
                             result = "0";

                             }*/
                            if (it.hasNext()) {
//                                System.out.print(result + ",");
                                salida.print(result + ",");
                            } else {
//                                System.out.print(result);
                                salida.print(result);
                            }
                        }
                        if (it4.hasNext()) {
//                            System.out.println("],");
                            salida.println("],");
                        } else {
//                            System.out.println("]];");
                            salida.println("]];");
                        }
                    }
                    salida.close();
                }

                /* Probamos la conexión de Java con CPLEX */
                //el anterior
                //String argumentos[] = {"-v", "G:\\Genetico\\opl\\ILP2_2\\ILP2_2.mod", pathILP + "\\ILP2_2.dat", pathILP + "\\salidaCplexILP2_2.txt", "28800"};
                //String argumentos[] = {"-v", "G:\\Genetico\\opl\\ILP2_3\\ILP2_3.mod", pathILP + "\\ILP2_3.dat", pathILP + "\\salidaCplexILP2_3.txt", "28800"};
                //OplRunILP.main(argumentos);

//                ArrayList<Integer> indiceCaminosElegidos = leerSalidaCplex(config.getK(), pathILP);
            }
        }
        
        
        
        
     /*   fr = new FileReader("solicitudes.txt");
        try (BufferedReader entrada = new BufferedReader(fr)) {


            while (entrada.readLine() != null) {
                cantSolicitudes++;
            }
        }

        fr = new FileReader("edges.txt");
        
        try (BufferedReader entrada = new BufferedReader(fr)) {


            while (entrada.readLine() != null) {
                cantEnlaces++;
            }
        }
*/

        
//
//
//        for (int x = 0; x < indiceCaminosElegidos.size(); x++) {
//            System.out.println(indiceCaminosElegidos.get(x));
////        }
//
//        ArrayList<ArrayList<String>> caminosElegidos = leerSoloCaminosElegidos(indiceCaminosElegidos);
//
//        for (int x = 0; x < caminosElegidos.size(); x++) {
//            System.out.println(caminosElegidos.get(x));
//        }
        
//        String alfaSegundaFase = leerSolicitudesSegundaFase(directedGraph, indiceCaminosElegidos);
//
//        FileWriter fw2 = new FileWriter("C:\\Users\\ysapymimbi\\opl\\ILP2_2\\ILP2_2.dat");
//
//        BufferedWriter bw2 = new BufferedWriter(fw2);
//        try (PrintWriter salida2 = new PrintWriter(bw2)) {
//            System.out.println("K = " + config.getK() + ";");
//            salida2.println("K = " + config.getK() + ";");
//            System.out.println("SD = " + cantSolicitudes + ";");
//            salida2.println("SD = " + cantSolicitudes + ";");
//            System.out.println("Ftotal = " + config.getfTotal() + ";");
//            salida2.println("Ftotal = " + config.getfTotal() + ";");
//            System.out.println("G = " + config.getG() + ";");
//            salida2.println("G = " + config.getG() + ";");
//            System.out.println("alfa = " + alfaSegundaFase);
//            salida2.println("alfa = " + alfaSegundaFase);
//            
//            
//            System.out.println("l = [");
//            salida2.println("l = [");
//            for (Iterator<ArrayList<String>> it = caminosElegidos.iterator(); it.hasNext();) {
//                ArrayList<String> camino = it.next();
//                System.out.print("[");
//                salida2.print("[");
//                for (Iterator<ArrayList<String>> it4 = caminosElegidos.iterator(); it4.hasNext();) {
//                    ArrayList<String> caminoComparador = it4.next();
//                    String result = "0";
//                    for (String enlace : camino) {
//
//                        if (caminoComparador.contains(enlace)) {
//                            result = "1";
//                            break;
//                        }
//
//                    }
//                    if (it4.hasNext()) {
//                        System.out.print(result + ",");
//                        salida2.print(result + ",");
//                    } else {
//                        System.out.print(result);
//                        salida2.print(result);
//                    }
//                }
//                if (it.hasNext()) {
//                    System.out.println("],");
//                    salida2.println("],");
//                } else {
//                    System.out.println("]];");
//                    salida2.println("]];");
//                }
//            }
//        }
//        
//        /* Probamos la conexión de Java con CPLEX */
//        String argumentos2[] = {"-v", "C:\\Users\\ysapymimbi\\opl\\ILP2_2\\ILP2_2.mod", "C:\\Users\\ysapymimbi\\opl\\ILP2_2\\ILP2_2.dat", "salidaCplexILP2_2.txt"};
//        OplRunILP.main(argumentos2);
    }

    private static int cargarGrafo(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph) {
        
        int cantEnlaces = 0;
        try {
            FileReader fr = new FileReader("edges.txt");
            String[] edgeText;
            DefaultWeightedEdge ed;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    cantEnlaces++;
                    edgeText = line.split("\t");
                    directedGraph.addVertex(edgeText[0]);
                    directedGraph.addVertex(edgeText[1]);
                    ed = directedGraph.addEdge(edgeText[0], edgeText[1]);
                    directedGraph.setEdgeWeight(ed, Double.parseDouble(edgeText[2]));
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        return cantEnlaces;
    }

    private static void calcularCaminosMasCortos(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph, 
            String origen, String destino, int contadorEscritura, 
            int k, double cantSolicitada, String pathSaltos, String salidaPath) throws IOException {
        try {
            int cont = 0;
            FileWriter fw = new FileWriter("kshortestpath.txt");
            FileWriter sfw;
//            FileWriter fw3;
            if (origen.equals("0") && destino.equals("1")) {
                sfw = new FileWriter(pathSaltos + "saltos.txt");
//                fw3 = new FileWriter(salidaPath);
            } else {
                sfw = new FileWriter(pathSaltos + "saltos.txt",true);
//                fw3 = new FileWriter(salidaPath, true);
            }
            FileWriter fw2;
            if (contadorEscritura == 0) {
                fw2 = new FileWriter("kshortestpathCompleto.txt");
            } else {
                fw2 = new FileWriter("kshortestpathCompleto.txt", true);
            }
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            BufferedWriter sbw = new BufferedWriter(sfw);
//            BufferedWriter bw3 = new BufferedWriter(fw3);
            try (PrintWriter salida = new PrintWriter(bw);
                    PrintWriter salida2 = new PrintWriter(bw2);
                    PrintWriter salida3 = new PrintWriter(sbw)) {
//                    PrintWriter salida4 = new PrintWriter(bw3)) {

                KShortestPaths caminosCandidatos = new KShortestPaths(directedGraph, origen, k);

                System.out.println("K-shortest path de " + origen + " a " + destino + " es ");

                List<GraphPath<String, DefaultWeightedEdge>> paths = caminosCandidatos.getPaths(destino);

                for (GraphPath<String, DefaultWeightedEdge> path : paths) {
//                    System.out.print("Camino " + ++cont + ": ");
                    int cantidadNodos = 0;
                    double distanciaPath = 0;
//                    salida4.print(origen + destino + "\t");
//                    salida4.print(origen + "\t");
//                    salida4.print(destino + "\t");
                    for (DefaultWeightedEdge edge : path.getEdgeList()) {
//                        System.out.print("<" + edge + ">\t");
                        salida.print("<" + edge + ">\t");
                        salida2.print(edge + "\t");
//                        if (cantidadNodos == 0) {
//                            salida4.print(directedGraph.getEdgeSource(edge) + "-" + directedGraph.getEdgeTarget(edge));
//                        } else {
//                            salida4.print("-" + directedGraph.getEdgeTarget(edge));
//                        }
                        cantidadNodos++;
                        distanciaPath = distanciaPath + directedGraph.getEdgeWeight(edge);
                    }
                    
//                    salida4.print("\t" + (int)cantSolicitada);
//                    salida4.print("\t" + (int)distanciaPath);
//                    salida4.print("\t" + (int)(distanciaPath*cantSolicitada) + "\n");
                    
//                    System.out.println(": " + path.getWeight());  // Indica el peso total de cada camino
                    salida.println(": \t" + path.getWeight());
                    salida2.println(": \t" + path.getWeight());
                    salida3.println((int)path.getWeight());
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String leerSolicitudes(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph, int k, String solicitudesPath, String path, String salidaPath) {

        String vectorAlfa = "[[";

        ArrayList<Solicitud> listaSolicitudes = new ArrayList();
        try {

            FileReader fr = new FileReader(solicitudesPath);
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
//                    cantSolicitudes++;
                    edgeText = line.split("\t");
                    Solicitud sol = new Solicitud(edgeText[0], edgeText[1], Double.parseDouble(edgeText[2]));
                    listaSolicitudes.add(sol);
                }
            }

            Double ftotal = 0.0;

            int contadorEscritura = 0;
            for (Iterator<Solicitud> it = listaSolicitudes.iterator(); it.hasNext();) {
                Solicitud solicitud = it.next();

                System.out.print("Origen: " + solicitud.getOrigen() + "\t"
                        + "Destino:" + solicitud.getDestino() + "\t"
                        + "Velocidad requerida:" + solicitud.getVelocidad() + "\t K=" + k +"\n");
                calcularCaminosMasCortos(directedGraph, solicitud.getOrigen(), solicitud.getDestino(), contadorEscritura, k, solicitud.getVelocidad(), path, salidaPath);
                ArrayList<Double> listaDistancias = leerDistancias();
                Double tsd;
                int cont = 0;
                for (Iterator<Double> it2 = listaDistancias.iterator(); it2.hasNext();) {
                    Double dist = it2.next();
//                    System.out.println("Distancia de camino " + ++cont + ": " + dist);
//                    System.out.println("alfa = " + solicitud.getVelocidad());
                    Double d = Math.ceil(solicitud.getVelocidad());

                    if (it.hasNext()) {
                        if (it2.hasNext()) {
                            vectorAlfa = vectorAlfa + d.intValue() + ",";
                        } else {
                            vectorAlfa = vectorAlfa + d.intValue() + "],[";
                        }
                    } else if (it2.hasNext()) {
                        vectorAlfa = vectorAlfa + d.intValue() + ",";
                    } else {
                        vectorAlfa = vectorAlfa + d.intValue();
                    }
                    tsd = solicitud.getVelocidad();
//                    System.out.println("Tsd = " + tsd);
                    ftotal += tsd;
                }
                contadorEscritura++;
            }

            vectorAlfa = vectorAlfa + "]];";

            //System.out.println("vectorAlfa = " + vectorAlfa);
//            System.out.println("ftotal = " + ftotal + ";");

        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }


        return vectorAlfa;
    }
    
    private static String leerSolicitudesSegundaFase(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph, ArrayList<Integer> indiceCaminosElegidos) {

        final double C = 154;

        ArrayList<Parametro> listaParams = leerParametros();

        String vectorAlfa = "[";

        ArrayList<Solicitud> listaSolicitudes = new ArrayList();
        try {

            FileReader fr = new FileReader("solicitudes.txt");
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    edgeText = line.split("\t");
                    Solicitud sol = new Solicitud(edgeText[0], edgeText[1], Double.parseDouble(edgeText[2]));
                    listaSolicitudes.add(sol);
                }
            }

            Double ftotal = 0.0;


            int contadorEscritura = 0;
            int cont = 0;
            for (Iterator<Solicitud> it = listaSolicitudes.iterator(); it.hasNext();) {
                Solicitud solicitud = it.next();

                System.out.print("Origen: " + solicitud.getOrigen() + "\t"
                        + "Destino:" + solicitud.getDestino() + "\t"
                        + "Velocidad requerida:" + solicitud.getVelocidad() + "\n");
                //calcularCaminosMasCortos(directedGraph, solicitud.getOrigen(), solicitud.getDestino(), contadorEscritura);
                ArrayList<Double> listaDistancias = leerDistanciasTodasSolicitudes();
                Double tsd;
                
                //for (int cont = 0; cont < indiceCaminosElegidos.size(); cont++) {
                    Double dist = listaDistancias.get(indiceCaminosElegidos.get(cont) - 1);
                    //if(indiceCaminosElegidos.get(cont) == cont + 1) {
                        cont = cont + 1;
//                        System.out.println("Distancia de camino " + cont + ": " + dist);
                        int nivelModulacionCorrespondiente = leerNivelModulacionCorrespondiente(dist, listaParams);
//                        System.out.println("Nivel de Modulación correspondiente es " + nivelModulacionCorrespondiente);
//                        System.out.println("alfa = " + Math.ceil(solicitud.getVelocidad() * C / (nivelModulacionCorrespondiente * C)));
                        Double d = Math.ceil(solicitud.getVelocidad() * C / (nivelModulacionCorrespondiente * C));

                        if (it.hasNext()) {
                            vectorAlfa = vectorAlfa + d.intValue() + ",";
                        } else {
                            vectorAlfa = vectorAlfa + d.intValue();
                        }
                        tsd = Math.ceil(solicitud.getVelocidad() * C / C);
//                        System.out.println("Tsd = " + tsd);
                        ftotal += tsd;
                    //}
                //}
                contadorEscritura++;
            }

            vectorAlfa = vectorAlfa + "];";

            //System.out.println("vectorAlfa = " + vectorAlfa);
//            System.out.println("ftotal = " + ftotal / C + ";");

        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }


        return vectorAlfa;
    }

    private static ArrayList<Parametro> leerParametros() {

        ArrayList<Parametro> listaParametros = new ArrayList();

        try {

            FileReader fr = new FileReader("parametros.txt");
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    edgeText = line.split("\t");
                    Parametro param = new Parametro(Double.parseDouble(edgeText[0]), Integer.parseInt(edgeText[1]));
                    listaParametros.add(param);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return listaParametros;
    }

    private static Configuracion leerConfiguraciones() {

        Configuracion config = null;

        try {

            FileReader fr = new FileReader("configuraciones.txt");
            String texto = new String();
            String configText[];

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    texto += line + "\t";
                }
                configText = texto.split("\t");
                config = new Configuracion(Integer.parseInt(configText[0]), Integer.parseInt(configText[1]));

            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado CARAJO!!!!!!: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }

    
    private static ArrayList<Double> leerDistancias() {

        ArrayList<Double> listaDistancias = new ArrayList();
        try {

            FileReader fr = new FileReader("kshortestpath.txt");
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    edgeText = line.split("\t");
                    Double dist = Double.parseDouble(edgeText[edgeText.length - 1]);
                    listaDistancias.add(dist);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return listaDistancias;
    }
    
    private static ArrayList<Double> leerDistanciasTodasSolicitudes() {

        ArrayList<Double> listaDistancias = new ArrayList();
        try {

            FileReader fr = new FileReader("kshortestpathCompleto.txt");
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    edgeText = line.split("\t");
                    Double dist = Double.parseDouble(edgeText[edgeText.length - 1]);
                    listaDistancias.add(dist);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return listaDistancias;
    }

    private static int leerNivelModulacionCorrespondiente(Double distancia, ArrayList<Parametro> listaParams) {
        int nivMod = 0;

        for (Parametro parametroMod : listaParams) {

            if (distancia < parametroMod.getDistanciaMaxima()) {
                nivMod = parametroMod.getNivelModulacion();
                break;
            }

        }
        return nivMod;
    }

    private static ArrayList<ArrayList<String>> leerTotalCaminos() {

        ArrayList<ArrayList<String>> listaCaminos = new ArrayList();

        try {
            FileReader fr = new FileReader("kshortestpathCompleto.txt");
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    ArrayList<String> listaEnlaces = new ArrayList();
                    edgeText = line.split("\\)");
                    String dist;
                    for (int i = 0; i < edgeText.length - 1; i++) {
                        dist = edgeText[i].trim().substring(1);
                        listaEnlaces.add(dist);
                    }
                    listaCaminos.add(listaEnlaces);
                }

            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return listaCaminos;
    }

    private static ArrayList<Integer> leerSalidaCplex(int k, String path) {

        ArrayList<Integer> listaCaminosElegidos = new ArrayList();

        try {
            FileReader fr = new FileReader(path + "\\salidaCplexILP2_3.txt");
            String[] edgeText;
            int indicadorLinea = 0;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                int lineaLectura = 0;

                while ((line = entrada.readLine()) != null) {


                    //line = entrada.readLine();
                    edgeText = line.split(".\\[");
                    lineaLectura = lineaLectura + 1;
                    String indices = new String();


                    if (lineaLectura >= 12) {
                        indicadorLinea = indicadorLinea + 1;
                        try{
                            indices = edgeText[1].replace("[", "").replace("]", "").replace(";", "").replace(" ", "");
                        } catch (Exception e) {
                            System.out.print(e);
                        }
                    }

                    for (int i = 0; i < indices.length() - 1; i++) {

                        if (indices.charAt(i) == '1') {
                            listaCaminosElegidos.add(indicadorLinea * k - k + i + 1);
                            break;
                        }

                    }



                }

            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return listaCaminosElegidos;
    }

    private static ArrayList<ArrayList<String>> leerSoloCaminosElegidos(ArrayList<Integer> indiceCaminosElegidos) {



        ArrayList<ArrayList<String>> listaCaminosElegidos = new ArrayList();

        try {
            FileReader fr = new FileReader("kshortestpathCompleto.txt");
            String[] edgeText;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                int contador = 1;
                while ((line = entrada.readLine()) != null) {
                    if (indiceCaminosElegidos.contains(contador)) {
                        ArrayList<String> listaEnlaces = new ArrayList();
                        edgeText = line.split("\\)");
                        String dist;
                        for (int i = 0; i < edgeText.length - 1; i++) {
                            dist = edgeText[i].trim().substring(1);
                            listaEnlaces.add(dist);
                        }
                        listaCaminosElegidos.add(listaEnlaces);
                    }
                    contador = contador + 1;
                }


            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger
                    .getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return listaCaminosElegidos;
    }
    
    private static String leerSaltos(String path) {
        String vectorAlfa = "[";
        int cant = 0;
        try {

            FileReader fr = new FileReader(path + "saltos.txt");

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                vectorAlfa = vectorAlfa + entrada.readLine();
                while ((line = entrada.readLine()) != null) {
                    vectorAlfa = vectorAlfa + ", " + line;
                    cant++;
                }
                vectorAlfa = vectorAlfa + "];";
            }catch (FileNotFoundException ex) {
                System.out.println("Archivo no encontrado: " + ex);
                Logger
                        .getLogger(ILP1.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        return vectorAlfa;
    }
    
    /**
     * Valores maximos de espectro disponible, ruta mas larga, costo mayor, posibles
     * @return 
     */
    private static List<String> leerMaximos(String path) {

        List<String> vectorAlfa = new ArrayList();
        try {

            FileReader fr = new FileReader(path + "maximos.txt");

            try (BufferedReader entrada = new BufferedReader(fr)) {
                vectorAlfa.add(entrada.readLine());
                vectorAlfa.add(entrada.readLine());
                vectorAlfa.add(entrada.readLine());
                
            }catch (FileNotFoundException ex) {
                System.out.println("Archivo no encontrado: " + ex);
                Logger
                        .getLogger(ILP1.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }catch (IOException ex) {
            Logger.getLogger(ILP1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        return vectorAlfa;
    }
    
}


//k2 150
//k3 200
//k4 50
//k4 100
//k4 150
//k4 200
//k5 todo
//k6 todo