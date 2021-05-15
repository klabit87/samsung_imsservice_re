package com.sun.mail.imap;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;

public class DefaultFolder extends IMAPFolder {
    protected DefaultFolder(IMAPStore store) {
        super("", 65535, store);
        this.exists = true;
        this.type = 2;
    }

    public String getName() {
        return this.fullName;
    }

    public Folder getParent() {
        return null;
    }

    public Folder[] list(final String pattern) throws MessagingException {
        ListInfo[] listInfoArr = null;
        ListInfo[] li = (ListInfo[]) doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", pattern);
            }
        });
        if (li == null) {
            return new Folder[0];
        }
        IMAPFolder[] folders = new IMAPFolder[li.length];
        for (int i = 0; i < folders.length; i++) {
            folders[i] = new IMAPFolder(li[i], (IMAPStore) this.store);
        }
        return folders;
    }

    public Folder[] listSubscribed(final String pattern) throws MessagingException {
        ListInfo[] listInfoArr = null;
        ListInfo[] li = (ListInfo[]) doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.lsub("", pattern);
            }
        });
        if (li == null) {
            return new Folder[0];
        }
        IMAPFolder[] folders = new IMAPFolder[li.length];
        for (int i = 0; i < folders.length; i++) {
            folders[i] = new IMAPFolder(li[i], (IMAPStore) this.store);
        }
        return folders;
    }

    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    public Folder getFolder(String name) throws MessagingException {
        return new IMAPFolder(name, 65535, (IMAPStore) this.store);
    }

    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("Cannot delete Default Folder");
    }

    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("Cannot rename Default Folder");
    }

    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Cannot append to Default Folder");
    }

    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Cannot expunge Default Folder");
    }
}
