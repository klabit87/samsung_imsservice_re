package javax.activation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import myjava.awt.datatransfer.DataFlavor;
import myjava.awt.datatransfer.UnsupportedFlavorException;

/* compiled from: DataHandler */
class ObjectDataContentHandler implements DataContentHandler {
    private DataContentHandler dch = null;
    private String mimeType;
    private Object obj;
    private DataFlavor[] transferFlavors = null;

    public ObjectDataContentHandler(DataContentHandler dch2, Object obj2, String mimeType2) {
        this.obj = obj2;
        this.mimeType = mimeType2;
        this.dch = dch2;
    }

    public DataContentHandler getDCH() {
        return this.dch;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        if (this.transferFlavors == null) {
            if (this.dch != null) {
                this.transferFlavors = this.dch.getTransferDataFlavors();
            } else {
                DataFlavor[] dataFlavorArr = new DataFlavor[1];
                this.transferFlavors = dataFlavorArr;
                dataFlavorArr[0] = new ActivationDataFlavor(this.obj.getClass(), this.mimeType, this.mimeType);
            }
        }
        return this.transferFlavors;
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            return dataContentHandler.getTransferData(df, ds);
        }
        if (df.equals(getTransferDataFlavors()[0])) {
            return this.obj;
        }
        throw new UnsupportedFlavorException(df);
    }

    public Object getContent(DataSource ds) {
        return this.obj;
    }

    public void writeTo(Object obj2, String mimeType2, OutputStream os) throws IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            dataContentHandler.writeTo(obj2, mimeType2, os);
        } else if (obj2 instanceof byte[]) {
            os.write((byte[]) obj2);
        } else if (obj2 instanceof String) {
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write((String) obj2);
            osw.flush();
        } else {
            throw new UnsupportedDataTypeException("no object DCH for MIME type " + this.mimeType);
        }
    }
}
