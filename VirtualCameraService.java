2. VirtualCameraService.java
  package com.example.virtualcamera;

import android.app.Service;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class VirtualCameraService extends Service {
    private static final String TAG = "VirtualCameraService";
    private CameraManager mCameraManager;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private CameraHook mCameraHook;

    public static void startService(android.content.Context context) {
        context.startService(new Intent(context, VirtualCameraService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "VirtualCameraService started");
        
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        startBackgroundThread();
        
        // 替换系统相机服务
        replaceSystemCamera();
        
        // 初始化相机Hook
        mCameraHook = new CameraHook(this);
        try {
            String[] cameraIds = mCameraManager.getCameraIdList();
            if (cameraIds.length > 0) {
                mCameraHook.hookCamera(cameraIds[0]); // Hook第一个相机
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to access camera", e);
        }
    }

    private void replaceSystemCamera() {
        // 使用root权限替换系统相机服务
        String cmd = "mount -o remount,rw /system && " +
                     "mv /system/bin/android.hardware.camera2.service /system/bin/android.hardware.camera2.service.bak && " +
                     "cp " + getApplicationInfo().nativeLibraryDir + "/libvirtualcamera.so /system/bin/android.hardware.camera2.service && " +
                     "chmod 755 /system/bin/android.hardware.camera2.service && " +
                     "mount -o remount,ro /system";
        
        RootUtils.executeCommand(cmd);
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Background thread interrupted", e);
            }
        }
        restoreSystemCamera();
    }

    private void restoreSystemCamera() {
        String cmd = "mount -o remount,rw /system && " +
                     "mv /system/bin/android.hardware.camera2.service.bak /system/bin/android.hardware.camera2.service && " +
                     "chmod 755 /system/bin/android.hardware.camera2.service && " +
                     "mount -o remount,ro /system";
        
        RootUtils.executeCommand(cmd);
    }
}
