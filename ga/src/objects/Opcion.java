package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 15/02/16.
 */
public class Opcion {

    int s, d, saltos, traf, x;
    List<Integer> ruta = new ArrayList<Integer>();

    public Opcion(int s, int d, int saltos, int traf, int x, List<Integer> ruta) {
        this.s = s;
        this.d = d;
        this.saltos = saltos;
        this.traf = traf;
        this.x = x;
        this.ruta = ruta;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public int getSaltos() {
        return saltos;
    }

    public void setSaltos(int saltos) {
        this.saltos = saltos;
    }

    public int getTraf() {
        return traf;
    }

    public void setTraf(int traf) {
        this.traf = traf;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public List<Integer> getRuta() {
        return ruta;
    }

    public void setRuta(List<Integer> ruta) {
        this.ruta = ruta;
    }
}
