package org.kanzian.stepcounter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer, mStepdetector;
	private float magnitudeOld = 0, magnitudeAverageOld = 0, diffOld,
			magnitudeAverage = 0;
	private final float timeConstant = (float) 0.015915494;
	private int stepcount = 0, stepcountDetector = 0;
	private boolean increasePrevious;
	private float maximum = Float.MIN_VALUE, minimum = Float.MAX_VALUE,
			average = Float.MIN_VALUE;
	private long timestampOld = System.nanoTime();
	private float treshold = 0.6f, peak;
	private float xo, yo, zo;
	private boolean NewStep;
	private int count = 0;
	boolean faultStep = false;
	private long lastStep = System.nanoTime();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		// mStepdetector = mSensorManager
		// .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		//
		// mSensorManager.registerListener(this, mStepdetector,
		// SensorManager.SENSOR_DELAY_FASTEST);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		// mSensorManager.registerListener(this, mStepdetector,
		// SensorManager.SENSOR_DELAY_FASTEST);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		long timestamp = System.nanoTime();
		TextView tvX = (TextView) findViewById(R.id.x_axis);
		tvX.setText(event.sensor.getType() + "");

		if (event.sensor.equals(mAccelerometer)) {

			TextView tvXa = (TextView) findViewById(R.id.x_axis_abs);
			TextView tvYa = (TextView) findViewById(R.id.y_axis_abs);
			TextView tvZa = (TextView) findViewById(R.id.z_axis_abs);
			TextView tvLin = (TextView) findViewById(R.id.lin);

			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			if (!mInitialized) {
				xo = x;
				yo = y;
				zo = z;
				tvXa.setText("0.0");
				tvYa.setText("0.0");
				tvYa.setText("0.0");
				mInitialized = true;
			} else {

				float dt = (timestamp - timestampOld) / 1000000000.0f;
				float alpha = dt / (timeConstant + dt);

				xo = (float) x * alpha + xo * (1.0f - alpha);
				yo = (float) y * alpha + yo * (1.0f - alpha);
				zo = (float) z * alpha + zo * (1.0f - alpha);

				float magnitude = (xo + yo + zo) / 3;

				if (magnitude >= maximum)
					maximum = magnitude;
				else if (magnitude <= minimum)
					minimum = magnitude;

				count++;

				if (count == 50) {
					count = 0;
					peak = maximum - minimum;
					average = (maximum - minimum) / 2;
					maximum = Float.MIN_VALUE;
					minimum = Float.MAX_VALUE;
				}

				if (Math.abs(magnitude - magnitudeAverageOld) > treshold) {
					magnitudeAverageOld = magnitudeAverage;
					magnitudeAverage = magnitude;
				} else {
					magnitudeAverageOld = magnitudeAverage;
				}

				if (magnitudeAverage < magnitudeAverageOld
						&& magnitude < average) {
					if (Math.abs(System.nanoTime() - lastStep) > (0.2 * 1000000000.0f))
						stepcount++;
					lastStep = System.nanoTime();
					tvLin.setText(stepcount + "");
				}

				timestampOld = timestamp;

				tvXa.setText(Float.toString(maximum));
				tvYa.setText(Float.toString(minimum));
				tvZa.setText(Float.toString(magnitudeAverage));

			}

		} else if (event.sensor.equals(mStepdetector)) {
			TextView tvStep = (TextView) findViewById(R.id.integrated);

			stepcountDetector++;
			tvStep.setText(stepcountDetector + "");
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void resetSteps(View view) {
		TextView tvLin = (TextView) findViewById(R.id.lin);
		TextView tvStep = (TextView) findViewById(R.id.integrated);
		stepcount = 0;
		stepcountDetector = 0;
		tvLin.setText(stepcount + "");
		tvStep.setText(stepcountDetector + "");
	}

	public void tresholdPlus(View view) {
		TextView tv = (TextView) findViewById(R.id.treshold);
		treshold += 0.2f;
		tv.setText(Float.toString(treshold));
	}

	public void tresholdMinus(View view) {
		TextView tv = (TextView) findViewById(R.id.treshold);
		treshold -= 0.2f;
		tv.setText(Float.toString(treshold));
	}
}
