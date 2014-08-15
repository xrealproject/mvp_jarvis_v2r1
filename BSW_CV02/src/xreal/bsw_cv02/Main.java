package xreal.bsw_cv02;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

	File myFile2;
	FileOutputStream fOut2;
	OutputStreamWriter myOutWriter2;
	BufferedWriter myBufferedWriter2;
	PrintWriter myPrintWriter2;

	// private SensorManager sensorManager;
	private long currentTime;
	private long startTime;

	float[] acceleration = new float[3];
//	float[] rotationRate = new float[3];
//	float[] magneticField = new float[3];

	boolean stopFlag = false;
	boolean startFlag = false;
	boolean isFirstSet = true;

	private Button recorderbtn;// button for recorder
	private float xAccel;
	private float yAccel;
	private float zAccel;
	private float xPreviousAccel;
//	private float yPreviousAccel;
//	private float zPreviousAccel;

	private boolean firstUpdate = true;

	private final float shakeThreshold = 10.0f;// 0.9f;16.0f;//freno brusco;
												// 10.0f;//rompemuelle
	private boolean shakeInitiated = false;
	private SensorManager mySensorManager;
	boolean startedRecording = false;
	// FB---------------------------------------------------------
	private LoginButton loginBtn;
	private Button shareBtn;
	private TextView userName;
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String TAG = "Debug";
//	private static String message = "Sample status posted from Xreal android app";
	// FB---------------------------------------------------------
	// GPS GPS GPS---------------------------------------------------------
	private LocationManager locationMangaer = null;
	private LocationListener locationListener = null;
	// private Button btnGetLocation = null;
	// private EditText editLocation = null;
	// private ProgressBar pb = null;
	private Boolean flag = false;
	// data logger txt --------------------
	String file_name;
	Button gpsstop; 
	double initialmillis = 0;
	double currentMillis;

	// data logger txt ---------------------
	// GPS GPS GPS---------------------------------------------------------
	String resultado;
	public static String Gps_toPost;
	// tesseract----------------------------------------------------------
	Button OCRbutton;
	Button OCRbutton2;
	Button OCRbutton3;
	Bitmap inputFrame;
	Bitmap plate;
	TextView display;
	ImageView imageView1;
	String DATA_PATH = "/sdcard/Tesseract/";
	String lang = "eng";
	String recognizedText;
	String[] frames={ " ", " ", " ", " ", " " };
	EditText editText1;
	// tesseract------------------------------------------------------------
	String test_file;// = "/sdcard/TestVideo/placas/trainning set/2medio/" + editText1.toString() + ".png";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// tesseract---Oncreate-------------------------
				if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager.open("tessdata/" + lang
								+ ".traineddata");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/"
								+ lang + ".traineddata");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied " + lang + " traineddata");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy " + lang + " traineddata "
										+ e.toString());
					}
				}
				// tesseract----------------------------

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// start button sensor
		startButton = (Button) findViewById(R.id.sensorONButton);
		startButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// start recording the sensor data
				Calendar c = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy.MM.dd_HH.mm.ss");
				final String formattedDate = df.format(c.getTime());
				file_name = formattedDate;
				// GPS GPS GPS-----------------------------------------------
				flag = displayGpsStatus();
				if (flag) {
					Log.v(TAG, "onClick");
					// editLocation
					// .setText("Please!! move your device to see the changes in coordinates."
					// + "\nWait..");
					// pb.setVisibility(View.VISIBLE);
					locationListener = new MyLocationListener();

					locationMangaer.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 1000, 1,
							locationListener);
					Toast.makeText(getBaseContext(),
							"Start recording GPS data", Toast.LENGTH_SHORT)
							.show();
					startFlag = true;
				} else {
					alertbox("Gps Status!!", "Your GPS is: OFF");
				}
				// GPS GPS GPS-------------------------------------------------
				try {
					myFile = new File("/sdcard/TestVideo/" + file_name
							+ "sensor.txt");
					myFile.createNewFile();

					fOut = new FileOutputStream(myFile);
					myOutWriter = new OutputStreamWriter(fOut);
					myBufferedWriter = new BufferedWriter(myOutWriter);
					myPrintWriter = new PrintWriter(myBufferedWriter);
					Toast.makeText(getBaseContext(),
							"Start recording sensor data", Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
				} finally {
					// startFlag = true;
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
				// GPS GPS GPS--------------------------------------
				if (startFlag) {
					locationMangaer.removeUpdates(locationListener);
					// pb.setVisibility(View.INVISIBLE);
					Toast.makeText(getBaseContext(), "Stopped loggin GPS data",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getBaseContext(), "Not recording",
							Toast.LENGTH_SHORT).show();
				}
				// GPS GPS GPS-------------------------------------
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
				startActivityForResult(intent, 1);
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

		editText1 = (EditText) findViewById(R.id.editText1);
//		final String test_file;// = "/sdcard/TestVideo/placas/trainning set/2medio/" + editText1.toString() + ".png";//for testing 33.jpg
		shareBtn = (Button) findViewById(R.id.shareButton); //SHARE
		shareBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
//				Log.v(TAG, "click share button");
//				String[] frames = Cincoframes(resultado);
//				for (int i = 0; i < frames.length; i++) {
//					postImage(frames[i]);
//					Log.v(TAG, "upload frame " + frames[i] + i);
//				}
//				postStatusMessage(Gps_toPost);
//				 postImage("/sdcard/TestVideo/1placa.png");
//				String topublish = Gps_toPost + "Recognized Plate: " + recognizedText;
//				postImage(resultado + "_placa" , topublish);
				//for testing
				String topublish = Gps_toPost + "Recognized Plate: " + "LCY-675";
				postImage("/sdcard/TestVideo/null_placaxxx.png" , topublish);
//				postStatusMessage(recognizedText);
			}
		});
		// FB---------------------------------------------------------
		// OCR--------------------------------------------------------
		display = (TextView) findViewById(R.id.textView_ocr);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		OCRbutton = (Button) findViewById(R.id.ocr); //OCR
		OCRbutton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
