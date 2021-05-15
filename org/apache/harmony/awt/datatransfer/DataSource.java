package org.apache.harmony.awt.datatransfer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DataSource implements DataProvider {
    protected final Transferable contents;
    private DataFlavor[] flavors;
    private List<String> nativeFormats;

    public DataSource(Transferable contents2) {
        this.contents = contents2;
    }

    private boolean isHtmlFlavor(DataFlavor f) {
        return "html".equalsIgnoreCase(f.getSubType());
    }

    /* access modifiers changed from: protected */
    public DataFlavor[] getDataFlavors() {
        if (this.flavors == null) {
            this.flavors = this.contents.getTransferDataFlavors();
        }
        return this.flavors;
    }

    public String[] getNativeFormats() {
        return (String[]) getNativeFormatsList().toArray(new String[0]);
    }

    public List<String> getNativeFormatsList() {
        if (this.nativeFormats == null) {
            this.nativeFormats = getNativesForFlavors(getDataFlavors());
        }
        return this.nativeFormats;
    }

    private static List<String> getNativesForFlavors(DataFlavor[] flavors2) {
        ArrayList<String> natives = new ArrayList<>();
        SystemFlavorMap flavorMap = SystemFlavorMap.getDefaultFlavorMap();
        for (DataFlavor nativesForFlavor : flavors2) {
            for (String nativeFormat : flavorMap.getNativesForFlavor(nativesForFlavor)) {
                if (!natives.contains(nativeFormat)) {
                    natives.add(nativeFormat);
                }
            }
        }
        return natives;
    }

    private String getTextFromReader(Reader r) throws IOException {
        StringBuilder buffer = new StringBuilder();
        char[] chunk = new char[1024];
        while (true) {
            int read = r.read(chunk);
            int len = read;
            if (read <= 0) {
                return buffer.toString();
            }
            buffer.append(chunk, 0, len);
        }
    }

    private String getText(boolean htmlOnly) {
        DataFlavor[] flavors2 = this.contents.getTransferDataFlavors();
        for (DataFlavor f : flavors2) {
            if (f.isFlavorTextType() && (!htmlOnly || isHtmlFlavor(f))) {
                try {
                    if (String.class.isAssignableFrom(f.getRepresentationClass())) {
                        return (String) this.contents.getTransferData(f);
                    }
                    return getTextFromReader(f.getReaderForText(this.contents));
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public String getText() {
        return getText(false);
    }

    public String[] getFileList() {
        try {
            List<?> list = (List) this.contents.getTransferData(DataFlavor.javaFileListFlavor);
            return (String[]) list.toArray(new String[list.size()]);
        } catch (Exception e) {
            return null;
        }
    }

    public String getURL() {
        try {
            return ((URL) this.contents.getTransferData(urlFlavor)).toString();
        } catch (Exception e) {
            try {
                return ((URL) this.contents.getTransferData(uriFlavor)).toString();
            } catch (Exception e2) {
                try {
                    return new URL(getText()).toString();
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    public String getHTML() {
        return getText(true);
    }

    public RawBitmap getRawBitmap() {
        DataFlavor[] flavors2 = this.contents.getTransferDataFlavors();
        for (DataFlavor f : flavors2) {
            Class<?> c = f.getRepresentationClass();
            if (c != null && Image.class.isAssignableFrom(c) && (f.isMimeTypeEqual(DataFlavor.imageFlavor) || f.isFlavorSerializedObjectType())) {
                try {
                    return getImageBitmap((Image) this.contents.getTransferData(f));
                } catch (Throwable th) {
                }
            }
        }
        return null;
    }

    private RawBitmap getImageBitmap(Image im) {
        if (im instanceof BufferedImage) {
            BufferedImage bi = (BufferedImage) im;
            if (bi.getType() == 1) {
                return getImageBitmap32(bi);
            }
        }
        int width = im.getWidth((ImageObserver) null);
        int height = im.getHeight((ImageObserver) null);
        if (width <= 0 || height <= 0) {
            return null;
        }
        BufferedImage bi2 = new BufferedImage(width, height, 1);
        Graphics gr = bi2.getGraphics();
        gr.drawImage(im, 0, 0, (ImageObserver) null);
        gr.dispose();
        return getImageBitmap32(bi2);
    }

    private RawBitmap getImageBitmap32(BufferedImage bi) {
        int[] buffer = new int[(bi.getWidth() * bi.getHeight())];
        DataBufferInt data = bi.getRaster().getDataBuffer();
        int bankCount = data.getNumBanks();
        int[] offsets = data.getOffsets();
        int bufferPos = 0;
        for (int i = 0; i < bankCount; i++) {
            int[] fragment = data.getData(i);
            System.arraycopy(fragment, offsets[i], buffer, bufferPos, fragment.length - offsets[i]);
            bufferPos += fragment.length - offsets[i];
        }
        return new RawBitmap(bi.getWidth(), bi.getHeight(), bi.getWidth(), 32, 16711680, 65280, 255, buffer);
    }

    public byte[] getSerializedObject(Class<?> clazz) {
        try {
            DataFlavor f = new DataFlavor(clazz, (String) null);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            new ObjectOutputStream(bytes).writeObject((Serializable) this.contents.getTransferData(f));
            return bytes.toByteArray();
        } catch (Throwable th) {
            return null;
        }
    }

    public boolean isNativeFormatAvailable(String nativeFormat) {
        return getNativeFormatsList().contains(nativeFormat);
    }
}
