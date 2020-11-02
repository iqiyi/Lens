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
package com.qiyi.lens.demo.actions;

import com.qiyi.lens.ui.devicepanel.blockInfos.AbsBlockInfo;
import com.qiyi.lens.utils.iface.ICustomBlockFactory;

/**
 * Lens 浮窗上支持自由添加展示模块的接口。 未测试，未支持
 */
public class BlockFactory implements ICustomBlockFactory {
    // called when a block is switch open
    @Override
    public AbsBlockInfo createBlockInfo(String key) {
        return null;
    }

    @Override
    public boolean onBlockSwitchChange(String key, boolean open) {
        return false;
    }
}
