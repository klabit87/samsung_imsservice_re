package org.apache.harmony.awt;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import org.apache.harmony.awt.datatransfer.DTK;

public final class ContextStorage {
    private static final ContextStorage globalContext = new ContextStorage();
    private final Object contextLock = new ContextLock(this, (ContextLock) null);
    private DTK dtk;
    private GraphicsEnvironment graphicsEnvironment;
    private volatile boolean shutdownPending = false;
    private Toolkit toolkit;

    private class ContextLock {
        private ContextLock() {
        }

        /* synthetic */ ContextLock(ContextStorage contextStorage, ContextLock contextLock) {
            this();
        }
    }

    public static void setDefaultToolkit(Toolkit newToolkit) {
        getCurrentContext().toolkit = newToolkit;
    }

    public static Toolkit getDefaultToolkit() {
        return getCurrentContext().toolkit;
    }

    public static void setDTK(DTK dtk2) {
        getCurrentContext().dtk = dtk2;
    }

    public static DTK getDTK() {
        return getCurrentContext().dtk;
    }

    public static Object getContextLock() {
        return getCurrentContext().contextLock;
    }

    public static GraphicsEnvironment getGraphicsEnvironment() {
        return getCurrentContext().graphicsEnvironment;
    }

    public static void setGraphicsEnvironment(GraphicsEnvironment environment) {
        getCurrentContext().graphicsEnvironment = environment;
    }

    private static ContextStorage getCurrentContext() {
        return globalContext;
    }

    public static boolean shutdownPending() {
        return getCurrentContext().shutdownPending;
    }

    /* access modifiers changed from: package-private */
    public void shutdown() {
    }
}
