package activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;

import caijh.jinmaigao.com.washcar.R;

/**
 * Created by Administrator on 2016/10/10.
 */
public class UpAppActivity extends Activity {
    private TextView down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upapp);
        down = (TextView) findViewById(R.id.download);
        String url = getIntent().getStringExtra("URL");
        Log.e("cjh", url);
        PackageManager manager = this.getPackageManager();
        String name="washcar"+getVersion()+".apk";
        FinalHttp finalHttp = new FinalHttp();
        finalHttp.download(url, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/"+name, new AjaxCallBack<File>() {
            @Override
            public void onSuccess(File file) {
                installApk();
            }

            @Override
            public void onLoading(long count, long current) {
                Log.e("cjh", current + "     " + count);

                down.setText("正在下载，请稍等");
            }
        });
    }

    private void installApk() {
        File apkfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download", "washcar"+getVersion()+".apk");
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        UpAppActivity.this.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return  version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
