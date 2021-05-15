package com.sec.internal.ims.settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NvStorage {
    private static final String DEFAULT_NAME = "DEFAULT";
    public static final String ID_OMADM = "omadm";
    private static final String IMS_NV_STORAGE_XML = "/efs/sec_efs/ims_nv_";
    private static final String LOG_TAG = NvStorage.class.getSimpleName();
    private static final String OMADM_PREFIX = "omadm/./3GPP_IMS/";
    protected static final String ROOT_ELEMENT = "NV_STORAGE";
    private static final String SILENT_REDIAL_PATH = "/efs/sec_efs/silent_redial";
    private Context mContext;
    protected Document mDoc = null;
    private SimpleEventLog mEventLog;
    private final Object mLock = new Object();
    private String mName;
    protected File mNvFile;
    private int mPhoneId;

    public NvStorage(Context context, String name, int phoneId) {
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 100);
        this.mContext = context;
        this.mName = "";
        this.mPhoneId = phoneId;
        setNvFile(name);
    }

    private void setName(String name) {
        if (TextUtils.isEmpty(name)) {
            this.mName = DEFAULT_NAME;
        } else {
            this.mName = name.split(",")[0];
        }
    }

    private String getName(String name) {
        if (TextUtils.isEmpty(name)) {
            return DEFAULT_NAME;
        }
        return name.split(",")[0];
    }

    public synchronized boolean setNvFile(String name) {
        String name2 = getName(name);
        if (this.mName.equalsIgnoreCase(name2)) {
            Log.d(LOG_TAG, "same nv file. do nothing");
            return false;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("loading new nv file: " + name2);
        setName(name2);
        this.mDoc = null;
        this.mNvFile = new File(IMS_NV_STORAGE_XML + this.mName + ".xml");
        String str = LOG_TAG;
        Log.d(str, "mNvFile: " + this.mNvFile);
        initNvStorage();
        initDoc();
        initElements();
        try {
            Os.chmod(this.mNvFile.getAbsolutePath(), 432);
            Os.chmod(SILENT_REDIAL_PATH, 432);
        } catch (ErrnoException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "chmod error!! : " + e);
        }
        return true;
    }

    public void close() {
        synchronized (this.mLock) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("Close : " + this.mNvFile);
            this.mNvFile = null;
            this.mDoc = null;
        }
    }

    private synchronized void initElements() {
        create(ID_OMADM);
    }

    private synchronized void initDoc() {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (this.mDoc == null) {
                Document parse = dBuilder.parse(this.mNvFile);
                this.mDoc = parse;
                if (parse == null) {
                    return;
                }
            }
            if (this.mDoc.getDocumentElement() == null) {
                Log.e(LOG_TAG, "mDoc.getDocumentElement(): null");
                initNvStorage(true);
            } else {
                this.mDoc.getDocumentElement().normalize();
            }
        } catch (ParserConfigurationException e) {
            Log.e(LOG_TAG, "dbFactory exception");
        } catch (SAXException e2) {
            String str = LOG_TAG;
            Log.e(str, "dBuilder.parse SAXException exception, " + e2);
            initNvStorage(true);
        } catch (IOException e3) {
            String str2 = LOG_TAG;
            Log.e(str2, "dBuilder.parse IOException exception, " + e3);
        }
    }

    private synchronized void initNvStorage() {
        initNvStorage(false);
    }

    private synchronized void initNvStorage(boolean forced) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("initNvStorage(): isForce: " + forced);
        File nvStorage = this.mNvFile;
        if (forced || !nvStorage.exists()) {
            try {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.createElement(ROOT_ELEMENT));
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("indent", "yes");
                transformer.transform(new DOMSource(doc), new StreamResult(nvStorage));
            } catch (ParserConfigurationException e) {
                Log.d(LOG_TAG, "initNvStorage exception");
            } catch (TransformerException e2) {
                String str = LOG_TAG;
                Log.d(str, "initNvStorage TransformerException exception" + e2);
            }
        }
    }

    private synchronized void create(String element) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("create: table " + element);
        initDoc();
        if (this.mDoc == null) {
            String str = LOG_TAG;
            Log.e(str, this.mNvFile.getName() + " open failed");
            return;
        }
        NodeList targetNode = this.mDoc.getElementsByTagName(element);
        if (targetNode == null || targetNode.getLength() == 0) {
            NodeList root = this.mDoc.getElementsByTagName(ROOT_ELEMENT);
            if (root != null) {
                if (root.getLength() != 0) {
                    root.item(0).appendChild(this.mDoc.createElement(element));
                    try {
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty("indent", "yes");
                        transformer.transform(new DOMSource(this.mDoc), new StreamResult(this.mNvFile));
                    } catch (TransformerException e) {
                        String str2 = LOG_TAG;
                        Log.d(str2, "create() TransformerException exception" + e);
                    }
                }
            }
            Log.e(LOG_TAG, "root is empty");
            return;
        }
        return;
    }

    public void insert(String element, ContentValues values) {
        String str = LOG_TAG;
        Log.d(str, "insert: " + values);
        synchronized (this.mLock) {
            save(element, values);
        }
    }

    public Cursor query(String element, String[] projection) {
        MatrixCursor cursor;
        String str = LOG_TAG;
        Log.d(str, "query: " + element + "," + Arrays.toString(projection));
        synchronized (this.mLock) {
            Map<String, Object> cv = readFromStorage(element, projection);
            if (ImsUtil.isCdmalessEnabled(this.mPhoneId) && cv != null && cv.containsKey("SMS_FORMAT")) {
                cv.put("SMS_FORMAT", "3GPP");
                Log.d(LOG_TAG, "VZW CDMA-less case! Return fake SMS_FORAMT(3GPP) by force");
            }
            cursor = null;
            if (cv != null && cv.size() > 0) {
                String[] columnNames = {"PATH", "VALUE"};
                String[] columnValues = new String[columnNames.length];
                cursor = new MatrixCursor(columnNames);
                for (Map.Entry<String, Object> entry : cv.entrySet()) {
                    columnValues[0] = "omadm/./3GPP_IMS/" + entry.getKey();
                    columnValues[1] = (String) entry.getValue();
                    cursor.addRow(columnValues);
                }
            }
        }
        return cursor;
    }

    private Map<String, Object> readFromStorage(String element, String[] projection) {
        Map<String, Object> cv = new HashMap<>();
        initDoc();
        Document document = this.mDoc;
        if (document == null) {
            String str = LOG_TAG;
            Log.e(str, this.mNvFile.getName() + " open failed");
            return null;
        }
        NodeList nList = document.getElementsByTagName(element);
        if (nList != null) {
            Node nNode = nList.item(0);
            if (nNode == null) {
                String str2 = LOG_TAG;
                Log.e(str2, "query(" + element + "): nNode is null");
                initElements();
                return null;
            }
            NamedNodeMap attributes = nNode.getAttributes();
            Set<String> values = null;
            if (projection != null) {
                for (int i = 0; i < projection.length; i++) {
                    projection[i] = projection[i].replace("omadm/./3GPP_IMS/", "");
                }
                values = new HashSet<>(Arrays.asList(projection));
            }
            for (int j = 0; j < attributes.getLength(); j++) {
                Node attr = attributes.item(j);
                if (values == null || values.contains(attr.getNodeName())) {
                    cv.put(attr.getNodeName(), attr.getNodeValue());
                }
            }
        }
        return cv;
    }

    public int delete(String element) {
        int numDeleteAttributes;
        this.mEventLog.add("delete: table " + element);
        synchronized (this.mLock) {
            initDoc();
            if (this.mDoc == null) {
                Log.e(LOG_TAG, this.mNvFile.getName() + " open failed");
                return 0;
            }
            NodeList targetChild = this.mDoc.getElementsByTagName(element);
            if (targetChild == null) {
                Log.e(LOG_TAG, "delete(" + element + "): targetChild is null");
                initElements();
                return 0;
            }
            numDeleteAttributes = 0;
            Element targetElement = (Element) targetChild.item(0);
            NamedNodeMap attributes = targetElement.getAttributes();
            int j = attributes.getLength();
            while (j > 0) {
                j--;
                targetElement.removeAttribute(attributes.item(j).getNodeName());
                numDeleteAttributes++;
            }
            try {
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(this.mDoc), new StreamResult(this.mNvFile));
            } catch (TransformerException e) {
                Log.d(LOG_TAG, "delete() TransformerException exception" + e);
            }
        }
        return numDeleteAttributes;
    }

    private void save(String element, ContentValues cv) {
        initDoc();
        Document document = this.mDoc;
        if (document == null) {
            String str = LOG_TAG;
            Log.e(str, this.mNvFile.getName() + " open failed");
            return;
        }
        Element targetElement = (Element) document.getElementsByTagName(element).item(0);
        if (targetElement == null) {
            String str2 = LOG_TAG;
            Log.e(str2, "save(" + element + "): targetElement is null");
            initElements();
            return;
        }
        for (Map.Entry<String, Object> e : cv.valueSet()) {
            String key = e.getKey().replace("omadm/./3GPP_IMS/", "");
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("save: " + key + " [" + e.getValue().toString() + "]");
            targetElement.setAttribute(key, e.getValue().toString());
            if ("silent_redial".equalsIgnoreCase(key)) {
                writeSilentRedial(e.getValue().toString());
            }
        }
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.transform(new DOMSource(this.mDoc), new StreamResult(this.mNvFile));
        } catch (TransformerException e2) {
            String str3 = LOG_TAG;
            Log.d(str3, "reset() TransformerException exception" + e2);
        }
        for (Map.Entry<String, Object> e3 : cv.valueSet()) {
            String key2 = e3.getKey().replace("omadm/./3GPP_IMS/", "");
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.notifyChange(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/" + key2, this.mPhoneId), (ContentObserver) null);
        }
    }

    private synchronized void writeSilentRedial(String value) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(SILENT_REDIAL_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            writer = null;
        }
        if (writer != null) {
            writer.print(value);
            writer.close();
        }
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of NvStorage:");
        String str = LOG_TAG;
        IMSLog.dump(str, "NV File: " + this.mNvFile.toString());
        this.mEventLog.dump();
        Optional.ofNullable(readFromStorage(ID_OMADM, (String[]) null)).map($$Lambda$NvStorage$Rz9INm4C9k7RguX0AjnIQzDQEU4.INSTANCE);
    }

    static /* synthetic */ Object lambda$dump$1(Map nv) {
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Last value of NV OMADM:");
        IMSLog.increaseIndent(LOG_TAG);
        nv.forEach($$Lambda$NvStorage$ppLgFsgRT8hdRzaEUIEiYAsXLJQ.INSTANCE);
        IMSLog.decreaseIndent(LOG_TAG);
        IMSLog.decreaseIndent(LOG_TAG);
        return null;
    }

    static /* synthetic */ void lambda$dump$0(String k, Object v) {
        String str = LOG_TAG;
        IMSLog.dump(str, k + ": " + v);
    }
}
