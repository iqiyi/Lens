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
package com.qiyi.lens.ui.traceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.LinkedList;

/**
 * 根据手机到的数据： 绘制方法执行的时间戳
 * version: 2: 新增选中功能： 当用户在面板中险种某一个数据的时候，在视图中就高亮的展示出来；
 * 支持点击放大功能；倍数： 2，4 , 8,16,32; 延迟放大：
 * todo: 待优化的实现，先不支持了。
 */
public class TraceView extends View implements View.OnClickListener {
    private TimeStampInfo info;
    private float density;
    private Paint paint, textPaint;
    private Factors factors;
    //[绘制参数]
    int BLOCK_LEVE_EXTRA = 15;
    int BLOCK_BASE = 30;
    final int THREAD_BLOCK_MARGIN = 10;
    final int STROKE_THREAD = 4;
    final int STROKE_STAMP = 4;
    final int STROKE_BLOCK = 2;
    final int COLOR_THREAD = Color.BLACK;
    final int COLOR_STAMP = Color.GREEN;
    final int[] COLOR_BLOCK = new int[]{Color.GRAY, Color.YELLOW, Color.BLUE, Color.RED, Color.MAGENTA, 0x13b1b4};
    int STAMP_HEIGHT = 30;
    int textHeight = 12;
    private int threadBlockHeight;
    private StampSelector selector;
    private final int MAX_LEVEL_DISPLAY = 5;


    //手势操作：[
    //支持水平方向的滑动； 支持放大或者缩小后，仍然以左侧0 点位置对其；
    //支持双手指缩放功能； 以手指中心点缩放
    private float scrollX;
    private GestureDetector detector;

    // 手势操作 end]

