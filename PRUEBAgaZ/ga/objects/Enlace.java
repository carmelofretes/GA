package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 15/02/16.
 */
public class Enlace {

    int inicio, fin;
    List<Boolean> ranuras;

    public Enlace (){
        inicio = -1;
        fin = -1;
        ranuras = new ArrayList<Boolean>();
    }

    public Enlace(int p, int s, List<Boolean> ranuras) {
        this.inicio = p;
        this.fin = s;
        this.ranuras = ranuras;
    }

    public int getInicio() {
        return inicio;
    }

    public void setInicio(int inicio) {
        this.inicio = inicio;
    }

    public int getFin() {
        return fin;
    }

    public void setFin(int fin) {
        this.fin = fin;
    }

    public List<Boolean> getRanuras() {
        return ranuras;
    }

    public void setRanuras(List<Boolean> ranuras) {
        this.ranuras = ranuras;
    }
}
