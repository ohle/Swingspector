package de.eudaemon.ideaswag;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwagConfigurable extends SettingsEditor<SwagConfiguration> implements PanelWithAnchor {

    private final CommonJavaParametersPanel commonPanel;
    private final ConfigurationModuleSelector moduleSelector;
    private final LabeledComponent<ModuleDescriptionsComboBox> module;
    private final LabeledComponent<EditorTextFieldWithBrowseButton> mainClass;
    private final JPanel mainPanel;
    private JComponent anchor;

    private final LabeledComponent<ShortcutTextField> shortcutField;

    public SwagConfigurable(@NotNull Project project) {

        mainClass = new LabeledComponent<>();
        mainClass.setLabelLocation(BorderLayout.WEST);
        mainClass.setText("Main class");
        mainClass.setComponent(new EditorTextFieldWithBrowseButton(project, true));

        shortcutField = new LabeledComponent<>();
        shortcutField.setLabelLocation(BorderLayout.WEST);
        shortcutField.setText("Keyboard shortcut");
        shortcutField.setComponent(new ShortcutTextField(false));

        module = new LabeledComponent<>();
        module.setLabelLocation(BorderLayout.WEST);
        module.setComponent(new ModuleDescriptionsComboBox());
        module.setText("Use classpath of module");
        moduleSelector = new ConfigurationModuleSelector(project, module.getComponent());

        commonPanel = new CommonJavaParametersPanel();
        commonPanel.setModuleContext(moduleSelector.getModule());
        module.getComponent()
                .addActionListener(a -> commonPanel.setModuleContext(moduleSelector.getModule()));

        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints =
                new GridBagConstraints(
                        0,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL,
                        JBUI.insetsTop(6),
                        0,
                        0);
        mainPanel.add(mainClass, constraints);
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridy++;
        mainPanel.add(shortcutField, constraints);
        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = JBInsets.create(12, 0);
        mainPanel.add(commonPanel, constraints);
        constraints.gridy++;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = JBUI.insetsTop(6);
        mainPanel.add(module, constraints);
        anchor = UIUtil.mergeComponentsWithAnchor(mainClass, commonPanel, module);
    }

    @Override
    protected void applyEditorTo(@NotNull SwagConfiguration config) throws ConfigurationException {
        commonPanel.applyTo(config);
        moduleSelector.applyTo(config);
        config.setMainClassName(mainClass.getComponent().getText().trim());
        config.setKeyStroke(shortcutField.getComponent().getKeyStroke());
    }

    @Override
    protected void resetEditorFrom(@NotNull SwagConfiguration config) {
        commonPanel.reset(config);
        moduleSelector.reset(config);
        mainClass
                .getComponent()
                .setText(
                        config.getMainClassName() != null
                                ? config.getMainClassName().replaceAll("\\$", "\\.")
                                : "");
        shortcutField.getComponent().setKeyStroke(config.getKeyStroke());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return mainPanel;
    }

    @Override
    public JComponent getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor_) {
        anchor = anchor_;
        commonPanel.setAnchor(anchor);
        module.setAnchor(anchor);
        mainClass.setAnchor(anchor);
        shortcutField.setAnchor(anchor);
    }
}
