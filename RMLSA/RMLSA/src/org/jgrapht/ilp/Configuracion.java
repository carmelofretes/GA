/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jgrapht.ilp;

/**
 *
 * @author Ivan
 */
public class Configuracion {
    private int k;
    private int fTotal;
    
    public Configuracion(int k, int fTotal) {
        this.k = k;
        this.fTotal = fTotal;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getfTotal() {
        return fTotal;
    }

    public void setfTotal(int fTotal) {
        this.fTotal = fTotal;
    }

}
