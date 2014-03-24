package com.example.testvideo;

import java.io.File;

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

public class MainActivity extends Activity implements OnClickListener {

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mRecordButton = (Button) findViewById(R.id.button1);
		mRecordButton.setOnClickListener(this);
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
		String videoName = String.valueOf(System.currentTimeMillis());
		
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



}
