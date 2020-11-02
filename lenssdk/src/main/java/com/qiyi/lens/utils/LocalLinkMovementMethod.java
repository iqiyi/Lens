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
package com.qiyi.lens.utils;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class LocalLinkMovementMethod extends LinkMovementMethod {

    private float slop;
    private float dx;
    private float dy;
    private boolean isMoved;
    private MovementMethod su;


    public LocalLinkMovementMethod() {
        su = ScrollingMovementMethod.getInstance();
    }

    public static LocalLinkMovementMethod getInstance() {
        return Holder.instance;
    }

    @Override //[modify , if moved , do not trigger spannable clickable click event]
    public boolean onTouchEvent(TextView widget,
                                Spannable buffer, MotionEvent event) {


        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            isMoved = false;
            slop = ViewConfiguration.getTouchSlop();
        } else if (isMoved) {
            return su.onTouchEvent(widget, buffer, event);
        }

        //[super class logic]
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            dx = x;
            dy = y;

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

            if (links.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    links[0].onClick(widget);

                } else {
                    su.onTouchEvent(widget, buffer, event);
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        } else if (action == MotionEvent.ACTION_MOVE) { //[new added logic]
            //[move]
            if (Math.abs(x - dx) > slop || Math.abs((y - dy)) > slop) {
                isMoved = true;
            }
            dx = x;
            dy = y;
        }
        return su.onTouchEvent(widget, buffer, event);
    }

    private static class Holder {
        static LocalLinkMovementMethod instance = new LocalLinkMovementMethod();
    }
}
