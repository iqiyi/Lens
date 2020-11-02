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
package com.qiyi.lens.ui.viewtree;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.viewinfo.CurrentViewInfoPanel;
import com.qiyi.lenssdk.R;

import java.util.Iterator;
import java.util.LinkedList;

public class ViewTreePanel extends FullScreenPanel {
    private ListView listView;
    private TAdapter adapter;
    private ViewList viewList;
    private CurrentViewInfoPanel analysePanel;
    private ViewPager viewPager;
    private ViewDraw imageView;

    public ViewTreePanel(FloatingPanel panel) {
        super(panel);
        setTitle(R.string.lens_panle_ac_view_tree_title);
    }

    @Override
    public View onCreateView(ViewGroup group) {
        View view = inflateView(R.layout.lens_view_tree_layout, group);
//        listView = view.findViewById(R.id.lens_tree_content_list);
        viewPager = view.findViewById(R.id.lens_view_pager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public @NonNull
            View instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    if (listView == null) {
                        listView = new ListView(container.getContext());
                    }
                    listView.setAdapter(adapter);
//                    listView.setLayoutParams(new ViewPager.LayoutParams());
                    container.addView(listView);
                    return listView;
                } else {
                    if (imageView == null) {
                        imageView = new ViewDraw(container.getContext());
                        imageView.setRef(adapter.rootView);
                    }
                    imageView.setLayoutParams(new ViewPager.LayoutParams());
                    container.addView(imageView);
                    return imageView;
                }
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                if (position == 0) {
                    container.removeView(listView);
                } else {
                    container.removeView(imageView);
                }
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        });
        return view;
    }


    public void setData(View root) {
        adapter = new TAdapter(root);
        viewList = new ViewList(root);

    }


    public void setMaxLevelView(View maxLevelView, int level) {
        if (maxLevelView != null) {
            viewList = new ViewList();
            adapter = new TAdapter(viewList.reversToRoot(maxLevelView, level));
        }


    }

    @Override
    public void onShow() {
        super.onShow();

    }

    @Override
    public void onDismiss() {

    }

    private static int colors[] = {
            Color.MAGENTA,
            0xffFF7F00,
            0xffBA55D3,
            0xff2dbb55,
            0xffF82ABF,
            0xff00FA9A
//
//            0xffADFF2F,
//            0xff8DEEEE,
//            0xff87CEFF,
//            0xff4876FF,
//            0xff00FA9A,
//            0xff00868B,
//            0xffFF82AB,
    };

    private class TAdapter extends BaseAdapter {
        View rootView;

        TAdapter(View view) {
            this.rootView = view;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public ViewNode getItem(int position) {
            return viewList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                convertView = inflateView(R.layout.lens_tree_item_view, parent);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.setData(getItem(position));
            return convertView;
        }
    }

    private class Holder implements View.OnClickListener {
        public TextView info;
        private ViewNode node;
        private View root;

        public Holder(View view) {
            info = view.findViewById(R.id.lens_tree_item_info);
            root = view;
            view.findViewById(R.id.lens_tree_item_analyse).setOnClickListener(this);
            view.findViewById(R.id.lens_tree_item_view).setOnClickListener(this);

        }

        public void setData(ViewNode ref) {
            this.node = ref;
            View value = ref.view;

            String className = value.getClass().getSimpleName();
            StringBuilder stringBuilder = new StringBuilder();
            int p = 0;
            while (p < node.level) {
                stringBuilder.append(" . ");
                p++;
            }

            info.setTextColor(colors[(node.level + colors.length) % colors.length]);
            if (node.view instanceof ViewGroup) {
                root.setOnClickListener(this);
            } else {
                root.setOnClickListener(null);
            }

            if (node.isOpen) {
                stringBuilder.append('-');
            } else if (node.view instanceof ViewGroup) {
                stringBuilder.append('+');
            } else {
                stringBuilder.append(' ');
            }

            stringBuilder.append(className);
            stringBuilder.append(" ");
            View view = (View) value;
            if (view.getVisibility() != View.GONE) {
                if (view.getVisibility() == View.INVISIBLE) {
                    stringBuilder.append("INVISIBLE");
                }
                stringBuilder.append('[')
                        .append(view.getLeft() + " " + view.getTop() + "  " + view.getRight() + "  " + view.getBottom())
                        .append(']');
            } else {
                stringBuilder.append("GONE");
            }

            info.setText(stringBuilder.toString());

        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.lens_tree_item_info || root == v) {
                viewList.onClick(node);
            } else if (id == R.id.lens_tree_item_analyse) {
                if (analysePanel == null) {
                    analysePanel = new CurrentViewInfoPanel(null);
                }
                analysePanel.setDataView(node.view, true);
                analysePanel.show();
            } else if (id == R.id.lens_tree_item_view) {
                imageView.select(node.view);
                viewPager.setCurrentItem(1);
            }

        }
    }

    private static class ViewNode {
        public View view;
        boolean isOpen;
        public int level;

        ViewNode(View node) {
            this.view = node;
            this.level = 0;
            this.isOpen = false;
        }
    }

    private class ViewList {
        LinkedList<ViewNode> nodes;
        boolean enableClick;

        ViewList(View root) {
            nodes = new LinkedList<>();
            nodes.add(new ViewNode(root));
            enableClick = true;

        }

        ViewList() {

            enableClick = false;
        }

        View reversToRoot(View maxLevel, final int level) {

            nodes = new LinkedList<>();

            View root = null;
            int p = level;
            if (maxLevel != null) {
                Object temp = maxLevel;
                while (temp instanceof View) {

                    View view = (View) temp;
                    root = view;
                    ViewNode node = new ViewNode(view);
                    if (level == p) {
                        node.isOpen = false;
                    } else {
                        node.isOpen = true;
                    }
                    node.level = p--;
                    nodes.addFirst(node);
                    temp = view.getParent();
                }

            }

            return root;
        }


        public void onClick(ViewNode node) {

            if (enableClick) {
                if (node.isOpen) {
                    //[close ]
                    //[remove all leve below this node]
                    node.isOpen = false;
                    removeUnder(node);
                } else {
                    node.isOpen = true;
                    addNodes(node);
                }
            }
        }

        private void removeUnder(ViewNode node) {
            Iterator<ViewNode> iterable = nodes.iterator();
            boolean started = false;

            while (iterable.hasNext()) {
                ViewNode nd = iterable.next();
                if (nd == node) {
                    started = true;
                } else if (started) {
                    if (nd.level > node.level) {
                        iterable.remove();
                    } else {
                        break;
                    }
                }

            }

            adapter.notifyDataSetChanged();

        }

        public int size() {
            return nodes.size();
        }

        public ViewNode get(int position) {
            return nodes.get(position);
        }

        //[add node after: check final]
        private void addNodes(ViewNode node) {
            View view = node.view;
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;

                int index = nodes.indexOf(node);

                int count = group.getChildCount();
                for (int i = count - 1; i >= 0; i--) {
                    View child = group.getChildAt(i);
                    ViewNode vn = new ViewNode(child);
                    vn.level = node.level + 1;
                    nodes.add(index + 1, vn);
                }

                if (count > 0) {
                    adapter.notifyDataSetChanged();
                }
            }

        }

    }

    @Override
    public boolean onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
            return true;
        } else {
            return false;
        }
    }


}
