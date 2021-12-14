package com.github.ohle.ideaswag;

import java.util.Arrays;

import java.util.stream.Collectors;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.unscramble.AnalyzeStacktraceUtil;

import de.eudaemon.swag.ComponentInfoMBean;

public class ComponentInfoPanel extends JPanel implements Disposable {

    private final ComponentInfoMBean componentInfo;
    private final int componentId;
    private final Project project;
    private final String title;

    @Override
    public void dispose() {}

    public ComponentInfoPanel(
            Project project_, ComponentInfoMBean componentInfo_, String title_, int componentId_) {
        componentInfo = componentInfo_;
        componentId = componentId_;
        project = project_;
        title = title_;
        setLayout(new BorderLayout());
        addAdditionTracePanel();
    }

    private void addAdditionTracePanel() {
        final TextConsoleBuilder builder =
                TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.filters(AnalyzeStacktraceUtil.EP_NAME.getExtensions(project));
        final ConsoleView consoleView = builder.getConsole();
        consoleView.allowHeavyFilters();
        AnalyzeStacktraceUtil.printStacktrace(consoleView, getStackTraceAsText());
        add(consoleView.getComponent());
    }

    private String getStackTraceAsText() {
        StackTraceElement[] stackTrace = componentInfo.getPlacementInfo(componentId).stackTrace;
        return Arrays.stream(stackTrace)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}
