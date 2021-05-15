package com.sec.internal.ims.gba;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DigestCalculator;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.parser.WwwAuthHeaderParser;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.log.IMSLog;

public class GbaBootstrapping {
    protected static final int CONNECTION_TIMEOUT = 10000;
    private static final int EXPIRE_TIME_THRESHOLD = 30;
    private static final String GBA_ME = "gba-me";
    private static final String GBA_UICC = "gba-u";
    protected static final int READ_DATA_TIMEOUT = 30000;
    private static SparseArray<GbaBootstrapping> sInstance = new SparseArray<>();
    private String LOG_TAG;
    private Nonce mBsfNonce;
    private String mBsfServer;
    private String mBtid;
    private String mCipherSuite;
    private byte[] mCk;
    Context mContext;
    private Gba mGba;
    private String mGbaKey;
    private IGbaServiceModule mGbaServiceModule;
    private String mGbaType;
    private String mISimDomain;
    private byte[] mIk;
    private String mImpi;
    private boolean mIsTls;
    private String mLifetime;
    private byte[] mNafId;
    private int mPhoneId;
    private byte[] mRes;
    private WwwAuthenticateHeader mResponseAuthHeader;
    boolean mTrustAllCerts;

    public static synchronized GbaBootstrapping getInstance(int phoneId) {
        GbaBootstrapping gbaBootstrapping;
        synchronized (GbaBootstrapping.class) {
            if (sInstance.get(phoneId) == null) {
                sInstance.put(phoneId, new GbaBootstrapping(phoneId));
            }
            gbaBootstrapping = sInstance.get(phoneId);
        }
        return gbaBootstrapping;
    }

    private GbaBootstrapping(int phoneId) {
        this.LOG_TAG = GbaBootstrapping.class.getSimpleName();
        this.mGbaServiceModule = null;
        this.mResponseAuthHeader = null;
        this.mContext = ImsRegistry.getContext();
        this.mGbaServiceModule = ImsRegistry.getGbaService();
        this.mGba = new Gba(30);
        this.mPhoneId = phoneId;
    }

