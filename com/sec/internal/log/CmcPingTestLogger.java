package com.sec.internal.log;

import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.CmcPingTestLogger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CmcPingTestLogger {
    private static final String GOOGLE_PUBLIC_NAMESERVER = "8.8.8.8";
    /* access modifiers changed from: private */
    public static String LOG_TAG = CmcPingTestLogger.class.getSimpleName();
    private static final int MAX_PING_COUNT = 3;
    private static final int PING_TIMEOUT_SECONDS = 5;
    private static final Map<String, String> PingServer = new HashMap<String, String>() {
        {
            put("ec1", "3.127.55.209");
            put("ase1", "18.140.41.245");
            put("ue1", "3.89.177.225");
            put("ane2", "13.124.244.70");
        }
    };

    public interface OnFinishListener {
        void OnFinish(int i);
    }

    public static void ping(List<String> pcscfList) {
        ping(pcscfList, (OnFinishListener) null);
    }

    public static void ping(List<String> pcscfList, OnFinishListener listener) {
        new Thread(new Runnable(pcscfList, listener) {
            public final /* synthetic */ List f$0;
            public final /* synthetic */ CmcPingTestLogger.OnFinishListener f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                CmcPingTestLogger.lambda$ping$0(this.f$0, this.f$1);
            }
        }).start();
    }

    static /* synthetic */ void lambda$ping$0(List pcscfList, OnFinishListener listener) {
        try {
            CountDownLatch latch = new CountDownLatch(2);
            StringBuilder[] outputBuffer = new StringBuilder[2];
            for (int i = 0; i < 2; i++) {
                outputBuffer[i] = new StringBuilder();
            }
            new PingThread(GOOGLE_PUBLIC_NAMESERVER, outputBuffer[0], latch).start();
            if (pcscfList != null && pcscfList.size() > 0) {
                String[] slice = ((String) pcscfList.get(0)).split("[-\\.]");
                if (slice.length == 5) {
                    new PingThread(PingServer.get(slice[2]), outputBuffer[1], latch).start();
                }
            }
            latch.await(10, TimeUnit.SECONDS);
            for (int i2 = 0; i2 < 2; i2++) {
                IMSLog.c(LogClass.CMC_PCSCF_PING_TEST, makePingLog(outputBuffer[i2].toString()));
            }
            if (listener != null) {
                listener.OnFinish(2);
            }
        } catch (Exception e) {
            IMSLog.e(LOG_TAG, e.getMessage());
        }
    }

    private static String makePingLog(String output) {
        Scanner scanner = new Scanner(output);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("PING")) {
                sb.append(line.split(" ")[1]);
            } else if (line.contains("packets transmitted")) {
                String[] splited = line.split(" ");
                sb.append(" ");
                sb.append(splited[0]);
                sb.append("/");
                sb.append(splited[3]);
            } else if (line.startsWith("rtt")) {
                String[] splited2 = line.split(" ")[3].split("/");
                for (int i = 0; i < 3; i++) {
                    sb.append("/");
                    sb.append(splited2[i]);
                }
            }
        }
        return sb.toString();
    }

    private static class PingThread extends Thread {
        CountDownLatch countDownLatch;
        String inetAddr;
        StringBuilder outputBuffer;

        public PingThread(String ip, StringBuilder sb, CountDownLatch latch) {
            this.inetAddr = ip;
            this.outputBuffer = sb;
            this.countDownLatch = latch;
        }

        /* Debug info: failed to restart local var, previous not found, register: 8 */
        public void run() {
            BufferedReader br;
            try {
                Process pingProcess = Runtime.getRuntime().exec(String.format("ping -c %d -W %d %s", new Object[]{3, 5, this.inetAddr}));
                try {
                    br = new BufferedReader(new InputStreamReader(pingProcess.getInputStream(), "UTF-8"));
                    int waitFor = pingProcess.waitFor();
                    while (true) {
                        String readLine = br.readLine();
                        String line = readLine;
                        if (readLine == null) {
                            break;
                        } else if (line.length() > 0) {
                            StringBuilder sb = this.outputBuffer;
                            sb.append(line);
                            sb.append("\n");
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    IMSLog.e(CmcPingTestLogger.LOG_TAG, e.getMessage());
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            } catch (Exception e2) {
                IMSLog.e(CmcPingTestLogger.LOG_TAG, e2.getMessage());
            }
            this.countDownLatch.countDown();
            return;
            throw th;
        }
    }
}
