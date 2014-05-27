package xreal.bsw_cv02;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
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

	private final float shakeThreshold = 7.0f;// 0.9f;16.0f;//freno brusco;
												// 10.0f;//rompemuelle
	private boolean shakeInitiated = false;
	private SensorManager mySensorManager;
	boolean startedRecording = false;

	// FB---------------------------------------------------------
	private LoginButton loginBtn;
	private Button postImageBtn;

	private TextView userName;

	private UiLifecycleHelper uiHelper;

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private static final String TAG = null;
	private static String message = "Sample status posted from android app";

	// FB---------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// file name to be entered

		// start button sensor
		startButton = (Button) findViewById(R.id.sensorONButton);
		startButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// start recording the sensor data
				Calendar c = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy.MM.dd_HH.mm.ss");
				final String formattedDate = df.format(c.getTime());
				String sensor_name = formattedDate;
				try {
					myFile = new File("/sdcard/TestVideo/" + sensor_name
							+ ".txt");
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
		stopButton = (Button) findViewById(R.id.sensorOFFButton);
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

		// FBoncreate---------------------------------------------------------
		uiHelper = new UiLifecycleHelper(this, statusCallback);
		uiHelper.onCreate(savedInstanceState);
		userName = (TextView) findViewById(R.id.welcome);
		loginBtn = (LoginButton) findViewById(R.id.loginButton);
		loginBtn.setUserInfoChangedCallback(new UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				if (user != null) {
					userName.setText("Hello, " + user.getName());
				} else {
					userName.setText("You are not logged");
				}
			}
		});

		postImageBtn = (Button) findViewById(R.id.shareButton);
		postImageBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				postImage();
			}
		});

		// FB---------------------------------------------------------
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
					stop_sensor();
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

	private void save() {

		myPrintWriter.println(currentTime - startTime + " " + acceleration[0]
				+ " " + acceleration[1] + " " + acceleration[2]);
	}

	private void stop_sensor() {
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

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the sensors
		mySensorManager.registerListener(this,
				mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		startedRecording = false;
		// FB---------------------------------------------------------
		uiHelper.onResume();
		buttonsEnabled(Session.getActiveSession().isOpened());
		// FB---------------------------------------------------------
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		mySensorManager.unregisterListener(this);
		// FB---------------------------------------------------------
		uiHelper.onPause();
		// FB---------------------------------------------------------
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
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

	private boolean isAccelerationChanged() { // agregar aceleracion
												// minima(freno largo)
	// float deltaX = Math.abs(xPreviousAccel - xAccel);
	// float deltaY = Math.abs(yPreviousAccel - yAccel);
	// float deltaZ = Math.abs(zPreviousAccel - zAccel);
	// return (deltaX > shakeThreshold && deltaY > shakeThreshold)
	// || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
	// || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
	// -------------
	// if(xAccel<-8.5){}
		double comp_accel;
		comp_accel = Math.sqrt(Math.pow(zAccel, 2) + Math.pow(yAccel, 2));// acceleracion
																			// paralela
																			// al
																			// suelo
		return (comp_accel > shakeThreshold);
	}

	private void executeShakeAction() {
		if (startedRecording == false) {
			stopFlag = true;
			stop_sensor();

			startedRecording = true;
			Intent intent = new Intent(Main.this, Recorder.class);
			startActivity(intent);
		}
	}

	// FB---------------------------------------------------------
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
		}
	}

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (state.isOpened()) {
				buttonsEnabled(true);
				Log.d("FacebookSampleActivity", "Facebook session opened");
			} else if (state.isClosed()) {
				buttonsEnabled(false);
				Log.d("FacebookSampleActivity", "Facebook session closed");
			}
		}
	};

	public void buttonsEnabled(boolean isEnabled) {
		postImageBtn.setEnabled(isEnabled);
		// updateStatusBtn.setEnabled(isEnabled);
	}

	public void postImage() {
		if (checkPermissions()) {
			// Bitmap img = BitmapFactory
			// .decodeFile("/sdcard/TestVideo/2014.05.27_13.27.24.3200000.png");
			Bitmap img = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
			Request uploadRequest = Request.newUploadPhotoRequest(
					Session.getActiveSession(), img, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							Toast.makeText(Main.this,
									"Photo uploaded successfully",
									Toast.LENGTH_LONG).show();
						}
					});
			uploadRequest.executeAsync();
		} else {
			requestPermissions();
		}
	}

	public void postStatusMessage() {
		if (checkPermissions()) {
			Request request = Request.newStatusUpdateRequest(
					Session.getActiveSession(), message,
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							if (response.getError() == null)
								Toast.makeText(Main.this,
										"Status updated successfully",
										Toast.LENGTH_LONG).show();
						}
					});
			request.executeAsync();
		} else {
			requestPermissions();
		}
	}

	public boolean checkPermissions() {
		Session s = Session.getActiveSession();
		if (s != null) {
			return s.getPermissions().contains("publish_actions");
		} else
			return false;
	}

	public void requestPermissions() {
		Session s = Session.getActiveSession();
		if (s != null)
			s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
					this, PERMISSIONS));
	}

	// FB---------------------------------------------------------
	// FB---------------------------------------------------------
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		uiHelper.onSaveInstanceState(savedState);
	}
	// FB---------------------------------------------------------
}