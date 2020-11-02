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
package com.qiyi.lens.ui.dns;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.dns.infos.RequestData;
import com.qiyi.lens.utils.FileUtils;
import com.qiyi.lens.utils.LL;
import com.qiyi.lens.utils.OSUtils;
import com.qiyi.lenssdk.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.CLIPBOARD_SERVICE;

public class HttpResponsePanel extends FullScreenPanel {
    private TextView responseContent;
    private TextView responseSavePath;
    private RequestData data;
    public Handler handler;
    private File fileDir;

    public HttpResponsePanel(final RequestData data, FloatingPanel panel) {
        super(panel);
        this.data = data;
        handler = new H(this);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_network_response_panel, viewGroup);
        responseContent = (TextView) content.findViewById(R.id.network_response_display);
        TextView responseContentCopy = (TextView) content.findViewById(R.id.network_response_copy);
        TextView responseContentSave = (TextView) content.findViewById(R.id.network_response_save);
        responseSavePath = (TextView) content.findViewById(R.id.network_response_save_path);
        TextView responsePush2Web = content.findViewById(R.id.network_response_push);
        if (!DataTransferManager.getInstance().hasReporter()) {
            responsePush2Web.setVisibility(View.GONE);
        }
        responseContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        responseContentCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence sequence = responseContent.getText();
                if (sequence != null) {
                    String s = responseContent.getText().toString();
                    if (!s.isEmpty()) {
                        copy(s);
                    }
                }

            }
        });
        responseContentSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data == null || data.url == null) {
                    return;
                }
                String url = data.url;
                CharSequence sequence = responseContent.getText();
                if (sequence != null) {
                    String s = responseContent.getText().toString();
                    if (!s.isEmpty()) {
                        writeToFile(url + " : \r\n\r\n\r\n" + s);
                    }
                }
            }
        });
        responsePush2Web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (responseContent.getText() != null) {
                    DataTransferManager.getInstance().push2Web(responseContent.getText().toString());
                }
            }
        });

        // String
        responseContent.setText(data.toString());
        return content;
    }


    private void writeToFile(String s) {
        if (OSUtils.isPreQ() && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            showToast("保存失败，请在设置中打开sdcard读写权限");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            if (fileDir == null) {
                if (OSUtils.isPreQ()) {
                    fileDir = new File("/sdcard");
                } else {
                    fileDir = getContext().getExternalFilesDir("Lens");
                }
            }
            String md5 = "url_response.txt"/*MD5Algorithm.md5(data.url)*/;
            if (FileUtils.writeStringToFile(fileDir, md5, s)) {
                String savedFilepath = fileDir.getPath() + "/" + md5;
                responseSavePath.setVisibility(View.VISIBLE);
                responseSavePath.setText("文件已保存到：" + savedFilepath);
                showToast("文件已保存");
            } else {
                showToast("文件保存失败");
            }
        }

//        showToast("文件已保存到："+savedFilepath);
    }

    private ClipboardManager myClipboard;
    private ClipData myClip;

    private void copy(String data) {
        if (myClipboard == null) {
            myClipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        }
        myClip = ClipData.newPlainText("text", data);
        myClipboard.setPrimaryClip(myClip);
        showToast("复制成功");
    }

    public void setData(RequestData data) {
        this.data = data;
    }

    @Override
    public void show() {
        super.show();
        if (data != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    requestByGet(data);
                }
            }).start();

        }
    }

    // Get方式请求
    public void requestByGet(RequestData data) {
        try {
            // 新建一个URL对象
            URL url = new URL(data.url);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            addHeaders(urlConn, data.headers);
            urlConn.setRequestMethod(data.requestType);

            if (data.body != null) {
                urlConn.setDoOutput(true);
            }

            // 设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);
            urlConn.setReadTimeout(5000);
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功


            if (data.body != null && "POST".equals(data.requestType)) {
                //[do post]
                data.postData(urlConn.getOutputStream());
            }


            Message msg = new Message();

            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
                msg.what = 1;
                String result = buffer.toString();
                if (!result.trim().isEmpty()) {
                    msg.obj = buffer.toString();
                } else {
                    msg.obj = "没有数据";
                }

                handler.sendMessage(msg);
            } else {
                msg.what = 2;
                msg.obj = "请求失败";
                handler.sendMessage(msg);
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = 2;
            msg.obj = "请求失败:" + e.toString();
            handler.sendMessage(msg);
        }

    }

    private void addHeaders(HttpURLConnection connection, Map<String, List<String>> headers) {
        if (headers != null && !headers.isEmpty()) {

            Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
            for (Map.Entry<String, List<String>> entry : entries) {
                String name = entry.getKey();
                List<String> list = entry.getValue();
                if (list != null && !list.isEmpty()) {
                    if (list.size() == 1) {
                        connection.setRequestProperty(name, list.get(0));
                        LL.d("add header:", name + " " + list.get(0));
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (String s : list) {
                            sb.append(s);
                            sb.append("; ");
                        }
                        sb.setLength(sb.length() - 2);
                        connection.setRequestProperty(name, sb.toString());
                        LL.d("add header:", name + " " + sb.toString());
                    }
                }

            }

        }

    }

    @Override
    public void onDismiss() {
        super.onDismiss();
        if (handler != null) {
            handler.removeMessages(0);
        }
        handler = null;
    }


    void setData(String var) {
        StringBuilder builder = new StringBuilder(data.toString());
        builder.append("\n");
        builder.append("\n");
        builder.append(var);
        responseContent.setText(builder.toString());
    }

    static class H extends Handler {
        private WeakReference<HttpResponsePanel> host;

        H(HttpResponsePanel var) {
            host = new WeakReference<>(var);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    HttpResponsePanel panel = host.get();
                    if (panel != null && msg.obj != null) {
                        panel.setData((RequestData) msg.obj);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
