package com.sec.internal.ims.config.adapters;

import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import java.util.List;
import java.util.Map;

public class HttpAdapterJibeAndSec extends HttpAdapter {
    protected static final String LOG_TAG = HttpAdapterJibeAndSec.class.getSimpleName();

    public HttpAdapterJibeAndSec(int phoneId) {
        super(phoneId);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String url) {
            if (!HttpAdapterJibeAndSec.this.configureUrlConnection(url)) {
                return false;
            }
            HttpAdapterJibeAndSec.this.mState = new ReadyState();
            return true;
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public IHttpAdapter.Response request() {
            HttpAdapterJibeAndSec.this.tryToConnectHttpUrlConnection();
            String stringBuffer = HttpAdapterJibeAndSec.this.mUrl.toString();
            HttpAdapterJibeAndSec httpAdapterJibeAndSec = HttpAdapterJibeAndSec.this;
            int resStatusCode = httpAdapterJibeAndSec.getResStatusCode(httpAdapterJibeAndSec.mHttpURLConn);
            HttpAdapterJibeAndSec httpAdapterJibeAndSec2 = HttpAdapterJibeAndSec.this;
            Map<String, List<String>> resHeader = httpAdapterJibeAndSec2.getResHeader(httpAdapterJibeAndSec2.mHttpURLConn);
            HttpAdapterJibeAndSec httpAdapterJibeAndSec3 = HttpAdapterJibeAndSec.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resHeader, httpAdapterJibeAndSec3.getResBody(httpAdapterJibeAndSec3.mHttpURLConn));
        }

        public boolean close() {
            HttpAdapterJibeAndSec.this.mHttpURLConn.disconnect();
            HttpAdapterJibeAndSec.this.mState = new IdleState();
            return true;
        }
    }
}
