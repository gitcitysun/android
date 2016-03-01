/**
 * 
 */
package com.zbb.scalepickerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import com.zbb.scalepickerview.ScalePickerView.OnFmChangeListener;
import com.zbb.scalepickerview.utils.LogUtils;


/**
 * @author zbb
 * @date Feb 2, 2016
 * 
 * 
 * 1. 最大值和最小值合并, 显示的是最大值
 * 2. 边界的值会被标记
 * 
 * 职责
 * 
 * |-- ~获取某个x轴上的fm值
 * |-- 根据给定的fm最大值和最小值生成
 * |-- ~左右滚动
 * |-- 复位某个fm的操作
 */
public class FMScrollView extends View{

	// ----------field
	Paint mRulePaint;//长刻度
	Paint mRulePaintShort;// 短刻度
	Paint mEdgeLinePaint;
	TextPaint mFmPaint;//数字 
	int mScreenWidth;
	
	int mRuleAndFmPadding = 80;//刻度和数字底部的间距
	int mRulePadding=15;// 刻度之间的距离
	int mRuleLongWidth=4;// 长刻度的粗细
	int mRuleShortWidth=3;// 短刻度的粗细
	int mRuleLongLength=150;// 长的刻度长度
	int mRuleShortLength=60;// 短的刻度长度
	int mRuleItemWidth=0;// 一个item的宽度, 等于5个mRulePadding
	int mRuleTextColor=0xffffffff;//刻度的颜色
	int mRulePreLoad=8;//中间的item为准, 左右预加载的数目
	int mEdgeLineGap=20;//边缘线高出长刻度的部分
	int mEdgeLineColor=0xffff0000;// 要红色
	int mViewPaddingTop=30;//默认上方留着空白
	
	int mFmTextSize=30;//fm数字的大小
	int mFmTextColor=0xffffffff;//fm数字的颜色, 默认白色

//	int mBg=R.drawable.fm_picker_bg;
	
	int mMarginTop=50;
	int mMarginBottom=50;
	int lastScrollX=100;// 最近滚动的位置
	int scrollingOffset=0;// 在没移动一个item宽度的时候, 产生的偏移量
	int SCROLL_SPEED=150;//像素/s
	int SCROLL_FRAME=60;// 帧数
	int SCROLL_DURATION=0;// 更新间隔400ms
	int SCROLL_SPEED_PER_DURATION=0;// 单位时间移动的距离
	int SCROLL_FM_POS_X_RIGHT=0;// 获取FM的判断点
	int SCROLL_FM_POS_X_LEFT=0;// 获取FM的
	public float fmMax=105;// 获取当前的fm最小值
	public float fmMin=95; // 获取当前fm的最大值
	float mFMCount; // fm的数量
	float mFMNumHeight=0;// 数字的高度
	float mCurrFM=0;// 记录当前的fm
	int mCenterPosX=0;//屏幕中点的位置 
	boolean isCycle=true;
	boolean isScrollPerformed=false;//是否滚动的标志位
	Scroller mScroller;
	// List<OnFmChangeListener> onFmChangeListeners=new ArrayList<OnFmChangeListener>();
	OnFmChangeListener onFmChangeListener;
	Direction direction;
	
	//t 
	Paint mDebugPaint;

	/**
	 * 初始化成员
	 */
	private void init() {
		
		// init field 
		mFMCount=fmMax-fmMin+1;
		mCurrFM=(int)(fmMin+mFMCount/2);
		
		LogUtils.d(getClass(), "mCurrFM="+mCurrFM);
//		mScreenWidth=getContext().getResources().getDisplayMetrics().widthPixels;//N zbb 
//		mCenterPosX=mScreenWidth/2;
		mRuleItemWidth=6*mRulePadding;
		mScroller=new Scroller(getContext());
		SCROLL_DURATION=1000/SCROLL_FRAME;
		SCROLL_SPEED_PER_DURATION=(int) (SCROLL_SPEED/1000.0*SCROLL_DURATION);
		
		LogUtils.d(getClass(), "SCROLL_SPEED_PER_DURATION="+SCROLL_SPEED_PER_DURATION+";mRuleItemWidth="+mRuleItemWidth);
		
		// t
		mDebugPaint=new Paint();
		mDebugPaint.setStyle(Style.STROKE);
		mDebugPaint.setStrokeWidth(3);
		mDebugPaint.setColor(0xffff00ff);
		
		mRulePaint=new Paint();
		mRulePaint.setColor(mRuleTextColor);// 白色
		mRulePaint.setStyle(Style.STROKE);
		mRulePaint.setStrokeWidth(mRuleLongWidth);
		
		mRulePaintShort=new Paint(mRulePaint);
		mRulePaintShort.setStrokeWidth(mRuleShortWidth);
		
		//fm数字
		mFmPaint=new TextPaint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
		mFmPaint.setColor(mFmTextColor);
		mFmPaint.setTextSize(mFmTextSize);
		
		mEdgeLinePaint=new Paint();
		mEdgeLinePaint.setColor(mEdgeLineColor);// 白色
		mEdgeLinePaint.setStyle(Style.STROKE);
		mEdgeLinePaint.setStrokeWidth(mRuleLongWidth);
		
		// 设置背景
//		setBackgroundResource(mBg);
		setBackgroundColor(0xff3A5FCD);
	}
	
