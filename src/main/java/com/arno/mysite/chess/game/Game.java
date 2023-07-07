package com.arno.mysite.chess.game;

import com.arno.mysite.chess.GUI.GamePanel;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Game {

    private final ArrayList<Player> players;
    private final Board board;
    private boolean isRunning;
    private final Scanner sc;

    public Game() {
        players = new ArrayList<>();
        board = new Board();
        this.isRunning = false;
        sc = new Scanner(System.in);
    }

    /**
     * For starting the TUI game
     */
    public void startGame(String ui) {
        if (ui.equals("TUI")) {
            this.isRunning = true;
            while (isRunning) {
                board.printBoard();
                board.setPiece(sc.nextInt(), sc.nextInt(), sc.nextInt(), sc.nextInt(), false);
            }
        }
        if (ui.equals("GUI")) {
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setTitle("chess");

            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);

            window.pack();

            window.setLocationRelativeTo(null);
            window.setVisible(true);

            gamePanel.startGameThread();
        }
    }

    public void startRunning() {
        this.isRunning = true;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public boolean getRunning() {
        return this.isRunning;
    }

    public Scanner getSc() {
        return this.sc;
    }
}
