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
package com.qiyi.lens.ui.viewinfo;

import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.objectinfo.ObjectInfoPanel;
import com.qiyi.lens.ui.viewtree.ViewDraw;
import com.qiyi.lens.ui.viewtree.ViewTreePanel;
import com.qiyi.lens.utils.LocalLinkMovementMethod;
import com.qiyi.lens.utils.configs.DebugInfoConfig;
import com.qiyi.lens.utils.configs.ViewInfoConfig;
import com.qiyi.lens.utils.iface.IViewInfoHandle;
import com.qiyi.lens.utils.iface.ObjectDescription;
import com.qiyi.lens.utils.reflect.AdapterInfo;
import com.qiyi.lens.utils.reflect.CollectionFailFetcher;
import com.qiyi.lens.utils.reflect.ObjectFieldCollector;
import com.qiyi.lens.utils.ReflectTool;
import com.qiyi.lenssdk.R;

public class CurrentViewInfoPanel extends FullScreenPanel implements ObjectFieldCollector.DataRefreshCallback, View.OnClickListener {
    private TextView currentViewInfo;
    private ObjectFieldCollector collector;
    private View mView;
    private View viewTree;
    private boolean isFromViewTree;
    private LinearLayout container;
    private Object[] customValues;
    private int containerIndex;
    private TextView clickDetailView;
    private ClickProxy clickProxy;

    public CurrentViewInfoPanel(FloatingPanel basePanel) {
        super(basePanel);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_current_view_info, viewGroup);
        currentViewInfo = content.findViewById(R.id.detail_info);
        currentViewInfo.setMovementMethod(LocalLinkMovementMethod.getInstance());
        currentViewInfo.setLinksClickable(false);
        currentViewInfo.setClickable(false);
        currentViewInfo.setTextColor(Color.BLACK);


        container = content.findViewById(R.id.lens_current_view_container);
        clickDetailView = content.findViewById(R.id.lens_onclick_event);
        containerIndex = container.getChildCount();
        viewTree = content.findViewById(R.id.panel_ac_info_veiw_tree);
        ViewDraw viewDraw = (ViewDraw) content.findViewById(R.id.lens_view_draw_id);
        viewDraw.setRef(mView);
        viewDraw.select(mView);
        //[set rate ]
        int mwd = viewDraw.getLayoutParams().width;
        viewDraw.getLayoutParams().height = (int) (mwd * mView.getHeight() * 1F / mView.getWidth());


