package com.example.android_danglingball;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
/*
 * 
 * 特点:
 * 1. 长时间位于所有界面的上方
 * 2. 响应点击(触摸)事件
 * 
 * 测试环境
 * 1. 
 * 
 * @author zbb
 *
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}
