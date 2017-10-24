package ga;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 16/03/16.
 */
public class AGHelper {

    public static List<List<Boolean>> leerTopologia(String pathTopologia) throws FileNotFoundException {
        FileReader fr = new FileReader(pathTopologia + "topologia_nsf.txt");
        BufferedReader bf = new BufferedReader(fr);
        String fila;
        int i;

        List<List<Boolean>> topologia = new ArrayList<List<Boolean>>();
        List<Boolean> filaAux = new ArrayList<Boolean>();

        try {
            while ((fila = bf.readLine()) != null) {
                for (i = 0; i < fila.length(); i++) {
                    if (fila.charAt(i) == '1') {
                        filaAux.add(true);
                    } else {
                        filaAux.add(false);
                    }
                }
                topologia.add(filaAux);
                filaAux = new ArrayList<Boolean>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return topologia;
    }

    public static int leerParametro(String pathTopologia, String parametro) throws FileNotFoundException {
        FileReader fr = new FileReader(pathTopologia + "parametros.txt");
        BufferedReader bf = new BufferedReader(fr);
        String fila, leido = "";
        String aux = "";
        Character caracter;
        boolean existe = false;
        int i, cantidad = 0;

        try {
            while ((fila = bf.readLine()) != null && !existe) {
                for (i = 0; i < fila.length(); i++) {
                    if (fila.contains(parametro)) {
                        existe = true;
                        leido = fila;
                        break;
                    }
                }
            }

            i = 0;
            caracter = leido.charAt(0);
            while (!(caracter.equals(' '))) {
                aux = aux + (caracter.toString());
                i++;
                caracter = leido.charAt(i);
            }

            cantidad = Integer.valueOf(aux);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cantidad;
    }

    public static double leerProbabMutacion(String pathTopologia, String parametro) throws FileNotFoundException {
        FileReader fr = new FileReader(pathTopologia + "parametros.txt");
        BufferedReader bf = new BufferedReader(fr);
        String fila, leido = "";
        String aux = "";
        Character caracter;
        boolean existe = false;
        int i;
        double cantidad = 0.0;

        try {
            while ((fila = bf.readLine()) != null && !existe) {
                for (i = 0; i < fila.length(); i++) {
                    if (fila.contains(parametro)) {
                        existe = true;
                        leido = fila;
                        break;
                    }
                }
            }

            i = 0;
            caracter = leido.charAt(0);
            while (!(caracter.equals(' '))) {
                aux = aux + (caracter.toString());
                i++;
                caracter = leido.charAt(i);
            }

            cantidad = Double.valueOf(aux);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cantidad;
    }

    public static List<DemandaInfo> leerDemandasInfo(String archivo) throws FileNotFoundException {
        FileReader fr = new FileReader(archivo);
        BufferedReader bf = new BufferedReader(fr);
        String fila, valor = "";
        DemandaInfo demandaInfo = new DemandaInfo();
        List<DemandaInfo> demandasInfo = new ArrayList<DemandaInfo>();
        Character caracter;
        List<Integer> ruta = new ArrayList<Integer>();
        int i;

        try {
            while ((fila = bf.readLine()) != null && !fila.isEmpty()) {

                // set demanda Id
                i = 0;
                valor = "";
                caracter = fila.charAt(i);
                while (!caracter.equals('\t')) {
                    valor = valor + caracter;
                    i++;
                    caracter = fila.charAt(i);
                }
                demandaInfo.setDemandaId(valor);

                // set origen
                i++;
                caracter = fila.charAt(i);
                valor = "";
                while (!caracter.equals('\t')) {
                    valor = valor + caracter;
                    i++;
                    caracter = fila.charAt(i);
                }
                demandaInfo.setOrigen(Integer.valueOf(valor));

                // set destino
                i++;
                caracter = fila.charAt(i);
                valor = "";
                while (!caracter.equals('\t')) {
                    valor = valor + caracter;
                    i++;
                    caracter = fila.charAt(i);
                }
                demandaInfo.setDestino(Integer.valueOf(valor));

                // set ruta
                i++;
                caracter = fila.charAt(i);
                valor = "";
                while (!caracter.equals('\t')) {
                    if (!caracter.equals('-')) {
                        valor = valor + caracter;
                        i++;
                        caracter = fila.charAt(i);
                    } else {
                        ruta.add(Integer.valueOf(valor));
                        i++;
                        caracter = fila.charAt(i);
                        valor = "";
                    }
                }
                ruta.add(Integer.valueOf(valor));
                demandaInfo.setRuta(ruta);
                ruta = new ArrayList<Integer>();

                // set traf
                i++;
                caracter = fila.charAt(i);
                valor = "";
                while (!caracter.equals('\t')) {
                    valor = valor + caracter;
                    i++;
                    caracter = fila.charAt(i);
                }
                demandaInfo.setTraf(Integer.valueOf(valor));

                // set distancia/saltos
                i++;
                caracter = fila.charAt(i);
                valor = "";
                while (!caracter.equals('\t')) {
                    valor = valor + caracter;
                    i++;
                    caracter = fila.charAt(i);
                }
                valor = valor + ".0";
                demandaInfo.setSaltos(Double.valueOf(valor));

                // set X = traf X saltos
                i++;
                caracter = fila.charAt(i);
                valor = "";
                while (!caracter.equals('\n') && !caracter.equals('\t') && i != fila.length()) {
                    valor = valor + caracter;
                    i++;
                    if (!(i == fila.length())) {
                        caracter = fila.charAt(i);
                    }
                }
                demandaInfo.setX(Integer.valueOf(valor));

                demandasInfo.add(demandaInfo);

                demandaInfo = new DemandaInfo();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return demandasInfo;
    }

}