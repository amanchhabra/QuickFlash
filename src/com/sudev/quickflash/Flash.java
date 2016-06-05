package com.sudev.quickflash;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sudev.quickflash.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Transformation;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("NewApi")
public class Flash extends Activity  implements OnGestureListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private static final ScheduledExecutorService worker = Executors
			.newSingleThreadScheduledExecutor();

	private float scale;
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	private int redColor = 150;
	private int greenColor = 150;
	private int blueColor = 255;
	public static boolean switchOn = false;
	private SensorManager sensorManager;
	private long lastUpdate;
	public static boolean switchFree = true;
	public static boolean lightStarted = false;
	private float currentDegree = 0f;
	private TextView tvHeading;
	private float[] mGravity = new float[3];
	private float[] mGeomagnetic = new float[3];
	private static final int SWIPE_MIN_DISTANCE = 10;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 20;
	private int width;
	private int ht;
	private Camera camera;

	private int getBrighterColor(int val) {
		// redColor+=val;
		// greenColor+=val;
		System.out.println("lite " + redColor + " " + greenColor + " "
				+ blueColor);
		if (val != -1) {
			return Color.rgb(redColor + val, greenColor + val, blueColor);
		} else
			return (Color.rgb(0, 0, 0));
	}

	@SuppressLint("NewApi")
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flash);
		final GestureDetector gestureScanner = new GestureDetector(this);
		scale = getBaseContext().getResources().getDisplayMetrics().density;
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(size);
			width = size.x;
			ht = size.y;
		} else {
			width = display.getWidth();
			ht = display.getHeight();
		}
		final int height = ht;
		final View leftButton = findViewById(R.id.left_button);
		final View leftSliderLite = findViewById(R.id.left_slider_lite);
		final View rightSliderLite = findViewById(R.id.right_slider_lite);
		final View rightButton = findViewById(R.id.right_button);
		final View liteContainer = findViewById(R.id.lite_container);
		final View switchKey = findViewById(R.id.switch_key);
		final View switchButton = findViewById(R.id.switch_on);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		findViewById(R.id.center_container).setOnTouchListener(new View.OnTouchListener() { 
            @Override
           public boolean onTouch(View v, MotionEvent event){
                return gestureScanner.onTouchEvent(event);
           }
  });
		
		lastUpdate = System.currentTimeMillis();
		switchButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (switchFree) {
					switchFree = false;

					if (switchOn) {
						switchOn = false;
						Log.i("info", "torch is turned off!");                   
				           camera.stopPreview();
				           camera.release();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							switchKey.animate().translationX(0)
									.setDuration(400);
						} else {

						}
						int widthLite = calculateDp(40);
						int heightLite = calculateDp(0);
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
								widthLite, heightLite);
						lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
						leftSliderLite.setLayoutParams(lp);
						rightSliderLite.setLayoutParams(lp);
						leftButton.setY(height - calculateDp(104));
						rightButton.setY(height - calculateDp(104));
		            	int htLite = 0;
		            	animateView(htLite,findViewById(R.id.lite_filler));
						Runnable task = new Runnable() {
							public void run() {
							}

						};
						liteContainer.setBackgroundColor(getBrighterColor(-1));
						// worker.schedule(task, 200, TimeUnit.MILLISECONDS);
						switchButton.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.switch_off));
						lightStarted = false;
						Flash.switchFree = true;

					} else {
						switchOn = true;
						if(!lightStarted)
						{
						leftButton.setY(height/2);
						rightButton.setY(height/2);
						int requiredLite = ((int) ((height/2) / (scale * 27))) * 27;
						int widthLite = calculateDp(40);
						int heightLite = calculateDp(requiredLite);
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
								widthLite, heightLite);
						lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
						leftSliderLite.setLayoutParams(lp);
						rightSliderLite.setLayoutParams(lp);
						liteContainer
								.setBackgroundColor(getBrighterColor((int) (requiredLite / (scale * 5))));
						}
						Log.i("info", "torch is turned on!");
				           camera = Camera.open();
				           Parameters p = camera.getParameters();
				           p.setFlashMode(Parameters.FLASH_MODE_OFF);
				           camera.setParameters(p);
				           camera.startPreview();
				           p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				           camera.setParameters(p);
				           int htLite = ht-calculateDp((int)(switchButton.getHeight()*1.5));
		            	animateView(htLite,findViewById(R.id.lite_filler));
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							switchKey
									.animate()
									.translationX(
											(int) ((switchButton.getWidth() / 2)))
									.setDuration(400);
						} else {

						}
						Runnable task = new Runnable() {
							public void run() {

							}
						};
						liteContainer.setBackgroundColor(getBrighterColor(0));
						// worker.schedule(task, 200, TimeUnit.MILLISECONDS);

						switchButton.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.switch_on));
						Flash.switchFree = true;

					}
				}
			}

		});
		leftButton.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!Flash.switchOn) {
					lightStarted = true;
					switchButton.performClick();
					}
				float buttonPos = event.getRawY() - calculateDp(55);
				if (buttonPos > calculateDp(-10)
						&& buttonPos < (height - calculateDp(89))) {
					leftButton.setY(buttonPos);
					rightButton.setY(buttonPos);
					int requiredLite = ((int) ((height - buttonPos) / (scale * 27))) * 27;
					int widthLite = calculateDp(40);
					int heightLite = calculateDp(requiredLite);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
							widthLite, heightLite);
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					leftSliderLite.setLayoutParams(lp);
					rightSliderLite.setLayoutParams(lp);
					liteContainer
							.setBackgroundColor(getBrighterColor((int) (requiredLite / (scale * 5))));
				}

				return true;
			}
		});
		rightButton.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!Flash.switchOn) {
					lightStarted = true;
					switchButton.performClick();
				}
				float buttonPos = event.getRawY() - calculateDp(55);
				if (buttonPos > calculateDp(-10)
						&& buttonPos < (height - calculateDp(89))) {
					leftButton.setY(buttonPos);
					rightButton.setY(buttonPos);
					int requiredLite = ((int) ((height - buttonPos - 54) / (scale * 27))) * 27;
					int widthLite = calculateDp(40);
					int heightLite = calculateDp(requiredLite);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
							widthLite, heightLite);
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					leftSliderLite.setLayoutParams(lp);
					rightSliderLite.setLayoutParams(lp);
					liteContainer
							.setBackgroundColor(getBrighterColor((int) (requiredLite / (scale * 5))));
				}
				return true;
			}
		});
	}

	private int calculateDp(int val) {
		return (int) (val * scale + 0.5f);
	}

