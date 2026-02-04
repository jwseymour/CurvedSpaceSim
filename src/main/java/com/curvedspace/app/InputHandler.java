package com.curvedspace.app;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {
    private final Set<Integer> keys = new HashSet<>();

    // Mouse state
    private int lastX = -1;
    private int lastY = -1;
    private int dx = 0;
    private int dy = 0;
    private boolean dragging = false;

    public boolean isPressed(int keyCode) {
        return keys.contains(keyCode);
    }

    // Get and reset deltas (poll once per frame)
    public int getDx() {
        int val = dx;
        dx = 0;
        return val;
    }

    public int getDy() {
        int val = dy;
        dy = 0;
        return val;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastX != -1) {
            dx += e.getX() - lastX;
            dy += e.getY() - lastY;
        }
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
        dragging = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
