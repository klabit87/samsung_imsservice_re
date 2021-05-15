package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.capability.Capabilities;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.contact.ContactUtil;
import com.gsma.services.rcs.contact.IContactService;
import com.gsma.services.rcs.contact.RcsContact;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactItem;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactPersisit;
import com.sec.internal.ims.servicemodules.tapi.service.utils.ContactInfo;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactServiceImpl extends IContactService.Stub {
    private static final String LOG_TAG = ContactServiceImpl.class.getSimpleName();
    private CapabilityDiscoveryService capabilityDiscoveryService;
    private Context mContext;
    private IPresenceModule presenceModule;

    private interface FilterContactInfo {
        boolean inScope(ContactInfo contactInfo);
    }

    public ContactServiceImpl(Context context) {
        this.mContext = null;
        this.presenceModule = null;
        this.capabilityDiscoveryService = null;
        this.presenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();
        this.capabilityDiscoveryService = (CapabilityDiscoveryService) ImsRegistry.getBinder("options", (String) null);
        this.mContext = context;
    }

    public RcsContact getRcsContact(ContactId contact) throws ServerApiException {
        if (contact == null || contact.toString() == null) {
            return null;
        }
        Capabilities capApi = null;
        ContactInfo contactInfo = getContactInfo(contact);
        boolean z = true;
        if (contactInfo.getRegistrationState() != 1) {
            z = false;
        }
        boolean registered = z;
        boolean blocked = isBlock(contactInfo.getContact());
        if (registered) {
            capApi = contactInfo.getCapabilities();
        }
        String str = LOG_TAG;
        Log.d(str, "getRcsContact ContactId = " + contact.toString() + ", contactInfo = " + contactInfo.toString() + ", registered = " + registered + ", capApi = " + capApi + ", DisplayName" + contactInfo.getDisplayName());
        return new RcsContact(contactInfo.getContact(), registered, capApi, contactInfo.getDisplayName(), getBlockTime(contactInfo.getContact()), blocked);
    }

    private List<RcsContact> getRcsContacts(FilterContactInfo filterContactInfo) throws ServerApiException {
        RcsContact contact2add;
        List<RcsContact> rcsContacts = new ArrayList<>();
        Set<ContactId> contacts = getContactIds();
        if (contacts == null) {
            return null;
        }
        for (ContactId contactid : contacts) {
            ContactInfo contactInfo = getContactInfo(contactid);
            if (!(contactInfo == null || !filterContactInfo.inScope(contactInfo) || (contact2add = getRcsContact(contactInfo.getContact())) == null)) {
                rcsContacts.add(contact2add);
            }
        }
        return rcsContacts;
    }

    public Set<ContactId> getContactIds() {
        Set<ContactId> rcsNumbers = new HashSet<>();
        try {
            com.sec.ims.options.Capabilities[] capabilitiesArray = this.capabilityDiscoveryService.getAllCapabilities(0);
            if (capabilitiesArray == null) {
                Log.d(LOG_TAG, "capabilitiesArray = null");
                return null;
            }
            for (com.sec.ims.options.Capabilities capabilities : capabilitiesArray) {
                String phoneNum = PhoneUtils.extractNumberFromUri(capabilities.getUri().toString());
                if (phoneNum != null) {
                    rcsNumbers.add(ContactUtil.getInstance(this.mContext).formatContact(phoneNum));
                }
            }
            return rcsNumbers;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<RcsContact> getRcsContacts() throws ServerApiException {
        int i;
        boolean z;
        Log.d(LOG_TAG, "getRcsContacts");
        List<RcsContact> rcsContactList = new ArrayList<>();
        ContactInfo contactInfo = new ContactInfo();
        try {
            com.sec.ims.options.Capabilities[] capabilitiesArray = this.capabilityDiscoveryService.getAllCapabilities(0);
            if (capabilitiesArray == null) {
                return null;
            }
            for (com.sec.ims.options.Capabilities capabilities : capabilitiesArray) {
                String phoneNum = PhoneUtils.extractNumberFromUri(capabilities.getUri().toString());
                if (phoneNum != null) {
                    ContactId contact = new ContactId(phoneNum);
                    contactInfo.setRcsStatusTimestamp(capabilities.getTimestamp().getTime());
                    contactInfo.setRcsDisplayName(capabilities.getDisplayName());
                    int i2 = 2;
                    if (capabilities.isAvailable()) {
                        i = 2;
                    } else {
                        i = 1;
                    }
                    contactInfo.setRcsStatus(i);
                    if (capabilities.isAvailable()) {
                        i2 = 1;
                    }
                    contactInfo.setRegistrationState(i2);
                    contactInfo.setContact(contact);
                    contactInfo.setCapabilities(CapabilityServiceImpl.transferCapabilities(capabilities));
                    ContactId contact2 = contactInfo.getContact();
                    if (contactInfo.getRegistrationState() == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    rcsContactList.add(new RcsContact(contact2, z, contactInfo.getCapabilities(), contactInfo.getDisplayName(), getBlockTime(contactInfo.getContact()), isBlock(contactInfo.getContact())));
                }
            }
            return rcsContactList;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<RcsContact> getRcsContactsOnline() throws ServerApiException {
        Log.d(LOG_TAG, "getRcsContactsOnline");
        return getRcsContacts(new FilterContactInfo() {
            public boolean inScope(ContactInfo contactInfo) {
                return contactInfo.getRegistrationState() == 1;
            }
        });
    }

    public List<RcsContact> getRcsContactsSupporting(final String serviceId) throws ServerApiException {
        Log.d(LOG_TAG, "getRcsContactsSupporting");
        return getRcsContacts(new FilterContactInfo() {
            public boolean inScope(ContactInfo contactInfo) {
                Set<String> supportedExtensions;
                Capabilities capabilities = contactInfo.getCapabilities();
                if (capabilities == null || (supportedExtensions = capabilities.getSupportedExtensions()) == null) {
                    return false;
                }
                for (String supportedExtension : supportedExtensions) {
                    if (supportedExtension.equals(serviceId)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void blockContact(ContactId contact) throws RemoteException {
        if (contact != null) {
            String str = LOG_TAG;
            Log.d(str, "Block contact:" + contact.toString());
            try {
                BlockContactPersisit.changeContactInfo(this.mContext, setBlockingState(contact, ContactInfo.BlockingState.BLOCKED));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            throw new ServerApiException("contact is null!");
        }
    }

    public void unblockContact(ContactId contact) throws RemoteException {
        if (contact != null) {
            String str = LOG_TAG;
            Log.d(str, "unblockContact contact" + contact.toString());
            try {
                BlockContactPersisit.changeContactInfo(this.mContext, setBlockingState(contact, ContactInfo.BlockingState.NOT_BLOCKED));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            throw new ServerApiException("contact is null!");
        }
    }

    public ContactInfo setBlockingState(ContactId contact, ContactInfo.BlockingState state) throws RemoteException {
        ContactInfo newInfo = new ContactInfo();
        newInfo.setBlockingState(state);
        newInfo.setBlockingTimestamp(System.currentTimeMillis());
        newInfo.setContact(contact);
        setContactInfo(newInfo);
        return newInfo;
    }

    public ContactInfo getContactInfo(ContactId contact) throws ServerApiException {
        ContactInfo newInfo = new ContactInfo();
        newInfo.setContact(contact);
        try {
            setContactInfo(newInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return newInfo;
    }

    public void setContactInfo(ContactInfo infos) throws RemoteException {
        infos.setRcsStatus(8);
        infos.setRegistrationState(0);
        infos.setRcsStatusTimestamp(System.currentTimeMillis());
        String telUri = "tel:" + PhoneUtils.extractNumberFromUri(infos.getContact().toString());
        int phoneId = SimUtil.getDefaultPhoneId();
        try {
            com.sec.ims.options.Capabilities capabilitiesfull = this.capabilityDiscoveryService.getCapabilities(ImsUri.parse(telUri), CapabilityRefreshType.ONLY_IF_NOT_FRESH.ordinal(), phoneId);
            if (capabilitiesfull != null) {
                infos.setRcsStatusTimestamp(capabilitiesfull.getTimestamp().getTime());
                infos.setRcsDisplayName(capabilitiesfull.getDisplayName());
                int i = 2;
                infos.setRcsStatus(capabilitiesfull.isAvailable() ? 2 : 1);
                if (capabilitiesfull.isAvailable()) {
                    i = 1;
                }
                infos.setRegistrationState(i);
                Log.d(LOG_TAG, "RcsStatus:" + infos.getRcsStatus() + "State:" + infos.getRegistrationState());
                Capabilities capabilities = CapabilityServiceImpl.transferCapabilities(capabilitiesfull);
                if (capabilities != null) {
                    infos.setCapabilities(capabilities);
                }
                PresenceInfo presenceInfo = null;
                IPresenceModule iPresenceModule = this.presenceModule;
                if (iPresenceModule != null && (presenceInfo = iPresenceModule.getPresenceInfoByContactId(telUri, phoneId)) == null) {
                    presenceInfo = this.presenceModule.getPresenceInfo(ImsUri.parse(telUri), phoneId);
                }
                if (presenceInfo != null) {
                    Log.d(LOG_TAG, "presenceInfo.getContactId() = " + presenceInfo.getContactId() + ", presenceInfo.getDisplayName() = " + presenceInfo.getDisplayName());
                    infos.setPresenceInfo(presenceInfo);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Cursor getCursor(ContactId contact) {
        return this.mContext.getContentResolver().query(Uri.parse("content://com.gsma.services.rcs.provider.blockedcontact/" + contact), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public boolean isBlock(ContactId contact) {
        boolean mIsBlocked;
        String blocked = "";
        Cursor cursor = getCursor(contact);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    blocked = cursor.getString(cursor.getColumnIndex(BlockContactItem.BlockDataItem.KEY_BLOCKED));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if ("BLOCKED".equals(blocked)) {
            mIsBlocked = true;
        } else {
            mIsBlocked = false;
        }
        String str = LOG_TAG;
        Log.d(str, "string blocked: " + blocked + "count ==1 mIsBlocked: " + mIsBlocked);
        if (cursor != null) {
            cursor.close();
        }
        return mIsBlocked;
        throw th;
    }

    public long getBlockTime(ContactId contact) {
        long rcsStatusTimestamp = -1;
        Cursor cursor = getCursor(contact);
        try {
            if (true != isBlock(contact)) {
                rcsStatusTimestamp = -1;
            } else if (cursor != null && cursor.moveToFirst()) {
                rcsStatusTimestamp = cursor.getLong(cursor.getColumnIndex(BlockContactItem.BlockDataItem.KEY_BLOCKING_TIMESTAMP));
            }
            if (cursor != null) {
                cursor.close();
            }
            return rcsStatusTimestamp;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }
}
