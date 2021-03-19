package com.chessAI.gui;

import com.chessAI.board.Board;
import com.chessAI.board.BoardUtils;
import com.chessAI.board.Move;
import com.chessAI.board.Tile;
import com.chessAI.piece.Piece;
import com.chessAI.player.MoveTransition;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static javax.imageio.ImageIO.read;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Table {

    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;

    private Board chessBoard;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private boolean highlightLegalMoves = true;
    private boolean holdingAPiece = false;

    private static String defaultPieceImagesPath = "art/";

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(680,600);
    private static Dimension BOARD_PANEL_DIMENSION = new Dimension(400,350);
    private static Dimension TILE_PANEL_DIMENSION = new Dimension(15,15);
    private static Color lightTileColor = Color.white;
    private static Color darkTileColor = Color.darkGray;
    private static Color lightHighlightedColor = Color.decode("#bbffa3");
    private static Color darkHighlightedColor = Color.decode("#7fb96a");


    public Table(){
        this.chessBoard = Board.createStandardBoard();
        JFrame gameFrame = new JFrame("PJChess");
        final JMenuBar tableMenuBar = createMenuBar();
        gameFrame.setJMenuBar(tableMenuBar);
        gameFrame.setSize(OUTER_FRAME_DIMENSION);
        gameFrame.setLayout(new BorderLayout());
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.boardDirection = BoardDirection.NORMAL;
        gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        gameFrame.setVisible(true);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //this.gameFrame.setResizable(false);


    }

    private JMenuBar createMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("OPEN UP DAT PGN");
            }
        });
         fileMenu.add(openPGN);

         final JMenuItem exitMenuItem = new JMenuItem("Exit");
         exitMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         fileMenu.add(exitMenuItem);
         return fileMenu;
    }

    private JMenu createPreferencesMenu(){
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);

        preferencesMenu.addSeparator();
        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight Legals", true);
        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
            }
        });
        //preferencesMenu.add(legalMoveHighlighterCheckbox);

        return preferencesMenu;
    }

    public enum BoardDirection{

        NORMAL{
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles){
                return boardTiles;
            }
            @Override
            BoardDirection opposite(){
                return FLIPPED;
            }
        },
        FLIPPED{
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles){
                return Lists.reverse(boardTiles);
            }
            @Override
            BoardDirection opposite(){
                return NORMAL;
            }
        };
        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;

        BoardPanel(){
            super(new GridLayout(8,8));
            this.boardTiles = new ArrayList<>();

            for(int i = 0; i < BoardUtils.NUM_TILES; i++){
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();

        }

        public void drawBoard(Board board) {
            removeAll();
            for(final TilePanel tilePanel : boardDirection.traverse(boardTiles)){
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    public class MoveLog {

        private final List<Move> moves;

        MoveLog(){
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves(){
            return moves;
        }

        public void addMove(final Move move){
            this.moves.add(move);
        }

        public int size(){
            return this.moves.size();
        }

        public void clear(){
            this.moves.clear();
        }

        public boolean removeMove(final Move move){
            return this.moves.remove(move);
        }

        public Move removeMove(int index){
            return this.moves.remove(index);
        }

    }



    private class TilePanel extends JPanel{

        private final int tileId;
        private Board board;

        TilePanel(final BoardPanel boardPanel, final int tileId){
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                   if(isRightMouseButton(e)){
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;

                        }else if (isLeftMouseButton(e)) {
                       sourceTile = null;
                       destinationTile = null;
                       humanMovedPiece = null;
                   }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    holdingAPiece = false;
                    if (isLeftMouseButton(e)) {
                        if (sourceTile == null && (chessBoard.getTile(tileId).isTileOccupied())) {
                            if ((chessBoard.getTile(tileId).getPiece().getPieceAlliance()
                                    == chessBoard.currentPlayer().getAlliance())) {
                                System.out.println(tileId);

                                holdingAPiece = true;
                                sourceTile = chessBoard.getTile(tileId);
                                humanMovedPiece = sourceTile.getPiece();
                                try {
                                    Toolkit t1 = Toolkit.getDefaultToolkit();
                                    BufferedImage image =
                                            read(new File(defaultPieceImagesPath +
                                                    chessBoard.getTile(tileId).getPiece().getPieceAlliance().toString().substring(0, 1)
                                                    + chessBoard.getTile(tileId).getPiece().toString() + ".png"));
                                    Point point = new Point(0, 0);
                                    Cursor cursor = t1.createCustomCursor(image, point, "Cursor");

                                    setCursor(cursor);
                                } catch (Exception x) {
                                }
                                if (humanMovedPiece == null) {
                                    sourceTile = null;
                                    holdingAPiece = false;
                                }
                            } else {
                                sourceTile = null;
                                destinationTile = null;
                                humanMovedPiece = null;
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            boardPanel.drawBoard(chessBoard);
                        }
                    });

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    TilePanel.super.setCursor(c);

                    if(holdingAPiece && sourceTile != null && destinationTile != null) {
                        final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(),
                                destinationTile.getTileCoordinate());
                        final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                        if (transition.getMoveStatus().isDone()){
                            chessBoard = transition.getTransistionBoard();
                            moveLog.addMove(move);
                            //TODO add move to log

                        }
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.redo(chessBoard,moveLog);
                                takenPiecesPanel.redo(moveLog);
                                boardPanel.drawBoard(chessBoard);
                            }
                        });
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                        if(holdingAPiece){
                            destinationTile = chessBoard.getTile(tileId);
                        }
                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });


            validate();
        }

        public void drawTile(Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightLegals(chessBoard);
            validate();
            repaint();

        }

        private void assignTilePieceIcon(Board board){

            this.removeAll();
            if(board.getTile(this.tileId).isTileOccupied()){
                try {
                    final BufferedImage image =
                             read(new File(defaultPieceImagesPath +
                                    board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0,1)
                                    + board.getTile(this.tileId).getPiece().toString() + ".png"));

                            add(new JLabel(new ImageIcon(image)));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        private void highlightLegals(final Board board) {
            if (highlightLegalMoves) {
                for (final Move move : pieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileId) {
                        if(BoardUtils.EIGHTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.SIXTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.FOURTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.SECOND_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.EIGHTH_RANK[move.getDestinationCoordinate()]){
                            setBackground(this.tileId % 2 == 0 ? lightHighlightedColor : darkHighlightedColor);
                        }else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
                                BoardUtils.FIFTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.THIRD_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.FIRST_RANK[move.getDestinationCoordinate()]){
                            setBackground(this.tileId % 2 != 0 ? lightHighlightedColor : darkHighlightedColor);
                        }
                    }
                }
            }
            if(board.currentPlayer().isInCheck()){
                if(board.currentPlayer().getPlayerKing().getPiecePosition() == this.tileId){
                    setBackground(Color.decode("#f08686"));
                }
            }
            if(board.currentPlayer().isInCheckMate()){
                if(board.currentPlayer().getPlayerKing().getPiecePosition() == this.tileId){
                    setBackground(Color.decode("#c82c2c"));
                }
            }
        }

            private Collection<Move> pieceLegalMoves(final Board board){
                if(humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()){
                    return humanMovedPiece.calculateLegalMoves(board);
                }
                return Collections.emptyList();
            }




        private void assignTileColor() {
            if(BoardUtils.EIGHTH_RANK[this.tileId] ||
                    BoardUtils.SIXTH_RANK[this.tileId] ||
                    BoardUtils.FOURTH_RANK[this.tileId] ||
                    BoardUtils.SECOND_RANK[this.tileId] ||
                    BoardUtils.EIGHTH_RANK[this.tileId]){
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            }else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
                    BoardUtils.FIFTH_RANK[this.tileId] ||
                    BoardUtils.THIRD_RANK[this.tileId] ||
                    BoardUtils.FIRST_RANK[this.tileId]){
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }


        }
    }
}
