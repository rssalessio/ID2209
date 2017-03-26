/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
/**
 *
 * @author dell
 */
public class User {
    public int age;
    public String occupation;
    public String gender;
    public Set<String> artifacts =new HashSet<String>();
    public Set<String> interests=new HashSet<String>();
    
    public String getInterests()
    {
        String ret = "";
        for(String s : interests)
            ret = ret+s+"-";
        return ret;
    }
    
    
    public boolean loadFromFile(String file)
    {
        int state = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
               line = line.toLowerCase();
               String[] splitted = line.split("\\s+");
               if (state == 0){
                    this.age = Integer.parseInt(splitted[0]);
                    this.occupation = splitted[1];
                    this.gender = (splitted[2]);
                    state = state+1;
               }
               else if (state == 1 ){
                   for (int x = 0; x < splitted.length; x++) {
                       interests.add(splitted[x]);
                   }
                   state = state+1;
               }
               else if (state == 2) {
                   for (int x = 0; x < splitted.length; x++) {
                       artifacts.add(splitted[x]);
                   }
                   state = state+1;
               }
               else { break; }
            }
        } catch(Exception exp) { return false; }
        return true;
    }
    
}
