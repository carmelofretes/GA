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
    private int pareto;  //ESTA VARIABLE USO PARA RANKEAR LOS INDIVIDUOS EN CONJUNTOS
    private int indiceRankeo;  // ESTE INDICE USO PARA PODER BORRAR DESPUES EL INDIVIDUO QUE YA ES NO DOMINADO POR NADIE
    private double perimetroCuboide; //ESTA VARIABLE ES PARA DETERMINAR EL PERIMETRO DEL CUBOIDE QUE RODEA LA SOLUCION

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

    public Solucion(List<Ruteo> ruteos, List<Enlace> enlaces, int cantBloq, Double fitness, int pareto, int indiceRankeo, double distanciaCrowding) {
        this.ruteos = ruteos;
        this.enlaces = enlaces;
        this.cantBloq = cantBloq;
        this.fitness = fitness;
        this.pareto = pareto;
        this.indiceRankeo = indiceRankeo ;
        this.perimetroCuboide = perimetroCuboide ;
    }

    public Solucion() {
        ruteos = new ArrayList<Ruteo>();
        enlaces = new ArrayList<Enlace>();
        cantBloq = 0;
        fitness = 0.0;
        pareto = -1;
        indiceRankeo = -99;
        perimetroCuboide = 0.0 ;
    }

    public Solucion (List<Enlace> enlaces){
        this.ruteos = new ArrayList<Ruteo>();
        this.enlaces = enlaces;
        this.cantBloq = 0;
        this.fitness = 0.0;
        this.pareto = -1;
        this.indiceRankeo = -99;
        this.perimetroCuboide = 0.0 ;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////
    //ESTE METODO UTILIZO PARA ORDENAR EL CONJUNTO DE PARETO, POR LA VARIABLE "PARETO"
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int compareTo(Solucion s) {
        //PARA ORDENAR EN RANKEO DE PARETO
        if (pareto < s.pareto) { return -1; }
        if (pareto > s.pareto) { return 1 ; }
/*        //PARA ORDENAR POR COSTO, CUANDO NECESITE CALCULAR LA DISTANCIA DE CROWDING
        if (costo < s.costo) { return -1; }
        if (costo > s.costo) { return 1 ; } 
        //PARA ORDENAR POR SALTO, CUANDO SE NECESITE CALCULAR LA DISTANCIA DE CROWDING
        if (saltos < s.saltos) { return -1; }
        if (saltos > s.saltos) { return 1 ; } 
        //PARA ORDENAR POR ESPECTRO, CUANDO SE NECESITE CALCULAR LA DISTANCIA DE CROWDING
        if (espectro < s.espectro) { return -1; }
        if (espectro > s.espectro) { return 1 ; } */
        
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
    
    public double getPerimetroCuboide() {
        return perimetroCuboide ;
    }
    
    public void setPerimetroCuboide(double perimetroCuboide) {
        this.perimetroCuboide = perimetroCuboide ;
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

    public int getIndiceRankeo() {
        return indiceRankeo;
    }

    public void setIndiceRankeo(int indiceRankeo) {
        this.indiceRankeo = indiceRankeo;
    }  
}
