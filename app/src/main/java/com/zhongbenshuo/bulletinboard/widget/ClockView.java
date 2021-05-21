package com.zhongbenshuo.bulletinboard.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;
import com.zhongbenshuo.bulletinboard.utils.StringUtil;

/**
 * Created by Android_Jian on 2018/11/3.
 */
public class ClockView extends View {

    private static final int DEFAULT_COLOR_LOWER = Color.parseColor("#1d953f");
    private static final int DEFAULT_COLOR_MIDDLE = Color.parseColor("#228fbd");
    private static final int DEFAULT_COLOR_HIGH = Color.RED;
    private static final int DEAFAULT_COLOR_TITLE = Color.BLACK;
    private static final int DEFAULT_TEXT_SIZE_DIAL = 11;
    private static final int DEFAULT_STROKE_WIDTH = 8;
    private static final int DEFAULT_RADIUS_DIAL = 128;
    private static final int DEAFAULT_TITLE_SIZE = 16;
    private static final int DEFAULT_VALUE_SIZE = 28;
    private static final int DEFAULT_ANIM_PLAY_TIME = 2000;

    private float default_value_min = 0;
    private float default_value_max = 100;
    private float default_value_middle_start = 30;
    private float default_value_middle_end = 70;

    private int colorDialLower;
    private int colorDialMiddle;
    private int colorDialHigh;
    private int textSizeDial;
    private int strokeWidthDial;
    private String titleDial;
    private int titleDialSize;
    private int titleDialColor;
    private int valueTextSize;
    private int animPlayTime;

    private int radiusDial;
    private int mRealRadius;
    private float currentValue;
    private String unit = "";