    private void loadBsfAddressAndISimDomain(int phoneId) {
        String bsfIp;
        int xcapUriPref = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.SS.XCAP_ROOT_URI_PREF, 2);
        int bsfPort = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.SS.BSF_PORT, 80);
        if (xcapUriPref == 1 || xcapUriPref == 2 || xcapUriPref == 3) {
            bsfIp = UtUtils.getBSFDomain(this.mContext, phoneId);
        } else {
            bsfIp = ImsRegistry.getString(phoneId, GlobalSettingsConstants.SS.BSF_IP, "");
        }
        String str = "http://" + bsfIp + ":" + bsfPort + "/";
        this.mBsfServer = str;
        if (bsfPort == 443) {
            this.mBsfServer = str.replace(OMAGlobalVariables.HTTP, OMAGlobalVariables.HTTPS);
        }
    }

    private int startBootstrappingProc() {
        HttpRequest httpReq = null;
        Mno mno = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId).getSimMno();
        try {
            String realm = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.SS.XDM_USER_ID_DOMAIN, "");
            if (TextUtils.isEmpty(realm)) {
                realm = getRealm();
            }
            httpReq = HttpRequest.get(this.mBsfServer).useCaches(false).connectTimeout(10000).readTimeout(READ_DATA_TIMEOUT).authorization(makeAuthenticateHeader(realm, "", ""));
            httpReq.userAgent(HttpController.VAL_3GPP_GBA);
            if (mno == Mno.TMOUS) {
                httpReq.header(HttpController.HEADER_X_TMUS_IMEI, this.mGbaServiceModule.getImei(this.mPhoneId));
            }
            if (this.mTrustAllCerts) {
                httpReq.trustAllCerts().trustAllHosts();
            }
            int code = httpReq.code();
            String str = this.LOG_TAG;
            IMSLog.s(str, "response: code=" + code + ", message=" + httpReq.message());
            if (httpReq != null) {
                httpReq.disconnect();
            }
            if (code != 401) {
                return -1;
            }
            String challenge = httpReq.wwwAuthenticate();
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "challenge: " + challenge);
            this.mResponseAuthHeader = new WwwAuthHeaderParser().wwwAuthHeaderParse(challenge);
            Nonce nonce = new Nonce();
            this.mBsfNonce = nonce;
            nonce.parseNonce(this.mResponseAuthHeader.getNonce());
            String nonce2 = StrUtil.bytesToHexString(this.mBsfNonce.getAutnRand());
            if (!this.mGbaType.equals("gba-u")) {
                parseIsimResponse(StrUtil.hexStringToBytes(SimManagerFactory.getSimManager().getIsimAuthentication(nonce2)));
            }
            String cnonce = getCnonce();
            String response = new DigestCalculator(this.mImpi, this.mResponseAuthHeader.getAlgorithm(), cnonce, this.mResponseAuthHeader.getNonce(), "00000001", this.mResponseAuthHeader.getQop(), this.mResponseAuthHeader.getRealm(), this.mRes, "GET", "/", (byte[]) null).calculateDigest();
            try {
                httpReq = HttpRequest.get(this.mBsfServer).useCaches(false).connectTimeout(10000).readTimeout(READ_DATA_TIMEOUT).acceptEncoding("gzip;q=0,identity;q=1").authorization("Digest username=\"" + this.mImpi + "\", realm=\"" + this.mResponseAuthHeader.getRealm() + "\", nonce=\"" + this.mResponseAuthHeader.getNonce() + "\", uri=\"/\", qop=\"" + this.mResponseAuthHeader.getQop() + "\", nc=" + "00000001" + ", cnonce=\"" + cnonce + "\", response=\"" + response + "\", opaque=\"" + this.mResponseAuthHeader.getOpaque() + "\", algorithm=" + this.mResponseAuthHeader.getAlgorithm());
                httpReq.userAgent(HttpController.VAL_3GPP_GBA);
                if (mno == Mno.TMOUS) {
                    httpReq.header(HttpController.HEADER_X_TMUS_IMEI, this.mGbaServiceModule.getImei(this.mPhoneId));
                }
                if (this.mTrustAllCerts) {
                    httpReq.trustAllCerts().trustAllHosts();
                }
                if (httpReq.ok()) {
                    getBsAssociationFromXml(httpReq.body());
                } else {
                    String str3 = this.LOG_TAG;
                    Log.d(str3, "BSF authenticate failed, " + httpReq.code() + " " + httpReq.message());
                }
            } catch (HttpRequest.HttpRequestException e) {
                if (e.getCause() != null) {
                    e.getCause().printStackTrace();
                }
            } catch (Throwable th) {
                httpReq.disconnect();
                throw th;
            }
            httpReq.disconnect();
            return 0;
        } catch (HttpRequest.HttpRequestException e2) {
            if (e2.getCause() != null) {
                e2.getCause().printStackTrace();
            }
            if (httpReq != null) {
                httpReq.disconnect();
            }
            return -1;
        } catch (Throwable th2) {
            if (httpReq != null) {
                httpReq.disconnect();
            }
            throw th2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a3, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.gba.BootstrappedSa getBootstrappedSa(java.lang.String r9, java.lang.String r10, java.lang.String r11, boolean r12) {
        /*
            r8 = this;
            monitor-enter(r8)
            r8.mTrustAllCerts = r12     // Catch:{ all -> 0x00e3 }
            r8.mCipherSuite = r11     // Catch:{ all -> 0x00e3 }
            java.lang.String r0 = com.sec.internal.ims.gba.GbaUtility.getNafUrl(r9)     // Catch:{ all -> 0x00e3 }
            boolean r1 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x00e3 }
            r2 = 0
            if (r1 == 0) goto L_0x0012
            monitor-exit(r8)
            return r2
        L_0x0012:
            java.nio.charset.Charset r1 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x00e3 }
            byte[] r1 = r0.getBytes(r1)     // Catch:{ all -> 0x00e3 }
            r8.mNafId = r1     // Catch:{ all -> 0x00e3 }
            int r1 = r8.mPhoneId     // Catch:{ all -> 0x00e3 }
            com.sec.internal.interfaces.ims.core.ISimManager r1 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r1)     // Catch:{ all -> 0x00e3 }
            com.sec.internal.constants.Mno r1 = r1.getSimMno()     // Catch:{ all -> 0x00e3 }
            r3 = 0
            r8.mIsTls = r3     // Catch:{ all -> 0x00e3 }
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.BELL     // Catch:{ all -> 0x00e3 }
            if (r1 == r3) goto L_0x0031
            boolean r3 = com.sec.internal.ims.gba.GbaUtility.isTls(r9)     // Catch:{ all -> 0x00e3 }
            r8.mIsTls = r3     // Catch:{ all -> 0x00e3 }
        L_0x0031:
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r3 = r8.mGbaServiceModule     // Catch:{ all -> 0x00e3 }
            int r4 = r8.mPhoneId     // Catch:{ all -> 0x00e3 }
            java.lang.String r3 = r3.getImpi(r4)     // Catch:{ all -> 0x00e3 }
            r8.mImpi = r3     // Catch:{ all -> 0x00e3 }
            int r3 = r8.mPhoneId     // Catch:{ all -> 0x00e3 }
            r8.loadBsfAddressAndISimDomain(r3)     // Catch:{ all -> 0x00e3 }
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r3 = r8.mGbaServiceModule     // Catch:{ all -> 0x00e3 }
            int r4 = r8.mPhoneId     // Catch:{ all -> 0x00e3 }
            boolean r3 = r3.isGbaUiccSupported(r4)     // Catch:{ all -> 0x00e3 }
            if (r3 == 0) goto L_0x0053
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TMOUS     // Catch:{ all -> 0x00e3 }
            if (r4 == r1) goto L_0x0053
            java.lang.String r4 = "gba-u"
            r8.mGbaType = r4     // Catch:{ all -> 0x00e3 }
            goto L_0x0057
        L_0x0053:
            java.lang.String r4 = "gba-me"
            r8.mGbaType = r4     // Catch:{ all -> 0x00e3 }
        L_0x0057:
            java.lang.String r4 = r8.LOG_TAG     // Catch:{ all -> 0x00e3 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e3 }
            r5.<init>()     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = "GbaBootstrapping() mBsfServer="
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mBsfServer     // Catch:{ all -> 0x00e3 }
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = ", mImpi="
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mImpi     // Catch:{ all -> 0x00e3 }
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = ", mGbaType="
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mGbaType     // Catch:{ all -> 0x00e3 }
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00e3 }
            com.sec.internal.log.IMSLog.s(r4, r5)     // Catch:{ all -> 0x00e3 }
            com.sec.internal.ims.gba.Gba r4 = r8.mGba     // Catch:{ all -> 0x00e3 }
            byte[] r5 = r8.mNafId     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mGbaType     // Catch:{ all -> 0x00e3 }
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x00e3 }
            byte[] r6 = r6.getBytes(r7)     // Catch:{ all -> 0x00e3 }
            int r7 = r8.mPhoneId     // Catch:{ all -> 0x00e3 }
            com.sec.internal.ims.gba.GbaValue r4 = r4.getGbaValue(r5, r6, r7)     // Catch:{ all -> 0x00e3 }
            if (r4 != 0) goto L_0x00a4
            r8.startBootstrappingProc()     // Catch:{ all -> 0x00e3 }
            java.lang.String r5 = r8.mGbaKey     // Catch:{ all -> 0x00e3 }
            if (r5 == 0) goto L_0x00a2
            java.lang.String r5 = r8.mBtid     // Catch:{ all -> 0x00e3 }
            if (r5 != 0) goto L_0x00b5
        L_0x00a2:
            monitor-exit(r8)
            return r2
        L_0x00a4:
            java.lang.String r2 = r4.getBtid()     // Catch:{ all -> 0x00e3 }
            r8.mBtid = r2     // Catch:{ all -> 0x00e3 }
            byte[] r2 = r4.getValue()     // Catch:{ all -> 0x00e3 }
            r5 = 2
            java.lang.String r2 = android.util.Base64.encodeToString(r2, r5)     // Catch:{ all -> 0x00e3 }
            r8.mGbaKey = r2     // Catch:{ all -> 0x00e3 }
        L_0x00b5:
            java.lang.String r2 = r8.LOG_TAG     // Catch:{ all -> 0x00e3 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e3 }
            r5.<init>()     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = "return GBA key: mGbaKey="
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mGbaKey     // Catch:{ all -> 0x00e3 }
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = ", mBtid="
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mBtid     // Catch:{ all -> 0x00e3 }
            r5.append(r6)     // Catch:{ all -> 0x00e3 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00e3 }
            com.sec.internal.log.IMSLog.s(r2, r5)     // Catch:{ all -> 0x00e3 }
            com.sec.internal.ims.gba.BootstrappedSa r2 = new com.sec.internal.ims.gba.BootstrappedSa     // Catch:{ all -> 0x00e3 }
            java.lang.String r5 = r8.mGbaKey     // Catch:{ all -> 0x00e3 }
            java.lang.String r6 = r8.mBtid     // Catch:{ all -> 0x00e3 }
            r2.<init>(r5, r6)     // Catch:{ all -> 0x00e3 }
            monitor-exit(r8)
            return r2
        L_0x00e3:
            r9 = move-exception
            monitor-exit(r8)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaBootstrapping.getBootstrappedSa(java.lang.String, java.lang.String, java.lang.String, boolean):com.sec.internal.ims.gba.BootstrappedSa");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x012a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x012f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x012f A[ExcHandler: IOException | ParserConfigurationException | SAXException (e java.lang.Throwable), Splitter:B:4:0x0009] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getBsAssociationFromXml(java.lang.String r20) {
        /*
            r19 = this;
            r1 = r19
            java.io.ByteArrayInputStream r0 = new java.io.ByteArrayInputStream     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x0131 }
            java.lang.String r2 = "utf-8"
            r3 = r20
            byte[] r2 = r3.getBytes(r2)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r0.<init>(r2)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r2 = r0
            javax.xml.parsers.DocumentBuilderFactory r0 = javax.xml.parsers.DocumentBuilderFactory.newInstance()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            javax.xml.parsers.DocumentBuilder r0 = r0.newDocumentBuilder()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r4 = r0
            org.w3c.dom.Document r0 = r4.parse(r2)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            org.w3c.dom.Element r0 = r0.getDocumentElement()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r5 = r0
            java.lang.String r0 = r5.getNodeName()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r6 = "BootstrappingInfo"
            boolean r0 = r0.equals(r6)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            if (r0 != 0) goto L_0x0030
            return
        L_0x0030:
            java.lang.String r0 = "btid"
            org.w3c.dom.NodeList r0 = r5.getElementsByTagName(r0)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r6 = 0
            org.w3c.dom.Node r0 = r0.item(r6)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            org.w3c.dom.Node r0 = r0.getFirstChild()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r0.getNodeValue()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r1.mBtid = r0     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = "lifetime"
            org.w3c.dom.NodeList r0 = r5.getElementsByTagName(r0)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            org.w3c.dom.Node r0 = r0.item(r6)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            org.w3c.dom.Node r0 = r0.getFirstChild()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r0.getNodeValue()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r1.mLifetime = r0     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r0 = r1.mNafId     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r6 = r1.mCipherSuite     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r6 = com.sec.internal.ims.gba.GbaUtility.convertCipherSuite(r6)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            boolean r7 = r1.mIsTls     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r0 = com.sec.internal.ims.gba.GbaUtility.getSecurityProtocolId(r0, r6, r7)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r6 = r0
            java.lang.String r0 = r1.LOG_TAG     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7.<init>()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = "mBtid : "
            r7.append(r8)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = r1.mBtid     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7.append(r8)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = ", mLifetime : "
            r7.append(r8)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = r1.mLifetime     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7.append(r8)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r7 = r7.toString()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            com.sec.internal.log.IMSLog.s(r0, r7)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mGbaType     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r7 = "gba-u"
            boolean r0 = r0.equals(r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            if (r0 == 0) goto L_0x00ba
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r0 = r1.mGbaServiceModule     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            com.sec.internal.ims.gba.Nonce r7 = r1.mBsfNonce     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r7 = r7.getRand()     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = r1.mBtid     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r9 = r1.mLifetime     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r0.storeGbaBootstrapParams(r7, r8, r9)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r0 = r1.mGbaServiceModule     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            int r7 = r1.mPhoneId     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            int r7 = com.sec.internal.helper.SimUtil.getSubId(r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = r1.mGbaType     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.nio.charset.Charset r9 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r8 = r8.getBytes(r9)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r0.getNafExternalKeyBase64Decoded(r7, r8, r6)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r1.mGbaKey = r0     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            goto L_0x00f3
        L_0x00ba:
            com.sec.internal.ims.gba.Nonce r0 = r1.mBsfNonce     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r0 = r0.getRand()     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7 = 1
            r8 = 17
            byte[] r12 = java.util.Arrays.copyOfRange(r0, r7, r8)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mGbaType     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r9 = r0.getBytes(r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r10 = r1.mCk     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r11 = r1.mIk     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mImpi     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r13 = r0.getBytes(r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r14 = r1.mNafId     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r15 = r1.mLifetime     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mBtid     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            boolean r7 = r1.mIsTls     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = r1.mCipherSuite     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r18 = com.sec.internal.ims.gba.GbaUtility.convertCipherSuite(r8)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r16 = r0
            r17 = r7
            java.lang.String r0 = com.sec.internal.ims.gba.GbaUtility.igenerateGbaMEKey(r9, r10, r11, r12, r13, r14, r15, r16, r17, r18)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r1.mGbaKey = r0     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
        L_0x00f3:
            com.sec.internal.ims.gba.Gba r13 = r1.mGba     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mGbaType     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r14 = r0.getBytes(r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            byte[] r15 = r1.mNafId     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mGbaKey     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7 = 2
            byte[] r16 = android.util.Base64.decode(r0, r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.mLifetime     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r7 = r1.mBtid     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r17 = r0
            r18 = r7
            r13.storeGbaKey(r14, r15, r16, r17, r18)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r0 = r1.LOG_TAG     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7.<init>()     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = "gbaKey = "
            r7.append(r8)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r8 = r1.mGbaKey     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            r7.append(r8)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            java.lang.String r7 = r7.toString()     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            com.sec.internal.log.IMSLog.s(r0, r7)     // Catch:{ IllegalArgumentException -> 0x012a, IOException | ParserConfigurationException | SAXException -> 0x012f, IOException | ParserConfigurationException | SAXException -> 0x012f }
            goto L_0x012e
        L_0x012a:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x012f }
        L_0x012e:
            goto L_0x0141
        L_0x012f:
            r0 = move-exception
            goto L_0x0134
        L_0x0131:
            r0 = move-exception
            r3 = r20
        L_0x0134:
            java.lang.Throwable r2 = r0.getCause()
            if (r2 == 0) goto L_0x0141
            java.lang.Throwable r2 = r0.getCause()
            r2.printStackTrace()
        L_0x0141:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaBootstrapping.getBsAssociationFromXml(java.lang.String):void");
    }

    private String makeAuthenticateHeader(String realm, String nonce, String response) {
        String username = this.mImpi;
        return "Digest username=\"" + username + "\", realm=\"" + realm + "\", uri=\"/\", nonce=\"" + nonce + "\", response=\"" + response + "\"";
    }

    private void parseIsimResponse(byte[] result) {
        if (StrUtil.bytesToHexString(new byte[]{result[0]}).equalsIgnoreCase("DB")) {
            byte pwdLen = result[1];
            byte ckLen = result[result[1] + 1 + 1];
            byte ikLen = result[result[1] + 1 + 1 + result[result[1] + 1 + 1] + 1];
            this.mRes = new byte[pwdLen];
            for (int i = 0; i < pwdLen; i++) {
                this.mRes[i] = result[i + 1 + 1];
            }
            this.mCk = new byte[ckLen];
            for (int i2 = 0; i2 < ckLen; i2++) {
                this.mCk[i2] = result[result[1] + 1 + 1 + 1 + i2];
            }
            this.mIk = new byte[ikLen];
            for (int i3 = 0; i3 < ikLen; i3++) {
                this.mIk[i3] = result[result[1] + 1 + 1 + 1 + ckLen + 1 + i3];
            }
        }
    }

    private String getRealm() {
        if (TextUtils.isEmpty(this.mImpi) || !this.mImpi.contains("@")) {
            return null;
        }
        String str = this.mImpi;
        return str.substring(str.indexOf("@") + 1);
    }

    private static String getCnonce() {
        return String.valueOf(System.currentTimeMillis());
    }
}
