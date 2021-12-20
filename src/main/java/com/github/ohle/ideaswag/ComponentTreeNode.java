package com.github.ohle.ideaswag;

import javax.swing.tree.DefaultMutableTreeNode;

import de.eudaemon.swag.ComponentInfoMBean;

public class ComponentTreeNode extends DefaultMutableTreeNode {

    private int nodeId;
    private ComponentInfoMBean info;

    public ComponentTreeNode(int nodeId_, ComponentInfoMBean info_) {
        super(nodeId_);
        nodeId = nodeId_;
        info = info_;
        addChildren();
    }

    private void addChildren() {
        for (Integer childId : info.getChildren(nodeId)) {
            add(new ComponentTreeNode(childId, info));
        }
    }

    public int getNodeId() {
        return nodeId;
    }

    public ComponentInfoMBean getInfo() {
        return info;
    }
}
