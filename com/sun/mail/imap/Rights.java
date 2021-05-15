package com.sun.mail.imap;

import java.util.Vector;

public class Rights implements Cloneable {
    private boolean[] rights;

    public static final class Right {
        public static final Right ADMINISTER = getInstance('a');
        public static final Right CREATE = getInstance('c');
        public static final Right DELETE = getInstance('d');
        public static final Right INSERT = getInstance('i');
        public static final Right KEEP_SEEN = getInstance('s');
        public static final Right LOOKUP = getInstance('l');
        public static final Right POST = getInstance('p');
        public static final Right READ = getInstance('r');
        public static final Right WRITE = getInstance('w');
        private static Right[] cache = new Right[128];
        char right;

        private Right(char right2) {
            if (right2 < 128) {
                this.right = right2;
                return;
            }
            throw new IllegalArgumentException("Right must be ASCII");
        }

        public static synchronized Right getInstance(char right2) {
            Right right3;
            synchronized (Right.class) {
                if (right2 < 128) {
                    if (cache[right2] == null) {
                        cache[right2] = new Right(right2);
                    }
                    right3 = cache[right2];
                } else {
                    throw new IllegalArgumentException("Right must be ASCII");
                }
            }
            return right3;
        }

        public String toString() {
            return String.valueOf(this.right);
        }
    }

    public Rights() {
        this.rights = new boolean[128];
    }

    public Rights(Rights rights2) {
        boolean[] zArr = new boolean[128];
        this.rights = zArr;
        System.arraycopy(rights2.rights, 0, zArr, 0, zArr.length);
    }

    public Rights(String rights2) {
        this.rights = new boolean[128];
        for (int i = 0; i < rights2.length(); i++) {
            add(Right.getInstance(rights2.charAt(i)));
        }
    }

    public Rights(Right right) {
        boolean[] zArr = new boolean[128];
        this.rights = zArr;
        zArr[right.right] = true;
    }

    public void add(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Rights rights2) {
        int i = 0;
        while (true) {
            boolean[] zArr = rights2.rights;
            if (i < zArr.length) {
                if (zArr[i]) {
                    this.rights[i] = true;
                }
                i++;
            } else {
                return;
            }
        }
    }

    public void remove(Right right) {
        this.rights[right.right] = false;
    }

    public void remove(Rights rights2) {
        int i = 0;
        while (true) {
            boolean[] zArr = rights2.rights;
            if (i < zArr.length) {
                if (zArr[i]) {
                    this.rights[i] = false;
                }
                i++;
            } else {
                return;
            }
        }
    }

    public boolean contains(Right right) {
        return this.rights[right.right];
    }

    public boolean contains(Rights rights2) {
        int i = 0;
        while (true) {
            boolean[] zArr = rights2.rights;
            if (i >= zArr.length) {
                return true;
            }
            if (zArr[i] && !this.rights[i]) {
                return false;
            }
            i++;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rights)) {
            return false;
        }
        Rights rights2 = (Rights) obj;
        int i = 0;
        while (true) {
            boolean[] zArr = rights2.rights;
            if (i >= zArr.length) {
                return true;
            }
            if (zArr[i] != this.rights[i]) {
                return false;
            }
            i++;
        }
    }

    public int hashCode() {
        int hash = 0;
        int i = 0;
        while (true) {
            boolean[] zArr = this.rights;
            if (i >= zArr.length) {
                return hash;
            }
            if (zArr[i]) {
                hash++;
            }
            i++;
        }
    }

    public Right[] getRights() {
        Vector v = new Vector();
        int i = 0;
        while (true) {
            boolean[] zArr = this.rights;
            if (i >= zArr.length) {
                Right[] rights2 = new Right[v.size()];
                v.copyInto(rights2);
                return rights2;
            }
            if (zArr[i]) {
                v.addElement(Right.getInstance((char) i));
            }
            i++;
        }
    }

    public Object clone() {
        Rights r = null;
        try {
            r = (Rights) super.clone();
            boolean[] zArr = new boolean[128];
            r.rights = zArr;
            System.arraycopy(this.rights, 0, zArr, 0, this.rights.length);
            return r;
        } catch (CloneNotSupportedException e) {
            return r;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (true) {
            boolean[] zArr = this.rights;
            if (i >= zArr.length) {
                return sb.toString();
            }
            if (zArr[i]) {
                sb.append((char) i);
            }
            i++;
        }
    }
}
