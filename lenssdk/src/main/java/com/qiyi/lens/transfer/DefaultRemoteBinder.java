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
package com.qiyi.lens.transfer;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lenssdk.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Scan QR code , then bind lens client to lens server
 */
public class DefaultRemoteBinder implements IRemoteBinder {
    @Override
    public boolean bind() {
        new InputPanel(null).show();
        return true;
    }

    public static class InputPanel extends FullScreenPanel {

        private TextView btnPaste;
        private ClipboardManager cpm;

        public InputPanel(FloatingPanel panel) {
            super(panel);
            cpm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }

        @Override
        protected View onCreateView(ViewGroup viewGroup) {
            LinearLayout contentView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.lens_editor_panel,
                    viewGroup, false);
            final EditText editText = contentView.findViewById(R.id.lens_edit);
            editText.setHint(R.string.lens_block_bind_remote_hint);
            btnPaste = new TextView(getContext());
            btnPaste.setVisibility(View.GONE);
            btnPaste.setText(R.string.lens_paste);
            btnPaste.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            btnPaste.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            lp.rightMargin = UIUtils.dp2px(getContext(), 24);
            ((ViewGroup) contentView.findViewById(R.id.len_title_bar_more_layout)).addView(btnPaste, 0, lp);
            if (cpm.getPrimaryClip() != null && cpm.getPrimaryClip().getItemCount() > 0) {
                btnPaste.setVisibility(View.VISIBLE);
            }
            btnPaste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cpm.getPrimaryClip() != null && cpm.getPrimaryClip().getItemCount() > 0) {
                        editText.setText(cpm.getPrimaryClip().getItemAt(0).getText());
                    }
                }
            });
            View save = contentView.findViewById(R.id.len_title_bar_operation);
            save.setVisibility(View.VISIBLE);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editText.getText() == null || TextUtils.isEmpty(editText.getText().toString().trim())) {
                        return;
                    }
                    DataTransferManager.getInstance().setRemoteUrl(editText.getText().toString());
                    dismiss();
                }
            });
            contentView.findViewById(R.id.len_title_bar_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            return contentView;
        }
    }
}
