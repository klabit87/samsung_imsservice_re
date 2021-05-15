package com.google.android.gms.internal;

final class zzfkh {
    static String zzbd(zzfgs zzfgs) {
        String str;
        zzfki zzfki = new zzfki(zzfgs);
        StringBuilder sb = new StringBuilder(zzfki.size());
        for (int i = 0; i < zzfki.size(); i++) {
            int zzld = zzfki.zzld(i);
            if (zzld == 34) {
                str = "\\\"";
            } else if (zzld == 39) {
                str = "\\'";
            } else if (zzld != 92) {
                switch (zzld) {
                    case 7:
                        str = "\\a";
                        break;
                    case 8:
                        str = "\\b";
                        break;
                    case 9:
                        str = "\\t";
                        break;
                    case 10:
                        str = "\\n";
                        break;
                    case 11:
                        str = "\\v";
                        break;
                    case 12:
                        str = "\\f";
                        break;
                    case 13:
                        str = "\\r";
                        break;
                    default:
                        if (zzld < 32 || zzld > 126) {
                            sb.append('\\');
                            sb.append((char) (((zzld >>> 6) & 3) + 48));
                            sb.append((char) (((zzld >>> 3) & 7) + 48));
                            zzld = (zzld & 7) + 48;
                        }
                        sb.append((char) zzld);
                        continue;
                }
            } else {
                str = "\\\\";
            }
            sb.append(str);
        }
        return sb.toString();
    }
}
