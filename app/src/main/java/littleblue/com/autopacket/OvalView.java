package littleblue.com.autopacket;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by xieqingyu on 2016/12/7.
 */

public class OvalView extends View {
    private String TAG = "OvalView";

    private int mViewHeight;
    private int mViewWidth;
    private int mColor;
    private float mAngleRatio = 1;

    public OvalView(Context context) {
        this(context, null);
        Log.i(TAG, "OvalView(context)");
    }

    public OvalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        Log.i(TAG, "OvalView(context, attrs)");
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OvalView, 0, 0);
        try {
            mColor = a.getColor(R.styleable.OvalView_oval_color, getResources().getColor(R.color.glass_grey));
            mAngleRatio = a.getFloat(R.styleable.OvalView_angle_ratio, 1.0f);
        } finally {
            a.recycle();
        }
    }

    public OvalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(TAG, "OvalView(context, attrs, defStyleAttr)");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure widthMeasureSpec: " + widthMeasureSpec + " heightMeasureSpec: " + heightMeasureSpec);
        Log.i(TAG, "onMeasure getMeasuredWidth: " + getMeasuredWidth() + " getMeasuredHeight: " + getMeasuredHeight());
        if (getMeasuredWidth() > 0 || getMeasuredHeight() > 0) {
            mViewWidth = getMeasuredWidth();
            mViewHeight = getMeasuredHeight();
            setMeasuredDimension(mViewWidth, mViewHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Log.i(TAG, "onDraw");
        RectF rectF = new RectF();//矩形
        rectF.top = 0;
        rectF.left = 0;
        rectF.right = mViewWidth;
        rectF.bottom = mViewHeight;

        Paint paint = new Paint();
        paint.setColor(mColor);
        paint.setAntiAlias(true);//抗锯齿
//        canvas.drawOval(rectF, paint);
//        canvas.drawRoundRect(rectF, mViewWidth, mViewWidth*6, paint);//调整rx和ry可以得到带不同的弧度圆角的矩形
        canvas.drawRoundRect(0, 0, mViewWidth, mViewHeight, mViewHeight, mViewHeight*mAngleRatio, paint);//调整rx和ry可以得到带不同的弧度圆角的矩形
    }

    public void setRoundAngle(float angleRatio) {
        mAngleRatio = angleRatio;
    }

    public void setWidthAndHeight(int width, int height) {
        mViewHeight = height;
        mViewWidth = width;
    }

    public void setColor(int color) {
        mColor = color;
    }
}
