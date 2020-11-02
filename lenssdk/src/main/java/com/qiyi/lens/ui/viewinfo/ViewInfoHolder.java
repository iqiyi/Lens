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


public class ViewInfoHolder {

    private static ViewInfoHolder sInstant;

    public static ViewInfoHolder getInstant() {
        if (sInstant == null) {
            sInstant = new ViewInfoHolder();
        }
        return sInstant;
    }

    private Widget currentWidget;
    private WidgetSelectCallback widgetSelectCallback;

    private ViewInfoHolder() {
    }

    public void setCurrentWidget(Widget currentWidget) {
        this.currentWidget = currentWidget;
        if (widgetSelectCallback != null) {
            widgetSelectCallback.onWidgetSelect(currentWidget);
        }
    }

    public Widget getCurrentWidget() {
        return currentWidget;
    }

    public void setWidgetSelectCallback(WidgetSelectCallback widgetSelectCallback) {
        this.widgetSelectCallback = widgetSelectCallback;
    }

    public interface WidgetSelectCallback {
        void onWidgetSelect(Widget widget);
    }
}
