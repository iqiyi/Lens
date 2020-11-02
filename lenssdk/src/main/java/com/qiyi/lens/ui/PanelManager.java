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
package com.qiyi.lens.ui;

import com.qiyi.lens.Constants;
import com.qiyi.lens.ui.viewinfo.SelectViewPanel;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

public class PanelManager {


    private static PanelManager instance = new PanelManager();
    private LinkedList<Pair> list = new LinkedList<>();

    private PanelManager() {

    }

    public static PanelManager getInstance() {
        return instance;
    }

    /**
     * 添加新的面板
     * 保存浮动窗口的状态。
     */
    void register(BasePanel panel, int type) {
        BasePanel pl = getTopPanel();
        // 业务逻辑：如果新打开的面板不是视图拾取面板，则执行下面的逻辑
        if (type != Constants.PANEL_SELECT_VIEW_PANEL && type != Constants.PANEL_SELECT_VIEW_PANEL_WINDOW) {
            if (pl instanceof SelectViewPanel) {
                pl.dismiss();
            }
            storeFloatingPanel();
            // 视图拾取面板，需要将浮动面板提到最前面
        } else if (panel instanceof SelectViewPanel) {
            SelectViewPanel selectViewPanel = (SelectViewPanel) panel;
            if (pl != null && selectViewPanel.shouldBringTofront()) {
                FloatingPanel floatingPanel = (FloatingPanel) pl;
                floatingPanel.bringToFront();
            }
        }
        list.add(new Pair(panel, type));
    }


    /**
     * 在
     */
    void removePanel(BasePanel panel) {
        if (!list.isEmpty()) {
            Iterator<Pair> iterator = list.iterator();
            LinkedList<BasePanel> panels = new LinkedList<>();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                if (p.matches(panel)) {
                    iterator.remove();
                    if (p.get() != null) {
                        panels.add(p.get());
                    }
                }
            }

            while (!panels.isEmpty()) {
                BasePanel pl = panels.pop();
                pl.dismiss();
            }

        }

        recoverFloatingPanel(panel.getPanelType());
    }

    public void removePanel(int type) {
        if (!list.isEmpty()) {
            Iterator<Pair> iterator = list.iterator();
            LinkedList<BasePanel> panels = new LinkedList<>();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                if (p.matches(type)) {
                    iterator.remove();
                    if (p.get() != null) {
                        panels.add(p.get());
                    }
                }
            }

            while (!panels.isEmpty()) {
                BasePanel pl = panels.pop();
                pl.dismiss();
            }

        }
        recoverFloatingPanel(type);
    }

    //[调用，用于解决偶然出现的面板无法关闭的问题。 在点击滑动浮窗关闭按钮的时候执行]
    public void dismissAllPanels() {
        if (!list.isEmpty()) {
            Iterator<Pair> iterator = list.iterator();
            LinkedList<BasePanel> panels = new LinkedList<>();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                iterator.remove();
                if (p.get() != null) {
                    panels.add(p.get());
                }
            }

            while (!panels.isEmpty()) {
                BasePanel pl = panels.pop();
                pl.dismiss();
            }

        }

    }

    BasePanel getTopPanel() {
        if (!list.isEmpty()) {
            return list.getLast().get();
        }
        return null;
    }

    static class Pair {
        WeakReference<BasePanel> pl;
        int type;

        Pair(BasePanel panel, int type) {
            this.type = type;
            pl = new WeakReference<>(panel);
        }

        boolean matches(BasePanel panel) {
            return pl.get() == null || pl.get() == panel;
        }

        boolean matches(int type) {
            if (pl.get() == null || type == this.type) {
                return true;
            } else if (type == Constants.PANEL_FULL_SCREEN_PANEL) {
                return (this.type & type) != 0;
            }

            return false;

        }

        public BasePanel get() {
            return pl.get();
        }
    }

    private void storeFloatingPanel() {
        BasePanel panel = getTopPanel();
        if (panel != null) {
            panel.onPause();
        }
    }

    private void recoverFloatingPanel(int type) {
        BasePanel panel = getTopPanel();
        if (panel != null) {
            panel.onResume();
        }
    }
}
