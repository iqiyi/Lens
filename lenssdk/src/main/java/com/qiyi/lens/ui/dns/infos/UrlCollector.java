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
package com.qiyi.lens.ui.dns.infos;

import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.Invalidate;

public class UrlCollector extends HttpInfo {

    public UrlCollector(Invalidate par) {
        super(par);
    }

    public void addHostInfo(HostInfo info) {
        infos.add(info);
    }


    @Override
    public String toString() {
        return "总共 " + infos.size() + " 项";
    }

    @Override
    public boolean isBasicType() {
        return false;
    }


    public HostInfo getHostInfo(String host) {
        for (Info info : infos) {
            HostInfo hostInfo = (HostInfo) info;
            if (host.equals(hostInfo.host)) {
                return hostInfo;
            }
        }
        return null;
    }


}
