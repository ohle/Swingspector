package de.eudaemon.ideaswag;

import java.awt.BorderLayout;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.Key;

class SwingspectorSettingsEditorFragment extends JPanel {
    public static final Key<Boolean> ACTIVE_KEY = new Key<>("Swingspector Active");
    public static final Key<KeyStroke> HOTKEY = new Key<>("Swingspector Hotkey");
    private final JCheckBox activeCheckbox;
    private final LabeledComponent<ShortcutTextField> shortcutField;
    private static final KeyStroke DEFAULT_HOTKEY =
            KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK);

    public SwingspectorSettingsEditorFragment() {
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

        add(cb);
        add(shortcutField);
    }

    public void resetFrom(ApplicationConfiguration config) {
        Boolean active = config.getCopyableUserData(ACTIVE_KEY);
        KeyStroke hotKey = config.getCopyableUserData(HOTKEY);
        if (hotKey == null) {
            hotKey = DEFAULT_HOTKEY;
        }
        activeCheckbox.setSelected(active != null && active);
        shortcutField.getComponent().setKeyStroke(hotKey);
    }

    public void applyTo(ApplicationConfiguration config) {
        config.putCopyableUserData(ACTIVE_KEY, activeCheckbox.isSelected());
        config.putCopyableUserData(HOTKEY, shortcutField.getComponent().getKeyStroke());
    }

    private void activeChanged() {
        shortcutField.setEnabled(activeCheckbox.isSelected());
    }
}
