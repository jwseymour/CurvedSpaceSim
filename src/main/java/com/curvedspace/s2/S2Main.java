package com.curvedspace.s2;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class S2Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("S^2 Geodesic Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            S2Panel panel = new S2Panel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
