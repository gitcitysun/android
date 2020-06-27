<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

有一个高于system bar的方法
windowmanager.layoutparams
flags = FLAG_LAYOUT_IN_SCREEN | FLAG_FULLSCREEN
type = TYPE_SYSTEM_ERROR

关键词WindowManager, 参数WindowManager.LayoutParams.type 

　/**
　　* 判断当前界面是否是桌面
　　*/
　　private boolean isHome() {
　　ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
　　List rti = mActivityManager.getRunningTasks(1);
　　return getHomes().contains(rti.get(0).topActivity.getPackageName());
　　}


=======================
1. 一个部局文件,定义显示的样子, 然后得到这个view, 处理事件
2. sp文件保存的是上一次悬浮窗的位置
3. 悬浮窗的事件由服务来处理
4. 监听用户的操作事件
5. 创建和关闭悬浮窗的代码写在一个工具类里
