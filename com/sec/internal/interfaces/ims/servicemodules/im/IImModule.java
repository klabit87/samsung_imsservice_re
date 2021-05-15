package com.sec.internal.interfaces.ims.servicemodules.im;

import android.os.Looper;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.im.IImSessionListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IServiceAvailabilityEventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public interface IImModule extends IServiceModule {
    void acceptFileTransfer(int i);

    void addParticipants(String str, List<ImsUri> list);

    Future<FtMessage> attachFileToGroupChat(String str, String str2, Set<NotificationStatus> set, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4, String str5, FileDisposition fileDisposition);

    Future<FtMessage> attachFileToSingleChat(int i, String str, ImsUri imsUri, Set<NotificationStatus> set, String str2, String str3, boolean z, boolean z2, boolean z3, boolean z4, String str4, FileDisposition fileDisposition);

    void cancelFileTransfer(int i);

    void closeChat(String str);

    Future<ImSession> createChat(List<ImsUri> list, String str, String str2, int i, String str3);

    FutureTask<Boolean> deleteChats(List<String> list, boolean z);

    FutureTask<Boolean> deleteChatsForUnsubscribe();

    FutureTask<Boolean> deleteMessages(List<String> list, boolean z);

    FutureTask<Boolean> deleteMessagesByImdnId(Map<String, Integer> map, boolean z);

    FtMessage getFtMessage(int i);

    ImConfig getImConfig();

    ImConfig getImConfig(int i);

    ImSession getImSession(String str);

    Looper getLooper();

    String getUserAliasFromPreference(int i);

    void handleEventDefaultAppChanged();

    boolean hasEstablishedSession();

    boolean isServiceRegistered(int i, String str);

    void readMessages(String str, List<String> list);

    void reconfiguration(long[] jArr);

    void registerChatEventListener(IChatEventListener iChatEventListener);

    void registerFtEventListener(ImConstants.Type type, IFtEventListener iFtEventListener);

    void registerImSessionListener(IImSessionListener iImSessionListener);

    void registerImSessionListenerByPhoneId(IImSessionListener iImSessionListener, int i);

    void registerImsOngoingFtListener(IImsOngoingFtEventListener iImsOngoingFtEventListener);

    void registerImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener iImsOngoingFtEventListener, int i);

    void registerMessageEventListener(ImConstants.Type type, IMessageEventListener iMessageEventListener);

    void registerServiceAvailabilityEventListener(IServiceAvailabilityEventListener iServiceAvailabilityEventListener) throws NullPointerException, IllegalStateException;

    void rejectFileTransfer(int i);

    void resendMessage(int i);

    void resumeReceivingTransfer(int i);

    void resumeSendingTransfer(int i, boolean z);

    void sendFile(long j);

    Future<ImMessage> sendMessage(String str, String str2, Set<NotificationStatus> set, String str3, String str4, int i, boolean z, boolean z2, boolean z3, String str5, String str6, String str7, List<ImsUri> list, boolean z4, String str8, String str9, String str10, String str11);

    void setAutoAcceptFt(int i);

    void setAutoAcceptFt(int i, int i2);

    void setUserAlias(int i, String str);

    void unregisterImSessionListener(IImSessionListener iImSessionListener);

    void unregisterImSessionListenerByPhoneId(IImSessionListener iImSessionListener, int i);

    void unregisterImsOngoingListener(IImsOngoingFtEventListener iImsOngoingFtEventListener);

    void unregisterImsOngoingListenerByPhoneId(IImsOngoingFtEventListener iImsOngoingFtEventListener, int i);

    void unregisterServiceAvailabilityEventListener(IServiceAvailabilityEventListener iServiceAvailabilityEventListener);

    void updateExtendedBotMsgFeature(int i);
}
