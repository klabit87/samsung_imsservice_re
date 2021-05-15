package javax.mail;

import java.util.Vector;

public class FetchProfile {
    private Vector headers = null;
    private Vector specials = null;

    public static class Item {
        public static final Item CONTENT_INFO = new Item("CONTENT_INFO");
        public static final Item ENVELOPE = new Item("ENVELOPE");
        public static final Item FLAGS = new Item("FLAGS");
        private String name;

        protected Item(String name2) {
            this.name = name2;
        }
    }

    public void add(Item item) {
        if (this.specials == null) {
            this.specials = new Vector();
        }
        this.specials.addElement(item);
    }

    public void add(String headerName) {
        if (this.headers == null) {
            this.headers = new Vector();
        }
        this.headers.addElement(headerName);
    }

    public boolean contains(Item item) {
        Vector vector = this.specials;
        return vector != null && vector.contains(item);
    }

    public boolean contains(String headerName) {
        Vector vector = this.headers;
        return vector != null && vector.contains(headerName);
    }

    public Item[] getItems() {
        Vector vector = this.specials;
        if (vector == null) {
            return new Item[0];
        }
        Item[] s = new Item[vector.size()];
        this.specials.copyInto(s);
        return s;
    }

    public String[] getHeaderNames() {
        Vector vector = this.headers;
        if (vector == null) {
            return new String[0];
        }
        String[] s = new String[vector.size()];
        this.headers.copyInto(s);
        return s;
    }
}
