package org.kanzian.stepcounter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private float magnitudeOld=0, magnitudeAverageOld;
	private final float alpha = (float) 0.5;
	private int stepcount = 0;
	private boolean increasePrevious;
	private float maximum,minimum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
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

		TextView tvXa = (TextView) findViewById(R.id.x_axis_abs);
		TextView tvYa = (TextView) findViewById(R.id.y_axis_abs);
		TextView tvZa = (TextView) findViewById(R.id.z_axis_abs);
		TextView tvLin = (TextView) findViewById(R.id.lin);

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		if (!mInitialized) {
			tvXa.setText("0.0");
			tvYa.setText("0.0");
			tvYa.setText("0.0");
			mInitialized = true;
		} else {
			
			float magnitude = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
					+ Math.pow(z, 2));
			
			float magnitudeAverage = (1 - alpha) * magnitudeAverageOld + alpha * magnitude;
		
			boolean increase = (magnitude - magnitudeOld > 0) ? true : false;
			if(increase != increasePrevious) {
				if(increase == true)
					minimum=magnitudeOld;
				else maximum=magnitudeOld;
				
				float diff = maximum-minimum;
				
				if(diff >= 6 ) {
					stepcount++;
					tvLin.setText(stepcount+"");
				}
			}
			
			increasePrevious = increase;
			magnitudeOld = magnitude;
			magnitudeAverageOld = magnitudeAverage;
			
			tvXa.setText(Float.toString(maximum));
			tvYa.setText(Float.toString(minimum));
			tvZa.setText(Float.toString(magnitudeAverage));

		}
		
	
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void resetSteps(View view) {
		TextView tvLin = (TextView) findViewById(R.id.lin);
		stepcount = 0;
		tvLin.setText(stepcount + "");
	}
}
