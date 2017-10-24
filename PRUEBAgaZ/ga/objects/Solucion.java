package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 15/02/16.
 */
public class Solucion {

    private List<Ruteo> ruteos;
    private List<Enlace> enlaces;
    private int cantBloq;
    private Double fitness;
    private Double costo;
    private Double saltos;
    private Double espectro;

    public Double getSaltos() {
        return saltos;
    }

    public void setSaltos(Double saltos) {
        this.saltos = saltos;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public Solucion(List<Ruteo> ruteos, List<Enlace> enlaces, int cantBloq, Double fitness) {
        this.ruteos = ruteos;
        this.enlaces = enlaces;
        this.cantBloq = cantBloq;
        this.fitness = fitness;
    }

    public Solucion() {
        ruteos = new ArrayList<Ruteo>();
        enlaces = new ArrayList<Enlace>();
        cantBloq = 0;
        fitness = 0.0;
    }

    public Solucion (List<Enlace> enlaces){
        this.ruteos = new ArrayList<Ruteo>();
        this.enlaces = enlaces;
        this.cantBloq = 0;
        this.fitness = 0.0;
    }

    public int getCantBloq() {
        return cantBloq;
    }

    public void setCantBloq(int cantBloq) {
        this.cantBloq = cantBloq;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public List<Ruteo> getRuteos() {
        return ruteos;
    }

    public void setRuteos(List<Ruteo> ruteos) {
        this.ruteos.addAll(ruteos);
    }

    public List<Enlace> getEnlaces() {
        return enlaces;
    }

    public void setEnlaces(List<Enlace> enlaces) {
        this.enlaces.addAll(enlaces);
    }

    public Double getEspectro() {
        return espectro;
    }

    public void setEspectro(Double espectro) {
        this.espectro = espectro;
    }


}
