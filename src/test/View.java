package test;


import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author admin
 */
public class View extends Canvas implements Runnable {
    
    private BufferStrategy bs;
    private boolean running;
    
    private BufferedImage map;
    private BufferedImage floor;
    private BufferedImage wall;
    private BufferedImage height;
    private BufferedImage edge;
    private BufferedImage offscreen;
    
    private Player player;
    
    public View() {
        try {
            map = ImageIO.read(getClass().getResourceAsStream("/res/map.png"));
            height = ImageIO.read(getClass().getResourceAsStream("/res/height.png"));
            floor = ImageIO.read(getClass().getResourceAsStream("/res/bricks2.jpg"));
            wall = ImageIO.read(getClass().getResourceAsStream("/res/bricks2_vertical.png"));
            edge = ImageIO.read(getClass().getResourceAsStream("/res/edge.png"));
            offscreen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
            player = new Player(map, height, floor, wall, edge, offscreen);
        } catch (IOException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    public void start() {
        createBufferStrategy(2);
        bs = getBufferStrategy();
        createAllEntities();
        startAllEntities();
        new Thread(this).start();
        addKeyListener(new Keyboard());
    }

    private void createAllEntities() {
    }

    private void startAllEntities() {
    }
    
    @Override
    public void run() {
        running = true;
        long previousTime = System.nanoTime();
        long deltaTime = 0;
        long unprocessedTime = 0;
        long timePerFrame = 1000000000 / 60;
        while (running) {
            long currentTime = System.nanoTime();
            deltaTime = currentTime - previousTime;
            previousTime = currentTime;
            unprocessedTime += deltaTime;
            while (unprocessedTime > timePerFrame) {
                unprocessedTime -= timePerFrame;
                update();
            }
            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            draw((Graphics2D) g);
            g.dispose();
            bs.show();
            
            //try {
            //    Thread.sleep(1000 / 240);
            //} catch (InterruptedException ex) {
            //}
        }
    }

    private void update() {
        player.update();
    }
    
    private void draw(Graphics2D g) {
        g.clearRect(0, 0, 800, 600);
        g.drawImage(map, 0, 0, null);
        player.draw(g);
        g.drawImage(offscreen, 0, 0, 800, 600, null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View view = new View();
            view.setPreferredSize(new Dimension(800, 600));
            JFrame frame = new JFrame();
            frame.setTitle("");
            frame.getContentPane().add(view);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
            view.start();
        });
    }

}
