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

import android.Manifest;
import android.content.Context;
import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class FileUtils {


    public static File createFile(String path, boolean checkPermission) {
        if (path != null) {
            File file = new File(path);
            if (file.getParentFile() == null) return null;
            try {
                file.getParentFile().mkdirs();
                // make sure , 都会进行权限验证 保证文件写出成功
                if (!checkPermission || Utils.checkPermission(ApplicationLifecycle.getInstance().getCurrentActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    return file;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public static File createFile(File fileDir, String fileName) {
        File file = null;
        if (fileDir == null || fileName == null || fileName.isEmpty()) {
            return file;
        }
        if (fileDir.isDirectory()) {
            String path = fileDir.getPath() + "/" + fileName;
            file = new File(path);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }


    public static boolean writeStringToFile(String dirName, String fileName, String message) {
        File file = createFilePath(dirName, fileName);
        if (file == null) {
            return false;
        }
        return writeStringToFile(file, message);
    }

    public static boolean writeStringToFile(File file, String message) {
        if (file == null) {
            return false;
        }
        FileWriter fr = null;
        try {
            fr = new FileWriter(file);
            fr.write(message);
            fr.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    public static String getFilePath(String dirName, String fileName) {
        Context context = ApplicationLifecycle.getInstance().getContext();
        String path;
        if (OSUtils.isPreQ()) {
            path = Environment.getExternalStorageDirectory() + "/" + dirName + "/" + context.getPackageName() + "/" + fileName;
        } else {
            path = context.getExternalFilesDir(dirName).getAbsolutePath() + "/" + fileName;
        }
        return path;
    }

    public static File createFilePath(String dirName, String fileName) {
        String path = getFilePath(dirName, fileName);
        if (!Utils.isEmpty(path)) {
            return createFile(path, true);
        }
        return null;
    }

    public static String getFilePrivate(String dir, String fileName) {
        Context context = ApplicationLifecycle.getInstance().getContext();
        String path = null;
        if (context != null) {
            File file = context.getExternalFilesDir(null);
            if (file != null) {
                path = file.getAbsolutePath() + "/" + dir + "/" + fileName;
            }
        }

        return path;
    }

    /**
     * 文件创建在内部存储上
     */
    public static File createFilePrivate(String dir, String fileName) {
        String path = getFilePrivate(dir, fileName);
        if (path != null) {
            return createFile(path, false);
        }
        return null;
    }


    public static boolean writeStringToFile(File fileDir, String fileName, String obj) {
        FileWriter fr = null;
        try {
            File extFile = createFile(fileDir, fileName);
            if (extFile != null) {
                fr = new FileWriter(extFile);
                fr.write(obj);
                fr.flush();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            FileUtils.closeSafely(fr);
        }
        return true;
    }


    /**
     * 拷贝文件
     */
    public static void writeFile(String path, FileInputStream inputStream) {
        FileOutputStream outputStream = null;
        try {

            outputStream = new FileOutputStream(new File(path));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 读取文件
     *
     * @param path 文本文件
     * @return 文件内容
     */
    public static String file2String(String path) {
        String result = "";
        if (path == null || path.length() == 0) {
            return result;
        }

        File file = new File(path);
        if (!file.exists()) {
            return result;
        }

        InputStreamReader reader = null;
        StringWriter writer = new StringWriter();


        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis);
            // 将输入流写入输出流
            char[] buffer = new char[4096];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            result = writer.toString();
        } catch (IOException e) {
//            ExceptionUtils.printStackTrace(e);
        } finally {
            silentlyCloseCloseable(reader);
            silentlyCloseCloseable(writer);
            silentlyCloseCloseable(fis);
        }
        return result;
    }


    public static String file2String(File file) {

        String result = "";
        if (file == null || !file.exists()) {
            return result;
        }

        InputStreamReader reader = null;
        StringWriter writer = new StringWriter();


        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis);
            // 将输入流写入输出流
            char[] buffer = new char[4096];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            result = writer.toString();
        } catch (IOException e) {
//            ExceptionUtils.printStackTrace(e);
        } finally {
            silentlyCloseCloseable(reader);
            silentlyCloseCloseable(writer);
            silentlyCloseCloseable(fis);
        }
        return result;
    }

    /**
     * 关闭数据流.
     */
    public static void silentlyCloseCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public static void closeSafely(Closeable closeable) {
        if (closeable != null) {
            silentlyCloseCloseable(closeable);
        }
    }


}
