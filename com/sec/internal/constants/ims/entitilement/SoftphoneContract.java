package com.sec.internal.constants.ims.entitilement;

import android.content.ContentUris;
import android.net.Uri;

public class SoftphoneContract {
    public static final String AUTHORITY = "com.sec.vsim.attsoftphone.settings";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.sec.vsim.attsoftphone.settings");
    public static final String PACKAGE_CONTEXT = "com.sec.imsservice";
    public static final String SERVICE_CLASS_NAME = "com.sec.internal.ims.entitlement.softphone.SoftphoneService";

    public interface AccountColumns {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String ENVIRONMENT = "environment";
        public static final String IMPI = "impi";
        public static final String LABEL = "label";
        public static final String MSISDN = "msisdn";
        public static final String SECRET_KEY = "secret_key";
        public static final String STATUS = "status";
        public static final String TOKEN_TYPE = "token_type";
        public static final String USERID = "userid";
    }

    public interface AddressColumns {
        public static final String ADDITIONAL_ADDRESS_INFO = "addressAdditional";
        public static final String ADDRESS_NAME = "name";
        public static final String CITY = "city";
        public static final String DEFAULT_STATUS = "default_status";
        public static final String E911AID = "E911AID";
        public static final String EXPIRE_DATE = "expire_date";
        public static final String FORMATTED_ADDRESS = "formattedAddress";
        public static final String HOUSE_NUMBER = "houseNumber";
        public static final String HOUSE_NUMBER_EXTENSION = "houseNumExt";
        public static final String STATE = "state";
        public static final String STATUS = "status";
        public static final String STREET_DIRECTION_PREFIX = "streetDir";
        public static final String STREET_DIRECTION_SUFFIX = "streetDirSuffix";
        public static final String STREET_NAME = "street";
        public static final String STREET_NAME_SUFFIX = "streetNameSuffix";
        public static final String ZIP = "zip";
    }

    public static final class AkaChallenge {
        public static final String ACTION_AKA_CHALLENGE_COMPLETE = "com.sec.imsservice.AKA_CHALLENGE_COMPLETE";
        public static final String ACTION_AKA_CHALLENGE_FAILED = "com.sec.imsservice.AKA_CHALLENGE_FAILED";
        public static final String ACTION_REQUEST_AKA_CHALLENGE = "com.sec.imsservice.REQUEST_AKA_CHALLENGE";
    }

    public static final class CallForwardingConditions {
        public static final int BUSY = 1;
        public static final int NOT_REACHABLE = 3;
        public static final int NO_ANSWER = 2;
        public static final int SS_CF_TYPE_CFNL = 8;
        public static final int UNCONDITIONAL = 0;
    }

    public interface CommonColumns {
        public static final String ACCOUNT_ID = "account_id";
        public static final String ID = "_id";
    }

    public static final class SoftphoneEnvironment {
        public static final int PROD = 1;
        public static final int STAGE = 0;
        public static final int UNKNOWN = -1;
    }

    public static final class SoftphoneRegistrationFailure {
        public static final String ACTION_TRY_REGISTER = "com.sec.ims.vsim.attsoftphone.action.TRY_REGISTER";
        public static final String EXTRA_IMPI = "com.sec.ims.vsim.attsoftphone.extra.IMPI";
        public static final String EXTRA_SUCCESS = "com.sec.ims.vsim.attsoftphone.extra.SUCCESS";
    }

    public static final class SoftphoneRequests {
        public static final int ADD_E911_ADDRESS = 7;
        public static final int GET_CALL_FORWARDING_INFO = 9;
        public static final int GET_CALL_WAITING_INFO = 8;
        public static final int OBTAIN_ACCESS_TOKEN = 0;
        public static final int OBTAIN_IMS_IDENTIFIERS = 4;
        public static final int OBTAIN_PD_COOKIES = 16;
        public static final int OBTAIN_TERMS_CONDITIONS = 2;
        public static final int PROVISION_ACCOUNT = 3;
        public static final int REFRESH_TOKEN = 15;
        public static final int RELEASE_IMS_IDENTIFIERS = 5;
        public static final int REQUEST_AKA_CHALLENGE = 19;
        public static final int RESERVE_IMS_IDENTIFIERS = 1;
        public static final int REVOKE_ACCESS_TOKEN = 12;
        public static final int REVOKE_REFRESH_TOKEN = 13;
        public static final int REVOKE_TOKEN = 18;
        public static final int SET_CALL_FORWARDING_INFO = 11;
        public static final int SET_CALL_WAITING_INFO = 10;
        public static final int TRY_DEREGISTER = 17;
        public static final int TRY_REGISTER = 14;
        public static final int VALIDATE_E911_ADDRESS = 6;
    }

