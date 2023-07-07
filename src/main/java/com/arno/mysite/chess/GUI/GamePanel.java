package com.arno.mysite.chess.GUI;

import com.arno.mysite.chess.game.Game;
import com.arno.mysite.chess.game.Board;
import com.arno.mysite.chess.game.Piece;

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
    private final Integer ENTITY_WIDTH = 75;
    private final Integer ENTITY_HEIGHT = 75;

    private Thread gameThread;
    private Game game;
    private ArrayList<Image> images;

    //used for communication between MouseListener and update()
    private PipedReader inputReader;
    private PipedWriter inputWriter;
    private PipedReader gameReader;
    private Boolean promotePawn;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Board.WIDTH*ENTITY_WIDTH + ENTITY_WIDTH, Board.HEIGHT*ENTITY_HEIGHT));
        this.setBackground(new Color(255, 255, 255));
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
        inputWriter = new PipedWriter();

        promotePawn = false;

        //Connects the reader to a writer
        try {
            inputReader = new PipedReader(inputWriter);
            gameReader = new PipedReader(game.getBoard().getPipedWriter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.paintComponent(getGraphics());
        Graphics2D graphics2D = (Graphics2D)getGraphics();

        double drawInterval = (double) 1000000000 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        draw(graphics2D, false, -1, -1);

        while(gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if(delta >= 1) {

                //Executes the game loop
                update(graphics2D);

                delta--;
            }
        }
    }

    /**
     * Reads the input and if the move is made, game.getBoard() is reversed
     */
    public void update(Graphics2D graphics2D) {

        int response;
        int x, y;
        Piece piece = null;

        int indicatedX = -1, indicatedY = -1;
        int targetX, targetY;

        try {
            //Calls promotePawn function
            if (promotePawn) {
                x = inputReader.read();
                y = inputReader.read();
                while (!(x == Board.WIDTH && (y == 0 || y == 1 || y == 2 || y == 3 || y == 4))) {
                    x = inputReader.read();
                    y = inputReader.read();
                }

                if (game.getBoard().isReversed()) {
                    switch (y) {
                        case 0 -> piece = game.getBoard().promotePawn('R', 'W');
                        case 1 -> piece = game.getBoard().promotePawn('B', 'W');
                        case 2 -> piece = game.getBoard().promotePawn('K', 'W');
                        case 3 -> piece = game.getBoard().promotePawn('Q', 'W');
                    }
                }
                if (!game.getBoard().isReversed()) {
                    switch (y) {
                        case 0 -> piece = game.getBoard().promotePawn('R', 'B');
                        case 1 -> piece = game.getBoard().promotePawn('B', 'B');
                        case 2 -> piece = game.getBoard().promotePawn('K', 'B');
                        case 3 -> piece = game.getBoard().promotePawn('Q', 'B');
                    }
                }
            }

            promotePawn = false;

            if (piece != null) {
                draw(graphics2D, false, -1, -1);
            }

            if (!game.getBoard().isReversed()) {
                while (true) {
                    //Prevent ArrayOutOfBounds exception
                    indicatedX = inputReader.read();
                    indicatedY = inputReader.read();
                    if (indicatedX < Board.WIDTH && indicatedY < Board.HEIGHT) {
                        if (game.getBoard().getPiece(indicatedX, indicatedY) != null && game.getBoard().getPiece(indicatedX, indicatedY).getRole() != 'B') {
                            break;
                        }
                    }
                }
            }
            if (game.getBoard().isReversed()) {
                //Prevent ArrayOutOfBounds exception
                while (true) {
                    //Prevent ArrayOutOfBounds exception
                    indicatedX = inputReader.read();
                    indicatedY = inputReader.read();
                    if (indicatedX < Board.WIDTH && indicatedY < Board.HEIGHT) {
                        if (game.getBoard().getPiece(indicatedX, indicatedY) != null && game.getBoard().getPiece(indicatedX, indicatedY).getRole() != 'W') {
                            break;
                        }
                    }
                }
            }


            draw(graphics2D, true, indicatedX, indicatedY);

            targetX = inputReader.read();
            targetY = inputReader.read();
            while (!(targetX < Board.WIDTH && targetY < Board.HEIGHT)) {
                targetX = inputReader.read();
                targetY = inputReader.read();
            }

            game.getBoard().setPiece(indicatedX, indicatedY, targetX, targetY, false);

            //Checks for input regarding pawn promotion
            response = gameReader.read();
            if (response == 0) {
                promotePawn = false;
            } else if (response == 1) {
                promotePawn = true;
            }
            draw(graphics2D, false, -1, -1);

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
    public void draw(Graphics2D graphics2D, boolean indicated, int indicatedX, int indicatedY) {

        Color color1 = new Color(0, 0, 0);
        Color color2 = new Color(255, 255, 255);
        Color color3 = new Color(100, 50, 230);

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

        if (indicated) {
            graphics2D.setColor(color3);
            graphics2D.fillRect(
                    indicatedX * ENTITY_WIDTH,
                    indicatedY * ENTITY_HEIGHT,
                    ENTITY_WIDTH,
                    ENTITY_HEIGHT);
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

        //SIDEBAR
        if (game.getBoard().isReversed()) {
            graphics2D.setColor(color1);
        } else {
            graphics2D.setColor(color2);
        }
        graphics2D.fillRect(
                Board.WIDTH * ENTITY_WIDTH,
                0,
                ENTITY_WIDTH,
                Board.HEIGHT * ENTITY_HEIGHT);

        if (promotePawn) {
            if (!game.getBoard().isReversed()) {
                graphics2D.drawImage(images.get(1), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 0, ENTITY_WIDTH, ENTITY_HEIGHT, null);
                graphics2D.drawImage(images.get(2), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 1, ENTITY_WIDTH, ENTITY_HEIGHT, null);
                graphics2D.drawImage(images.get(3), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 2, ENTITY_WIDTH, ENTITY_HEIGHT, null);
                graphics2D.drawImage(images.get(4), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 3, ENTITY_WIDTH, ENTITY_HEIGHT, null);
            }
            if (game.getBoard().isReversed()) {
                graphics2D.drawImage(images.get(7), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 0, ENTITY_WIDTH, ENTITY_HEIGHT, null);
                graphics2D.drawImage(images.get(8), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 1, ENTITY_WIDTH, ENTITY_HEIGHT, null);
                graphics2D.drawImage(images.get(9), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 2, ENTITY_WIDTH, ENTITY_HEIGHT, null);
                graphics2D.drawImage(images.get(10), Board.WIDTH * ENTITY_WIDTH, ENTITY_WIDTH * 3, ENTITY_WIDTH, ENTITY_HEIGHT, null);
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
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/pawn_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/rook_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/bishop_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/knight_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/queen_black.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/king_black.png");
        image = ImageIO.read(img);
        result.add(image);

        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/pawn_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/rook_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/bishop_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/knight_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/queen_white.png");
        image = ImageIO.read(img);
        result.add(image);
        img = new File("src/main/java/com/arno/mysite/chess/GUI/images/king_white.png");
        image = ImageIO.read(img);
        result.add(image);

        return result;
    }

    private class ClickListener extends MouseAdapter {

        /**
         * Sends the coordinates of clicks to the reader
         * @param event the event to be processed
         */
        @Override
        public void mousePressed(MouseEvent event) {
            try {
                inputWriter.write(event.getX() / ENTITY_WIDTH);
                inputWriter.write(event.getY() / ENTITY_HEIGHT);
                inputWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
