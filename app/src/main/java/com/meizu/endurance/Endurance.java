package com.meizu.endurance;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

    private static final int MSG_SUCCESS = 0;
    private static final int MSG_FAILURE = 1;

    private static final String manfid_path = "/sys/block/mmcblk0/device/manfid";
    private static final String cid_path = "/sys/block/mmcblk0/device/cid";
    private static final String name_path = "/sys/block/mmcblk0/device/name";
    private static final String size_path = "/sys/block/mmcblk0/size";

    private static final String TAG = "Endurance";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    public void writeFile(String fileName, byte[] bytes) throws IOException {
        try {
            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE|MODE_APPEND);
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete_file(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }
    public long get_userdata_free() {
        File data = Environment.getDataDirectory();
        StatFs data_stat = new StatFs(data.getPath());

        return data_stat.getAvailableBytes();
    }

    public void prepare_buffers(byte[] buff) {
        Random random = new Random();
        random.nextBytes(buff);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Endurance Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.meizu.endurance/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {
            switch(msg.what) {
                case MSG_SUCCESS:
                    TextView free = (TextView)findViewById(R.id.free);
                    free.setText((String) msg.obj);
                    break;
                case MSG_FAILURE:
                    break;
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Endurance Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.meizu.endurance/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private final class ButtonStartOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new Thread(write_thread).start();
            new Thread(update_timer).start();
        }
    }

    private final class ButtonStopOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    Runnable write_thread = new Runnable() {
        @Override
        public void run() {
            long free_size = get_userdata_free();
            byte[] bytes_buff = new byte[16 * 1024 * 1024];
            long count = (free_size - 100 * 1024 * 1024)/ (16 * 1024 * 1024);

            while (true) {
                for(int i = 0; i < count ; i++) {
                    prepare_buffers(bytes_buff);
                    try {
                        writeFile("dummy.bin", bytes_buff);

                        Log.i(TAG, "write dummy.bin success!");
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
                SystemClock.sleep(1000);
                delete_file("dummy.bin");
            }
        }
    };

    Runnable update_timer = new Runnable() {
        @Override
        public void run() {
            String free_h = getString(R.string.free_size);
            String free_text;
            long free_size,data_free_mb;

            while (true) {
                free_size = get_userdata_free();
                data_free_mb = free_size / 1024 / 1024;

                free_text = free_h + " " + String.valueOf(data_free_mb) + " MB";
                mHandler.obtainMessage(MSG_SUCCESS,free_text).sendToTarget();

                SystemClock.sleep(1000);
            }
        }
    };
}
