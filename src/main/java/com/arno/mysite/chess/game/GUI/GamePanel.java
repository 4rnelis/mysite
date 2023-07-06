package com.arno.mysite.chess.game.GUI;

import com.arno.mysite.chess.game.Game;
import com.arno.mysite.chess.game.Board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable{

    //Sets the framerate
    private final Integer FPS = 60;

    //Sets the dimensions of one entity object
    private final Integer ENTITY_WIDTH = 100;
    private final Integer ENTITY_HEIGHT = 100;

    private Thread gameThread;
    private Game game;
    private ArrayList<Image> images;

    //used for communication between MouseListener and update()
    private PipedReader pipedReader;
    private PipedWriter pipedWriter;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Board.WIDTH*ENTITY_WIDTH, Board.HEIGHT*ENTITY_HEIGHT));
        this.setBackground(Color.PINK);
        this.setDoubleBuffered(true);
        this.addMouseListener(new ClickListener());
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        //Loads images from files
        try {
            images = loadImages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        game = new Game();
        pipedWriter = new PipedWriter();

        //Connects the reader to a writer
        try {
            pipedReader = new PipedReader(pipedWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.paintComponent(getGraphics());
        Graphics2D graphics2D = (Graphics2D)getGraphics();

        double drawInterval = (double) 1000000000 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if(delta >= 1) {

                //Executes the game loop
                draw(graphics2D);
                update();

                delta--;
            }
        }
    }

    /**
     * Reads the input and if the move is made, game.getBoard() is reversed
     */
    public void update() {
        try {
            game.getBoard().setPiece(pipedReader.read(), pipedReader.read(), pipedReader.read(), pipedReader.read(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (game.getBoard().isBlackMated()) {
            System.out.println("WHITE WON!");
            gameThread = null;
        }

        if (game.getBoard().isWhiteMated()) {
            System.out.println("BLACK WON!");
            gameThread = null;
        }
    }


    /**
     * Draws the squares and pieces on the board
     * @param graphics2D != null;
     */
    public void draw(Graphics2D graphics2D) {

        Color color1 = new Color(0, 0, 0);
        Color color2 = new Color(255, 255, 255);

        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                if (i % 2 == 0) {
                    if (j % 2 == 0) {
                        drawRow(graphics2D, color2, i, j);
                    } else {
                        drawRow(graphics2D, color1, i, j);
                    }
                } else {
                    if (j % 2 == 0) {
                        drawRow(graphics2D, color1, i, j);
                    } else {
                        drawRow(graphics2D, color2, i, j);
                    }
                }
            }
        }

        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                if (game.getBoard().getBoardPlacement()[i][j] != null) {
                    if (game.getBoard().getBoardPlacement()[i][j].getRole() == 'B') {
                        switch (game.getBoard().getBoardPlacement()[i][j].getName()) {
                            case PAWN -> graphics2D.drawImage(images.get(0), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case ROOK -> graphics2D.drawImage(images.get(1), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case BISHOP -> graphics2D.drawImage(images.get(2), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case KNIGHT -> graphics2D.drawImage(images.get(3), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case QUEEN -> graphics2D.drawImage(images.get(4), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case KING -> graphics2D.drawImage(images.get(5), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                        }
                    } else {
                        switch (game.getBoard().getBoardPlacement()[i][j].getName()) {
                            case PAWN -> graphics2D.drawImage(images.get(6), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case ROOK -> graphics2D.drawImage(images.get(7), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case BISHOP -> graphics2D.drawImage(images.get(8), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case KNIGHT -> graphics2D.drawImage(images.get(9), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case QUEEN -> graphics2D.drawImage(images.get(10), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                            case KING -> graphics2D.drawImage(images.get(11), i * ENTITY_WIDTH, j * ENTITY_HEIGHT, ENTITY_WIDTH, ENTITY_HEIGHT,null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws squares according to position
     * color1/color2/color1/color2...
     * @param graphics2D != null
     * @param color1 != null
     * @param i != null
     * @param j != null
     */
    public void drawRow(Graphics2D graphics2D, Color color1, int i, int j) {
            graphics2D.setColor(color1);
//            graphics2D.setColor(color2);
        graphics2D.fillRect(
                i * ENTITY_WIDTH, //starting x
                j * ENTITY_HEIGHT, //starting y
                ENTITY_WIDTH, //width
                ENTITY_HEIGHT //height
        );
    }

    /**
     * Loads images being used for pieces
     * @return ArrayList of loaded images
     */
    public ArrayList<Image> loadImages() throws IOException {
        File img;
        BufferedImage image;

        ArrayList<Image> result = new ArrayList<>();
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/pawn_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/rook_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/bishop_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/knight_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/queen_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/king_black.png");
        image = ImageIO.read(img);
        result.add(image);

        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/pawn_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/rook_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/bishop_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/knight_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/queen_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/game/GUI/images/king_white.png");
        image = ImageIO.read(img);
        result.add(image);

        return result;
    }

    private class ClickListener extends MouseAdapter {

        //Checks if 1st or 2nd click
        private boolean place = false;

        /**
         * Sends the coordinates after first and second clicks to the reader
         * @param event the event to be processed
         */
        @Override
        public void mousePressed(MouseEvent event) {

            try {
                pipedWriter.write(event.getX() / ENTITY_WIDTH);
                pipedWriter.write(event.getY() / ENTITY_HEIGHT);

                if (place) {
                    pipedWriter.flush();
                }

                place = !place;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
