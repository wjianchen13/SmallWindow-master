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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 从顶部弹出来，过一段时间后消失
 */
public class NotificationView extends LinearLayout implements View.OnClickListener{

    /**
     * 动画时长
     */
    private static final int DURATION = 300;

    /**
     * 动画时长
     */
    private static final int DELAY_TIME = 2000;

    /**
     * 状态栏高度
     */
    private int statusHeight;
    private TextView tvTest;

    private WindowManager mWindowManager;
    public WindowManager.LayoutParams wmParams;
    private int mHeight;

    private ValueAnimator animIn;
    private ValueAnimator animOut;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg != null && msg.what == 1) {
                hide();
            }
        }
    };

    public NotificationView(Context context) {
        this(context, null);
    }

    public NotificationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_window_item,this);
        tvTest = findViewById(R.id.tv_test);
        tvTest.setText("nihaoyaayay");
        tvTest.setOnClickListener(this);

        statusHeight = getStatusHeight(context);
        mHeight = getResources().getDimensionPixelSize(R.dimen.float_view_height);
        wmParams = createLayoutParams();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void show(String str) {
        if(mWindowManager != null) {
            tvTest.setText(str);
            mWindowManager.addView(this, wmParams);
            post(new Runnable() {
                @Override
                public void run() {
                    animIn();
                }
            });
        }
    }

    private void hide() {
        animOut();
    }

    private void removeView() {
        if (mWindowManager != null && getWindowId() != null) {
            mWindowManager.removeView(this);
        }
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
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
        Toast.makeText(getContext(), "helele", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
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
        if(getParent() != null)
            mWindowManager.updateViewLayout(this, wmParams);
    }

    public void animIn() {
        final int dy = mHeight + statusHeight; // 动画需要移动的距离
        final int oy = wmParams.y; // 旧的坐标
        animIn = ValueAnimator.ofInt(0, 100);
        animIn.setDuration(DURATION);
        animIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int result = (int) animation.getAnimatedValue();
                int ny = oy + (int)(dy * ((float)result / 100f));
                log("result: " + result + "  ny: " + ny);
                updateViewPosition(ny);
            }
        });
        animIn.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                if(mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessageDelayed(1, DELAY_TIME);
                }
            }
        });
        animIn.start();
    }

    public void animOut() {
        if(animIn != null)
            animIn.cancel();
        final int dy = mHeight + statusHeight; // 动画需要移动的距离
        final int oy = wmParams.y; // 旧的坐标
        animOut = ValueAnimator.ofInt(0, 100);
        animOut.setDuration(DURATION);
        animOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int result = (int) animation.getAnimatedValue();
                int ny = oy - (int)(dy * ((float)result / 100f));
                log("result: " + result + "  ny: " + ny);
                updateViewPosition(ny);
            }
        });
        animOut.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                log("onAnimationEnd: ");
                log("onAnimationEnd1: " + (NotificationView.this.getParent()) + "  isAttachedToWindow(): " + getWindowId());
                removeView();
                log("onAnimationEnd2: " + (NotificationView.this.getParent()) + "  isAttachedToWindow(): " + getWindowId());
            }

        });
        animOut.start();
    }

    class AnimatorListenerImpl implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation, boolean isReverse) {

        }

        @Override
        public void onAnimationEnd(Animator animation, boolean isReverse) {

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
    }



    public static void log(String str) {
        System.out.println("=========================> " + str);
    }
}
