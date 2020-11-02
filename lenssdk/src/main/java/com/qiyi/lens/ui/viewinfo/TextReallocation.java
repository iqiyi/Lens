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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.LinkedList;

//[只检测一级碰撞。只挪动一次]
//原理： 如果和上一个碰撞，则上一个移动，递归检测上一个。 如果仍然有碰撞，不管
public class TextReallocation {

    int _swd;
    int _sht;
    private SelectWidgetView _host;
    private java.util.LinkedList<TextInfo> list = new LinkedList<TextInfo>();

    TextReallocation(SelectWidgetView host, int swd, int sht) {
        this._swd = swd;
        this._sht = sht;
        _host = host;
    }

    public void clear() {
        list.clear();
    }


    //[try to add info at x, y]
    void measure(TextInfo info, float x, float y) {

        if (info instanceof MultiTextInfo) {
            RectF rect = _host.getCurrentViewRect();
            if(rect != null) {
                info.setRange(rect.left, rect.right, rect.top, rect.bottom);
                info.measure(x, y);
                int wd = info.getWidth();
                int ht = info.getHeight();
                if (wd > rect.width() || ht > rect.height()) {
                    info.setRange(0, _swd, 0, _sht);
                    info.measure(x, y);
                }
            }
        } else {
            info.measure(x, y);
        }

        reArrange(info);
        list.add(info);
    }

    //[位置重新排列]

    /**
     * add this into to list , and re-arrange this list
     * add this info in the front
     */
    private void reArrange(TextInfo info) {
        for (TextInfo in : list) {
            if (in.needAdjust(info)) {
                info = in;
            }
        }
    }

    public void draw(Canvas canvas) {
        if (!list.isEmpty()) {
            for (TextInfo in : list) {
                in.draw(canvas);
            }
        }
    }

}
