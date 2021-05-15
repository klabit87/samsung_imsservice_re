package com.sec.internal.ims.xq.att.data;

import java.util.ArrayList;

public class XqEvent {
    private ArrayList<XqContent> mContent = new ArrayList<>();
    private XqMtrips mtrips;

    public enum XqContentType {
        UNDEFINED,
        UCHAR,
        USHORT,
        UINT32,
        STRING
    }

    public enum XqMtrips {
        UNDEFINED(0),
        M01(1),
        M02(2),
        M03(3),
        M04(4),
        M05(5),
        M06(6),
        SPTX(100),
        SPRX(101);
        
        private final int value;

        private XqMtrips(int value2) {
            this.value = value2;
        }

        public static XqMtrips castToType(int val) {
            for (XqMtrips i : values()) {
                if (i.getValue() == val) {
                    return i;
                }
            }
            return UNDEFINED;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static class XqContent {
        int intVal;
        String strVal;
        XqContentType type;

        XqContent(int type2, int intVal2, String strVal2) {
            if (type2 == 1) {
                this.type = XqContentType.UCHAR;
            } else if (type2 == 2) {
                this.type = XqContentType.USHORT;
            } else if (type2 == 3) {
                this.type = XqContentType.UINT32;
            } else if (type2 != 4) {
                this.type = XqContentType.UNDEFINED;
            } else {
                this.type = XqContentType.STRING;
            }
            this.intVal = intVal2;
            this.strVal = strVal2;
        }

        public boolean hasStrVal() {
            return this.strVal != null;
        }

        public String getStrVal() {
            return this.strVal;
        }

        public boolean hasIntVal() {
            return this.intVal >= 0;
        }

        public int getIntVal() {
            return this.intVal;
        }

        public XqContentType getType() {
            return this.type;
        }
    }

    public void setXqMtrips(int value) {
        this.mtrips = XqMtrips.castToType(value);
    }

    public XqMtrips getMtrip() {
        return this.mtrips;
    }

    public void setContent(int type, int intVal, String strVal) {
        this.mContent.add(new XqContent(type, intVal, strVal));
    }

    public ArrayList<XqContent> getMContentList() {
        return this.mContent;
    }

    public XqContent getMContent(int idx) {
        return this.mContent.get(idx);
    }
}
