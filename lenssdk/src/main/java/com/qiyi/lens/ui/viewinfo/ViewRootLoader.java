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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.UiThread;

import com.qiyi.lens.ui.viewinfo.json.Array;
import com.qiyi.lens.ui.viewinfo.json.IJson;
import com.qiyi.lens.ui.viewinfo.json.JsonCompiler;
import com.qiyi.lens.ui.viewinfo.uicheck.DrawingStack;
import com.qiyi.lens.ui.viewinfo.uicheck.ILoadCallback;
import com.qiyi.lens.ui.widget.FrameAnimation;
import com.qiyi.lens.ui.widget.FullScreenFrameLayout;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.LensReflectionTool;
import com.qiyi.lens.utils.StateLatch;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;
import com.qiyi.lenssdk.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * load  view root of this window manager
 */
public class ViewRootLoader {


    private static View[] parseViews(Object var) {
        if (var instanceof View[]) {
            return (View[]) var;
        } else if (var instanceof List) {
            List list = (List) var;
            int size = list.size();
            View[] views = new View[size];
            list.toArray(views);
            return views;
        }
        return null;
    }

    /**
     * 返回的只是当前WM 下，顶级的窗口
     */
    public static View getCurrentTopView() {

        WindowManager windowManager = (WindowManager) ApplicationLifecycle.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);
        Object var = LensReflectionTool.fieldChain(windowManager, new String[]{
                "mGlobal",
                "mViews"
        });
        if (var != null) {
            View[] mViews = parseViews(var);
            if (mViews == null || mViews.length == 0) return null;
            for (int p = mViews.length - 1; p >= 0; p--) {
                View view = mViews[p];
                WindowManager.LayoutParams wlp = (WindowManager.LayoutParams) view.getLayoutParams();
                if (wlp == null || wlp.type == WindowManager.LayoutParams.TYPE_TOAST) continue;
                if (view instanceof FullScreenFrameLayout || view instanceof FrameAnimation || view.getId() == R.id.lens_device_panel_root) {
                    continue;
                }
                return view;
            }
        }

        Activity activity = ApplicationLifecycle.getInstance().getCurrentActivity();
        if (activity != null) {
            return activity.getWindow().getDecorView();
        }
        return null;
    }


    public static DrawingStack loadByRootView(View contentView) {
        DrawingStack stack = new DrawingStack();
        if (contentView == null) return stack;
        stack.loadViews(contentView, new DrawingStack.ViewFilter() {
            @Override
            public boolean filterView(View view) {
                if ((view instanceof FullScreenFrameLayout || view instanceof FrameAnimation)) {
                    return true;
                } else if (view.getVisibility() != View.VISIBLE) {
                    return true;
                } else if (view.getWidth() == 0 || view.getHeight() == 0) {
                    return true;
                } else { // 剔除系统资源
                    int id = view.getId();
                    return id == R.id.lens_device_panel_root || id == android.R.id.statusBarBackground || id == android.R.id.navigationBarBackground;
                }
            }
        });
        return stack;
    }

    public static boolean isActivityRoot(View view) {
        Activity activity = ApplicationLifecycle.getInstance().getCurrentActivity();
        return activity != null && activity.getWindow().getDecorView() == view;
    }

    /**
     * 优先使用当前选中的视图，遍历进行验收。
     * 否则使用当前Activity 的根部视图进行UI 验收。
     */
    @UiThread // in case of view changed
    public static void prepareUIData(final IUIVerifyFactory factory, final View currentView, final ILoadCallback callback) {

        final DrawingStack stack;
        if (currentView == null) {
            View rootView = getCurrentTopView();
            if (rootView != null && isActivityRoot(rootView)) {
                rootView = rootView.findViewById(android.R.id.content);
            }
            stack = loadByRootView(rootView);
        } else {
            stack = new DrawingStack();
            stack.loadViews(currentView);
        }

        final DataLoad stateLatch = new DataLoad(2);
        Runnable jsonRunnable = new Runnable() {
            @Override
            public void run() {
                String value = loadJson(factory, stack);
                stateLatch.setJson(value);
                if (stateLatch.countDown()) {
                    // go
                    callback.onDataLoaded(stateLatch.json, stateLatch.file);
                }
            }
        };

        Runnable bitmapRunnable = new Runnable() {
            @Override
            public void run() {
                String var = loadBitmap(currentView);
                stateLatch.setFile(var);
                if (stateLatch.countDown()) {
                    // go
                    callback.onDataLoaded(stateLatch.json, stateLatch.file);
                }
            }
        };

        Executor executor = LensConfig.getInstance().getThreadPool();
        executor.execute(bitmapRunnable);
        executor.execute(jsonRunnable);

    }

    private static String loadBitmap(View currentView) {

        Bitmap bitmap = null;


        try {
            if (currentView == null) {
                Activity activity = ApplicationLifecycle.getInstance().getCurrentActivity();
                ViewGroup content = activity.findViewById(android.R.id.content);
                if (content != null) {
                    int count = content.getChildCount();
                    int wd = content.getWidth();
                    int ht = content.getHeight();
                    bitmap = Bitmap.createBitmap(wd, ht, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    Drawable drawable = content.getBackground();
                    if (drawable != null) {
                        drawable.draw(canvas);
                    } else {
                        drawable = activity.getWindow().getDecorView().getBackground();
                        if (drawable != null) {
                            drawable.draw(canvas);
                        }
                    }

                    for (int i = 0; i < count; i++) {
                        View view = content.getChildAt(i);
                        canvas.save();
                        canvas.translate(view.getLeft(), view.getTop());
                        view.draw(canvas);
                        canvas.restore();
                    }
                    //save bitmap to file
                    File file = activity.getExternalCacheDir();
                    if (file != null) {
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (file.exists()) {
                            File fl = new File(file.getAbsolutePath(), "lens-ui-veryfy.jpg");
                            try {
                                fl.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                            saveBitmapToFile(bitmap, fl);
                            return fl.getAbsolutePath();
                        }
                    }
                }

            }
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        return null;
    }

    private static void saveBitmapToFile(Bitmap bitmap, File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fileOutputStream);
        try {
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static String loadJson(IUIVerifyFactory factory, DrawingStack stack) {

//        IUIVerifyFactory factory = LensConfig.getInstance().getUIVeryfyFactory();
        JsonCompiler compiler = new JsonCompiler();
        Activity activity = ApplicationLifecycle.getInstance().getCurrentActivity();
        factory.onJsonBuild(activity, compiler);
        IJson[] data = stack.getVisibleViewData(factory);
        Array array = new Array(data);
        compiler.addPair("views", array);
        return compiler.value();

    }


    static class DataLoad extends StateLatch {
        String json;
        String file;

        DataLoad(int size) {
            super(size);
        }

        public void setJson(String var) {
            this.json = var;
        }

        public void setFile(String file) {
            this.file = file;
        }

    }
}
