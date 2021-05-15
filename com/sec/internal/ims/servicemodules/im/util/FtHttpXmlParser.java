package com.sec.internal.ims.servicemodules.im.util;

import android.util.Log;
import android.util.Xml;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FtHttpXmlParser {
    private static final String LOG_TAG = FtHttpXmlParser.class.getSimpleName();
    private static final String ns = null;
    private FtHttpFileInfo mFtHttpFileInfo;
    private FtHttpResumeInfo mFtHttpResumeInfo;

    public static FtHttpFileInfo parse(String input) throws XmlPullParserException, IOException {
        return new FtHttpXmlParser().parseFromString(input);
    }

    public static FtHttpResumeInfo parseResume(String input) throws XmlPullParserException, IOException {
        return new FtHttpXmlParser().parseResumeFromString(input);
    }

    private FtHttpFileInfo parseFromString(String input) throws XmlPullParserException, IOException {
        try {
            this.mFtHttpFileInfo = new FtHttpFileInfo();
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            parser.setInput(new StringReader(input));
            parser.nextTag();
            readFile(parser);
            String str = LOG_TAG;
            Log.i(str, "Parsing result: " + this.mFtHttpFileInfo);
        } catch (NullPointerException e) {
            e.printStackTrace();
            this.mFtHttpFileInfo = null;
        }
        return this.mFtHttpFileInfo;
    }

    private FtHttpResumeInfo parseResumeFromString(String input) throws XmlPullParserException, IOException {
        String str = LOG_TAG;
        Log.i(str, "Parse: " + input);
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
        parser.setInput(new StringReader(input));
        parser.nextTag();
        this.mFtHttpResumeInfo = readFileResumeInfo(parser);
        String str2 = LOG_TAG;
        Log.i(str2, "Parsing result: " + this.mFtHttpResumeInfo);
        return this.mFtHttpResumeInfo;
    }

    private void readFile(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "file");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if ("file-info".equals(parser.getName())) {
                    String type = parser.getAttributeValue(ns, "type");
                    if ("thumbnail".equals(type)) {
                        readThumbnailInfo(parser);
                    } else if ("file".equals(type)) {
                        readFileInfo(parser);
                    }
                } else {
                    skip(parser);
                }
            }
        }
    }

    private void readThumbnailInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "file-info");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if ("file-size".equals(name)) {
                    this.mFtHttpFileInfo.setThumbnailFileSize(readFileSize(parser));
                } else if ("content-type".equals(name)) {
                    this.mFtHttpFileInfo.setThumbnailContentType(readContentType(parser));
                } else if ("data".equals(name)) {
                    this.mFtHttpFileInfo.setThumbnailData(readData(parser));
                } else {
                    skip(parser);
                }
            }
        }
    }

    private void readFileInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "file-info");
        this.mFtHttpFileInfo.setFileDisposition(parser.getAttributeValue(ns, "file-disposition"));
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if ("file-size".equals(name)) {
                    this.mFtHttpFileInfo.setFileSize(readFileSize(parser));
                } else if ("file-name".equals(name)) {
                    this.mFtHttpFileInfo.setFileName(readFileName(parser));
                } else if ("content-type".equals(name)) {
                    this.mFtHttpFileInfo.setContentType(readContentType(parser));
                } else if ("data".equals(name)) {
                    this.mFtHttpFileInfo.setData(readData(parser));
                } else if ("e:branded-url".equals(name)) {
                    this.mFtHttpFileInfo.setBrandedUrl(readBrandedUrl(parser));
                } else if ("am:playing-length".equals(name)) {
                    this.mFtHttpFileInfo.setPlayingLength(readPlayingLength(parser));
                } else {
                    skip(parser);
                }
            }
        }
        parser.require(3, ns, "file-info");
    }

    private long readFileSize(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "file-size");
        long fileSize = Long.valueOf(readText(parser)).longValue();
        parser.require(3, ns, "file-size");
        return fileSize;
    }

    private String readFileName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "file-name");
        String fileName = readText(parser);
        parser.require(3, ns, "file-name");
        return fileName;
    }

    private String readContentType(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "content-type");
        String contentType = readText(parser);
        parser.require(3, ns, "content-type");
        return contentType;
    }

    private FtHttpFileInfo.Data readData(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "data");
        String url = "";
        String until = "";
        if ("data".equals(parser.getName())) {
            url = parser.getAttributeValue((String) null, ImsConstants.FtDlParams.FT_DL_URL);
            until = parser.getAttributeValue((String) null, "until");
            parser.nextTag();
        }
        parser.require(3, ns, "data");
        return new FtHttpFileInfo.Data(new URL(url), until);
    }

    private String readBrandedUrl(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "e:branded-url");
        String brandedUrl = readText(parser);
        parser.require(3, ns, "e:branded-url");
        return brandedUrl;
    }

    private FtHttpResumeInfo readFileResumeInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "file-resume-info");
        String url = "";
        long start = 0;
        long end = 0;
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if ("file-range".equals(name)) {
                    String str = parser.getAttributeValue(ns, "start");
                    if (str != null) {
                        start = Long.valueOf(str).longValue();
                    }
                    String str2 = parser.getAttributeValue(ns, "end");
                    if (str2 != null) {
                        end = Long.valueOf(str2).longValue();
                    }
                    parser.nextTag();
                } else if ("data".equals(name)) {
                    String str3 = parser.getAttributeValue(ns, ImsConstants.FtDlParams.FT_DL_URL);
                    if (str3 != null) {
                        url = str3;
                    }
                    parser.nextTag();
                } else {
                    skip(parser);
                }
            }
        }
        parser.require(3, ns, "file-resume-info");
        return new FtHttpResumeInfo(start, end, new URL(url));
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.next() != 4) {
            return "";
        }
        String result = parser.getText();
        parser.nextTag();
        return result;
    }

    private int readPlayingLength(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, ns, "am:playing-length");
        int playingLength = Integer.parseInt(readText(parser));
        parser.require(3, ns, "am:playing-length");
        return playingLength;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == 2) {
            int depth = 1;
            while (depth != 0) {
                int next = parser.next();
                if (next == 2) {
                    depth++;
                } else if (next == 3) {
                    depth--;
                }
            }
            return;
        }
        throw new IllegalStateException();
    }
}
