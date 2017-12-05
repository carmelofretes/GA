/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

/**
 *
 * @author carmelo
 */

/* Ejemplo Interface Iterator aprenderaprogramar.com */
public class Persona {
    private int idPersona;
    private String nombre;
    private int edad ;
    private int altura;
    
    public Persona () {}
    public Persona(int idPersona, String nombre, int edad, int altura) {
        this.idPersona = idPersona;         
        this.nombre = nombre;   
        this.edad = edad ;
        this.altura = altura;
    }

    public int getAltura(){return altura;} //Omitimos otros métodos para simplificar el ejercicio
    public void setAltura (int altura) { this.altura = altura ; } 
    
    public int getEdad () {
        return edad ;
    }
    public void setEdad (int edad) {
        this.edad = edad ;
    }

    @Override
    public String toString() {
        return "Persona-> ID: "+idPersona+" Nombre: "+nombre+" Edad: "+edad+" Altura: "+altura+"\n";
    }
}