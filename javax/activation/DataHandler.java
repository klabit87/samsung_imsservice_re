package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import myjava.awt.datatransfer.DataFlavor;
import myjava.awt.datatransfer.Transferable;
import myjava.awt.datatransfer.UnsupportedFlavorException;
import org.xbill.DNS.KEYRecord;

public class DataHandler implements Transferable {
    private static final DataFlavor[] emptyFlavors = new DataFlavor[0];
    private static DataContentHandlerFactory factory = null;
    private CommandMap currentCommandMap = null;
    private DataContentHandler dataContentHandler = null;
    private DataSource dataSource = null;
    private DataContentHandler factoryDCH = null;
    private DataSource objDataSource = null;
    /* access modifiers changed from: private */
    public Object object = null;
    /* access modifiers changed from: private */
    public String objectMimeType = null;
    private DataContentHandlerFactory oldFactory = null;
    private String shortType = null;
    private DataFlavor[] transferFlavors = emptyFlavors;

    public DataHandler(DataSource ds) {
        this.dataSource = ds;
        this.oldFactory = factory;
    }

    public DataHandler(Object obj, String mimeType) {
        this.object = obj;
        this.objectMimeType = mimeType;
        this.oldFactory = factory;
    }

    public DataHandler(URL url) {
        this.dataSource = new URLDataSource(url);
        this.oldFactory = factory;
    }

    private synchronized CommandMap getCommandMap() {
        if (this.currentCommandMap != null) {
            return this.currentCommandMap;
        }
        return CommandMap.getDefaultCommandMap();
    }

    public DataSource getDataSource() {
        DataSource dataSource2 = this.dataSource;
        if (dataSource2 != null) {
            return dataSource2;
        }
        if (this.objDataSource == null) {
            this.objDataSource = new DataHandlerDataSource(this);
        }
        return this.objDataSource;
    }

    public String getName() {
        DataSource dataSource2 = this.dataSource;
        if (dataSource2 != null) {
            return dataSource2.getName();
        }
        return null;
    }

    public String getContentType() {
        DataSource dataSource2 = this.dataSource;
        if (dataSource2 != null) {
            return dataSource2.getContentType();
        }
        return this.objectMimeType;
    }

