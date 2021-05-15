package com.sec.internal.interfaces.ims.cmstore;

import android.os.Message;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;

public interface IControllerCommonInterface {
    void pause();

    void resume();

    void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener);

    void start();

    void stop();

    boolean update(int i);

    boolean updateDelay(int i, long j);

    boolean updateDelayRetry(int i, long j);

    boolean updateMessage(Message message);
}
