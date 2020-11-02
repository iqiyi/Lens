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
package com.qiyi.lens.ui.dns.infos;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;
import okio.Source;
import okio.Timeout;

//[raw request info,　后期再添加 直接抓包hook 结果]
public class RequestData {

    public String url;// request url;
    public Map<String, List<String>> headers;
    public String requestType;// GET OR PUT
    public Object body;
    private String dataS;


    public RequestData(String u, Map<String, List<String>> m, String method, Object body) {
        url = u;
        headers = m;
        requestType = method;
        this.body = body;
    }

    @Override
    public String toString() {

        if (dataS == null) {
            StringBuilder headerSb = new StringBuilder();

            headerSb.append("Url:");
            headerSb.append(url);
            headerSb.append("\n");
            headerSb.append("Method:");
            headerSb.append(requestType);
            headerSb.append("\n");

            if (body != null) {
                headerSb.append("Body:");
                headerSb.append(body());
            }


            headerSb.append('\n');
            headerSb.append("Headers:");

            if (headers != null) {
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    List<String> values = entry.getValue();
                    if (values != null) {
                        for (String value : values) {
                            headerSb.append(entry.getKey()).append("=").append(value).append('\n');
                        }
                    }
                }
            }
            dataS = headerSb.toString();
        }

        return dataS;

    }

    public void postData(OutputStream steam) {

        if (body instanceof RequestBody) {
            RequestBody requestBody = (RequestBody) body;
            BufferedSink sink = Okio.buffer(Okio.sink(steam));
            try {
                requestBody.writeTo(sink);
                sink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String body() {
        if (body instanceof RequestBody) {
            String var = ((RequestBody) body).contentType().subtype();
            if ("x-www-form-urlencoded".equals(var)) {
                Buffer buffer = new Buffer(((RequestBody) body).contentType().charset().toString());
                try {
                    ((RequestBody) body).writeTo(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return URLDecoder.decode(buffer.getData());
            }

        }
        return "";
    }


    static class Buffer implements BufferedSink {


        public String data;
        String charSet;

        public Buffer(String charSet) {
            this.charSet = charSet;

        }


        public String getData() {
            return data;
        }

        @Override
        public okio.Buffer buffer() {
            return null;
        }

        @Override // yes : parse
        public BufferedSink write(ByteString byteString) throws IOException {
            data = byteString.utf8();
            return null;
        }

        @Override
        public BufferedSink write(byte[] source) throws IOException {
            return null;
        }

        @Override // yes
        public BufferedSink write(byte[] source, int offset, int byteCount) throws IOException {
            if (charSet == null || charSet.length() == 0) {
                data = new String(source, offset, byteCount);
            } else {
                data = new String(source, offset, byteCount, charSet);
            }
            return null;
        }

        @Override
        public long writeAll(Source source) throws IOException {
            return 0;
        }

        @Override
        public BufferedSink write(Source source, long byteCount) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeUtf8(String string) throws IOException {
            data = string;
            return null;
        }

        @Override
        public BufferedSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
            data = string.substring(beginIndex, endIndex);
            return null;
        }

        @Override
        public BufferedSink writeUtf8CodePoint(int codePoint) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeString(String string, Charset charset) throws IOException {
            data = string;
            return null;
        }

        @Override
        public BufferedSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeByte(int b) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeShort(int s) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeShortLe(int s) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeInt(int i) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeIntLe(int i) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeLong(long v) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeLongLe(long v) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeDecimalLong(long v) throws IOException {
            return null;
        }

        @Override
        public BufferedSink writeHexadecimalUnsignedLong(long v) throws IOException {
            return null;
        }

        @Override
        public void write(okio.Buffer source, long byteCount) throws IOException {

        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public Timeout timeout() {
            return null;
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public BufferedSink emit() throws IOException {
            return null;
        }

        @Override
        public BufferedSink emitCompleteSegments() throws IOException {
            return null;
        }

        @Override
        public OutputStream outputStream() {
            return null;
        }

        public int write(ByteBuffer src) throws IOException {
            return 0;
        }

        public boolean isOpen() {
            return false;
        }
    }
}
