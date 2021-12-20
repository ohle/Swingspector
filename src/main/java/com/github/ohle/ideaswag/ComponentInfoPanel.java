package com.github.ohle.ideaswag;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import javax.swing.border.EmptyBorder;

import javax.swing.table.TableCellRenderer;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.unscramble.AnalyzeStacktraceUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.PlacementInfo;
import de.eudaemon.swag.SizeInfos;
import org.jetbrains.annotations.NotNull;

public class ComponentInfoPanel extends JPanel implements Disposable {

    private static final String SPLIT_PROPORTION_KEY =
            "de.eudaemon.idea-swag.component-info-panel.main-split-proportion";
    private static final String RIGHT_SPLIT_PROPORTION_KEY =
            "de.eudaemon.idea-swag.component-info-panel.right-split-proportion";

    static final JBColor MIN_SIZE_COLOR =
            JBColor.namedColor("IDEASwag.MinSize.foreground", 0x268bd2);
    static final JBColor PREF_SIZE_COLOR =
            JBColor.namedColor("IDEASwag.PrefSize.foreground", 0x859900);
    static final JBColor MAX_SIZE_COLOR =
            JBColor.namedColor("IDEASwag.MaxSize.foreground", 0xd33682);

    private final RunningComponent component;
    private final Project project;
    private final String title;

    @Override
    public void dispose() {}

    public ComponentInfoPanel(RunningComponent component_) {
        component = component_;
        project = component_.getProject();
        title = Util.generateTitle(component.getDescription());
        setLayout(new BorderLayout());
        JBSplitter mainSplitter = new JBSplitter(SPLIT_PROPORTION_KEY, .7f);
        JBSplitter splitter = new JBSplitter(RIGHT_SPLIT_PROPORTION_KEY, .5f);
        splitter.setFirstComponent(createPlacementPanel());
        splitter.setSecondComponent(new VisualPanel());
        mainSplitter.setFirstComponent(splitter);
        mainSplitter.setSecondComponent(createPropertiesPanel());
        add(mainSplitter, BorderLayout.CENTER);
    }

    public String getTitle() {
        return title;
    }

    @NotNull
    private JComponent createPlacementPanel() {
        final TextConsoleBuilder builder =
                TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.filters(AnalyzeStacktraceUtil.EP_NAME.getExtensions(project));
        final ConsoleView consoleView = builder.getConsole();
        Disposer.register(this, consoleView);
        consoleView.allowHeavyFilters();
        AnalyzeStacktraceUtil.printStacktrace(consoleView, getStackTraceAsText());
        return consoleView.getComponent();
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

    private class VisualPanel extends JPanel {

        private final SizeInfos sizeInfos;

        private VisualPanel() {
            setLayout(new BorderLayout());
            sizeInfos = component.getSizeInfos();
            add(createSizeTablePanel(), BorderLayout.NORTH);
            add(
                    new JBScrollPane(
                            new Visualization(sizeInfos, component.getSnapshot().getImage())),
                    BorderLayout.CENTER);
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

        private void addCircle(JPanel container, JBColor color, GridBagConstraints c) {
            int oldAnchor = c.anchor;
            c.anchor = GridBagConstraints.CENTER;
            container.add(circle(color), c);
            c.anchor = oldAnchor;
        }

        private Component circle(JBColor color) {
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

    private class Visualization extends JPanel {

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
            g2d.drawImage(snapshot, 0, 0, null);
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
            int croppedWidth =
                    sizing.maximumSize.size.width - sizing.actualSize.width > SIZE_CUTOFF
                            ? sizing.actualSize.width + SIZE_CUTOFF
                            : sizing.maximumSize.size.width;
            int croppedHeight =
                    sizing.maximumSize.size.height - sizing.actualSize.height > SIZE_CUTOFF
                            ? sizing.actualSize.height + SIZE_CUTOFF
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
}
