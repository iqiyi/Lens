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

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.widget.tableView.TableBuilder;
import com.qiyi.lens.ui.widget.tableView.TableView;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.Utils;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lens.utils.iface.INetConfig;
import com.qiyi.lenssdk.R;

import static com.qiyi.lens.ui.dns.DNSSetting.SP_KEY_DIRECT_LINK_ENABLE;
import static com.qiyi.lens.ui.dns.DNSSetting.SP_KEY_DNS_INFO;
import static com.qiyi.lens.ui.dns.DNSSetting.SP_KEY_FILTER_INFO;
import static com.qiyi.lens.ui.dns.DNSSetting.SP_KEY_FILTER_WHITE_LIST;


public class DNSSettingPanel extends FullScreenPanel {
    private TableView tableView;
    private TableBuilder builder;
    private TableBuilder filterBuilder;
    private int itemPadding = 20;
    private int itemMinHeight = 40;
    private int itemMinWidth = 80;
    private CheckBox checkBox;
    private CheckBox filterCheckBox;
    private TableListener dnsListener;
    private TableListener filterListener;
    private ImageView whiteListBox;


    public DNSSettingPanel(FloatingPanel panel) {
        super(panel);
        itemPadding = UIUtils.dp2px(context, 20);
        itemMinHeight = UIUtils.dp2px(context, itemMinHeight);
        itemMinWidth = UIUtils.dp2px(context, itemMinWidth);
        setTitle(R.string.lens_panel_net_setting);
    }

    @Override
    public View onCreateView(ViewGroup viewGroup) {
        return inflateView(R.layout.lens_dns_setting, viewGroup);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        View patternView = findViewById(R.id.dns_setting_pattern_switch);
        patternView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNDSSettingSwitch(v);
            }
        });


        checkBox = (CheckBox) findViewById(R.id.dns_setting_checkbox);
        checkBox.setChecked(DNSSetting.isDNSEnabled(getContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleDNSEnable(isChecked);
            }
        });


        if (DNSSetting.isDirectLinkEnabled(getContext())) {
            patternView.setSelected(true);
        }


        whiteListBox = (ImageView) findViewById(R.id.dns_setting_pattern_switch_white_list);
        if (whiteListBox != null) {
            whiteListBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleWhiteListSet(v);
                }
            });
            whiteListBox.setSelected(DNSSetting.isWhiteListEnabled(getContext()));
        }

        filterCheckBox = (CheckBox) findViewById(R.id.filter_setting_checkbox);
        filterCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleFilterEnabled(isChecked);
            }
        });

        filterCheckBox.setChecked(DNSSetting.isFilterEnabled(getContext()));


        TableView filterTableView = (TableView) findViewById(R.id.filter_table_view);
        buildUrlFilterTableView(filterTableView);

        tableView = (TableView) findViewById(R.id.dns_table_view);
        builder = TableBuilder.obtain();
        dnsListener = new TableListener(builder);
        // test enviranment
        builder.setColumnCountRowCount(2, 1)
                .setColumnNames(new String[]{"host", "IP"})
                .setColumnNamesColor(getColor(R.color.lens_panel_default_dark_color)).setNamesTextSize(18)
                .setStretchableColumns(0,1)
                .enableExtraCol().setTableView(tableView).setStrokeWidth(3, 5)
                .setDataBinder(new TableBuilder.ItemDataBinder() {
                    @Override
                    public void bindData(String data, View view, int row, int column) {
                        if (row < 0) {
                            if (column < 2) {
                                TextView textView = (TextView) view;
                                textView.setText(data);
                            } else {
                                TextView textView = (TextView) view;
                                textView.setText(" + ");
                                textView.setOnClickListener(dnsListener.getAddLis());
                            }
                        } else if (column < 2) {
                            TextView textView = (TextView) view;
                            textView.setText(data);
                        }

                    }

                    @Override
                    public View createItemView(ViewGroup parent, int row, int column) {
                        if (column == 2 && row >= 0) {
                            //[control View]
                            return createControlView(dnsListener);
                        } else if (row < 0) {
                            return builder.createDefaultNamesView(getContext());
                        } else {
                            EditText textView = new EditText(getContext());
                            textView.setTextColor(Color.BLACK);
                            textView.setTextSize(13);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(itemPadding, 0, itemPadding, 0);
                            textView.setFocusable(false);
                            textView.setMinWidth(itemMinWidth);
                            textView.setMinHeight(itemMinHeight);
                            return textView;
                        }
                    }
                });
        builder.build(getContext());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
