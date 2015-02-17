package com.example.dd;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

/**
 * Author: Mayank Gautam and Sarthak Ahuja
 */
public class RecorderThread extends Thread {

    Context mContext;
    int mNumSamples;
    String fn;
    
    RecorderThread(Context context, int numSamples,String filename)
    {
    	fn=filename;
        mContext = context;
        mNumSamples=numSamples;
    }

    @Override
    public void run() {
        try {
            recordAudio();
        } catch (IOException e) {
            Log.e("Recorder",e.getMessage());
        } catch (WavFileException e) {
        	Log.e("Recorder",e.getMessage());
        }
    }

    public void recordAudio() throws IOException, WavFileException {
            AudioRecorder audioRecorder = new AudioRecorder(mNumSamples,fn);
            //Log.i("RecorderActivity", "started recording");
            audioRecorder.record();
            //Log.i("RecorderActivity", "finished recording");
    }

}
