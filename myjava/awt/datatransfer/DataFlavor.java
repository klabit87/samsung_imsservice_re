package myjava.awt.datatransfer;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import myjava.awt.datatransfer.MimeTypeProcessor;
import org.apache.harmony.awt.datatransfer.DTK;
import org.apache.harmony.awt.datatransfer.DataProvider;
import org.apache.harmony.awt.internal.nls.Messages;

public class DataFlavor implements Externalizable, Cloneable {
    public static final DataFlavor javaFileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List", "application/x-java-file-list");
    public static final String javaJVMLocalObjectMimeType = "application/x-java-jvm-local-objectref";
    public static final String javaRemoteObjectMimeType = "application/x-java-remote-object";
    public static final String javaSerializedObjectMimeType = "application/x-java-serialized-object";
    @Deprecated
    public static final DataFlavor plainTextFlavor = new DataFlavor("text/plain; charset=unicode; class=java.io.InputStream", "Plain Text");
    private static DataFlavor plainUnicodeFlavor = null;
    private static final long serialVersionUID = 8367026044764648243L;
    private static final String[] sortedTextFlavors = {"text/sgml", "text/xml", "text/html", "text/rtf", "text/enriched", "text/richtext", DataProvider.TYPE_URILIST, "text/tab-separated-values", "text/t140", "text/rfc822-headers", "text/parityfec", "text/directory", "text/css", "text/calendar", "application/x-java-serialized-object", "text/plain"};
    public static final DataFlavor stringFlavor = new DataFlavor("application/x-java-serialized-object; class=java.lang.String", "Unicode String");
    private String humanPresentableName;
    private MimeTypeProcessor.MimeType mimeInfo;
    private Class<?> representationClass;

    public static final DataFlavor getTextPlainUnicodeFlavor() {
        if (plainUnicodeFlavor == null) {
            plainUnicodeFlavor = new DataFlavor("text/plain; charset=" + DTK.getDTK().getDefaultCharset() + "; class=java.io.InputStream", "Plain Text");
        }
        return plainUnicodeFlavor;
    }

