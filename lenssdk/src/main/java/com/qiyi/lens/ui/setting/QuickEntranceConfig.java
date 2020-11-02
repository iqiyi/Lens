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
package com.qiyi.lens.ui.setting;

import android.content.Context;

import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lenssdk.R;

public class QuickEntranceConfig implements SettingGridAdapter.IConfigDataBuilder {
    private int[] resArray = {
            R.string.lens_block_app_developer_option,
            R.string.lens_block_app_setting_detail,
            R.string.lens_block_app_change_language,
            R.string.lens_setting_system_setting,
            R.string.lens_setting_wifi_setting,
            R.string.lens_setting_date_setting
    };


    public EventViewItem[] buildItems(Context context, ConfigEventCallBack changed, int columCount) {

        String[] jumps = LensConfig.getInstance().getCustomJumpNames();
        String[] orgin = toStringArray(context, resArray);
        if (jumps == null || jumps.length == 0) {
            return buildItems(context, changed, columCount, orgin, resArray);
        } else {

            int size = orgin.length + jumps.length;
            String[] var = new String[size];
            System.arraycopy(orgin, 0, var, 0, orgin.length);
            System.arraycopy(jumps, 0, var, orgin.length, jumps.length);

            int[] ids = new int[size];
            System.arraycopy(resArray, 0, ids, 0, resArray.length);

            return buildItems(context, changed, columCount, var, ids);
        }

    }


    private EventViewItem[] buildItems(Context context, ConfigEventCallBack changed, int columCount, String[] names, int[] events) {
        int count = names.length;
        int off = count % columCount;
        int size = count;
        if (off > 0) {
            size = count + (columCount - off);
        }
        EventViewItem[] items = new EventViewItem[size];
        int i = 0;
        while (i < count) {
            items[i] = new EventViewItem(changed, names[i], events[i]);
            i++;
        }

        return items;
    }


    private String[] toStringArray(Context context, int[] nameArray) {
        String[] names = new String[nameArray.length];
        int p = 0;
        int lens = nameArray.length;
        while (p < lens) {
            names[p] = context.getResources().getString(nameArray[p]);
            p++;
        }
        return names;
    }


}
