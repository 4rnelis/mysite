package com.arno.mysite.chess.game;

import java.util.ArrayList;

public class Board {
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private final ArrayList<Piece> pieces;
    private Piece[][] boardPlacement;

    private boolean reversed;

    private boolean blackChecked;
    private boolean whiteChecked;
    private boolean blackMated;
    private boolean whiteMated;

    private int turn;

    public Board() {
        this.pieces = new ArrayList<>();
        boardPlacement = new Piece[WIDTH][HEIGHT];
        createPieces();
        updateBoard();
        this.reversed = false;

        this.blackChecked = false;
        this.whiteChecked = false;
        this.blackMated = false;
        this.whiteMated = false;

        this.turn = 0;
    }

    /**
     * Creates an ArrayList of pieces
     */
    public void createPieces() {
        //Black pieces
        pieces.add(new Piece(Pieces.ROOK, 0, 0, 'B'));
        pieces.add(new Piece(Pieces.KNIGHT, 1, 0, 'B'));
        pieces.add(new Piece(Pieces.BISHOP, 2, 0, 'B'));
        pieces.add(new Piece(Pieces.QUEEN, 3, 0, 'B'));
        pieces.add(new Piece(Pieces.KING, 4, 0, 'B'));
        pieces.add(new Piece(Pieces.BISHOP, 5, 0, 'B'));
        pieces.add(new Piece(Pieces.KNIGHT, 6, 0, 'B'));
        pieces.add(new Piece(Pieces.ROOK, 7, 0, 'B'));
        for (int i = 0; i < WIDTH; i++) {
            pieces.add(new Piece(Pieces.PAWN, i, 1, 'B'));
        }

        //White pieces
        for (int i = 0; i < WIDTH; i++) {
            pieces.add(new Piece(Pieces.PAWN, i, HEIGHT-2, 'W'));
        }
        pieces.add(new Piece(Pieces.ROOK, 0, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.KNIGHT, 1, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.BISHOP, 2, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.QUEEN, 3, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.KING, 4, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.BISHOP, 5, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.KNIGHT, 6, HEIGHT-1, 'W'));
        pieces.add(new Piece(Pieces.ROOK, 7, HEIGHT-1, 'W'));
    }

    /**
     * Assigns pieces to the board and checks for check and/or mate. Ends user's turn
     */
    public void updateBoard() {
        reverseBoard();
        updatePlacement();

        blackChecked = isChecked('B');
        blackMated = isMated('B');
        whiteChecked = isChecked('W');
        whiteMated = isMated('W');

        turn++;
        System.out.println(turn);
    }

    /**
     * Assigns pieces to the board
     */
    public void updatePlacement() {
        boardPlacement = new Piece[WIDTH][HEIGHT];
        for (Piece piece : pieces) {
            if (piece != null) {
                boardPlacement[piece.getPosX()][piece.getPosY()] = piece;
            }
        }
        for (Piece piece : pieces) {
            if (piece != null) {
                updateCoveredFields(piece);
            }
        }
    }

