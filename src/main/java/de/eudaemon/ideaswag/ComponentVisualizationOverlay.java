package de.eudaemon.ideaswag;

import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ComponentVisualizationOverlay extends JPanel {
    public ComponentVisualizationOverlay() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 128));
        add(Box.createVerticalStrut(50));
        add(new JLabel("test"));
    }
}
