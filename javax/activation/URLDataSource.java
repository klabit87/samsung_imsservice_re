package javax.activation;

import com.sec.internal.helper.httpclient.HttpPostBody;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLDataSource implements DataSource {
    private URL url = null;
    private URLConnection url_conn = null;

    public URLDataSource(URL url2) {
        this.url = url2;
    }

    public String getContentType() {
        String type = null;
        try {
            if (this.url_conn == null) {
                this.url_conn = this.url.openConnection();
            }
        } catch (IOException e) {
        }
        URLConnection uRLConnection = this.url_conn;
        if (uRLConnection != null) {
            type = uRLConnection.getContentType();
        }
        if (type == null) {
            return HttpPostBody.CONTENT_TYPE_DEFAULT;
        }
        return type;
    }

    public String getName() {
        return this.url.getFile();
    }

    public InputStream getInputStream() throws IOException {
        return this.url.openStream();
    }

    public OutputStream getOutputStream() throws IOException {
        URLConnection openConnection = this.url.openConnection();
        this.url_conn = openConnection;
        if (openConnection == null) {
            return null;
        }
        openConnection.setDoOutput(true);
        return this.url_conn.getOutputStream();
    }

    public URL getURL() {
        return this.url;
    }
}
