package com.sec.internal.ims.servicemodules.options;

import java.util.ArrayList;
import java.util.Iterator;

public class Contact {
    private String mContactId;
    private ArrayList<ContactNumber> mContactNumberList = new ArrayList<>();
    private Object mContactNumberListLock = new Object();
    private String mName = null;
    private String mRawContactId = null;

    public static class ContactNumber {
        String mContactNormalizedNumber;
        String mContactNumber;

        public ContactNumber(String n1, String n2) {
            this.mContactNumber = n1;
            this.mContactNormalizedNumber = n2;
        }

        public String getNumber() {
            return this.mContactNumber;
        }

        public String getNormalizedNumber() {
            return this.mContactNormalizedNumber;
        }
    }

    public Contact(String id, String rawId) {
        this.mContactId = id;
        this.mRawContactId = rawId;
    }

    public String getId() {
        return this.mContactId;
    }

    public void setId(String id) {
        this.mContactId = id;
    }

    public String getRawId() {
        return this.mRawContactId;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void insertContactNumberIntoList(ContactNumber c) {
        synchronized (this.mContactNumberListLock) {
            try {
                Iterator<ContactNumber> it = this.mContactNumberList.iterator();
                while (it.hasNext()) {
                    if (it.next().getNumber().equals(c.getNumber())) {
                        return;
                    }
                }
                this.mContactNumberList.add(c);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<ContactNumber> getContactNumberList() {
        ArrayList<ContactNumber> arrayList;
        synchronized (this.mContactNumberListLock) {
            arrayList = (ArrayList) this.mContactNumberList.clone();
        }
        return arrayList;
    }

    public int hashCode() {
        int i = 1 * 31;
        String str = this.mContactId;
        int i2 = 0;
        int result = (i + (str == null ? 0 : str.hashCode())) * 31;
        String str2 = this.mRawContactId;
        if (str2 != null) {
            i2 = str2.hashCode();
        }
        return result + i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Contact other = (Contact) obj;
        String str = this.mContactId;
        if (str == null) {
            if (other.mContactId != null) {
                return false;
            }
        } else if (!str.equals(other.mContactId)) {
            return false;
        }
        String str2 = this.mRawContactId;
        if (str2 == null) {
            if (other.mRawContactId != null) {
                return false;
            }
        } else if (!str2.equals(other.mRawContactId)) {
            return false;
        }
        return true;
    }
}
