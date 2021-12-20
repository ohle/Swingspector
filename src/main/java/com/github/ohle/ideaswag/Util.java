package com.github.ohle.ideaswag;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;

import com.intellij.openapi.util.Key;

import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.ComponentInfoMBean;

public class Util {
    public static final Key<CompletableFuture<ComponentInfoMBean>> INFO_BEAN_KEY =
            Key.create("com.github.ohle.ideaswag.info-bean");

    public static String generateTitle(ComponentDescription description) {
        StringBuilder sb = new StringBuilder();
        boolean hasPrefix = false;
        if (description.name != null) {
            sb.append(description.name).append(" (");
            hasPrefix = true;
        } else if (description.text != null) {
            sb.append(StringUtils.abbreviate(description.text, 15)).append(" (");
            hasPrefix = true;
        }
        sb.append(description.simpleClassName);
        if (hasPrefix) {
            sb.append((")"));
        }
        return sb.toString();
    }
}
