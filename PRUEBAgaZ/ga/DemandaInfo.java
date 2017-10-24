package ga;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 23/02/16.
 */
public class DemandaInfo {

    private String demandaId;
    private int origen;
    private int destino;
    private int traf;
    private Double saltos;
    private int x;
    List<Integer> ruta;


    public DemandaInfo(String demandaId, int origen, int destino, int traf, Double saltos, int x, List<Integer> ruta) {
        this.demandaId = demandaId;
        this.origen = origen;
        this.destino = destino;
        this.traf = traf;
        this.saltos = saltos;
        this.x = x;
        this.ruta = ruta;
    }

    public DemandaInfo(){
        demandaId = "";
        origen = -1;
        destino = -1;
        traf = -1;
        saltos = -1.0;
        x = -1;
        ruta = new ArrayList<Integer>();
    }

    public String getDemandaId() {
        return demandaId;
    }

    public void setDemandaId(String demandaId) {
        this.demandaId = demandaId;
    }

    public int getOrigen() {
        return origen;
    }

    public void setOrigen(int origen) {
        this.origen = origen;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public int getTraf() {
        return traf;
    }

    public void setTraf(int traf) {
        this.traf = traf;
    }

    public Double getSaltos() {
        return saltos;
    }

    public void setSaltos(Double saltos) {
        this.saltos = saltos;
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
        this.ruta.addAll(ruta);
    }
}