    private Runnable INVALIDATE = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };


    public TraceView(Context context) {
        super(context);
        init(context);
    }

    public TraceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    private void init(Context context) {
        paint = new Paint();
        textPaint = new Paint();
        selector = new StampSelector();
        density = context.getResources().getDisplayMetrics()
                .density;
        textHeight = dp2px(textHeight);

        textPaint.setTextSize(textHeight);
        textPaint.setColor(Color.BLACK);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        textHeight = (int) (metrics.bottom - metrics.top);


        STAMP_HEIGHT = dp2px(STAMP_HEIGHT);
        BLOCK_BASE = dp2px(BLOCK_BASE);
        BLOCK_LEVE_EXTRA = dp2px(BLOCK_LEVE_EXTRA);

        detector = new GestureDetector(new GestureListener());
        factors = new Factors();
        setOnClickListener(this);

    }


    public void setTimeStamp(TimeStampInfo stamp) {
        this.info = stamp;
        factors.setTimeTotalRange(stamp.getTimeExpand());
    }


    private int dp2px(int dp) {
        return (int) (dp * density) + 1;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        ThreadInfo[] infos = info.getThreadInfos();
        if (infos != null && infos.length > 0) {
            int index = 0;
            for (ThreadInfo info : infos) {

                int baseLine = factors.getYForThreadByIndex(index);

                drawThread(canvas, baseLine);

                drawTimeBlocks(canvas, info.blocks, baseLine);

                drawStamps(canvas, info.stamps, baseLine);

                paint.setColor(Color.BLACK);
                textPaint.setColor(Color.BLACK);
                canvas.drawText(info.threadName, factors.left, baseLine - dp2px(10), textPaint);

                index++;
            }
        }


    }


    private void drawThread(Canvas canvas, int baseY) {
        paint.setColor(COLOR_THREAD);
        paint.setStrokeWidth(STROKE_THREAD);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(factors.left, baseY, factors.right, baseY, paint);

    }


    //[draw stamp with index : draw with stamp index]
    private void drawStamps(Canvas canvas, LinkedList<TimeStamp> stamps, int baseLine) {
        paint.setColor(COLOR_STAMP);
        paint.setStrokeWidth(STROKE_STAMP);
        paint.setStyle(Paint.Style.STROKE);
        textPaint.setColor(0xffFF00FF);

        if (stamps != null && stamps.size() > 0) {

            //[过滤不必要的绘制]
            long[] timeRange = factors.getTimeRange();

            for (TimeStamp stamp : stamps) {
                if (stamp.timeStamp <= timeRange[1] && stamp.timeStamp >= timeRange[0]) {
                    int x = factors.getXForStamp(stamp.timeStamp);
                    //[get Thread Index]
                    canvas.drawText("S" + stamp.functionIndex, x, baseLine - 6, textPaint);
                    canvas.drawLine(x, baseLine - STAMP_HEIGHT, x, baseLine, paint);
                }
            }

            //[test
//            int x = factores.getXForStamp(info.getTimeExpand()/2);
//            //[get Thread Index]
//            canvas.drawLine(x, baseLine - STAMP_HEGHT - 20, x, baseLine, paint);
            //]
        }

    }


    private int getHeightByLevel(int level) {
        int a = BLOCK_LEVE_EXTRA * (level - 1) + BLOCK_BASE;
        return Math.min(a, threadBlockHeight);
    }


    private void drawTimeBlocks(Canvas canvas, LinkedList<TimeGap> blocks, int baseLine) {
//        paint.setColor(COLOR_BLOCK);
        paint.setStrokeWidth(STROKE_STAMP);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(0xff8A2BE2);

        if (blocks != null && blocks.size() > 0) {

            long[] timeRange = factors.getTimeRange();

            for (TimeGap block : blocks) {

                //[过滤不必要的绘制]
                if (block.timeStamp > timeRange[1] || block.endTime < timeRange[0]) {
                    continue;
                }

                paint.setColor(COLOR_BLOCK[block.level % COLOR_BLOCK.length]);
                int start = factors.getXForStamp(block.timeStamp) + 1;
                int end = factors.getXForStamp(block.endTime) - 1;

                int top = baseLine - getHeightByLevel(block.level);
                if (end <= start) {
                    end = start + 2;
                }
                canvas.drawText("#" + block.functionIndex, start + 4, top, textPaint);
                canvas.drawRect(start, top, end, baseLine, paint);
            }
        }

    }


    @Override
    public void onLayout(boolean changed, int a, int b, int c, int d) {
        super.onLayout(changed, a, b, c, d);


        int pwd = getWidth() - getPaddingLeft() - getPaddingRight();
        int pht = getHeight() - getPaddingTop() - getPaddingBottom();

//        factores = new Factors(pwd, pht, getPaddingLeft() , getPaddingTop(), getPaddingBottom());
        factors.layout(pwd, pht, getPaddingLeft(), getPaddingTop(), getPaddingBottom());
        factors.setBlockHeight(threadBlockHeight);

    }


    @Override
    public void onMeasure(int swd, int sht) {


        //figure out thread block height
        int maxLevel = info.getMaxGapsLevel();
        if (maxLevel > MAX_LEVEL_DISPLAY) {
            maxLevel = MAX_LEVEL_DISPLAY;
        }
        int blockHeight = (maxLevel - 1) * BLOCK_LEVE_EXTRA + BLOCK_BASE;
        int margins = THREAD_BLOCK_MARGIN * 2;//[top & bottom]
        //[文字是直接展示在上面的]
        int min = dp2px(50);
        int threadBlockHeight = blockHeight + margins;
        if (threadBlockHeight < min) {
            threadBlockHeight = min;
        }
        this.threadBlockHeight = threadBlockHeight;

        threadBlockHeight = info.getThreadCount() * threadBlockHeight;

        int wd = MeasureSpec.getSize(swd);
        setMeasuredDimension(wd, threadBlockHeight + getPaddingTop() + getPaddingBottom());

    }


    public void setStampSelected(int type, int index) {
        if (selector.setSelected(type, index)) {
            invalidate();
        }
    }

    @Override
    public void onClick(View v) {
        if (factors.increaceFactorCount()) {
            removeCallbacks(INVALIDATE);
            postDelayed(INVALIDATE, 300);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return detector.onTouchEvent(motionEvent);
    }


    @Override
    public boolean canScrollHorizontally(int dx) {
        return factors.canScroll(dx);
    }

    class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            factors.onDown(e.getX());
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            factors.onScroll(distanceX);
            invalidate();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override //不支持
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }


}