	/**
	 * 控制刻度移动的
	 */
	@SuppressLint("HandlerLeak")
	private Handler moveHandler=new Handler(){
		public void handleMessage(Message msg) {


//			mScroller.computeScrollOffset();
//			int currX=mScroller.getCurrX();
//			int finalX=mScroller.getFinalX();
//			LogUtils.d(getClass(), "currX="+currX+";fianlX="+finalX);
			
//			int delta=lastScrollX-currX;
//			doScroll(-delta);
//			lastScrollX=currX;
			
			
//			if (!mScroller.isFinished()) {
//				this.sendEmptyMessage(0);
//			}
			doScroll(SCROLL_SPEED_PER_DURATION);
//			notifiObserverScroll(fm)

			sendEmptyMessageDelayed(0, SCROLL_DURATION);
		};
	};
	
	public void setFM(float fmMax,float fmMin){
		
		this.fmMax=(float) Math.ceil(fmMax);
		this.fmMin=(float) Math.floor(fmMin);
		
		mFMCount=this.fmMax-this.fmMin+1;
		LogUtils.d(getClass(), "设置新的范围,max="+this.fmMax+";min="+this.fmMin);
		
		// 设置完范围后, 需要重新设置中心的fm
		setCurrFM(mCurrFM);
		
		postInvalidate();
	}
	
	/**
	 * 根据x轴的值得到FM
	 * 
	 * @param x
	 * @return
	 */
	public float getFM(int x){
		
		// 以中间的为基准
		 if (mScreenWidth==0) {
			 
			 LogUtils.d(getClass(), "error for mScreenWidth=0");
			return 0;// throw?
		}
		 
		/*
		 *  刻度往左滑动的时候，scrollingOffset为负值， 中心的值在变大 
		 */
		 float resultFM=(float) (((int)((mCurrFM+(x-mCenterPosX-scrollingOffset)/(float)mRuleItemWidth-0.1f)*10))/10.0); // 负值， 点的位置比中心的小
		 
		// resultFM=checkFmFormat(resultFM);
		
		return cycleFm(resultFM);
	}
	
	/**
	 * 是否是一位小数
	 * @param resultFM
	 * @return
	 */
	private float checkFmFormat(float resultFM) {
		if (resultFM*10%1!=0.0f) {
			LogUtils.d(getClass(), "检测到非法格式, resultFM="+resultFM);
			resultFM=(int)((resultFM*10)/10f);
		}
		return resultFM;
	}

	private void notifiObserverScroll(float fm){
		if (onFmChangeListener!=null) {
			onFmChangeListener.onFMScroll(fm);
		}
	}
	
	private void notifiObserverConfirm(float fm){
		if (onFmChangeListener!=null) {
			onFmChangeListener.onFMConfirm(fm);
		}
	}
	
	public static enum Direction{
		LEFT,RIGHT
	}
	
	
	/**
	 * 注册监听器
	 * @param onFmChangeListener
	 */
	public void registOnFmChangeListener(OnFmChangeListener onFmChangeListener){
//		if (!onFmChangeListeners.contains(onFmChangeListener)) {
//			onFmChangeListeners.add(onFmChangeListener);
//		}
		this.onFmChangeListener=onFmChangeListener;
	}
	
	public void unRegistOnFmChangeListener(){
//		if (onFmChangeListeners.contains(onFmChangeListener)) {
//			onFmChangeListeners.remove(onFmChangeListener);
//		}
		this.onFmChangeListener=null;
	}
	
