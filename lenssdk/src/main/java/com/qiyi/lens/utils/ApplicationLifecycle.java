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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qiyi.lens.dynamic.LensContext;
import com.qiyi.lens.ui.devicepanel.DeviceInfoPanel;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;

import java.lang.ref.WeakReference;


public class ApplicationLifecycle {
    private Context mContext;
    private int panelWidth;
    private DeviceInfoPanel panel;
    private int activityCount = 0;
    private static ApplicationLifecycle lifeCircles;
    private WeakReference<Activity> activityWeakReference;
    public static final int PANEL_STATE_MIN = 0; //最小化
    public static final int PANEL_STATE_SHOW = 1; //展开
    public static final int PANEL_STATE_CLOSE = 2; //关闭
    public static int myDefaultPanelState = PANEL_STATE_SHOW;
    private Handler handler;
    private final String TAG = "Lens_life_circle";
    private boolean floatingWindowPermissionState;

    public ApplicationLifecycle(Context context) {
        this.mContext = context.getApplicationContext();
        handler = new Handler(Looper.getMainLooper());

    }

    // fix bug : 重复创建实例导致的问题
    public static ApplicationLifecycle create(Context context) {
        if (lifeCircles == null) {
            lifeCircles = new ApplicationLifecycle(context);
        }
        return lifeCircles;
    }

    public void watchLifeCircle(final int panelWidth, int state, @Nullable Activity activity) {
        myDefaultPanelState = state;
        this.panelWidth = panelWidth;
        Context context = mContext;
        floatingWindowPermissionState = Utils.hasFloatingWindowPermission();
        registerAppActions();


        Application application;
        if (context instanceof LensContext) {
            application = ((LensContext) context).getApplication();
        } else {
            application = (Application) context.getApplicationContext();
        }
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if (panel == null || activityCount == 0) {
                    //[fix: 热启动没有启动数据的问题]
                    TimeStampUtil.obtain(LensConfig.LAUNCH_TIME_STAMP_NAME).addStamp();
                }

                activityCount++;
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                handleActivityResume(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                activityCount--;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (activityCount == 0 && panel != null) {
                            panel.dismiss();
                            panel = null;
                        }
                    }
                });
            }
        });

        if (activity != null) {
            handleActivityResume(activity);
        }

    }

    /**
     * 1) check permission change : if float panel permission is permitted ,  change to floating window mode
     */
    private void handleActivityResume(Activity activity) {

        //  check permission change :
        boolean floatPermission = Utils.hasFloatingWindowPermission();
        if (floatPermission != floatingWindowPermissionState) {
            // todo change window mode
            floatingWindowPermissionState = floatPermission;
            if(panel!= null) {
                panel.dismiss();
            }
            panel = null;
        }

        activityWeakReference = new WeakReference<>(activity);
        TimeStampUtil stampUtil = TimeStampUtil.obtainNullable();
        if (stampUtil != null) {
            stampUtil.addStamp();
        }
        DataPool.obtain().putData(DataPool.DATA_TYPE_ACTIVITY, activity);

        if (myDefaultPanelState != PANEL_STATE_CLOSE) {
            if (panel == null) {
                showDevicePanel();
            } else { // do reattach
                //TODO 后续可以尝试 hook startActivity 使用 Share Element Transition 来处理
                panel.reattach(activity);
            }

        }
    }

    public static ApplicationLifecycle getInstance() {
        return lifeCircles;
    }

    public Activity getCurrentActivity() {
        if (activityWeakReference != null) {
            return activityWeakReference.get();
        }

        return null;
    }

    public Context getContext() {
        return mContext;
    }

    public DeviceInfoPanel getPanel() {
        return panel;
    }


    private Runnable realShowPanel = new Runnable() {
        @Override
        public void run() {
            if (panel == null) {
                panel = new DeviceInfoPanel(ApplicationLifecycle.this.panelWidth);
                panel.setInitState(myDefaultPanelState);
                if (myDefaultPanelState != PANEL_STATE_CLOSE) {
                    panel.show();
                }
            }
        }
    };


    /**
     * 为了不影响性能只有第一次启动的时候 才延迟; 后续都是不延迟的.
     */
    private void showDevicePanel() {
        // 为了不影响性能，延迟 500ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.post(realShowPanel);
            }
        }, 500);
    }

    /**
     * show lens floating window
     */
    public void show() {

        myDefaultPanelState = ApplicationLifecycle.PANEL_STATE_SHOW;
        if (panel == null) {
            showDevicePanel();
        } else {
            panel.show();
        }
    }

    private void registerAppActions() {

        EventBus.registerEvent(new DataCallBack() {
            @Override
            public void onDataArrived(Object data, int type) {
                LL.d("lens-hook", " hook exception found ");
                SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_LENS_CAN_HOOK, false, getContext());
            }
        }, DataPool.EVENT_ID_HOOK_SP_SAVE);
    }

}
