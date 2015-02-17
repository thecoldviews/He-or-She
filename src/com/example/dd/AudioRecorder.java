package com.example.dd;

import de.fau.cs.jstk.util.Constants;
import edu.rutgers.winlab.crowdpp.util.FileProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;
import android.widget.TextView;


public class AudioRecorder{

	private String TagActivity = "AudioRecorder";
    private int bufferSize = 2048;
    private AudioRecord recorder = null;
    NoiseSuppressor ns=null;
    private byte[] buffer;
    private byte[] original;
    private int audioLength;
    private String filename;

    private boolean isRecording=false;
    public AudioRecorder(int numSamples,String fn) {
    	filename=fn;
    	original=new byte[0];
        audioLength = numSamples* Constants.RECORDER_TIME*Constants.RECORDING_SAMPLE_RATE;
        
        bufferSize = AudioRecord.getMinBufferSize(Constants.RECORDING_SAMPLE_RATE,Constants.RECORDER_CHANNELS,Constants.RECORDER_AUDIO_ENCODING);
    }

    /**
     *
     */
    public void record() throws IOException{
        startRecording();
    }

    /**
     * return the original filename
     */
    private String getFilename() throws IOException {
        return filename;
    }

    public static String getAudioRecorderTempFile() throws IOException {
        return FileProcess.getSdPath()+"/DD/record_temp";
    }
    
    /**
     * returns the temp filename
     */
    private String getTempFilename() throws IOException {
        return getAudioRecorderTempFile();
    }

    /**
     * on clicking START recording will be started and audio Data is written to the
     * original file. DataToFile is done in a separate threads
     */
    @SuppressWarnings("unused")
	private void startRecording() throws IOException{

        int recBuffSize = AudioRecord.getMinBufferSize(Constants.RECORDING_SAMPLE_RATE,Constants.RECORDER_CHANNELS,Constants.RECORDER_AUDIO_ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                Constants.RECORDING_SAMPLE_RATE, Constants.RECORDER_CHANNELS,Constants.RECORDER_AUDIO_ENCODING, recBuffSize);
        
        buffer = new byte[bufferSize];
        recorder.startRecording();
        isRecording = true;
        writeAudioDataToFile();
        
    }

    /**
     * on clicking STOP the recorder is set to null
     * and also the temp file are saved to original file and
     * delete the temp files
     */
    private void stopRecording() throws IOException {
        if(null != recorder){
            isRecording = false;
            recorder.stop();
            
            recorder.release();
            recorder = null;
        }
        
        
        String fileName = getFilename();
        
        copyWaveFile(getTempFilename(),fileName);
        
        
    }

    /**
     * on clicking STOP the recorder is set to null
     * and also the temp file are saved to original file and
     * delete the temp files
     */
    private void serviceStopped(){
        if(null != recorder){
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        //deleteTempFile();
        // recordingThread1.stop();
    }

    /*
     * this function is to write the audioData to original File.
     * this function first gets the filename = TempFileName
     * recorder.read(buffer, 0, size) stores the audioData from h/w to buffer
     * and write that to FileOutputStream Object
     *
     */
    public <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, b, 0, aLen);
        System.arraycopy(b, 0, b, aLen, bLen);

        return c;
    }
    
    private void writeAudioDataToFile() throws IOException{
        byte data[] = null;
     //   WavFile wavFile = WavFile.newWavFile(new File(getFilename()),1,800,16,44100);
        int len=0;
        String filename = getTempFilename();
        FileOutputStream os = null;
        FileOutputStream ios = null;
        try {
            os = new FileOutputStream(filename);
            
        } catch (FileNotFoundException e) {
        	Log.i(TagActivity, "Exception: "+e.toString());
            
        }
        if(null != os){
        	
            while(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
               // data = getFrameBytes();
            	//Log.i(TagActivity, "recorder reading PCM data");
            	recorder.read(buffer, 0, bufferSize);
            	data = buffer;
            	byte[] one = original;
            	byte[] two = data;

            	byte[] combined = new byte[one.length + two.length];

            	for (int i = 0; i < combined.length; ++i)
            	{
            	    combined[i] = i < one.length ? one[i] : two[i - one.length];
            	}
            	original=combined;
                if(data!=null)
                {
                    len+=data.length;
                    try {
                        os.write(data);
                    //    System.out.println("len: " + len + " Length: " + audioLength);
                    } catch (IOException e) {
                        
                        Log.i(TagActivity, "Exception: "+e.toString());
                    }
                    if(len>=2*audioLength)
                    {
                        if(ios!=null)
                        {
                            ios.write(data);
                        }
                        stopRecording();
                        break;
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                
                Log.i(TagActivity, "Exception: "+e.toString());
            }
        }
    }

	/*public byte[] getFrameBytes(){
        recorder.read(buffer, 0, bufferSize);

        // analyze sound
        int totalAbsValue = 0;
        short sample = 0;
        float averageAbsValue = 0.0f;

        for (int i = 0; i < bufferSize; i += 2) {
            sample = (short)((buffer[i]) | buffer[i + 1] << 8);
            totalAbsValue += Math.abs(sample);
        }
        averageAbsValue = totalAbsValue / bufferSize / 2;

        //System.out.println(averageAbsValue);

        // no input
        if (averageAbsValue < 30){
            return null;
        }
        return buffer;
    }*/


    /*
	 * on click STOP, it is called,
	 * the data of the temp file is saved to a WAVE file
	 * inFilename is tempFile and outFilename is originalFile
	 *
	 *
	 */
    private void copyWaveFile(String inFilename,String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = Constants.RECORDING_SAMPLE_RATE;
        int channels = 1;
        long byteRate = Constants.RECORDER_BPP * Constants.RECORDING_SAMPLE_RATE * channels/8;
        int recBufferSize= AudioRecord.getMinBufferSize(Constants.RECORDING_SAMPLE_RATE,Constants.RECORDER_CHANNELS,Constants.RECORDER_AUDIO_ENCODING);
        byte[] data1 = new byte[recBufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            //	AppLog.logString("File size: " + totalDataLen);

            //first write header then tempFile(inFile)
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            out.getChannel().transferFrom(in.getChannel(),44,totalAudioLen);
            in.close();
            out.close();
        } catch (Exception e) {
            
        }
    }

    /*
     * it prepend the WAV header to the output WAVE file
     * WAV has a 44B header
     */
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8);  // block align
        header[33] = 0;
        header[34] = Constants.RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

}
