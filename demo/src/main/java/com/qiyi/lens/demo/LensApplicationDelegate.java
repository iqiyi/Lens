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
package com.qiyi.lens.demo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.qiyi.lens.Lens;
import com.qiyi.lens.LensUtil;
import com.qiyi.lens.demo.actions.BlockFactory;
import com.qiyi.lens.demo.actions.JumpAction;
import com.qiyi.lens.demo.dump.Dump;
import com.qiyi.lens.demo.dump.MyDumpFactory;
import com.qiyi.lens.dump.IDebugStatusChanged;
import com.qiyi.lens.utils.LL;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.TimeStampUtil;
import com.qiyi.lens.utils.configs.DisplayConfiguration;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lens.utils.configs.ViewInfoConfig;
import com.qiyi.lens.utils.iface.INetConfig;

import static android.content.Context.MODE_PRIVATE;

/**
 * for test purpose: no need use delegate like this
 */
public class LensApplicationDelegate {
    public static int count;
    public static String str = "nid";
    private Application mApplication;
    private boolean mLensInited;
    private Object abc = new Object() {
        @Dump
        public String iLikeDump() {
            return "this is a dump from obj";
        }
    };
    private String[] customJumpEntrance = {
            "小视频DBG",
    };

    private String[] customBlockEntrance = {
    };


    public LensApplicationDelegate(Application application) {
        mApplication = application;
    }

    public void attachBaseContext(Context context) {
//        LogcatHook.javaHook();
        TimeStampUtil.obtain(LensConfig.LAUNCH_TIME_STAMP_NAME).addStamp();
        TimeStampUtil.obtain(LensConfig.LAUNCH_TIME_STAMP_NAME).setEndViewId(android.R.id.content);

        LensUtil.addABTest("tets_Int", new int[]{0, 2, 5, 11, 22, 3, 44, 55, 66, 77, 88, 999, 9999, 99999, 99999, 999999});
        LensUtil.addABTest("test_String", new String[]{"aaa", "bbb", "ccc"});
        LensUtil.addABTest("test_boolean");
        LensUtil.addABTest("test_select_int", new int[]{});

        NetworkAnalyzeConfig.getInstance().setNetConfig(new INetConfig() {
            @Override
            public String loadTestEnvironmentData() {
                return null;
            }

            @Override
            public String loadUrlFilterData() {
                return null;
            }
        });

    }

    public void onCreate() {
//        LogcatHook.javaHook();
        NetworkAnalyzeConfig.getInstance().setUrlGrabEnabled(true);
        ViewInfoConfig.getInstance().setViewInfoHandler(ViewInfoHandle.class);
        LensUtil.setDumper(MyDumpFactory.class);
        LensUtil.setReporter(new LensReporter(mApplication));
        LensUtil.buildConfig()
                .defaultOpen(false)
//                .enableDeviceInfo(true)
//                .enableKeyLog(KeyLogConfig.builder().addFilter("Main").setMaxLine(1000))
//                .enableFPS(true)
//                .enableLaunchTime(true)
//                .setHookFrameWorkImpl(new HookFramework())
                .enableActivityAnalyzer(true)
                .enableNetworkAnalyze(false)
                .enableCrashInfo(true)
                .addCustomBlockEntrance(customBlockEntrance, new BlockFactory())
                .addCustomJumpEntrance(customJumpEntrance, new JumpAction())
                .initAsPluginMode(Lens.isSDKMode())
                .enableViewInfo(true)
                .show(Lens.wrapContext(mApplication), UIUtils.getScreenWidth(mApplication) / 5 * 3);
        mLensInited = true;
        NetworkAnalyzeConfig.getInstance().setDefaultUrlKeyFilter("sdk");
        NetworkAnalyzeConfig.getInstance().setDefaultUrlFilter("push");
        NetworkAnalyzeConfig.getInstance().addNetRequestWatch("push", new NetworkAnalyzeConfig.RequestWatch() {
            @Override
            public void onRequest(String url) {
                LL.d("l", "jsj");
            }
        });

        LensUtil.setUIVerifyFactory(UIVerify.class);
        LensUtil.setViewClickDebugHandle(ClickHandle.class);
        LensUtil.watchField("count", this);
        LensUtil.watchField("str", this);
        LensUtil.setDebugStatusChanged(new IDebugStatusChanged() {
            @Override
            public void onDebugChanged(boolean currentEnabled) {
                Toast.makeText(mApplication, "log = " + currentEnabled, Toast.LENGTH_SHORT).show();
            }
        });

        DisplayConfiguration.obtain()
                .setDisplayHeight(300)
                .setCustomDisplay(Display.class)
                .setRefreshDuration(500);

        mApplication.getSharedPreferences("test", MODE_PRIVATE);
    }

    @Dump
    public String dump() {
        return "mApplication=" + mApplication + '\n' + "mLensInited=" + mLensInited;
    }
}