	/**
	 * 对象滚动的方法
	 * 
	 * @param delta
	 */
	private void doScroll(int delta){
		
		scrollingOffset+=delta;
		int offsetCount = scrollingOffset/mRuleAndFmPadding;// 滚动多少个item的宽度
		
		
//		LogUtils.d(getClass(), "scrollingOffset="+scrollingOffset);
		if (Math.abs(scrollingOffset)>=mRuleItemWidth) {
			float newCenterFm=mCurrFM-(int)((scrollingOffset)/mRuleItemWidth);// 方向反的
//			LogUtils.d(getClass(), "newCenterFm="+newCenterFm);
			setCurrFM(cycleFm(newCenterFm));
			scrollingOffset=0;
		}
		// 需要考虑循环
		invalidate();
		
		//update scroll offset
//		scrollingOffset=scrollingOffset-offsetCount*mRuleAndFmPadding;
	}
	
	/**
	 * fm循环的
	 * @param oldFm
	 * @return
	 */
	private float cycleFm(float oldFm){
		while(true){
			if (isCycle) {
				
				if (oldFm<=fmMin) {// 最大值和最小值重叠
					oldFm+=(mFMCount-1);
				}else if(oldFm>fmMax){
					oldFm-=(mFMCount-1);
				}else {
					break;
				}
			}else {
				break;
			}
		}
		return oldFm;
	}
	
	/**
	 * 开始滚动
	 */
	public void startScroll(Direction direction){
		
		this.direction=direction;
		if (direction==Direction.LEFT) {
			SCROLL_SPEED_PER_DURATION=-Math.abs(SCROLL_SPEED_PER_DURATION);
		}else {
			SCROLL_SPEED_PER_DURATION=Math.abs(SCROLL_SPEED_PER_DURATION);
		}
//		if (!mScroller.computeScrollOffset()) {
//			LogUtils.d(getClass(), "开始滚动");
//			
//			mScroller.startScroll(100, 500, 300, 0, 4400);
//			
//			moveHandler.sendEmptyMessage(0);
//		}else {
//			
//			mScroller.forceFinished(true);
//		}
		
		isScrollPerformed=true;
//		scrollingOffset=0;
		moveHandler.removeMessages(0);
		
		moveHandler.sendEmptyMessage(0);
	}
	
	/**
	 * 停止滚动
	 */
	public void stopScroll(){
		
		isScrollPerformed=false;
//		notifiObserverConfirm(fm)
		moveHandler.removeMessages(0);
	}
	
	
	/**
	 * 对外提供
	 * @return
	 */
	public boolean isScroll(){
		return isScrollPerformed;
	}
	
	/**
	 * 设置当前的fm, 可以用来归位
	 */
	public void setCurrFM(float fm){
		
//		LogUtils.d(getClass(), "...setCurrFM(), 设置的fm="+fm);
//		if (isCycle) {
//			
//			if (fm < fmMin) {
//
//				fm += mFMCount;
//			} else if (fm > fmMax) {
//
////				LogUtils.d(getClass(), "fm="+fm);
//				fm = fm-mFMCount+1;// 不显示最小值
//			}
//		}
		
		LogUtils.d(getClass(), "当前中心的fm="+fm);
		
		int fmInt=(int)fm;
		float fmDec=fm-fmInt;//小数部分
		
		mCurrFM=fmInt;// mCurrFM只是整数部分
		scrollingOffset=-(int) (mRuleItemWidth*fmDec);
		
//		mCurrFM=fm;
		
		postInvalidate();
	}
	
	/**
	 * 包括刻度和fm数字, 绘制一个item
	 * 
	 * @param canvas
	 * @param posX 左上角的
	 * @param posY
	 */
	private void drawItem(Canvas canvas, int posX,int posY, int fmNum){
		
		canvas.drawLine(posX, posY, posX, posY+mRuleLongLength, mRulePaint);
		
		int startX;
		int startY;
		int stopX;
		int stopY;
		
		// 画四根短的
		for (int i = 1; i < 6; i++) {
			startX=posX+i*mRulePadding;
			startY=posY+(mRuleLongLength-mRuleShortLength)/2;
			stopX=startX;
			stopY=startY+mRuleShortLength;
			
			canvas.drawLine(startX, startY, stopX, stopY, mRulePaintShort);
		}

		// 画数字
		float fmNumWidth=mFmPaint.measureText(String.valueOf(fmNum));
		
		// 标记临界的线
		if (fmNum==fmMin||fmNum==fmMax) {
			
			drawEdgeLine(canvas, posX, posY);
		}
//		else {
			startX=(int) (posX-fmNumWidth/2);
			startY=posY+mRuleLongLength+mRuleAndFmPadding;
			canvas.drawText(String.valueOf(fmNum), startX, startY, mFmPaint);
//		}
	}
	
