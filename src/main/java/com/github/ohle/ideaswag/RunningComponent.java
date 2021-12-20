package com.github.ohle.ideaswag;

import java.util.Collection;

import java.util.stream.Collectors;

import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.ComponentInfoMBean;
import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.PlacementInfo;
import de.eudaemon.swag.SerializableImage;
import de.eudaemon.swag.SizeInfos;

public class RunningComponent {
    private final ComponentInfoMBean connectedBean;
    private final int componentId;

    public RunningComponent(ComponentInfoMBean connectedBean_, int componentId_) {
        connectedBean = connectedBean_;
        componentId = componentId_;
    }

    public static Collection<RunningComponent> getRoots(ComponentInfoMBean infoBean) {
        return infoBean.getRoots().stream()
                .map(rootId -> new RunningComponent(infoBean, rootId))
                .collect(Collectors.toSet());
    }

    public Collection<RunningComponent> getChildren() {
        return connectedBean.getChildren(componentId).stream()
                .map(id -> new RunningComponent(connectedBean, id))
                .collect(Collectors.toSet());
    }

    public SerializableImage getSnapshot() {
        return connectedBean.getSnapshot(componentId);
    }

    public SizeInfos getSizeInfos() {
        return connectedBean.getSizeInfos(componentId);
    }

    public RunningComponent getParent() {
        return new RunningComponent(connectedBean, connectedBean.getParent(componentId));
    }

    public ComponentDescription getDescription() {
        return connectedBean.getDescription(componentId);
    }

    public Collection<ComponentProperty> getAllProperties() {
        return connectedBean.getAllProperties(componentId);
    }

    public PlacementInfo getPlacementInfo() {
        return connectedBean.getPlacementInfo(componentId);
    }
}
