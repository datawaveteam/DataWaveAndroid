package dwai.datawave;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.nio.charset.Charset;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class MainActivity extends ActionBarActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        final EditText urlEditText = ((EditText) findViewById(R.id.urlEditText));
        urlEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    editTextPressedEnter(urlEditText);
                    return true;
                }
                return false;
            }


        });

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        startTone(('Q'-32) * 100);

                        Log.d("TAG STUFFFF AHH" , (('Q'-32) * 100) + "");
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
//        };
        }.start();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            float lastPitch = -1;
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                float pitchInHz = result.getPitch();
//                Log.d("TAG WHATEVER", ""+pitchInHz);
//                Log.d("TAG WHATEVER", (char)(pitchInHz / 100) + "");

                if (lastPitch > 0) {
                    if (pitchInHz == -1) {
                        Log.d("TAG WHATEVER", ""+lastPitch);
                        Log.d("TAG WHATEVER", (char)(((lastPitch / 100) + 32)) + "");
                        lastPitch = -1;
                    }
                    lastPitch = pitchInHz;
                }

                if (pitchInHz > -1) {
                    if (lastPitch == -1) {
                        lastPitch = pitchInHz;
                    }
                }
            }
        };
//        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
//
//        dispatcher.addAudioProcessor(p);
//        new Thread(dispatcher, "Audio Dispatcher").start();


    }


    private void editTextPressedEnter(EditText editText) {
        String url = editText.getText().toString();
        byte[] urlBytes = url.getBytes(Charset.forName("US-ASCII"));
        byte[][] urlBits = getBits(urlBytes);
    }

    private byte[][] getBits(byte[] bytes) {
        final int AMOUNT_BITS_IN_ASCII = 7;
        byte[][] bits = new byte[7][bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < AMOUNT_BITS_IN_ASCII; j++) {
                bits[i][j] = WaveUtil.getBit(j, bytes[i]);
            }
        }
        return bits;
    }


    Thread t;
    int sr = 44100;
    boolean isRunning = true;

    public void startTone(final double frequency) {
        isRunning = true;
        int buffsize = AudioTrack.getMinBufferSize(sr,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        // create an audiotrack object
        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC, sr,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffsize,
                AudioTrack.MODE_STREAM);

        short samples[] = new short[buffsize];
        int amp = 10000;
        double twopi = 8. * Math.atan(1.);
        double fr = frequency;
        double ph = 0.0;
        // start audio
        audioTrack.play();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isRunning = false;
            }
        }, 10);
        while (isRunning) {

            for (int i = 0; i < buffsize; i++) {
                samples[i] = (short) (amp * Math.sin(ph));
                ph += twopi * fr / sr;
            }
            audioTrack.write(samples, 0, buffsize);
        }
        audioTrack.stop();
        audioTrack.release();
    }

    public void stopTone() {
        isRunning = false;
    }


}