    protected static final Class<?> tryToLoadClass(String className, ClassLoader fallback) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            try {
                return ClassLoader.getSystemClassLoader().loadClass(className);
            } catch (ClassNotFoundException e2) {
                ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
                if (contextLoader != null) {
                    try {
                        return contextLoader.loadClass(className);
                    } catch (ClassNotFoundException e3) {
                        return fallback.loadClass(className);
                    }
                }
                return fallback.loadClass(className);
            }
        }
    }

    private static boolean isCharsetSupported(String charset) {
        try {
            return Charset.isSupported(charset);
        } catch (IllegalCharsetNameException e) {
            return false;
        }
    }

    public DataFlavor() {
        this.mimeInfo = null;
        this.humanPresentableName = null;
        this.representationClass = null;
    }

    public DataFlavor(Class<?> representationClass2, String humanPresentableName2) {
        this.mimeInfo = new MimeTypeProcessor.MimeType("application", "x-java-serialized-object");
        if (humanPresentableName2 != null) {
            this.humanPresentableName = humanPresentableName2;
        } else {
            this.humanPresentableName = "application/x-java-serialized-object";
        }
        this.mimeInfo.addParameter("class", representationClass2.getName());
        this.representationClass = representationClass2;
    }

    public DataFlavor(String mimeType, String humanPresentableName2) {
        try {
            init(mimeType, humanPresentableName2, (ClassLoader) null);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(Messages.getString("awt.16C", (Object) this.mimeInfo.getParameter("class")), e);
        }
    }

    public DataFlavor(String mimeType) throws ClassNotFoundException {
        init(mimeType, (String) null, (ClassLoader) null);
    }

    public DataFlavor(String mimeType, String humanPresentableName2, ClassLoader classLoader) throws ClassNotFoundException {
        init(mimeType, humanPresentableName2, classLoader);
    }

    private void init(String mimeType, String humanPresentableName2, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> cls;
        try {
            MimeTypeProcessor.MimeType parse = MimeTypeProcessor.parse(mimeType);
            this.mimeInfo = parse;
            if (humanPresentableName2 != null) {
                this.humanPresentableName = humanPresentableName2;
            } else {
                this.humanPresentableName = String.valueOf(parse.getPrimaryType()) + '/' + this.mimeInfo.getSubType();
            }
            String className = this.mimeInfo.getParameter("class");
            if (className == null) {
                className = "java.io.InputStream";
                this.mimeInfo.addParameter("class", className);
            }
            if (classLoader == null) {
                cls = Class.forName(className);
            } else {
                cls = classLoader.loadClass(className);
            }
            this.representationClass = cls;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(Messages.getString("awt.16D", (Object) mimeType));
        }
    }

    private String getCharset() {
        if (this.mimeInfo == null || isCharsetRedundant()) {
            return "";
        }
        String charset = this.mimeInfo.getParameter("charset");
        if (isCharsetRequired() && (charset == null || charset.length() == 0)) {
            return DTK.getDTK().getDefaultCharset();
        }
        if (charset == null) {
            return "";
        }
        return charset;
    }

    private boolean isCharsetRequired() {
        String type = this.mimeInfo.getFullType();
        return type.equals("text/sgml") || type.equals("text/xml") || type.equals("text/html") || type.equals("text/enriched") || type.equals("text/richtext") || type.equals(DataProvider.TYPE_URILIST) || type.equals("text/directory") || type.equals("text/css") || type.equals("text/calendar") || type.equals("application/x-java-serialized-object") || type.equals("text/plain");
    }

    private boolean isCharsetRedundant() {
        String type = this.mimeInfo.getFullType();
        return type.equals("text/rtf") || type.equals("text/tab-separated-values") || type.equals("text/t140") || type.equals("text/rfc822-headers") || type.equals("text/parityfec");
    }

    /* access modifiers changed from: package-private */
    public MimeTypeProcessor.MimeType getMimeInfo() {
        return this.mimeInfo;
    }

    public String getPrimaryType() {
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null) {
            return mimeType.getPrimaryType();
        }
        return null;
    }

    public String getSubType() {
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null) {
            return mimeType.getSubType();
        }
        return null;
    }

    public String getMimeType() {
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null) {
            return MimeTypeProcessor.assemble(mimeType);
        }
        return null;
    }

    public String getParameter(String paramName) {
        String lowerName = paramName.toLowerCase();
        if (lowerName.equals("humanpresentablename")) {
            return this.humanPresentableName;
        }
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null) {
            return mimeType.getParameter(lowerName);
        }
        return null;
    }

    public String getHumanPresentableName() {
        return this.humanPresentableName;
    }

    public void setHumanPresentableName(String humanPresentableName2) {
        this.humanPresentableName = humanPresentableName2;
    }

    public Class<?> getRepresentationClass() {
        return this.representationClass;
    }

    public final Class<?> getDefaultRepresentationClass() {
        return InputStream.class;
    }

    public final String getDefaultRepresentationClassAsString() {
        return getDefaultRepresentationClass().getName();
    }

    public boolean isRepresentationClassSerializable() {
        return Serializable.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassRemote() {
        return false;
    }

    public boolean isRepresentationClassReader() {
        return Reader.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassInputStream() {
        return InputStream.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassCharBuffer() {
        return CharBuffer.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassByteBuffer() {
        return ByteBuffer.class.isAssignableFrom(this.representationClass);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public String normalizeMimeTypeParameter(String parameterName, String parameterValue) {
        return parameterValue;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public String normalizeMimeType(String mimeType) {
        return mimeType;
    }

    public final boolean isMimeTypeEqual(DataFlavor dataFlavor) {
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null) {
            return mimeType.equals(dataFlavor.mimeInfo);
        }
        return dataFlavor.mimeInfo == null;
    }

    public boolean isMimeTypeEqual(String mimeType) {
        try {
            return this.mimeInfo.equals(MimeTypeProcessor.parse(mimeType));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public synchronized void writeExternal(ObjectOutput os) throws IOException {
        os.writeObject(this.humanPresentableName);
        os.writeObject(this.mimeInfo);
    }

    public synchronized void readExternal(ObjectInput is) throws IOException, ClassNotFoundException {
        this.humanPresentableName = (String) is.readObject();
        MimeTypeProcessor.MimeType mimeType = (MimeTypeProcessor.MimeType) is.readObject();
        this.mimeInfo = mimeType;
        this.representationClass = mimeType != null ? Class.forName(mimeType.getParameter("class")) : null;
    }

    public Object clone() throws CloneNotSupportedException {
        MimeTypeProcessor.MimeType mimeType;
        DataFlavor clone = new DataFlavor();
        clone.humanPresentableName = this.humanPresentableName;
        clone.representationClass = this.representationClass;
        MimeTypeProcessor.MimeType mimeType2 = this.mimeInfo;
        if (mimeType2 != null) {
            mimeType = (MimeTypeProcessor.MimeType) mimeType2.clone();
        } else {
            mimeType = null;
        }
        clone.mimeInfo = mimeType;
        return clone;
    }

    public String toString() {
        return String.valueOf(getClass().getName()) + "[MimeType=(" + getMimeType() + ");humanPresentableName=" + this.humanPresentableName + "]";
    }

    public boolean isMimeTypeSerializedObject() {
        return isMimeTypeEqual("application/x-java-serialized-object");
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DataFlavor)) {
            return false;
        }
        return equals((DataFlavor) o);
    }

    public boolean equals(DataFlavor that) {
        if (that == this) {
            return true;
        }
        if (that == null) {
            return false;
        }
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType == null) {
            if (that.mimeInfo == null) {
                return true;
            }
            return false;
        } else if (!mimeType.equals(that.mimeInfo) || !this.representationClass.equals(that.representationClass)) {
            return false;
        } else {
            if (!this.mimeInfo.getPrimaryType().equals("text") || isUnicodeFlavor()) {
                return true;
            }
            String charset1 = getCharset();
            String charset2 = that.getCharset();
            if (!isCharsetSupported(charset1) || !isCharsetSupported(charset2)) {
                return charset1.equalsIgnoreCase(charset2);
            }
            return Charset.forName(charset1).equals(Charset.forName(charset2));
        }
    }

    @Deprecated
    public boolean equals(String s) {
        if (s == null) {
            return false;
        }
        return isMimeTypeEqual(s);
    }

    public boolean match(DataFlavor that) {
        return equals(that);
    }

    public int hashCode() {
        return getKeyInfo().hashCode();
    }

    private String getKeyInfo() {
        String key = String.valueOf(this.mimeInfo.getFullType()) + ";class=" + this.representationClass.getName();
        if (!this.mimeInfo.getPrimaryType().equals("text") || isUnicodeFlavor()) {
            return key;
        }
        return String.valueOf(key) + ";charset=" + getCharset().toLowerCase();
    }

    public boolean isFlavorSerializedObjectType() {
        return isMimeTypeSerializedObject() && isRepresentationClassSerializable();
    }

    public boolean isFlavorRemoteObjectType() {
        return isMimeTypeEqual(javaRemoteObjectMimeType) && isRepresentationClassRemote();
    }

    public boolean isFlavorJavaFileListType() {
        return List.class.isAssignableFrom(this.representationClass) && isMimeTypeEqual(javaFileListFlavor);
    }

    public boolean isFlavorTextType() {
        if (equals(stringFlavor) || equals(plainTextFlavor)) {
            return true;
        }
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null && !mimeType.getPrimaryType().equals("text")) {
            return false;
        }
        String charset = getCharset();
        if (!isByteCodeFlavor()) {
            return isUnicodeFlavor();
        }
        if (charset.length() != 0) {
            return isCharsetSupported(charset);
        }
        return true;
    }

    public Reader getReaderForText(Transferable transferable) throws UnsupportedFlavorException, IOException {
        InputStream stream;
        Object data = transferable.getTransferData(this);
        if (data == null) {
            throw new IllegalArgumentException(Messages.getString("awt.16E"));
        } else if (data instanceof Reader) {
            Reader reader = (Reader) data;
            reader.reset();
            return reader;
        } else if (data instanceof String) {
            return new StringReader((String) data);
        } else {
            if (data instanceof CharBuffer) {
                return new CharArrayReader(((CharBuffer) data).array());
            }
            if (data instanceof char[]) {
                return new CharArrayReader((char[]) data);
            }
            String charset = getCharset();
            if (data instanceof InputStream) {
                stream = (InputStream) data;
                stream.reset();
            } else if (data instanceof ByteBuffer) {
                stream = new ByteArrayInputStream(((ByteBuffer) data).array());
            } else if (data instanceof byte[]) {
                stream = new ByteArrayInputStream((byte[]) data);
            } else {
                throw new IllegalArgumentException(Messages.getString("awt.16F"));
            }
            if (charset.length() == 0) {
                return new InputStreamReader(stream);
            }
            return new InputStreamReader(stream, charset);
        }
    }

    public static final DataFlavor selectBestTextFlavor(DataFlavor[] availableFlavors) {
        if (availableFlavors == null) {
            return null;
        }
        List<List<DataFlavor>> sorted = sortTextFlavorsByType(new LinkedList(Arrays.asList(availableFlavors)));
        if (sorted.isEmpty()) {
            return null;
        }
        List<DataFlavor> bestSorted = sorted.get(0);
        if (bestSorted.size() == 1) {
            return bestSorted.get(0);
        }
        if (bestSorted.get(0).getCharset().length() == 0) {
            return selectBestFlavorWOCharset(bestSorted);
        }
        return selectBestFlavorWCharset(bestSorted);
    }

    private static DataFlavor selectBestFlavorWCharset(List<DataFlavor> list) {
        List<DataFlavor> best = getFlavors(list, (Class<?>) Reader.class);
        if (best != null) {
            return best.get(0);
        }
        List<DataFlavor> best2 = getFlavors(list, (Class<?>) String.class);
        if (best2 != null) {
            return best2.get(0);
        }
        List<DataFlavor> best3 = getFlavors(list, (Class<?>) CharBuffer.class);
        if (best3 != null) {
            return best3.get(0);
        }
        List<DataFlavor> best4 = getFlavors(list, (Class<?>) char[].class);
        if (best4 != null) {
            return best4.get(0);
        }
        return selectBestByCharset(list);
    }

    private static DataFlavor selectBestByCharset(List<DataFlavor> list) {
        List<DataFlavor> best = getFlavors(list, new String[]{"UTF-16", "UTF-8", "UTF-16BE", "UTF-16LE"});
        if (best == null && (best = getFlavors(list, new String[]{DTK.getDTK().getDefaultCharset()})) == null && (best = getFlavors(list, new String[]{"US-ASCII"})) == null) {
            best = selectBestByAlphabet(list);
        }
        if (best == null) {
            return null;
        }
        if (best.size() == 1) {
            return best.get(0);
        }
        return selectBestFlavorWOCharset(best);
    }

    private static List<DataFlavor> selectBestByAlphabet(List<DataFlavor> list) {
        String[] charsets = new String[list.size()];
        LinkedList<DataFlavor> best = new LinkedList<>();
        for (int i = 0; i < charsets.length; i++) {
            charsets[i] = list.get(i).getCharset();
        }
        Arrays.sort(charsets, String.CASE_INSENSITIVE_ORDER);
        for (DataFlavor flavor : list) {
            if (charsets[0].equalsIgnoreCase(flavor.getCharset())) {
                best.add(flavor);
            }
        }
        if (best.isEmpty()) {
            return null;
        }
        return best;
    }

    private static List<DataFlavor> getFlavors(List<DataFlavor> list, String[] charset) {
        LinkedList<DataFlavor> sublist = new LinkedList<>();
        Iterator<DataFlavor> i = list.iterator();
        while (i.hasNext()) {
            DataFlavor flavor = i.next();
            if (isCharsetSupported(flavor.getCharset())) {
                for (String element : charset) {
                    if (Charset.forName(element).equals(Charset.forName(flavor.getCharset()))) {
                        sublist.add(flavor);
                    }
                }
            } else {
                i.remove();
            }
        }
        if (sublist.isEmpty()) {
            return null;
        }
        return list;
    }

    private static DataFlavor selectBestFlavorWOCharset(List<DataFlavor> list) {
        List<DataFlavor> best = getFlavors(list, (Class<?>) InputStream.class);
        if (best != null) {
            return best.get(0);
        }
        List<DataFlavor> best2 = getFlavors(list, (Class<?>) ByteBuffer.class);
        if (best2 != null) {
            return best2.get(0);
        }
        List<DataFlavor> best3 = getFlavors(list, (Class<?>) byte[].class);
        if (best3 != null) {
            return best3.get(0);
        }
        return list.get(0);
    }

    private static List<DataFlavor> getFlavors(List<DataFlavor> list, Class<?> klass) {
        LinkedList<DataFlavor> sublist = new LinkedList<>();
        for (DataFlavor flavor : list) {
            if (flavor.representationClass.equals(klass)) {
                sublist.add(flavor);
            }
        }
        if (sublist.isEmpty()) {
            return null;
        }
        return list;
    }

    private static List<List<DataFlavor>> sortTextFlavorsByType(List<DataFlavor> availableFlavors) {
        LinkedList<List<DataFlavor>> list = new LinkedList<>();
        for (String element : sortedTextFlavors) {
            List<DataFlavor> subList = fetchTextFlavors(availableFlavors, element);
            if (subList != null) {
                list.addLast(subList);
            }
        }
        if (!availableFlavors.isEmpty()) {
            list.addLast(availableFlavors);
        }
        return list;
    }

    private static List<DataFlavor> fetchTextFlavors(List<DataFlavor> availableFlavors, String mimeType) {
        LinkedList<DataFlavor> list = new LinkedList<>();
        Iterator<DataFlavor> i = availableFlavors.iterator();
        while (i.hasNext()) {
            DataFlavor flavor = i.next();
            if (!flavor.isFlavorTextType()) {
                i.remove();
            } else if (flavor.mimeInfo.getFullType().equals(mimeType)) {
                if (!list.contains(flavor)) {
                    list.add(flavor);
                }
                i.remove();
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    private boolean isUnicodeFlavor() {
        Class<?> cls = this.representationClass;
        if (cls != null) {
            return cls.equals(Reader.class) || this.representationClass.equals(String.class) || this.representationClass.equals(CharBuffer.class) || this.representationClass.equals(char[].class);
        }
        return false;
    }

    private boolean isByteCodeFlavor() {
        Class<?> cls = this.representationClass;
        if (cls != null) {
            return cls.equals(InputStream.class) || this.representationClass.equals(ByteBuffer.class) || this.representationClass.equals(byte[].class);
        }
        return false;
    }
}
