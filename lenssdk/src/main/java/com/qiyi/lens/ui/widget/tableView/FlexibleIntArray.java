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
package com.qiyi.lens.ui.widget.tableView;

public class FlexibleIntArray {
    private int[] ar;
    private int size;
    private int capacity;

    public FlexibleIntArray() {
        size = 0;
        ar = new int[6];
        capacity = 6;

    }

    public void add(int a) {
        if (size == capacity) {
            // enlarge buffer and copy data
            int[] aar = new int[capacity << 1];
            if (capacity >= 0) System.arraycopy(ar, 0, aar, 0, capacity);
            ar = aar;
            capacity = aar.length;
        }

        ar[size] = a;
        size++;

    }

    public int size() {
        return size;
    }

    public int get(int i) {
        if (i < size) {
            return ar[i];
        }
        return 0;
    }

    public boolean contains(int a) {
        for (int i = 0; i < size; i++) {
            if (ar[i] == a) {
                return true;
            }
        }

        return false;
    }

    public void addIfNotExist(int a) {
        if (!contains(a)) {
            add(a);
        }

    }
}
