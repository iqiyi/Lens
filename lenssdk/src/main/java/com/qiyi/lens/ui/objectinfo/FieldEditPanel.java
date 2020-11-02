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
package com.qiyi.lens.ui.objectinfo;

import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.lens.utils.ReflectTool;
import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.utils.reflect.FieldInfo;
import com.qiyi.lenssdk.R;

public class FieldEditPanel extends FullScreenPanel implements TextWatcher,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private FrameLayout flFieldValue;
    private EditText etFieldValue;
    private Switch swFieldValue;
    private Button btnApplyChange;

    private Object fieldHostObject;
    private FieldInfo info;


    public FieldEditPanel(FloatingPanel panel) {
        super(panel);
    }

    public void setData(Object host, FieldInfo fieldInfo) {
        this.fieldHostObject = host;
        this.info = fieldInfo;
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_layout_field_edit, viewGroup);

        TextView tvFieldHost = content.findViewById(R.id.tv_field_host);
        TextView tvFieldInfo = content.findViewById(R.id.tv_field_info);
        flFieldValue = content.findViewById(R.id.fl_field_value);
        btnApplyChange = content.findViewById(R.id.btn_apply);
        Button btnCancel = content.findViewById(R.id.btn_cancel);
        Button btnPush = content.findViewById(R.id.btn_push);

        if (fieldHostObject != null) {
            tvFieldHost.setText(fieldHostObject.getClass().getSimpleName());
        }
        tvFieldInfo.setText(info.getType() + " " + info.getSimpleName());

        if (info.getValue() instanceof View) {
            //[enable set visibilty]
            tvFieldInfo.setText("Edit visibility for " + info.getSimpleName());
            tvFieldHost.setText(info.getSimpleName());
            View destVidew = (View) info.getValue();
            View viewParent = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.lens_view_visible_setting, viewGroup, false);
            RadioGroup view = viewParent.findViewById(R.id.lens_view_visibl_setting_group);
            if (destVidew.getVisibility() == View.VISIBLE) {
                view.check(R.id.lens_view_vis_visible);
            } else if (destVidew.getVisibility() == View.INVISIBLE) {
                view.check(R.id.lens_view_vis_invisible);
            } else if (destVidew.getVisibility() == View.GONE) {
                view.check(R.id.lens_view_vis_gone);
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            flFieldValue.addView(viewParent, layoutParams);
            view.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    btnApplyChange.setEnabled(true);
                }
            });

            viewParent.findViewById(R.id.lesn_view_edit_remove_parent).setOnClickListener(this);
            viewParent.findViewById(R.id.lesn_view_edit_remove_children).setOnClickListener(this);

            setViewOnClick(viewParent,
//                    R.id.lesn_view_edit_remove_parent,
//                    R.id.lesn_view_edit_remove_children,
                    R.id.lens_view_setting_bg_blk,
                    R.id.lens_view_setting_bg_wt,
                    R.id.lens_view_setting_bg_ylow,
                    R.id.lens_view_setting_bg_red,
                    R.id.lens_view_setting_bg_none,
                    R.id.lens_view_setting_bg_blu);

        } else if (Boolean.TYPE.equals(info.getType())) {
            swFieldValue = new Switch(viewGroup.getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            flFieldValue.addView(swFieldValue, layoutParams);
            swFieldValue.setChecked((boolean) info.getValue());
            swFieldValue.setOnCheckedChangeListener(this);
        } else {
            etFieldValue = new EditText(viewGroup.getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            flFieldValue.addView(etFieldValue, layoutParams);
            etFieldValue.setText(info.getValue().toString());
            etFieldValue.addTextChangedListener(this);
        }

        btnApplyChange.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnPush.setOnClickListener(this);
        return content;
    }


    private void setViewOnClick(View parent, int... ids) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int vid = v.getId();
                View view = getValueView();
                if (view == null) return;
                int color;
                if (vid == R.id.lens_view_setting_bg_none) {
                    if (Build.VERSION.SDK_INT >= 16) {
                        view.setBackground(null);
                    } else {
                        view.setBackgroundDrawable(null);
                    }
                    return;
                } else if (vid == R.id.lens_view_setting_bg_red) {
                    color = Color.RED;
                } else if (vid == R.id.lens_view_setting_bg_blu) {
                    color = Color.BLUE;
                } else if (vid == R.id.lens_view_setting_bg_blk) {
                    color = Color.BLACK;
                } else if (vid == R.id.lens_view_setting_bg_wt) {
                    color = Color.WHITE;
                } else if (vid == R.id.lens_view_setting_bg_ylow) {
                    color = Color.YELLOW;
                } else {
                    return;
                }
                view.setBackgroundColor(color);

            }
        };
        if (ids != null) {
            for (int id : ids) {
                parent.findViewById(id).setOnClickListener(listener);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        boolean isChanged = !info.getValue().toString().equals(s.toString());
        btnApplyChange.setEnabled(isChanged);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        btnApplyChange.setEnabled(isChecked != (boolean) info.getValue());
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_apply) {


            if (info.getValue() instanceof View) {

                View view = (View) info.getValue();
                RadioGroup group = flFieldValue.findViewById(R.id.lens_view_visibl_setting_group);
                if (group != null) {
                    int id = group.getCheckedRadioButtonId();
                    if (id == R.id.lens_view_vis_visible) {
                        view.setVisibility(View.VISIBLE);
                    } else if (id == R.id.lens_view_vis_gone) {
                        view.setVisibility(View.GONE);
                    } else if (id == R.id.lens_view_vis_invisible) {
                        view.setVisibility(View.INVISIBLE);
                    }
                }

                dismiss();
                return;
            }

            Object newValue = null;
            try {
                switch (String.valueOf(info.getType())) {
                    case "boolean": {
                        newValue = swFieldValue.isChecked();
                        break;
                    }
                    case "float": {
                        newValue = Float.parseFloat(etFieldValue.getText().toString());
                        break;
                    }
                    case "int": {
                        newValue = Integer.parseInt(etFieldValue.getText().toString());
                        break;
                    }
                    case "long": {
                        newValue = Long.parseLong(etFieldValue.getText().toString());
                        break;
                    }
                    case "double": {
                        newValue = Double.parseDouble(etFieldValue.getText().toString());
                        break;
                    }
                    case "short": {
                        newValue = Short.parseShort(etFieldValue.getText().toString());
                        break;
                    }
                    case "byte": {
                        newValue = Byte.parseByte(etFieldValue.getText().toString());
                        break;
                    }
                    case "char": {
                        newValue = etFieldValue.getText().toString().charAt(0);
                        break;
                    }
                    default: {
                        newValue = etFieldValue.getText().toString();
                        break;
                    }
                }

                if (fieldHostObject != null) {
                    ReflectTool.setObjValue(fieldHostObject, info.getSimpleName(), newValue);
                }
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(v.getContext(), R.string.lens_please_check_format, Toast.LENGTH_LONG)
                        .show();
            }
        } else if (vid == R.id.btn_cancel) {
            dismiss();
        } else if (vid == R.id.lesn_view_edit_remove_children) {
            removeChildren();

        } else if (vid == R.id.lesn_view_edit_remove_parent) {
            removeParent();
        } else if (vid == R.id.btn_push) {
            String value = null;
            if (info.getType() == String.class && info.getValue() != null) {
                value = " = \"" + info.getValue() + "\"";
            } else if (info.getValue() instanceof View) {
                value = "";
            } else {
                value = " = " + info.getType();
            }
            DataTransferManager.getInstance().push2Web("\n" + info.getType() + " " + info.getSimpleName() + value + "\n");
        }
    }

    private View getValueView() {
        if (info.getValue() instanceof View) {

            View view = (View) info.getValue();
            RadioGroup group = flFieldValue.findViewById(R.id.lens_view_visibl_setting_group);
            if (group != null) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.lens_view_vis_visible) {
                    view.setVisibility(View.VISIBLE);
                } else if (id == R.id.lens_view_vis_gone) {
                    view.setVisibility(View.GONE);
                } else if (id == R.id.lens_view_vis_invisible) {
                    view.setVisibility(View.INVISIBLE);
                }
            }

            return view;
        }

        return null;
    }

    private void removeChildren() {
        View view = getValueView();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            group.removeAllViews();
        }

    }

    private void removeParent() {
        View view = getValueView();
        if (view != null && view.getParent() instanceof ViewGroup) {
            try {
                ((ViewGroup) view.getParent()).removeView(view);
            } catch (Exception e) {
                Toast.makeText(getContext(), "remove from parent fail", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
