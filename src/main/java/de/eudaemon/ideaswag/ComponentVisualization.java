package de.eudaemon.ideaswag;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.Gray;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI.Borders;

import de.eudaemon.swag.SizeInfos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

class ComponentVisualization extends JLayeredPane {

    private static final int SIZE_CUTOFF = 200;
    private static final int RULER_SIZE = 20;
    private static final Color GUIDE_COLOR = Gray._40;

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

    private final View view;
    private final JLabel positionLabel;
    private final JLabel measurementLabel;
    private final JLabel componentLabel;
    private RunningComponent componentUnderMouse = null;

    private final DistanceMeasurement distanceMeasurement = new DistanceMeasurement();
    private final AffineTransform contentTransform =
            AffineTransform.getTranslateInstance(-RULER_SIZE, -RULER_SIZE);
    private final Disposable disposer;

    ComponentVisualization(RunningComponent component_, Disposable disposer_) {
        component = component_;
        disposer = disposer_;
        snapshot = component.getSnapshot().getImage();
        view = new View();
        JBScrollPane scrollPane = new JBScrollPane(view);
        add(scrollPane, DEFAULT_LAYER);
        componentLabel =
                createHoverLabel(
                        Util.generateTitle(component.getDescription()), Borders.empty(5, 5, 1, 5));
        positionLabel = createHoverLabel("n/a", Borders.empty(1, 5));
        positionLabel.setVisible(false);
        measurementLabel = createHoverLabel("Click and drag to measure", Borders.empty(1, 5, 5, 5));
        add(componentLabel, POPUP_LAYER);
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

    private void updateComponentUnderMouse() {
        RunningComponent c = component.getComponentAt(getMousePositionOnComponent());
        boolean componentChanged =
                (c == null && componentUnderMouse != null)
                        || (!Objects.equals(c, componentUnderMouse));
        componentUnderMouse = c;
        if (componentChanged) {
            componentLabel.setText(Util.generateTitle(getSelectedComponent().getDescription()));
            ComponentVisualization.this.repaint();
        }
    }

    private RunningComponent getSelectedComponent() {
        return componentUnderMouse == null ? component : componentUnderMouse;
    }

    private Point2D getMousePositionOnComponent() {
        Point mousePosition = getMousePosition();
        if (mousePosition == null) {
            return null;
        }
        return contentTransform.transform(
                SwingUtilities.convertPoint(this, mousePosition, view), null);
    }

    private class View extends JPanel {

        public View() {
            setBorder(new EmptyBorder(JBInsets.create(10, 10)));
            addMouseMotionListener(
                    new MouseMotionAdapter() {
                        @Override
                        public void mouseMoved(MouseEvent e) {
                            Point2D pos = getMousePositionOnComponent();
                            if (pos == null) {
                                return;
                            }
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
            MouseAdapter distanceUpdateListener =
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getButton() != MouseEvent.BUTTON1) {
                                distanceMeasurement.reset();
                                updateDistanceLabel();
                            } else if (e.getClickCount() > 1) {
                                if (componentUnderMouse != null) {
                                    Util.openComponentTab(componentUnderMouse, disposer);
                                }
                            }
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                distanceMeasurement.reset();
                                Optional.ofNullable(getMousePositionOnComponent())
                                        .ifPresent(distanceMeasurement::start);
                            }
                        }

                        @Override
                        public void mouseDragged(MouseEvent e) {
                            if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
                                Optional.ofNullable(getMousePositionOnComponent())
                                        .ifPresent(distanceMeasurement::dragTo);
                                updateDistanceLabel();
                            }
                        }

                        private void updateDistanceLabel() {
                            int distance = distanceMeasurement.getDistance();
                            if (distance > 0) {
                                measurementLabel.setText(String.format("distance: %d", distance));
                            } else {
                                measurementLabel.setText("Click and drag to measure");
                            }
                            ComponentVisualization.this.repaint();
                        }
                    };
            addMouseListener(distanceUpdateListener);
            addMouseMotionListener(distanceUpdateListener);
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
            distanceMeasurement.paint(g2d);
        }