	/**
	 * 绘制多个items
	 * @param canvas
	 * @param startX
	 * @param startY
	 */
	private void drawItems(Canvas canvas, int startX, int startY){

		for (int i = -mRulePreLoad; i < mRulePreLoad+1; i++) {// N zbb 左边八个右边八个 共17个
			
			float showFM=mCurrFM+i;// 当前显示的fm
			
//			LogUtils.d(getClass(), "drawitems 显示的fm="+showFM+";fmcount="+mFMCount);
//			if (isCycle) {
//				if (showFM <= fmMin) {
//					// 84 85
//					showFM +=(mFMCount-1);// 最大值和最小值合并
//				} else if (showFM > fmMax) {
//
//					showFM = showFM-mFMCount+1;
//				}
////				else if(showFM==fmMin){
////					showFM=fmMax;
////				}
//			}
			drawItem(canvas, startX+i*mRuleItemWidth, startY, (int)cycleFm(showFM));
		}
	}
	
	/**
	 * 
	 * 画临界的线
	 * @param canvas
	 * @param i
	 * @param startY
	 */
	private void drawEdgeLine(Canvas canvas, int startX, int startY) {
		
		canvas.drawLine(startX, startY-20, startX, startY+mRuleLongLength+mEdgeLineGap, mEdgeLinePaint);
	}

	/**
	 * 绘制刻度
	 */
	private void drawRules(){
		
	}
	
	/**
	 * 绘制下方的数字
	 */
	private void drawFms(){
		
	}
	
	/**
	 * 绘制边框
	 */
	private void drawDebugRect(Canvas canvas, int startY){
		
//		canvas.drawlin
	}
	
	/**
	 * 得到对应位置的fm
	 */
	public void getFmByWidth(float width){
		
	}
	
	/*
	 *  
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
//		LogUtils.d(getClass(), "onDraw()....");
		
		// initResourceIfNecessary();
		
//		LogUtils.d(getClass(), "mCenterPosX="+mCenterPosX);
		drawItems(canvas, mCenterPosX+scrollingOffset, mViewPaddingTop);
		
		//t 标志物
//		Paint textpaPaint=new Paint();
//		textpaPaint.setColor(0xff00ff00);
//		textpaPaint.setStyle(Style.STROKE);
//		textpaPaint.setStrokeWidth(8);
//		canvas.drawText("90", 100+scrollingOffset, 500, mFmPaint);
//		canvas.drawPoint(100, 500, textpaPaint);
		
		
		//画一根中心的线
		Paint redPaint=new Paint();
		redPaint.setStyle(Style.STROKE);
		redPaint.setStrokeWidth(3);
		redPaint.setColor(0xff00ff00);
		canvas.drawLine(mCenterPosX, 0, mCenterPosX, 500, redPaint);
		
	}
	
	/**
	 * 初始化资源
	 */
	private void initResourceIfNecessary() {
		if (mFMNumHeight==0) {
			mFMNumHeight=mFmPaint.measureText("1");
			LogUtils.d(getClass(), "mFMNumHeight="+mFMNumHeight);
		}
	}

	/*
	 * 确定布局宽高
	 * 
	 * 	限高, 默认就是刻度的长度+数字的高度+数字和刻度的间隔
	 *  
	 *  (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		

		// 获得它的父容器为它设置的测量模式和大小
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
		
		LogUtils.d(getClass(), "父控件提供的宽高="+sizeWidth+"*"+sizeHeight);
		
		initResourceIfNecessary();
		
		int finalWidth=0;
		int finalHeight=0;
		
		// 测量高度
		int minHeight=(int) (mRuleLongLength+mRuleAndFmPadding+mFMNumHeight+mViewPaddingTop);
		
		if (modeHeight==MeasureSpec.EXACTLY) {
			
			
			finalHeight=Math.max(sizeHeight, minHeight);
			finalWidth=sizeWidth;
		}else{
			
			// 默认就是屏幕的宽度
			finalWidth=getContext().getResources().getDisplayMetrics().widthPixels;
			finalHeight=minHeight;
		}
		
		LogUtils.d(getClass(), "最终的宽高是:"+finalWidth+":"+finalHeight);
		
		// init field 
		mScreenWidth=finalWidth;
		mCenterPosX=finalWidth/2;
		
		
		
		setMeasuredDimension(finalWidth, finalHeight);
	}
	
	//------ 构造器

	public FMScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public FMScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public FMScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
}
