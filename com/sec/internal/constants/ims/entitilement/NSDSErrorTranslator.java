package com.sec.internal.constants.ims.entitilement;

import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.util.HashMap;
import java.util.Map;

public class NSDSErrorTranslator {
    private static final int GET_MSISDN = 7;
    private static final int GET_TOKEN = 8;
    private static final String LOG_TAG = NSDSErrorTranslator.class.getSimpleName();
    private static final int MANAGE_CONNECTIVITY = 4;
    private static final int MANAGE_LOC_AND_TC = 5;
    private static final int MANAGE_PUSH_TOKEN = 6;
    private static final int MANAGE_SERVICE = 3;
    private static final int REGISTERED_DEVICES = 9;
    private static final int REGISTERED_MSISDN = 2;
    private static final int REQ_3GPP_AUTH = 1;
    private static final int SERVICE_ENTITLEMENT_STATUS = 10;
    private static Map<String, Integer> mapNSDSMethodNames;

    static {
        HashMap hashMap = new HashMap();
        mapNSDSMethodNames = hashMap;
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, 1);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_MSISDN, 2);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_SERVICE, 3);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 4);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, 5);
        mapNSDSMethodNames.put("managePushToken", 6);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN, 7);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.GET_TOKEN, 8);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_DEVICES, 9);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, 10);
    }

    public static int translate(String nsdsMethodName, int operation, int nsdsErrorCode) {
        if (nsdsErrorCode == 1006) {
            return 1006;
        }
        int translatedMethodName = mapNSDSMethodNames.get(nsdsMethodName).intValue();
        if (translatedMethodName == 9) {
            return RegisteredDevicesErrorTranslator.translate(nsdsErrorCode);
        }
        switch (translatedMethodName) {
            case 1:
                return Request3gppAuthErrorTranslator.translate(nsdsErrorCode);
            case 2:
                return RegisteredMsisdnErrorTranslator.translate(nsdsErrorCode);
            case 3:
                return ManageServiceErrorTranslator.translate(operation, nsdsErrorCode);
            case 4:
                return ManageConnectivityErrorTranslator.translate(operation, nsdsErrorCode);
            case 5:
                return ManageLocationAndTCErrorTranslator.translate(nsdsErrorCode);
            case 6:
                return ManagePushTokenErrorTranslator.translate(nsdsErrorCode);
            default:
                String str = LOG_TAG;
                Log.d(str, "could not translate nsds error code unsupported method name:" + nsdsMethodName);
                return -1;
        }
    }

    static class Request3gppAuthErrorTranslator {
        Request3gppAuthErrorTranslator() {
        }

        public static int translate(int nsdsErrorCode) {
            if (nsdsErrorCode == 1004) {
                return 1001;
            }
            if (nsdsErrorCode == 1111) {
                return 1002;
            }
            Log.d("RegisteredMsisdnErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
            return -1;
        }
    }

    static class RegisteredMsisdnErrorTranslator {
        RegisteredMsisdnErrorTranslator() {
        }

        public static int translate(int nsdsErrorCode) {
            if (nsdsErrorCode == 1004 || nsdsErrorCode == 1029 || nsdsErrorCode == 1061 || nsdsErrorCode == 1111) {
                return 1100;
            }
            Log.d("RegisteredMsisdnErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
            return -1;
        }
    }

    static class RegisteredDevicesErrorTranslator {
        RegisteredDevicesErrorTranslator() {
        }

        public static int translate(int nsdsErrorCode) {
            if (nsdsErrorCode == 1004 || nsdsErrorCode == 1029 || nsdsErrorCode == 1111) {
                return 2000;
            }
            Log.d("RegisteredDevicesErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
            return -1;
        }
    }

    static class ManageLocationAndTCErrorTranslator {
        ManageLocationAndTCErrorTranslator() {
        }

        public static int translate(int nsdsErrorCode) {
            if (nsdsErrorCode == 1004 || nsdsErrorCode == 1029 || nsdsErrorCode == 1041 || nsdsErrorCode == 1111) {
                return 1800;
            }
            Log.d("ManageLocationAndTCErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
            return -1;
        }
    }

    static class ManagePushTokenErrorTranslator {
        ManagePushTokenErrorTranslator() {
        }

        public static int translate(int nsdsErrorCode) {
            if (nsdsErrorCode == 1004 || nsdsErrorCode == 1029 || nsdsErrorCode == 1046 || nsdsErrorCode == 1111) {
                return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_PUSH_TOKEN_GEN_FAILURE;
            }
            Log.d("ManagePushTokenErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
            return -1;
        }
    }

    static class ManageConnectivityErrorTranslator {
        ManageConnectivityErrorTranslator() {
        }

        public static int translate(int operation, int nsdsErrorCode) {
            if (operation == 0) {
                if (!(nsdsErrorCode == 1004 || nsdsErrorCode == 1022 || nsdsErrorCode == 1025)) {
                    if (nsdsErrorCode == 1054) {
                        return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_ACTIVATE_INVALID_DEVICE_GROUP;
                    }
                    if (nsdsErrorCode != 1111) {
                        Log.d("ManageConnectivityErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                        return -1;
                    }
                }
                return 1300;
            } else if (operation != 3) {
                Log.d("ManageConnectivityErrorTranslator", "could not translate operation:" + operation);
                return -1;
            } else if (nsdsErrorCode == 1004 || nsdsErrorCode == 1054 || nsdsErrorCode == 1111) {
                return 1400;
            } else {
                Log.d("ManageConnectivityErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                return -1;
            }
        }
    }

    static class ManageServiceErrorTranslator {
        ManageServiceErrorTranslator() {
        }

        public static int translate(int operation, int nsdsErrorCode) {
            if (operation == 0) {
                if (nsdsErrorCode != 1004) {
                    if (nsdsErrorCode == 1024) {
                        return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_INVALID_OWNER_ID;
                    }
                    if (nsdsErrorCode != 1029) {
                        if (nsdsErrorCode == 1044) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_MAX_SVC_INST_REACHED;
                        }
                        if (nsdsErrorCode == 1048) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_SVC_NOT_ENTITLED;
                        }
                        if (!(nsdsErrorCode == 1111 || nsdsErrorCode == 1040 || nsdsErrorCode == 1041)) {
                            Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                            return -1;
                        }
                    }
                }
                return 1500;
            } else if (operation != 1) {
                if (operation == 2) {
                    if (!(nsdsErrorCode == 1004 || nsdsErrorCode == 1024)) {
                        if (nsdsErrorCode == 1029) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS;
                        }
                        if (nsdsErrorCode == 1053) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID;
                        }
                        if (nsdsErrorCode != 1111) {
                            Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                            return -1;
                        }
                    }
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_GEN_FAILURE;
                } else if (operation != 5) {
                    if (operation != 7) {
                        Log.d("ManageServiceErrorTranslator", "could not translate operation:" + operation);
                        return -1;
                    } else if (nsdsErrorCode == 1004 || nsdsErrorCode == 1024 || nsdsErrorCode == 1029 || nsdsErrorCode == 1053 || nsdsErrorCode == 1111) {
                        return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_GEN_FAILURE;
                    } else {
                        Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                        return -1;
                    }
                } else if (nsdsErrorCode == 1004 || nsdsErrorCode == 1024 || nsdsErrorCode == 1029 || nsdsErrorCode == 1053 || nsdsErrorCode == 1111) {
                    return 1500;
                } else {
                    Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                    return -1;
                }
            } else if (nsdsErrorCode == 1004 || nsdsErrorCode == 1024 || nsdsErrorCode == 1029 || nsdsErrorCode == 1048 || nsdsErrorCode == 1053 || nsdsErrorCode == 1111) {
                return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE;
            } else {
                Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + nsdsErrorCode);
                return -1;
            }
        }
    }
}
