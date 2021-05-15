package org.apache.harmony.awt.datatransfer;

import java.awt.datatransfer.SystemFlavorMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataSnapshot implements DataProvider {
    private final String[] fileList;
    private final String html;
    private final String[] nativeFormats;
    private final RawBitmap rawBitmap;
    private final Map<Class<?>, byte[]> serializedObjects = Collections.synchronizedMap(new HashMap());
    private final String text;
    private final String url;

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0045, code lost:
        r1 = r2.getRepresentationClass();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public DataSnapshot(org.apache.harmony.awt.datatransfer.DataProvider r6) {
        /*
            r5 = this;
            r5.<init>()
            java.lang.String[] r0 = r6.getNativeFormats()
            r5.nativeFormats = r0
            java.lang.String r0 = r6.getText()
            r5.text = r0
            java.lang.String[] r0 = r6.getFileList()
            r5.fileList = r0
            java.lang.String r0 = r6.getURL()
            r5.url = r0
            java.lang.String r0 = r6.getHTML()
            r5.html = r0
            org.apache.harmony.awt.datatransfer.RawBitmap r0 = r6.getRawBitmap()
            r5.rawBitmap = r0
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            java.util.Map r0 = java.util.Collections.synchronizedMap(r0)
            r5.serializedObjects = r0
            r0 = 0
        L_0x0033:
            java.lang.String[] r1 = r5.nativeFormats
            int r2 = r1.length
            if (r0 < r2) goto L_0x0039
            return
        L_0x0039:
            r2 = 0
            r1 = r1[r0]     // Catch:{ ClassNotFoundException -> 0x0042 }
            java.awt.datatransfer.DataFlavor r1 = java.awt.datatransfer.SystemFlavorMap.decodeDataFlavor(r1)     // Catch:{ ClassNotFoundException -> 0x0042 }
            r2 = r1
            goto L_0x0043
        L_0x0042:
            r1 = move-exception
        L_0x0043:
            if (r2 == 0) goto L_0x0054
            java.lang.Class r1 = r2.getRepresentationClass()
            byte[] r3 = r6.getSerializedObject(r1)
            if (r3 == 0) goto L_0x0054
            java.util.Map<java.lang.Class<?>, byte[]> r4 = r5.serializedObjects
            r4.put(r1, r3)
        L_0x0054:
            int r0 = r0 + 1
            goto L_0x0033
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.awt.datatransfer.DataSnapshot.<init>(org.apache.harmony.awt.datatransfer.DataProvider):void");
    }

    public boolean isNativeFormatAvailable(String nativeFormat) {
        if (nativeFormat == null) {
            return false;
        }
        if (nativeFormat.equals("text/plain")) {
            if (this.text != null) {
                return true;
            }
            return false;
        } else if (nativeFormat.equals("application/x-java-file-list")) {
            if (this.fileList != null) {
                return true;
            }
            return false;
        } else if (nativeFormat.equals("application/x-java-url")) {
            if (this.url != null) {
                return true;
            }
            return false;
        } else if (nativeFormat.equals("text/html")) {
            if (this.html != null) {
                return true;
            }
            return false;
        } else if (!nativeFormat.equals("image/x-java-image")) {
            try {
                return this.serializedObjects.containsKey(SystemFlavorMap.decodeDataFlavor(nativeFormat).getRepresentationClass());
            } catch (Exception e) {
                return false;
            }
        } else if (this.rawBitmap != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getText() {
        return this.text;
    }

    public String[] getFileList() {
        return this.fileList;
    }

    public String getURL() {
        return this.url;
    }

    public String getHTML() {
        return this.html;
    }

    public RawBitmap getRawBitmap() {
        return this.rawBitmap;
    }

    public int[] getRawBitmapHeader() {
        RawBitmap rawBitmap2 = this.rawBitmap;
        if (rawBitmap2 != null) {
            return rawBitmap2.getHeader();
        }
        return null;
    }

    public byte[] getRawBitmapBuffer8() {
        RawBitmap rawBitmap2 = this.rawBitmap;
        if (rawBitmap2 == null || !(rawBitmap2.buffer instanceof byte[])) {
            return null;
        }
        return (byte[]) this.rawBitmap.buffer;
    }

    public short[] getRawBitmapBuffer16() {
        RawBitmap rawBitmap2 = this.rawBitmap;
        if (rawBitmap2 == null || !(rawBitmap2.buffer instanceof short[])) {
            return null;
        }
        return (short[]) this.rawBitmap.buffer;
    }

    public int[] getRawBitmapBuffer32() {
        RawBitmap rawBitmap2 = this.rawBitmap;
        if (rawBitmap2 == null || !(rawBitmap2.buffer instanceof int[])) {
            return null;
        }
        return (int[]) this.rawBitmap.buffer;
    }

    public byte[] getSerializedObject(Class<?> clazz) {
        return this.serializedObjects.get(clazz);
    }

    public byte[] getSerializedObject(String nativeFormat) {
        try {
            return getSerializedObject((Class<?>) SystemFlavorMap.decodeDataFlavor(nativeFormat).getRepresentationClass());
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getNativeFormats() {
        return this.nativeFormats;
    }
}
