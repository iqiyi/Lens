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
package com.qiyi.lens.dump;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.utils.FileUtils;
import com.qiyi.lenssdk.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import static android.content.Context.CLIPBOARD_SERVICE;

public class DumpDisplayLogPanel extends FullScreenPanel implements View.OnClickListener {
    private final static int MAX_DISPLAY_LENGTH = 10 * 1024;
    private String mLog;
    private TextView displayView;
    private int index;

    /**
     * @param panel
     * @param tagIndex : <0 : 加载全部的log 信息; 否则分类加载;
     */
    public DumpDisplayLogPanel(FloatingPanel panel, int tagIndex) {
        super(panel);
        this.index = tagIndex;
    }


    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.lens_display_log_panel, viewGroup, false);
        view.findViewById(R.id.lens_copy).setOnClickListener(this);
        view.findViewById(R.id.lens_share).setOnClickListener(this);
        if (!DataTransferManager.getInstance().hasReporter()) {
            view.findViewById(R.id.lens_view_on_web).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.lens_view_on_web).setOnClickListener(this);
        }
        displayView = view.findViewById(R.id.lens_text_view);
        displayLog();
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.lens_copy) {
            doCopy();
        } else if (v.getId() == R.id.lens_share) {
            doShare();
        } else if (v.getId() == R.id.lens_view_on_web) {
            DataTransferManager.getInstance().push2Web(mLog);
        }
    }

    private void doCopy() {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", mLog);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
        }
        showToast(context.getString(R.string.lens_search_sp_result_copy));
    }

    private void doShare() {
        File file = saveLog();
        if (file == null) {
            showToast("share failed");
            return;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uri = Uri.fromFile(file);
        } else {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, file.getName());
        intent.putExtra(Intent.EXTRA_TEXT, file.getName());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, "share"));
        }
    }

    private File saveLog() {
        File file = new File(context.getCacheDir(), UUID.randomUUID().toString() + ".txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, false);
            writer.write(mLog);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeSafely(writer);
        }
        return file;
    }


    private void setLog(String log) {
        mLog = log;
        displayLog();
    }

    private void displayLog() {
        if (mLog != null && displayView != null) {
            displayView.setText(mLog.length() > MAX_DISPLAY_LENGTH ? mLog.substring(0, MAX_DISPLAY_LENGTH) : mLog);
        }
    }


    @Override
    public void onShow() {
        super.onShow();
        loadDump(index);
    }


    private void loadDump(final int index) {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String dump;
                if (index < 0) {
                    dump = LogDumperHolder.getInstance().dump();
                } else {
                    dump = LogDumperHolder.getInstance().dump(index);
                }
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        setLog(dump);
                    }
                });
            }
        }).start();
    }


}