//				Log.v(TAG, "click ocr button");
//				String[] frames = Cincoframes(resultado);
//				plate = OCR_FUNCTION(frames[3]);
				//for testing
				test_file = "/sdcard/TestVideo/placas/trainning set/2medio/" + editText1.getText().toString() + ".jpg";
				Log.v(TAG, "test file: " + test_file);
				plate = OCR_FUNCTION(test_file);
			}
		});
		OCRbutton2 = (Button) findViewById(R.id.ocr2); //OCR2
		OCRbutton2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
//				Log.v(TAG, "click ocr button");
//				String[] frames = Cincoframes(resultado);
//				OCR_FUNCTION2(frames[2]);
				//for testing
				OCR_FUNCTION2(plate);
			}
		});
		OCRbutton3 = (Button) findViewById(R.id.ocr3); // procesa solo placa
		OCRbutton3.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
//				Log.v(TAG, "click ocr button");
//				String[] frames = Cincoframes(resultado);
//				OCR_FUNCTION2(frames[2]);
				//for testing
				plate = OCR_FUNCTION3(plate);
			}
		});
		// OCR---------------------------------------------------------
		// GPS GPS GPS--------------------------------------
		locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// GPS GPS GPS-------------------------------------
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
			// myOutWriter2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fOut.close();
			// fOut2.close();
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
		// OpenCV----------------------------
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
		// OpenCV----------------------------
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
//			yPreviousAccel = yNewAccel;
//			zPreviousAccel = zNewAccel;
			firstUpdate = false;
		} else {
			xPreviousAccel = xAccel;
//			yPreviousAccel = yAccel;
//			zPreviousAccel = zAccel;
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
		double comp_accel = 0;

		if (Math.abs(xPreviousAccel) > 9.45f && Math.abs(xAccel) < 10.15f) {// g*sin(15)=9.45
			comp_accel = Math.sqrt(Math.pow(zAccel, 2) + Math.pow(yAccel, 2));// paralela
																				// al
																				// suelo
		}
		return (comp_accel > shakeThreshold);
	}

	private void executeShakeAction() {
		if (startedRecording == false) {
			stopFlag = true;
			stop_sensor();

			startedRecording = true;
			Intent intent = new Intent(Main.this, Recorder.class);
			startActivityForResult(intent, 1);
		}
	}

	// FB---------------------------------------------------------
	private void onSessionStateChange(Session session, SessionState state,Exception exception) {
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
		shareBtn.setEnabled(isEnabled);
		// updateStatusBtn.setEnabled(isEnabled);
	}

	public String[] Cincoframes(String result) {
		Log.v(TAG, "inicia 5 frames");
		String[] frames_path = { " ", " ", " ", " ", " " };// inicializacion
															// correcta
		for (int i = 0; i < 5; i++) {
			long usecs = (i + 1) * 800000;
			frames_path[i] = result + "." + usecs + ".png";
			Log.v(TAG, "armado " + i + " " + result + "." + usecs + ".png"+ " " + frames_path[i]);
		}
		return frames_path;
	}

	public void postImage(String frame, String description) {
		if (checkPermissions()) {
			// Bitmap img =
			// BitmapFactory.decodeFile("/sdcard/TestVideo/2014.05.27_13.27.24.3200000.png");
			// Bitmap img = BitmapFactory.decodeResource(getResources(),
			// R.drawable.ic_launcher);
			Bitmap img = BitmapFactory.decodeFile(frame);
			Request uploadRequest = Request.newUploadPhotoRequest(Session.getActiveSession(), img, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							Toast.makeText(Main.this,
									"Photo uploaded successfully",
									Toast.LENGTH_LONG).show();
						}
					});
			
			Bundle params = uploadRequest.getParameters();
	        params.putString("name", description);
			uploadRequest.executeAsync();
		} else {
			requestPermissions();
		}
	}

