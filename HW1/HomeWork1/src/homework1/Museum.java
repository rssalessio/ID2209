/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
/**
 *
 * @author dell
 */
public class Museum {
    public Map<String,Artifact> artifacts = new HashMap<String,Artifact>();
    public String name;
    public boolean loadFromFile(String file)
    {
        int state = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
               line = line.toLowerCase();
               if (state == 0) {
                   this.name = line;
                   state = state+1;
               }
               else
               {
                    System.out.println(line);
                    String[] splitted = line.split("\\s+");
                    Artifact temp = new Artifact();
                    temp.id = Integer.parseInt(splitted[0]);
                    temp.name = splitted[1];
                    temp.creator = splitted[2];
                    temp.creationDate = splitted[3];
                    temp.creationPlace = splitted[4];
                    temp.genre = splitted[5];
                    temp.genreDetails  = splitted[6];
                    temp.description = splitted[7];
                    artifacts.put(temp.name, temp);
               }
            }
        } catch(IOException exp) { return false; }
        return true;
    }
    public boolean Equals(Museum me) {
        return this.name == me.name;
    }
}
