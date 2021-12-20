package com.github.ohle.ideaswag;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import de.eudaemon.swag.ComponentInfoMBean;

public class SwagNotificationHandler {

    private final ObjectName beanName;

    static class Holder {
        static final SwagNotificationHandler INSTANCE = new SwagNotificationHandler();
    }

    private static final Logger LOG = Logger.getInstance(SwagNotificationHandler.class);

    private SwagNotificationHandler() {
        try {
            beanName = new ObjectName("de.eudaemon.swag:type=ComponentInfo");
        } catch (MalformedObjectNameException e_) {
            throw new RuntimeException(e_);
        }
    }

    private Map<Integer, SwagHotkeyListener> hotkeyListeners = new HashMap<>();

    public static SwagNotificationHandler getInstance() {
        return Holder.INSTANCE;
    }

    public ComponentInfoMBean startListeningTo(int port, Project project) {
        try {
            Map<String, Object> connectionEnvironment = new HashMap<>();
            // JMX uses the thread's context classloader to deserialize objects. Since this
            // is called from the event thread, override with the plugin classloader to enable
            // access to SWAG classes
            connectionEnvironment.put(
                    JMXConnectorFactory.DEFAULT_CLASS_LOADER,
                    SwagNotificationHandler.class.getClassLoader());
            JMXConnector connector =
                    JMXConnectorFactory.connect(constructURL(port), connectionEnvironment);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            ComponentInfoMBean componentInfo =
                    MBeanServerInvocationHandler.newProxyInstance(
                            connection, beanName, ComponentInfoMBean.class, true);
            hotkeyListeners.put(port, new SwagHotkeyListener(componentInfo, project));
            return componentInfo;
        } catch (IOException e_) {
            return null;
        }
    }

    public void cleanup(int port) {
        if (hotkeyListeners.containsKey(port)) {
            hotkeyListeners.get(port).dispose();
            hotkeyListeners.remove(port);
        }
    }

    private static JMXServiceURL constructURL(int port) {
        try {
            return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi");
        } catch (MalformedURLException e_) {
            throw new RuntimeException(e_);
        }
    }
}
