package com.sec.internal.ims.util;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UriGeneratorFactory {
    private static final String LOG_TAG = "UriGeneratorFactory";
    static volatile UriGeneratorFactory sUriFactory = null;
    private ImsUri DEFAULT_URI = ImsUri.parse("sip:default@default");
    private Context mContext = null;
    private ImsUri[] mPrimaryImpu;
    private Map<ImsUri, UriGenerator> mUriGenerators;

    public static UriGeneratorFactory getInstance() {
        if (sUriFactory == null) {
            Context context = ImsRegistry.getContext();
            synchronized (UriGeneratorFactory.class) {
                if (sUriFactory == null) {
                    sUriFactory = new UriGeneratorFactory(context);
                }
            }
        }
        return sUriFactory;
    }

    public UriGeneratorFactory(Context context) {
        this.mContext = context;
        this.mUriGenerators = new ConcurrentHashMap();
        this.mUriGenerators.put(this.DEFAULT_URI, new UriGeneratorImpl(context, ImsUri.UriType.SIP_URI, "us", "example.com", TelephonyManagerWrapper.getInstance(context), SubscriptionManager.getDefaultDataSubscriptionId(), SimUtil.getDefaultPhoneId()));
        ImsUri[] imsUriArr = new ImsUri[TelephonyManagerWrapper.getInstance(context).getPhoneCount()];
        this.mPrimaryImpu = imsUriArr;
        Arrays.fill(imsUriArr, (Object) null);
    }

    public UriGenerator create(ImsRegistration reg, ImsUri.UriType uriType) {
        String countryCode;
        ImsProfile profile = reg.getImsProfile();
        Mno mno = Mno.fromName(profile.getMnoName());
        if (mno != null) {
            countryCode = mno.getCountryCode();
        } else {
            countryCode = "";
        }
        ITelephonyManager tm = TelephonyManagerWrapper.getInstance(this.mContext);
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (mno != null && mno == Mno.VZW) {
            return new VzwUriGenerator(this.mContext, uriType, countryCode, reg.getDomain(), tm, subId, reg.getPhoneId(), profile);
        } else if ("us".equalsIgnoreCase(countryCode)) {
            return new UriGeneratorUs(this.mContext, uriType, countryCode, reg.getDomain(), tm, subId, reg.getPhoneId(), profile);
        } else if (mno != null && (mno == Mno.TMOBILE || mno == Mno.EE || mno == Mno.EE_ESN || mno == Mno.TMOBILE_PL)) {
            return new UriGeneratorDT(this.mContext, uriType, countryCode, reg.getDomain(), getDerivedDomainFromImsi(reg.getImsProfile().getMcc(), reg.getImsProfile().getMnc()), tm, subId, reg.getPhoneId(), profile);
        } else if (mno != null && "kr".equalsIgnoreCase(countryCode)) {
            UriGeneratorKr uriGeneratorKr = new UriGeneratorKr(this.mContext, uriType, countryCode, reg.getDomain(), tm, subId, reg.getPhoneId(), profile);
            uriGeneratorKr.setMnoName(mno.getName());
            return uriGeneratorKr;
        } else if (mno == null || mno != Mno.RJIL) {
            return new UriGeneratorImpl(uriType, countryCode, reg.getDomain(), tm, subId, reg.getPhoneId(), profile);
        } else {
            return new UriGeneratorRjil(this.mContext, uriType, countryCode, reg.getDomain(), tm, subId, reg.getPhoneId(), profile);
        }
    }

    private String getDerivedDomainFromImsi(String mcc, String mnc) {
        Log.d(LOG_TAG, "getImsiBasedDomain: mcc=" + mcc + " mnc=" + mnc);
        if (TextUtils.isEmpty(mcc) || TextUtils.isEmpty(mnc)) {
            return "";
        }
        return String.format(Locale.US, "ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{Integer.valueOf(Integer.parseInt(mnc)), Integer.valueOf(Integer.parseInt(mcc))});
    }

    public void add(ImsUri impu, UriGenerator generator) {
        if (impu != null) {
            int phoneId = generator.getPhoneId();
            ImsUri[] imsUriArr = this.mPrimaryImpu;
            if (imsUriArr[phoneId] == null) {
                imsUriArr[phoneId] = impu;
            }
            this.mUriGenerators.put(impu, generator);
        }
    }

    public void removeByPhoneId(int phoneId) {
        this.mPrimaryImpu[phoneId] = null;
        for (ImsUri key : this.mUriGenerators.keySet()) {
            if (!this.DEFAULT_URI.equals(key) && this.mUriGenerators.get(key).getPhoneId() == phoneId) {
                this.mUriGenerators.remove(key);
            }
        }
    }

    public boolean contains(ImsUri uri) {
        return this.mUriGenerators.containsKey(uri);
    }

    public UriGenerator get() {
        UriGenerator generator = null;
        int phoneId = SimUtil.getDefaultPhoneId();
        ImsUri[] imsUriArr = this.mPrimaryImpu;
        if (imsUriArr[phoneId] != null) {
            generator = this.mUriGenerators.get(imsUriArr[phoneId]);
        }
        if (generator == null) {
            return this.mUriGenerators.get(this.DEFAULT_URI);
        }
        return generator;
    }

    public UriGenerator get(int phoneId) {
        UriGenerator generator = null;
        ImsUri[] imsUriArr = this.mPrimaryImpu;
        if (imsUriArr[phoneId] != null) {
            generator = this.mUriGenerators.get(imsUriArr[phoneId]);
        }
        if (generator == null) {
            return this.mUriGenerators.get(this.DEFAULT_URI);
        }
        return generator;
    }

    public UriGenerator get(ImsUri uri) {
        if (uri == null) {
            return get();
        }
        UriGenerator generator = this.mUriGenerators.get(uri);
        if (generator != null) {
            return generator;
        }
        Log.d(LOG_TAG, "get: UriGenerator not found for uri=" + IMSLog.checker(uri) + ". use default.");
        return get();
    }

    public void updateUriGenerator(ImsRegistration reg, ImsUri.UriType uriType) {
        UriGenerator ug;
        for (NameAddr addr : reg.getImpuList()) {
            ImsUri uri = addr.getUri();
            if (!contains(uri)) {
                ug = create(reg, uriType);
                ug.extractOwnAreaCode(reg.getPreferredImpu().getUri().getMsisdn());
                ug.updateRat(reg.getCurrentRat());
            } else {
                ug = get(uri);
            }
            if (reg.hasVolteService()) {
                ug.updateNetworkPreferredUriType(UriGenerator.URIServiceType.VOLTE_URI, uriType);
            }
            if (reg.hasRcsService()) {
                ug.updateNetworkPreferredUriType(UriGenerator.URIServiceType.RCS_URI, uriType);
            }
            add(uri, ug);
        }
    }
}
