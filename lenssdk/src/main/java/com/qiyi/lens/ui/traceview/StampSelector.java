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
package com.qiyi.lens.ui.traceview;

//[用于标记当前的选中状态， 只在图上展示已选中的内容的]
//stamp 选中后 将竖线 变高，变色；并执行闪烁动画
//block 选中后，block 显示为高亮色；并执行闪烁动画；
class StampSelector {
    private int _type = -1, _index = -1;

    public StampSelector() {
    }

    public boolean setSelected(int type, int index) {
        if (isSelected(type, index)) {
            return false;
        }
        _type = type;
        _index = index;
        return true;
    }

    public boolean isSelected(int type, int index) {
        return _type == type && index == _index;
    }

}
