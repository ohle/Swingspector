package de.eudaemon.ideaswag;

import java.util.EventListener;
import java.util.Objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.event.EventListenerList;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.Gray;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI.Borders;

import de.eudaemon.swag.SizeInfos;
import org.jetbrains.annotations.NotNull;

class ComponentVisualization extends JLayeredPane {

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
    private final View view;
    private final JLabel positionLabel;
    private final JLabel measurementLabel;
    private RunningComponent componentUnderMouse = null;

    private final AffineTransform contentTransform =
            AffineTransform.getTranslateInstance(-RULER_SIZE, -RULER_SIZE);

    ComponentVisualization(RunningComponent component_) {
        component = component_;
        sizing = component.getSizeInfos();
        snapshot = component.getSnapshot().getImage();
        view = new View();
        JBScrollPane scrollPane = new JBScrollPane(view);
        add(scrollPane, DEFAULT_LAYER);
        positionLabel = createHoverLabel("n/a", Borders.empty(5, 5, 1, 5));
        positionLabel.setVisible(false);
        measurementLabel = createHoverLabel("Click and drag to measure", Borders.empty(1, 5, 5, 5));
        add(positionLabel, POPUP_LAYER);
        add(measurementLabel, POPUP_LAYER);
    }

    private JLabel createHoverLabel(String defaultText, Border border) {
        JLabel label = new JLabel(defaultText);
        label.setOpaque(true);
        label.setBackground(new Color(0, 0, 0, 128));
        label.setBorder(border);
        return label;
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            int w = getWidth();
            int h = getHeight();
            for (Component defaultComponent : getComponentsInLayer(DEFAULT_LAYER)) {
                defaultComponent.setBounds(0, 0, w, h);
            }
            int offset = RULER_SIZE + 10;
            for (Component hoverComponent : getComponentsInLayer(POPUP_LAYER)) {
                if (!hoverComponent.isVisible()) {
                    continue;
                }
                Dimension size = hoverComponent.getPreferredSize();
                hoverComponent.setBounds(
                        w - size.width - 10, offset, (int) size.getWidth(), (int) size.getHeight());
                offset += size.height;
            }
        }
    }

    public void addHoverComponentListener(HoverComponentListener listener) {
        listeners.add(HoverComponentListener.class, listener);
    }

    public void removeHoverComponentListener(HoverComponentListener listener) {
        listeners.remove(HoverComponentListener.class, listener);
    }

    private void updateComponentUnderMouse() {
        RunningComponent c = component.getComponentAt(getMousePositionOnComponent());
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

    private Point2D getMousePositionOnComponent() {
        Point mousePosition = getMousePosition();
        if (mousePosition == null) {
            return null;
        }
        return contentTransform.transform(
                SwingUtilities.convertPoint(this, mousePosition, view), null);
    }

    public interface HoverComponentListener extends EventListener {
        void componentChanged(RunningComponent component);
    }

    private class View extends JPanel {
        public View() {
            setBorder(new EmptyBorder(JBInsets.create(10, 10)));
            addMouseMotionListener(
                    new MouseMotionAdapter() {
                        @Override
                        public void mouseMoved(MouseEvent e) {
                            Point2D pos = getMousePositionOnComponent();
                            if (pos.getX() > 0 && pos.getY() > 0) {
                                positionLabel.setVisible(true);
                                positionLabel.setText(
                                        String.format(
                                                "%d, %d", (int) pos.getX(), (int) pos.getY()));
                            } else {
                                positionLabel.setVisible(false);
                            }
                            ComponentVisualization.this.repaint();
                            updateComponentUnderMouse();
                        }
                    });
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
            for (int y = 100; y < getHeight(); y += 100) {
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
            Point2D pos = getMousePositionOnComponent();
            if (pos == null) {
                return;
            }
            g.setColor(GUIDE_COLOR);
            g.setStroke(GUIDE_STROKE);
            g.drawLine((int) pos.getX(), (int) pos.getY(), (int) pos.getX(), 0);
            g.drawLine((int) pos.getX(), (int) pos.getY(), 0, (int) pos.getY());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                    snapshot.getWidth() + RULER_SIZE, snapshot.getHeight() + RULER_SIZE);
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
}
