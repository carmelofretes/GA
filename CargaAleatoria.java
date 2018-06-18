package ga;

import java.io.*;
import java.util.Random;

/**
 * Created by ysapymimbi on 03/04/2017.
 */
public class CargaAleatoria {

    public static void main(String [] args) throws IOException {
        String topologia = "arpanet";
        String pathArchivo = "C:\\Users\\ysapymimbi\\IdeaProjects\\tesis\\src\\ga\\archivos\\" + topologia + "\\";
         crearCargaAleatoria(pathArchivo, 50);
         crearCargaAleatoria(pathArchivo, 100);
         crearCargaAleatoria(pathArchivo, 150);
         crearCargaAleatoria(pathArchivo, 200);

    }

    public static void crearCargaAleatoria(String pathArchivo, Integer cantDemandadaMaxima) throws IOException {

        // este archivo tiene la lista de nodos fuente-destino de todas las demandas, sin la cantidad demandada
        FileReader fr = new FileReader(pathArchivo + "solicitudes.txt");
        BufferedReader bf = new BufferedReader(fr);

        File f = new File(pathArchivo + "\\cargaAleatoria\\solicitudes_" + cantDemandadaMaxima.toString() + ".txt");
        FileWriter fw = new FileWriter(f);

        String fila, nuevaFila;
        int i;
        Integer carga;
        Random rnd = new Random();
        boolean finDeLinea;

        try {
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter wr = new PrintWriter(bw);
            while ((fila = bf.readLine()) != null && !fila.isEmpty() ) {
                finDeLinea = false;
                i = 0;
                while (!finDeLinea) {
                    if (fila.length() <= i || fila.charAt(i) == '\n') {
                        carga = rnd.nextInt(cantDemandadaMaxima - 1) + 1;
                        nuevaFila = fila + "\t" + carga.toString() + "\n";
                        finDeLinea = true;
                        wr.write(nuevaFila);
                    }
                    i++;
                }
            }

            wr.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
