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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
 
public class EmployeeUtil {
 
    public static void sortByName(List<Employee> employees) {
        Collections.sort(employees, new Comparator<Employee> () {
            @Override
            public int compare(Employee one, Employee two) {
                return one.getNames().compareTo(two.getNames());
            }
        });
    }
 
    public static void sortBySalary(List<Employee> employees) {
        Collections.sort(employees, new Comparator<Employee> () {
            @Override
            public int compare(Employee one, Employee two) {
                return one.getSalary().compareTo(two.getSalary());
            }
        });
    }
}
