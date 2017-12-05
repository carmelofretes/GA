/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

/**
 *
 * @author carmelo
 */
public class Employee {
    private Integer id;
    private String names;
    private Double salary;
 
    public Employee() {}
 
    public Employee(Integer id, String names, Double salary) {
        this.id = id;
        this.names = names;
        this.salary = salary;
    }
 
    public Integer getId() { return id; }
 
    public void setId(Integer id) { this.id = id; }
 
    public String getNames(){ return names; }
 
    public void setNames(String names) { this.names = names; }
 
    public Double getSalary() { return salary; }
 
    public void setSalary(Double salary) { this.salary = salary; }
 
    @Override
    public String toString() {
        return "ID:\t"+id+"\nNombres:\t"+names+"\nSalario:\t"+salary;
    }
 
}