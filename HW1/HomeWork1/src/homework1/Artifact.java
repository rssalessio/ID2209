/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

/**
 *
 * @author dell
 */
public class Artifact {
    public int id;
    public String name;
    public String creator;
    public String creationDate;
    public String creationPlace;
    public String genre;
    public String genreDetails;
    public String description;
    
    public boolean Equals(Artifact me) {
        return this.id == me.id || this.name == me.name;
    }
    
    public static Artifact toArtifact(String line)
    {
        String[] str = line.split("-");
        if (str.length != 8)
            return null;
        Artifact ret = new Artifact();
        ret.id = Integer.parseInt(str[0]);
        ret.name = str[1];
        ret.creator = str[2];
        ret.creationDate = str[3];
        ret.creationPlace = str[4];
        ret.genre = str[5];
        ret.genreDetails = str[6];
        ret.description = str[7];
        return ret;
    }
    
    public static Artifact[] toArtifacts(String[] lines)
    {
        Artifact[] ret = new Artifact[lines.length];
        int x = 0;
        for (String str: lines)
        {
            ret[x++] = toArtifact(str); 
        }
        return ret;
    }
}
