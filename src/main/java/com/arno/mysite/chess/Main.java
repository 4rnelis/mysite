package com.arno.mysite.chess;

import com.arno.mysite.chess.game.Game;

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.startGame("GUI");
    }
}
