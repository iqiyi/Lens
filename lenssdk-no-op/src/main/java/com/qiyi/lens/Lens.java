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
package com.qiyi.lens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.RestrictTo;

import com.qiyi.lens.dynamic.ExceptionHandler;
import com.qiyi.lens.dynamic.IoUtils;
import com.qiyi.lens.dynamic.LensClassLoader;
import com.qiyi.lens.dynamic.LensContext;
import com.qiyi.lens.dynamic.LensDownloader;
import com.qiyi.lens.noop.BuildConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Lens {
    private static final String SP_KEY_VERSION = "version";
    private static final String SP_KEY_LAST_LAUNCH_VERSION = "last.launch.version";
    private static final String FILE_LENS_READY_FLAG = "lens.ready";

    public static final String VERSION = BuildConfig.VERSION_NAME;
    private static Resources sLensResources;
    private static boolean sDebug;
    private static boolean sLoadPluginSuccess;
    private static final List<String> sDownloadConfigUrls = new ArrayList<>();
    private static String sPreferAbi;

    public static void saveVersion(Context context, String version) {
        SharedPreferenceUtils.set(SP_KEY_VERSION, version, context);
        try {
            new File(context.getFilesDir(), FILE_LENS_READY_FLAG).createNewFile();
        } catch (IOException e) {
            ExceptionHandler.throwIfDebug(e);
        }
    }

    public static String readVersion(Context context, String defaultValue) {
        return SharedPreferenceUtils.getString(SP_KEY_VERSION, defaultValue, context);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static boolean isDebug() {
        return sDebug;
    }

    public static void setPreferAbi(String preferAbi) {
        sPreferAbi = preferAbi;
    }

    @SuppressLint("SimpleDateFormat")
    public static void init(Context context, boolean debug) {
        if (context != null) {
            try {
                // fast fail. if not debug mode and lens is not enabled.
                if (!debug && !new File(context.getFilesDir(), FILE_LENS_READY_FLAG).exists()) {
                    return;
                }
                sDebug = debug;
                // if Lens VERSION upgrade. delete Compatible plugin
                if (isFirstLaunchInVersion(context)) {
                    revokeCompatiblePlugin(context);
                }
                // try load lens.
                String version = readVersion(context, VERSION);
                File debugApk = null;
                if (debug) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                            || context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        debugApk = new File(Environment.getExternalStorageDirectory(), "lens.apk");
                        if (debugApk.exists()) {
                            version = VERSION + new SimpleDateFormat("_MMddHHmmss").format(debugApk.lastModified());
                        }
                    }
                }
                File plugFile = getPluginFile(context, version);
                if (!plugFile.exists()) {
                    plugFile = getCompatiblePluginFile(context);
                }
                if (debug && !plugFile.exists() && debugApk != null && debugApk.exists()) {
                    IoUtils.copyFile(debugApk, plugFile);
                }
                if (plugFile.exists()) {
                    /* 必须在 hook classLoader 前获取 LensDownloader 否则会使用插件内部的 placeholder */
                    LensDownloader downloader = LensDownloader.get(context);
                    sLoadPluginSuccess = tryLoadPlugin(context, plugFile.getAbsolutePath(), plugFile.getParentFile().getAbsolutePath());

                    if (sLoadPluginSuccess) {
                        downloader.check();
                    }
                }
            } catch (Throwable t) {
                ExceptionHandler.throwIfDebug(t, "lenssdk", "load apk failed");
            }
        }
    }

    private static boolean isFirstLaunchInVersion(Context context) {
        String lastLaunchVersion = SharedPreferenceUtils.getString(SP_KEY_LAST_LAUNCH_VERSION, null, context);
        if (Lens.VERSION.equals(lastLaunchVersion)) {
            return false;
        } else {
            SharedPreferenceUtils.set(SP_KEY_LAST_LAUNCH_VERSION, Lens.VERSION, context);
            return true;
        }
    }

    private static void revokeCompatiblePlugin(Context context) {
        File compatiblePluginFile = getCompatiblePluginFile(context);
        if (compatiblePluginFile.exists()) {
            if (!compatiblePluginFile.delete()) {
                ExceptionHandler.throwIfDebug(new IOException(), "lenssdk", "delete Compatible plugin fail");
            }
            SharedPreferenceUtils.set(SP_KEY_VERSION, null, context);
            LensUtil.showManually(context);
        }
    }


    public static boolean isLoadPluginSuccess() {
        return sLoadPluginSuccess;
    }

    public static void showManually(Context context) {
        if (sLoadPluginSuccess) {
            try {
                LensClassLoader.get().loadClass("com.qiyi.lens.LensUtil")
                        .getMethod("showManually", Context.class)
                        .invoke(null, context);
            } catch (IllegalAccessException e) {
                ExceptionHandler.throwIfDebug(e);
            } catch (InvocationTargetException e) {
                ExceptionHandler.throwIfDebug(e);
            } catch (NoSuchMethodException e) {
                ExceptionHandler.throwIfDebug(e);
            } catch (ClassNotFoundException e) {
                ExceptionHandler.throwIfDebug(e);
            }
        } else {
            LensUtil.showManually(context);
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static File getPluginFile(Context context, String version) {
        String targetName = "lensSDK-" + version + ".apk";
        File dataDir = context.getDir("lens", Context.MODE_PRIVATE);
        return new File(dataDir, targetName);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static File getCompatiblePluginFile(Context context) {
        File dataDir = context.getDir("lens", Context.MODE_PRIVATE);
        return new File(dataDir, "lensSDK-compatible.apk");
    }

    /**
     * 读取后端返回的版本号，如果后端返回的是当前版本的插件，则是完全匹配模式，否则是兼容模式
     * 兼容模式有一个限制：在宿主升级后需要重新评估兼容性，不能沿用之前版本的插件
     *
     * @param pluginVersion 插件版本号
     * @return 是否需要使用兼容模式
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static boolean inCompatibleMode(String pluginVersion) {
        //后端可能返回了小版本号，需要认为是完全兼容的
        return !pluginVersion.startsWith(Lens.VERSION);
    }

    //[try load plugin at sd card]
    private static boolean tryLoadPlugin(Context context, String apk, String optDir) {
        ZipFile zip = null;
        try {
            ClassLoader origin = Lens.class.getClassLoader();
            if (origin == null) {
                throw new NullPointerException("ClassLoader is null");
            }
            // remove old version
            removeOldVersion(apk);
            // extract native lib
            zip = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("lib/")) {
                    InputStream is = zip.getInputStream(entry);
                    IoUtils.copyStream(is, new File(optDir, entry.getName()));
                }
            }
            // create standalone resources
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(apk, 0);
            if (packageInfo != null && packageInfo.applicationInfo != null) {
                packageInfo.applicationInfo.publicSourceDir = apk;
                packageInfo.applicationInfo.sourceDir = apk;
                sLensResources = context.getPackageManager().getResourcesForApplication(packageInfo.applicationInfo);
            }
            // hook classloader
            new LensClassLoader(apk, optDir, createLibrarySearchPath(optDir + "/lib")).hook();
            return true;
        } catch (Throwable e) {
            ExceptionHandler.throwIfDebug(e);
        } finally {
            IoUtils.close(zip);
        }

        return false;
    }

    private static void removeOldVersion(String apkPath) {
        List<File> toDeleteApk = new ArrayList<>();
        List<File> toDeleteDir = new ArrayList<>();
        File dir = new File(apkPath).getParentFile();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getAbsolutePath().equals(apkPath)) {
                        continue;
                    }
                    if (file.isFile() && file.getName().endsWith(".apk")) {
                        toDeleteApk.add(file);
                    }
                    if (file.isDirectory()) {
                        toDeleteDir.add(file);
                    }
                }
            }
        }
        if (toDeleteApk.isEmpty()) {
            // 没有旧版本 apk，说明不需要处理旧版本数据
            return;
        }
        List<File> toDelete = new ArrayList<>();
        toDelete.addAll(toDeleteApk);
        toDelete.addAll(toDeleteDir);
        for (File file : toDelete) {
            IoUtils.rm(file);
        }
    }

    private static String createLibrarySearchPath(String baseLibPath) {
        List<String> searchPaths = new ArrayList<>();
        // 添加客户端指定的 abi 在第一位
        searchPaths.add(new File(baseLibPath, sPreferAbi).getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (String supportedAbi : Build.SUPPORTED_ABIS) {
                File abi = new File(baseLibPath, supportedAbi);
                if (abi.exists()) {
                    searchPaths.add(abi.getAbsolutePath());
                }
            }
        } else {
            searchPaths.add(new File(baseLibPath, Build.CPU_ABI).getAbsolutePath());
            searchPaths.add(new File(baseLibPath, Build.CPU_ABI2).getAbsolutePath());
        }
        // 添加 armeabi 兜底
        String armeabiPath = new File(baseLibPath, "armeabi").getAbsolutePath();
        if (!searchPaths.contains(armeabiPath)) {
            searchPaths.add(armeabiPath);
        }
        return TextUtils.join(File.pathSeparator, searchPaths);
    }


    public static Context wrapContext(Context context) {
        // fast fail if plugin is not loaded.
        if (!sLoadPluginSuccess) {
            return context;
        }

        // should load LensContext from apk
        if (LensContext.class.getClassLoader() instanceof LensClassLoader) {
            return new LensContext(context, sLensResources);
        } else {
            // android 9 会读取到 no-op 里面的 LensContext. 这里使用反射
            try {
                if (LensClassLoader.get() != null) {
                    Class<?> lensContext = LensClassLoader.get().loadClass("com.qiyi.lens.dynamic.LensContext");
                    return (Context) lensContext.getConstructor(Context.class, Resources.class).newInstance(context, sLensResources);
                }
                // plugin is not ready
                return new LensContext(context, sLensResources);
            } catch (Throwable e) {
                throw new RuntimeException("wrap LensContext failed. check your proguard for LensContext?", e);
            }
        }
    }

    public static void addDownloadConfigUrl(String url) {
        sDownloadConfigUrls.add(url);
    }

    public static void addDownloadConfigUrl(int position, String url) {
        sDownloadConfigUrls.add(position, url);
    }

    public static List<String> getDownloadConfigUrls() {
        return sDownloadConfigUrls;
    }

    public static boolean isSDKMode() {
        return false;
    }

    public static void addDefaultIPHosts(String ipHosts) {

    }

}
