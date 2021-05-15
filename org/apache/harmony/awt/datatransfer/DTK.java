package org.apache.harmony.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.nio.charset.Charset;
import org.apache.harmony.awt.internal.nls.Messages;
import org.apache.harmony.misc.SystemUtils;

public abstract class DTK {
    protected final DataTransferThread dataTransferThread;
    private NativeClipboard nativeClipboard = null;
    private NativeClipboard nativeSelection = null;
    protected SystemFlavorMap systemFlavorMap;

    public abstract DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dragGestureEvent);

    public abstract DropTargetContextPeer createDropTargetContextPeer(DropTargetContext dropTargetContext);

    public abstract void initDragAndDrop();

    /* access modifiers changed from: protected */
    public abstract NativeClipboard newNativeClipboard();

    /* access modifiers changed from: protected */
    public abstract NativeClipboard newNativeSelection();

    public abstract void runEventLoop();

    protected DTK() {
        DataTransferThread dataTransferThread2 = new DataTransferThread(this);
        this.dataTransferThread = dataTransferThread2;
        dataTransferThread2.start();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static org.apache.harmony.awt.datatransfer.DTK getDTK() {
        /*
            java.lang.Object r0 = org.apache.harmony.awt.ContextStorage.getContextLock()
            monitor-enter(r0)
            boolean r1 = org.apache.harmony.awt.ContextStorage.shutdownPending()     // Catch:{ all -> 0x001e }
            if (r1 == 0) goto L_0x000e
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            r0 = 0
            return r0
        L_0x000e:
            org.apache.harmony.awt.datatransfer.DTK r1 = org.apache.harmony.awt.ContextStorage.getDTK()     // Catch:{ all -> 0x001e }
            if (r1 != 0) goto L_0x001c
            org.apache.harmony.awt.datatransfer.DTK r2 = createDTK()     // Catch:{ all -> 0x001e }
            r1 = r2
            org.apache.harmony.awt.ContextStorage.setDTK(r1)     // Catch:{ all -> 0x001e }
        L_0x001c:
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            return r1
        L_0x001e:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.awt.datatransfer.DTK.getDTK():org.apache.harmony.awt.datatransfer.DTK");
    }

    public synchronized SystemFlavorMap getSystemFlavorMap() {
        return this.systemFlavorMap;
    }

    public synchronized void setSystemFlavorMap(SystemFlavorMap newFlavorMap) {
        this.systemFlavorMap = newFlavorMap;
    }

    public NativeClipboard getNativeClipboard() {
        if (this.nativeClipboard == null) {
            this.nativeClipboard = newNativeClipboard();
        }
        return this.nativeClipboard;
    }

    public NativeClipboard getNativeSelection() {
        if (this.nativeSelection == null) {
            this.nativeSelection = newNativeSelection();
        }
        return this.nativeSelection;
    }

    private static DTK createDTK() {
        String name;
        int os = SystemUtils.getOS();
        if (os == 1) {
            name = "org.apache.harmony.awt.datatransfer.windows.WinDTK";
        } else if (os == 2) {
            name = "org.apache.harmony.awt.datatransfer.linux.LinuxDTK";
        } else {
            throw new RuntimeException(Messages.getString("awt.4E"));
        }
        try {
            return (DTK) Class.forName(name).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDefaultCharset() {
        return "unicode";
    }

    /* access modifiers changed from: protected */
    public String[] getCharsets() {
        return new String[]{"UTF-16", "UTF-8", "unicode", "ISO-8859-1", "US-ASCII"};
    }

    public void initSystemFlavorMap(SystemFlavorMap fm) {
        String[] charsets = getCharsets();
        appendSystemFlavorMap(fm, DataFlavor.stringFlavor, "text/plain");
        appendSystemFlavorMap(fm, charsets, "plain", "text/plain");
        appendSystemFlavorMap(fm, charsets, "html", "text/html");
        appendSystemFlavorMap(fm, DataProvider.urlFlavor, "application/x-java-url");
        appendSystemFlavorMap(fm, charsets, "uri-list", "application/x-java-url");
        appendSystemFlavorMap(fm, DataFlavor.javaFileListFlavor, "application/x-java-file-list");
        appendSystemFlavorMap(fm, DataFlavor.imageFlavor, "image/x-java-image");
    }

    /* access modifiers changed from: protected */
    public void appendSystemFlavorMap(SystemFlavorMap fm, DataFlavor flav, String nat) {
        fm.addFlavorForUnencodedNative(nat, flav);
        fm.addUnencodedNativeForFlavor(flav, nat);
    }

    /* access modifiers changed from: protected */
    public void appendSystemFlavorMap(SystemFlavorMap fm, String[] charsets, String subType, String nat) {
        TextFlavor.addUnicodeClasses(fm, nat, subType);
        for (int i = 0; i < charsets.length; i++) {
            if (charsets[i] != null && Charset.isSupported(charsets[i])) {
                TextFlavor.addCharsetClasses(fm, nat, subType, charsets[i]);
            }
        }
    }
}
