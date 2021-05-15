package com.sec.internal.ims.core.handler.secims;

import android.util.LongSparseArray;
import android.util.Pair;
import com.sec.ims.options.Capabilities;
import com.sec.internal.constants.ims.XmlElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XmlDataStructureWrapper {
    private static final String CAP_AVAILABLE = "true";
    private static final String CAP_SUPPORTED = "supported";
    private static final String CAP_UNAVAILABLE = "false";
    private static final String DEVICE_CAP_MOBILITY = "mobility";
    private static final String LANGUAGE = "lang";
    private static final String LOG_TAG = "XDM-WRAPPER";
    private static final String MEDIA_CAP_AUDIO = "audio";
    private static final String MEDIA_CAP_DUPLEX = "duplex";
    private static final String MEDIA_CAP_FULL_DUPLEX = "full";
    private static final String MEDIA_CAP_VIDEO = "video";
    private static final String XML_NS = "xml";
    private static LongSparseArray<List<XmlElement>> mMediaCapabilities;

    private XmlDataStructureWrapper() {
    }

    public static List<XmlElement> getTextElements(String elementName, List<Pair<String, String>> content) {
        List<XmlElement> textElements = new ArrayList<>();
        for (Pair<String, String> txt : content) {
            if (txt.first != null) {
                textElements.add(new XmlElement(elementName, (String) txt.second).addAttribute(LANGUAGE, (String) txt.first, XML_NS));
            } else {
                textElements.add(new XmlElement(elementName, (String) txt.second));
            }
        }
        return textElements;
    }

    public static List<XmlElement> getMediaCapabilityElements(long feature) {
        return mMediaCapabilities.get((long) ((int) feature)) == null ? new ArrayList() : mMediaCapabilities.get((long) ((int) feature));
    }

    static {
        LongSparseArray<List<XmlElement>> longSparseArray = new LongSparseArray<>();
        mMediaCapabilities = longSparseArray;
        longSparseArray.put((long) Capabilities.FEATURE_MMTEL, Arrays.asList(new XmlElement[]{new XmlElement("audio", "true"), new XmlElement("video", "false"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_MMTEL_VIDEO, Arrays.asList(new XmlElement[]{new XmlElement("audio", "true"), new XmlElement("video", "true"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_IPCALL, Arrays.asList(new XmlElement[]{new XmlElement("audio", "true"), new XmlElement("video", "false"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_IPCALL_VIDEO, Arrays.asList(new XmlElement[]{new XmlElement("audio", "true"), new XmlElement("video", "true"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_IPCALL_VIDEO_ONLY, Arrays.asList(new XmlElement[]{new XmlElement("audio", "true"), new XmlElement("video", "true"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
    }

    public static List<XmlElement> getDeviceCapabilityElements(List<String> capabilities) {
        List<XmlElement> deviceCapabilities = new ArrayList<>();
        for (String cap : capabilities) {
            deviceCapabilities.add(new XmlElement(cap).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(DEVICE_CAP_MOBILITY)));
        }
        return deviceCapabilities;
    }
}
