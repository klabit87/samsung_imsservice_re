package com.att.iqi.lib;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Metric implements Parcelable {
    public static final Parcelable.Creator<Metric> CREATOR = new Parcelable.Creator<Metric>() {
        public Metric createFromParcel(Parcel parcel) {
            int dataPosition = parcel.dataPosition();
            if (parcel.readInt() != 1) {
                parcel.readInt();
                String readString = parcel.readString();
                parcel.setDataPosition(dataPosition);
                return Metric.b(readString, parcel);
            }
            throw new IllegalArgumentException("API 1 not supported");
        }

        public Metric[] newArray(int i) {
            return new Metric[i];
        }
    };
    private static Map<String, Constructor<? extends Metric>> b = new HashMap();
    private final String a;
    private final int mMetricId;

    public static final class ID implements Parcelable {
        public static final Parcelable.Creator<ID> CREATOR = new Parcelable.Creator<ID>() {
            public ID createFromParcel(Parcel parcel) {
                return new ID(parcel);
            }

            public ID[] newArray(int i) {
                return new ID[i];
            }
        };
        private static final Pattern c = Pattern.compile("[A-Z0-9_]{4}");
        private final int a;
        private final String b;

        public ID(int i) {
            this.a = i;
            String a2 = a(i);
            this.b = a2;
            if (!b(a2)) {
                throw new IllegalArgumentException("Invalid Metric ID");
            }
        }

        protected ID(Parcel parcel) {
            parcel.readInt();
            this.a = parcel.readInt();
            this.b = parcel.readString();
        }

        public ID(String str) {
            if (b(str)) {
                this.b = str;
                this.a = a(str);
                return;
            }
            throw new IllegalArgumentException("Invalid Metric ID");
        }

        private static int a(String str) {
            if (str.length() != 4) {
                return 0;
            }
            return (str.charAt(3) & 255) | ((str.charAt(0) & 255) << 24) | ((str.charAt(1) & 255) << 16) | ((str.charAt(2) & 255) << 8);
        }

        private static String a(int i) {
            return new String(new char[]{(char) ((i >> 24) & 255), (char) ((i >> 16) & 255), (char) ((i >> 8) & 255), (char) (i & 255)});
        }

        private boolean b(String str) {
            return c.matcher(str).matches();
        }

        public int asInt() {
            return this.a;
        }

        public String asString() {
            return this.b;
        }

        public int describeContents() {
            return 0;
        }

        public boolean equals(Object obj) {
            if (obj == null || ID.class != obj.getClass()) {
                return false;
            }
            ID id = (ID) obj;
            return id.a == this.a && TextUtils.equals(id.b, this.b);
        }

        public int hashCode() {
            String str = this.b;
            return (str != null ? str.hashCode() : 0) + 3349;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(7);
            parcel.writeInt(this.a);
            parcel.writeString(this.b);
        }
    }

    protected Metric() {
        this.a = getClass().getCanonicalName();
        this.mMetricId = a();
    }

    protected Metric(Parcel parcel) {
        if (parcel.readInt() != 1) {
            int readInt = parcel.readInt();
            this.a = parcel.readString();
            parcel.readLong();
            parcel.setDataPosition(readInt);
            this.mMetricId = a();
            return;
        }
        throw new IllegalArgumentException("API 1 not supported");
    }

    private int a() {
        try {
            Field field = getClass().getField(UserConsentProviderContract.UserConsentList.ID);
            return ((ID) field.get(field)).asInt();
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Sub class must define an ID field");
        } catch (IllegalAccessException e2) {
            throw new IllegalArgumentException("Can't read ID field from sub class");
        }
    }

    /* access modifiers changed from: private */
    public static Metric b(String str, Parcel parcel) {
        try {
            Constructor<?> constructor = b.get(str);
            if (constructor == null) {
                constructor = Class.forName(str).getDeclaredConstructor(new Class[]{Parcel.class});
                constructor.setAccessible(true);
                b.put(str, constructor);
            }
            return (Metric) constructor.newInstance(new Object[]{parcel});
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e2) {
            return null;
        } catch (IllegalAccessException e3) {
            return null;
        } catch (InstantiationException e4) {
            return null;
        } catch (InvocationTargetException e5) {
            return null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public final int getId() {
        return this.mMetricId;
    }

    /* access modifiers changed from: protected */
    public abstract int serialize(ByteBuffer byteBuffer) throws BufferOverflowException;

    /* access modifiers changed from: protected */
    public void stringOut(ByteBuffer byteBuffer, String str) throws BufferOverflowException {
        if (str != null) {
            byteBuffer.put(str.replace(0, '_').getBytes(Charset.defaultCharset()));
        }
        byteBuffer.put((byte) 0);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(7);
        int dataPosition = parcel.dataPosition();
        parcel.writeInt(0);
        parcel.writeString(this.a);
        parcel.writeLong(0);
        int dataPosition2 = parcel.dataPosition();
        parcel.setDataPosition(dataPosition);
        parcel.writeInt(dataPosition2);
        parcel.setDataPosition(dataPosition2);
        parcel.writeInt(7);
    }
}
