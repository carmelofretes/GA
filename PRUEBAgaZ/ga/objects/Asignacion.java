package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 15/02/16.
 */
public class Asignacion {

    int s, d, ruta;
    List<Boolean> ranuras;

    public Asignacion(int s, int d, int ruta, List<Boolean> ranuras) {
        this.s = s;
        this.d = d;
        this.ruta = ruta;
        this.ranuras = ranuras;
    }

    public Asignacion(){
        s = -1;
        d = -1;
        ruta = -1;
        ranuras = new ArrayList<Boolean>();
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

    public int getRuta() {
        return ruta;
    }

    public void setRuta(int ruta) {
        this.ruta = ruta;
    }

    public List<Boolean> getRanuras() {
        return ranuras;
    }

    public void setRanuras(List<Boolean> ranuras) {
        this.ranuras = ranuras;
    }
}