//	@Override
//	public void onSensorChanged(SensorEvent event) {
//		final View needle = findViewById(R.id.needle);
//		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//			for (int i = 0; i < 3; i++) {
//				mGravity[i] = event.values[i];
//				mGeomagnetic[i] = 0.1f;
//			}
//		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//			for (int i = 0; i < 3; i++) {
//				mGeomagnetic[i] = event.values[i];
//				tvHeading.setText("Details: " + Float.toString(mGeomagnetic[0])
//						+ " degra" + Float.toString(mGeomagnetic[1]) + " degra"
//						+ Float.toString(mGeomagnetic[2]) + " degra");
//
//			}
//
//		if (mGravity != null && mGeomagnetic != null) {
//			float R[] = new float[9];
//			float I[] = new float[9];
//			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
//					mGeomagnetic);
//			if (success) {
//				float orientation[] = new float[3];
//				SensorManager.getOrientation(R, orientation);
//				float degree = Math.round(orientation[0]);
//				tvHeading.setText("Heading: " + Float.toString(degree)
//						+ " degra");
//				// create a rotation animation (reverse turn degree degrees)
//				RotateAnimation ra = new RotateAnimation(currentDegree,
//						-degree, Animation.RELATIVE_TO_SELF, 0.5f,
//						Animation.RELATIVE_TO_SELF, 0.5f);
//
//				// how long the animation will take place
//				ra.setDuration(210);
//
//				// set the animation after the end of the reservation status
//				ra.setFillAfter(true);
//
//				// Start the animation
//				needle.startAnimation(ra);
//				currentDegree = -degree;
//
//			}
//		}
//		// if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//		// getAccelerometer(event);
//		// }
//		// get the angle around the z-axis rotated
//		// float degree = Math.round(event.values[0]);

//	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if (accelationSquareRoot >= 2) //
		{
			if (actualTime - lastUpdate < 200) {
				return;
			}
			lastUpdate = actualTime;
			// Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
			// .show();
			// if (color) {
			// view.setBackgroundColor(Color.GREEN);
			//
			// } else {
			// view.setBackgroundColor(Color.RED);
			// }
			// color = !color;
		}
	}

//	@Override
//	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//	}

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors
//		sensorManager.registerListener(this,
//				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//				SensorManager.SENSOR_DELAY_UI);
//		sensorManager.registerListener(this,
//				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
	 final View liteContainer = findViewById(R.id.lite_filler);
		 try {
	            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH){
	                return false;
	            }
	            // right to left swipe
	            if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
	                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	View switchButton = findViewById(R.id.switch_on);
	            	int heightLite = ht-calculateDp((int)(switchButton.getHeight()*1.5));
	            	animateView(heightLite,liteContainer);
	            		            	return true;
	            } 
	            // left to right swipe
	            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
	                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	View lite = findViewById(R.id.lite_style);
	            	int heightLite = 1;
	            	animateView(heightLite,liteContainer);
	            	return true;
	            }
	        } catch (Exception e) {

	        }
	        return false;
	}

	public void animateView(final int heightLite,final View liteContainer)
	{
		Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
            	liteContainer.getLayoutParams().height = interpolatedTime == 1
                        ? (int)(heightLite * interpolatedTime)
                        : (int)(heightLite * interpolatedTime);
                liteContainer.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(heightLite / liteContainer.getContext().getResources().getDisplayMetrics().density));
        liteContainer.startAnimation(a);

	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

}
