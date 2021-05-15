package javax.activation;

import myjava.awt.datatransfer.DataFlavor;

public class ActivationDataFlavor extends DataFlavor {
    private String humanPresentableName;
    private MimeType mimeObject;
    private String mimeType;
    private Class representationClass;

    public ActivationDataFlavor(Class representationClass2, String mimeType2, String humanPresentableName2) {
        super(mimeType2, humanPresentableName2);
        this.mimeType = null;
        this.mimeObject = null;
        this.humanPresentableName = null;
        this.representationClass = null;
        this.mimeType = mimeType2;
        this.humanPresentableName = humanPresentableName2;
        this.representationClass = representationClass2;
    }

    public ActivationDataFlavor(Class representationClass2, String humanPresentableName2) {
        super((Class<?>) representationClass2, humanPresentableName2);
        this.mimeType = null;
        this.mimeObject = null;
        this.humanPresentableName = null;
        this.representationClass = null;
        this.mimeType = super.getMimeType();
        this.representationClass = representationClass2;
        this.humanPresentableName = humanPresentableName2;
    }

    public ActivationDataFlavor(String mimeType2, String humanPresentableName2) {
        super(mimeType2, humanPresentableName2);
        this.mimeType = null;
        this.mimeObject = null;
        this.humanPresentableName = null;
        this.representationClass = null;
        this.mimeType = mimeType2;
        try {
            this.representationClass = Class.forName("java.io.InputStream");
        } catch (ClassNotFoundException e) {
        }
        this.humanPresentableName = humanPresentableName2;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Class getRepresentationClass() {
        return this.representationClass;
    }

    public String getHumanPresentableName() {
        return this.humanPresentableName;
    }

    public void setHumanPresentableName(String humanPresentableName2) {
        this.humanPresentableName = humanPresentableName2;
    }

    public boolean equals(DataFlavor dataFlavor) {
        return isMimeTypeEqual(dataFlavor) && dataFlavor.getRepresentationClass() == this.representationClass;
    }

    public boolean isMimeTypeEqual(String mimeType2) {
        try {
            if (this.mimeObject == null) {
                this.mimeObject = new MimeType(this.mimeType);
            }
            return this.mimeObject.match(new MimeType(mimeType2));
        } catch (MimeTypeParseException e) {
            return this.mimeType.equalsIgnoreCase(mimeType2);
        }
    }

    /* access modifiers changed from: protected */
    public String normalizeMimeTypeParameter(String parameterName, String parameterValue) {
        return parameterValue;
    }

    /* access modifiers changed from: protected */
    public String normalizeMimeType(String mimeType2) {
        return mimeType2;
    }
}
