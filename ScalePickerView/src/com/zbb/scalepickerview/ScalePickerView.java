/**
 * 
 */
package com.zbb.scalepickerview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.zbb.scalepickerview.FMScrollView.Direction;
import com.zbb.scalepickerview.utils.LogUtils;

/**
 * @author zbb
 * @date Jan 25, 2016
 * 
 * 包含了 FMScrollView 和 Thumb(Seekbar)
 * 
 * TODO
 */
public class ScalePickerView extends ViewGroup{
	
//	private FMThumbView mFmThumbView;
	
	private boolean isQuickSeleted=true;//允许快速选择, 无需移动滑块
	private int MAX_PROGRESS=1000;
	
	private int mThumbRes=R.drawable.ic_launcher;//t
	private int mBgRes;
	private int minHeight;// 定义最小的高度
	private int maxVelocity;// 背景移动最大速率
	private int mEdgeWidth=100;
	private int mThumbPosX=-1;
	private int mThumbPosY=-1;
	private int mThumbCenterPosX=-1;// thumb左边中心的x坐标, 相对于seekbar自身
	private int mThumbCenterPosXRight=-1;
	private int mSeekbarCenterPosX=-1;
	private int tThumbMoveDis=-1; //手指滑动距离seekba中心的距离
	
	private ImageView mThumbIv;
	private ImageView mBgIv;
	private SeekBar mThumbSb;
	private FMScrollView mScrollView;
	
	GestureDetector mGestureDetector;
	Drawable mThumbDrawable;
	boolean isTrack=false;// 滑块是否是移动的状态
	boolean isReadyWheeling=false;
	OnFmChangeListener mFmChangeListener;
	
	public void registListener(OnFmChangeListener onFmChangeListener){
		mFmChangeListener=onFmChangeListener;
	}
	
