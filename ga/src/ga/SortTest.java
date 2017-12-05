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
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
 
public class SortTest {
    public static void main(String[] args) {
        List<Employee> employees = new ArrayList<>();
        Collections.addAll(employees,
                           new Employee(1, "Homer Simpson", 3500d),
                           new Employee(2, "Peter Griffin", 3000d),
                           new Employee(3, "Bugs Bunny", 3500d),
                           new Employee(4, "Chris Redfield", 2500d),
                           new Employee(5, "Alice Abernathy", 3500d)
                          );
 
        System.out.println("Listado Original...\n");
        //EmployeeUtil.sortByName(employees);
        for(Employee employee : employees)
            System.out.println(employee);
        
        System.out.println("Ordenando por nombre...\n");
        EmployeeUtil.sortByName(employees);
        for(Employee employee : employees)
            System.out.println(employee);
 
        System.out.println("\nOrdenando por sueldo...\n");
        EmployeeUtil.sortBySalary(employees);
        for(Employee employee : employees)
            System.out.println(employee);
 
    }
}
