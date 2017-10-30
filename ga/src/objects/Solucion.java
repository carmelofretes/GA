package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 15/02/16.
 */
public class Solucion implements Comparable<Solucion>{

    private List<Ruteo> ruteos;
    private List<Enlace> enlaces;
    private int cantBloq;
    private Double fitness;
    private Double costo;
    private Double saltos;
    private Double espectro;
    private int pareto;

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

    public Solucion(List<Ruteo> ruteos, List<Enlace> enlaces, int cantBloq, Double fitness, int pareto) {
        this.ruteos = ruteos;
        this.enlaces = enlaces;
        this.cantBloq = cantBloq;
        this.fitness = fitness;
        this.pareto = pareto;
    }

    public Solucion() {
        ruteos = new ArrayList<Ruteo>();
        enlaces = new ArrayList<Enlace>();
        cantBloq = 0;
        fitness = 0.0;
        pareto = -1;
    }

    public Solucion (List<Enlace> enlaces){
        this.ruteos = new ArrayList<Ruteo>();
        this.enlaces = enlaces;
        this.cantBloq = 0;
        this.fitness = 0.0;
        this.pareto = -1;
    }
    
    @Override
    public int compareTo(Solucion s) {
        if (pareto < s.pareto) { return -1; }
        if (pareto > s.pareto) { return 1 ; }
        return 0;
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
    
    public int getPareto() {
        return pareto;
    }

    public void setPareto(int pareto) {
        this.pareto = pareto;
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
