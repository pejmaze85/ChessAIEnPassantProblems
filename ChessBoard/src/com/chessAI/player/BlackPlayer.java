package com.chessAI.player;

import com.chessAI.Alliance;
import com.chessAI.board.Board;
import com.chessAI.board.Move;
import com.chessAI.board.Tile;
import com.chessAI.piece.Piece;
import com.chessAI.piece.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chessAI.board.Move.*;

public class BlackPlayer extends Player {
    public BlackPlayer(Board board, Collection<Move> whiteStandardLegalMoves,
                                    Collection<Move> blackStandardLegalMoves) {

        super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentsLegals) {
        final List<Move> kingCastles = new ArrayList<>();

        if(this.playerKing.isFirstMove() && !this.isInCheck()){
            if(!this.board.getTile(5).isTileOccupied() && !this.board.getTile(6).isTileOccupied()){
                final Tile rookTile = this.board.getTile(7);

                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()){
                    if(Player.calculateAttacksOnTile(5, opponentsLegals).isEmpty()
                            && Player.calculateAttacksOnTile(6, opponentsLegals).isEmpty()
                            && rookTile.getPiece().getPieceType().isRook()){
                        kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 6,
                                                                  (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 5));
                    }
                }
            }

            if(!this.board.getTile(1).isTileOccupied()
                    && !this.board.getTile(2).isTileOccupied()
                    && !this.board.getTile(3).isTileOccupied()){
                        final Tile rookTile = this.board.getTile(0);

                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove() &&
                Player.calculateAttacksOnTile(2, opponentsLegals).isEmpty() &&
                        Player.calculateAttacksOnTile(3,opponentsLegals).isEmpty()
                        && rookTile.getPiece().getPieceType().isRook()){
                    kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 2,
                                    (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 3));
                }
            }

        }

        return ImmutableList.copyOf(kingCastles);
    }
}
