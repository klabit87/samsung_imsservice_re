package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.net.Network;
import com.sec.internal.helper.SimUtil;
import com.squareup.okhttp.Dns;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import javax.net.SocketFactory;

public class NSDSNetworkInfoManager {
    private static NSDSNetworkInfoManager mNSDSNetworkInfoManager;
    private Dns[] mDnsList;
    /* access modifiers changed from: private */
    public Network[] mNetworkList;
    private final int mPhoneCount;
    private SocketFactory[] mSocketFactoryList;

    public NSDSNetworkInfoManager() {
        int phoneCount = SimUtil.getPhoneCount();
        this.mPhoneCount = phoneCount;
        this.mSocketFactoryList = new SocketFactory[phoneCount];
        this.mNetworkList = new Network[phoneCount];
        this.mDnsList = new Dns[phoneCount];
    }

    public static NSDSNetworkInfoManager getInstance() {
        if (mNSDSNetworkInfoManager == null) {
            mNSDSNetworkInfoManager = new NSDSNetworkInfoManager();
        }
        return mNSDSNetworkInfoManager;
    }

    public SocketFactory getSocketFactory(int phoneId) {
        return this.mSocketFactoryList[phoneId];
    }

    public void setSocketFactory(SocketFactory socketFactory, int phoneId) {
        this.mSocketFactoryList[phoneId] = socketFactory;
    }

    public void setNetwork(Network network, final int phoneId) {
        this.mNetworkList[phoneId] = network;
        setDns(new Dns() {
            public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                if (hostname != null) {
                    try {
                        return Arrays.asList(NSDSNetworkInfoManager.this.mNetworkList[phoneId].getAllByName(hostname));
                    } catch (NullPointerException e) {
                        throw new UnknownHostException("android.net.Network.getAllByName returned null");
                    }
                } else {
                    throw new UnknownHostException("hostname == null");
                }
            }
        }, phoneId);
    }

    public void setDns(Dns dns, int phoneId) {
        this.mDnsList[phoneId] = dns;
    }

    public Dns getDns(int phoneId) {
        return this.mDnsList[phoneId];
    }

    public void clearNetworkInfo(int phoneId) {
        this.mSocketFactoryList[phoneId] = null;
        this.mNetworkList[phoneId] = null;
        this.mDnsList[phoneId] = null;
    }
}
