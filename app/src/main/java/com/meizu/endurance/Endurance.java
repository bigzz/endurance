package com.meizu.endurance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Endurance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endurance);

        Button button_start = (Button)this.findViewById(R.id.button_start);
        Button button_stop = (Button)this.findViewById(R.id.button_stop);

        TextView manfid = (TextView)this.findViewById(R.id.text_manfid);    // /sys/block/mmcblk0/device/manfid
        TextView cid = (TextView)this.findViewById(R.id.text_cid);          // /sys/block/mmcblk0/device/cid
        TextView name = (TextView)this.findViewById(R.id.text_name);        // /sys/block/mmcblk0/device/name
        TextView size = (TextView)this.findViewById(R.id.text_size);        // /sys/block/mmcblk0/size

        TextView free = (TextView)this.findViewById(R.id.text_free);
        TextView total_write = (TextView)this.findViewById(R.id.text_tatal_write);
        TextView life_time_a = (TextView)this.findViewById(R.id.text_life_time_a);
        TextView life_time_b = (TextView)this.findViewById(R.id.text_life_time_b);

        button_start.setOnClickListener(new ButtonStartOnClickListener());
        button_stop.setOnClickListener(new ButtonStopOnClickListener());

    }

    private final class ButtonStartOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
        }
    }

    private final class ButtonStopOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {

        }
    }
}
