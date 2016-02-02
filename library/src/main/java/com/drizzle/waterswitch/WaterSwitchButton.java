package com.drizzle.waterswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Transformation;

/**
 * Created by drizzle on 16/1/28.
 */
public class WaterSwitchButton extends View implements View.OnClickListener {
	//status
	private boolean isChecked;
	private int PROGRESS;

	//color
	private int backColor;
	private int switchColor;

	//integer
	private int circleradius;
	private int smallcircleradius;

	private final int MAINPROGRESS = 80;
	private final int SECONDPROGRESS = 20;
	private final int DURATION = 500;

	//切点坐标,切线角度,贝塞尔控制点
	double angle;
	int bigTopX, bigTopY, bigBottomX, bigBottomY, smallTopX, smallTopY, smallBottomX, smallBottomY;
	int bezierTopX, bezierTopY, bezierBottomX, bezierBottomY;

	//tools
	private Paint backPaint;
	private Paint circlePaint;
	private Path waterPath;
	private RectF leftRect;
	private RectF rightRect;
	private RectF midRect;

	/**
	 * 状态变化回调
	 */
	public OnWaterSwitchChangedListener switchChangedListener;

	public void setOnWaterSwitchChangedListener(OnWaterSwitchChangedListener switchChangedListener) {
		this.switchChangedListener = switchChangedListener;
	}

	public WaterSwitchButton(Context context) {
		this(context, null);
	}

