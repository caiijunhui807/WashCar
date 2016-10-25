package activity;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.washcar.client.RemoteMessageArrivedListener;
import com.zhy.base.imageloader.ImageLoader;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import caijh.jinmaigao.com.washcar.R;
import protocol.MSGRunnable;
import usb.UsbDeviceService;
import utils.CommonUtils;

public class MainActivity extends Activity implements RemoteMessageArrivedListener {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager;

    private ExecutorService mExecutors = Executors.newCachedThreadPool();
    private ConcurrentLinkedQueue<Byte> mMessages = new ConcurrentLinkedQueue<Byte>();
    private Button but;
    private Button yeast;
    private Button dust;
    private Button water;

    private LinearLayout des;
    private RelativeLayout begin;

    private LinearLayout error;
    private String ewm;
    private UsbDeviceService mUsbBinder;
    private WashCarServiceTest wash;

    private ImageView ImageEWM;
    private ImageView errorImage;
    private TextView des_text;

    private TextView work, time, error_text1, error_text2, error_text3, error_text4, error_text5, error_text6, error_text7, error_text8, error_text9,
            error11, error21, error31, error4, error5, error6;


    /**
     * 用户是否扫描二维码并付款
     */
    private boolean isPay = false;

    /**
     * 是否暂停计时
     */
    private boolean isPause = true;
    /**
     * 使用时间是否已到
     */
    private boolean isUsed = true;

    /**
     * 是否开始计时
     */
    private boolean timeBegin = true;

