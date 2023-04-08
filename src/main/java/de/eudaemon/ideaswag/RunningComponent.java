package de.eudaemon.ideaswag;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.stream.Collectors;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.geom.Point2D;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.openapi.project.Project;

import de.eudaemon.swag.ChildBounds;
import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.ComponentInfoMBean;
import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.PlacementInfo;
import de.eudaemon.swag.SerializableImage;
import de.eudaemon.swag.SizeInfo;
import de.eudaemon.swag.SizeInfos;

public class RunningComponent {
    private final ComponentInfoMBean connectedBean;
    private final int componentId;
    private final Project project;

    private final Cached<Collection<RunningComponent>> children =
            new Cached<>(this::getChildrenInt, List.of());

    private final Cached<SerializableImage> snapshot = new Cached<>(this::getSnapshotInt, null);

    private static final SizeInfo NULL_SIZINFO = new SizeInfo(new Dimension(0, 0), false);
    private static final SizeInfos NULL_SIZEINFOS =
            new SizeInfos(new Dimension(0, 0), NULL_SIZINFO, NULL_SIZINFO, NULL_SIZINFO);

    private final Cached<SizeInfos> sizeInfos = new Cached<>(this::getSizeInfosInt, NULL_SIZEINFOS);

    private final Cached<RunningComponent> parent = new Cached<>(this::getParentInt, null);

    private final Cached<ComponentDescription> description =
            new Cached<>(
                    this::getDescriptionInt,
                    new ComponentDescription("unknown", "unknown", "unknown", "", ""));

    private final Cached<Collection<ComponentProperty>> properties =
            new Cached<>(this::getAllPropertiesInt, List.of());

    private final Cached<PlacementInfo> placementInfo =
            new Cached<>(
                    this::getPlacementInfoInt,
                    new PlacementInfo(null, -1, new StackTraceElement[] {}));

    private final Cached<RunningComponent> root = new Cached<>(this::getRootInt, null);

    public RunningComponent(ComponentInfoMBean connectedBean_, int componentId_, Project project_) {
        connectedBean = connectedBean_;
        componentId = componentId_;
        project = project_;
    }

    public boolean isValid() {
        return componentId >= 0;
    }

    public static Collection<RunningComponent> getRoots(
            ComponentInfoMBean infoBean, Project project) {
        return infoBean.getRoots().stream()
                .map(rootId -> new RunningComponent(infoBean, rootId, project))
                .collect(Collectors.toSet());
    }

    public Collection<RunningComponent> getChildren() {
        return getCached(children);
    }

    public SerializableImage getSnapshot() {
        return getCached(snapshot);
    }

    public SizeInfos getSizeInfos() {
        return getCached(sizeInfos);
    }

    public RunningComponent getParent() {
        return getCached(parent);
    }

    public ComponentDescription getDescription() {
        return getCached(description);
    }

    public Collection<ComponentProperty> getAllProperties() {
        return getCached(properties);
    }

    public PlacementInfo getPlacementInfo() {
        return getCached(placementInfo);
    }

    public RunningComponent getRoot() {
        return getCached(root);
    }

    private Collection<RunningComponent> getChildrenInt() {
        return connectedBean.getChildren(componentId).stream()
                .map(id -> new RunningComponent(connectedBean, id, project))
                .collect(Collectors.toSet());
    }

    private SerializableImage getSnapshotInt() {
        return connectedBean.getSnapshot(componentId);
    }

    private SizeInfos getSizeInfosInt() {
        return connectedBean.getSizeInfos(componentId);
    }

    private RunningComponent getParentInt() {
        return new RunningComponent(connectedBean, connectedBean.getParent(componentId), project);
    }

    private ComponentDescription getDescriptionInt() {
        return connectedBean.getDescription(componentId);
    }

    private Collection<ComponentProperty> getAllPropertiesInt() {
        return connectedBean.getAllProperties(componentId);
    }

    public RunningComponent getComponentAt(Point2D pos) {
        if (applicationIsStopped()) {
            return null;
        }
        Collection<ChildBounds> bounds = connectedBean.getVisibleChildrenBounds(componentId);
        return bounds.stream()
                .filter(b -> b.bounds.contains(pos))
                .findAny()
                .map(b -> new RunningComponent(connectedBean, b.childId, project))
                .orElse(null);
    }

    public Optional<Rectangle> getChildBounds(int childId) {
        if (applicationIsStopped()) {
            return Optional.empty();
        }
        return connectedBean.getVisibleChildrenBounds(componentId).stream()
                .filter(cb -> cb.childId == childId)
                .findAny()
                .map(cb -> cb.bounds);
    }

    private PlacementInfo getPlacementInfoInt() {
        return connectedBean.getPlacementInfo(componentId);
    }

    public Project getProject() {
        return project;
    }

    public int getId() {
        return componentId;
    }

    private RunningComponent getRootInt() {
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

    private boolean applicationIsStopped() {
        DebugProcessImpl debugProcess =
                DebuggerManagerEx.getInstanceEx(getProject()).getContext().getDebugProcess();
        return debugProcess != null && !debugProcess.getSession().isRunning();
    }

    private <T> T getCached(Cached<T> cached) {
        if (applicationIsStopped()) {
            return cached.getLastSeenValue();
        } else {
            return cached.get();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectedBean, componentId, project);
    }
}
