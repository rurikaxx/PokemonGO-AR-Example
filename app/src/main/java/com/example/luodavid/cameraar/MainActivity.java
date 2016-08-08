package com.example.luodavid.cameraar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // 陀螺儀移動1度的圖片位移量
    private final float DISPLACEMENT = 22;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceViewCallback surfaceViewCallback;
    private boolean previewing;
    private android.hardware.Camera mCamera;
    private SensorManager sensorManager;
    private int mCurrentCamIndex = 0;
    private Float tmp_x, tmp_y, tmp_z;
    private RelativeLayout group;
    private ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init()
    {
        surfaceViewCallback = new SurfaceViewCallback();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceViewCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        group = (RelativeLayout)findViewById(R.id.group);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        imgView = new ImageView(this);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pikachu);
        imgView.setImageBitmap(bitmap);
        group.addView(imgView);

        imgView.setY(400);
        imgView.setX(400);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;

        if( tmp_x != null && tmp_y != null && tmp_z != null )
        {
            float x_position = 0;
            float y_position = 0;
//            float z_position = 0;

            //角度位移量
            float x_angle= tmp_x - values[0];
            float y_angle = tmp_y - values[1];
//            float z_displacement = tmp_z - values[2];

            if( -180 > x_angle )
            {
                x_angle += 360;
            }
            else if( x_angle > 180 )
            {
                x_angle -= 360;
            }

            x_position = imgView.getX() + x_angle * DISPLACEMENT;
            y_position = imgView.getY() + y_angle * DISPLACEMENT;

            // 旋轉超過360度的x座標位置調整
            if( x_position > 180 * DISPLACEMENT )
            {
                x_position = 180 * -DISPLACEMENT;
            }
            else if ( 180 * -DISPLACEMENT > x_position )
            {
                x_position = 180 * DISPLACEMENT;
            }

            imgView.setX(x_position);
            imgView.setY(y_position);
        }

        tmp_x = values[0];
        tmp_y = values[1];
        tmp_z = values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        SetSensor();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    protected void SetSensor()
    {
        List sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        //如果有取到該手機的方位感測器，就註冊他。

        if (sensors.size()>0)
        {
            // 註冊Listener & 設定更新速度
            sensorManager.registerListener( MainActivity.this, (Sensor) sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }
        else
        {
            Toast.makeText( this, " 您的手機沒有方向感應器! ", Toast.LENGTH_LONG).show();
        }
    }

    private final class SurfaceViewCallback implements android.view.SurfaceHolder.Callback
    {
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
        int arg3) {
            if (previewing) {
                mCamera.stopPreview();
                previewing = false;
            }

            try {
                mCamera.setPreviewDisplay(arg0);
                mCamera.startPreview();
                previewing = true;
                setCameraDisplayOrientation( MainActivity.this,
                        mCurrentCamIndex, mCamera);
            } catch (Exception e) {
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mCamera = openFrontFacingCameraGingerbread();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            previewing = false;
        }
    }


    private android.hardware.Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        android.hardware.Camera cam = null;
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        cameraCount = android.hardware.Camera.getNumberOfCameras();// 相機數

        for (int i = 0; i < cameraCount; i++) {

            android.hardware.Camera.getCameraInfo(i, cameraInfo);// 相機訊息CameraInfo

            if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                cam = android.hardware.Camera.open(i);
                break;
            }
        }
        return cam;
    }

    // 判斷螢幕橫豎自動調整預覽畫面方向
    private static void setCameraDisplayOrientation(Activity activity,
                                                    int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {

            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);

    }
}
