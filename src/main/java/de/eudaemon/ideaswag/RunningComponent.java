package de.eudaemon.ideaswag;

import java.util.Collection;
import java.util.Objects;

import java.util.stream.Collectors;

import java.awt.geom.Point2D;

import com.intellij.openapi.project.Project;

import de.eudaemon.swag.ChildBounds;
import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.ComponentInfoMBean;
import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.PlacementInfo;
import de.eudaemon.swag.SerializableImage;
import de.eudaemon.swag.SizeInfos;

public class RunningComponent {
    private final ComponentInfoMBean connectedBean;
    private final int componentId;
    private final Project project;

    public RunningComponent(ComponentInfoMBean connectedBean_, int componentId_, Project project_) {
        connectedBean = connectedBean_;
        componentId = componentId_;
        project = project_;
    }

    public static Collection<RunningComponent> getRoots(
            ComponentInfoMBean infoBean, Project project) {
        return infoBean.getRoots().stream()
                .map(rootId -> new RunningComponent(infoBean, rootId, project))
                .collect(Collectors.toSet());
    }

    public Collection<RunningComponent> getChildren() {
        return connectedBean.getChildren(componentId).stream()
                .map(id -> new RunningComponent(connectedBean, id, project))
                .collect(Collectors.toSet());
    }

    public SerializableImage getSnapshot() {
        return connectedBean.getSnapshot(componentId);
    }

    public SizeInfos getSizeInfos() {
        return connectedBean.getSizeInfos(componentId);
    }

    public RunningComponent getParent() {
        return new RunningComponent(connectedBean, connectedBean.getParent(componentId), project);
    }

    public ComponentDescription getDescription() {
        return connectedBean.getDescription(componentId);
    }

    public Collection<ComponentProperty> getAllProperties() {
        return connectedBean.getAllProperties(componentId);
    }

    public RunningComponent getComponentAt(Point2D pos) {
        Collection<ChildBounds> bounds = connectedBean.getVisibleChildrenBounds(componentId);
        return bounds.stream()
                .filter(b -> b.bounds.contains(pos))
                .findAny()
                .map(b -> new RunningComponent(connectedBean, b.childId, project))
                .orElse(null);
    }

    public PlacementInfo getPlacementInfo() {
        return connectedBean.getPlacementInfo(componentId);
    }

    public Project getProject() {
        return project;
    }

    public int getId() {
        return componentId;
    }

    public RunningComponent getRoot() {
        return new RunningComponent(connectedBean, connectedBean.getRoot(componentId), project);
    }

    @Override
    public boolean equals(Object o_) {
        if (this == o_) {
            return true;
        }
        if (o_ == null || getClass() != o_.getClass()) {
            return false;
        }
        RunningComponent that = (RunningComponent) o_;
        return componentId == that.componentId
                && connectedBean.equals(that.connectedBean)
                && project.equals(that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectedBean, componentId, project);
    }
}
