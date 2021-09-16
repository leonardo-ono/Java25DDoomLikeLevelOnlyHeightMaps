package test;



import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author admin
 */
public class Keyboard implements KeyListener {

    public static boolean[] keyPressed = new boolean[256];
    
    public static boolean isKeyPressed(int keyCode) {
        return keyPressed[keyCode];
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressed[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyPressed[e.getKeyCode()] = false;
    }
    
}
