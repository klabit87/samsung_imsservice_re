package com.sec.internal.ims.gba;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.gba.IGbaEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.header.AuthorizationHeader;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.DigestAuth;
import com.sec.internal.helper.httpclient.DnsController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.helper.parser.WwwAuthHeaderParser;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.params.GbaData;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.ims.util.httpclient.GbaHttpController;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.gba.IGbaCallback;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class GbaServiceModule extends Handler implements IGbaServiceModule {
    private static final String GBA_ME = "gba-me";
    public static final String GBA_UICC = "gba-u";
    private static final String IMS_AUTH_NO_ERR_STRING = "db";
    private static final String IMS_AUTH_SYNC_FAIL = "dc";
    private static final String LOG_TAG = GbaServiceModule.class.getSimpleName();
    private static byte[] gbaKey = null;
    private Context mContext;
    private Gba mGba;
    private List<IGbaCallback> mGbaCallbacks = new ArrayList();
    private final Map<Integer, RemoteCallbackList<IGbaEventListener>> mGbaEventListeners = new ConcurrentHashMap();
    private IImsFramework mImsFramework;
    private HashMap<String, LastAuthInfo> mLastDigestInfoMap = null;
    private ITelephonyManager mTelephonyManager;
    private int resLen = 0;

    private static class LastAuthInfo {
        public DigestAuth digestAuth;
        public WwwAuthenticateHeader wwwHeader;

        private LastAuthInfo() {
            this.digestAuth = null;
            this.wwwHeader = null;
        }
    }

    public GbaServiceModule(Looper looper, Context context, IImsFramework framework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = framework;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        initGbaAccessibleObj();
    }

    public List<IGbaCallback> getGbaCallbacks() {
        return this.mGbaCallbacks;
    }

    public void storeGbaBootstrapParams(byte[] rand, String btid, String keyLifetime) {
        if (this.mTelephonyManager != null) {
            String str = LOG_TAG;
            Log.i(str, "rand :" + StrUtil.bytesToHexString(rand) + " btid :" + btid + " keyLifetime :" + keyLifetime);
            this.mTelephonyManager.setGbaBootstrappingParams(Arrays.copyOfRange(rand, 1, 17), btid, keyLifetime);
        }
    }

    public String transmitLogicChannel(int subId, String aid, String ChalData, int ChalDataLen) throws RemoteException {
        int i = subId;
        int channel = this.mTelephonyManager.iccOpenLogicalChannelAndGetChannel(subId, aid);
        String response = this.mTelephonyManager.iccTransmitApduLogicalChannel(subId, channel, 2, 136, 0, 132, ChalDataLen, ChalData);
        this.mTelephonyManager.iccCloseLogicalChannel(subId, channel);
        return response;
    }

    public String getNafExternalKeyBase64Decoded(int subId, byte[] gbaType, byte[] nafId) {
        if (this.mTelephonyManager == null) {
            return null;
        }
        String str = LOG_TAG;
        Log.d(str, "getNafExternalKeyBase64Decoded gbaType " + StrUtil.bytesToHexString(gbaType) + " nafId :" + StrUtil.bytesToHexString(nafId));
        String base64response = null;
        String response = getGbaKeyResponse(subId, StrUtil.bytesToHexString(nafId));
        if (response != null) {
            try {
                gbaKey = Hex.decodeHex(response.toCharArray());
            } catch (DecoderException e) {
                String str2 = LOG_TAG;
                Log.d(str2, "DecoderException thrown " + e);
            }
        }
        if (gbaKey != null) {
            String str3 = LOG_TAG;
            Log.d(str3, "getNafExternalKeyBase64Decoded string : " + StrUtil.bytesToHexString(gbaKey));
            base64response = parseResKeyFromIsimResponse(gbaKey);
        }
        Gba gba = this.mGba;
        if (gba != null) {
            gba.storeGbaKey(gbaType, nafId, gbaKey, getKeyLifetime(), getBtidFromSim());
        }
        return base64response;
    }

    public String parseResKeyFromIsimResponse(byte[] result) {
        String response = StrUtil.bytesToHexString(result);
        if (response == null) {
            return null;
        }
        String str = LOG_TAG;
        Log.i(str, "AkaResponse for GBA as received from sim: " + response);
        if (("" + response.charAt(0) + response.charAt(1)).equalsIgnoreCase(IMS_AUTH_NO_ERR_STRING)) {
            int parseInt = Integer.parseInt(response.substring(2, 4), 16);
            this.resLen = parseInt;
            if (parseInt <= 0) {
                throw new IllegalArgumentException("Illegal response recieved from iSim");
            }
        }
        int i = this.resLen;
        byte[] responsekey = new byte[i];
        System.arraycopy(result, 2, responsekey, 0, i);
        String passwordStr = Base64.encodeToString(responsekey, 2);
        String str2 = LOG_TAG;
        Log.i(str2, "AkaResponse for GBA to be sent: " + StrUtil.bytesToHexString(responsekey) + " base64 decode : " + passwordStr);
        return passwordStr;
    }

    public String getBtidFromSim() {
        String btid = this.mTelephonyManager.getBtid();
        String str = LOG_TAG;
        Log.d(str, "getBtid " + btid);
        return btid;
    }

    public String getKeyLifetime() {
        return this.mTelephonyManager.getKeyLifetime();
    }

    public String getImpi(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        String impi = "";
        if (sm == null) {
            return impi;
        }
        if (sm.hasIsim()) {
            impi = sm.getImpi();
        }
        if (TextUtils.isEmpty(impi)) {
            return sm.getDerivedImpi();
        }
        return impi;
    }

    public String getImei(int phoneId) {
        ITelephonyManager iTelephonyManager = this.mTelephonyManager;
        if (iTelephonyManager == null) {
            return null;
        }
        return iTelephonyManager.getDeviceId(phoneId);
    }

    public boolean initGbaAccessibleObj() {
        this.mGba = new Gba();
        this.mLastDigestInfoMap = new HashMap<>();
        return true;
    }

    public boolean isGbaUiccSupported(int subId) {
        return this.mTelephonyManager.isGbaSupported(subId);
    }

    public void getBtidAndGbaKey(HttpRequestParams requestParams, String fqdn, HttpResponseParams result, IGbaCallback callback) {
        byte[] gbaType;
        boolean isGbaU;
        String imDomain;
        String realm;
        GbaValue gbaValue;
        HttpResponseParams httpResponseParams = result;
        IGbaCallback iGbaCallback = callback;
        if (iGbaCallback != null) {
            int phoneId = requestParams.getPhoneId();
            int subId = SimUtil.getSubId(phoneId);
            byte[] nafId = GbaUtility.getNafId(fqdn);
            ITelephonyManager iTelephonyManager = this.mTelephonyManager;
            if (iTelephonyManager == null) {
                iGbaCallback.onComplete((String) null, (String) null, false, httpResponseParams);
                this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
                return;
            }
            boolean isGbaU2 = fqdn.contains("uicc") && iTelephonyManager.isGbaSupported(subId);
            if (isGbaU2) {
                gbaType = GBA_UICC.getBytes(StandardCharsets.UTF_8);
            } else {
                gbaType = GBA_ME.getBytes(StandardCharsets.UTF_8);
            }
            if (nafId == null) {
                isGbaU = isGbaU2;
            } else if (gbaType == null) {
                byte[] bArr = gbaType;
                isGbaU = isGbaU2;
            } else {
                if (requestParams.getIpVersion() != 3) {
                    GbaValue gbaValue2 = this.mGba.getGbaValue(nafId, gbaType, phoneId);
                    if (gbaValue2 != null && (httpResponseParams == null || result.getDataString() == null || (!result.getDataString().contains("B-TID") && !result.getDataString().contains("Expired")))) {
                        iGbaCallback.onComplete(gbaValue2.getBtid(), StrUtil.convertHexToString(StrUtil.bytesToHexString(gbaValue2.getValue())), isGbaU2, httpResponseParams);
                        return;
                    }
                } else if (((DnsController) requestParams.getDns()).getRetryCounter() == 0 && (gbaValue = this.mGba.getGbaValue(nafId, gbaType, phoneId)) != null && (httpResponseParams == null || result.getDataString() == null || (!result.getDataString().contains("B-TID") && !result.getDataString().contains("Expired")))) {
                    iGbaCallback.onComplete(gbaValue.getBtid(), StrUtil.convertHexToString(StrUtil.bytesToHexString(gbaValue.getValue())), isGbaU2, httpResponseParams);
                    return;
                }
                Log.i(LOG_TAG, "GBA: NO GBA information, need send BSF request");
                this.mGbaCallbacks.add(iGbaCallback);
                String bsfServer = requestParams.getBsfUrl();
                int bsfPort = this.mImsFramework.getInt(phoneId, GlobalSettingsConstants.SS.BSF_PORT, 80);
                if (bsfServer == null) {
                    boolean z = isGbaU2;
                } else if (bsfPort < 0) {
                    byte[] bArr2 = gbaType;
                    boolean z2 = isGbaU2;
                } else {
                    String imei = this.mTelephonyManager.getDeviceId(phoneId);
                    String imDomain2 = this.mTelephonyManager.getIsimDomain(subId);
                    String realm2 = this.mTelephonyManager.getIsimDomain(subId);
                    String impi = getImpi(phoneId);
                    ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                    boolean hasIsim = sm == null ? false : sm.hasIsim();
                    if ((TextUtils.isEmpty(imDomain2) || !hasIsim) && !TextUtils.isEmpty(impi)) {
                        imDomain = impi.split("@", 2)[1];
                    } else {
                        imDomain = imDomain2;
                    }
                    if (isRealmFromUsername(phoneId)) {
                        realm = imDomain;
                    } else if (TextUtils.isEmpty(realm2) || !realm2.startsWith("bsf")) {
                        realm = bsfServer;
                    } else {
                        realm = realm2;
                    }
                    if (httpResponseParams == null || TextUtils.isEmpty(result.getCipherSuite()) || !requestParams.getUseTls()) {
                        HttpRequestParams httpRequestParams = requestParams;
                    } else {
                        requestParams.setCipherSuite(result.getCipherSuite());
                    }
                    String str = impi;
                    byte[] bArr3 = gbaType;
                    boolean z3 = isGbaU2;
                    GbaHttpController.getInstance().sendBsfRequest(bsfServer, bsfPort, impi, imei, realm, gbaType, nafId, isGbaU2, requestParams);
                    return;
                }
                iGbaCallback.onComplete((String) null, (String) null, false, httpResponseParams);
                return;
            }
            iGbaCallback.onComplete((String) null, (String) null, isGbaU, httpResponseParams);
        }
    }

    public void getAuthorizationHeader(int phoneId, String requestUri, String wwwAuthenticateHeader, String cipherSuite) {
        final int i = phoneId;
        final String str = requestUri;
        final String str2 = wwwAuthenticateHeader;
        if (TextUtils.isEmpty(wwwAuthenticateHeader)) {
            String str3 = LOG_TAG;
            Log.i(str3, "Request from Msgp app without 401 URI :" + str);
            byte[] nafId = GbaUtility.getNafUrl(requestUri).getBytes(StandardCharsets.UTF_8);
            byte[] gbaType = GBA_ME.getBytes(StandardCharsets.UTF_8);
            GbaValue gbaValue = this.mGba.getGbaValue(nafId, gbaType, i);
            if (gbaValue == null) {
                notifyOnGbaError(i, "Required NAF Again");
                return;
            }
            LastAuthInfo authInfo = getAuthInfo(phoneId, requestUri);
            DigestAuth digestAuth = authInfo.digestAuth;
            WwwAuthenticateHeader wwwAuthHeader = authInfo.wwwHeader;
            String[] qop = wwwAuthHeader.getQop().split(",");
            byte[] bArr = nafId;
            byte[] bArr2 = gbaType;
            DigestAuth digestAuth2 = digestAuth;
            digestAuth.setDigestAuth(gbaValue.getBtid(), StrUtil.convertHexToString(StrUtil.bytesToHexString(gbaValue.getValue())), wwwAuthHeader.getRealm(), wwwAuthHeader.getNonce(), "GET", GbaUtility.getNafPath(requestUri), wwwAuthHeader.getAlgorithm(), qop[0]);
            String authHeader = AuthorizationHeader.getAuthorizationHeader(digestAuth2, wwwAuthHeader);
            authInfo.digestAuth = digestAuth2;
            setAuthInfo(i, str, authInfo);
            notifyOnGbaEvent(i, authHeader);
            String str4 = cipherSuite;
            return;
        }
        String str5 = LOG_TAG;
        Log.i(str5, "Request from Msg app with 401 Auth header URI : " + str);
        String realm = new WwwAuthHeaderParser().parseHeaderValue(str2).getRealm();
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        Network network = connectivityManager.getActiveNetwork();
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        if (realm.contains("3GPP-bootstrapping")) {
            HttpRequestParams requestParams = new HttpRequestParams();
            requestParams.setPhoneId(i);
            requestParams.setMethod(HttpRequestParams.Method.GET);
            requestParams.setUrl(str);
            requestParams.setConnectionTimeout(5000);
            DnsController dnsController = r8;
            Network network2 = network;
            DnsController dnsController2 = new DnsController(0, 0, network, linkProperties.getDnsServers(), 5, false, SimUtil.getSimMno(phoneId));
            HttpRequestParams requestParams2 = requestParams;
            requestParams2.setDns(dnsController);
            requestParams2.setBsfUrl(UtUtils.getBSFDomain(this.mContext, i));
            if (!TextUtils.isEmpty(cipherSuite)) {
                requestParams2.setCipherSuite(cipherSuite);
            } else {
                String str6 = cipherSuite;
            }
            resetAuthInfo(phoneId, requestUri);
            resetGbaKey(realm, requestParams2.getPhoneId());
            getBtidAndGbaKey(requestParams2, realm, (HttpResponseParams) null, new IGbaCallback() {
                public void onComplete(String btid, String gbaKey, boolean gbaUicc, HttpResponseParams gbaResult) {
                    GbaServiceModule.this.responseWwwHeader(i, str, btid, gbaKey, gbaUicc, str2);
                }

                public void onFail(IOException arg1) {
                    GbaServiceModule.this.notifyOnGbaError(i, "Error");
                }
            });
            return;
        }
        String str7 = cipherSuite;
        Network network3 = network;
    }

    /* access modifiers changed from: private */
    public void responseWwwHeader(int phoneId, String requestUri, String btId, String gbaKey2, boolean isGbaUiccSupported, String wwwAuthHeader) {
        int i = phoneId;
        String str = requestUri;
        URL nafUri = null;
        try {
            nafUri = new URL(str);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        WwwAuthHeaderParser wwwAuthHeaderParser = new WwwAuthHeaderParser();
        WwwAuthenticateHeader wwwAuthParsedHeader = wwwAuthHeaderParser.parseHeaderValue(wwwAuthHeader);
        String[] realms = wwwAuthParsedHeader.getRealm().split(";");
        String realm = "";
        String nafPath = "/";
        if (nafUri != null && !nafUri.getPath().isEmpty()) {
            nafPath = nafUri.getPath();
        }
        int length = realms.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            String val = realms[i2];
            if (!val.contains("uicc") || !isGbaUiccSupported) {
                if (!val.contains("uicc") && !isGbaUiccSupported) {
                    realm = val;
                    break;
                }
                i2++;
            } else {
                realm = val;
                break;
            }
        }
        String[] qop = wwwAuthParsedHeader.getQop().split(",");
        DigestAuth digestAuth = new DigestAuth();
        WwwAuthHeaderParser wwwAuthHeaderParser2 = wwwAuthHeaderParser;
        DigestAuth digestAuth2 = digestAuth;
        digestAuth.setDigestAuth(btId, gbaKey2, realm, wwwAuthParsedHeader.getNonce(), "GET", nafPath, wwwAuthParsedHeader.getAlgorithm(), qop[0]);
        String authHeader = AuthorizationHeader.getAuthorizationHeader(digestAuth2, wwwAuthParsedHeader);
        LastAuthInfo authInfo = new LastAuthInfo();
        authInfo.digestAuth = digestAuth2;
        authInfo.wwwHeader = wwwAuthParsedHeader;
        setAuthInfo(i, str, authInfo);
        notifyOnGbaEvent(i, authHeader);
    }

    public boolean isValidGbaKey(int phoneId, String nafUri) {
        if (TextUtils.isEmpty(nafUri)) {
            Log.d(LOG_TAG, "Invalid URI");
            return false;
        }
        if (this.mGba.getGbaValue(nafUri.getBytes(StandardCharsets.UTF_8), GBA_ME.getBytes(StandardCharsets.UTF_8), phoneId) == null) {
            return false;
        }
        return true;
    }

    public void registerGbaEventListener(int phoneId, IGbaEventListener listener) {
        String str = LOG_TAG;
        Log.d(str, "registerGbaEventListener to phone#" + phoneId);
        if (!this.mGbaEventListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mGbaEventListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mGbaEventListeners.get(Integer.valueOf(phoneId)).register(listener);
    }

    public void unregisterGbaEventListener(int phoneId, IGbaEventListener listener) {
        String str = LOG_TAG;
        Log.d(str, "unregisterGbaEventListener to phone#" + phoneId);
        if (this.mGbaEventListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mGbaEventListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public synchronized void notifyOnGbaEvent(int phoneId, String wwwHeader) {
        if (this.mGbaEventListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mGbaEventListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mGbaEventListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onGbaEventSuccess(wwwHeader);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify Gba event!", e);
                }
            }
            this.mGbaEventListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public synchronized void notifyOnGbaError(int phoneId, String error) {
        if (this.mGbaEventListeners.containsKey(Integer.valueOf(phoneId))) {
            int length = this.mGbaEventListeners.get(Integer.valueOf(phoneId)).beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mGbaEventListeners.get(Integer.valueOf(phoneId)).getBroadcastItem(i).onGbaEventFail(error);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "failed notify Gba event!", e);
                }
            }
            this.mGbaEventListeners.get(Integer.valueOf(phoneId)).finishBroadcast();
        }
    }

    public void resetGbaKey(String fqdn, int phoneId) {
        byte[] nafId;
        byte[] gbaType;
        String[] realms = fqdn.split("@");
        boolean isGbaU = true;
        if (realms[1].contains(";")) {
            nafId = realms[1].split(";")[0].getBytes(StandardCharsets.UTF_8);
        } else {
            nafId = realms[1].getBytes(StandardCharsets.UTF_8);
        }
        boolean isGbaSupported = this.mTelephonyManager.isGbaSupported(SimUtil.getSubId(phoneId));
        if (!fqdn.contains("uicc") || !isGbaSupported) {
            isGbaU = false;
        }
        if (isGbaU) {
            gbaType = GBA_UICC.getBytes(StandardCharsets.UTF_8);
        } else {
            gbaType = GBA_ME.getBytes(StandardCharsets.UTF_8);
        }
        this.mGba.removeGbaKey(nafId, gbaType, phoneId);
    }

    public String storeGbaDataAndGenerateKey(String btid, String lifetime, String nonce, String cipherSuite, byte[] gbaType, byte[] nafId, GbaData keys, boolean isTLS, int phoneId) {
        String gbaKey2 = generateGbaKey(gbaType, nafId, StrUtil.hexStringToBytes(splitRandAutn(nonce)[0]), btid, lifetime, cipherSuite, keys, isTLS, phoneId);
        String str = LOG_TAG;
        Log.i(str, "storeGbaDataAndGenerateKey(): base64 gbaKey: " + gbaKey2);
        return gbaKey2;
    }

    private static String[] splitRandAutn(String nonce) {
        String akaChallenge = StrUtil.bytesToHexString(Base64.decode(nonce.getBytes(), 2));
        Log.i(LOG_TAG, "Decoded AKA Challenge: " + akaChallenge + " length: " + akaChallenge.length());
        if (akaChallenge.length() < 64) {
            return new String[]{"", ""};
        }
        return new String[]{"10" + akaChallenge.substring(0, 32), "10" + akaChallenge.substring(32, 64)};
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0094  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String generateGbaKey(byte[] r21, byte[] r22, byte[] r23, java.lang.String r24, java.lang.String r25, java.lang.String r26, com.sec.internal.ims.gba.params.GbaData r27, boolean r28, int r29) {
        /*
            r20 = this;
            r1 = r20
            r12 = r21
            r13 = r22
            r14 = r23
            com.sec.internal.helper.os.ITelephonyManager r0 = r1.mTelephonyManager
            if (r0 != 0) goto L_0x000e
            r0 = 0
            return r0
        L_0x000e:
            java.lang.String r0 = new java.lang.String
            r0.<init>(r12)
            r15 = r0
            r0 = 0
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "generateGbaKey(): gbaType: "
            r3.append(r4)
            java.lang.String r4 = new java.lang.String
            r4.<init>(r12)
            r3.append(r4)
            java.lang.String r4 = " nafId: "
            r3.append(r4)
            java.lang.String r4 = new java.lang.String
            r4.<init>(r13)
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            r16 = 0
            java.lang.String r2 = "gba-u"
            boolean r2 = r2.equals(r15)
            if (r2 == 0) goto L_0x00c1
            r11 = r24
            r10 = r25
            r1.storeGbaBootstrapParams(r14, r11, r10)
            r2 = 0
            r3 = 0
            r4 = 0
            byte[] r9 = com.sec.internal.ims.gba.GbaUtility.convertCipherSuite(r26)
            r8 = r28
            byte[] r17 = com.sec.internal.ims.gba.GbaUtility.getSecurityProtocolId(r13, r9, r8)
            java.lang.String r7 = com.sec.internal.helper.StrUtil.bytesToHexString(r17)
            int r0 = com.sec.internal.helper.SimUtil.getSubId(r29)
            java.lang.String r6 = r1.getGbaKeyResponse(r0, r7)
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "generateGbaKey(): response: "
            r2.append(r4)
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
            if (r6 == 0) goto L_0x008a
            char[] r0 = r6.toCharArray()     // Catch:{ DecoderException -> 0x0089 }
            byte[] r0 = org.apache.commons.codec.binary.Hex.decodeHex(r0)     // Catch:{ DecoderException -> 0x0089 }
            r3 = r0
            goto L_0x008b
        L_0x0089:
            r0 = move-exception
        L_0x008a:
            r0 = r3
        L_0x008b:
            if (r0 == 0) goto L_0x0094
            java.lang.String r16 = r1.parseResKeyFromIsimResponse(r0)
            r5 = r16
            goto L_0x0096
        L_0x0094:
            r5 = r16
        L_0x0096:
            com.sec.internal.ims.gba.Gba r2 = r1.mGba
            if (r2 == 0) goto L_0x00b8
            if (r5 == 0) goto L_0x00b8
            java.nio.charset.Charset r3 = java.nio.charset.StandardCharsets.UTF_8
            byte[] r16 = r5.getBytes(r3)
            r3 = r21
            r4 = r22
            r18 = r5
            r5 = r16
            r16 = r6
            r6 = r25
            r19 = r7
            r7 = r24
            r8 = r29
            r2.storeGbaKey(r3, r4, r5, r6, r7, r8)
            goto L_0x00be
        L_0x00b8:
            r18 = r5
            r16 = r6
            r19 = r7
        L_0x00be:
            r5 = r18
            goto L_0x0119
        L_0x00c1:
            r11 = r24
            r10 = r25
            r2 = 1
            r3 = 17
            byte[] r17 = java.util.Arrays.copyOfRange(r14, r2, r3)
            r9 = r29
            java.lang.String r8 = r1.getImpi(r9)
            byte[] r0 = com.sec.internal.ims.gba.GbaUtility.convertCipherSuite(r26)
            java.lang.String r2 = r27.getCipkey()
            byte[] r3 = com.sec.internal.helper.StrUtil.hexStringToBytes(r2)
            java.lang.String r2 = r27.getIntkey()
            byte[] r4 = com.sec.internal.helper.StrUtil.hexStringToBytes(r2)
            java.nio.charset.Charset r2 = java.nio.charset.StandardCharsets.UTF_8
            byte[] r6 = r8.getBytes(r2)
            r2 = r21
            r5 = r17
            r7 = r22
            r18 = r8
            r8 = r25
            r9 = r24
            r10 = r28
            r11 = r0
            java.lang.String r9 = com.sec.internal.ims.gba.GbaUtility.igenerateGbaMEKey(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
            com.sec.internal.ims.gba.Gba r2 = r1.mGba
            if (r2 == 0) goto L_0x0117
            java.nio.charset.Charset r3 = java.nio.charset.StandardCharsets.UTF_8
            byte[] r5 = r9.getBytes(r3)
            r3 = r21
            r4 = r22
            r6 = r25
            r7 = r24
            r8 = r29
            r2.storeGbaKey(r3, r4, r5, r6, r7, r8)
        L_0x0117:
            r5 = r9
            r9 = r0
        L_0x0119:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaServiceModule.generateGbaKey(byte[], byte[], byte[], java.lang.String, java.lang.String, java.lang.String, com.sec.internal.ims.gba.params.GbaData, boolean, int):java.lang.String");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v1, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v4, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.gba.params.GbaData getPassword(java.lang.String r16, boolean r17, int r18) {
        /*
            r15 = this;
            java.lang.String[] r1 = splitRandAutn(r16)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r2 = 0
            r2 = r1[r2]
            r0.append(r2)
            r2 = 1
            r2 = r1[r2]
            r0.append(r2)
            java.lang.String r2 = r0.toString()
            r3 = 0
            if (r17 == 0) goto L_0x0027
            int r0 = com.sec.internal.helper.SimUtil.getSubId(r18)
            r4 = r15
            java.lang.String r0 = r15.getIsimResponse(r0, r2)
            r5 = r0
            goto L_0x0034
        L_0x0027:
            r4 = r15
            com.sec.internal.interfaces.ims.core.ISimManager r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r18)
            if (r0 != 0) goto L_0x0030
            r5 = r3
            goto L_0x0034
        L_0x0030:
            java.lang.String r5 = r0.getIsimAuthentication(r2)
        L_0x0034:
            if (r5 != 0) goto L_0x0037
            return r3
        L_0x0037:
            java.lang.String r0 = r5.toLowerCase()
            java.lang.String r6 = "dc"
            boolean r0 = r0.startsWith(r6)
            if (r0 == 0) goto L_0x004b
            com.sec.internal.ims.gba.params.GbaData r0 = new com.sec.internal.ims.gba.params.GbaData
            java.lang.String r3 = ""
            r0.<init>(r5, r3, r3)
            return r0
        L_0x004b:
            java.lang.String r0 = r5.toLowerCase()
            java.lang.String r6 = "db"
            boolean r0 = r0.startsWith(r6)
            if (r0 != 0) goto L_0x006e
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "getPassword(): wrong IsimResponse: "
            r6.append(r7)
            r6.append(r5)
            java.lang.String r6 = r6.toString()
            android.util.Log.e(r0, r6)
            return r3
        L_0x006e:
            r6 = 0
            byte[] r0 = com.sec.internal.helper.StrUtil.hexStringToBytes(r5)     // Catch:{ RuntimeException -> 0x0075 }
            r6 = r0
        L_0x0074:
            goto L_0x0077
        L_0x0075:
            r0 = move-exception
            goto L_0x0074
        L_0x0077:
            if (r6 != 0) goto L_0x007a
            return r3
        L_0x007a:
            r0 = 1
            int r3 = r0 + 1
            byte r0 = r6[r0]
            java.lang.String r7 = new java.lang.String
            int r8 = r3 + r0
            byte[] r8 = java.util.Arrays.copyOfRange(r6, r3, r8)
            java.lang.String r9 = "CP1252"
            java.nio.charset.Charset r9 = java.nio.charset.Charset.forName(r9)
            r7.<init>(r8, r9)
            int r3 = r3 + r0
            java.lang.String r8 = LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "getPassword(): password = "
            r9.append(r10)
            r9.append(r7)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r8, r9)
            r8 = 0
            r9 = 0
            if (r17 != 0) goto L_0x00cd
            int r10 = r3 + 1
            byte r3 = r6[r3]
            int r11 = r0 * 2
            int r11 = r11 + 4
            int r11 = r11 + 2
            int r12 = r3 * 2
            int r12 = r12 + r11
            java.lang.String r8 = r5.substring(r11, r12)
            int r10 = r10 + r3
            byte r13 = r6[r10]
            if (r13 >= 0) goto L_0x00c3
            int r13 = 256 - r13
        L_0x00c3:
            int r11 = r12 + 2
            int r14 = r13 * 2
            int r14 = r14 + r11
            java.lang.String r9 = r5.substring(r11, r14)
            r3 = r10
        L_0x00cd:
            com.sec.internal.ims.gba.params.GbaData r10 = new com.sec.internal.ims.gba.params.GbaData
            r10.<init>(r7, r8, r9)
            r11 = r18
            r10.setPhoneId(r11)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaServiceModule.getPassword(java.lang.String, boolean, int):com.sec.internal.ims.gba.params.GbaData");
    }

    private String getGbaKeyResponse(int subId, String challenge) {
        if (this.mTelephonyManager == null) {
            return null;
        }
        String chalData = "DE" + Integer.toHexString(challenge.length() / 2) + challenge + "00";
        String response = "";
        try {
            response = transmitLogicChannel(subId, this.mTelephonyManager.getAidForAppType(subId, 5), chalData, (chalData.length() / 2) - 1);
            Log.i(LOG_TAG, "getGbaKeyResponse response " + response);
            return response;
        } catch (RemoteException e) {
            return response;
        }
    }

    private String getIsimResponse(int subId, String challenge) {
        if (this.mTelephonyManager == null) {
            return null;
        }
        String ChalData = "DD" + challenge + "00";
        String response = "";
        try {
            response = transmitLogicChannel(subId, this.mTelephonyManager.getAidForAppType(subId, 5), ChalData, (ChalData.length() / 2) - 1);
            Log.i(LOG_TAG, "getIsimResponse response " + response);
            return response;
        } catch (RemoteException e) {
            return response;
        }
    }

    private boolean isRealmFromUsername(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        return mno == Mno.KPN_NED || mno == Mno.TELEFONICA_CZ || mno == Mno.TELEFONICA_SLOVAKIA;
    }

    private void setAuthInfo(int phoneId, String requestUri, LastAuthInfo authInfo) {
        HashMap<String, LastAuthInfo> hashMap = this.mLastDigestInfoMap;
        hashMap.put(GbaUtility.getNafUrl(requestUri) + SimUtil.getSubId(phoneId), authInfo);
    }

    private void resetAuthInfo(int phoneId, String requestUri) {
        HashMap<String, LastAuthInfo> hashMap = this.mLastDigestInfoMap;
        hashMap.remove(GbaUtility.getNafUrl(requestUri) + SimUtil.getSubId(phoneId));
    }

    private LastAuthInfo getAuthInfo(int phoneId, String requestUri) {
        HashMap<String, LastAuthInfo> hashMap = this.mLastDigestInfoMap;
        return hashMap.get(GbaUtility.getNafUrl(requestUri) + SimUtil.getSubId(phoneId));
    }
}
