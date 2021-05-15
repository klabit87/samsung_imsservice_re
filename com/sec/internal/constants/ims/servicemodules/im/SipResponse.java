package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.servicemodules.ss.UtError;
import com.sec.internal.ims.servicemodules.volte2.CallStateMachine;

public enum SipResponse implements IEnumerationWithId<SipResponse> {
    SIP_100_TRYING(100),
    SIP_180_RINGING(MNO.EVR_ESN),
    SIP_181_CALL_IS_BEING_FORWARDED(MNO.VODAFONE_NZ),
    SIP_182_QUEUED(MNO.TPG_SG),
    SIP_183_SESSION_PROGRESS(MNO.MOVISTAR_MEXICO),
    SIP_200_OK(200),
    SIP_202_ACCEPTED(202),
    SIP_204_NO_NOTIFICATION(204),
    SIP_300_MULTIPLE_CHOICES(300),
    SIP_301_MOVED_PERMANENTLY(CallStateMachine.ON_TIMER_VZW_EXPIRED),
    SIP_302_MOVED_TEMPORARILY(CallStateMachine.ON_REINVITE_TIMER_EXPIRED),
    SIP_305_USE_PROXY(CallStateMachine.ON_DUMMY_DNS_TIMER_EXPIRED),
    SIP_380_ALTERNATIVE_SERVICE(380),
    SIP_400_BAD_REQUEST(400),
    SIP_401_UNAUTHORIZED(401),
    SIP_402_PAYMENT_REQUIRED(402),
    SIP_403_FORBIDDEN(403),
    SIP_404_NOT_FOUND(404),
    SIP_405_METHOD_NOT_ALLOWED(AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED),
    SIP_406_NOT_ACCEPTABLE(RegistrationEvents.EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF),
    SIP_407_PROXY_AUTHENTICATION_REQUIRED(RegistrationEvents.EVENT_CHECK_UNPROCESSED_OMADM_CONFIG),
    SIP_408_REQUEST_TIMEOUT(408),
    SIP_409_CONFLICT(409),
    SIP_410_GONE(410),
    SIP_411_LENGTH_REQUIRED(411),
    SIP_412_CONDITIONAL_REQUEST_FAILED(UtError.PRECONDITION_FAILED),
    SIP_413_REQUEST_ENTITY_TOO_LARGE(413),
    SIP_414_REQUEST_URI_TOO_LOG(414),
    SIP_415_UNSUPPORTED_MEDIA_TYPE(AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE),
    SIP_416_UNSUPPORTED_URI_SCHEME(416),
    SIP_417_UNKNOWN_RESOURCE_PRIORITY(417),
    SIP_420_BAD_EXTENSION(420),
    SIP_421_EXTENSION_REQUIRED(421),
    SIP_422_SESSION_INTERVAL_TOO_SMALL(422),
    SIP_423_INTERVAL_TOO_BRIEF(423),
    SIP_424_BAD_LOCATION_INFORMATION(424),
    SIP_428_USE_IDENTITY_HEADER(428),
    SIP_429_PROVIDE_REFERRER_IDENTITY(OMAGlobalVariables.HTTP_TOO_MANY_REQUEST),
    SIP_430_FLOW_FAILED(430),
    SIP_433_ANONYMITY_DISALLOWED(433),
    SIP_436_BAD_IDENTITY_INFO(436),
    SIP_437_UNSUPPORTED_CERTIFICATE(437),
    SIP_438_INVALID_IDENTITY_HEADER(438),
    SIP_439_FIRST_HOP_LACKS_OUTBOUND_SUPPORT(439),
    SIP_470_CONSENT_NEEDED(470),
    SIP_480_TEMPORARILY_UNAVAILABLE(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE),
    SIP_481_CALL_TRANSACTION_DOES_NOT_EXIST(481),
    SIP_482_LOOP_DETECTED(482),
    SIP_483_TOO_MANY_HOPS(483),
    SIP_484_ADDRESS_INCOMPLETE(484),
    SIP_485_AMBIGUOUS(485),
    SIP_486_BUSY_HERE(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE),
    SIP_487_REQUEST_TERMINATED(487),
    SIP_488_NOT_ACCEPTABLE_HERE(488),
    SIP_489_BAD_EVENT(489),
    SIP_491_REQUEST_PENDING(491),
    SIP_493_UNDECIPHERABLE(493),
    SIP_494_SECURITY_AGREEMENT_REQUIRED(494),
    SIP_500_SERVER_INTERNAL_ERROR(500),
    SIP_501_NOT_IMPLEMENTED(501),
    SIP_502_BAD_GATEWAY(502),
    SIP_503_SERVICE_UNAVAILABLE(503),
    SIP_504_SERVER_TIME_OUT(Id.REQUEST_IM_SENDMSG),
    SIP_505_VERSION_NOT_SUPPORTED(Id.REQUEST_IM_SEND_COMPOSING_STATUS),
    SIP_513_MESSAGE_TOO_LARGE(513),
    SIP_580_PRECONDITION_FAILURE(580),
    SIP_600_BUSY_EVERYWHERE(600),
    SIP_603_DECLINE(Id.REQUEST_UPDATE_TIME_IN_PLANI),
    SIP_604_DOES_NOT_EXIST_ANYWHERE(604),
    SIP_606_NOT_ACCEPTABLE(606),
    SIP_703_NO_DNS_RESULTS(703),
    SIP_709_NO_RESPONSE(709),
    SIP_UNKNOWN_RESPONSE(999);
    
    private static final ReverseEnumMap<SipResponse> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(SipResponse.class);
    }

    private SipResponse(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public SipResponse getFromId(int id2) {
        return fromId(id2);
    }

    public static SipResponse fromId(int id2) {
        SipResponse response = SIP_UNKNOWN_RESPONSE;
        try {
            return map.get(Integer.valueOf(id2));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return response;
        }
    }
}
