package com.meizu.endurance;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Endurance extends AppCompatActivity {

    private static final int MSG_FREE_SIZE = 0;
    private static final int MSG_LIFE_TIME_A = 1;
    private static final int MSG_LIFE_TIME_B = 2;
    private static final int MSG_TOTAL_WRITE =3;

    private static final String manfid_path = "/sys/block/mmcblk0/device/manfid";
    private static final String cid_path = "/sys/block/mmcblk0/device/cid";
    private static final String name_path = "/sys/block/mmcblk0/device/name";
    private static final String size_path = "/sys/block/mmcblk0/size";

    private static final String TAG = "Endurance";

    private boolean isRunning = true;
    private boolean isWriting = true;

    private long total_size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endurance);

        Button button_start = (Button) this.findViewById(R.id.button_start);
        Button button_stop = (Button) this.findViewById(R.id.button_stop);

        String manfid_h = this.getString(R.string.manfid);
        TextView manfid = (TextView) this.findViewById(R.id.manfid);    // /sys/block/mmcblk0/device/manfid
        try {
            String str_manfid = readFile(manfid_path);
            manfid.setText(manfid_h + " " + str_manfid);
            Log.i(TAG, "read str_manfid success!");
        } catch (Exception e) {
            manfid.setText(manfid_h + " ERR");
            Log.e(TAG, e.toString());
        }
