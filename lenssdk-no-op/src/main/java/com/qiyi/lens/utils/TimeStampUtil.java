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

/**
 * 线程同步问题
 */
public class TimeStampUtil {

    public TimeStampUtil(String k) {

    }

    public void addStamp() {
    }

    public void addStamp(String name) {
    }

    public void addStamp(int key) {
    }

    public void addStamp(int key, long stamp , String name) {
    }

    public void addStamp(int key, long stamp) {
    }



    public int getSize() {
        return 0;
    }

    public void end() {
    }

    //[

    public static com.qiyi.lens.utils.TimeStampUtil obtain(String key) {
        return new com.qiyi.lens.utils.TimeStampUtil(key);
    }

    public static com.qiyi.lens.utils.TimeStampUtil obtainNullable(String key) {
        return null;
    }


    public static boolean addNullableStamp(String stampName , int  key) {
        return false;
    }





    //only to add a stamp here
    public static boolean addNullableStamp(String stampName) {

        return false;
    }


    public static boolean addNullableLaunchStamp(){
        return false;
    }

    public static boolean addNullableLaunchStamp(int key){

        return false;
    }

    /**
     *
     * @param stampName
     * @param tagName  display for a function
     * @param key
     * @return
     */
    public static boolean addNullableStamp(String stampName  , int  key,  long stamp , String tagName) {
        return false;
    }

    /**
     *
     * @param stampName
     * @param tagName  display for a function
     * @param key
     * @return
     */
    public static boolean addNullableStamp(String stampName  , int  key, String tagName) {
        return false;
    }


    public long getTotalTime() {
        return 0;
    }


    //[check form top to bottom]

    /**
     * 算法原理： 从顶向下便利，如果发现方法id 相同，不管多少个都标记为 同一个执行方法域；标记为方法执行间隔时间；
     * 因此有多个的时候，将会标记相隔最久的一个；
     * 原理存在漏洞： 如何区分重入方法；
     * v2: 采用外部传入标记方法区间；方法重入计算执行时间间隔问题
     * test case ：[[[1]]];   [1][1][][1]
     * 方法废弃； 在stamp info 里面重新实现；
     *
     * @param stringBuilder
     */
    private void buildIntervals(StringBuilder stringBuilder) {

    }

    public String build() {
        return "";
    }
    //]

    public void setEndViewId(int id) {

    }
    public void setEndViewIds(int[] ids) {
    }


    //[defalt return true: if not set]
    public boolean isTraceEndView(int vid) {
        return false;
    }


    /**
     * 新增提供一些列的不需要提供stamp key 的函数
     *
     * @param key
     */
    public static void setDefaultStampKey(String key) {
    }

    public static TimeStampUtil obtain() {
        return obtain(LensConfig.LAUNCH_TIME_STAMP_NAME);
    }

    public static TimeStampUtil obtainNullable() {
        return null;
    }

    public static boolean addNullableStamp(int key) {
        return false;
    }


    //only to add a stamp here
    public static boolean addNullableStamp() {
        return false;
    }

    public static boolean addNullableStamp(int key, long stamp, String tagName) {
        return false;
    }
    /**
     * @param tagName   display for a function
     * @param key
     * @return
     */
    public static boolean addNullableStamp(int key, String tagName) {
        return false;
    }

    public static void startStamp(String tagName) {
    }

    public static void stopStamp(String tagName) {
    }

    public void stopAndPost(){
    }


}
