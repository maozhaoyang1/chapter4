package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    private TimerHandler mHandler;

    //弱引用防止内存泄漏
    private static final class TimerHandler extends Handler {
        private WeakReference<Clock> clockViewWeakReference;
        private TimerHandler(Clock clockView) {
            clockViewWeakReference = new WeakReference<>(clockView);
        }
        @Override
        public void handleMessage(Message msg) {
            Clock view = clockViewWeakReference.get();
            if (view != null) {
                view.invalidate();//重新绘制
            }
        }
    }

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }

    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(hoursValuesColor);
        paint.setTextSize(50f);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        int rEnd = mCenterX - (int) (mWidth * 0.1f);
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;

        for (int i = 0; i < FULL_ANGLE; i += 30 /* Step */) {

            int centerX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int centerY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));
            int baseLineY = (int) (centerY - top / 2 - bottom / 2);
            int x = 12 - ((i / 30 + 9) % 12);
            if(x < 10)
                canvas.drawText("0"+x,centerX,baseLineY,paint);
            else
                canvas.drawText(""+x,centerX,baseLineY,paint);

        }

    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        Paint paint = new Paint();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        //seconds
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(secondsNeedleColor);
        paint.setStrokeWidth(5f);

        int second_angle =( 360 - second * 6) + 90;
        int rEnd = mCenterX - (int) (mWidth * 0.14f);
        float endX = (float) (mCenterX + rEnd * Math.cos(Math.toRadians(second_angle)));
        float endY = (float) (mCenterY - rEnd * Math.sin(Math.toRadians(second_angle)));
        canvas.drawLine(mCenterX,mCenterY,endX,endY,paint);
        //minutes
        paint.setColor(minutesNeedleColor);
        paint.setStrokeWidth(8f);
        double minute_angle = (double) ( 360 - minute * 6) + 90 - second / 60.0 * 6.0;
        //System.out.println("minute_angle="+minute_angle);
        rEnd = mCenterX - (int) (mWidth * 0.2f);
        endX = (float) (mCenterX + rEnd * Math.cos(Math.toRadians(minute_angle)));
        endY = (float) (mCenterY - rEnd * Math.sin(Math.toRadians(minute_angle)));
        canvas.drawLine(mCenterX,mCenterY,endX,endY,paint);
        //hours
        paint.setColor(hoursNeedleColor);
        paint.setStrokeWidth(12f);
        if(hour>12)
                hour-=12;
        double hours_angle =(double) ( 360 - hour * 30) + 90 - minute / 60.0 * 30.0 - second / 3600.0 * 30.0;
        //System.out.println("hours_angle="+hours_angle);
        rEnd = mCenterX - (int) (mWidth * 0.3f);
        endX = (float) (mCenterX + rEnd * Math.cos(Math.toRadians(hours_angle)));
        endY = (float) (mCenterY - rEnd * Math.sin(Math.toRadians(hours_angle)));
        canvas.drawLine(mCenterX,mCenterY,endX,endY,paint);

        //刷新
        mHandler = new TimerHandler(this);
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paint = new Paint();
        //centerInner
        paint.setColor(centerInnerColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX,mCenterY,10,paint);
        //centerOuter
        paint.setColor(centerOuterColor);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mCenterX,mCenterY,15,paint);
    }


    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}