    public InputStream getInputStream() throws IOException {
        DataSource dataSource2 = this.dataSource;
        if (dataSource2 != null) {
            return dataSource2.getInputStream();
        }
        DataContentHandler dch = getDataContentHandler();
        if (dch == null) {
            throw new UnsupportedDataTypeException("no DCH for MIME type " + getBaseType());
        } else if (!(dch instanceof ObjectDataContentHandler) || ((ObjectDataContentHandler) dch).getDCH() != null) {
            final DataContentHandler fdch = dch;
            final PipedOutputStream pos = new PipedOutputStream();
            InputStream pin = new PipedInputStream(pos);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        fdch.writeTo(DataHandler.this.object, DataHandler.this.objectMimeType, pos);
                        try {
                            pos.close();
                        } catch (IOException e) {
                        }
                    } catch (IOException e2) {
                        pos.close();
                    } catch (Throwable th) {
                        try {
                            pos.close();
                        } catch (IOException e3) {
                        }
                        throw th;
                    }
                }
            }, "DataHandler.getInputStream").start();
            return pin;
        } else {
            throw new UnsupportedDataTypeException("no object DCH for MIME type " + getBaseType());
        }
    }

    public void writeTo(OutputStream os) throws IOException {
        DataSource dataSource2 = this.dataSource;
        if (dataSource2 != null) {
            byte[] data = new byte[KEYRecord.Flags.FLAG2];
            InputStream is = dataSource2.getInputStream();
            while (true) {
                try {
                    int read = is.read(data);
                    int bytes_read = read;
                    if (read > 0) {
                        os.write(data, 0, bytes_read);
                    } else {
                        return;
                    }
                } finally {
                    is.close();
                }
            }
        } else {
            getDataContentHandler().writeTo(this.object, this.objectMimeType, os);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        DataSource dataSource2 = this.dataSource;
        if (dataSource2 != null) {
            return dataSource2.getOutputStream();
        }
        return null;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        if (factory != this.oldFactory) {
            this.transferFlavors = emptyFlavors;
        }
        if (this.transferFlavors == emptyFlavors) {
            this.transferFlavors = getDataContentHandler().getTransferDataFlavors();
        }
        return this.transferFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        DataFlavor[] lFlavors = getTransferDataFlavors();
        for (DataFlavor equals : lFlavors) {
            if (equals.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return getDataContentHandler().getTransferData(flavor, this.dataSource);
    }

    public synchronized void setCommandMap(CommandMap commandMap) {
        if (commandMap != this.currentCommandMap || commandMap == null) {
            this.transferFlavors = emptyFlavors;
            this.dataContentHandler = null;
            this.currentCommandMap = commandMap;
        }
    }

    public CommandInfo[] getPreferredCommands() {
        if (this.dataSource != null) {
            return getCommandMap().getPreferredCommands(getBaseType(), this.dataSource);
        }
        return getCommandMap().getPreferredCommands(getBaseType());
    }

    public CommandInfo[] getAllCommands() {
        if (this.dataSource != null) {
            return getCommandMap().getAllCommands(getBaseType(), this.dataSource);
        }
        return getCommandMap().getAllCommands(getBaseType());
    }

    public CommandInfo getCommand(String cmdName) {
        if (this.dataSource != null) {
            return getCommandMap().getCommand(getBaseType(), cmdName, this.dataSource);
        }
        return getCommandMap().getCommand(getBaseType(), cmdName);
    }

    public Object getContent() throws IOException {
        Object obj = this.object;
        if (obj != null) {
            return obj;
        }
        return getDataContentHandler().getContent(getDataSource());
    }

    public Object getBean(CommandInfo cmdinfo) {
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            return cmdinfo.getCommandObject(this, cld);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private synchronized DataContentHandler getDataContentHandler() {
        if (factory != this.oldFactory) {
            this.oldFactory = factory;
            this.factoryDCH = null;
            this.dataContentHandler = null;
            this.transferFlavors = emptyFlavors;
        }
        if (this.dataContentHandler != null) {
            return this.dataContentHandler;
        }
        String simpleMT = getBaseType();
        if (this.factoryDCH == null && factory != null) {
            this.factoryDCH = factory.createDataContentHandler(simpleMT);
        }
        if (this.factoryDCH != null) {
            this.dataContentHandler = this.factoryDCH;
        }
        if (this.dataContentHandler == null) {
            if (this.dataSource != null) {
                this.dataContentHandler = getCommandMap().createDataContentHandler(simpleMT, this.dataSource);
            } else {
                this.dataContentHandler = getCommandMap().createDataContentHandler(simpleMT);
            }
        }
        if (this.dataSource != null) {
            this.dataContentHandler = new DataSourceDataContentHandler(this.dataContentHandler, this.dataSource);
        } else {
            this.dataContentHandler = new ObjectDataContentHandler(this.dataContentHandler, this.object, this.objectMimeType);
        }
        return this.dataContentHandler;
    }

    private synchronized String getBaseType() {
        if (this.shortType == null) {
            String ct = getContentType();
            try {
                this.shortType = new MimeType(ct).getBaseType();
            } catch (MimeTypeParseException e) {
                this.shortType = ct;
            }
        }
        return this.shortType;
    }

    public static synchronized void setDataContentHandlerFactory(DataContentHandlerFactory newFactory) {
        Class<DataHandler> cls = DataHandler.class;
        synchronized (cls) {
            if (factory == null) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    try {
                        security.checkSetFactory();
                    } catch (SecurityException ex) {
                        if (cls.getClassLoader() != newFactory.getClass().getClassLoader()) {
                            throw ex;
                        }
                    }
                }
                factory = newFactory;
            } else {
                throw new Error("DataContentHandlerFactory already defined");
            }
        }
    }
}
