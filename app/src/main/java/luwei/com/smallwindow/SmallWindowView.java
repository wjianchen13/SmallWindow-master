package luwei.com.smallwindow;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Author   : luweicheng on 2018/8/24 11:22
 * E-mail   ：1769005961@qq.com
 * GitHub   : https://github.com/luweicheng24
 * function:
 **/
public class SmallWindowView extends LinearLayout implements View.OnClickListener{
    private final int screenHeight;
    private final int screenWidth;
    private int statusHeight;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;

    private TextView tvTest;

    private WindowManager wm;
    public WindowManager.LayoutParams wmParams;
    private int mHeight;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };


    public SmallWindowView(Context context) {
        this(context, null);
    }

    public WindowManager getWm() {
        return wm;
    }

    public void setWm(WindowManager wm) {
        this.wm = wm;
    }

    public WindowManager.LayoutParams getWmParams() {
        return wmParams;
    }

    public void setWmParams(WindowManager.LayoutParams wmParams) {
        this.wmParams = wmParams;
//        this.wmParams.x = screenWidth;
    }

    public SmallWindowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmallWindowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        log("SmallWindowView");
        inflate(getContext(), R.layout.view_window_item,this);
        tvTest = findViewById(R.id.tv_test);
        tvTest.setOnClickListener(this);
        tvTest.setText("nihaoyaayay");
        statusHeight = getStatusHeight(context);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        mHeight = getResources().getDimensionPixelSize(R.dimen.float_view_height);
        wmParams = createLayoutParams();
    }

    public void show(String str) {
        if(wm != null) {
            tvTest.setText(str);
            wm.addView(this, wmParams);
            post(new Runnable() {
                @Override
                public void run() {
                    int h = getHeight();
                    animIn();
                }
            });
        }
    }

    public void hide() {
        animOut();
    }

    private void removeView() {
        if (wm != null && getWindowId() != null) {
            wm.removeView(this);
        }
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 可以移动到窗口之外
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.x = 0;
        mLayoutParams.y = -(statusHeight + mHeight);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0 以上type需要设置成这个
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return mLayoutParams;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
    }

    public void setContent(String str) {
        if(tvTest != null)
            tvTest.setText(str);
    }


    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public void updateViewPosition(int newY) {
        wmParams.y = newY;
//        isAttachedToWindow()
        wm.updateViewLayout(this, wmParams);
    }

    public void testAnimOut() {
        final int dy = mHeight + statusHeight; // 动画需要移动的距离
        final int oy = wmParams.y; // 旧的坐标
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int result = (int) animation.getAnimatedValue();
                int ny = oy - (int)(dy * ((float)result / 100f));
                log("result: " + result + "  ny: " + ny);
                updateViewPosition(ny);
            }
        });
        valueAnimator.start();
    }

    public void testAnimIn() {
        final int dy = mHeight + statusHeight; // 动画需要移动的距离
        final int oy = wmParams.y; // 旧的坐标
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int result = (int) animation.getAnimatedValue();
                int ny = oy + (int)(dy * ((float)result / 100f));
                log("result: " + result + "  ny: " + ny);
                updateViewPosition(ny);
            }
        });

        valueAnimator.start();
    }

    public void animOut() {
        final int dy = mHeight + statusHeight; // 动画需要移动的距离
        final int oy = wmParams.y; // 旧的坐标
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int result = (int) animation.getAnimatedValue();
                int ny = oy - (int)(dy * ((float)result / 100f));
                log("result: " + result + "  ny: " + ny);
                updateViewPosition(ny);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {

            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                log("onAnimationEnd: ");
                log("onAnimationEnd1: " + (SmallWindowView.this.getParent()));
                removeView();
                log("onAnimationEnd1: " + (SmallWindowView.this.getParent()));
            }

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.start();
    }

    public void animIn() {
        final int dy = mHeight + statusHeight; // 动画需要移动的距离
        final int oy = wmParams.y; // 旧的坐标
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int result = (int) animation.getAnimatedValue();
                int ny = oy + (int)(dy * ((float)result / 100f));
                log("result: " + result + "  ny: " + ny);
                updateViewPosition(ny);
            }
        });

        valueAnimator.start();
    }



    public static void log(String str) {
        System.out.println("=========================> " + str);
    }
}
