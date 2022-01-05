package de.eudaemon.ideaswag;

import java.util.Objects;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentViewActions {

    public static class OpenInTree extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Util.getOpenComponentTab()
                    .ifPresent(
                            rc -> {
                                Util.openTreeTab(
                                        rc.getRoot(),
                                        Objects.requireNonNull(getComponentInfoPanel(e))
                                                .getDisposer());
                                Util.getOpenTreeTab().ifPresent(tp -> tp.selectComponent(rc));
                            });
        }
    }

    @Nullable
    private static ComponentInfoPanel getComponentInfoPanel(@NotNull AnActionEvent e) {
        return (ComponentInfoPanel) e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }
}
