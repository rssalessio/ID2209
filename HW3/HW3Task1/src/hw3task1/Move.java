/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw3task1;

/**
 *
 * @author dell
 */
public class Move {
    int row=-1;
    int column=-1;
    public Move( int r, int c)
    {
        row = r;
        column = c;
    }
    public Move(String s)
    {
        fromString(s);
    }
    public String toString()
    {
        return row+"-"+column;
    }
    public void fromString(String str)
    {
        String[] l = str.split("-");
        if (l.length != 2) return;
        row = Integer.parseInt(l[0]);
        column=Integer.parseInt(l[1]);
        
    }
}
