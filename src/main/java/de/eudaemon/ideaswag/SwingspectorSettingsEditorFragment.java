package de.eudaemon.ideaswag;

import java.awt.BorderLayout;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.event.DocumentEvent;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Key;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;

import org.jetbrains.annotations.NotNull;

class SwingspectorSettingsEditorFragment extends JPanel {
    public static final Key<Boolean> ACTIVE_KEY = new Key<>("Swingspector Active");
    public static final Key<KeyStroke> HOTKEY = new Key<>("Swingspector Hotkey");
    public static final Key<Double> TIMEOUT = new Key<>("Swingspector Connection Timeout");
    private final JCheckBox activeCheckbox;
    private final LabeledComponent<ShortcutTextField> shortcutField;
    private final LabeledComponent<JBTextField> timeoutField;
    private static final KeyStroke DEFAULT_HOTKEY =
            KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK);

    private static final Double DEFAULT_TIMEOUT = 5.0;

    public SwingspectorSettingsEditorFragment(@NotNull Project project) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        shortcutField = new LabeledComponent<>();
        shortcutField.setLabelLocation(BorderLayout.WEST);
        shortcutField.setText("Swingspector hotkey");
        shortcutField.setComponent(new ShortcutTextField(false));

        LabeledComponent<JCheckBox> cb = new LabeledComponent<>();
        cb.setLabelLocation(BorderLayout.WEST);
        cb.setText("Use Swingspector");
        activeCheckbox = new JCheckBox();
        cb.setComponent(activeCheckbox);
        activeCheckbox.addActionListener(a -> activeChanged());
        activeChanged();

        timeoutField = new LabeledComponent<>();
        timeoutField.setLabelLocation(BorderLayout.WEST);
        timeoutField.setText("Connection timeout (seconds)");
        timeoutField.setComponent(new JBTextField());

        add(cb);
        add(shortcutField);
        add(timeoutField);
        new ComponentValidator(project)
                .withValidator(
                        v -> {
                            String text = timeoutField.getComponent().getText();
                            try {
                                double timeout = Double.parseDouble(text);
                                if (timeout < 500.0e-3) {
                                    v.updateInfo(
                                            new ValidationInfo(
                                                    "Timeout must be at least 0.5 seconds.",
                                                    timeoutField.getComponent()));
                                } else {
                                    v.updateInfo(null);
                                }
                            } catch (NumberFormatException nfe) {
                                v.updateInfo(
                                        new ValidationInfo(
                                                "Please enter a number",
                                                timeoutField.getComponent()));
                            }
                        })
                .installOn(timeoutField.getComponent());
        timeoutField
                .getComponent()
                .getDocument()
                .addDocumentListener(
                        new DocumentAdapter() {
                            @Override
                            protected void textChanged(@NotNull DocumentEvent e) {
                                ComponentValidator.getInstance(timeoutField.getComponent())
                                        .ifPresent(ComponentValidator::revalidate);
                            }
                        });
    }

    public void resetFrom(ApplicationConfiguration config) {
        Boolean active = config.getCopyableUserData(ACTIVE_KEY);
        KeyStroke hotKey = config.getCopyableUserData(HOTKEY);
        Double timeout = config.getCopyableUserData(TIMEOUT);
        if (hotKey == null) {
            hotKey = DEFAULT_HOTKEY;
        }
        if (timeout == null) {
            timeout = DEFAULT_TIMEOUT;
        }
        activeCheckbox.setSelected(active != null && active);
        shortcutField.getComponent().setKeyStroke(hotKey);
        timeoutField.getComponent().setText(String.valueOf(timeout));
    }

    public void applyTo(ApplicationConfiguration config) {
        config.putCopyableUserData(ACTIVE_KEY, activeCheckbox.isSelected());
        config.putCopyableUserData(HOTKEY, shortcutField.getComponent().getKeyStroke());
        try {
            config.putCopyableUserData(
                    TIMEOUT, Double.parseDouble(timeoutField.getComponent().getText()));
        } catch (NumberFormatException ignored) {
        }
    }

    private void activeChanged() {
        shortcutField.setEnabled(activeCheckbox.isSelected());
    }
}
