/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;
import jade.core.AID;
import jade.lang.acl.StringACLCodec;
import java.io.StringReader;
import java.util.*;
/**
 *
 * @author dell
 */
public class Tour {
    List<String> artifacts = new LinkedList();
    String curator;
    String museum;
    
    public void setCurator(String c)
    {
        curator = c;
    }
    public void setMuseum(String m)
    {
        museum = m;
    }
    public void add(String name)
    {
        artifacts.add(name);
    }
    
    public String ConverttoString()
    {
        String ret=curator+"-"+museum+"-";
        for (String s : artifacts)
        {
            ret = ret+s+"-";
        }
        return ret;
    }
    
    public void fromString(String s)
    {
        int x = 0;
        artifacts.clear();
        if (s.split("-").length > 1)
        {
            for (String retval: s.split("-"))
            {
                if (x == 0)
                {
                    curator = retval;
                }
                else if (x==1)
                    museum = retval;
                else
                    artifacts.add(retval);
                x++;
            }
        }
    }
}
