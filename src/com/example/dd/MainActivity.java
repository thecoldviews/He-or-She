package com.example.dd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import edu.rutgers.winlab.crowdpp.util.Constants;
import edu.rutgers.winlab.crowdpp.util.FileProcess;
import edu.rutgers.winlab.crowdpp.util.Now;


public class MainActivity extends ActionBarActivity{

	TextView status;
	TextView pitch;
	TextView gender;
	TextView log;
	TextView speaker;
	Chronometer time;
	CheckBox button;
	
	private File crowdppDir, testDir; 	
	
	private long sys_time;
	private String date, start, end;
	private int speaker_count; 
	
	// default values when the data is not available
	private double percentage = -1;
	private double latitude = -1;
	private double longitude = -1;	
	
	private String test_log;
	
	public static String calWavFile;
	private String testWavFile;
	
	/** gender estimation algorithm */
	public static int getGender(double pitch) {
		// uncertain
		int gender = 0;	
		// male		
		if (pitch < 160) {
			gender = 1;	
		}
		// female
		else if (pitch > 190) {
			gender = 2;	
		}
		return gender;
	}
	
	/** Speaker counting task. */
	class Test extends AsyncTask<String, String, Integer> {
		String test="lol";
		@Override
		protected Integer doInBackground(String... arg0) {
			Log.i("SpeakerCountTask", "Start YIN");
			speaker_count=0;
			int size=0;
			try {
				ArrayList<Float> to=Yin.writeFile(testWavFile);
				Log.i("SpeakerCountTask", "Finish YIN");
				for(int i=0;i<to.size();i++){
					if(to.get(i)>0){
						speaker_count=speaker_count+Math.round((to.get(i)));
						test+=((String.valueOf(Math.round((to.get(i)))))+" ");
						size=size+1;
					}
				}
				speaker_count=speaker_count/size;
		
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			return speaker_count;
		}
		
	    @Override
	    protected void onProgressUpdate(String... values) {
	    		log.setText("Counting...");
				Log.i("SpeakCountTask", "Counting");
	    }
	
	    @Override
	    protected void onCancelled(Integer result) {
				Log.i("SpeakCountTask", "Cancelled");
	    }
	      
	    @Override
	    protected void onPreExecute() {
				Log.i("SpeakCountTask", "Begin to count");
	    }
	
	    @Override
	    protected void onPostExecute(Integer result) {
	    	pitch.setText(String.valueOf(result));
	    	if(getGender(result)==1){
	    		gender.setText("Male");
	    	}
	    	else{
	    		gender.setText("Female");
	    	}
	    	
	    	Log.i("SpeakCountTask", "Counting Done!");
	    	log.setText(test);    	
	    }
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status=(TextView)findViewById(R.id.status);
        time=(Chronometer)findViewById(R.id.time);
        gender=(TextView)findViewById(R.id.gender);
        pitch=(TextView)findViewById(R.id.pitch);
        button=(CheckBox)findViewById(R.id.button);
        log=(TextView)findViewById(R.id.log);
        
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getApplicationContext(), "Can not find SD card ...", Toast.LENGTH_SHORT).show();			
			finish();
		}

		crowdppDir = new File(Constants.crowdppPath);
		if (!crowdppDir.exists() || !crowdppDir.isDirectory()) {
			crowdppDir.mkdir();
		} 
        
        testDir = new File(Constants.testPath);
		if (!testDir.exists() || !testDir.isDirectory()) {
			Log.e("lol","Created Folder");
			testDir.mkdir();
		}	
		else{
			Log.e("lol","Not Folder");
		}
        
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView,
        			boolean isChecked) {
        		// TODO Auto-generated method stub
        		
    		    	if (isChecked) {
    		    	    pitch.setText("");
    		    	    gender.setText("");
    		    	    log.setText("");
    		    		time.setBase(SystemClock.elapsedRealtime());
    		    		time.start();
    		    		log.setText("Recording...");
    		  			testWavFile = testDir + "/" + FileProcess.newFileOnTime("wav");
    		  			// start audio recording
    		  			Bundle mbundle = new Bundle();
    		  			mbundle.putString("audiopath", testWavFile);
    		  			Log.i("Main", "start audio recording service");
    		  			Intent recordIntent = new Intent(getApplicationContext(), AudioRecordService.class);
    		  			recordIntent.putExtras(mbundle);
    		  			date = Now.getDate();
    		  			start = Now.getTimeOfDay();
    		  			startService(recordIntent);
    		  			time.setVisibility(View.VISIBLE);
    		    	} 
    		    	else {	
    		  			// stop audio recording
    		  			Intent recordIntent = new Intent(getApplicationContext(), AudioRecordService.class);
    		  			stopService(recordIntent);
    		  			time.stop();
    		  			end = Now.getTimeOfDay();
    		  			button.setClickable(false);
    		  			// start speaker counting test
    		  			new Test().execute();
    		  			button.setClickable(true);		  			
    		      }
        	}
        });
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

}