    public void printBoard() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (boardPlacement[j][i] == null) {
                    System.out.print(" |EMPTY| ");
                } else {
                    System.out.print(" |" + boardPlacement[j][i].getName() + boardPlacement[j][i].getRole() + "| ");
                }
            }
            System.out.println();
        }
    }

    public void reverseBoard() {
        this.reversed = !reversed;
    }

    public Piece getPiece(int x, int y) {
        return boardPlacement[x][y];
    }

    /**
     * If the move is valid (the role is according to the turn and the rules are respected), executes it.
     * moves piece from starting coords to new coords
     *
     * @param force selects whether to do condition checks
     */
    public void setPiece(int currentX, int currentY, int newX, int newY, boolean force) {
        for (Piece piece : pieces) {
            //piece != null required because of checks
            if (piece != null) {
                if (piece.getPosX() == currentX && piece.getPosY() == currentY) {
                    if (force || (!reversed && piece.getRole() == 'W') || (reversed && piece.getRole() == 'B')) {
                        if (force || determineMoveRules(piece, newX, newY)) {
                            if (force || !futureMoveChecked(piece, newX, newY)) {
                                if (getPiece(newX, newY) != null) {
                                    pieces.set(pieces.indexOf(getPiece(newX, newY)), null);
                                }

                                piece.setPosX(newX);
                                piece.setPosY(newY);

                                if (!force) {
                                    updateBoard();
                                } else {
                                    updatePlacement();
                                }

                                return;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("INVALID MOVE!");
    }

    /**
     * Determines if a piece is allowed to move according to rules
     */
    public boolean determineMoveRules(Piece piece, int newX, int newY) {
        switch (piece.getName()) {
            case PAWN -> {
                return pawnMovement(piece, newX, newY);
            }
            case BISHOP -> {
                return bishopMovement(piece, newX, newY);
            }
            case ROOK -> {
                return rookMovement(piece, newX, newY);
            }
            case KNIGHT -> {
                return knightMovement(piece, newX, newY);
            }
            case QUEEN -> {
                return queenMovement(piece, newX, newY);
            }
            case KING -> {
                return kingMovement(piece, newX, newY);
            }
        }
        return false;
    }

    /**
     * Gets all the squares the piece has access to in the next move
     * @param piece != null
     */
    public void updateCoveredFields(Piece piece) {
        switch (piece.getName()) {
            case PAWN -> piece.setCoveredFields(getPawnCoveredFields(piece)); //Redirects to a function that updates fields according to pawn movement
            case BISHOP -> piece.setCoveredFields(getBishopCoveredFields(piece)); //Redirects to a function that updates fields according to bishop movement
            case ROOK -> piece.setCoveredFields(getRookCoveredFields(piece)); //Redirects to a function that updates fields according to rook movement
            case KNIGHT -> piece.setCoveredFields(getKnightCoveredFields(piece)); //Redirects to a function that updates fields according to knight movement
            case QUEEN -> piece.setCoveredFields(getQueenCoveredFields(piece)); //Redirects to a function that updates fields according to queen movement
            case KING -> piece.setCoveredFields(getKingCoveredFields(piece)); //Redirects to a function that updates fields according to king movement
        }
    }

    /**
     * finds the king piece based on colour
     * @param role != null
     * @return king piece;
     */
    public Piece findKing(char role) {
        for (Piece piece : pieces) {
            //piece != null required because of checks
            if (piece != null) {
                if (piece.getRole() == role && piece.getName() == Pieces.KING) {
                    return piece;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the king of the colour in params
     * @param role != null
     */
    public boolean isChecked(char role) {
        int x = findKing(role).getPosX();
        int y = findKing(role).getPosY();
        for (Piece piece : pieces) {
            //piece != null required because of checks
            if (piece != null) {
                for (Integer[] integers : piece.getCoveredFields()) {
                    if (piece.getRole() != role && integers[0] == x && integers[1] == y) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if after making a particular move, the owner piece gets a check (invalid!)
     */
    public boolean futureMoveChecked(Piece piece, int newX, int newY) {

        Piece tempPiece = null;
        int index = -1;

        int x = piece.getPosX();
        int y = piece.getPosY();

        if (getPiece(newX, newY) != null) {
            tempPiece = getPiece(newX, newY);
            index = pieces.indexOf(getPiece(newX, newY));
        }

        setPiece(x, y, newX, newY, true);


        if (isChecked(piece.getRole())) {
            System.out.println("CHECKED!");
            setPiece(newX, newY, x, y, true);
            if (index != -1 && tempPiece != null) {
                boardPlacement[newX][newY] = tempPiece;
                pieces.set(index, getPiece(newX, newY));
            }
            return true;
        }

        setPiece(newX, newY, x, y, true);
        if (index != -1 && tempPiece != null) {
            boardPlacement[newX][newY] = tempPiece;
            pieces.set(index, getPiece(newX, newY));
        }
        return false;
    }

    /**
     * if there's a check, simulates all possible moves to see if it's possible to avoid it
     * @param role select which role's mate to check
     * @return whether there's a mate
     */
    public boolean isMated(char role) {

        if (isChecked(role)) {

            Piece tempPiece;
            int index;

            int x, y;

            for (Piece piece : pieces) {
                if (piece != null) {

                    index = -1;
                    tempPiece = null;
                    x = piece.getPosX();
                    y = piece.getPosY();

                    ArrayList<Integer[]> moves = new ArrayList<>();

                    if (piece.getRole() == role) {
                        //if pawn, gets the possible moves
                        if (piece.getName() == Pieces.PAWN) {
                            moves = getPawnMovement(piece);
                        }
                        for (Integer[] integers : piece.getCoveredFields()) {
                            if (determineMoveRules(piece, integers[0], integers[1])) {
                                moves.add(integers);
                            }
                        }

                        for (Integer[] integers : moves) {

                            //If piece is to be attacked, store its copy
                            if (getPiece(integers[0], integers[1]) != null) {
                                tempPiece = getPiece(integers[0], integers[1]);
                                index = pieces.indexOf(getPiece(integers[0], integers[1]));
                            }

                            //make test move
                            setPiece(x, y, integers[0], integers[1], true);

                            if (!isChecked(role)) {

                                //undo test move
                                setPiece(integers[0], integers[1], x, y, true);
                                //insert copy back into array
                                if (index != -1 && tempPiece != null) {
                                    boardPlacement[integers[0]][integers[1]] = tempPiece;
                                    pieces.set(index, getPiece(integers[0], integers[1]));
                                }
                                updatePlacement();
                                return false;
                            }

                            //undo test move
                            setPiece(integers[0], integers[1], x, y, true);
                            //insert copy back into array
                            if (index != -1 && tempPiece != null) {
                                boardPlacement[integers[0]][integers[1]] = tempPiece;
                                pieces.set(index, getPiece(integers[0], integers[1]));
                            }
                            updatePlacement();
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public ArrayList<Integer[]> getPawnCoveredFields(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        if (piece.getRole() == 'W') {
            if (!(piece.getPosX() - 1 < 0 || piece.getPosY() - 1 < 0) && !(piece.getPosX() - 1 > WIDTH-1 || piece.getPosY() - 1 > HEIGHT-1)) {
                result.add(new Integer[]{piece.getPosX() - 1, piece.getPosY() - 1});
            }

            if (!(piece.getPosX() + 1 < 0 || piece.getPosY() - 1 < 0) && !(piece.getPosX() + 1 > WIDTH-1 || piece.getPosY() - 1 > HEIGHT-1)) {
                result.add(new Integer[]{piece.getPosX() + 1, piece.getPosY() - 1});
            }
        } else if (piece.getRole() == 'B') {
            if (!(piece.getPosX() + 1 < 0 || piece.getPosY() + 1 < 0) && !(piece.getPosX() + 1 > WIDTH-1 || piece.getPosY() + 1 > HEIGHT-1)) {
                result.add(new Integer[]{piece.getPosX() + 1, piece.getPosY() + 1});
            }

            if (!(piece.getPosX() - 1 < 0 || piece.getPosY() + 1 < 0) && !(piece.getPosX() - 1 > WIDTH-1 || piece.getPosY() + 1 > HEIGHT-1)) {
                result.add(new Integer[]{piece.getPosX() - 1, piece.getPosY() + 1});
            }
        }

        return result;
    }

    public ArrayList<Integer[]> getBishopCoveredFields(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        int x = piece.getPosX();
        int y = piece.getPosY();

        while (!(x < 0 || x+1 > WIDTH-1) && !(y < 0 || y+1 > HEIGHT-1)) {
            if (getPiece(x+1, y+1) == null) {
                result.add(new Integer[]{x+1, y+1});
            }
            if (getPiece(x+1, y+1) != null && getPiece(x+1, y+1).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x+1, y+1});
                break;
            }
            if (getPiece(x+1, y+1) != null && getPiece(x+1, y+1).getRole() == piece.getRole()) {
                break;
            }
            x++;
            y++;
        }

        x = piece.getPosX();
        y = piece.getPosY();

        while (!(x-1 < 0 || x > WIDTH-1) && !(y < 0 || y+1 > HEIGHT-1)) {
            if (getPiece(x-1, y+1) == null) {
                result.add(new Integer[]{x-1, y+1});
            }
            if (getPiece(x-1, y+1) != null && getPiece(x-1, y+1).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x-1, y+1});
                break;
            }
            if (getPiece(x-1, y+1) != null && getPiece(x-1, y+1).getRole() == piece.getRole()) {
                break;
            }
            x--;
            y++;
        }

        x = piece.getPosX();
        y = piece.getPosY();

        while (!(x-1 < 0 || x > WIDTH-1) && !(y-1 < 0 || y > HEIGHT-1)) {
            if (getPiece(x-1, y-1) == null) {
                result.add(new Integer[]{x-1, y-1});
            }
            if (getPiece(x-1, y-1) != null && getPiece(x-1, y-1).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x-1, y-1});
                break;
            }
            if (getPiece(x-1, y-1) != null && getPiece(x-1, y-1).getRole() == piece.getRole()) {
                break;
            }
            x--;
            y--;
        }

        x = piece.getPosX();
        y = piece.getPosY();

        while (!(x < 0 || x+1 > WIDTH-1) && !(y-1 < 0 || y > HEIGHT-1)) {
            if (getPiece(x+1, y-1) == null) {
                result.add(new Integer[]{x+1, y-1});
            } else if (getPiece(x+1, y-1) != null && getPiece(x+1, y-1).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x+1, y-1});
                break;
            } else if (getPiece(x+1, y-1) != null && getPiece(x+1, y-1).getRole() == piece.getRole()) {
                break;
            }
            x++;
            y--;
        }
        return result;
    }

    public ArrayList<Integer[]> getRookCoveredFields(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        int x = piece.getPosX();
        int y = piece.getPosY();

        while (!(x+1 < 0 || x+1 > WIDTH-1) && !(y < 0 || y > HEIGHT-1)) {
            if (getPiece(x+1, y) == null) {
                result.add(new Integer[]{x+1, y});
            }
            if (getPiece(x+1, y) != null && getPiece(x+1, y).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x+1, y});
                break;
            }
            if (getPiece(x+1, y) != null && getPiece(x+1, y).getRole() == piece.getRole()) {
                break;
            }
            x++;
        }

        x = piece.getPosX();

        while (!(x-1 < 0 || x-1 > WIDTH-1) && !(y < 0 || y > HEIGHT-1)) {
            if (getPiece(x-1, y) == null) {
                result.add(new Integer[]{x-1, y});
            }
            if (getPiece(x-1, y) != null && getPiece(x-1, y).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x-1, y});
                break;
            }
            if (getPiece(x-1, y) != null && getPiece(x-1, y).getRole() == piece.getRole()) {
                break;
            }
            x--;
        }

        x = piece.getPosX();

        while (!(x < 0 || x > WIDTH-1) && !(y-1 < 0 || y-1 > HEIGHT-1)) {
            if (getPiece(x, y-1) == null) {
                result.add(new Integer[]{x, y-1});
            }
            if (getPiece(x, y-1) != null && getPiece(x, y-1).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x, y-1});
                break;
            }
            if (getPiece(x, y-1) != null && getPiece(x, y-1).getRole() == piece.getRole()) {
                break;
            }
            y--;
        }

        y = piece.getPosY();

        while (!(x < 0 || x > WIDTH-1) && !(y+1 < 0 || y+1 > HEIGHT-1)) {
            if (getPiece(x, y+1) == null) {
                result.add(new Integer[]{x, y+1});
            }
            if (getPiece(x, y+1) != null && getPiece(x, y+1).getRole() == piece.getOpposingRole()) {
                result.add(new Integer[]{x, y+1});
                break;
            }
            if (getPiece(x, y+1) != null && getPiece(x, y+1).getRole() == piece.getRole()) {
                break;
            }
            y++;
        }

        return result;
    }

    public ArrayList<Integer[]> getKnightCoveredFields(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        if (!(piece.getPosX()+2 < 0 || piece.getPosX()+2 > WIDTH-1) && !(piece.getPosY()+1 < 0 || piece.getPosY()+1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+2, piece.getPosY()+1) != null && getPiece(piece.getPosX()+2, piece.getPosY()+1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+2, piece.getPosY()+1) == null) {
                result.add(new Integer[]{piece.getPosX()+2, piece.getPosY()+1});
            }
        }
        if (!(piece.getPosX()+2 < 0 || piece.getPosX()+2 > WIDTH-1) && !(piece.getPosY()-1 < 0 || piece.getPosY()-1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+2, piece.getPosY()-1) != null && getPiece(piece.getPosX()+2, piece.getPosY()-1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+2, piece.getPosY()-1) == null) {
                result.add(new Integer[]{piece.getPosX()+2, piece.getPosY()-1});
            }
        }

        if (!(piece.getPosX()-2 < 0 || piece.getPosX()-2 > WIDTH-1) && !(piece.getPosY()+1 < 0 || piece.getPosY()+1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-2, piece.getPosY()+1) != null && getPiece(piece.getPosX()-2, piece.getPosY()+1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-2, piece.getPosY()+1) == null) {
                result.add(new Integer[]{piece.getPosX()-2, piece.getPosY()+1});
            }
        }
        if (!(piece.getPosX()-2 < 0 || piece.getPosX()-2 > WIDTH-1) && !(piece.getPosY()-1 < 0 || piece.getPosY()-1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-2, piece.getPosY()-1) != null && getPiece(piece.getPosX()-2, piece.getPosY()-1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-2, piece.getPosY()-1) == null) {
                result.add(new Integer[]{piece.getPosX()-2, piece.getPosY()-1});
            }
        }

        if (!(piece.getPosX()+1 < 0 || piece.getPosX()+1 > WIDTH-1) && !(piece.getPosY()+2 < 0 || piece.getPosY()+2 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+1, piece.getPosY()+2) != null && getPiece(piece.getPosX()+1, piece.getPosY()+2).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+1, piece.getPosY()+2) == null) {
                result.add(new Integer[]{piece.getPosX()+1, piece.getPosY()+2});
            }
        }
        if (!(piece.getPosX()+1 < 0 || piece.getPosX()+1 > WIDTH-1) && !(piece.getPosY()-2 < 0 || piece.getPosY()-2 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+1, piece.getPosY()-2) != null && getPiece(piece.getPosX()+1, piece.getPosY()-2).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+1, piece.getPosY()-2) == null) {
                result.add(new Integer[]{piece.getPosX()+1, piece.getPosY()-2});
            }
        }

        if (!(piece.getPosX()-1 < 0 || piece.getPosX()-1 > WIDTH-1) && !(piece.getPosY()+2 < 0 || piece.getPosY()+2 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-1, piece.getPosY()+2) != null && getPiece(piece.getPosX()-1, piece.getPosY()+2).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-1, piece.getPosY()+2) == null) {
                result.add(new Integer[]{piece.getPosX()-1, piece.getPosY()+2});
            }
        }
        if (!(piece.getPosX()-1 < 0 || piece.getPosX()-1 > WIDTH-1) && !(piece.getPosY()-2 < 0 || piece.getPosY()-2 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-1, piece.getPosY()-2) != null && getPiece(piece.getPosX()-1, piece.getPosY()-2).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-1, piece.getPosY()-2) == null) {
                result.add(new Integer[]{piece.getPosX()-1, piece.getPosY()-2});
            }
        }
        return result;
    }

    public ArrayList<Integer[]> getQueenCoveredFields(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        result.addAll(getBishopCoveredFields(piece));
        result.addAll(getRookCoveredFields(piece));

        return result;
    }

    public ArrayList<Integer[]> getKingCoveredFields(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        if (!(piece.getPosX()-1 < 0 || piece.getPosX()-1 > WIDTH-1) && !(piece.getPosY()-1 < 0 || piece.getPosY()-1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-1, piece.getPosY()-1) != null && getPiece(piece.getPosX()-1, piece.getPosY()-1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-1, piece.getPosY()-1) == null) {
                result.add(new Integer[]{piece.getPosX()-1, piece.getPosY()-1});
            }
        }

        if (!(piece.getPosX() < 0 || piece.getPosX() > WIDTH-1) && !(piece.getPosY()-1 < 0 || piece.getPosY()-1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX(), piece.getPosY()-1) != null && getPiece(piece.getPosX(), piece.getPosY()-1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX(), piece.getPosY()-1) == null) {
                result.add(new Integer[]{piece.getPosX(), piece.getPosY()-1});
            }
        }

        if (!(piece.getPosX()+1 < 0 || piece.getPosX()+1 > WIDTH-1) && !(piece.getPosY()-1 < 0 || piece.getPosY()-1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+1, piece.getPosY()-1) != null && getPiece(piece.getPosX()+1, piece.getPosY()-1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+1, piece.getPosY()-1) == null) {
                result.add(new Integer[]{piece.getPosX()+1, piece.getPosY()-1});
            }
        }

        if (!(piece.getPosX()-1 < 0 || piece.getPosX()-1 > WIDTH-1) && !(piece.getPosY() < 0 || piece.getPosY() > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-1, piece.getPosY()) != null && getPiece(piece.getPosX()-1, piece.getPosY()).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-1, piece.getPosY()) == null) {
                result.add(new Integer[]{piece.getPosX()-1, piece.getPosY()});
            }
        }

        if (!(piece.getPosX()+1 < 0 || piece.getPosX()+1 > WIDTH-1) && !(piece.getPosY() < 0 || piece.getPosY() > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+1, piece.getPosY()) != null && getPiece(piece.getPosX()+1, piece.getPosY()).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+1, piece.getPosY()) == null) {
                result.add(new Integer[]{piece.getPosX()+1, piece.getPosY()});
            }
        }

        if (!(piece.getPosX()-1 < 0 || piece.getPosX()-1 > WIDTH-1) && !(piece.getPosY()+1 < 0 || piece.getPosY()+1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()-1, piece.getPosY()+1) != null && getPiece(piece.getPosX()-1, piece.getPosY()+1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()-1, piece.getPosY()+1) == null) {
                result.add(new Integer[]{piece.getPosX()-1, piece.getPosY()+1});
            }
        }

        if (!(piece.getPosX() < 0 || piece.getPosX() > WIDTH-1) && !(piece.getPosY()+1 < 0 || piece.getPosY()+1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX(), piece.getPosY()+1) != null && getPiece(piece.getPosX(), piece.getPosY()+1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX(), piece.getPosY()+1) == null) {
                result.add(new Integer[]{piece.getPosX(), piece.getPosY()+1});
            }
        }

        if (!(piece.getPosX()+1 < 0 || piece.getPosX()+1 > WIDTH-1) && !(piece.getPosY()+1 < 0 || piece.getPosY()+1 > HEIGHT-1)) {
            if ((getPiece(piece.getPosX()+1, piece.getPosY()+1) != null && getPiece(piece.getPosX()+1, piece.getPosY()+1).getRole() != piece.getRole())
                    || getPiece(piece.getPosX()+1, piece.getPosY()+1) == null) {
                result.add(new Integer[]{piece.getPosX()+1, piece.getPosY()+1});
            }
        }

        return result;
    }

    public ArrayList<Integer[]> getPawnMovement(Piece piece) {
        ArrayList<Integer[]> result = new ArrayList<>();

        if (piece.getRole() == 'B') {
            if (pawnMovement(piece, piece.getPosX(), piece.getPosY()+1)) {
                result.add(new Integer[]{piece.getPosX(), piece.getPosY()+1});
            }
            if (pawnMovement(piece, piece.getPosX(), piece.getPosY()+2)) {
                result.add(new Integer[]{piece.getPosX(), piece.getPosY()+2});
            }
        } else {
            if (pawnMovement(piece, piece.getPosX(), piece.getPosY()-1)) {
                result.add(new Integer[]{piece.getPosX(), piece.getPosY()-1});
            }
            if (pawnMovement(piece, piece.getPosX(), piece.getPosY()-2)) {
                result.add(new Integer[]{piece.getPosX(), piece.getPosY()-2});
            }
        }
        return result;
    }

    private boolean pawnMovement(Piece piece, int newX, int newY) {
        if (piece.getRole() == 'B') {
            if (newX == piece.getPosX()) {
                //for the first move
                if (piece.getPosY() == 1) {
                    if (getPiece(piece.getPosX(), piece.getPosY() + 1) == null //checks if the square in front is empty
                            && (newY == piece.getPosY() + 1 //if chosen to move one tile to the front
                            || getPiece(piece.getPosX(), piece.getPosY() + 2) == null //checks if the square 2 tiles in front is empty
                            && (newY == piece.getPosY() + 2))) //if chosen to move two tiles to the front
                    {
                        return true;
                    }
                }
                //standard move
                if (getPiece(piece.getPosX(), piece.getPosY() + 1) == null //checks if the square in front is empty
                        && (newY == piece.getPosY() + 1)) { //if chosen to move one tile to the front
                    return true;
                }
            }
            //if taking a piece to the left
            if (!(piece.getPosX() - 1 < 0 || piece.getPosX() - 1 > WIDTH-1) && !(piece.getPosY() + 1 < 0 || piece.getPosY() + 1 > HEIGHT-1)) {
                if (getPiece(piece.getPosX() - 1, piece.getPosY() + 1) != null //checks if the square in front of the left is populated
                        && getPiece(piece.getPosX() - 1, piece.getPosY() + 1).getRole() != piece.getRole() //checks if piece is an enemy piece
                        && (newY == piece.getPosY() + 1) && newX == piece.getPosX() - 1) { //if chosen to move one tile to the front
                    return true;
                }
            }
            //if taking a piece to the right
            if (!(piece.getPosX() + 1 < 0 || piece.getPosX() + 1 > WIDTH-1) && !(piece.getPosY() + 1 < 0 || piece.getPosY() + 1 > HEIGHT-1)) {
                if (getPiece(piece.getPosX() + 1, piece.getPosY() + 1) != null //checks if the square in front of the left is populated
                        && getPiece(piece.getPosX() + 1, piece.getPosY() + 1).getRole() != piece.getRole() //checks if piece is an enemy piece
                        && (newY == piece.getPosY() + 1) && newX == piece.getPosX() + 1) { //if chosen to move one tile to the front
                    return true;
                }
            }
        }

        if (piece.getRole() == 'W') {
            if (newX == piece.getPosX()) {
                //for the first move
                if (piece.getPosY() == HEIGHT - 2) {
                    if (getPiece(piece.getPosX(), piece.getPosY() - 1) == null //checks if the square in front is empty
                            && (newY == piece.getPosY() - 1 //if chosen to move one tile to the front
                            || getPiece(piece.getPosX(), piece.getPosY() - 2) == null //checks if the square 2 tiles in front is empty
                            && (newY == piece.getPosY() - 2))) //if chosen to move two tiles to the front
                    {
                        return true;
                    }
                }
                //standard move
                if (getPiece(piece.getPosX(), piece.getPosY() - 1) == null //checks if the square in front is empty
                        && (newY == piece.getPosY() - 1)) { //if chosen to move one tile to the front
                    return true;
                }
            }
            //if taking a piece to the left
            if (!(piece.getPosX() - 1 < 0 || piece.getPosX() - 1 > WIDTH-1) && !(piece.getPosY() - 1 < 0 || piece.getPosY() - 1 > HEIGHT-1)) {
                if (getPiece(piece.getPosX() - 1, piece.getPosY() - 1) != null //checks if the square in front of the left is populated
                        && getPiece(piece.getPosX() - 1, piece.getPosY() - 1).getRole() != piece.getRole() //checks if piece is an enemy piece
                        && (newY == piece.getPosY() - 1) && newX == piece.getPosX() - 1) { //if chosen to move one tile to the front
                    return true;
                }
            }
            //if taking a piece to the right
            if (!(piece.getPosX() + 1 < 0 || piece.getPosX() + 1 > WIDTH-1) && !(piece.getPosY() - 1 < 0 || piece.getPosY() - 1 > HEIGHT-1)) {
                if (getPiece(piece.getPosX() + 1, piece.getPosY() - 1) != null //checks if the square in front of the left is populated
                        && getPiece(piece.getPosX() + 1, piece.getPosY() - 1).getRole() != piece.getRole() //checks if piece is an enemy piece
                        && (newY == piece.getPosY() - 1) && newX == piece.getPosX() + 1) { //if chosen to move one tile to the front
                    return true;
                }
            }
        }
        return false;
    }

    private boolean rookMovement(Piece piece, int newX, int newY) {
        //if horizontal movement
        if ((newX != piece.getPosX() && newY == piece.getPosY())) {
            int x = piece.getPosX();
            int y = piece.getPosY();
            while (x != newX) {
                if (x < newX) {
                    x++;
                }
                if (x > newX) {
                    x--;
                }
                if ((x != newX && y != newY) && getPiece(x, y) != null) {
                    return false;
                }
            }
            //check if the target square is empty or contains an enemy piece
            return !(getPiece(newX, newY) != null //If not-empty
                    && getPiece(newX, newY).getRole() == piece.getRole() //If the same colour
            );
        }
        //if vertical movement
        if ((newX == piece.getPosX() && newY != piece.getPosY())) {
            int x = piece.getPosX();
            int y = piece.getPosY();
            while (y != newY) {
                if (y < newY) {
                    y++;
                }
                if (y > newY) {
                    y--;
                }
                if ((x != newX && y != newY) && getPiece(x, y) != null) {
                    return false;
                }
            }
            //check if the target square is empty or contains an enemy piece
            return !(getPiece(newX, newY) != null //If not-empty
                    && getPiece(newX, newY).getRole() == piece.getRole() //If the same colour
            );
        }
        return false;
    }

    private boolean bishopMovement(Piece piece, int newX, int newY) {
        //diagonal change sum should be equal;
        if (Math.abs(newX - piece.getPosX()) == Math.abs(newY - piece.getPosY())) {
            //check for obstacles
            int x = piece.getPosX();
            int y = piece.getPosY();
            while (x != newX && y != newY) {
                if (newX - piece.getPosX() < 0) {
                    if (newY - piece.getPosY() < 0) {
                        x--;
                        y--;
                    }
                    if (newY - piece.getPosY() > 0) {
                        x--;
                        y++;
                    }
                }
                if (newX - piece.getPosX() > 0) {
                    if (newY - piece.getPosY() < 0) {
                        x++;
                        y--;
                    }
                    if (newY - piece.getPosY() > 0) {
                        x++;
                        y++;
                    }
                }
                if ((x != newX && y != newY) && getPiece(x, y) != null) {
                    return false;
                }
            }
            //check if the target square is empty or contains an enemy piece
            return !(getPiece(newX, newY) != null //If not-empty
                    && getPiece(newX, newY).getRole() == piece.getRole() //If the same colour
            );
        }
        return false;
    }

    private boolean knightMovement(Piece piece, int newX, int newY) {
        if (Math.abs(newX - piece.getPosX()) == 2 && Math.abs(newY - piece.getPosY()) == 1 || Math.abs(newX - piece.getPosX()) == 1 && Math.abs(newY - piece.getPosY()) == 2) {
            return !(getPiece(newX, newY) != null && getPiece(newX, newY).getRole() == piece.getRole());
        }
        return false;
    }

    private boolean queenMovement(Piece piece, int newX, int newY) {
        //diag and vert movement
        return (bishopMovement(piece, newX, newY) || rookMovement(piece, newX, newY));
    }

    private boolean kingMovement(Piece piece, int newX, int newY) {
        if ((Math.abs(newX - piece.getPosX()) == 1 && Math.abs(newY - piece.getPosY()) == 1) //if moving diagonal
                || (Math.abs(newX - piece.getPosX()) == 1 && Math.abs(newY - piece.getPosY()) == 0) //if moving horizontal
                || (Math.abs(newX - piece.getPosX()) == 0 && Math.abs(newY - piece.getPosY()) == 1)) //if moving vertical
        {
            return !(getPiece(newX, newY) != null && getPiece(newX, newY).getRole() == piece.getRole());
        }
        return false;
    }

    public Piece[][] getBoardPlacement() {
        return this.boardPlacement;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean isBlackChecked() {
        return blackChecked;
    }

    public boolean isWhiteChecked() {
        return whiteChecked;
    }

    public boolean isBlackMated() {
        return blackMated;
    }

    public boolean isWhiteMated() {
        return whiteMated;
    }
}
