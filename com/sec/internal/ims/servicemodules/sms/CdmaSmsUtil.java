package com.sec.internal.ims.servicemodules.sms;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class CdmaSmsUtil {
    public static final String CONTENT_TYPE_3GPP2 = "application/vnd.3gpp2.sms";
    private static final String LOG_TAG = SmsServiceModule.class.getSimpleName();
    public static final int PARAM_ID_BEARER_DATA = 8;
    public static final int PARAM_ID_BEARER_REPLY_OPTION = 6;
    public static final int PARAM_ID_ORIGINATING_ADDRESS = 2;
    public static final int PARAM_ID_SERVICE_CATEGORY = 1;
    public static final int PARAM_ID_TELESERVICE = 0;
    public static final int SMS_MSG_TYPE_PP = 0;
    public static final int TELESERVICE_WAP = 4100;

    public static boolean isValid3GPP2PDU(byte[] tPdu) {
        if (tPdu == null || tPdu.length < 6) {
            return false;
        }
        DataInputStream disIn = new DataInputStream(new ByteArrayInputStream(tPdu));
        int verifiedParamId = 0;
        while (disIn.available() > 0) {
            try {
                int paramId = disIn.readByte();
                if (disIn.available() <= 0) {
                    Log.e(LOG_TAG, "isValid3GPP2PDU() no data after paramId");
                    try {
                        disIn.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
                int len = disIn.readByte() & 255;
                if (disIn.available() > 0 && paramId == 0) {
                    len = disIn.readByte() & 255;
                }
                if (disIn.available() < len) {
                    String str = LOG_TAG;
                    Log.e(str, "isValid3GPP2PDU() wrong after PARAM" + paramId);
                    try {
                        disIn.close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                    return false;
                }
                if (paramId == 0) {
                    verifiedParamId |= 1;
                } else if (paramId != 1) {
                    if (paramId == 2) {
                        verifiedParamId |= 2;
                    } else if (paramId != 6) {
                        if (paramId != 8) {
                            String str2 = LOG_TAG;
                            Log.e(str2, "isValid3GPP2PDU() Invalid paramID [" + paramId + "]");
                            try {
                                disIn.close();
                            } catch (IOException ex3) {
                                ex3.printStackTrace();
                            }
                            return false;
                        }
                        verifiedParamId |= 8;
                    } else if (disIn.skip((long) len) < 0) {
                        try {
                            disIn.close();
                        } catch (IOException ex4) {
                            ex4.printStackTrace();
                        }
                        return false;
                    }
                }
                if (paramId != 6) {
                    if (disIn.skip((long) len) < 0) {
                        try {
                            disIn.close();
                        } catch (IOException ex5) {
                            ex5.printStackTrace();
                        }
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                disIn.close();
            } catch (Throwable th) {
                try {
                    disIn.close();
                } catch (IOException ex6) {
                    ex6.printStackTrace();
                }
                throw th;
            }
        }
        try {
            disIn.close();
        } catch (IOException ex7) {
            ex7.printStackTrace();
        }
        if (verifiedParamId == 11) {
            return true;
        }
        Log.e(LOG_TAG, "isValid3GPP2PDU() PDU doesn't have mandatory paramId");
        return false;
    }

    public static boolean isAdminMsg(byte[] tPdu) {
        if (tPdu == null || tPdu.length < 5) {
            return false;
        }
        DataInputStream disIn = new DataInputStream(new ByteArrayInputStream(tPdu));
        try {
            int msgType = disIn.readByte();
            int paramId = disIn.readByte();
            if (msgType == 0 && paramId == 0 && (disIn.readUnsignedByte() << 8) + disIn.readUnsignedByte() == 4100) {
                try {
                    disIn.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return true;
            }
            try {
                disIn.close();
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            disIn.close();
        } catch (Throwable th) {
            try {
                disIn.close();
            } catch (IOException ex3) {
                ex3.printStackTrace();
            }
            throw th;
        }
    }
}
