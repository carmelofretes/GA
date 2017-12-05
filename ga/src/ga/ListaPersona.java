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
import java.util.ArrayList;
import java.util.Iterator;
public class ListaPersona {
    private ArrayList<Persona> listapersona = null;  // Campo de la clase
    public ListaPersona() {listapersona = new ArrayList<Persona>();}  // Constructor de la clase
    public ArrayList<Persona> getListaPersonas(){return listapersona;} //Omitimos otros métodos
    public Iterator<Persona> iterator() {return new MiIteradorListaPersonas();} // Método de la clase
    @Override
    public String toString() {return listapersona.toString();}  // Método de la clase
    protected class MiIteradorListaPersonas implements Iterator<Persona>  // Clase interna
    {
        public int posicion = 0;    public boolean sepuedeeliminar = false;  // Campos
        @Override
        public boolean hasNext() {return posicion < listapersona.size();}  // Método
        @Override
        public Persona next() {  // Método
            Persona res = listapersona.get(posicion); // Creamos un objeto Persona igual al que recorremos
            posicion ++;
            sepuedeeliminar = true;
            return res;}
        @Override
        public void remove() {
            if (sepuedeeliminar) {
                listapersona.remove(posicion-1);
                posicion--;
                sepuedeeliminar = false; }
        } // Cierre del método remove
    } // Cierre de la clase interna
}  // Cierre de la clase