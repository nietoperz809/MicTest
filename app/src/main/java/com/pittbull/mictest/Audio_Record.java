package com.pittbull.mictest;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

/**
 * @author RAHUL BARADIA
 */
public class Audio_Record extends Activity
{
    private static final int RECORDER_SAMPLERATE = 8000;

    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    private AudioOut player;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio__record);

        setButtonHandlers();
        enableButtons(false);

    }

    private void setButtonHandlers()
    {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable)
    {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording)
    {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    static final float ALPHA = 1.5f;


    protected short[] lowPass(short[] input, short[] output)
    {
        for (int i = 0; i < input.length; i++)
        {
            float f = output[i] + ALPHA * (input[i] - output[i]);
            output[i] = (short)f;
        }
        return output;
    }

    private void startRecording()
    {
        final int bufferSize = 2048;
        final int shortSize = bufferSize / 2;

        /*
        = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize < 2048)
            bufferSize =
         */

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        recorder.startRecording();

        player = new AudioOut(RECORDER_SAMPLERATE, 4 * bufferSize);

        //AutomaticGainControl.create(recorder.getAudioSessionId());

        isRecording = true;

        recordingThread = new Thread(new Runnable()
        {

            short buff[] = new short[shortSize];
            //short buff2[] = new short[shortSize];

            public void run()
            {
                while (isRecording)
                {
                    int shortsRead = recorder.read(buff, 0, shortSize);
                    //lowPass (buff, buff2);
                    player.submit (buff, shortsRead);
                    System.out.println("submit" + shortsRead);
                }
            }
        });
        recordingThread.start();
    }

    //    //Conversion of short to byte
    //    private byte[] short2byte(short[] sData)
    //    {
    //        int shortArrsize = sData.length;
    //        byte[] bytes = new byte[shortArrsize * 2];
    //
    //        for (int i = 0; i < shortArrsize; i++)
    //        {
    //            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
    //            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
    //            sData[i] = 0;
    //        }
    //        return bytes;
    //    }
    //
    //    private void writeAudioDataToFile()
    //    {
    //        // Write the output audio in byte
    //        String filePath = "/sdcard/8k16bitMono.wav";
    //
    //        short sData[] = new short[BufferElements2Rec];
    //
    //        FileOutputStream os = null;
    //        try
    //        {
    //            os = new FileOutputStream(filePath);
    //        }
    //        catch (FileNotFoundException e)
    //        {
    //            e.printStackTrace();
    //        }
    //
    //        while (isRecording)
    //        {
    //            // gets the voice output from microphone to byte format
    //            recorder.read(sData, 0, BufferElements2Rec);
    //            System.player.println("Short wirting to file" + sData.toString());
    //            try
    //            {
    //                // writes the data to file from buffer stores the voice buffer
    //                byte bData[] = short2byte(sData);
    //
    //                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
    //
    //            }
    //            catch (IOException e)
    //            {
    //                e.printStackTrace();
    //            }
    //        }
    //
    //        try
    //        {
    //            os.close();
    //        }
    //        catch (IOException e)
    //        {
    //            e.printStackTrace();
    //        }
    //    }

    private void stopRecording()
    {
        // stops the recording activity
        if (null != recorder)
        {
            isRecording = false;


            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btnStart:
                {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btnStop:
                {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    // onClick of backbutton finishes the activity.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}