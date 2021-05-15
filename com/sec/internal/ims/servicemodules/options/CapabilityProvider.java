package com.sec.internal.ims.servicemodules.options;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityService;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.constants.ims.util.CscParserConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.IshIntents;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CapabilityProvider extends ContentProvider {
    private static final String ADDITIONAL_INFO_LOCAL_OFFLINE = "local_offline;";
    private static final String ADDITIONAL_INFO_NONE = "";
    private static final String ADDITIONAL_INFO_REMOTE_OFFLINE = "remote_offline;";
    private static final String ADDITIONAL_INFO_REMOTE_ONLINE = "fresh;";
    private static final String AUTHORITY = "com.samsung.rcs.serviceprovider";
    private static final String LOG_TAG = "CapabilityProvider";
    private static final int N_INCALL_SERVICE = 4;
    private static final int N_LOOKUP_URI = 1;
    private static final int N_LOOKUP_URI_ID = 2;
    private static final int N_OPERATOR_RCS_VERSION = 7;
    private static final int N_OWN_CAPS = 5;
    private static final int N_RCS_BIG_DATA = 8;
    private static final int N_RCS_ENABLED_STATIC = 6;
    private static final int N_SIP_URI = 3;
    private static final Pattern OPTIONS_PATTERN = Pattern.compile("\\?");
    /* access modifiers changed from: private */
    public static boolean ready_ish = true;
    /* access modifiers changed from: private */
    public static boolean ready_vsh = true;
    /* access modifiers changed from: private */
    public Map<ImsUri, Capabilities> mAsyncResults;
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public int mDataNetworkType = 0;
    /* access modifiers changed from: private */
    public boolean mIsWifiConnected = false;
    /* access modifiers changed from: private */
    public ImsUri mLastInCallUri = null;
    private final Object mLock = new Object();
    private UriMatcher mMatcher;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataConnectionStateChanged(int state, int networkType) {
            Log.i(CapabilityProvider.LOG_TAG, "onDataConnectionStateChanged(): state [" + state + "] networkType [" + TelephonyManagerExt.getNetworkTypeName(networkType) + "]");
            int unused = CapabilityProvider.this.mDataNetworkType = networkType;
        }
    };
    /* access modifiers changed from: private */
    public ShareServiceBroadcastReceiver mReceiver;
    /* access modifiers changed from: private */
    public ICapabilityService mService = null;
    private final ConnectivityManager.NetworkCallback mWifiStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            boolean unused = CapabilityProvider.this.mIsWifiConnected = true;
            Log.i(CapabilityProvider.LOG_TAG, "mWifiStateListener.onAvailable(): mIsWifiConnected [" + CapabilityProvider.this.mIsWifiConnected + "]");
        }

        public void onLost(Network network) {
            boolean unused = CapabilityProvider.this.mIsWifiConnected = false;
            Log.i(CapabilityProvider.LOG_TAG, "mWifiStateListener.onAvailable(): mIsWifiConnected [" + CapabilityProvider.this.mIsWifiConnected + "]");
        }
    };

    public boolean onCreate() {
        Log.i(LOG_TAG, "onCreate");
        this.mContext = getContext();
        this.mAsyncResults = new HashMap();
        UriMatcher uriMatcher = new UriMatcher(0);
        this.mMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "lookup/*/#", 2);
        this.mMatcher.addURI(AUTHORITY, "lookup/*", 1);
        this.mMatcher.addURI(AUTHORITY, "sip/*", 3);
        this.mMatcher.addURI(AUTHORITY, "incall/*", 4);
        this.mMatcher.addURI(AUTHORITY, "own", 5);
        this.mMatcher.addURI(AUTHORITY, "rcs_enabled_static", 6);
        this.mMatcher.addURI(AUTHORITY, "operator_rcs_version", 7);
        this.mMatcher.addURI(AUTHORITY, "rcs_big_data/*", 8);
        Log.i(LOG_TAG, "Connecting to CapabilityDiscoveryService.");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.CapabilityService");
        this.mReceiver = new ShareServiceBroadcastReceiver();
        new ImsPhoneStateManager(this.mContext, 64).registerListener(this.mPhoneStateListener);
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(12).build(), this.mWifiStateListener);
        ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(CapabilityProvider.LOG_TAG, "Connected to CapabilityDiscoveryService.");
                ICapabilityService unused = CapabilityProvider.this.mService = ICapabilityService.Stub.asInterface(service);
                try {
                    int phoneCount = SimUtil.getPhoneCount();
                    for (int i = 0; i < phoneCount; i++) {
                        CapabilityProvider.this.mService.registerListener(new ICapabilityServiceEventListener.Stub() {
                            public void onCapabilitiesChanged(List<ImsUri> uris, Capabilities capex) throws RemoteException {
                                if (uris != null) {
                                    for (ImsUri uri : uris) {
                                        if (UriUtil.equals(uri, CapabilityProvider.this.mLastInCallUri)) {
                                            boolean unused = CapabilityProvider.ready_ish = true;
                                            boolean unused2 = CapabilityProvider.ready_vsh = true;
                                            CapabilityProvider.this.notifyInCallServicesChange();
                                        }
                                        ImsUri found = null;
                                        Iterator it = CapabilityProvider.this.mAsyncResults.keySet().iterator();
                                        while (true) {
                                            if (!it.hasNext()) {
                                                break;
                                            }
                                            ImsUri pendingUri = (ImsUri) it.next();
                                            if (UriUtil.equals(pendingUri, uri)) {
                                                found = pendingUri;
                                                break;
                                            }
                                        }
                                        if (found != null) {
                                            CapabilityProvider.this.mAsyncResults.put(found, capex);
                                            CapabilityProvider.this.wakeup();
                                        }
                                        CapabilityProvider.this.notifyCapabilityChange(uri);
                                    }
                                }
                            }

                            public void onMultipleCapabilitiesChanged(List<ImsUri> list, List<Capabilities> list2) throws RemoteException {
                            }

                            public void onOwnCapabilitiesChanged() throws RemoteException {
                                boolean unused = CapabilityProvider.ready_ish = true;
                                boolean unused2 = CapabilityProvider.ready_vsh = true;
                                CapabilityProvider.this.notifyOwnServicesChange();
                                CapabilityProvider.this.notifyInCallServicesChange();
                            }

                            public void onCapabilityAndAvailabilityPublished(int errorCode) throws RemoteException {
                            }
                        }, i);
                    }
                } catch (RemoteException | NullPointerException e) {
                    e.printStackTrace();
                }
                CapabilityProvider.this.mContext.registerReceiver(CapabilityProvider.this.mReceiver, CapabilityProvider.createIntentFilter());
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(CapabilityProvider.LOG_TAG, "Disconnected.");
                ICapabilityService unused = CapabilityProvider.this.mService = null;
            }
        }, 1, ContextExt.CURRENT_OR_SELF);
        return false;
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    private void waitForUpdate(ImsUri remote) {
        IMSLog.s(LOG_TAG, "waitForUpdate: remote uri " + remote);
        try {
            this.mAsyncResults.put(remote, (Object) null);
            synchronized (this.mLock) {
                this.mLock.wait(1500);
            }
        } catch (InterruptedException e) {
            this.mAsyncResults.remove(remote);
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void wakeup() {
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private Capabilities getAsyncCapexResult(ImsUri identity) {
        Capabilities capex = this.mAsyncResults.get(identity);
        if (capex != null) {
            this.mAsyncResults.remove(identity);
        }
        return capex;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        IMSLog.s(LOG_TAG, "query(Uri, String[], String, String[], String) - uri: " + uri + ", selection: " + selection + ", args: " + Arrays.toString(selectionArgs));
        if (this.mService == null) {
            Log.e(LOG_TAG, "query before provider was started! Returning empty response");
            return new MatrixCursor(Projections.SERVICE_PROJECTION);
        }
        String[] split = OPTIONS_PATTERN.split(uri.toString());
        Uri uri2 = Uri.parse(split[0]);
        List<String> pathList = uri2.getPathSegments();
        CapabilityRefreshType refreshType = getRequeryStrategyId(split.length == 2 ? split[1] : null);
        int phoneId = UriUtil.getSimSlotFromUri(uri2);
        switch (this.mMatcher.match(uri2)) {
            case 2:
                IMSLog.s(LOG_TAG, phoneId, "N_LOOKUP_URI_ID | Operation for uri: ".concat(uri2.toString()));
                return queryLookupUriId(pathList, refreshType, phoneId);
            case 3:
                IMSLog.s(LOG_TAG, phoneId, "N_SIP_URI | Operation for uri: ".concat(uri2.toString()));
                return querySipUri(projection, pathList, refreshType, phoneId);
            case 4:
                IMSLog.s(LOG_TAG, phoneId, "N_INCALL_SERVICE | Operation for uri: ".concat(uri2.toString()));
                return queryIncallService(pathList, refreshType, phoneId);
            case 5:
                IMSLog.s(LOG_TAG, phoneId, "N_OWN_CAPS | Operation for uri: ".concat(uri2.toString()));
                return queryOwnCaps(phoneId);
            case 6:
                IMSLog.i(LOG_TAG, phoneId, "N_RCS_ENABLED_STATIC");
                return queryRcsEnabledStatic(phoneId);
            case 7:
                IMSLog.i(LOG_TAG, phoneId, "N_OPERATOR_RCS_VERSION");
                return queryOperatorRcsVersion(phoneId);
            case 8:
                IMSLog.s(LOG_TAG, phoneId, "N_RCS_BIG_DATA | Operation for uri: ".concat(uri2.toString()));
                return queryRcsBigData(pathList, phoneId);
            default:
                IMSLog.s(LOG_TAG, phoneId, "UNDEFINED CATEGORY! | Operation for uri: ".concat(uri2.toString()));
                throw new UnsupportedOperationException("Operation not supported for uri: ".concat(uri2.toString()));
        }
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:146)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:71)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    private android.database.Cursor queryLookupUriId(java.util.List<java.lang.String> r26, com.sec.ims.options.CapabilityRefreshType r27, int r28) {
        /*
            r25 = this;
            r8 = r25
            r9 = r26
            r10 = r28
            java.lang.String r11 = "CapabilityProvider"
            java.lang.String r0 = "queryLookupUriId"
            com.sec.internal.log.IMSLog.i(r11, r10, r0)
            int r0 = r26.size()
            r12 = 1
            int r0 = r0 - r12
            java.lang.Object r0 = r9.get(r0)
            r13 = r0
            java.lang.String r13 = (java.lang.String) r13
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            int r1 = r26.size()
            int r1 = r1 + -2
            java.lang.Object r1 = r9.get(r1)
            java.lang.String r1 = (java.lang.String) r1
            r0.append(r1)
            java.lang.String r1 = "/"
            r0.append(r1)
            r0.append(r13)
            java.lang.String r14 = r0.toString()
            android.database.MatrixCursor r0 = new android.database.MatrixCursor
            java.lang.String[] r1 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.SERVICE_PROJECTION
            r0.<init>(r1)
            r15 = r0
            com.sec.ims.options.ICapabilityService r0 = r8.mService     // Catch:{ RemoteException -> 0x01ee }
            int r1 = r27.ordinal()     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.options.Capabilities[] r0 = r0.getCapabilitiesByContactId(r13, r1, r10)     // Catch:{ RemoteException -> 0x01ee }
            if (r0 != 0) goto L_0x0065
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x01ee }
            r1.<init>()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r2 = "queryLookupUriId: Capabilities not found for contactId "
            r1.append(r2)     // Catch:{ RemoteException -> 0x01ee }
            r1.append(r13)     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            com.sec.internal.log.IMSLog.e(r11, r10, r1)     // Catch:{ RemoteException -> 0x01ee }
            return r15
        L_0x0065:
            r1 = 0
            java.lang.String r2 = "remote_offline;"
            int r7 = r0.length     // Catch:{ RemoteException -> 0x01ee }
            r16 = 0
            r6 = r16
            r24 = r2
            r2 = r1
            r1 = r24
        L_0x0073:
            if (r6 >= r7) goto L_0x01eb
            r3 = r0[r6]     // Catch:{ RemoteException -> 0x01ee }
            r5 = r3
            boolean r3 = r5.getExpired()     // Catch:{ RemoteException -> 0x01ee }
            r17 = r3
            boolean r3 = r5.isAvailable()     // Catch:{ RemoteException -> 0x01ee }
            r18 = r3
            if (r18 == 0) goto L_0x0094
            if (r17 == 0) goto L_0x008e
            java.lang.String r3 = ""
            r1 = r3
            r19 = r1
            goto L_0x0096
        L_0x008e:
            java.lang.String r3 = "fresh;"
            r1 = r3
            r19 = r1
            goto L_0x0096
        L_0x0094:
            r19 = r1
        L_0x0096:
            int r20 = r2 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM     // Catch:{ RemoteException -> 0x01ee }
            boolean r1 = r5.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            if (r1 != 0) goto L_0x00ac
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM     // Catch:{ RemoteException -> 0x01ee }
            boolean r1 = r5.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            if (r1 == 0) goto L_0x00a9
            goto L_0x00ac
        L_0x00a9:
            r3 = r16
            goto L_0x00ad
        L_0x00ac:
            r3 = r12
        L_0x00ad:
            com.sec.ims.util.ImsUri r1 = r5.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r21 = r5.getDisplayName()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r22 = r5.getNumber()     // Catch:{ RemoteException -> 0x01ee }
            r1 = r25
            r12 = r5
            r5 = r21
            r21 = r6
            r6 = r19
            r23 = r7
            r7 = r22
            java.lang.Object[] r1 = r1.createImRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r1)     // Catch:{ RemoteException -> 0x01ee }
            int r22 = r20 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT     // Catch:{ RemoteException -> 0x01ee }
            boolean r1 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            if (r1 != 0) goto L_0x00e7
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE     // Catch:{ RemoteException -> 0x01ee }
            boolean r1 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            if (r1 == 0) goto L_0x00e4
            goto L_0x00e7
        L_0x00e4:
            r3 = r16
            goto L_0x00e8
        L_0x00e7:
            r3 = 1
        L_0x00e8:
            com.sec.ims.util.ImsUri r1 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r5 = r12.getDisplayName()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r7 = r12.getNumber()     // Catch:{ RemoteException -> 0x01ee }
            r1 = r25
            r2 = r20
            r6 = r19
            java.lang.Object[] r1 = r1.createFtRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r1)     // Catch:{ RemoteException -> 0x01ee }
            int r20 = r22 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x01ee }
            boolean r3 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r1 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r5 = r12.getDisplayName()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r7 = r12.getNumber()     // Catch:{ RemoteException -> 0x01ee }
            r1 = r25
            r2 = r22
            r6 = r19
            java.lang.Object[] r1 = r1.createFtHttpRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r1)     // Catch:{ RemoteException -> 0x01ee }
            int r7 = r20 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STANDALONE_MSG     // Catch:{ RemoteException -> 0x01ee }
            boolean r3 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r1 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r5 = r12.getDisplayName()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r22 = r12.getNumber()     // Catch:{ RemoteException -> 0x01ee }
            r1 = r25
            r2 = r20
            r6 = r19
            r20 = r0
            r0 = r7
            r7 = r22
            java.lang.Object[] r1 = r1.createSlmRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r1)     // Catch:{ RemoteException -> 0x01ee }
            int r7 = r0 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH     // Catch:{ RemoteException -> 0x01ee }
            boolean r1 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r2 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.Object[] r0 = r8.createGeolocationPushRow(r0, r1, r2)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r0)     // Catch:{ RemoteException -> 0x01ee }
            int r2 = r7 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_GEO_VIA_SMS     // Catch:{ RemoteException -> 0x01ee }
            boolean r0 = r12.hasFeature(r0)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r1 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.Object[] r0 = r8.createGeoPushViaSMSRow(r7, r0, r1)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r0)     // Catch:{ RemoteException -> 0x01ee }
            int r0 = r2 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT     // Catch:{ RemoteException -> 0x01ee }
            boolean r3 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r1 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r5 = r12.getDisplayName()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r7 = r12.getNumber()     // Catch:{ RemoteException -> 0x01ee }
            r1 = r25
            r6 = r19
            java.lang.Object[] r1 = r1.createFtSfGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r1)     // Catch:{ RemoteException -> 0x01ee }
            int r2 = r0 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_INTEGRATED_MSG     // Catch:{ RemoteException -> 0x01ee }
            boolean r1 = r12.hasFeature(r1)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r3 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.Object[] r0 = r8.createIntegratedMessageRow(r0, r1, r3)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r0)     // Catch:{ RemoteException -> 0x01ee }
            int r0 = r2 + 1
            long r3 = com.sec.ims.options.Capabilities.FEATURE_PUBLIC_MSG     // Catch:{ RemoteException -> 0x01ee }
            boolean r3 = r12.hasFeature(r3)     // Catch:{ RemoteException -> 0x01ee }
            com.sec.ims.util.ImsUri r1 = r12.getUri()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r5 = r12.getDisplayName()     // Catch:{ RemoteException -> 0x01ee }
            java.lang.String r7 = r12.getNumber()     // Catch:{ RemoteException -> 0x01ee }
            r1 = r25
            r6 = r19
            java.lang.Object[] r1 = r1.createPublicMsgRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x01ee }
            r15.addRow(r1)     // Catch:{ RemoteException -> 0x01ee }
            int r6 = r21 + 1
            r2 = r0
            r1 = r19
            r0 = r20
            r7 = r23
            r12 = 1
            goto L_0x0073
        L_0x01eb:
            r20 = r0
            goto L_0x0204
        L_0x01ee:
            r0 = move-exception
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "queryLookupUriId: no uris exist for lookup, returning empty response: "
            r1.append(r2)
            r1.append(r14)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.e(r11, r10, r1)
        L_0x0204:
            return r15
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityProvider.queryLookupUriId(java.util.List, com.sec.ims.options.CapabilityRefreshType, int):android.database.Cursor");
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x010b A[Catch:{ RemoteException -> 0x02c7 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor querySipUri(java.lang.String[] r23, java.util.List<java.lang.String> r24, com.sec.ims.options.CapabilityRefreshType r25, int r26) {
        /*
            r22 = this;
            r8 = r22
            r9 = r26
            java.lang.String r0 = "CapabilityProvider"
            java.lang.String r1 = "querySipUri"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            android.database.MatrixCursor r1 = new android.database.MatrixCursor
            java.lang.String[] r2 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.SERVICE_PROJECTION
            r1.<init>(r2)
            r10 = r1
            int r1 = r24.size()
            r11 = 1
            int r1 = r1 - r11
            r12 = r24
            java.lang.Object r1 = r12.get(r1)
            java.lang.String r1 = (java.lang.String) r1
            java.util.ArrayList r13 = r8.getImsUriListFromQuery(r1)
            if (r13 == 0) goto L_0x02d0
            boolean r1 = r13.isEmpty()
            if (r1 == 0) goto L_0x0030
            goto L_0x02d0
        L_0x0030:
            com.sec.ims.options.ICapabilityService r1 = r8.mService     // Catch:{ RemoteException -> 0x02c7 }
            int r2 = r25.ordinal()     // Catch:{ RemoteException -> 0x02c7 }
            int r3 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.options.Capabilities[] r1 = r1.getCapabilitiesWithFeatureByUriList(r13, r2, r3, r9)     // Catch:{ RemoteException -> 0x02c7 }
            r14 = r1
            if (r14 != 0) goto L_0x0055
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x02c7 }
            r1.<init>()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r2 = "querySipUri: Capabilities not found for "
            r1.append(r2)     // Catch:{ RemoteException -> 0x02c7 }
            r1.append(r13)     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.internal.log.IMSLog.s(r0, r9, r1)     // Catch:{ RemoteException -> 0x02c7 }
            return r10
        L_0x0055:
            int r15 = r14.length     // Catch:{ RemoteException -> 0x02c7 }
            r16 = 0
            r7 = r16
        L_0x005a:
            if (r7 >= r15) goto L_0x02c5
            r1 = r14[r7]     // Catch:{ RemoteException -> 0x02c7 }
            r6 = r1
            if (r6 != 0) goto L_0x0065
            r21 = r7
            goto L_0x02be
        L_0x0065:
            java.lang.String[] r1 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.FEATURE_TAG_PROJECTION     // Catch:{ RemoteException -> 0x02c7 }
            java.util.List r1 = java.util.Arrays.asList(r1)     // Catch:{ RemoteException -> 0x02c7 }
            java.util.List r2 = java.util.Arrays.asList(r23)     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r1.equals(r2)     // Catch:{ RemoteException -> 0x02c7 }
            if (r1 == 0) goto L_0x0089
            java.lang.String r1 = "querySipUri: return feature tags."
            com.sec.internal.log.IMSLog.i(r0, r9, r1)     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String[] r1 = r6.getFeatureTag()     // Catch:{ RemoteException -> 0x02c7 }
            int r2 = r1.length     // Catch:{ RemoteException -> 0x02c7 }
            if (r2 <= 0) goto L_0x0085
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
        L_0x0085:
            r21 = r7
            goto L_0x02be
        L_0x0089:
            r2 = 0
            java.lang.String r1 = "querySipUri: return service info."
            com.sec.internal.log.IMSLog.i(r0, r9, r1)     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r6.hasNoRcsFeatures()     // Catch:{ RemoteException -> 0x02c7 }
            if (r1 == 0) goto L_0x0099
            r1 = 0
            r5 = r1
            goto L_0x00b1
        L_0x0099:
            boolean r1 = r6.getExpired()     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r6.isAvailable()     // Catch:{ RemoteException -> 0x02c7 }
            if (r3 == 0) goto L_0x00ad
            if (r1 == 0) goto L_0x00a9
            java.lang.String r4 = ""
            r5 = r4
            goto L_0x00b1
        L_0x00a9:
            java.lang.String r4 = "fresh;"
            r5 = r4
            goto L_0x00b1
        L_0x00ad:
            java.lang.String r4 = "remote_offline;"
            r5 = r4
        L_0x00b1:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x02c7 }
            r1.<init>()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r3 = "querySipUri: RCS additionalInfo = "
            r1.append(r3)     // Catch:{ RemoteException -> 0x02c7 }
            r1.append(r5)     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.internal.log.IMSLog.i(r0, r9, r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r17 = r2 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r6.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            if (r1 != 0) goto L_0x00dc
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r6.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            if (r1 == 0) goto L_0x00d9
            goto L_0x00dc
        L_0x00d9:
            r3 = r16
            goto L_0x00dd
        L_0x00dc:
            r3 = r11
        L_0x00dd:
            com.sec.ims.util.ImsUri r1 = r6.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r18 = r6.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r19 = r6.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r20 = r5
            r5 = r18
            r11 = r6
            r6 = r20
            r21 = r7
            r7 = r19
            java.lang.Object[] r1 = r1.createImRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r19 = r17 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            if (r1 != 0) goto L_0x0117
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            if (r1 == 0) goto L_0x0114
            goto L_0x0117
        L_0x0114:
            r3 = r16
            goto L_0x0118
        L_0x0117:
            r3 = 1
        L_0x0118:
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r17
            java.lang.Object[] r1 = r1.createFtRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r17 = r19 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r19
            java.lang.Object[] r1 = r1.createFtHttpRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r7 = r17 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STANDALONE_MSG     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r19 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r17
            r6 = r20
            r12 = r7
            r7 = r19
            java.lang.Object[] r1 = r1.createSlmRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r7 = r12 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r2 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.Object[] r1 = r8.createGeolocationPushRow(r12, r1, r2)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r2 = r7 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEO_VIA_SMS     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r3 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.Object[] r1 = r8.createGeoPushViaSMSRow(r7, r1, r3)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r12 = r2 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            java.lang.Object[] r1 = r1.createFtSfGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r2 = r12 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_INTEGRATED_MSG     // Catch:{ RemoteException -> 0x02c7 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r3 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.Object[] r1 = r8.createIntegratedMessageRow(r12, r1, r3)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r12 = r2 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STICKER     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            java.lang.Object[] r1 = r1.createStickerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r17 = r12 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_CALL_COMPOSER     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r12
            r6 = r20
            java.lang.Object[] r1 = r1.createCallComposerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r12 = r17 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_MAP     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r17
            r6 = r20
            java.lang.Object[] r1 = r1.createSharedMapRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r17 = r12 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_SKETCH     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r12
            r6 = r20
            java.lang.Object[] r1 = r1.createSharedSketchRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r12 = r17 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_POST_CALL     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r17
            r6 = r20
            java.lang.Object[] r1 = r1.createPostCallRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
            int r17 = r12 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_PUBLIC_MSG     // Catch:{ RemoteException -> 0x02c7 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02c7 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02c7 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02c7 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02c7 }
            r1 = r22
            r2 = r12
            java.lang.Object[] r1 = r1.createPublicMsgRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02c7 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02c7 }
        L_0x02be:
            int r7 = r21 + 1
            r12 = r24
            r11 = 1
            goto L_0x005a
        L_0x02c5:
            return r10
        L_0x02c7:
            r0 = move-exception
            r0.printStackTrace()
            r10.close()
            r1 = 0
            return r1
        L_0x02d0:
            java.lang.String r1 = "querySipUri: no valid uri to request"
            com.sec.internal.log.IMSLog.e(r0, r9, r1)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityProvider.querySipUri(java.lang.String[], java.util.List, com.sec.ims.options.CapabilityRefreshType, int):android.database.Cursor");
    }

    private Cursor queryIncallService(List<String> pathList, CapabilityRefreshType refreshType, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "queryIncallService");
        String phoneNumber = Uri.decode(pathList.get(pathList.size() - 1));
        MatrixCursor mc = new MatrixCursor(Projections.INCALL_PROJECTION);
        try {
            ImsUri teluri = UriGeneratorFactory.getInstance().get().getNormalizedUri(phoneNumber, true);
            Capabilities capex = this.mService.getCapabilities(teluri, refreshType.ordinal(), phoneId);
            if (refreshType.equals(CapabilityRefreshType.ALWAYS_FORCE_REFRESH)) {
                waitForUpdate(teluri);
                capex = getAsyncCapexResult(teluri);
            }
            if (capex == null) {
                IMSLog.s(LOG_TAG, phoneId, "queryIncallService: Capabilities not found for " + phoneNumber);
                this.mLastInCallUri = teluri;
                return mc;
            }
            this.mLastInCallUri = capex.getUri();
            if (capex.hasFeature(Capabilities.FEATURE_ISH) || capex.hasFeature(Capabilities.FEATURE_VSH)) {
                Capabilities capex_own = this.mService.getOwnCapabilities(phoneId);
                if (capex_own == null) {
                    IMSLog.i(LOG_TAG, phoneId, "queryIncallService: own capex is null");
                    return mc;
                } else if (!capex_own.isAvailable()) {
                    IMSLog.i(LOG_TAG, phoneId, "queryIncallService: own capex is not available");
                    return mc;
                } else if (capex_own.hasFeature(Capabilities.FEATURE_ISH) || capex_own.hasFeature(Capabilities.FEATURE_VSH)) {
                    IMSLog.i(LOG_TAG, phoneId, "queryIncallService: ready_ish = " + ready_ish + ", ready_vsh = " + ready_vsh);
                    if (this.mDataNetworkType == 3 && !this.mIsWifiConnected) {
                        ready_vsh = false;
                    }
                    boolean hasfeature_ish = false;
                    if (ready_ish && capex.hasFeature(Capabilities.FEATURE_ISH) && capex_own.hasFeature(Capabilities.FEATURE_ISH)) {
                        hasfeature_ish = true;
                    }
                    boolean hasfeature_vsh = false;
                    if (ready_vsh && capex.hasFeature(Capabilities.FEATURE_VSH) && capex_own.hasFeature(Capabilities.FEATURE_VSH)) {
                        hasfeature_vsh = true;
                    }
                    IMSLog.i(LOG_TAG, phoneId, "queryIncallService: hasfeature_ish = " + hasfeature_ish);
                    IMSLog.i(LOG_TAG, phoneId, "queryIncallService: hasfeature_vsh = " + hasfeature_vsh);
                    String remoteUri = capex.getUri().toString();
                    int rowId = 0 + 1;
                    mc.addRow(createShareVideoRow(0, hasfeature_vsh, remoteUri));
                    int rowId2 = rowId + 1;
                    mc.addRow(createImageFileShareRow(rowId, hasfeature_ish, remoteUri));
                    int i = rowId2 + 1;
                    mc.addRow(createImageCameraShareRow(rowId2, hasfeature_ish, remoteUri));
                    return mc;
                } else {
                    IMSLog.i(LOG_TAG, phoneId, "queryIncallService: No hasFeature for ish, vsh in own capex");
                    return mc;
                }
            } else {
                IMSLog.s(LOG_TAG, phoneId, "queryIncallService: No hasFeature for ish, vsh " + phoneNumber);
                return mc;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0087 A[Catch:{ RemoteException -> 0x021b }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor queryOwnCaps(int r18) {
        /*
            r17 = this;
            r8 = r17
            r9 = r18
            java.lang.String r0 = ""
            java.lang.String r1 = "CapabilityProvider"
            java.lang.String r2 = "queryOwnCaps"
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            android.database.MatrixCursor r2 = new android.database.MatrixCursor
            java.lang.String[] r3 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.SERVICE_PROJECTION
            r2.<init>(r3)
            r10 = r2
            com.sec.ims.options.ICapabilityService r2 = r8.mService     // Catch:{ RemoteException -> 0x021b }
            com.sec.ims.options.Capabilities r2 = r2.getOwnCapabilities(r9)     // Catch:{ RemoteException -> 0x021b }
            r11 = r2
            if (r11 != 0) goto L_0x0020
            return r10
        L_0x0020:
            r2 = r0
            com.sec.ims.util.ImsUri r3 = r11.getUri()     // Catch:{ RemoteException -> 0x021b }
            if (r3 == 0) goto L_0x0032
            com.sec.ims.util.ImsUri r3 = r11.getUri()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException -> 0x021b }
            r2 = r3
            r12 = r2
            goto L_0x0033
        L_0x0032:
            r12 = r2
        L_0x0033:
            boolean r2 = r11.isAvailable()     // Catch:{ RemoteException -> 0x021b }
            if (r2 == 0) goto L_0x003a
            goto L_0x003c
        L_0x003a:
            java.lang.String r0 = "local_offline;"
        L_0x003c:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x021b }
            r2.<init>()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r3 = "queryOwnCaps: RCS additionalInfo = "
            r2.append(r3)     // Catch:{ RemoteException -> 0x021b }
            r2.append(r0)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x021b }
            com.sec.internal.log.IMSLog.i(r1, r9, r2)     // Catch:{ RemoteException -> 0x021b }
            r2 = 0
            int r13 = r2 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM     // Catch:{ RemoteException -> 0x021b }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            r14 = 0
            r15 = 1
            if (r1 != 0) goto L_0x0069
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM     // Catch:{ RemoteException -> 0x021b }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            if (r1 == 0) goto L_0x0067
            goto L_0x0069
        L_0x0067:
            r3 = r14
            goto L_0x006a
        L_0x0069:
            r3 = r15
        L_0x006a:
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createImRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r16 = r13 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT     // Catch:{ RemoteException -> 0x021b }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            if (r1 != 0) goto L_0x0092
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE     // Catch:{ RemoteException -> 0x021b }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            if (r1 == 0) goto L_0x0090
            goto L_0x0092
        L_0x0090:
            r3 = r14
            goto L_0x0093
        L_0x0092:
            r3 = r15
        L_0x0093:
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r13
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createFtRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r13 = r16 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r16
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createFtHttpRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r14 = r13 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r13
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createFtInGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r13 = r14 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STANDALONE_MSG     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r14
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createSlmRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r1 = r13 + 1
            int r2 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH     // Catch:{ RemoteException -> 0x021b }
            boolean r2 = r11.hasFeature(r2)     // Catch:{ RemoteException -> 0x021b }
            java.lang.Object[] r2 = r8.createGeolocationPushRow(r13, r2, r12)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r2)     // Catch:{ RemoteException -> 0x021b }
            int r2 = r1 + 1
            int r3 = com.sec.ims.options.Capabilities.FEATURE_GEO_VIA_SMS     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r3)     // Catch:{ RemoteException -> 0x021b }
            java.lang.Object[] r1 = r8.createGeoPushViaSMSRow(r1, r3, r12)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r1 = r2 + 1
            long r3 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_CHAT_SESSION     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r3)     // Catch:{ RemoteException -> 0x021b }
            java.lang.Object[] r2 = r8.createChatbotChatSessionRow(r2, r3, r12)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r2)     // Catch:{ RemoteException -> 0x021b }
            int r2 = r1 + 1
            long r3 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_STANDALONE_MSG     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r3)     // Catch:{ RemoteException -> 0x021b }
            java.lang.Object[] r1 = r8.createChatbotSlmRow(r1, r3, r12)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r3 = r2 + 1
            long r4 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_EXTENDED_MSG     // Catch:{ RemoteException -> 0x021b }
            boolean r1 = r11.hasFeature(r4)     // Catch:{ RemoteException -> 0x021b }
            java.lang.Object[] r1 = r8.createExtendedbotRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r13 = r3 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT     // Catch:{ RemoteException -> 0x021b }
            boolean r4 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r3
            r3 = r4
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createFtSfGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r2 = r13 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_INTEGRATED_MSG     // Catch:{ RemoteException -> 0x021b }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.Object[] r1 = r8.createIntegratedMessageRow(r13, r1, r12)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r13 = r2 + 1
            long r3 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_CALL_COMPOSER     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r3)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createCallComposerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r14 = r13 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_MAP     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r13
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createSharedMapRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r13 = r14 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_SKETCH     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r14
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createSharedSketchRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r14 = r13 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_POST_CALL     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r13
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createPostCallRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r13 = r14 + 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STICKER     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r14
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createStickerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            int r14 = r13 + 1
            long r1 = com.sec.ims.options.Capabilities.FEATURE_PUBLIC_MSG     // Catch:{ RemoteException -> 0x021b }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x021b }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x021b }
            r1 = r17
            r2 = r13
            r4 = r12
            r6 = r0
            java.lang.Object[] r1 = r1.createPublicMsgRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x021b }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x021b }
            goto L_0x021f
        L_0x021b:
            r0 = move-exception
            r0.printStackTrace()
        L_0x021f:
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityProvider.queryOwnCaps(int):android.database.Cursor");
    }

    private Cursor queryRcsEnabledStatic(int phoneId) {
        int i = phoneId;
        IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic");
        MatrixCursor mc = new MatrixCursor(new String[]{CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS_CHAT_SERVICE});
        boolean isEnableRcs = false;
        boolean isEnableRcsChat = false;
        boolean isSimMobilityFeatureEnabled = SimUtil.isSimMobilityFeatureEnabled();
        String str = CloudMessageProviderContract.JsonData.TRUE;
        if (!isSimMobilityFeatureEnabled || !ImsUtil.isSimMobilityActivated(phoneId)) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            ContentValues cscSettings = CscParser.getCscImsSetting(sm != null ? sm.getNetworkNames() : null, i);
            if (cscSettings == null || cscSettings.size() <= 0) {
                IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic: cscSettings is null, isEnableRcs = false, isEnableRcsChat = false");
            } else {
                isEnableRcs = CollectionUtils.getBooleanValue(cscSettings, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS, false);
                isEnableRcsChat = CollectionUtils.getBooleanValue(cscSettings, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS_CHAT_SERVICE, false);
                IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic: Customer, isEnableRcs = " + isEnableRcs + ", isEnableRcsChat = " + isEnableRcsChat);
            }
            String[] strArr = new String[2];
            strArr[0] = isEnableRcs ? str : ConfigConstants.VALUE.INFO_COMPLETED;
            if (!isEnableRcsChat) {
                str = ConfigConstants.VALUE.INFO_COMPLETED;
            }
            strArr[1] = str;
            mc.addRow(strArr);
            return mc;
        }
        List<ImsProfile> profileList = ImsProfileLoaderInternal.getProfileList(this.mContext, i);
        if (profileList != null && profileList.size() > 0) {
            Iterator<ImsProfile> it = profileList.iterator();
            while (true) {
                if (it.hasNext()) {
                    ImsProfile profile = it.next();
                    if (profile != null && profile.getEnableRcs()) {
                        isEnableRcsChat = profile.getEnableRcsChat();
                        isEnableRcs = profile.getEnableRcs();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic: SimMobility, isEnableRcs = " + isEnableRcs + ", isEnableRcsChat = " + isEnableRcsChat);
        String[] strArr2 = new String[2];
        strArr2[0] = isEnableRcs ? str : ConfigConstants.VALUE.INFO_COMPLETED;
        if (!isEnableRcsChat) {
            str = ConfigConstants.VALUE.INFO_COMPLETED;
        }
        strArr2[1] = str;
        mc.addRow(strArr2);
        return mc;
    }

    private Cursor queryOperatorRcsVersion(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "queryOperatorRcsVersion");
        MatrixCursor mc = new MatrixCursor(new String[]{"OperatorRcsVersion"});
        mc.addRow(new String[]{ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "")});
        return mc;
    }

    private Cursor queryRcsBigData(List<String> pathList, int phoneId) {
        String remoteRcsStatus;
        int i = phoneId;
        IMSLog.i(LOG_TAG, i, "queryRcsBigData");
        MatrixCursor mc = new MatrixCursor(new String[]{"RemoteRcsStatus", "RemoteLegacyLatching", "PhoneNumber"});
        String phoneNumber = Uri.decode(pathList.get(pathList.size() - 1));
        try {
            try {
                Capabilities capex = this.mService.getCapabilities(UriGeneratorFactory.getInstance().get().getNormalizedUri(phoneNumber, true), CapabilityRefreshType.DISABLED.ordinal(), i);
                if (capex == null) {
                    IMSLog.s(LOG_TAG, "queryRcsBigData: Capabilities not found for " + phoneNumber);
                    return mc;
                }
                if (capex.hasNoRcsFeatures()) {
                    remoteRcsStatus = null;
                } else {
                    long mAvailableFeatures = capex.getAvailableFeatures();
                    remoteRcsStatus = (mAvailableFeatures > ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) ? 1 : (mAvailableFeatures == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) ? 0 : -1)) != 0 && (mAvailableFeatures > ((long) Capabilities.FEATURE_NON_RCS_USER) ? 1 : (mAvailableFeatures == ((long) Capabilities.FEATURE_NON_RCS_USER) ? 0 : -1)) != 0 && (mAvailableFeatures > ((long) Capabilities.FEATURE_NOT_UPDATED) ? 1 : (mAvailableFeatures == ((long) Capabilities.FEATURE_NOT_UPDATED) ? 0 : -1)) != 0 ? ADDITIONAL_INFO_REMOTE_ONLINE : ADDITIONAL_INFO_REMOTE_OFFLINE;
                }
                boolean remoteLegacyLatching = capex.getLegacyLatching();
                IMSLog.s(LOG_TAG, i, "queryRcsBigData: remoteRcsStatus = " + remoteRcsStatus + ", remoteLegacyLatching = " + remoteLegacyLatching + ", phoneNumber = " + phoneNumber);
                String[] strArr = new String[3];
                strArr[0] = remoteRcsStatus;
                strArr[1] = remoteLegacyLatching ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED;
                strArr[2] = phoneNumber;
                mc.addRow(strArr);
                return mc;
            } catch (RemoteException e) {
                e = e;
                e.printStackTrace();
                return mc;
            }
        } catch (RemoteException e2) {
            e = e2;
            e.printStackTrace();
            return mc;
        }
    }

    private ArrayList<ImsUri> getImsUriListFromQuery(String urisToQuery) {
        if (urisToQuery == null) {
            Log.e(LOG_TAG, "getImsUriListFromQuery: null uris");
            return null;
        }
        ArrayList<ImsUri> uriList = new ArrayList<>();
        String[] uriStringList = urisToQuery.split("\\s*,\\s*");
        if (uriStringList != null) {
            for (String uriString : uriStringList) {
                ImsUri sipUri = ImsUri.parse(uriString);
                if (!(sipUri == null || sipUri.toString().length() == 0)) {
                    uriList.add(sipUri);
                }
            }
        }
        return uriList;
    }

    private CapabilityRefreshType getRequeryStrategyId(String extraString) {
        if ("disable_requery".equals(extraString)) {
            return CapabilityRefreshType.DISABLED;
        }
        if ("force_requery".equals(extraString)) {
            return CapabilityRefreshType.ALWAYS_FORCE_REFRESH;
        }
        if ("force_requery_uce".equals(extraString)) {
            return CapabilityRefreshType.FORCE_REFRESH_UCE;
        }
        if ("force_requery_sync".equals(extraString)) {
            return CapabilityRefreshType.FORCE_REFRESH_SYNC;
        }
        if ("msg_conditional_requery".equals(extraString)) {
            return CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX;
        }
        return CapabilityRefreshType.ONLY_IF_NOT_FRESH;
    }

    private Object[] createImRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_CHAT, Integer.valueOf(enabled), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createFtRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_FT, Integer.valueOf(enabled), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createFtHttpRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_FT_HTTP, Integer.valueOf(enabled), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createFtInGroupChatRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), "ft-in-group-chat", Integer.valueOf(enabled), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createFtSfGroupChatRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_SF_GROUP_CHAT, Integer.valueOf(enabled), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createSlmRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_STANDALONE_MSG, Integer.valueOf(enabled), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createGeolocationPushRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_GEOLOCATION_PUSH, Integer.valueOf(enabled), null, null, sip, null, null, null};
    }

    private Object[] createGeoPushViaSMSRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), Integer.valueOf(Capabilities.FEATURE_GEO_VIA_SMS), Integer.valueOf(enabled), null, null, sip, null, null, null};
    }

    private Object[] createIntegratedMessageRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_INTEGRATED_MSG, Integer.valueOf(enabled), null, null, sip, null, null, null};
    }

    private Object[] createStickerRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_STICKER, Integer.valueOf(enabled), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createPublicMsgRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_PUBLIC_MSG, Integer.valueOf(enabled), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, sip, displayName, additionalInfo, number};
    }

    private Object[] createShareVideoRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_VSH, Integer.valueOf(enabled), Intents.LIVE_VIDEO_SHARE_INTENT_NAME, Intents.INTENT_CATEGORY, sip, "Live video"};
    }

    private Object[] createImageFileShareRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_ISH, Integer.valueOf(enabled), Intents.IMAGE_FILE_SHARE_INTENT_NAME, Intents.INTENT_CATEGORY, sip, "Picture"};
    }

    private Object[] createImageCameraShareRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), Capabilities.FEATURE_TAG_ISH, Integer.valueOf(enabled), Intents.IMAGE_CAMERA_SHARE_INTENT_NAME, Intents.INTENT_CATEGORY, sip, "Take a picture"};
    }

    private Object[] createCallComposerRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        Log.i(LOG_TAG, "has call composer feature: " + enabled);
        return new Object[]{Integer.valueOf(id), "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\"", Integer.valueOf(enabled), null, null, sip, displayName, additionalInfo, number};
    }

    private Object[] createSharedMapRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        Log.i(LOG_TAG, "has shared map feature: " + enabled);
        return new Object[]{Integer.valueOf(id), "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\"", Integer.valueOf(enabled), null, null, sip, displayName, additionalInfo, number};
    }

    private Object[] createSharedSketchRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        Log.i(LOG_TAG, "has shared sketch feature: " + enabled);
        return new Object[]{Integer.valueOf(id), "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\"", Integer.valueOf(enabled), null, null, sip, displayName, additionalInfo, number};
    }

    private Object[] createPostCallRow(int id, boolean enabled, String sip, String displayName, String additionalInfo, String number) {
        Log.i(LOG_TAG, "has post call feature: " + enabled);
        return new Object[]{Integer.valueOf(id), "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\"", Integer.valueOf(enabled), null, null, sip, displayName, additionalInfo, number};
    }

    private Object[] createChatbotChatSessionRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot\"", Integer.valueOf(enabled), null, null, sip, null, null, null};
    }

    private Object[] createChatbotSlmRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot.sa\"", Integer.valueOf(enabled), null, null, sip, null, null, null};
    }

    private Object[] createExtendedbotRow(int id, boolean enabled, String sip) {
        return new Object[]{Integer.valueOf(id), "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.xbotmessage\"", Integer.valueOf(enabled), null, null, sip, null, null, null};
    }

    static class Projections {
        static final String[] FEATURE_TAG_PROJECTION = {Columns.SERVICE_INDICATOR};
        static final String[] INCALL_PROJECTION = {"_id", Columns.SERVICE_INDICATOR, Columns.IS_ENABLED, Columns.INTENT_NAME, Columns.INTENT_CATEGORY, "sip_uri", "service_name"};
        static final String[] SERVICE_PROJECTION = {"_id", Columns.SERVICE_INDICATOR, Columns.IS_ENABLED, Columns.INTENT_NAME, Columns.INTENT_CATEGORY, "sip_uri", Columns.DISPLAYNAME, Columns.ADDITIONAL_INFO, "number"};

        Projections() {
        }
    }

    /* access modifiers changed from: private */
    public void notifyCapabilityChange(ImsUri updated) {
        Log.i(LOG_TAG, "notifyCapabilityChange");
        IMSLog.s(LOG_TAG, "notifyCapabilityChange: uri " + updated);
        ContentResolver cr = getContext().getContentResolver();
        cr.notifyChange(Uri.parse("content://com.samsung.rcs.serviceprovider/sip/" + updated.toString()), (ContentObserver) null);
    }

    /* access modifiers changed from: private */
    public void notifyOwnServicesChange() {
        Log.i(LOG_TAG, "notifyOwnServicesChange");
        getContext().getContentResolver().notifyChange(Uri.parse("content://com.samsung.rcs.serviceprovider/own"), (ContentObserver) null);
    }

    /* access modifiers changed from: private */
    public void notifyInCallServicesChange() {
        Log.i(LOG_TAG, "notifyInCallServicesChange");
        getContext().getContentResolver().notifyChange(Uri.parse("content://com.samsung.rcs.serviceprovider/incall"), (ContentObserver) null);
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    protected class ShareServiceBroadcastReceiver extends BroadcastReceiver {
        protected ShareServiceBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            IMSLog.s(CapabilityProvider.LOG_TAG, "ShareServiceBroadcastReceiver: action = " + action);
            if (action.equals(IshIntents.IshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY) || action.equals(VshIntents.VshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY)) {
                boolean unused = CapabilityProvider.ready_ish = false;
                boolean unused2 = CapabilityProvider.ready_vsh = false;
                CapabilityProvider.this.notifyInCallServicesChange();
            }
        }
    }

    /* access modifiers changed from: private */
    public static IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        filter.addAction(IshIntents.IshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY);
        filter.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        filter.addAction(VshIntents.VshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY);
        return filter;
    }
}
