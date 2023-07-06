package com.arno.mysite.chess.game;

import java.util.ArrayList;

public class Piece {
    private Pieces name;
    private int posX;
    private int posY;
    private char role;
    private ArrayList<Integer[]> coveredFields;

    private boolean moved;
    private boolean enPassant;

    public Piece(Pieces name, int posX, int posY, char role) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.role = role;
        this.coveredFields = new ArrayList<>();
        this.moved = false;
        this.enPassant = false;
    }

    public Pieces getName() {
        return name;
    }

    public void setName(Pieces name) {
        this.name = name;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public char getRole() {
        return role;
    }

    public void setRole(char role) {
        this.role = role;
    }

    public ArrayList<Integer[]> getCoveredFields() {
        return coveredFields;
    }

    public void setCoveredFields(ArrayList<Integer[]> coveredFields) {
        this.coveredFields = coveredFields;
    }

    public char getOpposingRole() {
        if (getRole() == 'B') {
            return 'W';
        } else {
            return 'B';
        }
    }

    public void printCoveredFields() {
        for (Integer[] integers : coveredFields) {
            System.out.println(integers[0] + " " + integers[1] + "  ");
        }
        System.out.println();
    }

    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public void setEnPassant(boolean enPassant) {
        this.enPassant = enPassant;
    }

    @Override
    public String toString() {
        return name + " " + posX + " " + posY + " " + role;
    }
}