/*
        TextView cid = (TextView)this.findViewById(R.id.text_cid);          // /sys/block/mmcblk0/device/cid
        try {
            String str_cid = readFile(cid_path);

            cid.setText(str_cid);
            Log.i(TAG, "read str_cid success!");
        } catch (Exception e) {
            cid.setText("ERR");
            Log.e(TAG, e.toString());
        }
*/
        String name_h = this.getString(R.string.name);
        TextView name = (TextView) this.findViewById(R.id.vendor_name);        // /sys/block/mmcblk0/device/name
        try {
            String str_name = readFile(name_path);
            name.setText(name_h + " " + str_name);
            Log.i(TAG, "read str_cid success!");
        } catch (Exception e) {
            name.setText(name_h + " ERR");
            Log.e(TAG, e.toString());
        }

        String size_h = this.getString(R.string.capacity);
        TextView size = (TextView) this.findViewById(R.id.emmc_size);        // /sys/block/mmcblk0/size
        try {

            String str_size = readFile(size_path);
            int mmc_size = Integer.valueOf(str_size).intValue() / 2048;
            String str_size_mb = String.valueOf(mmc_size);

            size.setText(size_h + " " + str_size_mb + "MB");
            Log.i(TAG, "read str_cid success!");
        } catch (Exception e) {
            size.setText(size_h + " ERR");
            Log.e(TAG, e.toString());
        }

        //TextView free = (TextView)this.findViewById(R.id.free);
        TextView total_write = (TextView) this.findViewById(R.id.total_write);
        TextView life_time_a = (TextView) this.findViewById(R.id.life_time_a);
        TextView life_time_b = (TextView) this.findViewById(R.id.life_time_b);

        button_start.setOnClickListener(new ButtonStartOnClickListener());
        button_stop.setOnClickListener(new ButtonStopOnClickListener());

        button_start.setClickable(true);
        button_stop.setClickable(false);
        button_stop.setEnabled(false);

    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public String readFile(String filename) throws Exception {

        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = (fis.read(buffer))) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = new String(baos.toByteArray());
        //System.out.println(result);

        baos.close();
        fis.close();

        return replaceBlank(result);
    }

    public byte[] read_EXT_CSD() throws Exception {
        //  /sys/kernel/debug/mmc0/mmc0:0001/ext_csd
        File file = new File("/sys/kernel/debug/mmc0/mmc0:0001/ext_csd");
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = (fis.read(buffer))) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        baos.close();
        fis.close();

        return buffer;
    }

    public void writeFile(String filename, byte[] bytes) throws IOException {
        try {
            FileOutputStream fout = openFileOutput(filename, MODE_PRIVATE|MODE_APPEND);
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long get_userdata_free() {
        File data = Environment.getDataDirectory();
        StatFs data_stat = new StatFs(data.getPath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return data_stat.getAvailableBytes();
        }else
            return 0;
    }

    public void prepare_buffers(byte[] buff) {
        Random random = new Random();
        random.nextBytes(buff);
    }

    private  Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {
            switch(msg.what) {
                case MSG_FREE_SIZE:
                    TextView free = (TextView)findViewById(R.id.free);
                    String free_h = getString(R.string.free_size);
                    long free_size_mb = (long) msg.obj;
                    free.setText(free_h + " " + String.valueOf(free_size_mb) + " MB");
                    break;
                case MSG_LIFE_TIME_A:
                    TextView life_time_a = (TextView) findViewById(R.id.life_time_a);
                    String life_tima_a_h = getString(R.string.emmc_life_time_a);
                    String life_a = (String) msg.obj;
                    life_time_a.setText(life_tima_a_h + " " + life_a);
                    break;
                case MSG_LIFE_TIME_B:
                    TextView life_time_b = (TextView) findViewById(R.id.life_time_b);
                    String life_tima_b_h = getString(R.string.emmc_life_time_b);
                    String life_b = (String) msg.obj;
                    life_time_b.setText(life_tima_b_h + " " + life_b);
                    break;
                case MSG_TOTAL_WRITE:
                    TextView total_write = (TextView) findViewById(R.id.total_write);
                    String total_write_h = getString(R.string.total_write_size_mb);
                    long total_write_mb = (long) msg.obj;
                    total_write.setText(total_write_h + " " + String.valueOf(total_write_mb));
                    break;
            }
        }
    };

    private final class ButtonStartOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button button_start = (Button) findViewById(R.id.button_start);
            Button button_stop = (Button) findViewById(R.id.button_stop);

            button_start.setClickable(false);
            button_start.setEnabled(false);
            button_stop.setClickable(true);
            button_stop.setEnabled(true);

            isRunning = true;
            isWriting = true;
            new Thread(write_thread).start();
            new Thread(update_timer).start();
        }
    }

    private final class ButtonStopOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button button_start = (Button) findViewById(R.id.button_start);
            Button button_stop = (Button) findViewById(R.id.button_stop);

            button_start.setClickable(true);
            button_start.setEnabled(true);
            button_stop.setClickable(false);
            button_stop.setEnabled(false);

            isRunning = false;
            isWriting = false;
        }
    }

    Runnable write_thread = new Runnable() {
        @Override
        public void run() {
            long free_size,count;
            int buff_size = 64 * 1024 * 1024;
            long resoved_size = 1024 * 1024 * 1024;
            byte[] bytes_buff = new byte[buff_size];

            SharedPreferences mySharedPreferences=getSharedPreferences("info", 0);
            SharedPreferences.Editor editor=mySharedPreferences.edit();
            total_size = mySharedPreferences.getLong("total", 0);

            while (isRunning) {
                free_size = get_userdata_free();
                count = (free_size - resoved_size)/buff_size;
                prepare_buffers(bytes_buff);
                for(int i = 0; i < count && isWriting ; i++) {
                    try {
                        writeFile("dummy.bin", bytes_buff);
                        total_size += (long) buff_size;
                        Log.i(TAG, "write dummy.bin success!" + i + " " + count + total_size);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
                editor.putLong("total", total_size);
                editor.commit();

                SystemClock.sleep(100);
                deleteFile("dummy.bin");
                SystemClock.sleep(1000);
            }
        }
    };

    Runnable update_timer = new Runnable() {
        @Override
        public void run() {
            long free_size,data_free_mb;
            byte[] ext_csd = new byte[1024];
            String life_time_a,life_time_b;
            while (isRunning) {
                free_size = get_userdata_free();
                try {
                    ext_csd = read_EXT_CSD();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                data_free_mb = free_size / 1024 / 1024;
                life_time_a = "0X" + String.valueOf(ext_csd[536]-48) + String.valueOf(ext_csd[535]-48);  //byte 535 byte 536 [0x 536 535]
                life_time_b = "0X" + String.valueOf(ext_csd[538]-48) + String.valueOf(ext_csd[537]-48);  //byte 537 byte 538 [0x 538 537]

                mHandler.obtainMessage(MSG_FREE_SIZE,data_free_mb).sendToTarget();
                mHandler.obtainMessage(MSG_TOTAL_WRITE,total_size/(1024 *1024)).sendToTarget();
                mHandler.obtainMessage(MSG_LIFE_TIME_A,life_time_a).sendToTarget();
                mHandler.obtainMessage(MSG_LIFE_TIME_B,life_time_b).sendToTarget();
                SystemClock.sleep(1000);
            }
        }
    };
}
