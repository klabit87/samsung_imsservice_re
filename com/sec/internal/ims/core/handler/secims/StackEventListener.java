package com.sec.internal.ims.core.handler.secims;

import com.sec.ims.util.SipError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.X509CertVerifyRequest;
import java.util.List;

public abstract class StackEventListener {
    public void onISIMAuthRequested(int handle, String nonce, int tid) {
    }

    public void onRegistered(int handle, List<String> list, List<String> list2, SipError error, int retryAfter, int ecmpMode, String sipResponse) {
    }

    public void onRegImpuNotification(int handle, String impu) {
    }

    public void onContactActivated(int handle) {
    }

    public void onDeregistered(int handle, SipError error, int retryAfter) {
    }

    public void onSubscribed(int handle, SipError error) {
    }

    public void onDnsResponse(String hostname, List<String> list, int port, int handle) {
    }

    public void onRegInfoNotification(int handle, RegInfoChanged regInfo) {
    }

    public void onUpdatePani() {
    }

    public void onRefreshRegNotification(int handle) {
    }

    public void onUpdateRouteTableRequested(int handle, int operation, String ipAddress) {
    }

    public void onX509CertVerifyRequested(X509CertVerifyRequest request) {
    }

    public void onRegEventContactUriNotification(int handle, List<String> list, int isRegi, String contactUriType) {
    }
}
