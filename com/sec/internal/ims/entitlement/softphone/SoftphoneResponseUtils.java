package com.sec.internal.ims.entitlement.softphone;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.log.IMSLog;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class SoftphoneResponseUtils {
    private static final String LOG_TAG = SoftphoneResponseUtils.class.getSimpleName();
    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        public XmlPullParser createParser() {
            try {
                return (XmlPullParser) Class.forName("android.util.Xml").getMethod("newPullParser", new Class[0]).invoke((Object) null, new Object[0]);
            } catch (Exception e) {
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
                    return parser;
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
    };

    private SoftphoneResponseUtils() {
    }

    public static <T> T parseJsonResponse(String json, Class<T> genericType) {
        if (json == null) {
            return null;
        }
        JsonParser parser = new JsonParser();
        try {
            return new Gson().fromJson(parser.parse(json), genericType);
        } catch (JsonSyntaxException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "cannot parse result" + e.getMessage());
            return null;
        }
    }

    static GsonXml createGsonXml(boolean namespaces) {
        return new GsonXmlBuilder().setXmlParserCreator(PARSER_CREATOR).setTreatNamespaces(namespaces).setSameNameLists(true).create();
    }

    public static <T> T parseXmlResponse(String xml, Class<T> genericType, boolean namespaces) {
        if (xml == null) {
            return null;
        }
        try {
            return createGsonXml(namespaces).fromXml(xml, genericType);
        } catch (Exception e) {
            String str = LOG_TAG;
            IMSLog.s(str, "cannot parse result" + e.getMessage());
            return null;
        }
    }

    public static <T> T parseJsonResponse(HttpResponseParams httpResponse, Class<T> genericType, int successCode) {
        T response = null;
        if (httpResponse == null) {
            try {
                response = genericType.newInstance();
                genericType.getField("mSuccess").setBoolean(response, false);
                genericType.getField("mReason").set(response, "Null response");
                genericType.getField("mStatusCode").setInt(response, 0);
                return response;
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
                String str = LOG_TAG;
                IMSLog.s(str, "cannot parse result" + e.getMessage());
                return response;
            }
        } else if (httpResponse.getStatusCode() == successCode) {
            T response2 = parseJsonResponse(httpResponse.getDataString(), genericType);
            String str2 = LOG_TAG;
            IMSLog.i(str2, "parseJsonResponse(): parsed response: " + response2);
            if (response2 == null) {
                try {
                    response2 = genericType.newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e2) {
                    String str3 = LOG_TAG;
                    IMSLog.s(str3, "cannot parse result" + e2.getMessage());
                    return response2;
                }
            }
            genericType.getField("mSuccess").setBoolean(response2, true);
            return response2;
        } else {
            try {
                response = genericType.newInstance();
                genericType.getField("mSuccess").setBoolean(response, false);
                genericType.getField("mReason").set(response, getErrorString(httpResponse));
                genericType.getField("mStatusCode").setInt(response, httpResponse.getStatusCode());
                return response;
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e3) {
                String str4 = LOG_TAG;
                IMSLog.s(str4, "cannot parse result" + e3.getMessage());
                return response;
            }
        }
    }

    public static <T> T parseXmlResponse(HttpResponseParams httpResponse, Class<T> genericType, int successCode, boolean namespaces) {
        T response = null;
        if (httpResponse == null) {
            try {
                response = genericType.newInstance();
                genericType.getField("mSuccess").setBoolean(response, false);
                genericType.getField("mReason").set(response, "Null response");
                genericType.getField("mStatusCode").setInt(response, 0);
                return response;
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
                String str = LOG_TAG;
                IMSLog.s(str, "cannot parse result" + e.getMessage());
                return response;
            }
        } else if (httpResponse.getStatusCode() == successCode) {
            T response2 = parseXmlResponse(httpResponse.getDataString(), genericType, namespaces);
            if (response2 == null) {
                try {
                    response2 = genericType.newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e2) {
                    String str2 = LOG_TAG;
                    IMSLog.s(str2, "cannot parse result" + e2.getMessage());
                    return response2;
                }
            }
            genericType.getField("mSuccess").setBoolean(response2, true);
            return response2;
        } else {
            try {
                response = genericType.newInstance();
                genericType.getField("mSuccess").setBoolean(response, false);
                genericType.getField("mReason").set(response, getErrorString(httpResponse));
                genericType.getField("mStatusCode").setInt(response, httpResponse.getStatusCode());
                return response;
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e3) {
                String str3 = LOG_TAG;
                IMSLog.s(str3, "cannot parse result" + e3.getMessage());
                return response;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00f9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getErrorString(com.sec.internal.helper.httpclient.HttpResponseParams r6) {
        /*
            int r0 = r6.getStatusCode()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "HTTP Response Code: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r1, r2)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "error"
            r1.append(r2)
            java.lang.String r2 = ":"
            r1.append(r2)
            java.lang.String r3 = java.lang.Integer.toString(r0)
            r1.append(r3)
            java.lang.StringBuilder r1 = r1.append(r2)
            r3 = -1
            if (r0 != r3) goto L_0x0041
            java.lang.String r2 = "Unable to get response"
            r1.append(r2)
            java.lang.String r2 = r1.toString()
            return r2
        L_0x0041:
            r3 = 0
            r4 = 200(0xc8, float:2.8E-43)
            if (r0 == r4) goto L_0x00c0
            r4 = 408(0x198, float:5.72E-43)
            if (r0 == r4) goto L_0x00ba
            r4 = 411(0x19b, float:5.76E-43)
            if (r0 == r4) goto L_0x00b4
            r4 = 414(0x19e, float:5.8E-43)
            if (r0 == r4) goto L_0x00ae
            r4 = 500(0x1f4, float:7.0E-43)
            if (r0 == r4) goto L_0x00a8
            r4 = 400(0x190, float:5.6E-43)
            if (r0 == r4) goto L_0x00c0
            r4 = 401(0x191, float:5.62E-43)
            if (r0 == r4) goto L_0x0090
            switch(r0) {
                case 403: goto L_0x008a;
                case 404: goto L_0x0084;
                case 405: goto L_0x007e;
                default: goto L_0x0061;
            }
        L_0x0061:
            switch(r0) {
                case 502: goto L_0x0078;
                case 503: goto L_0x0072;
                case 504: goto L_0x006b;
                default: goto L_0x0064;
            }
        L_0x0064:
            java.lang.String r4 = "Unexpected response status."
            r1.append(r4)
            goto L_0x00d7
        L_0x006b:
            java.lang.String r4 = "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI or some other auxiliary server it needed to access in attempting to complete the request."
            r1.append(r4)
            goto L_0x00d7
        L_0x0072:
            java.lang.String r4 = "The server is currently unable to receive requests; please retry."
            r1.append(r4)
            goto L_0x00d7
        L_0x0078:
            java.lang.String r4 = "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request."
            r1.append(r4)
            goto L_0x00d7
        L_0x007e:
            java.lang.String r4 = "A request was made of a resource using a request method not supported by that resource."
            r1.append(r4)
            goto L_0x00d7
        L_0x0084:
            java.lang.String r4 = "The server has not found anything matching the Request-URI."
            r1.append(r4)
            goto L_0x00d7
        L_0x008a:
            java.lang.String r4 = "Access permission error."
            r1.append(r4)
            goto L_0x00d7
        L_0x0090:
            java.lang.String r4 = r6.getDataString()
            java.lang.Class<com.sec.internal.ims.entitlement.softphone.responses.PolicyExceptionResponse> r5 = com.sec.internal.ims.entitlement.softphone.responses.PolicyExceptionResponse.class
            java.lang.Object r4 = parseJsonResponse(r4, r5)
            com.sec.internal.ims.entitlement.softphone.responses.PolicyExceptionResponse r4 = (com.sec.internal.ims.entitlement.softphone.responses.PolicyExceptionResponse) r4
            if (r4 == 0) goto L_0x00d7
            com.sec.internal.ims.entitlement.softphone.responses.PolicyExceptionResponse$RequestError r5 = r4.mRequestError
            if (r5 == 0) goto L_0x00d7
            com.sec.internal.ims.entitlement.softphone.responses.PolicyExceptionResponse$RequestError r5 = r4.mRequestError
            com.sec.internal.ims.entitlement.softphone.responses.ExceptionResponse r3 = r5.mException
            goto L_0x00d7
        L_0x00a8:
            java.lang.String r4 = "The server encountered an internal error or timed out; please retry."
            r1.append(r4)
            goto L_0x00d7
        L_0x00ae:
            java.lang.String r4 = "The Request-URI is longer than the server is willing to interpret."
            r1.append(r4)
            goto L_0x00d7
        L_0x00b4:
            java.lang.String r4 = "The Content-Length header was not specified."
            r1.append(r4)
            goto L_0x00d7
        L_0x00ba:
            java.lang.String r4 = "The client did not produce a request within the time that the server was prepared to wait."
            r1.append(r4)
            goto L_0x00d7
        L_0x00c0:
            java.lang.String r4 = r6.getDataString()
            java.lang.Class<com.sec.internal.ims.entitlement.softphone.responses.ServiceExceptionResponse> r5 = com.sec.internal.ims.entitlement.softphone.responses.ServiceExceptionResponse.class
            java.lang.Object r4 = parseJsonResponse(r4, r5)
            com.sec.internal.ims.entitlement.softphone.responses.ServiceExceptionResponse r4 = (com.sec.internal.ims.entitlement.softphone.responses.ServiceExceptionResponse) r4
            if (r4 == 0) goto L_0x00d7
            com.sec.internal.ims.entitlement.softphone.responses.ServiceExceptionResponse$RequestError r5 = r4.mRequestError
            if (r5 == 0) goto L_0x00d7
            com.sec.internal.ims.entitlement.softphone.responses.ServiceExceptionResponse$RequestError r5 = r4.mRequestError
            com.sec.internal.ims.entitlement.softphone.responses.ExceptionResponse r3 = r5.mException
        L_0x00d7:
            if (r3 == 0) goto L_0x00f9
            java.lang.String r4 = r3.mMessageId
            r1.append(r4)
            r1.append(r2)
            java.lang.String r2 = r3.mText
            r1.append(r2)
            java.lang.String r2 = r3.mVariables
            if (r2 == 0) goto L_0x00ef
            java.lang.String r2 = r3.mVariables
            r1.append(r2)
        L_0x00ef:
            java.lang.String r2 = r3.mValues
            if (r2 == 0) goto L_0x0111
            java.lang.String r2 = r3.mValues
            r1.append(r2)
            goto L_0x0111
        L_0x00f9:
            java.lang.String r2 = r6.getDataString()
            java.lang.Class<com.sec.internal.ims.entitlement.softphone.responses.GeneralErrorResponse> r4 = com.sec.internal.ims.entitlement.softphone.responses.GeneralErrorResponse.class
            java.lang.Object r2 = parseJsonResponse(r2, r4)
            com.sec.internal.ims.entitlement.softphone.responses.GeneralErrorResponse r2 = (com.sec.internal.ims.entitlement.softphone.responses.GeneralErrorResponse) r2
            if (r2 == 0) goto L_0x0111
            java.lang.String r4 = r2.mError
            if (r4 == 0) goto L_0x0111
            java.lang.String r4 = r2.mError
            r1.append(r4)
        L_0x0111:
            java.lang.String r2 = r1.toString()
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.softphone.SoftphoneResponseUtils.getErrorString(com.sec.internal.helper.httpclient.HttpResponseParams):java.lang.String");
    }
}
