/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author carmelo
 */
public class prueba {
    public static void main(String [] args) throws IOException {
        System.out.println("Aqui creamos la lista:");
        System.out.println("List<String> ejemploLista = new ArrayList<>();");     
        List<String> ejemploLista = new ArrayList<>();
            ejemploLista.add("Enero");
            ejemploLista.add("Frebrero");
            ejemploLista.add("Marzo");
            ejemploLista.add("Abril");
            ejemploLista.add("Mayo");
            ejemploLista.add("Junio");
            ejemploLista.add("Julio");
        System.out.println("El tamaño de la lista creada es de:" + ejemploLista.size());
        System.out.println(ejemploLista);
        for (int i = 0; i<=ejemploLista.size() - 1; i++) {
            System.out.println(ejemploLista.get(i));
        }
        
    
            
    }
    
}
