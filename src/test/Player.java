package test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 *
 * @author Leo
 */
public class Player {
    
    public BufferedImage terrain;
    public BufferedImage heightMap;
    private BufferedImage floor;
    private BufferedImage wall;
    private BufferedImage edge;
    private BufferedImage offscreen;
    private Graphics2D offscreenG;

    public double x = 400;
    public double y = 300;
    public double direction = 0; // angle in radians
    
    private final int blockedColor;

    public double height = 16;
    
    public Player(BufferedImage terrain, BufferedImage heightMap, BufferedImage floor, BufferedImage wall, BufferedImage edge, BufferedImage offscreen) {
        this.terrain = terrain;
        this.heightMap = heightMap;
        this.floor = floor;
        this.wall = wall;
        this.edge = edge;
        this.offscreen = offscreen;
        offscreenG = (Graphics2D) offscreen.getGraphics();
        blockedColor = terrain.getRGB(0, 0);
    }
    
    public void update() {
        double speed = 4.0;
        if (Keyboard.isKeyPressed(KeyEvent.VK_LEFT)) {
            direction -= 0.1;
        }
        else if (Keyboard.isKeyPressed(KeyEvent.VK_RIGHT)) {
            direction += 0.1;
        }
        
        if (Keyboard.isKeyPressed(KeyEvent.VK_UP)) {
            move(speed, 0);
        }
        else if (Keyboard.isKeyPressed(KeyEvent.VK_DOWN)) {
            move(-speed, 0);
        }

        if (Keyboard.isKeyPressed(KeyEvent.VK_Z)) {
            move(-speed, Math.toRadians(90));
        }
        else if (Keyboard.isKeyPressed(KeyEvent.VK_X)) {
            move(speed, Math.toRadians(90));
        }
        
        int collisionAdjust = 0;
        while ((normal = checkCollision(x, y)) != null) {
            x -= 0.05 * normal[0];
            y -= 0.05 * normal[1];
            collisionAdjust++;
        }
        if (collisionAdjust > 0) System.out.println("collision adjust: " + collisionAdjust);
    }
    
    private void move(double speed, double strafe) {
        double rx = Math.cos(direction + strafe);
        double ry = Math.sin(direction + strafe);
        x += speed * rx;
        y += speed * ry;
    }
    
    double[] normal; 

    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.drawRect((int) (x - 10), (int) (y - 10), 20, 20);
        
        double ad = 360.0 / circleDivision;
        for (double a = 0; a < 359; a += ad) {
            double ar = Math.toRadians(a);
            double vxc = radius * Math.cos(ar);
            double vyc = radius * Math.sin(ar);
            int cx = (int) (x + vxc);
            int cy = (int) (y + vyc);
            g.fillOval(cx - 1, cy - 1, 2, 2);
        }

        if (normal != null) {
            g.setColor(Color.BLUE);
            g.drawLine((int) x, (int) y, (int) (x + 50 * normal[0]), (int) (y + 50 * normal[1]));
        }
        
