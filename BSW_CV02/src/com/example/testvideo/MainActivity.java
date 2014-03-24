package com.example.testvideo;

import java.io.File;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener, SensorEventListener {

	private static final int REQ_CODE_TAKE_VIDEO = 9999;
	public native String helloLog();
	private Uri mUri;
	private Button mRecordButton;
	public static final String TEST_VIDEO_FILES_ROOT_PATH = "/TestVideo";
	public static final String TEST_VIDEO_VIDEOS_PATH = "/TestVideo Videos";
	public static final String EXTENSION_MP4 = ".mp4";
	static {
		System.loadLibrary("testvideo");
	}
	
    EditText txtData;
    Button startButton;
    Button stopButton;

    File myFile;
    FileOutputStream fOut;
    OutputStreamWriter myOutWriter;
    BufferedWriter myBufferedWriter;
    PrintWriter myPrintWriter;


    private SensorManager sensorManager;
    private long currentTime;
    private long startTime;

    float[] acceleration = new float[3];
    float[] rotationRate = new float[3];
    float[] magneticField = new float[3];

    boolean stopFlag = false;
    boolean startFlag = false;
    boolean isFirstSet = true;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mRecordButton = (Button) findViewById(R.id.button1);
		mRecordButton.setOnClickListener(this);
	
        // file name to be entered
        
        // start button
        startButton = (Button) findViewById(R.id.button3);
        startButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // start recording the sensor data
                try {
                	String path_sensores="/sdcard/ResearchData/" ;
                	File path_registro = new File(path_sensores);
                	path_registro.mkdirs();
                	
        			Calendar c = Calendar.getInstance();
        	        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        	        final String formattedDate = df.format(c.getTime());
                	
            		String sensores_name =  formattedDate;
            				//String.valueOf(System.currentTimeMillis());
                	
                	myFile = new File(path_sensores + sensores_name + ".txt");		//txtData.getText()
                    myFile.createNewFile();
                    												// crear folders contenedores

                    fOut = new FileOutputStream(myFile);
                    myOutWriter = new OutputStreamWriter(fOut);
                    myBufferedWriter = new BufferedWriter(myOutWriter);
                    myPrintWriter = new PrintWriter(myBufferedWriter);

                    Toast.makeText(getBaseContext(), "Start recording the data", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    startFlag = true;
                }
            }
        });

        // stop button
        stopButton = (Button) findViewById(R.id.button4);
        stopButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // stop recording the sensor data
                try {
                    stopFlag = true;
                    Toast.makeText(getBaseContext(), "Done recording the data set", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    
	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.button1) {
			recordVideo();
		}
	}
	
	public void onMyButtonClick(View view)
	{
		//show C++ code when press button
		TextView textView = new TextView(this);
		textView.setText(helloLog());
		setContentView(textView);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
		super.onActivityResult(requestCode, resultCode, dataIntent);

		switch (requestCode) {
		case REQ_CODE_TAKE_VIDEO:
			if (resultCode == RESULT_OK) {
				Uri selectedVideo = mUri;
				
				if (selectedVideo != null) {
					Log.d("TEST_VIDEO", "URI: " + selectedVideo);
					String filePath = selectedVideo.toString();
					refreshVideoGallery(MainActivity.this, filePath);
					Toast.makeText(MainActivity.this, "Video saved, filepath: " + filePath, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(MainActivity.this, "Error saving video", Toast.LENGTH_LONG).show();
				}			
			}
			break;	
		}
	}
	
	private void recordVideo() {
		
		Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        final String formattedDate = df.format(c.getTime());
    			
		String videoName = formattedDate; 
				//String.valueOf(System.currentTimeMillis());
		
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		String videoLocation = "";
		if (Environment.getExternalStorageDirectory() != null) {
    		videoLocation = Environment.getExternalStorageDirectory() + TEST_VIDEO_FILES_ROOT_PATH + TEST_VIDEO_VIDEOS_PATH;
			mUri = Uri.fromFile(new File(videoLocation, videoName + EXTENSION_MP4));
		} else {
			videoLocation = Environment.getDataDirectory() + TEST_VIDEO_FILES_ROOT_PATH + TEST_VIDEO_VIDEOS_PATH;
    		mUri = Uri.fromFile(new File(videoLocation, videoName + EXTENSION_MP4));
		}
		
        final File sddir = new File(videoLocation);
        if (!sddir.exists()) {
            sddir.mkdirs();
        }
        
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, REQ_CODE_TAKE_VIDEO);
		
	}
	
	public void refreshVideoGallery(Context context, String filePath) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.parse(filePath));
		context.sendBroadcast(intent);
		
	}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (startFlag) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acceleration[0] = event.values[0];
                acceleration[1] = event.values[1];
                acceleration[2] = event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                rotationRate[0] = event.values[0];
                rotationRate[1] = event.values[1];
                rotationRate[2] = event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticField[0] = event.values[0];
                magneticField[1] = event.values[1];
                magneticField[2] = event.values[2];
            }

            if (isFirstSet) {
                startTime = System.currentTimeMillis();
                isFirstSet = false;
            }

            currentTime = System.currentTimeMillis();

            for (int i = 0;i<1; i++) {
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
        }
    }

    private void save() {

            myPrintWriter.write(currentTime - startTime + " " + acceleration[0] + " " + acceleration[1] + " " + acceleration[2]
                        + " " + rotationRate[0] + " " + rotationRate[1] + " " + rotationRate[2] 
                        + " " + magneticField[0] + " " + magneticField[1] + " " + magneticField[2] + "\n");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the sensors
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

}