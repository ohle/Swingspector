package com.github.ohle.ideaswag;

import java.util.Arrays;

import java.util.stream.Collectors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.unscramble.AnalyzeStacktraceUtil;

import de.eudaemon.swag.ComponentInfoMBean;
import de.eudaemon.swag.SizeInfos;

public class ComponentInfoPanel extends JPanel implements Disposable {

    private static final String SPLIT_PROPORTION_KEY =
            "de.eudaemon.idea-swag.component-info-panel.split-proportion";
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
        JBSplitter splitter = new JBSplitter(SPLIT_PROPORTION_KEY, .5f);
        splitter.setFirstComponent(new VisualPanel());
        splitter.setSecondComponent(createAdditionTracePanel());
        add(splitter, BorderLayout.CENTER);
    }

    private JComponent createAdditionTracePanel() {
        final TextConsoleBuilder builder =
                TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.filters(AnalyzeStacktraceUtil.EP_NAME.getExtensions(project));
        final ConsoleView consoleView = builder.getConsole();
        consoleView.allowHeavyFilters();
        AnalyzeStacktraceUtil.printStacktrace(consoleView, getStackTraceAsText());
        return consoleView.getComponent();
    }

    private String getStackTraceAsText() {
        StackTraceElement[] stackTrace = componentInfo.getPlacementInfo(componentId).stackTrace;
        return Arrays.stream(stackTrace)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private class VisualPanel extends JPanel {

        private final SizeInfos sizing;
        private final BufferedImage snapshot;

        private VisualPanel() {
            sizing = componentInfo.getSizeInfos(componentId);
            snapshot = componentInfo.getSnapshot(componentId).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(snapshot, 0, 0, null);
        }

        @Override
        public Dimension getPreferredSize() {
            return super.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return sizing.actualSize;
        }
    }
}
