/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.io.IOException;
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
       System.out.println("Aqui creamos la lista:");
        System.out.println("List<String> ejemploLista = new ArrayList<>();");     
        List<Integer> ejemploLista = new ArrayList<>();
            ejemploLista.add(4);
            ejemploLista.add(7);
            ejemploLista.add(2);
            ejemploLista.add(1);
            ejemploLista.add(3);
            ejemploLista.add(5);
            ejemploLista.add(6);
        System.out.println("El tamaño de la lista creada es de:" + ejemploLista.size());
        System.out.println(ejemploLista);
        Collections.sort(ejemploLista);
        for (int i = 0; i<=ejemploLista.size() - 1; i++) {
            System.out.println(ejemploLista.get(i));
        }

/*        Random rnd = new Random();
        int numeroRandom = rnd.nextInt(5);
        int numero = numeroRandom + 1;
        System.out.println("Número aleatorio: " + numeroRandom + " + 1 = " + numero);
        System.out.println("double\t" + Double.MIN_VALUE + "\t" + Double.MAX_VALUE);*/
        
        
    
            
    }
    
}
