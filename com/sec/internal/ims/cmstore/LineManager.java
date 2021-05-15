package com.sec.internal.ims.cmstore;

import android.util.Log;
import com.sec.internal.interfaces.ims.cmstore.ILineStatusChangeCallBack;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineManager {
    private static final String TAG = LineManager.class.getSimpleName();
    private final ILineStatusChangeCallBack mILineStatusChangeCallBack;
    private final Map<String, LineWorkingStatus> mLineStatus = new HashMap();
    private final List<LineStatusObserver> mLineStatusOberserverList = new ArrayList();

    public interface LineStatusObserver {
        void onLineAdded(String str);
    }

    private enum LineWorkingStatus {
        WORKING
    }

    public LineManager(ILineStatusChangeCallBack lineChangeCallback) {
        this.mILineStatusChangeCallBack = lineChangeCallback;
    }

    public void registerLineStatusOberser(LineStatusObserver listener) {
        this.mLineStatusOberserverList.add(listener);
        if (this.mLineStatus.size() >= 1) {
            for (String number : this.mLineStatus.keySet()) {
                listener.onLineAdded(number);
            }
        }
    }

    public void initLineStatus() {
        List<String> status = this.mILineStatusChangeCallBack.notifyLoadLineStatus();
        if (status == null || status.size() == 0) {
            Log.i(TAG, "no line added yet");
            return;
        }
        for (String line : status) {
            addLine(line);
        }
    }

    public void addLine(String line) {
        String str = TAG;
        Log.i(str, "addLine :: " + IMSLog.checker(line));
        this.mLineStatus.put(line, LineWorkingStatus.WORKING);
        for (LineStatusObserver observer : this.mLineStatusOberserverList) {
            observer.onLineAdded(line);
        }
    }
}
