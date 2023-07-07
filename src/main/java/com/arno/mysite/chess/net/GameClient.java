package com.arno.mysite.chess.net;

import com.arno.mysite.chess.game.Game;

import java.net.DatagramSocket;
import java.net.Inet6Address;

public class GameClient implements Runnable {

    private Inet6Address inet6Address;
    private DatagramSocket datagramSocket;
    private Game game;
    @Override
    public void run() {

    }
}
