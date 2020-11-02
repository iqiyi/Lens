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

public class FlexibleIntArray {
    private int capacity = 10;
    private int[] arr;
    private int size;

    public FlexibleIntArray(int capacity) {
        this.capacity = capacity;
        arr = new int[this.capacity];
        size = 0;
    }

    public FlexibleIntArray() {
        arr = new int[capacity];
        size = 0;
    }

    public int size() {
        return size;
    }

    public void push(int a) {
        if (size >= capacity) {
            int[] ar = new int[capacity + 10];
            if (capacity >= 0) System.arraycopy(arr, 0, ar, 0, capacity);
            arr = ar;
            capacity = capacity + 10;
        }

        arr[size] = a;
        size++;
    }

    public int get(int index) {
        return arr[index];
    }

    public void indexAutoIncrease(int index) {
        if (index < size) {
            arr[index] += 1;
        } else {
            push(1);
        }
    }


    public int[] getArr() {
        return arr;
    }


    public int indexOf(int value) {
        if (arr != null) {
            int p = 0;
            int len = size;
            while (p < len) {
                if (arr[p] == value) {
                    return p;
                }
                p++;
            }
        }
        return -1;
    }

    public void removeFormer(int beforeIndex) {
        if (size <= beforeIndex) {
            size = 0;
        } else {//[smaller than size]
            int p = beforeIndex;
            int len = size;
            int i = 0;
            while (p < len) {
                arr[i] = arr[p];
                i++;
                p++;
            }

        }
    }
}
