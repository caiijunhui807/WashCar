package utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Created by pengl on 2016/7/4.
 */
public class MyUtils {

    /**
     * 校验和
     * @param b
     * @return
     */
    public static byte chekSum(byte [] b)
    {
        byte sum=0;
        for (int i = 1; i < b.length-2; i++) {
              sum+=b[i];
        }
        return (byte)(sum%256) ;
    }
    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 获取设备的mac地址
     * @return
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";
        String result="";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);


            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        String[] s=macSerial.split(":");
        for(int i=0;i<s.length;i++)
        {
            result+=s[i];
        }
        return result;
    }






}