    /**
     * 倒计时
     */
    private int totaltime = 29;
    private int totalSec = 59;
    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isPause) {
                        totalSec--;
                        timeBegin = false;
                        if (totalSec < 10) {
                            time.setText("总剩余时间:   " + totaltime + ":0" + totalSec);
                        } else {

                            time.setText("总剩余时间:   " + totaltime + ":" + totalSec);
                        }

                        if (totalSec == 0) {
                            totalSec = 59;
                            totaltime--;
                            if (totaltime == -1) {
                                timer.cancel();
                                isUsed = false;
                                timeBegin = true;
                                time.setText("使用时间已到 ! ");
                                totaltime = 29;
                            }
                        }
                    }

                }
            });
        }
    };

    /**
     * 设备是否可以正常运行 fasle故障 ；true正常
     */
    private boolean isOperation = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            goneErrorText();
            Bundle bundle = msg.getData();
            byte key = bundle.getByte("key");
            byte error1 = bundle.getByte("error1");
            byte error2 = bundle.getByte("error2");
            byte error3 = bundle.getByte("error3");
            if (key != (byte) 0x00) {
                isOperation = true;
            }
            if (error1 == (byte) 0x00 && error2 == (byte) 0x00&& key != (byte)0x00) {

//                isOperation = true;
                // 按键操作->支付成功  isUsed=true ；  isOperation  机器是否可以正常运行
                if (isUsed && isOperation) {
                    //隐藏所有的设备错误提示
                    goneErrorText();
                    //开始计时，防止重复开始 程序崩溃
                    if (timeBegin) {
                        timer.schedule(task, 0, 1000);
                    }
                    //隐藏 提示扫描二维码
                    des_text.setText("请扫描二维码");
                    begin.setVisibility(View.GONE);
                    //二维码隐藏
                    ImageEWM.setVisibility(View.GONE);
                    errorImage.setVisibility(View.GONE);
                    sendDataToUsb(new byte[]{(byte) 0xe0, (byte) 0x08, 0x00, 0x00, 0x00, (byte) 0X01, 0x00, 0x00, 0x00, (byte) ((0x08 + 0X01 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00) % 256), (byte) 0x0e});
                    String hexString = CommonUtils.decodeByteToHexString(key);
                    String binaryString = CommonUtils.hexString2binaryString(hexString);
                    char[] stringBIn = binaryString.toCharArray();
                    for (int i = 0; i < stringBIn.length; i++) {
                        if (stringBIn[i] == '1') {
                            switch (7-i) {

                                case 1:
                                    work.setText("泡沫");
                                    yeast.setBackgroundResource(R.color.white);
                                    dust.setBackgroundResource(R.color.weather_text_color);
                                    water.setBackgroundResource(R.color.weather_text_color);
                                    break;
                                case 0:
                                    work.setText("吸尘");
                                    yeast.setBackgroundResource(R.color.weather_text_color);
                                    dust.setBackgroundResource(R.color.white);
                                    water.setBackgroundResource(R.color.weather_text_color);
                                    break;
                                case 2:
                                    work.setText("取水");
                                    yeast.setBackgroundResource(R.color.weather_text_color);
                                    dust.setBackgroundResource(R.color.weather_text_color);
                                    water.setBackgroundResource(R.color.white);
                                    break;
                            }
                        }

                    }

                } else if (!isUsed) {
                    begin.setVisibility(View.VISIBLE);
                    hideButton();
                    work.setText("使用时间已到");
                    sendDataToUsb(new byte[]{(byte) 0xe0, (byte) 0x08, 0x00, 0x00, 0x00, (byte) 0X02, 0x00, 0x00, 0x00, (byte) ((0xe8 + 0X02 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00) % 256), (byte) 0x0e});
                }
            }
            //使用过的设备没有放置正确的位置
            if (error3 != (byte) 0x00 && isUsed==false) {
                String hexString = CommonUtils.decodeByteToHexString(error3);
                String binaryString = CommonUtils.hexString2binaryString(hexString);
                char[] stringBIn = binaryString.toCharArray();
                hideButton();
                sendDataToUsb(new byte[]{(byte) 0xe0, (byte) 0x08, 0x00, 0x00, 0x00, (byte) 0X02, 0x00, 0x00, 0x00, (byte) ((0xe8 + 0X02 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00) % 256), (byte) 0x0e});
                for (int i = 0; i < stringBIn.length; i++) {
                    if (stringBIn[i] == '1') {
                        switch (7-i) {
                            case 0:
                                error11.setText("泡沫枪头未归位");
                                error11.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                error21.setText("水枪头位未归位");
                                error21.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                error31.setText("吸尘器头位未归位");
                                error31.setVisibility(View.VISIBLE);
                                break;
                            case 3:
                                error4.setText("扫把未归位");
                                error4.setVisibility(View.VISIBLE);
                                break;
                            case 4:
                                error5.setText("扫把舱门未关闭");
                                error5.setVisibility(View.VISIBLE);
                                break;
                            case 5:
                                error6.setText("吸尘器舱门未关闭");
                                error6.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                }
            }
           // 机器设备出现故障，不能正常运行，维护中.
            if (error1 != (byte) 0x00) {
                errorImage.setVisibility(View.VISIBLE);
                ImageEWM.setVisibility(View.GONE);
                des_text.setText("机器故障");
                begin.setVisibility(View.VISIBLE);
                isOperation = false;
                hideButton();
                sendDataToUsb(new byte[]{(byte) 0xe0, (byte) 0x08, 0x00, 0x00, 0x00, (byte) 0X03, 0x00, 0x00, 0x00, (byte) ((0x08 + 0X03 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00) % 256), (byte) 0x0e});

                String hexString = CommonUtils.decodeByteToHexString(error1);
                String binaryString = CommonUtils.hexString2binaryString(hexString);
                char[] stringBIn = binaryString.toCharArray();

                for (int i = 0; i < stringBIn.length; i++) {
                    if (stringBIn[i] == '1') {
                        switch (7-i) {
                            case 0:
                                error_text1.setText("NTC故障");
                                error_text1.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                error_text2.setText("温度过高");
                                error_text2.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                error_text3.setText("温度过低");
                                error_text3.setVisibility(View.VISIBLE);
                                break;
                            case 3:
                                error_text4.setText("泡沫不足");
                                error_text4.setVisibility(View.VISIBLE);
                                break;
                            case 4:
                                error_text5.setText("水量不足");
                                error_text5.setVisibility(View.VISIBLE);
                                break;
                            case 5:
                                error_text6.setText("缺水或流量计坏");
                                error_text6.setVisibility(View.VISIBLE);
                                break;
                            case 6:
                                error_text7.setText("水阀故障");
                                error_text7.setVisibility(View.VISIBLE);
                                break;
                            case 7:
                                error_text8.setText("吸尘器压力不足");
                                error_text8.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                }
            }
            if (error2 != (byte) 0x00) {
                des_text.setText("机器故障");
                begin.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                ImageEWM.setVisibility(View.GONE);
                isOperation = false;
                hideButton();
                sendDataToUsb(new byte[]{(byte) 0xe0, (byte) 0x08, 0x00, 0x00, 0x00, (byte) 0X03, 0x00, 0x00, 0x00, (byte) ((0x08 + 0X03 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00) % 256), (byte) 0x0e});
                switch (error2) {
                    case (byte) 0x01:
                        error_text9.setText("水泵压力不足");
                        error_text9.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    };


    /**
     * 判断收到的信息是否为一整条
     */
    private boolean isTotal = false;

    private BroadcastReceiver mUsbDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            TextView text = new TextView(MainActivity.this);
            text.setText(CommonUtils.decodeBytesToHexString(data));
            des.addView(text, 0);
            //使得信息完整
            for (byte item : data) {
                if (item == (byte) 0xee) {
                    isTotal = true;
                }

                if (isTotal) {
                    mMessages.add(item);
                    if (item == (byte) 0xaa) {
                        isTotal = false;
                    }
                }
                if (item == (byte) 0xaa) {
                    mExecutors.execute(new MSGRunnable(mMessages, handler));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        initView();
        initData();
    }

    private void initView() {
        yeast = (Button) findViewById(R.id.yeast);
        dust = (Button) findViewById(R.id.dust);
        water = (Button) findViewById(R.id.water);
        des = (LinearLayout) findViewById(R.id.des);
        work = (TextView) findViewById(R.id.work);
        time = (TextView) findViewById(R.id.time);
        ImageEWM = (ImageView) findViewById(R.id.ewmImage);
        begin = (RelativeLayout) findViewById(R.id.begin);
        des_text = (TextView) findViewById(R.id.des_text);
        errorImage = (ImageView) findViewById(R.id.errorImage);
        errorImage.setBackgroundResource(R.mipmap.maintenance);
        ImageEWM.setVisibility(View.VISIBLE);


        hideButton();


        error = (LinearLayout) findViewById(R.id.error);
        error_text1 = (TextView) findViewById(R.id.error_text1);
        error_text2 = (TextView) findViewById(R.id.error_text2);
        error_text3 = (TextView) findViewById(R.id.error_text3);
        error_text4 = (TextView) findViewById(R.id.error_text4);
        error_text5 = (TextView) findViewById(R.id.error_text5);
        error_text6 = (TextView) findViewById(R.id.error_text6);
        error_text7 = (TextView) findViewById(R.id.error_text7);
        error_text8 = (TextView) findViewById(R.id.error_text8);
        error_text9 = (TextView) findViewById(R.id.error_text9);
        error11 = (TextView) findViewById(R.id.error1);
        error21 = (TextView) findViewById(R.id.error2);
        error31 = (TextView) findViewById(R.id.error3);
        error4 = (TextView) findViewById(R.id.error4);
        error5 = (TextView) findViewById(R.id.error5);
        error6 = (TextView) findViewById(R.id.error6);

    }

    private void initData() {
        Intent usbIntent = new Intent(this, UsbDeviceService.class);
        getApplicationContext().bindService(usbIntent, UsbConnection, Service.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbDeviceService.ACTION_RECEIVE_USB_DATA);
        registerReceiver(mUsbDataReceiver, filter);
        getEWM();

    }

    /**
     * 获取二维码
     */
    private void getEWM() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wash = new WashCarServiceTest();
                    wash.init(MainActivity.this);
                    ewm = wash.getEWMInfo();
                    if (ewm != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, ewm, Toast.LENGTH_LONG).show();
                                ImageLoader.with(MainActivity.this).load(ewm, ImageEWM);
                                ImageEWM.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUsbDataReceiver);

        if (mUsbBinder != null)
            getApplicationContext().unbindService(UsbConnection);
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    /**
     * 向USB发送命令数据
     *
     * @param data 字节内容
     */
    private void sendDataToUsb(byte[] data) {
        Intent intent = new Intent(UsbDeviceService.ACTION_SEND_USB_DATA);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }


    /**
     * bind USB服务时的连接接口
     */
    private ServiceConnection UsbConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUsbBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUsbBinder = ((UsbDeviceService.LocalBinder) service).getService();
        }
    };


    /**
     * 服务器发出消息，在此处接收
     *
     * @param
     * @return
     */
    @Override
    public String receiveMsg(String s) {
        //TODO 接收服务器的消息
        Toast.makeText(MainActivity.this, "监听接收的消息------" + s + "-----------", Toast.LENGTH_LONG).show();
        TextView text = new TextView(MainActivity.this);
        text.setText(s);
        des.addView(text, 0);

        return null;
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.freshen:
                goneErrorText();
                break;
        }
    }

    /**
     * 恢复默认按键颜色
     */
    private void hideButton() {
        yeast.setBackgroundResource(R.color.weather_text_color);
        dust.setBackgroundResource(R.color.weather_text_color);
        water.setBackgroundResource(R.color.weather_text_color);
    }

    /**
     * 隐藏所有错误信息
     */
    private void goneErrorText() {
        error_text1.setVisibility(View.GONE);
        error_text2.setVisibility(View.GONE);
        error_text3.setVisibility(View.GONE);
        error_text4.setVisibility(View.GONE);
        error_text5.setVisibility(View.GONE);
        error_text6.setVisibility(View.GONE);
        error_text7.setVisibility(View.GONE);
        error_text8.setVisibility(View.GONE);
        error_text9.setVisibility(View.GONE);
        error11.setVisibility(View.GONE);
        error21.setVisibility(View.GONE);
        error31.setVisibility(View.GONE);
        error4.setVisibility(View.GONE);
        error5.setVisibility(View.GONE);
        error6.setVisibility(View.GONE);
    }
}
