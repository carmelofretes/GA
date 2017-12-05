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
import java.util.Iterator;
public class Programa {
    public static void main(String arg[]) {
            ListaPersona lp = new ListaPersona();
            Iterator<Persona> it;
            Persona e; // Este objeto lo usaremos para almacenar temporalmente objetos Persona
            lp.getListaPersonas().add(new Persona(1,"Maria",20,175));
            lp.getListaPersonas().add(new Persona(2,"Carla",15,160));
            lp.getListaPersonas().add(new Persona(3,"Enriqueta",25,190));
            lp.getListaPersonas().add(new Persona(4,"Violeta",19,170));
            System.out.println("Lista antes de recorrer/eliminar: "+lp.toString());
            it = lp.iterator();
            while (it.hasNext() ) {
                e = it.next();
                if (e.getAltura()<170) {it.remove(); } //  it.remove() elimina la persona de la colección
            }
            System.out.println("Lista después de recorrer/eliminar: " + lp.toString() );
      }  
}