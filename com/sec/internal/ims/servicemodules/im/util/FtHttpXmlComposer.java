package com.sec.internal.ims.servicemodules.im.util;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.ims.servicemodules.im.interfaces.IFtHttpXmlComposer;

public class FtHttpXmlComposer implements IFtHttpXmlComposer {
    private static final String LOG_TAG = FtHttpXmlComposer.class.getSimpleName();

    public String composeXmlForAudioMessage(FtHttpFileInfo fileInfo, int playingLength) {
        Log.i(LOG_TAG, "buildXMLForAudioMessage");
        StringBuilder sb = new StringBuilder();
        appendXmlVersionAndEncoding(sb);
        sb.append("<file");
        appendFtHttpNamespace(sb);
        appendAudioMessagingNamespace(sb);
        if (!TextUtils.isEmpty(fileInfo.getBrandedUrl())) {
            appendFtHttpExtNamespace(sb);
        }
        sb.append(">\n");
        if (fileInfo.isThumbnailExist()) {
            appendThumbnail(sb, fileInfo);
        }
        sb.append("\t<file-info type=\"file\" file-disposition=\"render\">\n");
        appendFileSize(sb, fileInfo.getFileSize());
        appendFileName(sb, fileInfo.getFileName());
        appendContentType(sb, fileInfo.getContentType());
        appendPlayingLength(sb, playingLength);
        appendDataUrlAndUntil(sb, fileInfo.getDataUrl().toString(), fileInfo.getDataUntil());
        if (!TextUtils.isEmpty(fileInfo.getBrandedUrl())) {
            appendBrandedUrl(sb, fileInfo.getBrandedUrl());
        }
        sb.append("\t</file-info>\n");
        sb.append("</file>\n");
        return sb.toString();
    }

    private void appendXmlVersionAndEncoding(StringBuilder sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    }

    private void appendFtHttpNamespace(StringBuilder sb) {
        sb.append(" xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:fthttp\"");
    }

    private void appendFtHttpExtNamespace(StringBuilder sb) {
        sb.append(" xmlns:e=\"urn:gsma:params:xml:ns:rcs:rcs:up:fthttpext\"");
    }

    private void appendAudioMessagingNamespace(StringBuilder sb) {
        sb.append(" xmlns:am=\"urn:gsma:params:xml:ns:rcs:rcs:rram\"");
    }

    private void appendThumbnail(StringBuilder sb, FtHttpFileInfo fileInfo) {
        sb.append("\t<file-info type=\"thumbnail\">\n");
        sb.append("\t\t<file-size>");
        sb.append(fileInfo.getThumbnailFileSize());
        sb.append("</file-size>\n");
        sb.append("\t\t<content-type>");
        sb.append(fileInfo.getThumbnailContentType());
        sb.append("</content-type>\n");
        sb.append("\t\t<data url=\"");
        sb.append(fileInfo.getThumbnailDataUrl().toString().replace("&", "&amp;"));
        sb.append("\" until=\"");
        sb.append(fileInfo.getThumbnailDataUntil());
        sb.append("\"/>\n");
        sb.append("\t</file-info>\n");
    }

    private void appendFileSize(StringBuilder sb, long fileSize) {
        sb.append("\t\t<file-size>");
        sb.append(fileSize);
        sb.append("</file-size>\n");
    }

    private void appendFileName(StringBuilder sb, String fileName) {
        sb.append("\t\t<file-name>");
        sb.append(fileName);
        sb.append("</file-name>\n");
    }

    private void appendContentType(StringBuilder sb, String contentType) {
        sb.append("\t\t<content-type>");
        sb.append(contentType);
        sb.append("</content-type>\n");
    }

    private void appendPlayingLength(StringBuilder sb, int playingLength) {
        sb.append("\t\t<am:playing-length>");
        sb.append(playingLength);
        sb.append("</am:playing-length>\n");
    }

    private void appendDataUrlAndUntil(StringBuilder sb, String url, String until) {
        sb.append("\t\t<data url=\"");
        sb.append(url.replace("&", "&amp;"));
        sb.append("\" until=\"");
        sb.append(until);
        sb.append("\"/>\n");
    }

    private void appendBrandedUrl(StringBuilder sb, String brandedUrl) {
        sb.append("\t\t<e:branded-url>");
        sb.append(brandedUrl.replace("&", "&amp;"));
        sb.append("</e:branded-url>\n");
    }
}
