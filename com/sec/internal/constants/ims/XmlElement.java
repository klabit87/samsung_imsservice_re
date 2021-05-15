package com.sec.internal.constants.ims;

import java.util.ArrayList;
import java.util.List;

public class XmlElement {
    public List<Attribute> mAttributes;
    public List<XmlElement> mChildElements;
    public String mName;
    public String mNamespace;
    public String mValue;

    public static class Attribute {
        public String mName;
        public String mNamespace;
        public String mValue;

        public Attribute(String name, String value) {
            this.mName = name;
            this.mValue = value;
            this.mNamespace = null;
        }

        public Attribute(String name, String value, String ns) {
            this.mName = name;
            this.mValue = value;
            this.mNamespace = ns;
        }

        public int hashCode() {
            int i = 1 * 31;
            String str = this.mName;
            int i2 = 0;
            int result = (i + (str == null ? 0 : str.hashCode())) * 31;
            String str2 = this.mValue;
            int result2 = (result + (str2 == null ? 0 : str2.hashCode())) * 31;
            String str3 = this.mNamespace;
            if (str3 != null) {
                i2 = str3.hashCode();
            }
            return result2 + i2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Attribute other = (Attribute) obj;
            String str = this.mName;
            if (str == null) {
                if (other.mName != null) {
                    return false;
                }
            } else if (!str.equals(other.mName)) {
                return false;
            }
            String str2 = this.mValue;
            if (str2 == null) {
                if (other.mValue != null) {
                    return false;
                }
            } else if (!str2.equals(other.mValue)) {
                return false;
            }
            String str3 = this.mNamespace;
            if (str3 == null) {
                if (other.mNamespace != null) {
                    return false;
                }
            } else if (!str3.equals(other.mNamespace)) {
                return false;
            }
            return true;
        }
    }

    public XmlElement(String name) {
        this.mName = name;
        this.mValue = null;
        this.mNamespace = null;
        this.mAttributes = new ArrayList();
        this.mChildElements = new ArrayList();
    }

    public XmlElement(String name, String value) {
        this(name);
        this.mValue = value;
    }

    public XmlElement(String name, String value, String ns) {
        this(name, value);
        this.mNamespace = ns;
    }

    public XmlElement setValue(String value) {
        this.mValue = value;
        return this;
    }

    public XmlElement setNamespace(String ns) {
        this.mNamespace = ns;
        return this;
    }

    public XmlElement addAttribute(String name, String value) {
        if (value != null) {
            this.mAttributes.add(new Attribute(name, value));
        }
        return this;
    }

    public XmlElement addAttribute(String name, String value, String ns) {
        if (value != null) {
            this.mAttributes.add(new Attribute(name, value, ns));
        }
        return this;
    }

    public XmlElement addChildElement(XmlElement element) {
        this.mChildElements.add(element);
        return this;
    }

    public XmlElement addChildElements(List<XmlElement> elements) {
        this.mChildElements.addAll(elements);
        return this;
    }

    public XmlElement setParent(XmlElement element) {
        return element.addChildElement(this);
    }

    public int hashCode() {
        int i = 1 * 31;
        String str = this.mName;
        int i2 = 0;
        int result = (i + (str == null ? 0 : str.hashCode())) * 31;
        String str2 = this.mValue;
        int result2 = (result + (str2 == null ? 0 : str2.hashCode())) * 31;
        String str3 = this.mNamespace;
        int result3 = (result2 + (str3 == null ? 0 : str3.hashCode())) * 31;
        List<Attribute> list = this.mAttributes;
        int result4 = (result3 + (list == null ? 0 : list.hashCode())) * 31;
        List<XmlElement> list2 = this.mChildElements;
        if (list2 != null) {
            i2 = list2.hashCode();
        }
        return result4 + i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        XmlElement other = (XmlElement) obj;
        String str = this.mName;
        if (str == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!str.equals(other.mName)) {
            return false;
        }
        String str2 = this.mValue;
        if (str2 == null) {
            if (other.mValue != null) {
                return false;
            }
        } else if (!str2.equals(other.mValue)) {
            return false;
        }
        String str3 = this.mNamespace;
        if (str3 == null) {
            if (other.mNamespace != null) {
                return false;
            }
        } else if (!str3.equals(other.mNamespace)) {
            return false;
        }
        List<Attribute> list = this.mAttributes;
        if (list == null) {
            if (other.mAttributes != null) {
                return false;
            }
        } else if (!list.equals(other.mAttributes)) {
            return false;
        }
        List<XmlElement> list2 = this.mChildElements;
        if (list2 == null) {
            if (other.mChildElements != null) {
                return false;
            }
        } else if (!list2.equals(other.mChildElements)) {
            return false;
        }
        return true;
    }
}
