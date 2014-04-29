package xreal.bsw_cv02;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity implements SensorEventListener {

	// EditText txtData;
	Button startButton;
	Button stopButton;

	File myFile;
	FileOutputStream fOut;
	OutputStreamWriter myOutWriter;
	BufferedWriter myBufferedWriter;
	PrintWriter myPrintWriter;

	// private SensorManager sensorManager;
	private long currentTime;
	private long startTime;

	float[] acceleration = new float[3];
	float[] rotationRate = new float[3];
	float[] magneticField = new float[3];

	boolean stopFlag = false;
	boolean startFlag = false;
	boolean isFirstSet = true;

	private Button recorderbtn;// button for recorder
	private float xAccel;
	private float yAccel;
	private float zAccel;
	private float xPreviousAccel;
	private float yPreviousAccel;
	private float zPreviousAccel;

	private boolean firstUpdate = true;
	private final float shakeThreshold = 0.9f;
	private boolean shakeInitiated = false;
	private SensorManager mySensorManager;
	boolean startedRecording = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// file name to be entered

		// start button sensor
		startButton = (Button) findViewById(R.id.button1);
		startButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// start recording the sensor data
				try {
					myFile = new File("/sdcard/TestVideo/" + "txtt8" + ".txt");
					myFile.createNewFile();

					fOut = new FileOutputStream(myFile);
					myOutWriter = new OutputStreamWriter(fOut);
					myBufferedWriter = new BufferedWriter(myOutWriter);
					myPrintWriter = new PrintWriter(myBufferedWriter);

					Toast.makeText(getBaseContext(),
							"Start recording the data", Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
				} finally {
					startFlag = true;
				}
			}
		});

		// stop button sensor
		stopButton = (Button) findViewById(R.id.button2);
		stopButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// stop recording the sensor data
				try {
					stopFlag = true;
					Toast.makeText(getBaseContext(),
							"Done recording the data set", Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		recorderbtn = (Button) findViewById(R.id.recorder);
		// start the VideoRecorder with accelerometer
		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mySensorManager.registerListener(this,
				mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		startedRecording = false;

		// start the VideoRecorder on the click of the button
		recorderbtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startedRecording = true;
				Intent intent = new Intent(Main.this, Recorder.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (startFlag) {

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				acceleration[0] = event.values[0];
				acceleration[1] = event.values[1];
				acceleration[2] = event.values[2];
			}

			if (isFirstSet) {
				startTime = System.currentTimeMillis();
				isFirstSet = false;
			}

			currentTime = System.currentTimeMillis();

			for (int i = 0; i < 1; i++) {
				if (!stopFlag) {
					save();
				}

				else {
					try {
						myOutWriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NullPointerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						fOut.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NullPointerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if (startedRecording == false) {
				updateAccelParameters(event.values[0], event.values[1],
						event.values[2]);
				if ((!shakeInitiated) && isAccelerationChanged()) {
					shakeInitiated = true;
				} else if ((shakeInitiated) && isAccelerationChanged()) {
					executeShakeAction();
				} else if ((shakeInitiated) && (!isAccelerationChanged())) {
					shakeInitiated = false;
				}
			}
		}
	}

	private void save() {

		myPrintWriter.println(currentTime - startTime + " " + acceleration[0]
				+ " " + acceleration[1] + " " + acceleration[2]);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the sensors
		mySensorManager.registerListener(this,
				mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mySensorManager.registerListener(this,
				mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_NORMAL);
		mySensorManager.registerListener(this,
				mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		mySensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	private void updateAccelParameters(float xNewAccel, float yNewAccel,
			float zNewAccel) {

		if (firstUpdate) {
			xPreviousAccel = xNewAccel;
			yPreviousAccel = yNewAccel;
			zPreviousAccel = zNewAccel;
			firstUpdate = false;
		} else {
			xPreviousAccel = xAccel;
			yPreviousAccel = yAccel;
			zPreviousAccel = zAccel;
		}
		xAccel = xNewAccel;
		yAccel = yNewAccel;
		zAccel = zNewAccel;
	}

	private boolean isAccelerationChanged() {
		float deltaX = Math.abs(xPreviousAccel - xAccel);
		float deltaY = Math.abs(yPreviousAccel - yAccel);
		float deltaZ = Math.abs(zPreviousAccel - zAccel);
		return (deltaX > shakeThreshold && deltaY > shakeThreshold)
				|| (deltaX > shakeThreshold && deltaZ > shakeThreshold)
				|| (deltaY > shakeThreshold && deltaZ > shakeThreshold);
	}

	private void executeShakeAction() {
		if (startedRecording == false) {
			startedRecording = true;
			Intent intent = new Intent(Main.this, Recorder.class);
			startActivity(intent);
		}
	}
}