    private Paint arcPaint;
    private RectF mRect;
    private Paint pointerPaint;
    private Paint.FontMetrics fontMetrics;
    private Paint titlePaint;
    private Path pointerPath;

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ClockView);
        colorDialLower = attributes.getColor(R.styleable.ClockView_color_dial_lower, DEFAULT_COLOR_LOWER);
        colorDialMiddle = attributes.getColor(R.styleable.ClockView_color_dial_middle, DEFAULT_COLOR_MIDDLE);
        colorDialHigh = attributes.getColor(R.styleable.ClockView_color_dial_high, DEFAULT_COLOR_HIGH);
        textSizeDial = (int) attributes.getDimension(R.styleable.ClockView_text_size_dial, sp2px(DEFAULT_TEXT_SIZE_DIAL));
        strokeWidthDial = (int) attributes.getDimension(R.styleable.ClockView_stroke_width_dial, dp2px(DEFAULT_STROKE_WIDTH));
        radiusDial = (int) attributes.getDimension(R.styleable.ClockView_radius_circle_dial, dp2px(DEFAULT_RADIUS_DIAL));
        titleDial = attributes.getString(R.styleable.ClockView_text_title_dial);
        titleDialSize = (int) attributes.getDimension(R.styleable.ClockView_text_title_size, dp2px(DEAFAULT_TITLE_SIZE));
        titleDialColor = attributes.getColor(R.styleable.ClockView_text_title_color, DEAFAULT_COLOR_TITLE);
        valueTextSize = (int) attributes.getDimension(R.styleable.ClockView_text_size_value, dp2px(DEFAULT_VALUE_SIZE));
        animPlayTime = attributes.getInt(R.styleable.ClockView_animator_play_time, DEFAULT_ANIM_PLAY_TIME);
    }

    private void initPaint() {
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(strokeWidthDial);

        pointerPaint = new Paint();
        pointerPaint.setAntiAlias(true);
        pointerPaint.setStyle(Paint.Style.FILL);
        pointerPaint.setTextSize(textSizeDial);
        pointerPaint.setTextAlign(Paint.Align.CENTER);
        fontMetrics = pointerPaint.getFontMetrics();

        titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);

        pointerPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int mWidth, mHeight;
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mWidth = getPaddingLeft() + radiusDial * 2 + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                mWidth = Math.min(mWidth, widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            mHeight = getPaddingTop() + radiusDial * 2 + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST) {
                mHeight = Math.min(mHeight, heightSize);
            }
        }

        setMeasuredDimension(mWidth, mHeight);

        radiusDial = Math.min((getMeasuredWidth() - getPaddingLeft() - getPaddingRight()),
                (getMeasuredHeight() - getPaddingTop() - getPaddingBottom())) / 2;
        mRealRadius = radiusDial - strokeWidthDial / 2;
        mRect = new RectF(-mRealRadius, -mRealRadius, mRealRadius, mRealRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
        drawPointerLine(canvas);
        drawTitleDial(canvas);
        drawPointer(canvas);
    }

    private void drawArc(Canvas canvas) {
        canvas.translate(getPaddingLeft() + radiusDial, getPaddingTop() + radiusDial);
        float degree = 270f / (default_value_max - default_value_min);
        LogUtils.d("单位数据对应的角度为：" + degree);
        arcPaint.setColor(colorDialLower);
        canvas.drawArc(mRect, 135, (default_value_middle_start - default_value_min) * degree, false, arcPaint);
        LogUtils.d("绘制圆弧：起始角度：" + 135 + "，旋转角度：" + (default_value_middle_start - default_value_min) * degree);
        arcPaint.setColor(colorDialMiddle);
        canvas.drawArc(mRect, 135 + (default_value_middle_start - default_value_min) * degree, degree * (default_value_middle_end - default_value_middle_start), false, arcPaint);
        LogUtils.d("绘制圆弧：起始角度：" + (135 + (default_value_middle_start - default_value_min) * degree) + "，旋转角度：" + degree * (default_value_middle_end - default_value_middle_start));
        arcPaint.setColor(colorDialHigh);
        canvas.drawArc(mRect, 135 + (default_value_middle_end - default_value_min) * degree, degree * (default_value_max - default_value_middle_end), false, arcPaint);
        LogUtils.d("绘制圆弧：起始角度：" + (135 + (default_value_middle_end - default_value_min) * degree) + "，旋转角度：" + degree * (default_value_max - default_value_middle_end));
    }

    /**
     * 绘制分割线
     *
     * @param canvas
     */
    private void drawPointerLine(Canvas canvas) {
        canvas.rotate(135);
        // 把数据进行50等分
        float unitValue = (default_value_max - default_value_min) / 50f;
        for (int i = 0; i < 51; i++) {     //一共需要绘制51个表针
            if (i * unitValue + default_value_min <= default_value_middle_start) {
                pointerPaint.setColor(colorDialLower);
            } else if (i * unitValue + default_value_min <= default_value_middle_end) {
                pointerPaint.setColor(colorDialMiddle);
            } else {
                pointerPaint.setColor(colorDialHigh);
            }
            if (i % 10 == 0) {
                // 长表针
                pointerPaint.setStrokeWidth(4);
                canvas.drawLine(radiusDial, 0, radiusDial - strokeWidthDial - dp2px(6), 0, pointerPaint);
                // 绘制数值
                drawPointerText(canvas, i);
            } else {
                // 短表针
                pointerPaint.setStrokeWidth(2);
                canvas.drawLine(radiusDial, 0, radiusDial - strokeWidthDial - dp2px(4), 0, pointerPaint);
            }
            canvas.rotate(5.4f);
        }
    }

    /**
     * 绘制刻度盘的数字
     *
     * @param canvas
     * @param i
     */
    private void drawPointerText(Canvas canvas, int i) {
        canvas.save();
        int currentCenterX = (int) (radiusDial - strokeWidthDial - dp2px(12) - pointerPaint.measureText(String.valueOf(i)) / 2);
        canvas.translate(currentCenterX, 0);
        canvas.rotate(360 - 135 - 5.4f * i);        //坐标系总旋转角度为360度

        int textBaseLine = (int) (0 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom);
        LogUtils.d("文字基线：fontMetrics.bottom：" + fontMetrics.bottom + "，fontMetrics.top：" + fontMetrics.top + "，textBaseLine：" + textBaseLine);
        float value = default_value_min + (default_value_max - default_value_min) * i / 50f;
        canvas.drawText(StringUtil.removeZero(String.valueOf(value)), 0, textBaseLine, pointerPaint);
        canvas.restore();
    }

    /**
     * 绘制标题和数值
     *
     * @param canvas
     */
    private void drawTitleDial(Canvas canvas) {
        titlePaint.setColor(titleDialColor);
        titlePaint.setTextSize(titleDialSize);
        canvas.rotate(-45f - 5.4f);       //恢复坐标系为起始中心位置
        canvas.drawText(titleDial, 0, -radiusDial / 4, titlePaint);

        if (currentValue <= default_value_middle_start) {
            titlePaint.setColor(colorDialLower);
        } else if (currentValue <= default_value_middle_end) {
            titlePaint.setColor(colorDialMiddle);
        } else {
            titlePaint.setColor(colorDialHigh);
        }
        titlePaint.setTextSize(valueTextSize);
        canvas.drawText(StringUtil.removeZero(String.valueOf(currentValue)) + unit, 0, radiusDial * 3 / 4, titlePaint);
    }

    /**
     * 绘制指针
     *
     * @param canvas
     */
    private void drawPointer(Canvas canvas) {
        if (currentValue <= default_value_max) {
            // 如果实际值不大于量程的最大值，按照实际值计算角度
            float degree = 270f / (default_value_max - default_value_min);
            int currentDegree = (int) ((currentValue - default_value_min) * degree + 135);
            canvas.rotate(currentDegree);
        } else {
            // 如果实际值大于量程的最大值，直接按照最大值的角度绘制指针
            canvas.rotate(45);
        }

        pointerPath.moveTo(radiusDial - strokeWidthDial - dp2px(12), 0);
        pointerPath.lineTo(0, -dp2px(3));
        pointerPath.lineTo(-12, 0);
        pointerPath.lineTo(0, dp2px(3));
        pointerPath.close();
        canvas.drawPath(pointerPath, titlePaint);
    }

    public void setCompleteDegree(float degree, String unit) {
        currentValue = degree;
        this.unit = unit;
        invalidate();
    }

    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }

    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getResources().getDisplayMetrics());
    }

    public void setTitle(String title) {
        titleDial = title;
        invalidate();
    }

    public void setColor(int colorDialLower, int colorDialMiddle, int colorDialHigh) {
        this.colorDialLower = colorDialLower;
        this.colorDialMiddle = colorDialMiddle;
        this.colorDialHigh = colorDialHigh;
        invalidate();
    }

    public void setValue(float default_value_min, float default_value_max, float default_value_middle_start, float default_value_middle_end) {
        this.default_value_min = default_value_min;
        this.default_value_max = default_value_max;
        this.default_value_middle_start = default_value_middle_start;
        this.default_value_middle_end = default_value_middle_end;
        invalidate();
    }

}
