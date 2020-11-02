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
package com.qiyi.lens.ui.abtest.content;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.qiyi.lens.ui.abtest.Value;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lenssdk.R;

/**
 * 支持以文本编辑方式展示内容数据
 * AB Test Module :  used for edit a String value
 */
public class EditTextContent extends ValueContent implements View.OnClickListener, TextWatcher {

    private EditText eText;
    private String hintVar;
    private boolean isChanged;
    private View confirmView;

    public EditTextContent(ViewGroup parent, String key, Value type) {
        super(parent, key, type);
    }

    @Override
    public void loadView() {

        if (getChildCount() == 0) {
            View view = UIUtils.inflateVew(_parent, R.layout.lens_abt_content_edit_text, false);
            confirmView = view.findViewById(R.id.lens_abt_content_text_confirm);
            confirmView.setOnClickListener(this);
            eText = view.findViewById(R.id.lens_abt_content_text);
            _parent.addView(view);
        } else {
            //[edit text reset: bind data]

        }


        eText.removeTextChangedListener(this);
        eText.addTextChangedListener(this);
        Value value = _value;
        String var = value.getValue(_key);
        if (var == null) {
            var = "";
        }

        if (_value.isNumberType()) {
            eText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            eText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        hintVar = var;
        eText.setHint(var);
        eText.requestFocus();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.lens_abt_content_text_confirm) {
            save();
            if (_panelView != null) {
                _panelView.dismiss();
            }
        }
    }

    private void save() {
        String text = eText.getText().toString();
        _value.setValue(_key, text);
    }


    @Override
    public void detachView() {
        super.detachView();
        eText.removeTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String nVar = s.toString();

        if (!nVar.equals(hintVar)) {
            if (!isChanged) {
                isChanged = true;
                confirmView.setBackgroundResource(R.drawable.lens_round_rect_hi_light);
            }
        } else if (isChanged) {
            isChanged = false;
            confirmView.setBackgroundResource(R.drawable.lens_round_rect_light);
        }


    }
}
