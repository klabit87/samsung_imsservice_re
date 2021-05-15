package myjava.awt.datatransfer;

public class UnsupportedFlavorException extends Exception {
    private static final long serialVersionUID = 5383814944251665601L;

    public UnsupportedFlavorException(DataFlavor flavor) {
        super("flavor = " + String.valueOf(flavor));
    }
}
