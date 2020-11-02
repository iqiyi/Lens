/*
 *
 * Copyright (C) 2020 iQIYI (www.iqiyi.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.qiyi.lens.hook.iface;

import android.annotation.TargetApi;
import android.os.Build;

import com.qiyi.lens.ui.dns.DNSSetting;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

public class HttpsConnectionProxy extends HttpsURLConnection {

    private HttpsURLConnection delegate;
    private boolean isHooked;

    /**
     * Constructor for the HttpURLConnection.
     *
     * @param u the URL
     */
    @Deprecated
    protected HttpsConnectionProxy(URL u) {
        super(u);
    }

    @Override
    public String getCipherSuite() {
        return delegate.getCipherSuite();
    }

    @Override
    public Certificate[] getLocalCertificates() {
        return delegate.getLocalCertificates();
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        return delegate.getServerCertificates();
    }

    public HttpsConnectionProxy(HttpsURLConnection connection) {
        super(connection.getURL());
        this.delegate = connection;

    }

    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return delegate.usingProxy();
    }

    @Override
    public void connect() throws IOException {
        hookRequest();
        delegate.connect();
    }

    public String getHeaderFieldKey(int n) {
        return delegate.getHeaderFieldKey(n);
    }


    public void setFixedLengthStreamingMode(int contentLength) {
        delegate.setFixedLengthStreamingMode(contentLength);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setFixedLengthStreamingMode(long contentLength) {
        delegate.setFixedLengthStreamingMode(contentLength);
    }


    public void setChunkedStreamingMode(int chunk) {
        delegate.setChunkedStreamingMode(chunk);
    }

    public String getHeaderField(int n) {
        return delegate.getHeaderField(n);
    }

    public void setInstanceFollowRedirects(boolean followRedirects) {
        delegate.setInstanceFollowRedirects(followRedirects);
    }

    public boolean getInstanceFollowRedirects() {
        return delegate.getInstanceFollowRedirects();
    }

    public void setRequestMethod(String method) throws ProtocolException {
        delegate.setRequestMethod(method);
    }

    public String getRequestMethod() {
        return delegate.getRequestMethod();
    }

    public int getResponseCode() throws IOException {
        return delegate.getResponseCode();
    }

    public String getResponseMessage() throws IOException {
        return delegate.getResponseMessage();
    }

    public long getHeaderFieldDate(String name, long Default) {
        return delegate.getHeaderFieldDate(name, Default);
    }

    public Permission getPermission() throws IOException {
        return delegate.getPermission();
    }

    public InputStream getErrorStream() {
        return delegate.getErrorStream();
    }


    //  for https

    public Principal getPeerPrincipal()
            throws SSLPeerUnverifiedException {
        return delegate.getPeerPrincipal();
    }


    public Principal getLocalPrincipal() {
        return delegate.getLocalPrincipal();
    }

    public void setHostnameVerifier(HostnameVerifier v) {
        delegate.setHostnameVerifier(v);
    }

    public HostnameVerifier getHostnameVerifier() {
        return delegate.getHostnameVerifier();
    }

    public void setSSLSocketFactory(SSLSocketFactory sf) {
        delegate.setSSLSocketFactory(sf);
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return delegate.getSSLSocketFactory();
    }

    // http URL Connection
    public void setConnectTimeout(int timeout) {
        delegate.setConnectTimeout(timeout);
    }

    public int getConnectTimeout() {
        return delegate.getConnectTimeout();
    }

    public void setReadTimeout(int timeout) {
        delegate.setReadTimeout(timeout);
    }

    public int getReadTimeout() {
        return delegate.getReadTimeout();
    }

    public URL getURL() {
        return delegate.getURL();
    }

    public int getContentLength() {
        return delegate.getContentLength();
    }

    @TargetApi(Build.VERSION_CODES.N)
    public long getContentLengthLong() {
        return delegate.getContentLengthLong();
    }

    public String getContentType() {
        return delegate.getContentType();
    }

    public String getContentEncoding() {
        return delegate.getContentEncoding();
    }

    public long getExpiration() {
        return delegate.getExpiration();
    }

    public long getDate() {
        return delegate.getDate();
    }

    public long getLastModified() {
        return delegate.getLastModified();
    }

    public String getHeaderField(String name) {
        return delegate.getHeaderField(name);
    }

    public Map<String, List<String>> getHeaderFields() {
        return delegate.getHeaderFields();
    }

    public int getHeaderFieldInt(String name, int Default) {
        return delegate.getHeaderFieldInt(name, Default);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public long getHeaderFieldLong(String name, long Default) {
        return delegate.getHeaderFieldLong(name, Default);
    }

//    @Deprecated
//    public long getHeaderFieldDate(String name, long Default){
//        return delegate.getHeaderFieldDate(name, Default);
//    }

    public Object getContent() throws IOException {
        return delegate.getContent();
    }

    public Object getContent(Class[] classes) throws IOException {
        return delegate.getContent(classes);
    }


    public InputStream getInputStream() throws IOException {
        hookRequest();
        return delegate.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        hookRequest();
        return delegate.getOutputStream();
    }

    public String toString() {
        return delegate.toString();
    }

    public void setDoInput(boolean doInput) {
        delegate.setDoInput(doInput);
    }

    public boolean getDoInput() {
        return delegate.getDoInput();
    }

    public void setDoOutput(boolean doOutput) {
        delegate.setDoOutput(doOutput);
    }

    public boolean getDoOutput() {
        return delegate.getDoOutput();
    }

    public void setAllowUserInteraction(boolean allowUserInteraction) {
        delegate.setAllowUserInteraction(allowUserInteraction);
    }

    public boolean getAllowUserInteraction() {
        return delegate.getAllowUserInteraction();
    }

    public void setUseCaches(boolean usecaches) {
        delegate.setUseCaches(useCaches);
    }

    public boolean getUseCaches() {
        return delegate.getUseCaches();
    }

    public void setIfModifiedSince(long ifModifiedSince) {
        delegate.setIfModifiedSince(ifModifiedSince);
    }

    public long getIfModifiedSince() {
        return delegate.getIfModifiedSince();
    }

    public boolean getDefaultUseCaches() {
        return delegate.getDefaultUseCaches();
    }

    public void setDefaultUseCaches(boolean defaultUseCaches) {
        delegate.setDefaultUseCaches(defaultUseCaches);
    }

    public void setRequestProperty(String key, String value) {
        delegate.setRequestProperty(key, value);
    }

    public void addRequestProperty(String key, String value) {
        delegate.addRequestProperty(key, value);
    }

    public String getRequestProperty(String key) {
        return delegate.getRequestProperty(key);
    }

    public Map<String, List<String>> getRequestProperties() {
        return delegate.getRequestProperties();
    }


    private void hookRequest() {
        if (!isHooked) {
            isHooked = true;
            URLConnection connection = delegate;
            URL url = connection.getURL();
            if (url != null) {
                String destUrl = url.toString();
                NetworkAnalyzeConfig.getInstance().onRequest(destUrl);

                Map<String, List<String>> headers = connection.getRequestProperties();
                DNSSetting.onRequestUrl(destUrl, headers, getRequestMethod(), null);
            }
        }

    }
}
