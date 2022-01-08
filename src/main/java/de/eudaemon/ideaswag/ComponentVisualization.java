package de.eudaemon.ideaswag;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import javax.swing.border.EmptyBorder;

import com.intellij.ui.Gray;
import com.intellij.util.ui.JBInsets;

import de.eudaemon.swag.SizeInfos;

class ComponentVisualization extends JPanel {

    private static final int SIZE_CUTOFF = 200;

    private final SizeInfos sizing;
    private final BufferedImage snapshot;
    private final Stroke normalStroke = new BasicStroke(1);
    private final Stroke croppedStroke =
            new BasicStroke(
                    1F,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    new float[] {10.0f, 5.0f},
                    0.0f);
    private final RunningComponent component;

    ComponentVisualization(RunningComponent component_) {
        component = component_;
        sizing = component.getSizeInfos();
        snapshot = component.getSnapshot().getImage();
        setBorder(new EmptyBorder(JBInsets.create(10, 10)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        drawCheckerBoard(g2d);
        if (snapshot != null) {
            g2d.drawImage(snapshot, 0, 0, null);
        }
        g2d.setColor(ComponentInfoPanel.MAX_SIZE_COLOR);
        if (isMaximumSizeCropped()) {
            g2d.setStroke(croppedStroke);
            Dimension size = getCroppedMaximumSize();
            g2d.drawRect(0, 0, size.width, size.height);
        } else {
            g2d.setStroke(normalStroke);
            g2d.drawRect(0, 0, sizing.maximumSize.size.width, sizing.maximumSize.size.height);
        }
        g2d.setStroke(normalStroke);
        g2d.setColor(ComponentInfoPanel.PREF_SIZE_COLOR);
        g2d.drawRect(0, 0, sizing.preferredSize.size.width, sizing.preferredSize.size.height);
        g2d.setColor(ComponentInfoPanel.MIN_SIZE_COLOR);
        g2d.drawRect(0, 0, sizing.minimumSize.size.width, sizing.minimumSize.size.height);
    }

    private void drawCheckerBoard(Graphics2D g) {
        int squareSize = 5;
        for (int y = 0; y * squareSize < getHeight(); y++) {
            for (int x = 0; x * squareSize < getWidth(); x++) {
                g.setColor(x % 2 == y % 2 ? Gray._150 : Gray._70);
                g.fillRect(x * squareSize, y * squareSize, squareSize, squareSize);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension size = getCroppedMaximumSize();
        return new Dimension(size.width + 2, size.height + 2);
    }

    private Dimension getCroppedMaximumSize() {
        int w = Math.max(sizing.actualSize.width, sizing.minimumSize.size.width);
        int h = Math.max(sizing.actualSize.height, sizing.minimumSize.size.height);
        int croppedWidth =
                sizing.maximumSize.size.width - w > SIZE_CUTOFF
                        ? w + SIZE_CUTOFF
                        : sizing.maximumSize.size.width;
        int croppedHeight =
                sizing.maximumSize.size.height - h > SIZE_CUTOFF
                        ? h + SIZE_CUTOFF
                        : sizing.maximumSize.size.width;
        return new Dimension(croppedWidth, croppedHeight);
    }

    private boolean isMaximumSizeCropped() {
        return sizing.maximumSize.size.width - sizing.actualSize.width > SIZE_CUTOFF
                || sizing.maximumSize.size.height - sizing.actualSize.height > SIZE_CUTOFF;
    }
}
