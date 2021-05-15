package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.upload.FileUpload;
import com.gsma.services.rcs.upload.FileUploadInfo;
import com.gsma.services.rcs.upload.FileUploadService;
import com.gsma.services.rcs.upload.FileUploadServiceConfiguration;
import com.gsma.services.rcs.upload.IFileUpload;
import com.gsma.services.rcs.upload.IFileUploadListener;
import com.gsma.services.rcs.upload.IFileUploadService;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload.FileUploadMessage;
import com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload.IFileUploadTaskListener;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class FileUploadServiceImpl extends IFileUploadService.Stub implements IFileUploadTaskListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = FileUploadService.class.getSimpleName();
    private Context mContext = null;
    private ImConfig mImConfig = null;
    private IImModule mImModule = null;
    private Object mLock = new Object();
    private int mMaxUploadCnt = 0;
    private final RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();
    private final RemoteCallbackList<IFileUploadListener> mUploadListeners = new RemoteCallbackList<>();
    private Hashtable<String, IFileUpload> mUploadTasks = new Hashtable<>();

    public FileUploadServiceImpl(Context context, IImModule service) {
        this.mContext = context;
        this.mImModule = service;
        this.mImConfig = service.getImConfig();
    }

    public FileUploadServiceConfiguration getConfiguration() throws ServerApiException {
        return new FileUploadServiceConfiguration(Math.max(this.mImConfig.getMaxSizeExtraFileTr(), this.mImConfig.getMaxSizeFileTr()));
    }

    public IFileUpload uploadFile(Uri fileUri, boolean fileicon) throws ServerApiException {
        Uri uri = fileUri;
        if (uri != null) {
            int phoneId = SimUtil.getDefaultPhoneId();
            ImConfig imConfig = this.mImModule.getImConfig(phoneId);
            this.mImConfig = imConfig;
            if (imConfig.getFtHttpCsUri() == null) {
                Log.e(LOG_TAG, "Can't find proper http content server.");
                throw new ServerApiException("Can't find proper http content server.");
            } else if (this.mMaxUploadCnt == 0 || this.mUploadTasks.size() < this.mMaxUploadCnt) {
                String filePath = FileUtils.getFilePathFromUri(this.mContext, uri);
                if (filePath != null) {
                    File file = new File(filePath);
                    long MaxSizeFileTr = Math.max(this.mImConfig.getMaxSizeExtraFileTr(), this.mImConfig.getMaxSizeFileTr());
                    if (MaxSizeFileTr == 0 || file.length() <= MaxSizeFileTr) {
                        FileUploadImpl fuImpl = new FileUploadImpl(new FileUploadMessage(phoneId, this.mImConfig, this.mImModule.getLooper(), fileUri, filePath, file.getName(), file.length(), fileicon), this);
                        addFileUploadTask(fuImpl);
                        fuImpl.startUpload();
                        return fuImpl;
                    }
                    Log.e(LOG_TAG, "Max file size exceeds!");
                    throw new ServerApiException("Max file size exceeds");
                }
                String str = LOG_TAG;
                Log.e(str, "Can't retrieve file path from uri: " + uri);
                throw new ServerApiException("Can't retrieve file path from uri: " + uri);
            } else {
                Log.e(LOG_TAG, "Max file transfer tasks achieved!");
                throw new ServerApiException("Max file transfer tasks achieved");
            }
        } else {
            Log.e(LOG_TAG, "Invalid file uri!");
            throw new ServerApiException("Invalid file uri");
        }
    }

    public boolean canUploadFile() throws ServerApiException {
        if (this.mMaxUploadCnt == 0 || this.mUploadTasks.size() < this.mMaxUploadCnt) {
            return true;
        }
        return false;
    }

    public List<IBinder> getFileUploads() throws ServerApiException {
        ArrayList<IBinder> result = new ArrayList<>(this.mUploadTasks.size());
        Enumeration<IFileUpload> e = this.mUploadTasks.elements();
        while (e.hasMoreElements()) {
            result.add(e.nextElement().asBinder());
        }
        return result;
    }

    public IFileUpload getFileUpload(String uploadId) throws ServerApiException {
        return this.mUploadTasks.get(uploadId);
    }

    private void addFileUploadTask(FileUploadImpl task) {
        this.mUploadTasks.put(task.getUploadId(), task);
    }

    private void removeFileUploadTask(String uploadId) {
        this.mUploadTasks.remove(uploadId);
    }

    public void addFileUploadEventListener(IFileUploadListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mUploadListeners.register(listener);
        }
    }

    public void removeFileUploadEventListener(IFileUploadListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mUploadListeners.unregister(listener);
        }
    }

    private void broadcastFileUploadStateChanged(String uploadId, FileUpload.State state) {
        int listenerCnt = this.mUploadListeners.beginBroadcast();
        for (int i = 0; i < listenerCnt; i++) {
            try {
                this.mUploadListeners.getBroadcastItem(i).onStateChanged(uploadId, state);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mUploadListeners.finishBroadcast();
    }

    private void broadcastFileUploadProgress(String uploadId, long currentSize, long totalSize) {
        int listenerCnt = this.mUploadListeners.beginBroadcast();
        for (int i = 0; i < listenerCnt; i++) {
            try {
                this.mUploadListeners.getBroadcastItem(i).onProgressUpdate(uploadId, currentSize, totalSize);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mUploadListeners.finishBroadcast();
    }

    private void broadcastFileUploadComplete(String uploadId, FileUploadInfo info) {
        int listenerCnt = this.mUploadListeners.beginBroadcast();
        for (int i = 0; i < listenerCnt; i++) {
            try {
                this.mUploadListeners.getBroadcastItem(i).onUploaded(uploadId, info);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mUploadListeners.finishBroadcast();
    }

    public void onUploadStateChanged(String uploadId, FileUpload.State state, boolean finished) {
        synchronized (this.mLock) {
            broadcastFileUploadStateChanged(uploadId, state);
            if (finished) {
                removeFileUploadTask(uploadId);
            }
        }
    }

    public void onUploadProgress(String uploadId, long currentSize, long totalSize) {
        synchronized (this.mLock) {
            broadcastFileUploadProgress(uploadId, currentSize, totalSize);
        }
    }

    public void onUploadComplete(String uploadId, FileUploadInfo info) {
        synchronized (this.mLock) {
            broadcastFileUploadComplete(uploadId, info);
        }
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null || manager.getRegistrationInfo().length <= 0 || this.mImModule.getImConfig().getFtDefaultMech() != ImConstants.FtMech.HTTP) {
            return false;
        }
        return true;
    }

    public void addEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(listener);
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.unregister(listener);
        }
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.mLock) {
            int N = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        String str = LOG_TAG;
                        Log.e(str, "Can't notify listener: " + e);
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public int getServiceVersion() throws RemoteException {
        return 2;
    }
}