	/**
	 * 构造器中初始化对象
	 * @param context
	 */
	private void init(Context context){
		// init field
//		mGestureDetector=new GestureDetector(context, mThumbGestureListener);
//		
//		// 添加thumb
//		mThumbIv=new ImageView(context);
////		mThumbIv.setLayoutParams(new LayoutParams(100, 100));
//		mThumbIv.setImageResource(mThumbRes);
//		mThumbIv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		mThumbIv.setOnTouchListener(onThumbTouchListener);
//		addView(mThumbIv);
		
		mScrollView = new FMScrollView(getContext());
		addView(mScrollView);
		
		/*
		 * seekbar返回的
		 * 
		 * wheel滚动的速度
		 * wheel是否滚动
		 * wheel滚动的方向
		 * wheel当前界面中的value: thumb中心点的x坐标
		 */
		mThumbSb=new SeekBar(context);
		
		mThumbDrawable=context.getResources().getDrawable(mThumbRes);
		mThumbSb.setMax(MAX_PROGRESS);
		mThumbSb.setThumb(mThumbDrawable);
		mThumbSb.setProgressDrawable(context.getResources().getDrawable(R.drawable.fm_seekbar_empty_bg));
		mThumbSb.setLayoutParams(new LayoutParams(-1, -2));
//		LogUtils.d(getClass(), "thumb 图片的高度="+mThumbDrawable.getBounds().height());
		addView(mThumbSb);
		
		// 添加监听器
		mThumbSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				LogUtils.d(getClass(), "...onStartTrackingTouch()");

				Rect rect = seekBar.getThumb().getBounds();
				
				LogUtils.d(getClass(), rect.toShortString()); // 这里的坐标是相对于自身的
				LogUtils.d(getClass(), "thumb的中心坐标是="+rect.centerX());
				
				isTrack=true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				LogUtils.d(getClass(), "...onStopTrackingTouch()");

				Rect rect = seekBar.getThumb().getBounds();
				LogUtils.d(getClass(), rect.toShortString());
				
				isTrack=false;
				
				notifyListenerFMConfirm();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
//				LogUtils.d(getClass(), "...onProgressChanged(), progress="
//						+ progress + ";fromUser=" + fromUser);
				
				if (progress==MAX_PROGRESS||progress==0) {
					isReadyWheeling=true;
				}else {
					isReadyWheeling=false;
				}
				
				/*
				 * x坐标=seekBar.getThumb().getBounds()中的x坐标+mEdgeWidth
				 */
				notifyListenerFMScroll();
			}
		});
		mThumbSb.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isAllowWheeling()) {
					
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						
						
						break;
					case MotionEvent.ACTION_MOVE:
						
						// 需要判断左右
						
						float x=event.getX();
						float wheelingPerc=0;
						
						if (x<mThumbCenterPosX) {
							// 左滑
							
							/**
							 * <pre>
							 * 速率根据手指滑动的距离判断
							 * 
							 * 手指当前位置与基准位置进行比较
							 * 基准位置, 1. seekbar左边缘 2.thumb最边上时中心的位置(相差thumb宽度的一半)
							 * 
							 * 两种情况:
							 * 
							 * 1. thumb从中间一直滑到边缘,然后手指继续往下滑动
							 * 2. thumb已处在边缘, 手指往下滑动
							 * 
							 * 	- 只是按下, 不会调用action_move
							 * </pre>
							 */
							wheelingPerc=(x-mThumbCenterPosX)/(float)(mThumbCenterPosX+mEdgeWidth);
							
							if (!mScrollView.isScroll()) {
								mScrollView.startScroll(Direction.RIGHT);
							}
							
						}else {
							// 右滑
							wheelingPerc=(x-mThumbCenterPosXRight)/(float)(mThumbCenterPosX+mEdgeWidth);
							
							if (!mScrollView.isScroll()) {
								mScrollView.startScroll(Direction.LEFT);
							}
						}
						
						 // LogUtils.d(getClass(), "滚动速率百分比="+wheelingPerc);
						notifyListenerFMScroll();
						
						break;
					case MotionEvent.ACTION_UP:
						
						stopScroll();
						break;
					default:
						break;
					}
				}else {
					stopScroll();
				}
				
				
				return false;
			}
		});
		
		// 添加bg
//		mBgIv=new ImageView(context);
//		mBgIv.setImageResource(mb)
	}
	
	public void setFM(float max, float min){
		mScrollView.setFM(max, min);
	}
	
	public void setScrollEnable(boolean enable){
		mThumbSb.setEnabled(enable);
	}
	
	public void setCurrFM(float fm){
		
		// TODO 先瞬时的
		
		// thumb到中间
		mThumbSb.setProgress(mThumbSb.getMax()/2);
		// scrollview 对应的滚动到中间
		mScrollView.setCurrFM(fm);
//		notifyListenerFMConfirm();
		if (mFmChangeListener!=null) {
			mFmChangeListener.onFMConfirm(fm);
		}
	}
	
	/**
	 * 适配新的fm
	 * 
	 * 如果新的fm超出返回,扩大滚动的范围
	 */
	public void checkFM(float fm) {
		
		float tFmMax=0;
		float tFmMin=0;
		if (fm>mScrollView.fmMax) {
			tFmMax=fm;
			tFmMin=mScrollView.fmMin;
			mScrollView.setFM(tFmMax, tFmMin);
		}else if(fm<mScrollView.fmMin){
			tFmMax=mScrollView.fmMax;
			tFmMin=fm;
			mScrollView.setFM(tFmMax, tFmMin);
		}
	}

	private void notifyListenerFMScroll(){
		if (mFmChangeListener!=null) {
			mFmChangeListener.onFMScroll(getFM(mThumbSb));
		}
//		getFM(mThumbSb);
	}
	
	private void notifyListenerFMConfirm(){
		if (mFmChangeListener!=null) {
			mFmChangeListener.onFMConfirm(getFM(mThumbSb));
		}
//		LogUtils.d(getClass(), "确定的fm="+getFM(mThumbSb));
	}
	
	
	
	public float getFM(SeekBar seekBar){
		int fmX=seekBar.getThumb().getBounds().centerX()+mEdgeWidth;
		float fm = mScrollView.getFM(fmX);
		LogUtils.d(getClass(), "获取到的fm="+fm);
		
		return fm;// N zbb ....有误差
	}
	
	private void stopScroll(){
		if (mScrollView.isScroll()) {
			mScrollView.stopScroll();
		}
	}
	
	/**
	 * Wheeling是否允许滚动了
	 * @return
	 */
	private boolean isAllowWheeling(){
		return isTrack&&isReadyWheeling;
	}
	
