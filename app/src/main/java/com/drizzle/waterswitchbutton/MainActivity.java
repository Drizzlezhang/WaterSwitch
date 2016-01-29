package com.drizzle.waterswitchbutton;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import com.drizzle.waterswitch.OnWaterSwitchChangedListener;
import com.drizzle.waterswitch.WaterSwitchButton;

public class MainActivity extends AppCompatActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final WaterSwitchButton switchButton = (WaterSwitchButton) findViewById(R.id.water_switch);
		final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.layout);
		switchButton.setOnWaterSwitchChangedListener(new OnWaterSwitchChangedListener() {
			@Override public void onWaterSwitchChanged(boolean isChecked) {
				if (isChecked) {
					frameLayout.setBackgroundColor(Color.WHITE);
				} else {
					frameLayout.setBackgroundColor(Color.BLACK);
				}
			}
		});
	}
}
