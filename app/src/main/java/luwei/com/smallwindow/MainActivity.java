package luwei.com.smallwindow;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WindowManager wm;
    protected SmallWindowView windowView;
    private WindowManager.LayoutParams mLayoutParams;
    private int OVERLAY_PERMISSION_REQ_CODE = 2;
    private boolean isRange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        initSmallViewLayout();
    }

    /**
     * 显示窗口
     * @param view
     */
    public void alertWindow(View view) {
        alertWindow(); // 调用Base中的弹出窗口
    }

    /**
     * 移除窗口
     * @param view
     */
    public void dismissWindow(View view) {
        dismissWindow();
    }

    /**
     * 跳转activity
     * @param view
     */
    public void nextActivity(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    /**
     * 设置内容
     * @param view
     */
    public void test(View view) {
        windowView.setContent("hello");
    }

    /**
     * 改变位置
     * @param view
     */
    public void test1(View view) {
        windowView.testAnimOut();
    }

    /**
     * 动画进入
     * @param view
     */
    public void test2(View view) {
        int h = windowView.getHeight();
        windowView.testAnimIn();
    }

    SmallWindowView sv;

    /**
     * 添加一个View
     * @param view
     */
    public void test3(View view) {
        sv = createFloatView();
        sv.setWm(wm);
        sv.show("strrrrrr");
    }

    /**
     * 隐藏View
     * @param view
     */
    public void test4(View view) {
        sv.hide();
    }

    /**
     * 显示View，自动隐藏
     * @param view
     */
    public void test5(View view) {
        FloatWindowManager.showTitleView(this);
    }

    public void alertWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 7.0 以上需要引导用去设置开启窗口浮动权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0 以上type需要设置成这个
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            requestDrawOverLays();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 6.0 动态申请
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 1);
        }
    }

    private SmallWindowView createFloatView() {
        SmallWindowView v = (SmallWindowView) LayoutInflater.from(this).inflate(R.layout.small_window, null);
        return v;
    }

    public void initSmallViewLayout() {
        windowView = (SmallWindowView) LayoutInflater.from(this).inflate(R.layout.small_window, null);
        mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        //使用非CENTER时，可以通过设置XY的值来改变View的位置
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
        windowView.setWm(wm);
        windowView.setWmParams(mLayoutParams);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (wm != null && windowView.getWm() == null) {
                wm.addView(windowView, mLayoutParams);
            }
        } else {
            Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show();
        }
    }


    private int[] location = new int[2]; // 小窗口位置坐标


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            isRange = calcPointRange(event);
//        }
//        if (isRange) {
//            windowView.dispatchTouchEvent(event);
//        }
        return super.onTouchEvent(event);
    }

    /**
     *  计算当前点击事件坐标是否在小窗口内
     * @param event
     * @return
     */
    private boolean calcPointRange(MotionEvent event) {
        windowView.getLocationOnScreen(location);
        int width = windowView.getMeasuredWidth();
        int height = windowView.getMeasuredHeight();
        float curX = event.getRawX();
        float curY = event.getRawY();
        if (curX >= location[0] && curX <= location[0] + width && curY >= location[1] && curY <= location[1] + height) {
            return true;
        }
        return false;
    }

    private static final String TAG = "BaseActivity";

    // android 23 以上先引导用户开启这个权限 该权限动态申请不了
    @TargetApi(Build.VERSION_CODES.M)
    public void requestDrawOverLays() {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Toast.makeText(this, "can not DrawOverlays", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainActivity.this.getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            if (wm != null && windowView.getWindowId() == null) {
                wm.addView(windowView, mLayoutParams);
            }
            Toast.makeText(this, "权限已经授予", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "设置权限拒绝", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "设置权限成功", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 移除window
    public void dismissWindow() {
        if (wm != null && windowView != null && windowView.getWindowId() != null) {
            wm.removeView(windowView);
        }
    }

}
