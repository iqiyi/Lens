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
package com.qiyi.lens.dynamic;

import androidx.annotation.RestrictTo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


@RestrictTo(RestrictTo.Scope.LIBRARY)
public class IoUtils {

    public static void copyStream(InputStream is, File file) throws IOException {
        if (is == null || file == null) {
            return;
        }
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs() && !file.exists() && !file.createNewFile()) {
            throw new IOException("failed to create file " + file.getAbsolutePath());
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 64];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } finally {
            close(is, fos);
        }
    }

    public static void copyFile(File src, File desc) throws IOException {
        copyStream(new FileInputStream(src), desc);
    }

    public static String readURL(String url) throws IOException {
        InputStream is = null;
        try {
            is = new URL(url).openConnection().getInputStream();
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } finally {
            close(is);
        }
    }

    public static void saveURL(String url, File file) throws IOException {
        InputStream is = new URL(url).openConnection().getInputStream();
        copyStream(is, file);
    }

    public static void close(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                try {
                    if (closeable != null) {
                        closeable.close();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static void rm(File file) {
        if (file.isFile()) {
            if (!file.delete()) {
                Log.w("lens", "delete file failed " + file);
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    rm(f);
                }
            }
        }
    }
}
