package de.eudaemon.ideaswag;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.util.ui.JBUI;

import de.eudaemon.swag.SizeInfos;

class ComponentAppearancePanel extends JPanel {

    private final SizeInfos sizeInfos;
    private final RunningComponent component;

    ComponentAppearancePanel(RunningComponent component_) {
        component = component_;
        setLayout(new BorderLayout());
        sizeInfos = component.getSizeInfos();
        add(createSizeTablePanel(), BorderLayout.NORTH);
        ComponentVisualization view = new ComponentVisualization(component);
        add(view, BorderLayout.CENTER);
    }

    private Component createSizeTablePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c =
                new GridBagConstraints(
                        0,
                        0,
                        4,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL,
                        JBUI.insets(2),
                        0,
                        0);
        panel.add(new JLabel("<html><b>Sizes</b></html>"), c);
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        panel.add(new JLabel("actual"), c);
        c.gridx = GridBagConstraints.RELATIVE;
        addDimLabel(panel, sizeInfos.actualSize, c);
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("minimum"), c);
        c.gridx = GridBagConstraints.RELATIVE;
        addDimLabel(panel, sizeInfos.minimumSize.size, c);
        addCircle(panel, ComponentInfoPanel.MIN_SIZE_COLOR, c);
        if (sizeInfos.minimumSize.set) {
            panel.add(new JLabel(Actions.PinTab), c);
        }
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("preferred"), c);
        c.gridx = GridBagConstraints.RELATIVE;
        addDimLabel(panel, sizeInfos.preferredSize.size, c);
        addCircle(panel, ComponentInfoPanel.PREF_SIZE_COLOR, c);
        if (sizeInfos.preferredSize.set) {
            panel.add(new JLabel(Actions.PinTab), c);
        }
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("maximum"), c);
        c.gridx = GridBagConstraints.RELATIVE;
        addDimLabel(panel, sizeInfos.maximumSize.size, c);
        addCircle(panel, ComponentInfoPanel.MAX_SIZE_COLOR, c);
        if (sizeInfos.maximumSize.set) {
            panel.add(new JLabel(Actions.PinTab), c);
        }

        c.gridx = 4;
        c.gridy++;
        c.weighty = 1.0;
        c.weightx = 1.0;
        panel.add(new JLabel(""), c);

        return panel;
    }

    private void addDimLabel(JPanel container, Dimension dimension, GridBagConstraints c) {
        Insets oldInsets = c.insets;
        c.insets = JBUI.insets(2, 10, 2, 2);
        container.add(dimLabel(dimension), c);
        c.insets = oldInsets;
    }

    private void addCircle(JPanel container, Color color, GridBagConstraints c) {
        int oldAnchor = c.anchor;
        c.anchor = GridBagConstraints.CENTER;
        container.add(circle(color), c);
        c.anchor = oldAnchor;
    }

    private Component circle(Color color) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g)
                        .setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.fillOval(0, 0, 10, 10);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, 10);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
    }

    private JLabel dimLabel(Dimension dim) {
        return new JLabel(String.format("%dx%d", dim.width, dim.height));
    }
}
