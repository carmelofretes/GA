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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.*;

public class ILP1 {

    public static void main(String args[]) throws IOException {

        try {
            Configuracion config = leerConfiguraciones();
            // Definimos e instanciamos el grafo con pesos
            SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph =
                    new SimpleDirectedGraph(DefaultWeightedEdge.class);
            cargarGrafo(directedGraph);
            String alfa = leerSolicitudes(directedGraph, config.getK());
            FileReader fr = new FileReader("solicitudes.txt"); //se vuelve a leer el archivo para contar cuantas lineas tiene el archivo
            int cantSolicitudes = 0;
            try (BufferedReader entrada = new BufferedReader(fr)) {
                
                
                while (entrada.readLine() != null) {
                    cantSolicitudes++;
                }
            }
            try {
                FileWriter fw = new FileWriter("C:\\Users\\ACER\\opl\\ILP 1\\ilp1.dat");

                BufferedWriter bw = new BufferedWriter(fw);
                try (PrintWriter salida = new PrintWriter(bw)) {
                    System.out.println("K = " + config.getK() + ";");
                    salida.println("K = " + config.getK() + ";");
                    System.out.println("SD = " + cantSolicitudes + ";");
                    salida.println("SD = " + cantSolicitudes + ";");
                    System.out.println("Ftotal = " + config.getfTotal() + ";");
                    salida.println("Ftotal = " + config.getfTotal() + ";");
//                    System.out.println("G = " + config.getG() + ";");
//                    salida.println("G = " + config.getG() + ";");
                    System.out.println("alfa = " + alfa);
                    salida.println("alfa = " + alfa);

                    ArrayList<ArrayList<String>> listaTotalCaminos = leerTotalCaminos();

                    System.out.println("l = [");
                    salida.println("l = [");
                    for (Iterator<ArrayList<String>> it = listaTotalCaminos.iterator(); it.hasNext();) {
                        ArrayList<String> camino = it.next();
                        System.out.print("[");
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
                                System.out.print(result + ",");
                                salida.print(result + ",");
                            } else {
                                System.out.print(result);
                                salida.print(result);
                            }
                        }
                        if (it.hasNext()) {
                            System.out.println("],");
                            salida.println("],");
                        } else {
                            System.out.println("]];");
                            salida.println("]];");
                        }
                    }
                }
                
                /* Probamos la conexión de Java con CPLEX */
                String argumentos[] = {"-v", "C:\\Users\\ACER\\opl\\ILP 1\\ILP 1.mod", "C:\\Users\\ACER\\opl\\ILP 1\\ilp1.dat", "salidaCplexILP1.txt"};
                OplRunILP.main(argumentos);
                
                
            }catch (IOException ex) {
                Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }catch (FileNotFoundException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    private static void cargarGrafo(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph) {
        try {

            FileReader fr = new FileReader("edges.txt");
            String[] edgeText;
            DefaultWeightedEdge ed;

            try (BufferedReader entrada = new BufferedReader(fr)) {
                String line;
                while ((line = entrada.readLine()) != null) {
                    edgeText = line.split("\t");
                    directedGraph.addVertex(edgeText[0]);
                    directedGraph.addVertex(edgeText[1]);
                    ed = directedGraph.addEdge(edgeText[0], edgeText[1]);
                    directedGraph.setEdgeWeight(ed, Double.parseDouble(edgeText[2]));
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void calcularCaminosMasCortos(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph, String origen, String destino, int contadorEscritura, int k) {
        try {
            int cont = 0;
            FileWriter fw = new FileWriter("kshortestpath.txt");
            FileWriter fw2;
            if (contadorEscritura == 0) {
                fw2 = new FileWriter("kshortestpathCompleto.txt");
            } else {
                fw2 = new FileWriter("kshortestpathCompleto.txt", true);
            }
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            try (PrintWriter salida = new PrintWriter(bw); PrintWriter salida2 = new PrintWriter(bw2)) {

                KShortestPaths caminosCandidatos = new KShortestPaths(directedGraph, origen, k);

                System.out.println("K-shortest path de " + origen + " a " + destino + " es ");

                List<GraphPath<String, DefaultEdge>> paths = caminosCandidatos.getPaths(destino);

                for (GraphPath<String, DefaultEdge> path : paths) {
                    System.out.print("Camino " + ++cont + ": ");
                    for (DefaultEdge edge : path.getEdgeList()) {
                        System.out.print("<" + edge + ">\t");
                        salida.print("<" + edge + ">\t");
                        salida2.print(edge + "\t");
                    }
                    System.out.println(": " + path.getWeight());  // Indica el peso total de cada camino
                    salida.println(": \t" + path.getWeight());
                    salida2.println("");
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String leerSolicitudes(SimpleDirectedGraph<String, DefaultWeightedEdge> directedGraph, int k) {

        final double C = 154;

        ArrayList<Parametro> listaParams = leerParametros();

        String vectorAlfa = "[[";

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
            for (Iterator<Solicitud> it = listaSolicitudes.iterator(); it.hasNext();) {
                Solicitud solicitud = it.next();

                System.out.print("Origen: " + solicitud.getOrigen() + "\t"
                        + "Destino:" + solicitud.getDestino() + "\t"
                        + "Velocidad requerida:" + solicitud.getVelocidad() + "\n");
                calcularCaminosMasCortos(directedGraph, solicitud.getOrigen(), solicitud.getDestino(), contadorEscritura, k);
                ArrayList<Double> listaDistancias = leerDistancias();
                Double tsd;
                int cont = 0;
                for (Iterator<Double> it2 = listaDistancias.iterator(); it2.hasNext();) {
                    Double dist = it2.next();
                    System.out.println("Distancia de camino " + ++cont + ": " + dist);
                    int nivelModulacionCorrespondiente = leerNivelModulacionCorrespondiente(dist, listaParams);
                    System.out.println("Nivel de Modulación correspondiente es " + nivelModulacionCorrespondiente);
                    System.out.println("alfa = " + Math.ceil(solicitud.getVelocidad() * C / (nivelModulacionCorrespondiente * C)));
                    Double d = Math.ceil(solicitud.getVelocidad() * C / (nivelModulacionCorrespondiente * C));

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
                    tsd = Math.ceil(solicitud.getVelocidad() * C / C); //se asume que la velocidad es múltiplo de C
                    //System.out.println("Tsd = " + tsd);
                    ftotal += tsd;
                }
                contadorEscritura++;
            }

            vectorAlfa = vectorAlfa + "]];";

            //System.out.println("vectorAlfa = " + vectorAlfa);
            //System.out.println("ftotal = " + ftotal / C + ";");

        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex);
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
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
            System.out.println("Archivo no encontrado: " + ex);
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ILP1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaCaminos;
    }

    
}
