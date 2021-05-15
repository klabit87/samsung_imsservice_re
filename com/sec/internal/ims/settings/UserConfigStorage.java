package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import com.sec.imsservice.R;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.XmlUtils;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UserConfigStorage {
    private static final String KEY_LOADED = "loaded";
    private static final String LOG_TAG = "UserConfigStorage";
    private Context mContext;
    private final Object mLock = new Object();
    private String mMnoname;
    private int mPhoneId;

    protected UserConfigStorage(Context context, String initialMnoname, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "UserConfigStorage()");
        this.mContext = context;
        this.mMnoname = initialMnoname;
        this.mPhoneId = phoneId;
    }

    public void insert(ContentValues values) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "insert()");
        ImsSharedPrefHelper.put(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, values);
    }

    public Cursor query(String[] projection) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "query()");
        if (!isLoaded()) {
            synchronized (this.mLock) {
                initUserConfiguration();
            }
        }
        if (projection == null) {
            return null;
        }
        Map<String, String> value = ImsSharedPrefHelper.getStringArray(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, projection);
        MatrixCursor ret = new MatrixCursor((String[]) value.keySet().toArray(new String[0]));
        ret.addRow(value.values());
        return ret;
    }

    public boolean isLoaded() {
        boolean z;
        synchronized (this.mLock) {
            z = ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, false);
        }
        return z;
    }

    public void reset(String mnoname) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "reset()");
        synchronized (this.mLock) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (sm != null && !sm.isSimAvailable()) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "reset() sim not available");
            } else if (ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "mnoname", "").equals(mnoname)) {
                int i = this.mPhoneId;
                IMSLog.d(LOG_TAG, i, "reset() same mnoname " + mnoname);
            } else {
                ImsSharedPrefHelper.clear(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG);
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, false);
                this.mMnoname = mnoname;
                initUserConfiguration();
            }
        }
    }

    private void initUserConfiguration() {
        IMSLog.d(LOG_TAG, this.mPhoneId, "initUserConfiguration()");
        if (!"DEFAULT".equals(this.mMnoname)) {
            XmlPullParser xpp = this.mContext.getResources().getXml(R.xml.userconfiguration);
            ContentValues values = new ContentValues();
            ContentValues tmp = new ContentValues();
            String name = null;
            String mnoname = null;
            try {
                XmlUtils.beginDocument(xpp, "configurations");
                while (true) {
                    int next = xpp.next();
                    int type = next;
                    if (next == 1) {
                        break;
                    } else if (type == 2) {
                        int cnt = xpp.getAttributeCount();
                        for (int i = 0; i < cnt; i++) {
                            if ("name".equalsIgnoreCase(xpp.getAttributeName(i))) {
                                name = xpp.getAttributeValue(i);
                            } else if ("mnoname".equalsIgnoreCase(xpp.getAttributeName(i))) {
                                mnoname = xpp.getAttributeValue(i);
                            } else {
                                tmp.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                            }
                        }
                        if (!"default".equalsIgnoreCase(name)) {
                            if (mnoname != null && mnoname.equals(this.mMnoname)) {
                                values.putAll(tmp);
                                break;
                            }
                        } else {
                            values.putAll(tmp);
                        }
                        tmp.clear();
                    }
                }
                insert(values);
                int i2 = this.mPhoneId;
                IMSLog.i(LOG_TAG, i2, name + ", " + mnoname + ":" + values.toString());
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, true);
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "mnoname", this.mMnoname);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }
}
