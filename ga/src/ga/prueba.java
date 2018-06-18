/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author carmelo
 */
public class prueba {
    
    public static void main(String [] args) throws IOException {
        
        File f = new File("G:/Genetico/prueba/prueba.txt");
        FileWriter fw = new FileWriter(f);

        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter wr = new PrintWriter(bw);

        wr.write("PRIMERO");
        wr.write("\tSEGUNDO");
        wr.write("\tTERCERO");
        wr.write("\tCUARTO");
        wr.write("\tQUINTO");
        wr.write("\tSEXTO");

        wr.close();
        bw.close();        
/*        System.out.println("Aqui creamos la lista:");
        System.out.println("List<String> ejemploLista = new ArrayList<>();");     
        List<Integer> ejemploLista = new ArrayList<>();
        List<Integer> ejemploUsados = new ArrayList<>();
        List<Integer> ejemploMasUsados = new ArrayList<>();
        int aux;
        int aux2;
            ejemploLista.add(4);
            ejemploLista.add(7);
            ejemploLista.add(2);
            ejemploLista.add(1);
            ejemploLista.add(3);
            ejemploLista.add(5);
            ejemploLista.add(6);
            ejemploUsados.add(10);
            ejemploUsados.add(15);
            ejemploUsados.add(20);
            ejemploUsados.add(14);
            ejemploUsados.add(9);
            ejemploUsados.add(12);
            ejemploUsados.add(8);            
        System.out.println("El tamaño de la lista creada es de:" + ejemploLista.size());
        System.out.println(ejemploLista);
        System.out.println(ejemploUsados);
        //Collections.sort(ejemploLista);

        for (int i = 0; i < ejemploLista.size(); i++) {
            for (int j = i + 1; j < ejemploLista.size(); j++) {
                if (ejemploUsados.get(i) > ejemploUsados.get(j)){
                    aux = ejemploLista.get(i);
                    aux2 = ejemploUsados.get(i); 
                    ejemploLista.set(i, ejemploLista.get(j));
                    ejemploLista.set(j, aux);
                    ////////////////////////////////////////////
                    ejemploUsados.set(i, ejemploUsados.get(j));
                    ejemploUsados.set(j, aux2);
                }
            }
        }     
        
        System.out.println(ejemploLista);*/

/*        for (int i = 0; i<=ejemploLista.size() - 1; i++) {
            //System.out.println(ejemploLista.get(i));
            if (esImpar(ejemploLista.get(i))) {
                System.out.println(ejemploLista.get(i) + " es IMPAR");
            } else   
                    System.out.println(ejemploLista.get(i) + " es PAR");                
        }
 */
    }
       

/*        Random rnd = new Random();
        int numeroRandom = rnd.nextInt(5);
        int numero = numeroRandom + 1;
        System.out.println("Número aleatorio: " + numeroRandom + " + 1 = " + numero);
        System.out.println("double\t" + Double.MIN_VALUE + "\t" + Double.MAX_VALUE);*/
 
    public static boolean esImpar(Integer iNumero) {
        if (iNumero%2 != 0)
            return true;
        else
            return false;
    }    
    
}