        private void paintSizeRectangles(Graphics2D g2d) {
            g2d.setColor(Util.MAX_SIZE_COLOR);
            RunningComponent c;
            int x, y;
            if (componentUnderMouse == null) {
                c = component;
                x = y = 0;
            } else {
                c = componentUnderMouse;
                Rectangle bounds = component.getChildBounds(c.getId()).orElseThrow();
                x = bounds.x;
                y = bounds.y;
            }
            SizeInfos sizing;
            try {
                sizing = c.getSizeInfos();
            } catch (Throwable t) {
                // process may already be disconnected
                return;
            }
            if (sizing == null) {
                return;
            }
            if (isMaximumSizeCropped(sizing)) {
                g2d.setStroke(CROPPED_STROKE);
                Dimension size = getCroppedMaximumSize(sizing);
                drawRect(x, y, size.width, size.height, g2d);
            } else {
                g2d.setStroke(NORMAL_STROKE);
                drawRect(x, y, sizing.maximumSize.size.width, sizing.maximumSize.size.height, g2d);
            }
            g2d.setStroke(NORMAL_STROKE);
            g2d.setColor(Util.PREF_SIZE_COLOR);
            drawRect(x, y, sizing.preferredSize.size.width, sizing.preferredSize.size.height, g2d);
            g2d.setColor(Util.MIN_SIZE_COLOR);
            drawRect(x, y, sizing.minimumSize.size.width, sizing.minimumSize.size.height, g2d);
        }

        private void drawRect(int x, int y, int w, int h, Graphics2D g) {
            // Components can sometimes return unreasonable sizes, making drawRect heat the CPU
            // for ages. In those cases, simply refuse to draw them.
            if (w > 5000 || h > 5000) {
                return;
            }
            g.drawRect(x, y, w, h);
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
            if (snapshot != null) {
                return new Dimension(
                        snapshot.getWidth() + RULER_SIZE, snapshot.getHeight() + RULER_SIZE);
            } else {
                return new Dimension(RULER_SIZE, RULER_SIZE);
            }
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = getCroppedMaximumSize(component.getSizeInfos());
            return new Dimension(size.width + 2, size.height + 2);
        }

        private Dimension getCroppedMaximumSize(SizeInfos sizing) {
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

        private boolean isMaximumSizeCropped(SizeInfos sizing) {
            return sizing.maximumSize.size.width - sizing.actualSize.width > SIZE_CUTOFF
                    || sizing.maximumSize.size.height - sizing.actualSize.height > SIZE_CUTOFF;
        }
    }

    private static class DistanceMeasurement {
        private Point start = null;
        private Point end = null;

        private final int CROSSHAIR_LENGTH = 5;

        void reset() {
            start = null;
            end = null;
        }

        void start(Point2D p) {
            start = new Point((int) p.getX(), (int) p.getY());
        }

        void dragTo(Point2D p) {
            if (start == null) {
                return;
            }
            double dx = Math.abs(p.getX() - start.x);
            double dy = Math.abs(p.getY() - start.y);
            if (dx >= dy) {
                end = new Point((int) p.getX(), start.y);
            } else {
                end = new Point(start.x, (int) p.getY());
            }
        }

        void paint(Graphics2D g) {
            if (start == null || end == null) {
                return;
            }
            g.setStroke(NORMAL_STROKE);
            paintCrosshair(start, g);
            paintCrosshair(end, g);
            g.drawLine(start.x, start.y, end.x, end.y);
        }

        private void paintCrosshair(Point p, Graphics2D g) {
            g.drawLine(p.x - CROSSHAIR_LENGTH - 2, p.y, p.x - 2, p.y);
            g.drawLine(p.x + 2, p.y, p.x + 2 + CROSSHAIR_LENGTH, p.y);
            g.drawLine(p.x, p.y - CROSSHAIR_LENGTH - 2, p.x, p.y - 2);
            g.drawLine(p.x, p.y + 2, p.x, p.y + CROSSHAIR_LENGTH + 2);
        }

        int getDistance() {
            if (start == null || end == null) {
                return 0;
            }

            // We can just use manhattan distance because the line is always axis-aligned
            return Math.abs(end.x - start.x) + Math.abs(end.y - start.y);
        }
    }
}
