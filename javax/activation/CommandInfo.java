package javax.activation;

import java.beans.Beans;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class CommandInfo {
    private String className;
    private String verb;

    public CommandInfo(String verb2, String className2) {
        this.verb = verb2;
        this.className = className2;
    }

    public String getCommandName() {
        return this.verb;
    }

    public String getCommandClass() {
        return this.className;
    }

    public Object getCommandObject(DataHandler dh, ClassLoader loader) throws IOException, ClassNotFoundException {
        InputStream is;
        Object new_bean = Beans.instantiate(loader, this.className);
        if (new_bean != null) {
            if (new_bean instanceof CommandObject) {
                ((CommandObject) new_bean).setCommandContext(this.verb, dh);
            } else if (!(!(new_bean instanceof Externalizable) || dh == null || (is = dh.getInputStream()) == null)) {
                ((Externalizable) new_bean).readExternal(new ObjectInputStream(is));
            }
        }
        return new_bean;
    }
}
