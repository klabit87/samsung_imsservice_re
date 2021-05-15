package javax.mail;

public final class PasswordAuthentication {
    private String password;
    private String userName;

    public PasswordAuthentication(String userName2, String password2) {
        this.userName = userName2;
        this.password = password2;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }
}
