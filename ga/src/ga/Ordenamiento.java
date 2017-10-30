/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;
import java.util.Arrays;

/**
 *
 * @author carmelo
 */

public class Ordenamiento {

    static class Persona implements Comparable<Persona> {

        public String nombre;
        public int edad, altura;
        // Constructor de la clase
        public Persona(String nombre, int edad, int altura) {
            this.nombre = nombre;
            this.edad = edad;
            this.altura = altura;
        }
        
        @Override
        public int compareTo(Persona o) { 
            if (edad < o.edad) { return -1; }
            if (edad > o.edad) { return 1 ; }
            return 0;
        }

    }
    
    // Método para imprimir el array de Personas
    static void imprimeArrayPersonas(Persona[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.println((i+1) + ". " + array[i].nombre + " - Altura: " + array[i].altura + " - Edad: " + array[i].edad);
        }
    }

    public static void main(String[] args) {
        Persona[] arrayPersonas = new Persona[5];
        arrayPersonas[0] = new Persona("Mario", 22, 187);
        arrayPersonas[1] = new Persona("Pepe", 52, 173);
        arrayPersonas[2] = new Persona("Manuel", 27, 158);
        arrayPersonas[3] = new Persona("David", 25, 164);
        arrayPersonas[4] = new Persona("Alberto", 80, 184);

        System.out.println("Array sin ordenar por edad");
        imprimeArrayPersonas(arrayPersonas);
        
        //Se ordena el ARRAY
        Arrays.sort(arrayPersonas);
        System.out.println("Array ordenado por edad");
        imprimeArrayPersonas(arrayPersonas);

        
    }
}