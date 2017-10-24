/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jgrapht.ilp;

/**
 *
 * @author Ivan
 */
public class Parametro {
    private Double distanciaMaxima;
    private int nivelModulacion;

    public Parametro(Double distanciaMaxima, int nivelModulacion) {
        this.distanciaMaxima = distanciaMaxima;
        this.nivelModulacion = nivelModulacion;
    }

    public Double getDistanciaMaxima() {
        return distanciaMaxima;
    }

    public void setDistanciaMaxima(Double distanciaMaxima) {
        this.distanciaMaxima = distanciaMaxima;
    }

    public int getNivelModulacion() {
        return nivelModulacion;
    }

    public void setNivelModulacion(int nivelModulacion) {
        this.nivelModulacion = nivelModulacion;
    }

    
}
