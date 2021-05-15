package com.sec.internal.helper.os;

import android.os.Parcel;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.SignalStrength;
import com.sec.ims.extensions.ReflectionUtils;
import java.util.Iterator;
import java.util.List;

public class SignalStrengthWrapper {
    private final SignalStrength mSignalStrength;

    public SignalStrengthWrapper(SignalStrength signalStrength) {
        this.mSignalStrength = signalStrength;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mSignalStrength.writeToParcel(out, flags);
    }

    public int getEvdoEcio() {
        return this.mSignalStrength.getEvdoEcio();
    }

    public boolean isGsm() {
        return this.mSignalStrength.isGsm();
    }

    public int getEvdoSnr() {
        return this.mSignalStrength.getEvdoSnr();
    }

    public int describeContents() {
        return this.mSignalStrength.describeContents();
    }

    public int getGsmSignalStrength() {
        return this.mSignalStrength.getGsmSignalStrength();
    }

    public int getGsmBitErrorRate() {
        return this.mSignalStrength.getGsmBitErrorRate();
    }

    public int getLevel() {
        return this.mSignalStrength.getLevel();
    }

    public int getCdmaEcio() {
        return this.mSignalStrength.getCdmaEcio();
    }

    public int getCdmaDbm() {
        return this.mSignalStrength.getCdmaDbm();
    }

    public int getEvdoDbm() {
        return this.mSignalStrength.getEvdoDbm();
    }

    public String toString() {
        return this.mSignalStrength.toString();
    }

    public int getDbm(int networkType) {
        CellSignalStrength cellSignalStrength = null;
        try {
            List<CellSignalStrength> cellSignalStrengthsList = (List) ReflectionUtils.invoke2(SignalStrength.class.getMethod("getCellSignalStrengths", new Class[0]), this.mSignalStrength, new Object[0]);
            if (cellSignalStrengthsList != null && cellSignalStrengthsList.size() > 0) {
                Iterator<CellSignalStrength> it = cellSignalStrengthsList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    CellSignalStrength css = it.next();
                    if (networkType != 13 || !(css instanceof CellSignalStrengthLte)) {
                        if (networkType != 4 || !(css instanceof CellSignalStrengthCdma)) {
                            if (networkType != 17 || !(css instanceof CellSignalStrengthTdscdma)) {
                                if (networkType != 16 || !(css instanceof CellSignalStrengthGsm)) {
                                    if (networkType == 20 && (css instanceof CellSignalStrengthNr)) {
                                        cellSignalStrength = (CellSignalStrengthNr) css;
                                        break;
                                    }
                                } else {
                                    cellSignalStrength = (CellSignalStrengthGsm) css;
                                    break;
                                }
                            } else {
                                cellSignalStrength = (CellSignalStrengthTdscdma) css;
                                break;
                            }
                        } else {
                            cellSignalStrength = (CellSignalStrengthCdma) css;
                            break;
                        }
                    } else {
                        cellSignalStrength = (CellSignalStrengthLte) css;
                        break;
                    }
                }
            }
            if (cellSignalStrength == null) {
                return 0;
            }
            return cellSignalStrength.getDbm();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getLteRsrp() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthLte     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthLte r4 = (android.telephony.CellSignalStrengthLte) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getRsrp()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getLteRsrp():int");
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getLteRsrq() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthLte     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthLte r4 = (android.telephony.CellSignalStrengthLte) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getRsrq()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getLteRsrq():int");
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getNrSsRsrp() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthNr     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthNr r4 = (android.telephony.CellSignalStrengthNr) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getSsRsrp()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getNrSsRsrp():int");
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getNrSsRsrq() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthNr     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthNr r4 = (android.telephony.CellSignalStrengthNr) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getSsRsrq()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getNrSsRsrq():int");
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getNrSsSinr() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthNr     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthNr r4 = (android.telephony.CellSignalStrengthNr) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getSsSinr()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getNrSsSinr():int");
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getLteLevel() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthLte     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthLte r4 = (android.telephony.CellSignalStrengthLte) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getLevel()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getLteLevel():int");
    }

    /* JADX WARNING: type inference failed for: r5v2, types: [android.telephony.CellSignalStrength] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getNrLevel() {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            java.lang.Class<android.telephony.SignalStrength> r2 = android.telephony.SignalStrength.class
            java.lang.String r3 = "getCellSignalStrengths"
            java.lang.Class[] r4 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.reflect.Method r2 = r2.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.SignalStrength r3 = r7.mSignalStrength     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object[] r4 = new java.lang.Object[r1]     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.lang.Object r3 = com.sec.ims.extensions.ReflectionUtils.invoke2(r2, r3, r4)     // Catch:{ NoSuchMethodException -> 0x0040 }
            java.util.List r3 = (java.util.List) r3     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r3 == 0) goto L_0x0038
            int r4 = r3.size()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r4 <= 0) goto L_0x0038
            java.util.Iterator r4 = r3.iterator()     // Catch:{ NoSuchMethodException -> 0x0040 }
        L_0x0022:
            boolean r5 = r4.hasNext()     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r5 == 0) goto L_0x0038
            java.lang.Object r5 = r4.next()     // Catch:{ NoSuchMethodException -> 0x0040 }
            android.telephony.CellSignalStrength r5 = (android.telephony.CellSignalStrength) r5     // Catch:{ NoSuchMethodException -> 0x0040 }
            boolean r6 = r5 instanceof android.telephony.CellSignalStrengthNr     // Catch:{ NoSuchMethodException -> 0x0040 }
            if (r6 == 0) goto L_0x0037
            r4 = r5
            android.telephony.CellSignalStrengthNr r4 = (android.telephony.CellSignalStrengthNr) r4     // Catch:{ NoSuchMethodException -> 0x0040 }
            r0 = r4
            goto L_0x0038
        L_0x0037:
            goto L_0x0022
        L_0x0038:
            if (r0 != 0) goto L_0x003b
            return r1
        L_0x003b:
            int r1 = r0.getLevel()     // Catch:{ NoSuchMethodException -> 0x0040 }
            return r1
        L_0x0040:
            r0 = move-exception
            r0.printStackTrace()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.SignalStrengthWrapper.getNrLevel():int");
    }

    public int getInvalidSignalStrength() {
        Integer inValidSignal = 0;
        try {
            inValidSignal = (Integer) ReflectionUtils.getValueOf("SIGNAL_STRENGTH_NONE_OR_UNKNOWN", SignalStrength.class);
            if (inValidSignal == null) {
                inValidSignal = 0;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return inValidSignal.intValue();
    }

    public boolean isValidSignal() {
        Integer inValidSignal = 0;
        try {
            inValidSignal = (Integer) ReflectionUtils.getValueOf("SIGNAL_STRENGTH_NONE_OR_UNKNOWN", SignalStrength.class);
            if (inValidSignal == null) {
                inValidSignal = 0;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        if (inValidSignal.equals(Integer.valueOf(getLteLevel()))) {
            return true;
        }
        return false;
    }
}
