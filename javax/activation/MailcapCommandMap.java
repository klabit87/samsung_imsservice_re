package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MailcapFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MailcapCommandMap extends CommandMap {
    private static final int PROG = 0;
    private static MailcapFile defDB = null;
    private MailcapFile[] DB;

    public MailcapCommandMap() {
        List dbv = new ArrayList(5);
        dbv.add((Object) null);
        LogSupport.log("MailcapCommandMap: load HOME");
        try {
            String user_home = System.getProperty("user.home");
            if (user_home != null) {
                MailcapFile mf = loadFile(String.valueOf(user_home) + File.separator + ".mailcap");
                if (mf != null) {
                    dbv.add(mf);
                }
            }
        } catch (SecurityException e) {
        }
        LogSupport.log("MailcapCommandMap: load SYS");
        try {
            MailcapFile mf2 = loadFile(String.valueOf(System.getProperty("java.home")) + File.separator + "lib" + File.separator + "mailcap");
            if (mf2 != null) {
                dbv.add(mf2);
            }
        } catch (SecurityException e2) {
        }
        LogSupport.log("MailcapCommandMap: load JAR");
        loadAllResources(dbv, "mailcap");
        LogSupport.log("MailcapCommandMap: load DEF");
        synchronized (MailcapCommandMap.class) {
            if (defDB == null) {
                defDB = loadResource("mailcap.default");
            }
        }
        MailcapFile mailcapFile = defDB;
        if (mailcapFile != null) {
            dbv.add(mailcapFile);
        }
        MailcapFile[] mailcapFileArr = new MailcapFile[dbv.size()];
        this.DB = mailcapFileArr;
        this.DB = (MailcapFile[]) dbv.toArray(mailcapFileArr);
    }

    private MailcapFile loadResource(String name) {
        InputStream clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(getClass(), name);
            if (clis != null) {
                MailcapFile mailcapFile = new MailcapFile(clis);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: successfully loaded mailcap file: " + name);
                }
                if (clis != null) {
                    try {
                        clis.close();
                    } catch (IOException e) {
                    }
                }
                return mailcapFile;
            }
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: not loading mailcap file: " + name);
            }
            if (clis == null) {
                return null;
            }
            try {
                clis.close();
                return null;
            } catch (IOException e2) {
                return null;
            }
        } catch (IOException e3) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, e3);
            }
            if (clis == null) {
                return null;
            }
            clis.close();
            return null;
        } catch (SecurityException sex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, sex);
            }
            if (clis == null) {
                return null;
            }
            clis.close();
            return null;
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    private void loadAllResources(List v, String name) {
        URL[] urls;
        InputStream clis;
        boolean anyLoaded = false;
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            if (cld != null) {
                urls = SecuritySupport.getResources(cld, name);
            } else {
                urls = SecuritySupport.getSystemResources(name);
            }
            if (urls != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: getResources");
                }
                for (URL url : urls) {
                    clis = null;
                    if (LogSupport.isLoggable()) {
                        LogSupport.log("MailcapCommandMap: URL " + url);
                    }
                    try {
                        InputStream clis2 = SecuritySupport.openStream(url);
                        if (clis2 != null) {
                            v.add(new MailcapFile(clis2));
                            anyLoaded = true;
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MailcapCommandMap: successfully loaded mailcap file from URL: " + url);
                            }
                        } else if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: not loading mailcap file from URL: " + url);
                        }
                        if (clis2 != null) {
                            try {
                                clis2.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException ioex) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: can't load " + url, ioex);
                        }
                        if (clis != null) {
                            clis.close();
                        }
                    } catch (SecurityException sex) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: can't load " + url, sex);
                        }
                        if (clis != null) {
                            clis.close();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, ex);
            }
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        if (!anyLoaded) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: !anyLoaded");
            }
            MailcapFile mf = loadResource("/" + name);
            if (mf != null) {
                v.add(mf);
            }
        }
    }

    private MailcapFile loadFile(String name) {
        try {
            return new MailcapFile(name);
        } catch (IOException e) {
            return null;
        }
    }

    public MailcapCommandMap(String fileName) throws IOException {
        this();
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: load PROG from " + fileName);
        }
        MailcapFile[] mailcapFileArr = this.DB;
        if (mailcapFileArr[0] == null) {
            mailcapFileArr[0] = new MailcapFile(fileName);
        }
    }

    public MailcapCommandMap(InputStream is) {
        this();
        LogSupport.log("MailcapCommandMap: load PROG");
        MailcapFile[] mailcapFileArr = this.DB;
        if (mailcapFileArr[0] == null) {
            try {
                mailcapFileArr[0] = new MailcapFile(is);
            } catch (IOException e) {
            }
        }
    }

    public synchronized CommandInfo[] getPreferredCommands(String mimeType) {
        List cmdList;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; i++) {
            if (this.DB[i] != null) {
                Map cmdMap = this.DB[i].getMailcapList(mimeType);
                if (cmdMap != null) {
                    appendPrefCmdsToList(cmdMap, cmdList);
                }
            }
        }
        for (int i2 = 0; i2 < this.DB.length; i2++) {
            if (this.DB[i2] != null) {
                Map cmdMap2 = this.DB[i2].getMailcapFallbackList(mimeType);
                if (cmdMap2 != null) {
                    appendPrefCmdsToList(cmdMap2, cmdList);
                }
            }
        }
        return (CommandInfo[]) cmdList.toArray(new CommandInfo[cmdList.size()]);
    }

    private void appendPrefCmdsToList(Map cmdHash, List cmdList) {
        for (String verb : cmdHash.keySet()) {
            if (!checkForVerb(cmdList, verb)) {
                cmdList.add(new CommandInfo(verb, (String) ((List) cmdHash.get(verb)).get(0)));
            }
        }
    }

    private boolean checkForVerb(List cmdList, String verb) {
        Iterator ee = cmdList.iterator();
        while (ee.hasNext()) {
            if (((CommandInfo) ee.next()).getCommandName().equals(verb)) {
                return true;
            }
        }
        return false;
    }

    public synchronized CommandInfo[] getAllCommands(String mimeType) {
        List cmdList;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; i++) {
            if (this.DB[i] != null) {
                Map cmdMap = this.DB[i].getMailcapList(mimeType);
                if (cmdMap != null) {
                    appendCmdsToList(cmdMap, cmdList);
                }
            }
        }
        for (int i2 = 0; i2 < this.DB.length; i2++) {
            if (this.DB[i2] != null) {
                Map cmdMap2 = this.DB[i2].getMailcapFallbackList(mimeType);
                if (cmdMap2 != null) {
                    appendCmdsToList(cmdMap2, cmdList);
                }
            }
        }
        return (CommandInfo[]) cmdList.toArray(new CommandInfo[cmdList.size()]);
    }

    private void appendCmdsToList(Map typeHash, List cmdList) {
        for (String verb : typeHash.keySet()) {
            for (String cmd : (List) typeHash.get(verb)) {
                cmdList.add(new CommandInfo(verb, cmd));
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    public synchronized CommandInfo getCommand(String mimeType, String cmdName) {
        List v;
        String cmdClassName;
        List v2;
        String cmdClassName2;
        if (mimeType != null) {
            try {
                mimeType = mimeType.toLowerCase(Locale.ENGLISH);
            } catch (Throwable th) {
                throw th;
            }
        }
        for (int i = 0; i < this.DB.length; i++) {
            if (this.DB[i] != null) {
                Map cmdMap = this.DB[i].getMailcapList(mimeType);
                if (!(cmdMap == null || (v2 = (List) cmdMap.get(cmdName)) == null || (cmdClassName2 = (String) v2.get(0)) == null)) {
                    return new CommandInfo(cmdName, cmdClassName2);
                }
            }
        }
        for (int i2 = 0; i2 < this.DB.length; i2++) {
            if (this.DB[i2] != null) {
                Map cmdMap2 = this.DB[i2].getMailcapFallbackList(mimeType);
                if (!(cmdMap2 == null || (v = (List) cmdMap2.get(cmdName)) == null || (cmdClassName = (String) v.get(0)) == null)) {
                    return new CommandInfo(cmdName, cmdClassName);
                }
            }
        }
        return null;
    }

    public synchronized void addMailcap(String mail_cap) {
        LogSupport.log("MailcapCommandMap: add to PROG");
        if (this.DB[0] == null) {
            this.DB[0] = new MailcapFile();
        }
        this.DB[0].appendToMailcap(mail_cap);
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    public synchronized DataContentHandler createDataContentHandler(String mimeType) {
        List v;
        DataContentHandler dch;
        List v2;
        DataContentHandler dch2;
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: createDataContentHandler for " + mimeType);
        }
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; i++) {
            if (this.DB[i] != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("  search DB #" + i);
                }
                Map cmdMap = this.DB[i].getMailcapList(mimeType);
                if (!(cmdMap == null || (v2 = (List) cmdMap.get("content-handler")) == null || (dch2 = getDataContentHandler((String) v2.get(0))) == null)) {
                    return dch2;
                }
            }
        }
        for (int i2 = 0; i2 < this.DB.length; i2++) {
            if (this.DB[i2] != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("  search fallback DB #" + i2);
                }
                Map cmdMap2 = this.DB[i2].getMailcapFallbackList(mimeType);
                if (!(cmdMap2 == null || (v = (List) cmdMap2.get("content-handler")) == null || (dch = getDataContentHandler((String) v.get(0))) == null)) {
                    return dch;
                }
            }
        }
        return null;
    }

    private DataContentHandler getDataContentHandler(String name) {
        Class cl;
        if (LogSupport.isLoggable()) {
            LogSupport.log("    got content-handler");
        }
        if (LogSupport.isLoggable()) {
            LogSupport.log("      class " + name);
        }
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            try {
                cl = cld.loadClass(name);
            } catch (Exception e) {
                cl = Class.forName(name);
            }
            if (cl != null) {
                return (DataContentHandler) cl.newInstance();
            }
            return null;
        } catch (IllegalAccessException e2) {
            if (!LogSupport.isLoggable()) {
                return null;
            }
            LogSupport.log("Can't load DCH " + name, e2);
            return null;
        } catch (ClassNotFoundException e3) {
            if (!LogSupport.isLoggable()) {
                return null;
            }
            LogSupport.log("Can't load DCH " + name, e3);
            return null;
        } catch (InstantiationException e4) {
            if (!LogSupport.isLoggable()) {
                return null;
            }
            LogSupport.log("Can't load DCH " + name, e4);
            return null;
        }
    }

    public synchronized String[] getMimeTypes() {
        List mtList;
        mtList = new ArrayList();
        for (int i = 0; i < this.DB.length; i++) {
            if (this.DB[i] != null) {
                String[] ts = this.DB[i].getMimeTypes();
                if (ts != null) {
                    for (int j = 0; j < ts.length; j++) {
                        if (!mtList.contains(ts[j])) {
                            mtList.add(ts[j]);
                        }
                    }
                }
            }
        }
        return (String[]) mtList.toArray(new String[mtList.size()]);
    }

    public synchronized String[] getNativeCommands(String mimeType) {
        List cmdList;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; i++) {
            if (this.DB[i] != null) {
                String[] cmds = this.DB[i].getNativeCommands(mimeType);
                if (cmds != null) {
                    for (int j = 0; j < cmds.length; j++) {
                        if (!cmdList.contains(cmds[j])) {
                            cmdList.add(cmds[j]);
                        }
                    }
                }
            }
        }
        return (String[]) cmdList.toArray(new String[cmdList.size()]);
    }
}
