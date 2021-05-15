package javax.activation;

import java.io.IOException;
import java.io.OutputStream;
import myjava.awt.datatransfer.DataFlavor;
import myjava.awt.datatransfer.UnsupportedFlavorException;

/* compiled from: DataHandler */
class DataSourceDataContentHandler implements DataContentHandler {
    private DataContentHandler dch = null;
    private DataSource ds = null;
    private DataFlavor[] transferFlavors = null;

    public DataSourceDataContentHandler(DataContentHandler dch2, DataSource ds2) {
        this.ds = ds2;
        this.dch = dch2;
    }

    public DataFlavor[] getTransferDataFlavors() {
        if (this.transferFlavors == null) {
            DataContentHandler dataContentHandler = this.dch;
            if (dataContentHandler != null) {
                this.transferFlavors = dataContentHandler.getTransferDataFlavors();
            } else {
                DataFlavor[] dataFlavorArr = new DataFlavor[1];
                this.transferFlavors = dataFlavorArr;
                dataFlavorArr[0] = new ActivationDataFlavor(this.ds.getContentType(), this.ds.getContentType());
            }
        }
        return this.transferFlavors;
    }

    public Object getTransferData(DataFlavor df, DataSource ds2) throws UnsupportedFlavorException, IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            return dataContentHandler.getTransferData(df, ds2);
        }
        if (df.equals(getTransferDataFlavors()[0])) {
            return ds2.getInputStream();
        }
        throw new UnsupportedFlavorException(df);
    }

    public Object getContent(DataSource ds2) throws IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            return dataContentHandler.getContent(ds2);
        }
        return ds2.getInputStream();
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            dataContentHandler.writeTo(obj, mimeType, os);
            return;
        }
        throw new UnsupportedDataTypeException("no DCH for content type " + this.ds.getContentType());
    }
}