//	public void postStatusMessage(String status_post) {
////		status_post = message;
//		if (checkPermissions()) {
//			Request request = Request.newStatusUpdateRequest(
//					Session.getActiveSession(), status_post,
//					new Request.Callback() {
//						@Override
//						public void onCompleted(Response response) {
//							if (response.getError() == null)
//								Toast.makeText(Main.this,
//										"Status updated successfully",
//										Toast.LENGTH_LONG).show();
//						}
//					});
//			request.executeAsync();
//		} else {
//			requestPermissions();
//		}
//	}

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
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				resultado = result;
				Log.v(TAG, "resultado obtenido " + resultado);
				Log.v(TAG, "Gpsto post public static: " + Gps_toPost);
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		uiHelper.onSaveInstanceState(savedState);
	}
	// FB---------------------------------------------------------
	// GPS GPS GPS-----------------------------------------------------------------------------
	/*----------Method to Check GPS is enable or disable ------------- */
	private Boolean displayGpsStatus() {
		ContentResolver contentResolver = getBaseContext().getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(
				contentResolver, LocationManager.GPS_PROVIDER);
		if (gpsStatus) {
			return true;
		} else {
			return false;
		}
	}
	/*----------Method to create an AlertBox ------------- */
	protected void alertbox(String title, String mymessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your Device's GPS is Disable")
				.setCancelable(false)
				.setTitle("** Gps Status **")
				.setPositiveButton("Gps On",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// finish the current activity
								// AlertBoxAdvance.this.finish();
								Intent myIntent = new Intent(
										Settings.ACTION_SECURITY_SETTINGS);
								startActivity(myIntent);
								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// cancel the dialog box
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	/*----------Listener class to get coordinates ------------- */
	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			// data logger txt ---------------
			currentMillis = System.currentTimeMillis() - initialmillis;
			// data logger txt ---------------
			// editLocation.setText("");
			// pb.setVisibility(View.INVISIBLE);
			Toast.makeText(getBaseContext(),"Location changed : Lat: " + loc.getLatitude() + " Lng: "+ loc.getLongitude(), Toast.LENGTH_SHORT).show();
			String longitude = " " + loc.getLongitude();
			Log.v(TAG, longitude);
			String latitude = " " + loc.getLatitude();
			Log.v(TAG, latitude);
			String speed = " " + loc.getSpeed();// speed m/s
			String timesstamp = " " + currentMillis;
			
//			List<Address> addresses = getAddress(loc.getLatitude(), loc.getLongitude());
//			Log.v(TAG, addresses.get(0).getAddressLine(2));// country
			Log.v(TAG, "to get addres!");
			String address = getAddress2(loc.getLatitude(), loc.getLongitude());
			Gps_toPost="tstamp: "+ timesstamp + "\n"+ "long: "+  longitude +"\n"+"lat: "+ latitude +"\n"+"speed: "+ speed +"\n"+ "Address: " + address;
			// String s = longitude + "\n" + latitude;
			// editLocation.setText(s);
			// data logger txt ---------------
			writeLog(file_name, timesstamp + longitude + latitude + speed);
			// data logger txt ---------------
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	
		public List<Address> getAddress(double latitude, double longitude) {
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getBaseContext(),Locale.getDefault());
                if (latitude != 0 || longitude != 0) {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getAddressLine(1);
                    String country = addresses.get(0).getAddressLine(2);
//                Log.d("TAG", "address = "+address+", city ="+city+", country = "+country );
                    Log.v("TAG", "address = "+address+", city ="+city+", country = "+country );
                    return addresses;
                } else {
                    Toast.makeText(getBaseContext(), "latitude and longitude are null",
                            Toast.LENGTH_LONG).show();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
		
		public String getAddress2(double latitude, double longitude) {
			Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
			String add = "";
			try {
				List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
//				String add = "";
				if (addresses.size() > 0){
					for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
						add += addresses.get(0).getAddressLine(i) + "\n";
				}
				Gps_toPost = Gps_toPost + "\n" + add;
				Toast.makeText(getBaseContext(), add,Toast.LENGTH_LONG).show();
				Log.v(TAG, "to get addres! : " + add);
			}
			catch (IOException e1) {                
				e1.printStackTrace();
			}
			return add;
		}
	}

	// data logger txt ---------------
	private void writeLog(String file, String log) {
		Log.d("GPSLoggerService", "writeLog:" + log);

		File f = new File(Environment.getExternalStorageDirectory()
				+ "/TestVideo/" + file + "GPS.txt");
		try {
			FileWriter filewriter = new FileWriter(f, true);
			filewriter.write(log + "\n");
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// data logger txt ---------------
	// GPS GPS GPS---------------------------------------------------------<<<<
	// tesseract----------------------------
	public String Tesseract_function(String image_path) { // probada no tocar
		TessBaseAPI baseApi = new TessBaseAPI();
		// DATA_PATH = Path to the storage
		// lang = for which the language data exists, usually "eng"
		baseApi.init(DATA_PATH, lang);
		// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata",
		// "eng");
		Bitmap inputTess = BitmapFactory.decodeFile(image_path);
		baseApi.setImage(inputTess);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		// display.setText(Integer.toString(cannybmp.getPixel(100,120)));//show
		// value of a pixel
		// display.setText(recognizedText);
		return recognizedText;
	}
	
	public String Tesseract_function2(Bitmap toTesseract) { // probada no tocar
		TessBaseAPI baseApi = new TessBaseAPI();
		// DATA_PATH = Path to the storage
		// lang = for which the language data exists, usually "eng"
		baseApi.init(DATA_PATH, lang);
		// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata",
		// "eng");
//		Bitmap inputTess = BitmapFactory.decodeFile(image_path);
		baseApi.setImage(toTesseract);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		// display.setText(Integer.toString(cannybmp.getPixel(100,120)));//show
		// value of a pixel
		// display.setText(recognizedText);
		return recognizedText;
	}
	// tesseract----------------------------
	// OpenCV-----------------------------------------------------------
	// ---AsyncInitialization----oPENcv------------------------------------------------
	// private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {

				Log.i(TAG, "OpenCV loaded successfully");
				Log.v(TAG, "resultado obtenido " + resultado);
				// mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	// OpenCV----------------------------------------------------------
//	public void OCR_FUNCTION(String imagePath) { //funciona con 33
//		Mat outputMat = toGrayMat(imagePath);
//		// outputMat = thresholdMat(outputMat);
//		Mat threshMat = thresholdMat(outputMat);
//		outputMat = cannyMat(threshMat);
//		dilateMat(outputMat, 5.0f);
//		//dibujar contorno encontrado de cuadrilatero
//		List<MatOfPoint> cuadcontour = detectar_cuadrilateros1(outputMat);
//		outputMat = drawCont2(outputMat, cuadcontour);
//		// salidas
//		Mat clipped_plate = clipping2(threshMat, outputMat);
//		showonImageView(clipped_plate);
//		String plate_name = resultado + "_placa";
//		saveMat(clipped_plate, plate_name);
//		// FINAL STEP-------------Tesseract
//		recognizedText = Tesseract_function("/sdcard/TestVideo/"+plate_name+".png");// threshold_clipped2.png
//		Log.v(TAG, "TEXTO obtenido: " + recognizedText);
//		display.setText(recognizedText);
//	}
	
	public Bitmap OCR_FUNCTION(String imagePath) {// Log.v(TAG, "column");
		Mat grayMat = toGrayMat(imagePath);
		Mat equalMat = histeq2(grayMat);
		Mat toBlur = blurMat(equalMat);
		Mat threshMat = thresholdMat(toBlur);
		Mat cannieMat = cannyMat(threshMat);
		Mat dilateM = dilateMat(cannieMat, 1.1f);
//		Mat morphClose = morphologicalMat(dilateM);
//		 dibujar contorno encontrado de cuadrilatero
		List<MatOfPoint> cuadcontour = detectar_cuadrilateros1(dilateM);
		Mat outputMat = drawCont2(dilateM, cuadcontour);//dibuja sobre lo qe no deberia
		Mat clipp = clipping2(threshMat, outputMat);
		if(cuadcontour.size()==0){
			display.setText("Plate not Found");
			return null;
		}
		
		else{
		Bitmap zoomed = zoomMat(clipp, cuadcontour.get(0),1);
		
//		showonImageView(clipp);
		String plate_name = resultado + "_placa";
//		saveMat(clipp, plate_name);
		imageView1.setImageBitmap(zoomed);
		saveBitmap(zoomed, plate_name);
		// FINAL STEP-------------Tesseract
//		recognizedText = Tesseract_function2("/sdcard/TestVideo/"+plate_name+".png",zoomed);// threshold_clipped2.png
//		Log.v(TAG, "TEXTO obtenido: " + recognizedText);
//		display.setText(recognizedText);
		return zoomed;
		}
	}
	
	public Bitmap OCR_FUNCTION3(Bitmap zoomed) {
		Mat matin = new Mat(zoomed.getWidth(), zoomed.getHeight(),CvType.CV_8UC1);
		Utils.bitmapToMat(zoomed, matin);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat mHierarchy = new Mat();
		Imgproc.findContours(matin, contours, mHierarchy,Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
		Mat outputMat = drawCont2(matin, contours);
		Utils.matToBitmap(outputMat, zoomed);
		imageView1.setImageBitmap(zoomed);
		return zoomed;
	}
	
	public void OCR_FUNCTION2(Bitmap zoomed) {// Log.v(TAG, "column");
		// FINAL STEP-------------Tesseract
		if(zoomed != null){
			recognizedText = Tesseract_function2(zoomed);// threshold_clipped2.png
			Log.v(TAG, "TEXTO obtenido: " + recognizedText);
			display.setText(recognizedText);
		}
		else{
			recognizedText= "No plate recognized";
		}
	}
	public Mat blurMat(Mat GrayMat) {
		Mat toBlur = new Mat();
		org.opencv.core.Size s = new Size(5, 5);
		Imgproc.GaussianBlur(GrayMat, toBlur, s, 1);
		return toBlur;
	}
	public Bitmap zoomMat(Mat in, MatOfPoint cuad1, int Scalefactor){//obtiene maximos y minimos
//		Mat out = new Mat();
		List<Point> list1 = cuad1.toList();
		int rowStart=0;
		int rowEnd=0;
		int colStart=0;
		int colEnd=0;
		int xa,ya;
		for(int k=0;k<list1.size();k++){
			xa=(int) list1.get(k).x;
			ya=(int) list1.get(k).y;
			Log.v(TAG,"punto "+k+": "+ xa + " " + ya);
			if(k==0){
				rowStart=ya;
				rowEnd=ya;
				colStart=xa;
				colEnd=xa;}
			else{
				if(xa>colEnd){Log.v(TAG, "-new col end: " +colEnd+" "+xa);colEnd=xa;}
				if(xa<colStart){Log.v(TAG, "-new col start: " +colStart+" "+xa);colStart=xa;}
				if(ya>rowEnd){Log.v(TAG, "-new row end: " +rowEnd+" "+ya);rowEnd=ya;}
				if(ya<rowStart){Log.v(TAG, "-new row start: " +rowStart+" "+ya);rowStart=ya;}
			}
		}

		Utils.matToBitmap(in, inputFrame);
		Bitmap outbitmap = Bitmap.createBitmap(inputFrame, colStart, rowStart, colEnd-colStart, rowEnd-rowStart);
		Bitmap scaledoutbitmap = Bitmap.createScaledBitmap(outbitmap, outbitmap.getWidth()*Scalefactor, outbitmap.getHeight()*Scalefactor, false);
//		showOutput.setImageBitmap(scaledoutbitmap);
		return scaledoutbitmap;
	}

	public Mat toGrayMat(String imagePath) { // checkout local branch and merge
												// remote branch
		inputFrame = BitmapFactory.decodeFile(imagePath);
		Mat imageMat = new Mat(inputFrame.getWidth(), inputFrame.getHeight(),
				CvType.CV_8UC1);
		Utils.bitmapToMat(inputFrame, imageMat);
		Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
		return imageMat;
	}

	public Mat thresholdMat(Mat GrayMat) { // checkout local branch and merge
		// remote branch
		Mat outputMat = GrayMat.clone();
		// ------------------
		Imgproc.threshold(GrayMat, outputMat, 50, 255, Imgproc.THRESH_BINARY_INV
				| Imgproc.THRESH_OTSU);
		// ------------------
		Core.convertScaleAbs(outputMat, outputMat, 50, 0);
		return outputMat;
	}

	public Mat cannyMat(Mat image) {
		Mat canny_img = new Mat();
		Core.convertScaleAbs(image, canny_img, 10, 0);
		Imgproc.Canny(image, canny_img, 66, 90);
		return canny_img;
	}

	public Mat dilateMat(Mat cannyMat, float dilatation_factor) {
		float dilation_size = dilatation_factor;
		Point point = new Point(dilation_size, dilation_size);
		// Imgproc.dilate(cannyMat, cannyMat, kernel, anchor,
		// iterations);dilate(cannyMat, cannyMat, Mat(), point);
		org.opencv.core.Size s = new Size(2 * dilation_size + 1,
				2 * dilation_size + 1);
		Mat element = Imgproc.getStructuringElement(2, s, point); // dilation_type
																	// =
																	// MORPH_ELLIPSE
		Imgproc.dilate(cannyMat, cannyMat, element);
		return cannyMat;
	}

	public List<MatOfPoint> detectar_cuadrilateros1(Mat canniedMat) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat mHierarchy = new Mat();
		Imgproc.findContours(canniedMat, contours, mHierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		MatOfPoint2f approx2f = new MatOfPoint2f();
		// Mat dst = canniedMat.clone();
		List<MatOfPoint2f> contours2f = ListMatofPoint2f(contours);
		int count = 0;
		List<MatOfPoint> contour_cuadrilateros = new ArrayList<MatOfPoint>();
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.approxPolyDP(contours2f.get(i), approx2f,
					Imgproc.arcLength(contours2f.get(i), true) * 0.045, true);// 0.07
			MatOfPoint approx = new MatOfPoint(approx2f.toArray());
			// if((Imgproc.contourArea(contours.get(i)))> 5.0 &&//
			// (Imgproc.contourArea(contours.get(i))) <200 &&//
			// Imgproc.isContourConvex(approx))
			if ((Imgproc.contourArea(contours.get(i))) > 1500.0
					&& Imgproc.isContourConvex(approx)) {// 1000<
				if (approx.toList().size() >= 4) {
					count++;
					Log.v(TAG, "Cuadrilatero encontrado: " + count + " "
							+ Imgproc.contourArea(contours.get(i)));
					contour_cuadrilateros.add(approx);
				}
			}
		}
		return contour_cuadrilateros;
	}

	private List<MatOfPoint2f> ListMatofPoint2f(List<MatOfPoint> toConvert) {
		List<MatOfPoint2f> matofPoint2f = new ArrayList<MatOfPoint2f>();
		Iterator<MatOfPoint> itr = toConvert.iterator();
		while (itr.hasNext()) {
			MatOfPoint SrcMtx = itr.next();
			MatOfPoint2f NewMtx = new MatOfPoint2f(SrcMtx.toArray());
			matofPoint2f.add(NewMtx);
		}
		return matofPoint2f;
	}

	public Mat drawCont2(Mat image, List<MatOfPoint> contours) { // fill de
																	// contours
		Mat drawing = Mat.zeros(image.size(), CvType.CV_8UC1);
		Scalar color = new Scalar(255, 0, 0);
		Mat hierarchy = new Mat();
		Point point = new Point(0, 0);
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.drawContours(drawing, contours, i, color, -1, 8, hierarchy,
					0, point);
		}
		return drawing;
	}

	public Mat clipping2(Mat thresholdMat, Mat mask) {
		Mat clippedMat = new Mat();
		Core.multiply(mask, thresholdMat, clippedMat);

		return clippedMat;
	}

	void showonImageView(Mat output) {
		Utils.matToBitmap(output, inputFrame);
		imageView1.setImageBitmap(inputFrame);
	}

	void saveMat(Mat imagetosave, String namepng) {
		Utils.matToBitmap(imagetosave, inputFrame);
		File file = new File("/sdcard/TestVideo/" + namepng + ".png");
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			inputFrame.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void saveBitmap(Bitmap imagetosave, String namepng) {
		File file = new File("/sdcard/TestVideo/" + namepng + ".png");
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			imagetosave.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Mat histeq2(Mat in) { //equalizacion histograma
		Mat out = new Mat(in.size(), in.type());
		Imgproc.equalizeHist(in, out);
		return out;
	}
	// OpenCV-----------------------------------------------------------
}