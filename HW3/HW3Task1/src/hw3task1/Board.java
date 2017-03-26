package hw3task1;

import static java.lang.Math.abs;
import java.util.Stack;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * Represents the chessboard
 */
public class Board {
    public int[] board = null; //board cells
    public int size = 0; //size of the chessboard
    Stack<Move> moves = new Stack();
    public Board(int N)
    {
        
        setSize(N);
    }
    
    public void setSize(int N)
    {
        size = N;
        board = new int[size]; //we need only to consider rows or column. board[x][1] is the row of column x. board[x][2] represents which  agent is in that column
        clearBoard();
    }
    public void undoLastMove()
    {
        Move last = moves.pop();
        board[last.row]=-1;
    }
    public void clearBoard() //clear everything in the board
    {
        for (int i=0; i<size; i++ )
                board[i]= -1;
        
    }
    public void update(int row, int col)
    {
        board[row]=col;
        moves.push(new Move(row,col));
    }
    public void update(Move m)
    {
        
        board[m.row]= m.column;
        moves.push(m);
    }
    
    public void printBoard()
    {
        System.out.println("----Board status----");
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                if (board[i] == j )
                    System.out.print("Q");
                else
                    System.out.print("x");
            }
            System.out.print("\n");
        }
        System.out.println("--------------------");
    }
    
    public boolean rowPromising(int x)
    {
        if (x < size)
        {
            for (int k = 0; k < x; k++)
            {   
                if (board[x] == board[k] || abs(board[x]-board[k]) == x-k)
                    return false;
            }
            return true;
        }
        else
            return false;
    }
}
