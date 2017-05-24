package com.cretin.www.relativelayoutdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cretin.www.relativelayoutdemo.R;


/**
 * it is a view where view or viewgroup can be scaled,rotated,moved on it
 * Created by cretin on 16/11/16.
 */

public class MoveScaleRotateView extends RelativeLayout {
    private Context mContext;

    //默认的触摸点ID
    private static final int INVALID_POINTER_ID = -1;
    //子View上的两个手指的触摸点ID
    private int mChildPtrID1 = INVALID_POINTER_ID, mChildPtrID2
            = INVALID_POINTER_ID;
    //父View上的两个手指的触摸点ID
    private int mPtrID1 = INVALID_POINTER_ID, mPtrID2 = INVALID_POINTER_ID;

    //父布局的Event事件
    private MotionEvent mEvent;

    //记录点击在子View上的x和y坐标
    private float mChildActionDownX = 0;
    private float mChildActionDownY = 0;

    //记录点击在父View上的第一个点和第二个点的x和y坐标
    private float mActionDownX1 = 0;
    private float mActionDownX2 = 0;
    private float mActionDownY1 = 0;
    private float mActionDownY2 = 0;

    //初始的旋转角度
    private float mDefaultAngle;
    //当前旋转角度
    private float mAngle;

    //记录原始落点的时候两个手指之间的距离
    private float oldDist = 0;

    //测试View
    private View view;

    //初始化操作
    private void init(Context context) {
        mContext = context;
        view = View.inflate(context, R.layout.layout_test_rela_view, null);
        addView(view);
        view.setX(100);
        view.setY(100);

        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
                    case MotionEvent.ACTION_DOWN:
                        mChildPtrID1 = event.getPointerId(event.getActionIndex());
                        if ( mEvent != null ) {
                            mChildActionDownX = mEvent.getX(event.findPointerIndex(mChildPtrID1))
                                    - view.getX();
                            mChildActionDownY = mEvent.getY(event.findPointerIndex(mChildPtrID1))
                                    - view.getY();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //非第一个触摸点按下
                        mChildPtrID2 = event.getPointerId(event.getActionIndex());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if ( mEvent != null ) {
                            float x1 = mEvent.getX(mEvent.findPointerIndex(mChildPtrID1));
                            float y1 = mEvent.getY(mEvent.findPointerIndex(mChildPtrID1));
                            view.setX(x1 - mChildActionDownX);
                            view.setY(y1 - mChildActionDownY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mChildPtrID1 = INVALID_POINTER_ID;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        //非第一个触摸点抬起
                        mChildPtrID2 = INVALID_POINTER_ID;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mChildPtrID1 = INVALID_POINTER_ID;
                        mChildPtrID2 = INVALID_POINTER_ID;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mEvent = event;
        switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
            case MotionEvent.ACTION_DOWN:
                mPtrID1 = event.getPointerId(event.getActionIndex());
                mActionDownX1 = event.getX(event.findPointerIndex(mPtrID1));
                mActionDownY1 = event.getY(event.findPointerIndex(mPtrID1));
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //非第一个触摸点按下
                mPtrID2 = event.getPointerId(event.getActionIndex());
                mActionDownX2 = event.getX(event.findPointerIndex(mPtrID2));
                mActionDownY2 = event.getY(event.findPointerIndex(mPtrID2));

                oldDist = spacing(event, mPtrID1, mPtrID2);
                break;
            case MotionEvent.ACTION_MOVE:
                if ( mPtrID1 != INVALID_POINTER_ID && mPtrID2 != INVALID_POINTER_ID ) {
                    float x1 = 0, x2 = 0, y1 = 0, y2 = 0;
                    x1 = event.getX(event.findPointerIndex(mPtrID1));
                    y1 = event.getY(event.findPointerIndex(mPtrID1));
                    x2 = event.getX(event.findPointerIndex(mPtrID2));
                    y2 = event.getY(event.findPointerIndex(mPtrID2));

                    //在这里处理旋转逻辑
                    mAngle = angleBetweenLines(mActionDownX1, mActionDownY1, mActionDownX2,
                            mActionDownY2, x1, y1, x2, y2) + mDefaultAngle;
                    view.setRotation(mAngle);

                    //在这里处理缩放的逻辑
                    //处理缩放模块
                    float newDist = spacing(event, mPtrID1, mPtrID2);
                    float scale = newDist / oldDist;
                    if ( newDist > oldDist + 1 ) {
                        zoom(scale, view);
                        oldDist = newDist;
                    }
                    if ( newDist < oldDist - 1 ) {
                        zoom(scale, view);
                        oldDist = newDist;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mPtrID1 = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //非第一个触摸点抬起
                mPtrID2 = INVALID_POINTER_ID;
                mDefaultAngle = mAngle;
                break;
            case MotionEvent.ACTION_CANCEL:
                mPtrID1 = INVALID_POINTER_ID;
                mPtrID2 = INVALID_POINTER_ID;
                break;
        }
        return true;
    }

    //对控件进行缩放操作
    private void zoom(float scale, View view) {
        int w = view.getWidth();
        int h = view.getHeight();
        view.setLayoutParams(new RelativeLayout.LayoutParams(( int ) (w * scale), ( int ) (h * scale)));
    }

    /**
     * 计算两点之间的距离
     *
     * @param event
     * @return 两点之间的距离
     */
    private float spacing(MotionEvent event, int ID1, int ID2) {
        float x = event.getX(ID1) - event.getX(ID2);
        float y = event.getY(ID1) - event.getY(ID2);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 计算刚开始触摸的两个点构成的直线和滑动过程中两个点构成直线的角度
     *
     * @param fX  初始点一号x坐标
     * @param fY  初始点一号y坐标
     * @param sX  初始点二号x坐标
     * @param sY  初始点二号y坐标
     * @param nfX 终点一号x坐标
     * @param nfY 终点一号y坐标
     * @param nsX 终点二号x坐标
     * @param nsY 终点二号y坐标
     * @return 构成的角度值
     */
    private float angleBetweenLines(float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY) {
        float angle1 = ( float ) Math.atan2((fY - sY), (fX - sX));
        float angle2 = ( float ) Math.atan2((nfY - nsY), (nfX - nsX));

        float angle = (( float ) Math.toDegrees(angle1 - angle2)) % 360;
        if ( angle < -180.f ) angle += 360.0f;
        if ( angle > 180.f ) angle -= 360.0f;
        return -angle;
    }


    public MoveScaleRotateView(Context context) {
        super(context);
        init(context);
    }

    public MoveScaleRotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MoveScaleRotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public MoveScaleRotateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * 测试用 显示Toast
     *
     * @param msg
     */
    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 测试用 打印log
     *
     * @param log
     */
    private void log(String log) {
        Log.e("HHHHHHHHHH", log);
    }

    /**
     * 测试用 打印log 指定TAG
     *
     * @param log
     * @param tag
     */
    private void log(String log, String tag) {
        Log.e(tag, log);
    }


}