    public static final class SoftphoneAccount implements CommonColumns, AccountColumns {
        public static final int ACCESS_OBTAINED = 2;
        public static final String ACTIVATE = "activate";
        public static final String ACTIVE_ACCOUNT = "active_account";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(SoftphoneContract.AUTHORITY_URI, TABLE_NAME);
        public static final String DEACTIVATE = "deactivate";
        public static final int DEREGISTERED = 4;
        public static final String FULL_FUNCTIONAL_ACCOUNT = "full_functional_account";
        public static final int INACTIVE = 0;
        public static final int MANUAL_DEREGISTERED = 3;
        public static final String PENDING_ACCOUNT = "pending_account";
        public static final String REGISTER = "register";
        public static final int REGISTERED = 5;
        public static final String REGISTERED_ACCOUNT = "registered_account";
        public static final String TABLE_NAME = "account";
        public static final int TGUARD_OBTAINED = 1;

        private SoftphoneAccount() {
        }

        public static final Uri buildActiveAccountUri() {
            return Uri.withAppendedPath(CONTENT_URI, ACTIVE_ACCOUNT);
        }

        public static final Uri buildActiveAccountMumUri(long userId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(CONTENT_URI, ACTIVE_ACCOUNT), userId);
        }

        public static final Uri buildPendingAccountUri(long userId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(CONTENT_URI, PENDING_ACCOUNT), userId);
        }

        public static final Uri buildFunctionalAccountUri() {
            return Uri.withAppendedPath(CONTENT_URI, FULL_FUNCTIONAL_ACCOUNT);
        }

        public static final Uri buildRegisteredAccountUri() {
            return Uri.withAppendedPath(CONTENT_URI, REGISTERED_ACCOUNT);
        }

        public static final Uri buildAccountUri(String impi) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, "impi"), impi);
        }

        public static final Uri buildAccountIdUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, "account_id"), accountId);
        }

        public static final Uri buildAccountIdUri(String accountId, long userId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, "account_id"), accountId), userId);
        }

        public static final Uri buildActivateAccountUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, ACTIVATE), accountId);
        }

        public static final Uri buildDeActivateAccountUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, DEACTIVATE), accountId);
        }

        public static final Uri buildRegisteredAccountUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, REGISTER), accountId);
        }

        public static final Uri buildAccountLabelUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, "label"), accountId);
        }

        public static final Uri buildAccountLabelUri(String accountId, long userId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, "label"), accountId), userId);
        }
    }

    public static final class SoftphoneAddress implements CommonColumns, AddressColumns {
        public static final String ACCOUNT_ADDRESS = "account_address";
        public static final String ACTIVE_ADDRESS = "active_address";
        public static final String BY_ACCOUNT = "by_account";
        public static final String BY_IMPI = "by_impi";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(SoftphoneContract.AUTHORITY_URI, "address");
        public static final int CURRENT = 1;
        public static final String CURRENT_ADDRESS = "current_address";
        public static final int DEFAULT = 2;
        public static final String DEFAULT_ADDRESS = "default_address";
        public static final String DELETE = "delete";
        public static final int DELETED = -1;
        public static final String GET_CURRENT_ADDRESS = "get_current_address";
        public static final String GET_DEFAULT_ADDRESS = "get_default_address";
        public static final String SAVED_ADDRESS = "saved_address";
        public static final String SET = "set";
        public static final String TABLE_NAME = "address";
        public static final int UNKNOWN = 0;
        public static final String UNSET = "unset";

        private SoftphoneAddress() {
        }

        public static final Uri buildAddressUri(long addressId) {
            return ContentUris.withAppendedId(CONTENT_URI, addressId);
        }

        public static final Uri buildAddressUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, ACCOUNT_ADDRESS), accountId);
        }

        public static final Uri buildActiveAddressUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, ACTIVE_ADDRESS), accountId);
        }

        public static final Uri buildSetCurrentAddressUri(String accountId, long addressId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, CURRENT_ADDRESS), SET), accountId), addressId);
        }

        public static final Uri buildUnsetCurrentAddressUri(String accountId, long addressId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, CURRENT_ADDRESS), UNSET), accountId), addressId);
        }

        public static final Uri buildResetCurrentAddressUri(String accountId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, CURRENT_ADDRESS), UNSET), accountId), 0);
        }

        public static final Uri buildGetCurrentAddressUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, GET_CURRENT_ADDRESS), BY_ACCOUNT), accountId);
        }

        public static final Uri buildGetCurrentAddressUriByImpi(String impi) {
            return Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, GET_CURRENT_ADDRESS), BY_IMPI), impi);
        }

        public static final Uri buildSetDefaultAddressUri(String accountId, long addressId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, DEFAULT_ADDRESS), SET), accountId), addressId);
        }

        public static final Uri buildUnsetDefaultAddressUri(String accountId, long addressId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, DEFAULT_ADDRESS), UNSET), accountId), addressId);
        }

        public static final Uri buildGetDefaultAddressUri(String accountId) {
            return Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, GET_DEFAULT_ADDRESS), BY_ACCOUNT), accountId);
        }

        public static final Uri buildDeleteAddressUri(String accountId, long addressId) {
            return ContentUris.withAppendedId(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(CONTENT_URI, SAVED_ADDRESS), DELETE), accountId), addressId);
        }
    }
}
