package com.chessAI;

import com.chessAI.board.Board;
import com.chessAI.gui.Table;

public class PJChess {
    public static void main (String args[]){

        Board board;
        board = Board.createStandardBoard();

        System.out.println(board);

        Table table = new Table();
    }
}
