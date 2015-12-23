package com.example.dd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.*;
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


public class MainActivity extends Activity{

	TextView status;
	TextView thresholds;
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
		if (pitch < Constants.mu) {
			gender = 1;	
		}
		// female
		else if (pitch > Constants.fl) {
			gender = 2;	
		}
		return gender;
	}
	
	/** gender estimation algorithm */
	public static int getSpeech(double pitch) {
		// uncertain
		int speech = 0;	
		
		if (pitch > Constants.ml && pitch < Constants.fu) {
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
        int delay = 1000; //sec * milisec
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1000*Constants.duration*2+ delay, pi); // Millisec * Second * Samples+1
    }

    public void cancelAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast( this, 0, new Intent("com.example.filterMe"),0);
        alarmManager.cancel(pi);
        status.setText("Alarm Stopped");
        
    }
	
    public static int Median(ArrayList<Integer> to)
    {
        Collections.sort(to);
        if(to.size()==0){
        	return 0;
        }
        if (to.size() % 2 == 1)
    	return to.get((to.size()+1)/2-1);
        else
        {
    	int lower = to.get(to.size()/2-1);
    	int upper = to.get(to.size()/2);
     
    	return (int) Math.round((lower + upper) / 2.0);
        }	
    }
  
    private void record(String a) throws InterruptedException {
        Thread recordingThread = new RecorderThread(this, 1,a);
        recordingThread.start();
        recordingThread.join();
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status=(TextView)findViewById(R.id.status);
        thresholds=(TextView)findViewById(R.id.thresholds);
        speech=(TextView)findViewById(R.id.speech);
        time=(Chronometer)findViewById(R.id.time);
        gender=(TextView)findViewById(R.id.gender);
        pitch=(TextView)findViewById(R.id.pitch);
        button=(CheckBox)findViewById(R.id.button);
        log=(TextView)findViewById(R.id.log);
        showUserSettings();
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {  
            	
            	    	time.setBase(SystemClock.elapsedRealtime());
        	    		time.start();
        	    		log.setText("Recording...");
        	    		status.setText("Recording Audio");
            	 
	    		
	  			testWavFile = testDir + "/" + FileProcess.newFileOnTime("wav");	  			
	  			date = Now.getDate();
	  			start = Now.getTimeOfDay();

	  			time.setVisibility(View.VISIBLE);
	  			try {
					record(testWavFile);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	                    	    String test="";
	                   			int mean=0;
	                    	   	
	                    	   	status.setText("Analysing Audio");
	                    	   	
		       		  			time.stop();
		       		  			end = Now.getTimeOfDay();
		       		  			button.setClickable(false);
		       		  			// start speaker counting test
			       		  		Log.i("SpeakerCountTask", "Start YIN");
			       				int count=0;
			       				int countspeech=0;
			       				int size=0;
			       				int median=0;
			       				try {
			       					ArrayList<Float> to=Yin.writeFile(testWavFile);
			       					ArrayList<Integer> toint=new ArrayList<Integer>();
			       					Log.i("SpeakerCountTask", "Finish YIN");
			       					
			       					for(int j=0;j<to.size();j++){
			       						if(getSpeech(to.get(j))==1){
			       							countspeech=countspeech+1;
			       						}
			       						if(to.get(j)>0){
			       							count=count+Math.round((to.get(j)));
			       							toint.add(Math.round((to.get(j))));
			       							test+=((String.valueOf(Math.round((to.get(j)))))+" ");
			       							size=size+1;
			       							
			       						}
			       						else{
			       							toint.add(Math.round(0));
			       							test+=(" . ");
			       							
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
			       				
			       				
			       				int pitchval=mean;
			       				pitch.setText(String.valueOf(pitchval));
			       		    	if(getSpeech(pitchval)==1 && (countspeech/size)>Constants.percentagespeech){
			       		    		speech.setText("Yes");
			       		    		if(getGender(pitchval)==1){
			       			    		gender.setText("Male");
			       			    	}
			       			    	else if(getGender(pitchval)==2){
			       			    		gender.setText("Female");
			       			    	}
			       			    	else{
			       			    		gender.setText("Human");
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.settingsmenu:
            Intent i = new Intent(this, settings.class);
            startActivityForResult(i,1);
            break;
 
        }
 
        return true;
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case 1:
            showUserSettings();
            break;
 
        }
 
    }
 
    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        Constants.ml=sharedPrefs.getInt("lm", 100);
        Constants.mu=sharedPrefs.getInt("um", 160);
        Constants.fl=sharedPrefs.getInt("lf", 190);
        Constants.fu=sharedPrefs.getInt("uf", 600);
        Constants.percentagespeech=sharedPrefs.getInt("Percentage", 30)/100.00;
        Constants.duration=sharedPrefs.getInt("Duration", 2)/2;
        
        StringBuilder builder = new StringBuilder();
 
        builder.append("\n Lower Male: "
        		+ String.valueOf(Constants.ml));
 
        builder.append("\n Upper Male:"
                + String.valueOf(Constants.mu));
 
        builder.append("\n Lower Female: "
        		+ String.valueOf(Constants.fl));
        
        builder.append("\n Upper Female: "
        		+ String.valueOf(Constants.fu));
        
        builder.append("\n Duration: "
        		+ String.valueOf(Constants.duration*2));
        
        builder.append("\n Percentage%: "
        		+ String.valueOf(Constants.percentagespeech*100));
        thresholds.setText(builder.toString());
        
    }

}
