package de.eudaemon.ideaswag;

import java.util.EventListener;
import java.util.Objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import javax.swing.event.EventListenerList;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.Gray;
import com.intellij.util.ui.JBInsets;

import de.eudaemon.swag.SizeInfos;
import org.jetbrains.annotations.NotNull;

class ComponentVisualization extends JPanel {

    private static final int SIZE_CUTOFF = 200;
    private static final int RULER_SIZE = 20;
    private static final Color GUIDE_COLOR = Gray._40;

    private final SizeInfos sizing;
    private final BufferedImage snapshot;
    private static final Stroke NORMAL_STROKE = new BasicStroke(1);
    private static final Stroke CROPPED_STROKE =
            new BasicStroke(
                    1F,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    new float[] {10.0f, 5.0f},
                    0.0f);
    private static final BasicStroke GUIDE_STROKE =
            new BasicStroke(
                    1,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    new float[] {2.0f, 5.0f},
                    0.0f);
    private final RunningComponent component;
    private static final @NotNull Logger LOG = Logger.getInstance(ComponentVisualization.class);

    private final EventListenerList listeners = new EventListenerList();
    private RunningComponent componentUnderMouse = null;

    private final AffineTransform contentTransform =
            AffineTransform.getTranslateInstance(-RULER_SIZE, -RULER_SIZE);

    ComponentVisualization(RunningComponent component_) {
        System.out.println(UIManager.getColor(EditorColors.GUTTER_BACKGROUND));
        component = component_;
        sizing = component.getSizeInfos();
        snapshot = component.getSnapshot().getImage();
        addMouseMotionListener(
                new MouseAdapter() {

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        repaint();
                        updateComponentUnderMouse();
                    }
                });
        setBorder(new EmptyBorder(JBInsets.create(10, 10)));
    }

    public void addHoverComponentListener(HoverComponentListener listener) {
        listeners.add(HoverComponentListener.class, listener);
    }

    public void removeHoverComponentListener(HoverComponentListener listener) {
        listeners.remove(HoverComponentListener.class, listener);
    }

    private void updateComponentUnderMouse() {
        Point2D pos = contentTransform.transform(getMousePosition(), null);
        RunningComponent c = component.getComponentAt(pos);
        boolean componentChanged =
                (c == null && componentUnderMouse != null)
                        || (!Objects.equals(c, componentUnderMouse));
        componentUnderMouse = c;
        if (componentChanged) {
            for (HoverComponentListener l : listeners.getListeners(HoverComponentListener.class)) {
                l.componentChanged(componentUnderMouse);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        paintRulers(g2d);
        g2d.translate(RULER_SIZE, RULER_SIZE);
        paintCheckerBoard(g2d);
        if (snapshot != null) {
            g2d.drawImage(snapshot, 0, 0, null);
        }
        paintSizeRectangles(g2d);
        paintPositionGuides(g2d);
    }

    private void paintSizeRectangles(Graphics2D g2d) {
        g2d.setColor(ComponentInfoPanel.MAX_SIZE_COLOR);
        if (isMaximumSizeCropped()) {
            g2d.setStroke(CROPPED_STROKE);
            Dimension size = getCroppedMaximumSize();
            g2d.drawRect(0, 0, size.width, size.height);
        } else {
            g2d.setStroke(NORMAL_STROKE);
            g2d.drawRect(0, 0, sizing.maximumSize.size.width, sizing.maximumSize.size.height);
        }
        g2d.setStroke(NORMAL_STROKE);
        g2d.setColor(ComponentInfoPanel.PREF_SIZE_COLOR);
        g2d.drawRect(0, 0, sizing.preferredSize.size.width, sizing.preferredSize.size.height);
        g2d.setColor(ComponentInfoPanel.MIN_SIZE_COLOR);
        g2d.drawRect(0, 0, sizing.minimumSize.size.width, sizing.minimumSize.size.height);
    }

    private void paintRulers(Graphics2D g) {
        EditorColorsScheme editorScheme = EditorColorsManager.getInstance().getGlobalScheme();
        Color bg = editorScheme.getColor(EditorColors.GUTTER_BACKGROUND);
        Color fg = editorScheme.getColor(EditorColors.LINE_NUMBERS_COLOR);
        RenderingHints oldRenderingHints = g.getRenderingHints();

        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), RULER_SIZE);
        g.fillRect(0, 0, RULER_SIZE, getHeight());
        g.setColor(fg);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (int x = RULER_SIZE; x < getWidth(); x += 2) {
            int len = ticLength(x - RULER_SIZE);
            g.drawLine(x, RULER_SIZE, x, RULER_SIZE - len);
        }
        for (int y = RULER_SIZE; y < getHeight(); y += 2) {
            int len = ticLength(y - RULER_SIZE);
            g.drawLine(RULER_SIZE - len, y, RULER_SIZE, y);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int x = 100; x < getWidth(); x += 100) {
            String label = String.valueOf(x);
            double width = g.getFontMetrics().getStringBounds(label, g).getWidth();
            g.drawString(label, (int) (x + RULER_SIZE - width / 2.0), RULER_SIZE - 10);
        }
        for (int y = 100; y < getWidth(); y += 100) {
            AffineTransform transform = g.getTransform();
            String label = String.valueOf(y);
            double width = g.getFontMetrics().getStringBounds(label, g).getWidth();
            g.rotate(3.0 * Math.PI / 2.0, RULER_SIZE - 10, y + RULER_SIZE);
            g.drawString(label, (int) (RULER_SIZE - 10 - width / 2.0), y + RULER_SIZE);
            g.setTransform(transform);
        }
        g.setRenderingHints(oldRenderingHints);
    }

    private int ticLength(int x) {
        int len = 5;
        if (x % 50 == 0) {
            len = 10;
        } else if (x % 10 == 0) {
            len = 7;
        }
        return len;
    }

    private void paintCheckerBoard(Graphics2D g) {
        int squareSize = 5;
        for (int y = 0; y * squareSize < getHeight(); y++) {
            for (int x = 0; x * squareSize < getWidth(); x++) {
                g.setColor(x % 2 == y % 2 ? Gray._150 : Gray._70);
                g.fillRect(x * squareSize, y * squareSize, squareSize, squareSize);
            }
        }
    }

    private void paintPositionGuides(Graphics2D g) {
        Point mousePosition = getMousePosition();
        if (mousePosition == null) {
            return;
        }
        try {
            Point2D pos = g.getTransform().inverseTransform(mousePosition, null);
            g.setColor(GUIDE_COLOR);
            g.setStroke(GUIDE_STROKE);
            g.drawLine((int) pos.getX(), (int) pos.getY(), (int) pos.getX(), 0);
            g.drawLine((int) pos.getX(), (int) pos.getY(), 0, (int) pos.getY());
        } catch (NoninvertibleTransformException e) {
            LOG.error(e);
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

    public interface HoverComponentListener extends EventListener {
        void componentChanged(RunningComponent component);
    }
}
