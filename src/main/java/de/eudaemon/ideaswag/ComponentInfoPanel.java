package de.eudaemon.ideaswag;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.ClassUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.unscramble.AnalyzeStacktraceUtil;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.ComponentProperty.ListenerSet;
import de.eudaemon.swag.PlacementInfo;
import de.eudaemon.swag.SerializableImage;
import de.eudaemon.swag.SizeInfos;
import org.jetbrains.annotations.NotNull;

public class ComponentInfoPanel extends JPanel implements Disposable, Refreshable {

    private static final String SPLIT_PROPORTION_KEY =
            "de.eudaemon.idea-swag.component-info-panel.main-split-proportion";
    private static final String RIGHT_SPLIT_PROPORTION_KEY =
            "de.eudaemon.idea-swag.component-info-panel.right-split-proportion";

    static final Color MIN_SIZE_COLOR = new Color(0x268bd2);
    static final Color PREF_SIZE_COLOR = new Color(0x859900);
    static final Color MAX_SIZE_COLOR = new Color(0xd33682);

    private final RunningComponent component;
    private final Project project;
    private final Disposable disposer;

    public ComponentInfoPanel(RunningComponent component_, Disposable disposer_) {
        component = component_;
        project = component_.getProject();
        disposer = disposer_;
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void dispose() {}

    @Override
    public void refresh() {
        removeAll();
        JBSplitter mainSplitter = new JBSplitter(SPLIT_PROPORTION_KEY, .7f);
        JBSplitter splitter = new JBSplitter(RIGHT_SPLIT_PROPORTION_KEY, .5f);
        splitter.setFirstComponent(createPlacementPanel());
        splitter.setSecondComponent(new VisualPanel());
        mainSplitter.setFirstComponent(splitter);
        mainSplitter.setSecondComponent(createPropertiesPanel());
        add(mainSplitter, BorderLayout.CENTER);
    }

    @NotNull
    private JComponent createPlacementPanel() {
        final TextConsoleBuilder builder =
                TextConsoleBuilderFactory.getInstance().createBuilder(project);
        JPanel panel = new JPanel(new BorderLayout());
        builder.filters(AnalyzeStacktraceUtil.EP_NAME.getExtensions(project));
        final ConsoleView consoleView = builder.getConsole();
        Disposer.register(this, consoleView);
        consoleView.allowHeavyFilters();
        AnalyzeStacktraceUtil.printStacktrace(consoleView, getStackTraceAsText());
        JComponent console = consoleView.getComponent();
        Disposer.register(this, consoleView);
        consoleView.scrollTo(0);
        panel.add(console, BorderLayout.CENTER);
        AnAction actionGroup = ActionManager.getInstance().getAction("IdeaSWAG.ComponentView");
        ActionToolbar toolBar =
                ActionManager.getInstance()
                        .createActionToolbar(
                                ActionPlaces.TOOLWINDOW_CONTENT, (ActionGroup) actionGroup, false);
        toolBar.setTargetComponent(this);
        add(toolBar.getComponent(), BorderLayout.WEST);
        return panel;
    }

    private String getStackTraceAsText() {
        PlacementInfo placementInfo = component.getPlacementInfo();
        StackTraceElement[] stackTrace = placementInfo.stackTrace;
        RunningComponent parent = component.getParent();

        String layoutDescription =
                component.getAllProperties().stream()
                        .filter(p -> "layout".equals(p.key))
                        .findAny()
                        .map(l -> " (Layout " + l.valueDescription + ")")
                        .orElse("null");
        String prelude =
                "Added to "
                        + Util.generateTitle(parent.getDescription())
                        + layoutDescription
                        + "\nat index "
                        + placementInfo.index
                        + "\nwith constraints "
                        + placementInfo.constraints
                        + ":\n";
        return prelude
                + Arrays.stream(stackTrace)
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n     ", "     ", ""));
    }

    public RunningComponent getRunningComponent() {
        return component;
    }

    public Disposable getDisposer() {
        return disposer;
    }

    private class VisualPanel extends JPanel {

        private final SizeInfos sizeInfos;

        private VisualPanel() {
            setLayout(new BorderLayout());
            sizeInfos = component.getSizeInfos();
            SerializableImage snapshot = component.getSnapshot();
            BufferedImage img = snapshot == null ? null : snapshot.getImage();
            add(createSizeTablePanel(), BorderLayout.NORTH);
            add(new JBScrollPane(new Visualization(sizeInfos, img)), BorderLayout.CENTER);
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
            addCircle(panel, MIN_SIZE_COLOR, c);
            if (sizeInfos.minimumSize.set) {
                panel.add(new JLabel(Actions.PinTab), c);
            }
            c.gridx = 0;
            c.gridy++;
            panel.add(new JLabel("preferred"), c);
            c.gridx = GridBagConstraints.RELATIVE;
            addDimLabel(panel, sizeInfos.preferredSize.size, c);
            addCircle(panel, PREF_SIZE_COLOR, c);
            if (sizeInfos.preferredSize.set) {
                panel.add(new JLabel(Actions.PinTab), c);
            }
            c.gridx = 0;
            c.gridy++;
            panel.add(new JLabel("maximum"), c);
            c.gridx = GridBagConstraints.RELATIVE;
            addDimLabel(panel, sizeInfos.maximumSize.size, c);
            addCircle(panel, MAX_SIZE_COLOR, c);
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
                                    RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
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

    private static class Visualization extends JPanel {

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

        private Visualization(SizeInfos sizing_, BufferedImage snapshot_) {
            sizing = sizing_;
            snapshot = snapshot_;
            setBorder(new EmptyBorder(JBInsets.create(10, 10)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            drawCheckerBoard(g2d);
            if (snapshot != null) {
                g2d.drawImage(snapshot, 0, 0, null);
            }
            g2d.setColor(MAX_SIZE_COLOR);
            if (isMaximumSizeCropped()) {
                g2d.setStroke(croppedStroke);
                Dimension size = getCroppedMaximumSize();
                g2d.drawRect(0, 0, size.width, size.height);
            } else {
                g2d.setStroke(normalStroke);
                g2d.drawRect(0, 0, sizing.maximumSize.size.width, sizing.maximumSize.size.height);
            }
            g2d.setStroke(normalStroke);
            g2d.setColor(PREF_SIZE_COLOR);
            g2d.drawRect(0, 0, sizing.preferredSize.size.width, sizing.preferredSize.size.height);
            g2d.setColor(MIN_SIZE_COLOR);
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

    private JComponent createPropertiesPanel() {
        JBTabbedPane tabbedPane = new JBTabbedPane();
        Collection<ComponentProperty> props = component.getAllProperties();
        Map<String, List<ComponentProperty>> propsByCategory =
                props.stream().collect(Collectors.groupingBy(cp -> cp.category));

        for (String category : propsByCategory.keySet()) {
            List<ComponentProperty> theseProps = propsByCategory.get(category);
            JBTable table = new JBTable(new ComponentPropertiesModel(theseProps));
            table.setDefaultRenderer(ComponentProperty.class, new PropertyRenderer());
            table.setDefaultEditor(ComponentProperty.class, new PropertyEditor());
            int width = 15;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, 0);
                Component c = table.prepareRenderer(renderer, row, 0);
                width = Math.max(c.getPreferredSize().width + 1, width);
            }
            table.getColumnModel().getColumn(0).setPreferredWidth(width);
            table.getColumnModel().getColumn(0).setMaxWidth(width);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            tabbedPane.add(category, new JBScrollPane(table));
        }

        return tabbedPane;
    }

    private class ListenerCell extends JPanel {

        public ListenerCell(ComponentProperty.ListenerSet listeners) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            for (String className : listeners.classNames) {
                JButton button = new JButton();
                button.setContentAreaFilled(false);
                button.setBorder(new EmptyBorder(JBInsets.create(0, 0)));
                button.setHorizontalAlignment(SwingConstants.LEFT);
                Color linkColor = UIManager.getColor("Hyperlink.linkColor");
                button.setText(
                        String.format(
                                "<html><u><font color=#%02x%02x%02x>%s</font></u></html>",
                                linkColor.getRed(),
                                linkColor.getGreen(),
                                linkColor.getBlue(),
                                className));
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                button.addActionListener(a -> navigateTo(className));
                add(button);
            }
        }

        private void navigateTo(String className) {
            PsiClass clazz = ClassUtil.findPsiClass(PsiManager.getInstance(project), className);
            if (clazz != null) {
                PsiElement navElement = clazz.getNavigationElement();
                if (navElement instanceof Navigatable && ((Navigatable) navElement).canNavigate()) {
                    ((Navigatable) navElement).navigate(true);
                } else {
                    Notifications.Bus.notify(
                            new Notification(
                                    "idea-swag notifications",
                                    "Could not navigate to class",
                                    "Class '" + className + "' cannot be navigated to",
                                    NotificationType.ERROR),
                            project);
                }
            } else {
                Notifications.Bus.notify(
                        new Notification(
                                "idea-swag notifications",
                                "Class not found",
                                "No class '" + className + "' found in current project",
                                NotificationType.ERROR),
                        project);
            }
        }
    }

    private class PropertyRenderer implements TableCellRenderer {
        private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            if (value instanceof ComponentProperty.ListenerSet) {
                return new ListenerCell((ListenerSet) value);
            } else if (value instanceof ComponentProperty) {
                return defaultRenderer.getTableCellRendererComponent(
                        table,
                        ((ComponentProperty) value).valueDescription,
                        isSelected,
                        hasFocus,
                        row,
                        column);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private class PropertyEditor extends AbstractTableCellEditor {

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof ComponentProperty.ListenerSet) {
                return new ListenerCell((ListenerSet) value);
            } else {
                return renderer.getTableCellRendererComponent(
                        table, value, isSelected, true, row, column);
            }
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