        castRays(g);
    }
    
    private void castRays(Graphics2D g) {
        
        int rayPreviousHeight = -1;
        
        int playerFloorHeight = ((heightMap.getRGB((int) x, (int) y) & 255)) / 8;
        
        offscreenG.clearRect(0, 0, 400, 300);
        double fov = Math.toRadians(60);
        double d = 200 / Math.tan(fov / 2);
        outer:
        for (int planeX = 0; planeX < 400; planeX++) {
            double a = Math.atan2(planeX - 200, d) + direction;
            double rx = Math.cos(a);
            double ry = Math.sin(a);
            
            int maxFloorY = 299;
            double cx = x;
            double cy = y;
            for (double depth = 1; depth < 1550; depth += 2.0) {
                cx = x + depth * rx;
                cy = y + depth * ry;
                //g.setColor(Color.GREEN);
                //g.drawLine((int) (cx), (int) (cy), (int) (cx + 1), (int) (cy + 1));
                
                boolean isWall = false;
                boolean isEndOfLevel = false;
                if (isBlocked((int) (cx), (int) (cy))) {
                    //continue outer;
                    isEndOfLevel = true;
                }
                
                
                int floorX = ((int) cx) & (floor.getWidth() - 1);
                int floorY = ((int) cy) & (floor.getHeight() - 1);
                int floorPixel = 0;
                int floorHeight = 0;
                try {
                    floorPixel = floor.getRGB(floorX, floorY);
                    floorHeight = (heightMap.getRGB((int) cx, (int) cy) & 255);
                    
                    if (rayPreviousHeight < 0) {
                        rayPreviousHeight = floorHeight;
                    }
                    
                    if (floorHeight - rayPreviousHeight > 10) {
                        floorPixel = 255;
                        isWall = true;
                    }
                    rayPreviousHeight = floorHeight;
                }
                catch (Exception e) {
                    //e.printStackTrace();
                }
                
                
                double z = depth * Math.cos(direction - a);
                double originalFloorScreenHeight = d * (floorHeight / 8) / z;
                
                int floorScreenY = (int) (d * ((playerFloorHeight + 24 + height - (floorHeight / 8) - 16) / z)) + 150;
                
                if (isEndOfLevel) {
                    floorScreenY = (int) (d * ((playerFloorHeight + 24 + height - 100) / z)) + 150;
                }
                
                //System.out.println("floorScrenY = " + floorScreenY);
                if (floorScreenY < 0) {
                    floorScreenY = 0;
                }
                else if (floorScreenY > offscreen.getHeight() - 1) {
                    floorScreenY = offscreen.getHeight() - 1;
                }

                //offscreen.setRGB(planeX, 150, 255);
                
                if (isWall) {
                    //try {
                    //    floorY = (int) (screenY % (floor.getHeight() - 1));
                    //    floorPixel = floor.getRGB(floorX, floorY);
                    //}
                    //catch (Exception e) {
                    //    e.printStackTrace();
                    //}
                    
                    // floorHeight -> height from height map
                    
                    int textureScreenHeight = maxFloorY - floorScreenY;
                    int dx1 = planeX;
                    int dy1 = maxFloorY;
                    int dx2 = dx1 + 1;
                    int dy2 = floorScreenY;
                    int sx1 = (int) (cx + cy) % wall.getWidth();
                    int sy1 = wall.getHeight() - floorHeight - 1;
                    int sx2 = sx1 + 1;
                    int sy2 = wall.getHeight() - 1;
                    offscreenG.drawImage(wall, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    
//                    for (int screenY = maxFloorY; screenY > floorScreenY; screenY--) {
//                        offscreen.setRGB(planeX, screenY, floorPixel);
//                    }
                }
                else {
                    for (int screenY = maxFloorY; screenY > floorScreenY; screenY--) {
                        offscreen.setRGB(planeX, screenY, floorPixel);
                    }
                }

                if (floorScreenY < maxFloorY) {
                    maxFloorY = floorScreenY;
                }
                
                if (isEndOfLevel) {
                    continue outer;
                }
            }
            
        }
    }
    
    private double radius = 16;
    private double circleDivision = 36;
    
    public double[] checkCollision(double px, double py) {
        double normalX = 0;
        double normalY = 0;
        double ad = 360.0 / circleDivision;
        boolean collided = false;
        for (double a = 0; a < 360; a += ad) {
            double ar = Math.toRadians(a);
            double vxc = radius * Math.cos(ar);
            double vyc = radius * Math.sin(ar);
            int cx = (int) (px + vxc);
            int cy = (int) (py + vyc);
            
            if (isBlocked(cx, cy)) {
                normalX += vxc;
                normalY += vyc;
                collided = true;
                // break;
            }
        }

        double length = Math.sqrt(normalX * normalX + normalY * normalY);
        if (length != 0) {
            normalX /= length;
            normalY /= length;
        }
        return collided ? new double[] { normalX, normalY } : null;
    }
    
    private boolean isBlocked(int x, int y) {
        try {
            return terrain.getRGB(x, y) == blockedColor;
        }
        catch (Exception e) {
            return true;
        }
    }
    
}
