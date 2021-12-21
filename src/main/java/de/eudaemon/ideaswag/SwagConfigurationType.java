package de.eudaemon.ideaswag;

import javax.swing.Icon;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons.RunConfigurations;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwagConfigurationType extends ConfigurationTypeBase {
    public SwagConfigurationType() {
        this(
                "SwagApplicationConfiguration",
                "SWAG Swing Application",
                "Run a Swing application " + "with SWAG",
                RunConfigurations.Application);
    }

    public SwagConfigurationType(
            @NotNull String id,
            @Nls @NotNull String displayName,
            @Nls @Nullable String description,
            @Nullable Icon icon) {
        super(id, displayName, description, icon);
        addFactory(new SwagConfiguration.Factory(this));
    }
}