//	private OnTouchListener onThumbTouchListener=new OnTouchListener() {
//		
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			
////			LogUtils.d(getClass(), "...onTouch()....");
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				LogUtils.d(getClass(), "...MotionEvent.ACTION_DOWN, 坐标:"+event.getX()+","+event.getY());
//				
//				// 需要记录原来的位置
////				LogUtils.d(getClass(), "action_down时的位置是:"+mthu);
//				tDownPosX=event.getX();
//				break;
//			case MotionEvent.ACTION_MOVE:
//				LogUtils.d(getClass(), "...MotionEvent.ACTION_MOVE, 坐标:"+event.getX()+","+event.getY());
//				
//				tThumbMoveDis=(int) (event.getX()-tDownPosX);
//				mThumbIv.setX(mThumbPosX+tThumbMoveDis);
//				break;
//			case MotionEvent.ACTION_UP:
//				LogUtils.d(getClass(), "...MotionEvent.ACTION_UP, 坐标:"+event.getX()+","+event.getY());
//				
//				mThumbPosX+=tThumbMoveDis;
//				tDownPosX=-1;
//				tThumbMoveDis=-1;
//				break;
//			default:
//				break;
//			}
//			
//			return true; // true 表示不传递下去
//		}
//	};
	
//	private SimpleOnGestureListener mThumbGestureListener=new SimpleOnGestureListener(){
//		
//		public boolean onDown(MotionEvent e) {
////			LogUtils.d(getClass(), "...onDown()");
//			return false;
//		};
//		
//		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//			
//			// 
//			LogUtils.d(getClass(), "...distanceX="+distanceX);
//			
//			return false;
//		};
//		
//		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//			LogUtils.d(getClass(), "...onFliing()");
//			return super.onFling(e1, e2, velocityX, velocityY);
//		};
//	};

	/* (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 * 
	 * width: margin
	 * height : 固定值
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		LogUtils.d(getClass(), "onMeasure()...");
		
		// 获得它的父容器为它设置的测量模式和大小
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
		
		LogUtils.d(getClass(), "sizeWidth="+sizeWidth+";sizeHeight="+sizeHeight);

		//所有的孩子采用同样的测量方式
		for (int i = 0; i < getChildCount(); i++) {
			View cThumb=getChildAt(i);
			measureChild(cThumb, widthMeasureSpec, heightMeasureSpec);
		}
		
		initResIfNecessary();
		
		int finalHeight=0;
		
//		if (thumbHeight>minHeight) {
//			finalHeight=thumbHeight;
//		}
		
//		if (modeWidth!=MeasureSpec.EXACTLY) {
//			
//			int thumbHeight=getChildAt(1).getHeight();
//			int scrollHeight=getChildAt(0).getHeight();
//			if (thumbHeight>scrollHeight) {
//				finalHeight=thumbHeight;
//			}else {
//				finalHeight=scrollHeight;
//			}
//		}else {
//			finalHeight=sizeHeight;
//		}
		
//		if (modeWidth==MeasureSpec.EXACTLY) {//matchparent or 具体的值
//			if (finalHeight<sizeHeight) {
//				finalHeight=sizeHeight;
//			}
//		}
		
		
		
		setMeasuredDimension(sizeWidth, sizeHeight);// 使用的时候采用固定的模式, matchparent+固定值
	}
	
	
	/**
	 * 在onMeasure中调用, 初始化一些资源
	 */
	private void initResIfNecessary(){
		View cThumb=getChildAt(1);
		// 获取thumb的高度
		int thumbHeight=mThumbSb.getThumb().getBounds().height();
		// 获取thumb的宽度
		int thumbWidth=mThumbSb.getThumb().getBounds().width();
		
		// 获取thumb中心的坐标
		if (mThumbCenterPosX==-1) {
			mThumbCenterPosX=thumbWidth/2;
			mThumbCenterPosXRight=cThumb.getMeasuredWidth()-mEdgeWidth*2-thumbWidth/2;
		}
		
		// 获取seekbar中心的位置
		if (mSeekbarCenterPosX==-1) {
			mSeekbarCenterPosX=(cThumb.getMeasuredWidth()-mEdgeWidth*2)/2;
		}
		
		LogUtils.d(getClass(), "thumbHeight="+thumbHeight+";thumbWidth="+thumbWidth);	
		LogUtils.d(getClass(), "获取左边中心的x坐标="+mThumbCenterPosX+";右边的="+mThumbCenterPosXRight);
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		LogUtils.d(getClass(), "onDraw()...");
	}
	
	/* (non-Javadoc)
	 * 
	 * 排列方式?
	 * 
	 * 三个都居中
	 * 
	 * 依次 bg scrollview thumb
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		LogUtils.d(getClass(), "onLayout()....");
		
		int cl;
		int ct;
		int cr;
		int cb;
		
		//控制孩子的位置
		int cCount=getChildCount();
		LogUtils.d(getClass(), "cCount="+cCount);
		
		// scroll view
		View cScroll=getChildAt(0);
		cl=0;
		ct=getHeight()/2-cScroll.getMeasuredHeight()/2;
		cr=getWidth();
		cb=getHeight()/2+cScroll.getMeasuredHeight()/2;
		cScroll.layout(cl,ct,cr,cb);
		
		// thumb	
		View cThumb=getChildAt(1);
		cl=mEdgeWidth;
		ct=getHeight()/2-cThumb.getMeasuredHeight()/2;
		cr=getWidth()-mEdgeWidth;
		cb=getHeight()/2+cThumb.getMeasuredHeight()/2;
		cThumb.layout(cl,ct,cr,cb);// t margin暂时无效
		
//		if (mThumbPosX==-1) {// 初始化thumb的位置
//			mThumbPosX=getWidth()/2-cThumb.getMeasuredWidth()/2;// thumb 左上角x坐标
//			mThumbPosY=getHeight()/2-cThumb.getMeasuredHeight()/2;
//			
//			LogUtils.d(getClass(), "初始化thumb的位置 mThumbPosX="+mThumbPosX+";mThumbPosY="+mThumbPosY);
//		}
		
	}
	
	/* (non-Javadoc)
	 * @see android.view.ViewGroup#generateLayoutParams(android.view.ViewGroup.LayoutParams)
	 */
	@Override
	protected LayoutParams generateLayoutParams(LayoutParams p) {
		LogUtils.d(getClass(), "generateLayoutParams()...");
		return new MarginLayoutParams(p);
	}
	
	/**
	 * 对外提供的方法, 得到当前的fm
	 * 
	 * @return
	 */
//	public float getFM(){
//		return 0;
//	}

	/**
	 * 对外提供的接口
	 * @author zbb
	 * @date Jan 28, 2016
	 */
	public static interface OnFmChangeListener{
		
		/**
		 * 滚动时返回的fm
		 * @param fm
		 */
		public void onFMScroll(float fm);
		
		/**
		 * 最终确认的fm
		 * @param fm
		 */
		public void onFMConfirm(float fm);
		
		/**
		 * 取消的操作
		 */
		public void onFMPickCancel();
	}
	

	public ScalePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public ScalePickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public ScalePickerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}
}
