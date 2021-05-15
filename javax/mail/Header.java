package javax.mail;

public class Header {
    protected String name;
    protected String value;

    public Header(String name2, String value2) {
        this.name = name2;
        this.value = value2;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
