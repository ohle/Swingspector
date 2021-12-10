package com.github.ohle.ideaswag;

import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class SwagConfigurationOptions extends JvmMainMethodRunConfigurationOptions {
    private final StoredProperty<String> foo = string("").provideDelegate(this, "foo");

    public String getFoo() {
        return foo.getValue(this);
    }

    public void setFoo(String newFoo) {
        foo.setValue(this, newFoo);
    }
}
