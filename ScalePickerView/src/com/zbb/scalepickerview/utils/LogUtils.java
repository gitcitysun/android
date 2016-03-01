package com.zbb.scalepickerview.utils;

import android.util.Log;
import android.view.View;
/**
 * 
 * @author zbb
 * @date Mar 1, 2016
 */
public class LogUtils {
	
	public static boolean isDebug=true;
	public static String TAG="zbb";
	
	public static void d(Class cls,String str){
		if (isDebug) {
			Log.d(TAG, cls.getSimpleName()+":"+str);
		}
	}
	public static void d(View view,String str){
		if (isDebug) {
			Log.d(TAG, view.getClass().getSimpleName()+":"+str);
		}
	}
}
