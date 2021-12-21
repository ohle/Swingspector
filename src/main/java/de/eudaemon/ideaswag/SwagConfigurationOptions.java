package de.eudaemon.ideaswag;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class SwagConfigurationOptions extends JvmMainMethodRunConfigurationOptions {
    private final StoredProperty<Integer> keyCode =
            property(KeyEvent.VK_F12).provideDelegate(this, "keyCode");
    private final StoredProperty<Integer> modifiers =
            property(0).provideDelegate(this, "keyModifiers");

    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke(keyCode.getValue(this), modifiers.getValue(this));
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        if (keyStroke != null) {
            keyCode.setValue(this, keyStroke.getKeyCode());
            modifiers.setValue(this, keyStroke.getModifiers());
        } else {
            keyCode.setValue(this, KeyEvent.VK_UNDEFINED);
        }
    }
}
