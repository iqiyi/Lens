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

import android.os.AsyncTask;
import android.util.Log;

public class SimpleTask<Params, Result> extends AsyncTask<Params, Void, Result> {

    private static final String TAG = "SimpleTask";

    private Callback<Params, Result> callback;

    private Callback<Params, Result> getCallback() {
        return callback;
    }

    public SimpleTask(Callback<Params, Result> callback) {
        this.callback = callback;
    }

    @Override
    protected final void onPreExecute() {

    }

    @Override
    protected final Result doInBackground(Params[] params) {
        if (getCallback() != null) {
            try {
                return getCallback().doInBackground(params);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            Log.w(TAG, "doInBackground: getCallback() == null");
        }
        return null;
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (getCallback() != null) {
            try {
                getCallback().onPostExecute(result);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            callback = null;
        } else {
            Log.w(TAG, "onPostExecute: getCallback() == null");
        }
    }

    public interface Callback<T, K> {
        K doInBackground(T[] params);

        void onPostExecute(K result);
    }
}
