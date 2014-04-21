package xreal.BSW_CV02;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main extends Activity 
{
	private Button recorderbtn;//button for recorder
//	private float xAccel;
//	private float yAccel;
//	private float zAccel;
//	private float xPreviousAccel;
//	private float yPreviousAccel;
//	private float zPreviousAccel;
//
//	private boolean firstUpdate = true;
//	private final float shakeThreshold = 0.9f;
//	private boolean shakeInitiated = false;	
//	private SensorManager mySensorManager;
	boolean startedRecording=false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		recorderbtn = (Button)findViewById(R.id.recorder);
		//start the VideoRecorder with accelerometer
//		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); 
//		mySensorManager.registerListener(mySensorEventListener, mySensorManager
//				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//				SensorManager.SENSOR_DELAY_NORMAL);


		//start the VideoRecorder on the click of the button 
	   recorderbtn.setOnClickListener(new View.OnClickListener(){
		  	public void onClick(View view){
		  		startedRecording=true;
		  		Intent intent = new Intent(Main.this, Recorder.class);
		  		startActivity(intent);
		  	  }
	       });	
}
}