        return content;
    }


    public void setDataView(View view, boolean viewTree) {
        mView = view;
        this.isFromViewTree = viewTree;

    }

    @Override
    public void onShow() {
        super.onShow();

        if ((mView instanceof ViewGroup) && !isFromViewTree) {
            viewTree.setVisibility(View.VISIBLE);
            viewTree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showViewTree();
                }
            });
        }


        if (mView != null) {

            new Thread() {
                @Override
                public void run() {
                    collector = new ObjectFieldCollector(mView, true);

                    collector.setDataRefreshCallBack(CurrentViewInfoPanel.this);
                    final Spannable data = collector.makeSpannable();
                    Handler handler = getMainHandler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Object[] values = loadFeilInfoData();
                            if (values != null) {
                                loadEntranceViews(values);
                            }
                            loadViewData();
                            currentViewInfo.setText(data);

                        }
                    });
                }

            }.start();
        }
    }

    private void showViewTree() {
        ViewTreePanel treePanel = new ViewTreePanel(null);
        treePanel.setData(mView);
        treePanel.show();
        dismiss();
    }


    @Override
    public void onDataRefresh() {

        final Spannable data = collector.makeSpannable();
        currentViewInfo.post(new Runnable() {
            @Override
            public void run() {
                currentViewInfo.setText(data);
            }
        });
    }

    @Override
    public void onDismiss() {
        super.onDismiss();
        if (mView != null && clickProxy != null && clickProxy.handle != null) {
            View.OnClickListener clickListener = clickProxy.handle.get();
            if (clickListener != null) {
                mView.setOnClickListener(clickProxy.handle.get());
            }
        }
        mView = null;
        viewTree = null;
        customValues = null;
    }


    private Object[] handleAdapterInfoLoad(IViewInfoHandle handle, Object selectView, AdapterInfo adapterInfo, int position) {
        Object[] data = null;
        if (adapterInfo.adapter instanceof RecyclerView.Adapter) {
            RecyclerView.Adapter adapter = (RecyclerView.Adapter) adapterInfo.adapter;
            CollectionFailFetcher failFetcher = new CollectionFailFetcher(adapter);
            data = failFetcher.getObjectSizeAndIndexAt(handle, selectView, adapter.getItemCount(), position);
        } else if (adapterInfo.adapter instanceof ListAdapter) {
            ListAdapter adapter = (ListAdapter) adapterInfo.adapter;
            if (adapter.getCount() > 0) {
                Object value = adapter.getItem(position);
                if (value == null) {

                    CollectionFailFetcher failFetcher = new CollectionFailFetcher(adapter);
                    data = failFetcher.getObjectSizeAndIndexAt(handle, selectView, adapter.getCount(), position);
                }
            }

        } else if (adapterInfo.adapter instanceof PagerAdapter) {
            PagerAdapter adapter = (PagerAdapter) adapterInfo.adapter;
            if (adapter.getCount() > 0 && adapter.getCount() > adapterInfo.adapterIndex) {
                CollectionFailFetcher failFetcher = new CollectionFailFetcher(adapter);
                data = failFetcher.getObjectSizeAndIndexAt(handle, selectView, adapter.getCount(), position);
            }

        }

        if (data == null) {
            ObjectDescription description = new ObjectDescription(adapterInfo.adapter, "Adapter : index " + adapterInfo.adapterIndex);
            return new Object[]{description};
        }
        return data;
    }


    private Object[] loadFeilInfoData() {

        AdapterInfo adapterInfo = new AdapterInfo(mView, true);

        Class<? extends IViewInfoHandle> handle = ViewInfoConfig.getInstance().getViewInfoHandle();
        if (handle == null) {

            return handleAdapterInfoLoad(null, mView, adapterInfo, adapterInfo.adapterIndex);
        }

        try {
            IViewInfoHandle handler = (IViewInfoHandle) handle.newInstance();
            Object[] data = handler.onViewSelect(mView, adapterInfo.adapter, adapterInfo.adapterIndex, adapterInfo.childIndex);

            if (data == null) {
                data = handleAdapterInfoLoad(handler, mView, adapterInfo, adapterInfo.adapterIndex);
            }
            return data;
        } catch (InstantiationException var10) {
            var10.printStackTrace();
        } catch (IllegalAccessException var11) {
            var11.printStackTrace();
        }

        return null;
    }

    private void loadEntranceViews(Object[] values) {

        if (container != null) {

            int count = container.getChildCount();
            if (count > containerIndex) {
                //[remove]
                for (int i = count - 1; i >= containerIndex; i--) {
                    container.removeViewAt(i);
                }
            }

            this.customValues = values;
            int index = 0;
            for (Object var : values) {
                if (var != null) {
                    if (var instanceof ObjectDescription && ((ObjectDescription) var).value == null) {
                        continue;
                    }
                    inflateObjectValueView(var, index++, R.layout.lens_item_object_ana_entrance);
                }
            }
        }
    }

    private void loadViewData() {

        if (container == null) {
            return;
        }
        int index = container.getChildCount();

        View view = inflateObjectValueView(mView, index, R.layout.lens_view_object_ana_entrance);

        Class<? extends IViewClickHandle> click = DebugInfoConfig.getInstance().getViewClickHandle();
//        if(click != null) {
        View clickView = view.findViewById(R.id.panel_ac_view_click);
        if (clickView != null) {
            boolean valid = false;
            Object value = ReflectTool.getField(mView, "mListenerInfo");
            if (value != null) {
                Object onClick = ReflectTool.getField(value, "mOnClickListener");
                if (onClick instanceof View.OnClickListener) {

                    if (clickDetailView != null) {
                        clickDetailView.setVisibility(View.VISIBLE);
                        clickDetailView.setText("点击事件：" + onClick.getClass().toString());
                    }

                    if (click != null) {

                        try {
                            valid = true;
                            IViewClickHandle handle = click.newInstance();
                            ClickProxy proxy = new ClickProxy(handle, (View.OnClickListener) onClick);
                            this.clickProxy = proxy;
                            mView.setOnClickListener(proxy);
                            clickView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mView.performClick();
                                }
                            });
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (!valid) {
                clickView.setVisibility(View.GONE);
            }
        }
    }

    private View inflateObjectValueView(Object var, int index, @LayoutRes int layoutId) {
        View group = inflateView(layoutId, container);

        TextView view = (TextView) group.findViewById(R.id.panel_ac_info_activity);
        container.addView(group);
        view.setId(index);
        view.setOnClickListener(this);
        if (var instanceof ObjectDescription) {
            ObjectDescription description = (ObjectDescription) var;

            if (description.value != null) {
                if (description.objectDescription == null || description.objectDescription.length() == 0) {
                    view.setText(description.value.getClass().getSimpleName());
                } else {
                    view.setText(description.toString());
                }
            }
        } else {
            if (var instanceof CharSequence) {
                view.setText((CharSequence) var);
            } else {
                view.setText(var.getClass().getSimpleName());
            }
        }
        return group;
    }

    @Override //[only dor view fail jump ]
    public void onClick(View v) {
        if (customValues != null) {
            int id = v.getId();
            if (id >= 0 && id < customValues.length) {
                Object value = customValues[id];
                showObjectDetail(value);
                return;
            }

        }
        //[show the value of current]
        showObjectDetail(mView);

    }

    private void showObjectDetail(Object value) {
        if (value instanceof ObjectDescription) {
            ObjectInfoPanel.showValue(((ObjectDescription) value).value, takePanel());
        } else {
            ObjectInfoPanel.showValue(value, takePanel());
        }
    }
}
