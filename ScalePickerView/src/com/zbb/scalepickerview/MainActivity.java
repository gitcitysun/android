package com.zbb.scalepickerview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.zbb.scalepickerview.ScalePickerView.OnFmChangeListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final TextView tv=(TextView) findViewById(R.id.tv);
		ScalePickerView scalePickerView=(ScalePickerView) findViewById(R.id.scalepicker);
		scalePickerView.registListener(new OnFmChangeListener() {
			
			@Override
			public void onFMScroll(float fm) {
				tv.setText("当前="+fm+" MHZ...");
			}
			
			@Override
			public void onFMPickCancel() {
			}
			
			@Override
			public void onFMConfirm(float fm) {
				tv.setText("当前="+fm+" MHZ");
			}
		});
	}
}
