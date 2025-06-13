RootUtils.java
  package com.example.virtualcamera;

import android.content.Context;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;

public class RootUtils {
    private static final String TAG = "RootUtils";
    
    public static boolean isDeviceRooted() {
        // 检查设备是否root
        return checkRootMethod1() || checkRootMethod2();
    }
    
    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }
    
    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", 
                          "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", 
                          "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su" };
        for (String path : paths) {
            if (new java.io.File(path).exists()) return true;
        }
        return false;
    }
    
    public static boolean requestRootAccess() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("id\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            Log.e(TAG, "Root access rejected", e);
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                Log.e(TAG, "Error closing stream", e);
            }
        }
    }
    
    public static String executeCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        StringBuilder output = new StringBuilder();
        
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            process.waitFor();
            
            java.io.InputStream inputStream = process.getInputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                output.append(new String(buffer, 0, length));
            }
            
            inputStream = process.getErrorStream();
            while ((length = inputStream.read(buffer)) != -1) {
                output.append(new String(buffer, 0, length));
            }
            
            return output.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error executing command", e);
            return null;
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                Log.e(TAG, "Error closing stream", e);
            }
        }
    }
}
