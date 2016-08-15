package com.cretin.www.relativelayoutdemo;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cretin.www.relativelayoutdemo.app.AppConstants;
import com.cretin.www.relativelayoutdemo.utils.ImageUtils;
import com.cretin.www.relativelayoutdemo.view.MyRelativeLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements MyRelativeLayout.MyRelativeTouchCallBack {
    private MyRelativeLayout rela;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initUI();
    }

    public void initUI() {
        rela = (MyRelativeLayout) findViewById(R.id.id_rela);
        rela.setMyRelativeTouchCallBack(this);
    }

    /**
     * 当时重写这个方法是因为项目中有左右滑动切换不同滤镜的效果
     *
     * @param direction
     */
    @Override
    public void touchMoveCallBack(int direction) {
        if (direction == AppConstants.MOVE_LEFT) {
            Toast.makeText(MainActivity.this, "你在向左滑动！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "你在向右滑动！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 这个方法可以用来实现滑到某一个地方删除该TextView的实现
     *
     * @param textView
     */
    @Override
    public void onTextViewMoving(TextView textView) {
        Log.d(TAG, "TextView正在滑动");
    }

    @Override
    public void onTextViewMovingDone() {
        Toast.makeText(MainActivity.this, "标签TextView滑动完毕！", Toast.LENGTH_SHORT).show();
    }

    public void btnClickExplain(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意:");
        builder.setMessage("一、点击自定义View可以创建标签，点击标签滑动可以改变标签位置；\n" +
                "二、在View上进行两指缩放旋转可以改变标签的方向和大小，当有多个标签TextView存在时，两指缩放和旋转会自动寻找离两指中心点最近的TextView标签进行操作！\n" +
                "三、在自定义上可识别左滑和右滑操作；\n" +
                "四、可监控TextView的实时滑动!\n" +
                "五、对话框中可滑动滑条改变字体颜色!\n" +
                "六、点击保存能将画布生成图片保存下来!\n" +
                "七、有需要的伙伴可以自定义添加新功能！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    public void btnClickLoad(View view) {
        Bitmap bitmap = ImageUtils.createViewBitmap(rela, rela.getWidth(), rela.getHeight());
        String fileName = "CRETIN_" + new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date()) + ".png";
        String result = ImageUtils.saveBitmapToFile(bitmap, fileName);
        Toast.makeText(MainActivity.this, "保存位置:" + result, Toast.LENGTH_SHORT).show();
    }
}
