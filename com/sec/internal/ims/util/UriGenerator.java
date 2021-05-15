package com.sec.internal.ims.util;

import com.sec.ims.util.ImsUri;
import java.util.List;
import java.util.Set;

public abstract class UriGenerator {

    public enum URIServiceType {
        VOLTE_URI,
        RCS_URI
    }

    public abstract void extractOwnAreaCode(String str);

    public abstract ImsUri getNetworkPreferredUri(ImsUri.UriType uriType, String str);

    public abstract ImsUri getNetworkPreferredUri(ImsUri imsUri);

    public abstract ImsUri getNetworkPreferredUri(URIServiceType uRIServiceType, ImsUri imsUri);

    public abstract ImsUri getNetworkPreferredUri(URIServiceType uRIServiceType, ImsUri imsUri, String str);

    public abstract ImsUri getNetworkPreferredUri(URIServiceType uRIServiceType, String str, String str2);

    public abstract ImsUri getNetworkPreferredUri(String str);

    public abstract Set<ImsUri> getNetworkPreferredUri(URIServiceType uRIServiceType, Set<ImsUri> set);

    public abstract Set<ImsUri> getNetworkPreferredUri(Set<ImsUri> set);

    public abstract ImsUri getNormalizedUri(String str);

    public abstract ImsUri getNormalizedUri(String str, boolean z);

    public abstract int getPhoneId();

    public abstract ImsUri getUssdRuri(String str);

    public abstract ImsUri normalize(ImsUri imsUri);

    public abstract Set<ImsUri> swapUriType(List<ImsUri> list);

    public abstract void updateNetworkPreferredUriType(URIServiceType uRIServiceType, ImsUri.UriType uriType);

    public abstract void updateRat(int i);
}
