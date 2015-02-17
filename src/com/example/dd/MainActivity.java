package com.example.dd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
	TextView speech;
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
	
	/** gender estimation algorithm */
	public static int getSpeech(double pitch) {
		// uncertain
		int speech = 0;	
		
		if (pitch > 100 && pitch < 500) {
			speech = 1;	
		}
		else {
			speech = 0;	
		}
		return speech;
	}
	
   
    public void setAlarms(Context context)
    {	    	
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast( this, 0, new Intent("com.example.filterMe"),0);
        int delay = 6000; //sec * milisec
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), delay, pi); // Millisec * Second * Samples+1
    }

    public void cancelAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast( this, 0, new Intent("com.example.filterMe"),0);
        alarmManager.cancel(pi);
        status.setText("Alarm Stopped");
        pitch.setText("");
        speech.setText("");
	    gender.setText("");
	    log.setText("");
    }
	
    public static int Median(ArrayList<Integer> to)
    {
        Collections.sort(to);
     
        if (to.size() % 2 == 1)
    	return to.get((to.size()+1)/2-1);
        else
        {
    	int lower = to.get(to.size()/2-1);
    	int upper = to.get(to.size()/2);
     
    	return (int) Math.round((lower + upper) / 2.0);
        }	
    }
  
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status=(TextView)findViewById(R.id.status);
        speech=(TextView)findViewById(R.id.speech);
        time=(Chronometer)findViewById(R.id.time);
        gender=(TextView)findViewById(R.id.gender);
        pitch=(TextView)findViewById(R.id.pitch);
        button=(CheckBox)findViewById(R.id.button);
        log=(TextView)findViewById(R.id.log);
        
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {     
            	status.setText("Alarm Stopped");
                pitch.setText("");
	    	    gender.setText("");
	    	    speech.setText("");
	    	    log.setText("");
	    		time.setBase(SystemClock.elapsedRealtime());
	    		time.start();
	    		log.setText("Recording...");
	  			testWavFile = testDir + "/" + FileProcess.newFileOnTime("wav");
	  			// start audio recording
	  			Bundle mbundle = new Bundle();
	  			mbundle.putString("audiopath", testWavFile);
	  			Log.i("Main", "start audio recording service");
	  			status.setText("Recording Audio");
	  			Intent recordIntent = new Intent(getApplicationContext(), AudioRecordService.class);
	  			recordIntent.putExtras(mbundle);
	  			date = Now.getDate();
	  			start = Now.getTimeOfDay();
	  			startService(recordIntent);
	  			time.setVisibility(View.VISIBLE);
	    	
	  			runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	                     final Handler handler = new Handler();
	                     handler.postDelayed(new Runnable() {
	                       @Override
	                       public void run() {
	                    	    String test="";
	                   			int mean=0;
	                    	   	Intent recordIntent = new Intent(getApplicationContext(), AudioRecordService.class);
	                    	   	status.setText("Analysing Audio");
	                    	   	stopService(recordIntent);
		       		  			time.stop();
		       		  			end = Now.getTimeOfDay();
		       		  			button.setClickable(false);
		       		  			// start speaker counting test
			       		  		Log.i("SpeakerCountTask", "Start YIN");
			       				int count=0;
			       				int size=0;
			       				int median=0;
			       				try {
			       					ArrayList<Float> to=Yin.writeFile(testWavFile);
			       					ArrayList<Integer> toint=new ArrayList<Integer>();
			       					Log.i("SpeakerCountTask", "Finish YIN");
			       					
			       					for(int i=0;i<to.size();i++){
			       						if(to.get(i)>0){
			       							count=count+Math.round((to.get(i)));
			       							toint.add(Math.round((to.get(i))));
			       							test+=((String.valueOf(Math.round((to.get(i)))))+" ");
			       							size=size+1;
			       							
			       						}
			       					}
			       					median=Median(toint);
			       					if(size!=0){
			       						mean=(Math.round(count)/size);
			       					}
			       				} catch (IOException e) {
			       					e.printStackTrace();
			       				} catch (Exception e) {
			       					e.printStackTrace();
			       				}
			       				
			       				int pitchval=median;
			       				pitch.setText(String.valueOf(pitchval));
			       		    	if(getSpeech(pitchval)==1){
			       		    		speech.setText("Yes");
			       		    		if(getGender(pitchval)==1){
			       			    		gender.setText("Male");
			       			    	}
			       			    	else{
			       			    		gender.setText("Female");
			       			    	}
			       		    	}
			       		    	else{
			       		    		speech.setText("No");
			       			    	gender.setText("-");
			       			    	
			       		    	}
			       		    	Log.i("SpeakCountTask", "Counting Done!");
			       		    	log.setText("Mean:"+String.valueOf(mean)+" Median:"+String.valueOf(median)+"\n"+test);  
			       		    	status.setText("Alarm Stopped");
		       		  			button.setClickable(true);	                     //add your code here
	                       }
	                     }, 3000);

	                }
	            });
            }
	   };
	   registerReceiver(br, new IntentFilter("com.example.filterMe") );
        
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
    		    	    setAlarms(getApplicationContext());
    		        }
    		    	else{
    		    		cancelAlarm(getApplicationContext());
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
