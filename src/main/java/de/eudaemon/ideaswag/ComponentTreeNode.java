package de.eudaemon.ideaswag;

import javax.swing.tree.DefaultMutableTreeNode;

public class ComponentTreeNode extends DefaultMutableTreeNode {

    private final RunningComponent component;

    public ComponentTreeNode(RunningComponent component_) {
        super(component_);
        component = component_;
        addChildren();
    }

    private void addChildren() {
        for (RunningComponent child : component.getChildren()) {
            add(new ComponentTreeNode(child));
        }
    }

    public RunningComponent getComponent() {
        return component;
    }
}
