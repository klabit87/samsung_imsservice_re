package com.sun.mail.imap;

public class ACL implements Cloneable {
    private String name;
    private Rights rights;

    public ACL(String name2) {
        this.name = name2;
        this.rights = new Rights();
    }

    public ACL(String name2, Rights rights2) {
        this.name = name2;
        this.rights = rights2;
    }

    public String getName() {
        return this.name;
    }

    public void setRights(Rights rights2) {
        this.rights = rights2;
    }

    public Rights getRights() {
        return this.rights;
    }

    public Object clone() throws CloneNotSupportedException {
        ACL acl = (ACL) super.clone();
        acl.rights = (Rights) this.rights.clone();
        return acl;
    }
}
