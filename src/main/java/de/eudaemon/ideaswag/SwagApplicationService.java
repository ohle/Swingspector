package de.eudaemon.ideaswag;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;

/**
 * Application-level service that serves only as a disposable parent to prevent memory-leak warnings
 * that otherwise occur when the IDE is closed, which apparently sometimes skips the dispose action
 * invoked by the process termination event
 */
@Service
public final class SwagApplicationService implements Disposable {
    @Override
    public void dispose() {}
}
