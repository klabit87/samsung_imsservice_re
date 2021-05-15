package com.google.android.gms.internal;

import java.util.List;

final class zzfjf {
    static String zza(zzfjc zzfjc, String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ");
        sb.append(str);
        zza(zzfjc, sb, 0);
        return sb.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:62:0x018e, code lost:
        if (((java.lang.Boolean) r9).booleanValue() == false) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01a0, code lost:
        if (((java.lang.Integer) r9).intValue() == 0) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01b1, code lost:
        if (((java.lang.Float) r9).floatValue() == 0.0f) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01c3, code lost:
        if (((java.lang.Double) r9).doubleValue() == 0.0d) goto L_0x0190;
     */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01f9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void zza(com.google.android.gms.internal.zzfjc r12, java.lang.StringBuilder r13, int r14) {
        /*
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            java.util.TreeSet r2 = new java.util.TreeSet
            r2.<init>()
            java.lang.Class r3 = r12.getClass()
            java.lang.reflect.Method[] r3 = r3.getDeclaredMethods()
            int r4 = r3.length
            r5 = 0
            r6 = r5
        L_0x001a:
            java.lang.String r7 = "get"
            if (r6 >= r4) goto L_0x0049
            r8 = r3[r6]
            java.lang.String r9 = r8.getName()
            r1.put(r9, r8)
            java.lang.Class[] r9 = r8.getParameterTypes()
            int r9 = r9.length
            if (r9 != 0) goto L_0x0046
            java.lang.String r9 = r8.getName()
            r0.put(r9, r8)
            java.lang.String r9 = r8.getName()
            boolean r7 = r9.startsWith(r7)
            if (r7 == 0) goto L_0x0046
            java.lang.String r7 = r8.getName()
            r2.add(r7)
        L_0x0046:
            int r6 = r6 + 1
            goto L_0x001a
        L_0x0049:
            java.util.Iterator r2 = r2.iterator()
        L_0x004d:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0212
            java.lang.Object r3 = r2.next()
            java.lang.String r3 = (java.lang.String) r3
            java.lang.String r4 = ""
            java.lang.String r3 = r3.replaceFirst(r7, r4)
            java.lang.String r6 = "List"
            boolean r6 = r3.endsWith(r6)
            r8 = 1
            if (r6 == 0) goto L_0x00d2
            java.lang.String r6 = "OrBuilderList"
            boolean r6 = r3.endsWith(r6)
            if (r6 != 0) goto L_0x00d2
            java.lang.String r6 = r3.substring(r5, r8)
            java.lang.String r6 = r6.toLowerCase()
            java.lang.String r6 = java.lang.String.valueOf(r6)
            int r9 = r3.length()
            int r9 = r9 + -4
            java.lang.String r9 = r3.substring(r8, r9)
            java.lang.String r9 = java.lang.String.valueOf(r9)
            int r10 = r9.length()
            if (r10 == 0) goto L_0x0095
            java.lang.String r6 = r6.concat(r9)
            goto L_0x009b
        L_0x0095:
            java.lang.String r9 = new java.lang.String
            r9.<init>(r6)
            r6 = r9
        L_0x009b:
            java.lang.String r9 = java.lang.String.valueOf(r3)
            int r10 = r9.length()
            if (r10 == 0) goto L_0x00aa
            java.lang.String r9 = r7.concat(r9)
            goto L_0x00af
        L_0x00aa:
            java.lang.String r9 = new java.lang.String
            r9.<init>(r7)
        L_0x00af:
            java.lang.Object r9 = r0.get(r9)
            java.lang.reflect.Method r9 = (java.lang.reflect.Method) r9
            if (r9 == 0) goto L_0x00d2
            java.lang.Class r10 = r9.getReturnType()
            java.lang.Class<java.util.List> r11 = java.util.List.class
            boolean r10 = r10.equals(r11)
            if (r10 == 0) goto L_0x00d2
            java.lang.String r3 = zztz(r6)
            java.lang.Object[] r4 = new java.lang.Object[r5]
            java.lang.Object r4 = com.google.android.gms.internal.zzfhu.zza((java.lang.reflect.Method) r9, (java.lang.Object) r12, (java.lang.Object[]) r4)
            zzb(r13, r14, r3, r4)
            goto L_0x004d
        L_0x00d2:
            java.lang.String r6 = "set"
            java.lang.String r9 = java.lang.String.valueOf(r3)
            int r10 = r9.length()
            if (r10 == 0) goto L_0x00e4
            java.lang.String r6 = r6.concat(r9)
            goto L_0x00ea
        L_0x00e4:
            java.lang.String r9 = new java.lang.String
            r9.<init>(r6)
            r6 = r9
        L_0x00ea:
            java.lang.Object r6 = r1.get(r6)
            java.lang.reflect.Method r6 = (java.lang.reflect.Method) r6
            if (r6 == 0) goto L_0x004d
            java.lang.String r6 = "Bytes"
            boolean r6 = r3.endsWith(r6)
            if (r6 == 0) goto L_0x011e
            int r6 = r3.length()
            int r6 = r6 + -5
            java.lang.String r6 = r3.substring(r5, r6)
            java.lang.String r6 = java.lang.String.valueOf(r6)
            int r9 = r6.length()
            if (r9 == 0) goto L_0x0113
            java.lang.String r6 = r7.concat(r6)
            goto L_0x0118
        L_0x0113:
            java.lang.String r6 = new java.lang.String
            r6.<init>(r7)
        L_0x0118:
            boolean r6 = r0.containsKey(r6)
            if (r6 != 0) goto L_0x004d
        L_0x011e:
            java.lang.String r6 = r3.substring(r5, r8)
            java.lang.String r6 = r6.toLowerCase()
            java.lang.String r6 = java.lang.String.valueOf(r6)
            java.lang.String r9 = r3.substring(r8)
            java.lang.String r9 = java.lang.String.valueOf(r9)
            int r10 = r9.length()
            if (r10 == 0) goto L_0x013d
            java.lang.String r6 = r6.concat(r9)
            goto L_0x0143
        L_0x013d:
            java.lang.String r9 = new java.lang.String
            r9.<init>(r6)
            r6 = r9
        L_0x0143:
            java.lang.String r9 = java.lang.String.valueOf(r3)
            int r10 = r9.length()
            if (r10 == 0) goto L_0x0152
            java.lang.String r9 = r7.concat(r9)
            goto L_0x0157
        L_0x0152:
            java.lang.String r9 = new java.lang.String
            r9.<init>(r7)
        L_0x0157:
            java.lang.Object r9 = r0.get(r9)
            java.lang.reflect.Method r9 = (java.lang.reflect.Method) r9
            java.lang.String r10 = "has"
            java.lang.String r3 = java.lang.String.valueOf(r3)
            int r11 = r3.length()
            if (r11 == 0) goto L_0x016e
            java.lang.String r3 = r10.concat(r3)
            goto L_0x0173
        L_0x016e:
            java.lang.String r3 = new java.lang.String
            r3.<init>(r10)
        L_0x0173:
            java.lang.Object r3 = r0.get(r3)
            java.lang.reflect.Method r3 = (java.lang.reflect.Method) r3
            if (r9 == 0) goto L_0x004d
            java.lang.Object[] r10 = new java.lang.Object[r5]
            java.lang.Object r9 = com.google.android.gms.internal.zzfhu.zza((java.lang.reflect.Method) r9, (java.lang.Object) r12, (java.lang.Object[]) r10)
            if (r3 != 0) goto L_0x01fb
            boolean r3 = r9 instanceof java.lang.Boolean
            if (r3 == 0) goto L_0x0195
            r3 = r9
            java.lang.Boolean r3 = (java.lang.Boolean) r3
            boolean r3 = r3.booleanValue()
            if (r3 != 0) goto L_0x0193
        L_0x0190:
            r3 = r8
            goto L_0x01f6
        L_0x0193:
            r3 = r5
            goto L_0x01f6
        L_0x0195:
            boolean r3 = r9 instanceof java.lang.Integer
            if (r3 == 0) goto L_0x01a3
            r3 = r9
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            if (r3 != 0) goto L_0x0193
            goto L_0x0190
        L_0x01a3:
            boolean r3 = r9 instanceof java.lang.Float
            if (r3 == 0) goto L_0x01b4
            r3 = r9
            java.lang.Float r3 = (java.lang.Float) r3
            float r3 = r3.floatValue()
            r4 = 0
            int r3 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1))
            if (r3 != 0) goto L_0x0193
            goto L_0x0190
        L_0x01b4:
            boolean r3 = r9 instanceof java.lang.Double
            if (r3 == 0) goto L_0x01c6
            r3 = r9
            java.lang.Double r3 = (java.lang.Double) r3
            double r3 = r3.doubleValue()
            r10 = 0
            int r3 = (r3 > r10 ? 1 : (r3 == r10 ? 0 : -1))
            if (r3 != 0) goto L_0x0193
            goto L_0x0190
        L_0x01c6:
            boolean r3 = r9 instanceof java.lang.String
            if (r3 == 0) goto L_0x01cf
            boolean r3 = r9.equals(r4)
            goto L_0x01f6
        L_0x01cf:
            boolean r3 = r9 instanceof com.google.android.gms.internal.zzfgs
            if (r3 == 0) goto L_0x01da
            com.google.android.gms.internal.zzfgs r3 = com.google.android.gms.internal.zzfgs.zzpnw
            boolean r3 = r9.equals(r3)
            goto L_0x01f6
        L_0x01da:
            boolean r3 = r9 instanceof com.google.android.gms.internal.zzfjc
            if (r3 == 0) goto L_0x01e8
            r3 = r9
            com.google.android.gms.internal.zzfjc r3 = (com.google.android.gms.internal.zzfjc) r3
            com.google.android.gms.internal.zzfjc r3 = r3.zzczu()
            if (r9 != r3) goto L_0x0193
            goto L_0x0190
        L_0x01e8:
            boolean r3 = r9 instanceof java.lang.Enum
            if (r3 == 0) goto L_0x0193
            r3 = r9
            java.lang.Enum r3 = (java.lang.Enum) r3
            int r3 = r3.ordinal()
            if (r3 != 0) goto L_0x0193
            goto L_0x0190
        L_0x01f6:
            if (r3 != 0) goto L_0x01f9
            goto L_0x0207
        L_0x01f9:
            r8 = r5
            goto L_0x0207
        L_0x01fb:
            java.lang.Object[] r4 = new java.lang.Object[r5]
            java.lang.Object r3 = com.google.android.gms.internal.zzfhu.zza((java.lang.reflect.Method) r3, (java.lang.Object) r12, (java.lang.Object[]) r4)
            java.lang.Boolean r3 = (java.lang.Boolean) r3
            boolean r8 = r3.booleanValue()
        L_0x0207:
            if (r8 == 0) goto L_0x004d
            java.lang.String r3 = zztz(r6)
            zzb(r13, r14, r3, r9)
            goto L_0x004d
        L_0x0212:
            boolean r0 = r12 instanceof com.google.android.gms.internal.zzfhu.zzd
            if (r0 == 0) goto L_0x0235
            r0 = r12
            com.google.android.gms.internal.zzfhu$zzd r0 = (com.google.android.gms.internal.zzfhu.zzd) r0
            com.google.android.gms.internal.zzfhq<java.lang.Object> r0 = r0.zzppp
            java.util.Iterator r0 = r0.iterator()
            boolean r1 = r0.hasNext()
            if (r1 != 0) goto L_0x0226
            goto L_0x0235
        L_0x0226:
            java.lang.Object r12 = r0.next()
            java.util.Map$Entry r12 = (java.util.Map.Entry) r12
            r12.getKey()
            java.lang.NoSuchMethodError r12 = new java.lang.NoSuchMethodError
            r12.<init>()
            throw r12
        L_0x0235:
            com.google.android.gms.internal.zzfhu r12 = (com.google.android.gms.internal.zzfhu) r12
            com.google.android.gms.internal.zzfko r0 = r12.zzpph
            if (r0 == 0) goto L_0x0240
            com.google.android.gms.internal.zzfko r12 = r12.zzpph
            r12.zzd(r13, r14)
        L_0x0240:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfjf.zza(com.google.android.gms.internal.zzfjc, java.lang.StringBuilder, int):void");
    }

    static final void zzb(StringBuilder sb, int i, String str, Object obj) {
        if (obj instanceof List) {
            for (Object zzb : (List) obj) {
                zzb(sb, i, str, zzb);
            }
            return;
        }
        sb.append(10);
        for (int i2 = 0; i2 < i; i2++) {
            sb.append(' ');
        }
        sb.append(str);
        if (obj instanceof String) {
            sb.append(": \"");
            sb.append(zzfkh.zzbd(zzfgs.zztv((String) obj)));
            sb.append('\"');
        } else if (obj instanceof zzfgs) {
            sb.append(": \"");
            sb.append(zzfkh.zzbd((zzfgs) obj));
            sb.append('\"');
        } else if (obj instanceof zzfhu) {
            sb.append(" {");
            zza((zzfhu) obj, sb, i + 2);
            sb.append("\n");
            for (int i3 = 0; i3 < i; i3++) {
                sb.append(' ');
            }
            sb.append("}");
        } else {
            sb.append(": ");
            sb.append(obj.toString());
        }
    }

    private static final String zztz(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (Character.isUpperCase(charAt)) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(charAt));
        }
        return sb.toString();
    }
}