	public WaterSwitchButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaterSwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray array =
			context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaterSwitchButton, defStyleAttr, 0);
		isChecked = array.getBoolean(R.styleable.WaterSwitchButton_ischecked, false);
		backColor = array.getColor(R.styleable.WaterSwitchButton_backcolor, Color.LTGRAY);
		switchColor = array.getColor(R.styleable.WaterSwitchButton_switch_color, Color.GREEN);
		array.recycle();
		initData(context);
		initPaints();
		setOnClickListener(this);
	}

	//切换动画
	private void turnSwitch() {
		WaterAnimation animation = new WaterAnimation(0, 100, this);
		animation.setInterpolator(new BounceInterpolator());//设置不同的Interpolator
		animation.setDuration(DURATION);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {

			}

			@Override public void onAnimationEnd(Animation animation) {
				post(runnable);
			}

			@Override public void onAnimationRepeat(Animation animation) {

			}
		});
		startAnimation(animation);
	}

	@Override public void onClick(View v) {
		//只有当进度值为0时才允许点击
		if (PROGRESS == 0) {
			turnSwitch();
		} else {
			return;
		}
	}

	Runnable runnable = new Runnable() {
		@Override public void run() {
			if (isChecked) {
				isChecked = false;
			} else {
				isChecked = true;
			}
			if (switchChangedListener != null) {
				switchChangedListener.onWaterSwitchChanged(isChecked);
			}
			PROGRESS = 0;
		}
	};

	private void initData(Context context) {
		circleradius = DensityUtils.dip2px(context, 20);
		smallcircleradius = DensityUtils.dip2px(context, 12);
	}

	private void initPaints() {
		backPaint = new Paint();
		backPaint.setAntiAlias(true);
		backPaint.setStyle(Paint.Style.FILL);
		circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Paint.Style.FILL);
		waterPath = new Path();
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int width;
		int height;
		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = (int) (getPaddingLeft() + getPaddingRight() + circleradius * 4);
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = (int) (getPaddingTop() + getPaddingBottom() + circleradius * 2);
		}
		setMeasuredDimension(width, height);
	}

	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//画背景
		backPaint.setColor(backColor);
		leftRect = new RectF(getleftRx() - smallcircleradius, getMiddleRy() - smallcircleradius,
			getleftRx() + smallcircleradius, getMiddleRy() + smallcircleradius);
		rightRect = new RectF(getRightRx() - smallcircleradius, getMiddleRy() - smallcircleradius,
			getRightRx() + smallcircleradius, getMiddleRy() + smallcircleradius);
		midRect =
			new RectF(getleftRx(), getMiddleRy() - smallcircleradius, getRightRx(), getMiddleRy() + smallcircleradius);
		canvas.drawArc(leftRect, 90, 180, true, backPaint);
		canvas.drawArc(rightRect, 270, 180, true, backPaint);
		canvas.drawRect(midRect, backPaint);
		//画水滴
		circlePaint.setColor(switchColor);
		//从关闭到打开
		canvas.drawCircle(getBigCircleX(PROGRESS), getMiddleRy(), getBigRadius(PROGRESS), circlePaint);
		canvas.drawCircle(getSmallCircleX(PROGRESS), getMiddleRy(), getSmallRadius(PROGRESS), circlePaint);
		initPath();
		canvas.drawPath(waterPath, circlePaint);
	}

	//贝塞尔线
	private void initPath() {
		waterPath.reset();
		angle = getAngle();
		//获取四个切点和贝塞尔控制点
		if (isChecked) {
			bigTopX = getBigCircleX(PROGRESS) + (int) (getBigRadius(PROGRESS) * Math.sin(angle));
			bigBottomX = getBigCircleX(PROGRESS) + (int) (getBigRadius(PROGRESS) * Math.sin(angle));
			smallTopX = getSmallCircleX(PROGRESS) + (int) (getSmallRadius(PROGRESS) * Math.sin(angle));
			smallBottomX = getSmallCircleX(PROGRESS) + (int) (getSmallRadius(PROGRESS) * Math.sin(angle));
		} else {
			bigTopX = getBigCircleX(PROGRESS) - (int) (getBigRadius(PROGRESS) * Math.sin(angle));
			bigBottomX = getBigCircleX(PROGRESS) - (int) (getBigRadius(PROGRESS) * Math.sin(angle));
			smallTopX = getSmallCircleX(PROGRESS) - (int) (getSmallRadius(PROGRESS) * Math.sin(angle));
			smallBottomX = getSmallCircleX(PROGRESS) - (int) (getSmallRadius(PROGRESS) * Math.sin(angle));
		}
		bigTopY = getMiddleRy() - (int) (getBigRadius(PROGRESS) * Math.cos(angle));
		bigBottomY = getMiddleRy() + (int) (getBigRadius(PROGRESS) * Math.cos(angle));
		smallTopY = getMiddleRy() - (int) (getSmallRadius(PROGRESS) * Math.cos(angle));
		smallBottomY = getMiddleRy() + (int) (getSmallRadius(PROGRESS) * Math.cos(angle));

		bezierTopX = (smallTopX + bigTopX) / 2;
		bezierTopY = smallTopY - (smallTopY - bigTopY) / 4;
		bezierBottomX = (smallBottomX + smallBottomX) / 2;
		bezierBottomY = smallBottomY + (bigBottomY - smallBottomY) / 4;

		waterPath.moveTo(bigTopX, bigTopY);
		waterPath.quadTo(bezierTopX, bezierTopY, smallTopX, smallTopY);
		waterPath.lineTo(smallBottomX, smallBottomY);
		waterPath.quadTo(bezierBottomX, bezierBottomY, bigBottomX, bigBottomY);
		waterPath.close();
	}

	/**
	 * 获取两个固定圆心的位置
	 */
	private int getleftRx() {
		int backLength = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		return getPaddingLeft() + backLength / 2 - circleradius;
	}

	private int getMiddleRy() {
		int backHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
		return getPaddingTop() + backHeight / 2;
	}

	private int getRightRx() {
		int backLength = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		return getPaddingLeft() + backLength / 2 + circleradius;
	}

	/**
	 * 获得两个圆切线与圆心连线的夹角
	 */
	private double getAngle() {
		if (getSmallCircleX(PROGRESS) == getBigCircleX(PROGRESS)) {
			return 0;
		} else {
			if (isChecked) {
				return Math.asin(
					(circleradius - getSmallRadius(PROGRESS)) / (getSmallCircleX(PROGRESS) - getBigCircleX(PROGRESS)));
			} else {
				return Math.asin(
					(circleradius - getSmallRadius(PROGRESS)) / (getBigCircleX(PROGRESS) - getSmallCircleX(PROGRESS)));
			}
		}
	}

	/**
	 * 根据progress获取两个移动圆的圆心X值
	 */
	private int getBigCircleX(int progress) {
		if (!isChecked) {
			if (progress <= MAINPROGRESS) {
				return getleftRx() + progress * (getRightRx() - getleftRx()) / MAINPROGRESS;
			} else {
				return getRightRx();
			}
		} else {
			if (progress <= MAINPROGRESS) {
				return getRightRx() - progress * (getRightRx() - getleftRx()) / MAINPROGRESS;
			} else {
				return getleftRx();
			}
		}
	}

	private int getSmallCircleX(int progress) {
		if (!isChecked) {
			if (progress <= MAINPROGRESS) {
				return getleftRx();
			} else {
				return getleftRx() + (progress - MAINPROGRESS) * (getRightRx() - getleftRx()) / SECONDPROGRESS;
			}
		} else {
			if (progress <= MAINPROGRESS) {
				return getRightRx();
			} else {
				return getRightRx() - (progress - MAINPROGRESS) * (getRightRx() - getleftRx()) / SECONDPROGRESS;
			}
		}
	}

	/**
	 * 动态获取圆的半径
	 */
	private int getSmallRadius(int progress) {
		if (progress < MAINPROGRESS) {
			return (circleradius - circleradius * progress / (2 * MAINPROGRESS));
		} else {
			return circleradius * (progress - MAINPROGRESS) / (2 * SECONDPROGRESS);
		}
	}

	private int getBigRadius(int progress) {
		if (progress < SECONDPROGRESS) {
			return circleradius * (100 - progress) / 100;
		} else if (progress > MAINPROGRESS) {
			return circleradius * progress / 100;
		} else {
			return circleradius * MAINPROGRESS / 100;
		}
	}

	private void setPROGRESS(int PROGRESS) {
		this.PROGRESS = PROGRESS;
		postInvalidate();
	}

	public boolean isChecked() {
		return isChecked;
	}

	private class WaterAnimation extends Animation {
		private int startProgress;
		private int endProgress;
		private WaterSwitchButton switchButton;

		public WaterAnimation(int startProgress, int endProgress, WaterSwitchButton switchButton) {
			this.startProgress = startProgress;
			this.endProgress = endProgress;
			this.switchButton = switchButton;
		}

		@Override protected void applyTransformation(float interpolatedTime, Transformation t) {
			int progress = (int) (startProgress + ((endProgress - startProgress) * interpolatedTime));
			switchButton.setPROGRESS(progress);
			switchButton.requestLayout();
		}
	}
}