//                tryHttprequest();
                // load data form SP

                String filter = SharedPreferenceUtils.getSharedPreferences(SP_KEY_FILTER_INFO, getContext());
                if (filter.length() == 0) {
                    String urlFilter = NetworkAnalyzeConfig.getInstance().getDefaultUrlGrabFilter();
                    if (!Utils.isEmpty(urlFilter)) {
                        filter = urlFilter;
                    } else {
                        // try get from INetConfig
                        INetConfig config = NetworkAnalyzeConfig.getInstance().getNetConfig();
                        if (config != null) {
                            filter = config.loadUrlFilterData();
                        }
                    }
                }

                // load test evn data form sp
                String dns = SharedPreferenceUtils.getSharedPreferences(SP_KEY_DNS_INFO, getContext());
                String[] d;
                if (dns.length() == 0) {
                    d = loadLocalDNSData();
                } else {
                    d = Utils.string2Array(dns);
                }

                final String[] data = d;
                final String[] flt = Utils.string2Array(filter);
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        displayData(data, flt);
                    }
                });
            }
        });
    }

    private void displayData(String[] data, String[] filters) {
        if (isAdded) {
            builder.setData(data);
            builder.build(getContext());
            filterBuilder.setData(filters);
            filterBuilder.build(getContext());
        }
    }


    public void onDataChanged(TableBuilder builder) {
        if (builder == filterBuilder) {//[filter data changed]
            DNSSetting.updateFilterMap(filterBuilder.getData());
        } else if (builder == this.builder) {//[dns data changed]
            DNSSetting.updateDNSMap(builder.getData());
        }

    }

    /**
     * read from config setting
     * then from INetConfig
     */
    private String[] loadLocalDNSData() {
        String hots = NetworkAnalyzeConfig.getInstance().getDefaultIPHosts();
        if (Utils.isEmpty(hots)) {
            INetConfig config = NetworkAnalyzeConfig.getInstance().getNetConfig();
            if (config != null) {
                hots = config.loadTestEnvironmentData();
            }
        }
        String[] ss = new String[0];

        if (!Utils.isEmpty(hots)) {
            String[] vars = hots.split(",");
            if (vars.length > 0) {
                String[] ar = new String[ss.length + vars.length];
                int p = 0;
                String[] var6 = ss;
                int var7 = ss.length;

                int var8;
                String s;
                for (var8 = 0; var8 < var7; ++var8) {
                    s = var6[var8];
                    ar[p++] = s;
                }

                var6 = vars;
                var7 = vars.length;

                for (var8 = 0; var8 < var7; ++var8) {
                    s = var6[var8];
                    ar[p++] = s;
                }

                return ar;
            }
        }

        return ss;
    }

    private TextView createControlTextView() {
        TextView textView = (TextView) inflateView(R.layout.lens_text_view, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, -2);
        int mg = UIUtils.dp2px(getContext(), 6);
        clp.leftMargin = mg;
        clp.rightMargin = mg;
        textView.setLayoutParams(clp);
        return textView;
    }

    private View createControlView(TableListener listener) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        TextView editView = createControlTextView();
        editView.setText(" ↓ ");
        editView.setOnClickListener(listener.getEditLis());

        TextView deleView = createControlTextView();
        deleView.setText(" — ");
        deleView.setOnClickListener(listener.getDeleteLis());

        linearLayout.addView(editView);
        linearLayout.addView(deleView);
        linearLayout.setMinimumHeight(itemMinHeight);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        return linearLayout;
    }

    private void handleDNSEnable(boolean enable) {
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                showToast("退出程序再次进入生效");
            }
        });

        if (enable) {
            showToast("enabled");
            EventBus.onDataArrived(true, DataPool.EVENT_DNS_SET_CHANGE);
            SharedPreferenceUtils.setSharedPreferences(DNSSetting.SP_KEY_DNS_ENABLE, true, getContext());
            DNSSetting.enableDNSSetting(getContext());
        } else {
            showToast("disabled");
            EventBus.onDataArrived(false, DataPool.EVENT_DNS_SET_CHANGE);
            SharedPreferenceUtils.setSharedPreferences(DNSSetting.SP_KEY_DNS_ENABLE, false, getContext());
            DNSSetting.disableDNSSetting();
        }
    }

    private void handleFilterEnabled(boolean enable) {
        if (enable) {
            SharedPreferenceUtils.setSharedPreferences(DNSSetting.SP_KEY_FILTER_ENABLE, true, getContext());
            DNSSetting.enableFilterSetting(getContext());
        } else {
            SharedPreferenceUtils.setSharedPreferences(DNSSetting.SP_KEY_FILTER_ENABLE, false, getContext());
            DNSSetting.disableFilterSetting();
        }
    }

    private void buildUrlFilterTableView(TableView tableView) {
        filterBuilder = TableBuilder.obtain();
        filterListener = new TableListener(filterBuilder);
        filterBuilder.setTableView(tableView)
                .enableExtraCol()
                .setColumnNames(new String[]{"hosts"})
                .setStretchableColumns(0)
                .setColumnCountRowCount(1, 0)
                .setColumnNamesColor(getColor(R.color.lens_panel_default_dark_color)).setStrokeWidth(3, 5)
                .setDataBinder(new TableBuilder.ItemDataBinder() {
                    @Override
                    public void bindData(String data, View view, int row, int column) {
                        if (row < 0) {
                            if (column < 1) {
                                TextView textView = (TextView) view;
                                textView.setText(data);
                            } else {
                                TextView textView = (TextView) view;
                                textView.setText(" + ");
                                textView.setOnClickListener(filterListener.getAddLis());
                            }
                        } else if (column < 1) {
                            TextView textView = (TextView) view;
                            textView.setText(data);
                        }

                    }

                    @Override
                    public View createItemView(ViewGroup parent, int row, int column) {
                        if (column == 1 && row >= 0) {
                            //[control View]
                            return createControlView(filterListener);
                        } else if (row < 0) {
                            return filterListener.getBuilder().createDefaultNamesView(getContext());
                        } else {
                            EditText textView = new EditText(getContext());
                            textView.setTextColor(Color.BLACK);
                            textView.setTextSize(13);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(itemPadding, 0, itemPadding, 0);
                            textView.setFocusable(false);
                            textView.setMinWidth(itemMinWidth);
                            textView.setMinHeight(itemMinHeight);
                            return textView;
                        }
                    }
                })
                .build(getContext());

    }

    class TableListener {
        TableBuilder builder;

        public TableListener(TableBuilder bld) {
            this.builder = bld;
        }

        private void rotate(View view, int from, int to) {
            RotateAnimation rotateAnimation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setFillBefore(true);
            rotateAnimation.setDuration(300);
            view.startAnimation(rotateAnimation);
        }


        private boolean saveData(View view) {
            TableRow row = (TableRow) builder.getTableRow(view);
            int count = builder.getColCount();
            String[] data = new String[builder.getColCount()];
            if (row != null && count > 0) {

                for (int i = 0; i < count; i++) {
                    EditText editText = (EditText) row.getChildAt(i);
                    data[i] = editText.getText().toString();
                }
                if (count == 1 || Utils.isValidIP(data[count - 1])) {
                    builder.saveData(row, data);
                    builder.printData();
                    return true;
                }
            }
            return false;
        }

        private void enableEdit(View v, boolean enable) {
            if (enable) {
                rotate(v, 0, 90);
            } else {
                rotate(v, 90, 0);
            }
            View view = builder.getTableRow(v);
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                int count = group.getChildCount();
                boolean hasfocus = false;
                for (int i = 0; i < count; i++) {
                    View item = group.getChildAt(i);
                    item.setFocusable(enable);
                    item.setFocusableInTouchMode(enable);
                    if (enable && !hasfocus) {
                        hasfocus = true;
                        EditText editText = (EditText) item;
                        editText.setSelection(editText.getText().length());
                        item.requestFocus();
                    }
                }

            }
        }

        View.OnClickListener editLis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder != null) {
                    if (v.isActivated()) {
                        if (saveData(v)) {
                            DNSSettingPanel.this.onDataChanged(builder);
                            enableEdit(v, false);
                        } else {

                            DNSSettingPanel.this.showToast("IP 地址有误");
//                            Toast.makeText(v.getContext(),"IP 地址有误",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        enableEdit(v, true);
                    }
                    v.setActivated(!v.isActivated());

                }

            }
        };

        View.OnClickListener deleteLis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder != null) {
                    builder.deleteRow(v);
                    DNSSettingPanel.this.onDataChanged(builder);
                }
            }
        };

        View.OnClickListener addLis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder != null) {
                    builder.addRow(null);
                }

            }
        };

        public View.OnClickListener getAddLis() {
            return addLis;
        }

        public View.OnClickListener getEditLis() {
            return editLis;
        }

        public View.OnClickListener getDeleteLis() {
            return deleteLis;
        }

        public TableBuilder getBuilder() {
            return builder;
        }
    }

    @Override
    public void onDismiss() {
        super.onDismiss();
        //[save data]
        new SaveTask(getContext(), builder.getData(), filterBuilder.getData()).run();
    }

    static class SaveTask {
        String[] dnsData;
        String[] filterData;
        Context appContext;

        public SaveTask(Context context, String[] dns, String[] filter) {
            this.dnsData = dns;
            this.filterData = filter;
            appContext = context;
        }

        public void run() {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    boolean enable = DNSSetting.isHttpDirectLinkEnabled();
                    SharedPreferenceUtils.setSharedPreferences(SP_KEY_DIRECT_LINK_ENABLE, enable ? 1 : 0, appContext);
                    SharedPreferenceUtils.setSharedPreferences(SP_KEY_FILTER_WHITE_LIST, DNSSetting.isWhiteListEnabled(), appContext);

                    //[check data]
                    if (dnsData != null && dnsData.length > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        int len = dnsData.length;
                        for (int i = 0; i < len; ) {
                            String s1 = dnsData[i++];
                            String s2 = dnsData[i++];
                            if (!Utils.isEmpty(s1) && !Utils.isEmpty(s2)) {
                                // do nothing
                                stringBuilder.append(s1.trim());
                                stringBuilder.append(',');
                                stringBuilder.append(s2.trim());
                                stringBuilder.append(',');
                            }
                        }
                        if(stringBuilder.length() > 0) {
                            stringBuilder.setLength(stringBuilder.length() - 1);
                        }
                        SharedPreferenceUtils.setSharedPreferences(SP_KEY_DNS_INFO, stringBuilder.toString(), appContext);
                    } else {
                        SharedPreferenceUtils.setSharedPreferences(SP_KEY_DNS_INFO, "", appContext);
                    }

                    if (filterData != null && filterData.length > 0) {
                        int len = filterData.length;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < len; ) {
                            String s1 = filterData[i++];
                            if (!Utils.isEmpty(s1)) {
                                stringBuilder.append(s1);
                                stringBuilder.append(',');
                            }
                        }
                        stringBuilder.setLength(stringBuilder.length() - 1);
                        SharedPreferenceUtils.setSharedPreferences(SP_KEY_FILTER_INFO, Utils.array2String(filterData), appContext);
                    } else {
                        SharedPreferenceUtils.setSharedPreferences(SP_KEY_FILTER_INFO, "", appContext);
                    }


                }
            });
        }
    }


    private void handleNDSSettingSwitch(View view) {
        boolean isOn = !view.isSelected();
        view.setSelected(isOn);
        if (isOn) {
            DNSSetting.setDNSSettingMode(1);
        } else {
            DNSSetting.setDNSSettingMode(0);
        }
    }

    private void handleWhiteListSet(View view) {
        boolean isOn = !view.isSelected();
        view.setSelected(isOn);
        if (isOn) {
            DNSSetting.setWhiteListEnabled(true);
        } else {
            DNSSetting.setWhiteListEnabled(false);
        }
    }

}
