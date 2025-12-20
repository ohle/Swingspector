package de.eudaemon.ideaswag;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import com.intellij.icons.AllIcons.General;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.ClassUtil;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.unscramble.AnalyzeStacktraceUtil;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.JBInsets;

import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.ComponentProperty.ListenerSet;
import de.eudaemon.swag.PlacementInfo;
import org.jetbrains.annotations.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
        splitter.setSecondComponent(new ComponentAppearancePanel(component, disposer));
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
        ActionToolbar toolBar =
                ActionManager.getInstance()
                        .createActionToolbar(
                                ActionPlaces.TOOLWINDOW_CONTENT, createToolbarActionGroup(), false);
        toolBar.setTargetComponent(this);
        add(toolBar.getComponent(), BorderLayout.WEST);
        return panel;
    }

    private ActionGroup createToolbarActionGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(
                Util.actionBuilder()
                        .description("Open this component in tree view")
                        .text("Open In Tree View")
                        .icon(General.Locate)
                        .build(this::openInTree));
        group.add(
                Util.actionBuilder()
                        .description("Open the parent of this component")
                        .text("Open Parent")
                        .icon(General.ArrowUp)
                        .build(this::openParent));
        group.add(
                Util.actionBuilder()
                        .description("Refresh view")
                        .icon(Actions.Refresh)
                        .build(this::refresh));
        return group;
    }

    private void openInTree() {
        Util.openTreeTab(getRunningComponent().getRoot(), getDisposer());
        Util.getOpenTreeTab(project).ifPresent(tp -> tp.selectComponent(getRunningComponent()));
    }

    private void openParent() {
        RunningComponent parent = getRunningComponent().getParent();
        if (parent.isValid()) {
            Util.openComponentTab(parent, getDisposer());
        }
    }

    private String getStackTraceAsText() {
        PlacementInfo placementInfo = component.getPlacementInfo();
        StackTraceElement[] stackTrace = placementInfo.stackTrace;
        RunningComponent parent = component.getParent();

        String layoutDescription =
                parent.getProperty("layout")
                        .map(l -> " (Layout " + l.valueDescription + ")")
                        .orElse("null");
        String prelude =
                "Added to "
                        + Util.generateTitle(parent.getDescription())
                        + layoutDescription
                        + "\nat index "
                        + placementInfo.index
                        + "\nwith constraints "
                        + prettyConstraints(placementInfo.constraints)
                        + ":\n";
        return prelude
                + Arrays.stream(stackTrace)
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n     ", "     ", ""));
    }

    private String prettyConstraints(Object constraints) {
        if (constraints instanceof GridBagConstraints gbc) {
            return new ToStringBuilder(gbc, ToStringStyle.NO_CLASS_NAME_STYLE)
                    .append("gridx", gbc.gridx)
                    .append("gridy", gbc.gridy)
                    .append("gridwidth", gbc.gridwidth)
                    .append("gridheight", gbc.gridheight)
                    .append("fill", describeGridBagFill(gbc.fill))
                    .append("ipadx", gbc.ipadx)
                    .append("ipady", gbc.ipady)
                    .append("insets", gbc.insets)
                    .append("anchor", describeGridBagAnchor(gbc.anchor))
                    .append("weightx", gbc.weightx)
                    .append("weighty", gbc.weighty)
                    .toString();
        } else {
            return constraints.toString();
        }
    }

    private String describeGridBagAnchor(int anchor) {
        return switch (anchor) {
            case GridBagConstraints.CENTER -> "CENTER";
            case GridBagConstraints.PAGE_START -> "PAGE_START";
            case GridBagConstraints.PAGE_END -> "PAGE_END";
            case GridBagConstraints.LINE_START -> "LINE_START";
            case GridBagConstraints.LINE_END -> "LINE_END";
            case GridBagConstraints.FIRST_LINE_START -> "FIRST_LINE_START";
            case GridBagConstraints.FIRST_LINE_END -> "FIRST_LINE_END";
            case GridBagConstraints.LAST_LINE_START -> "LAST_LINE_START";
            case GridBagConstraints.LAST_LINE_END -> "LAST_LINE_END";
            case GridBagConstraints.NORTHWEST -> "NORTHWEST";
            case GridBagConstraints.NORTH-> "NORTH";
            case GridBagConstraints.NORTHEAST -> "NORTHEAST";
            case GridBagConstraints.WEST-> "WEST";
            case GridBagConstraints.EAST-> "EAST";
            case GridBagConstraints.SOUTHWEST -> "SOUTHWEST";
            case GridBagConstraints.SOUTH-> "SOUTH";
            case GridBagConstraints.SOUTHEAST -> "SOUTHEAST";
            default -> "Unknown (%d)".formatted(anchor);
        };
    }

    private String describeGridBagFill(int fill) {
        return switch (fill) {
            case GridBagConstraints.NONE -> "NONE";
            case GridBagConstraints.HORIZONTAL ->  "HORIZONTAL";
            case GridBagConstraints.VERTICAL ->  "VERTICAL";
            case GridBagConstraints.BOTH ->  "BOTH";
            default -> "Unknown (%d)".formatted(fill);
        };
    }

    public RunningComponent getRunningComponent() {
        return component;
    }

    public Disposable getDisposer() {
        return disposer;
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
                                    "Swingspector notifications",
                                    "Could not navigate to class",
                                    "Class '" + className + "' cannot be navigated to",
                                    NotificationType.ERROR),
                            project);
                }
            } else {
                Notifications.Bus.notify(
                        new Notification(
                                "Swingspector notifications",
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
