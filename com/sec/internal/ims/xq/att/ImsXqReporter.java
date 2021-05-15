package com.sec.internal.ims.xq.att;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.att.iqi.lib.Metric;
import com.att.iqi.lib.metrics.mm.MM01;
import com.att.iqi.lib.metrics.mm.MM02;
import com.att.iqi.lib.metrics.mm.MM03;
import com.att.iqi.lib.metrics.mm.MM04;
import com.att.iqi.lib.metrics.mm.MM05;
import com.att.iqi.lib.metrics.mm.MM06;
import com.att.iqi.lib.metrics.sp.SPRX;
import com.att.iqi.lib.metrics.sp.SPTX;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.xq.att.XqAgent.XqClient;
import com.sec.internal.ims.xq.att.data.XqEvent;
import com.sec.internal.interfaces.ims.core.handler.IMiscHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public class ImsXqReporter extends Handler {
    private static final int CLIENT_CHECK_TIME = 1000;
    private static final int EVENT_XQ_CLIENT_CHECK = 2;
    private static final int EVENT_XQ_MTRIP_RECEIVED = 1;
    private static final String LOG_TAG = "ImsXqReporter";
    private static final String XQ_SERVICE_LIB = "libiq_service.so";
    private static final long XQ_SUBMIT_MTRIP_GAP = 5;
    /* access modifiers changed from: private */
    public XqClient mClient;
    private final Context mContext;
    private IMiscHandler mMiscHandler = null;
    private boolean mStarted = false;
    /* access modifiers changed from: private */
    public AtomicInteger mSubmitting = new AtomicInteger();

    public ImsXqReporter(Context context) {
        this.mContext = context;
        IMiscHandler miscHandler = ImsRegistry.getHandlerFactory().getMiscHandler();
        this.mMiscHandler = miscHandler;
        miscHandler.registerForXqMtripEvent(this, 1, (Object) null);
    }

    public void start() {
        if (isXqEnabled(this.mContext)) {
            Log.d(LOG_TAG, "start");
            this.mSubmitting.set(0);
            if (this.mClient == null) {
                Log.d(LOG_TAG, "Create Client.");
                this.mClient = new XqClient();
            }
            isClientReady();
            this.mStarted = true;
        }
    }

    public void stop() {
        if (this.mStarted) {
            XqClient xqClient = this.mClient;
            if (xqClient != null) {
                xqClient.resetXqClient();
                this.mClient = null;
            }
            this.mStarted = false;
        }
        Log.d(LOG_TAG, "stop");
    }

    public static boolean isXqEnabled(Context context) {
        try {
            Class sServiceManagerClass = Class.forName("android.os.ServiceManager");
            if (sServiceManagerClass.getMethod("checkService", new Class[]{String.class}).invoke(sServiceManagerClass, new Object[]{"iqi"}) != null) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(LOG_TAG, "isXqEnabled: Exception");
            return false;
        }
    }

    private boolean isClientReady() {
        XqClient xqClient = this.mClient;
        if (xqClient != null) {
            return xqClient.isMgrReady();
        }
        return false;
    }

    private void onXqMtripEventReceived(AsyncResult result) {
        if (isXqEnabled(this.mContext)) {
            if (!this.mStarted) {
                Log.e(LOG_TAG, "Xqenabled but XqReporter not start yet.");
                start();
            }
            XqEvent xqMessage = (XqEvent) result.result;
            switch (AnonymousClass2.$SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[xqMessage.getMtrip().ordinal()]) {
                case 1:
                    submit01Mtrip(xqMessage);
                    return;
                case 2:
                    submit02Mtrip(xqMessage);
                    return;
                case 3:
                    submit03Mtrip(xqMessage);
                    return;
                case 4:
                    submit04Mtrip(xqMessage);
                    return;
                case 5:
                    submit05Mtrip(xqMessage);
                    return;
                case 6:
                    submit06Mtrip(xqMessage);
                    return;
                case 7:
                    submitSRXtrip(xqMessage);
                    return;
                case 8:
                    submitSTXtrip(xqMessage);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.xq.att.ImsXqReporter$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips;

        static {
            int[] iArr = new int[XqEvent.XqMtrips.values().length];
            $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips = iArr;
            try {
                iArr[XqEvent.XqMtrips.M01.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.M02.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.M03.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.M04.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.M05.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.M06.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.SPRX.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$xq$att$data$XqEvent$XqMtrips[XqEvent.XqMtrips.SPTX.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private void submit01Mtrip(XqEvent mtrip) {
        MM01 mm01 = new MM01();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.M01) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 5) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                mm01.setType(getUcharValueFromXqContent(mtrip.getMContent(0)));
                mm01.setDirection(getUcharValueFromXqContent(mtrip.getMContent(1)));
                mm01.setCallId(getStringValueFromXqContent(mtrip.getMContent(2)));
                mm01.setRequestUri(getStringValueFromXqContent(mtrip.getMContent(3)));
                mm01.setTo(getStringValueFromXqContent(mtrip.getMContent(4)));
                submitMtrip(mm01, MM01.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submit02Mtrip(XqEvent mtrip) {
        MM02 mm02 = new MM02();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.M02) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 2) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                mm02.setRegState(getUcharValueFromXqContent(mtrip.getMContent(0)));
                mm02.setCallId(getStringValueFromXqContent(mtrip.getMContent(1)));
                submitMtrip(mm02, MM02.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submit03Mtrip(XqEvent mtrip) {
        MM03 mm03 = new MM03();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.M03) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 2) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                mm03.setRegState(getShortValueFromXqContent(mtrip.getMContent(0)));
                mm03.setCallId(getStringValueFromXqContent(mtrip.getMContent(1)));
                submitMtrip(mm03, MM03.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submit04Mtrip(XqEvent mtrip) {
        MM04 mm04 = new MM04();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.M04) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 4) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                mm04.setDialedString(getStringValueFromXqContent(mtrip.getMContent(0)));
                mm04.setCallId(getStringValueFromXqContent(mtrip.getMContent(1)));
                mm04.setOriginatingUri(getStringValueFromXqContent(mtrip.getMContent(2)));
                mm04.setTerminatingUri(getStringValueFromXqContent(mtrip.getMContent(3)));
                submitMtrip(mm04, MM04.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submit05Mtrip(XqEvent mtrip) {
        MM05 mm05 = new MM05();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.M05) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 2) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                mm05.setCallState(getUcharValueFromXqContent(mtrip.getMContent(0)));
                mm05.setCallId(getStringValueFromXqContent(mtrip.getMContent(1)));
                submitMtrip(mm05, MM05.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submit06Mtrip(XqEvent mtrip) {
        MM06 mm06 = new MM06();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.M06) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 3) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                mm06.setTerminationDirection(getUcharValueFromXqContent(mtrip.getMContent(0)));
                mm06.setResponseCode(getShortValueFromXqContent(mtrip.getMContent(1)));
                mm06.setCallId(getStringValueFromXqContent(mtrip.getMContent(2)));
                submitMtrip(mm06, MM06.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submitSRXtrip(XqEvent mtrip) {
        SPRX srx = new SPRX();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.SPRX) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 3) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                srx.setTransId(getIntValueFromXqContent(mtrip.getMContent(0)));
                srx.setCSeq(getIntValueFromXqContent(mtrip.getMContent(1)));
                srx.setMessage(getStringValueFromXqContent(mtrip.getMContent(2)));
                submitMtrip(srx, SPRX.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submitSTXtrip(XqEvent mtrip) {
        SPTX stx = new SPTX();
        if (mtrip.getMtrip() != XqEvent.XqMtrips.SPTX) {
            Log.e(LOG_TAG, "wrong Mtrip : " + mtrip.getMtrip());
        } else if (mtrip.getMContentList().size() != 3) {
            Log.e(LOG_TAG, "wrong content size : " + mtrip.getMContentList().size());
        } else {
            try {
                stx.setTransId(getIntValueFromXqContent(mtrip.getMContent(0)));
                stx.setCSeq(getIntValueFromXqContent(mtrip.getMContent(1)));
                stx.setMessage(getStringValueFromXqContent(mtrip.getMContent(2)));
                submitMtrip(stx, SPTX.ID);
            } catch (WrongContentException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void submitMtrip(final Metric m, final Metric.ID id) {
        if (!isClientReady()) {
            Log.d(LOG_TAG, "ignore Mtrip as not-init");
        } else {
            postDelayed(new Runnable() {
                public void run() {
                    if (ImsXqReporter.this.mClient != null) {
                        ImsXqReporter.this.mClient.submitMetric(m, id);
                        if (ImsXqReporter.this.mSubmitting.decrementAndGet() < 0) {
                            ImsXqReporter.this.mSubmitting.set(0);
                        }
                    }
                }
            }, ((long) this.mSubmitting.incrementAndGet()) * XQ_SUBMIT_MTRIP_GAP);
        }
    }

    public void handleMessage(Message msg) {
        if (isXqEnabled(this.mContext)) {
            int i = msg.what;
            if (i == 1) {
                onXqMtripEventReceived((AsyncResult) msg.obj);
            } else if (i == 2) {
                isClientReady();
            }
        }
    }

    private byte getUcharValueFromXqContent(XqEvent.XqContent c) throws WrongContentException {
        if (!c.hasIntVal() || c.getType() != XqEvent.XqContentType.UCHAR) {
            throw new WrongContentException("unmatched Content type : " + c.getType());
        } else if (c.getIntVal() <= 127 && c.getIntVal() >= -128) {
            return (byte) c.getIntVal();
        } else {
            throw new WrongContentException("value range limit exceeded : " + c.getIntVal());
        }
    }

    private short getShortValueFromXqContent(XqEvent.XqContent c) throws WrongContentException {
        if (!c.hasIntVal() || c.getType() != XqEvent.XqContentType.USHORT) {
            throw new WrongContentException("unmatched Content type : " + c.getType());
        } else if (c.getIntVal() <= 32767 && c.getIntVal() >= -32768) {
            return (short) c.getIntVal();
        } else {
            throw new WrongContentException("value range limit exceeded : " + c.getIntVal());
        }
    }

    private int getIntValueFromXqContent(XqEvent.XqContent c) throws WrongContentException {
        if (c.hasIntVal() && c.getType() == XqEvent.XqContentType.UINT32) {
            return c.getIntVal();
        }
        throw new WrongContentException("unmatched Content type : " + c.getType());
    }

    private String getStringValueFromXqContent(XqEvent.XqContent c) throws WrongContentException {
        if (c.hasStrVal() && c.getType() == XqEvent.XqContentType.STRING) {
            return c.getStrVal();
        }
        throw new WrongContentException("unmatched Content type : " + c.getType());
    }
}
