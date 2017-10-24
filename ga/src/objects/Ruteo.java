package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 23/02/16.
 */
public class Ruteo {

    private String demandaId;
    private int oriden;
    private int destino;
    private int rutaNro;
    private List<Integer> ranurasUsadas;

    public Ruteo(String demandaId, int oriden, int destino, int rutaNro, List<Integer> ranurasUsadas) {
        this.demandaId = demandaId;
        this.oriden = oriden;
        this.destino = destino;
        this.rutaNro = rutaNro;
        this.ranurasUsadas = ranurasUsadas;
    }

    public Ruteo() {
        this.demandaId = "";
        this.oriden = -1;
        this.destino = -1;
        this.rutaNro = -1;
        this.ranurasUsadas = new ArrayList<Integer>();
    }

    public String getDemandaId() {
        return demandaId;
    }

    public void setDemandaId(String demandaId) {
        this.demandaId = demandaId;
    }

    public int getOriden() {
        return oriden;
    }

    public void setOriden(int oriden) {
        this.oriden = oriden;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public int getRutaNro() {
        return rutaNro;
    }

    public void setRutaNro(int rutaNro) {
        this.rutaNro = rutaNro;
    }

    public List<Integer> getRanurasUsadas () {
        return ranurasUsadas;
    }

    public void setRanurasUsadas (List<Integer> ranurasUsadas){
        this.ranurasUsadas.addAll(ranurasUsadas);
    }

    public void clonar (Ruteo ruteo) {
        ruteo.setDemandaId(this.getDemandaId());
        ruteo.setOriden(this.getOriden());
        ruteo.setDestino(this.getDestino());
        ruteo.setRutaNro(this.getRutaNro());
        ruteo.setRanurasUsadas(this.getRanurasUsadas());
    }
}
