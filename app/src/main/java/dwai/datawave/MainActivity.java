package dwai.datawave;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        final int sleep = 300;
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        for (int i = 0; i < 40000; i += 100) {
                            startTone(i);
                            Thread.sleep(1000);
                            Log.d("TAAG", "Frequency was " + i);
                            stopTone();

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
//        }.start();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TAG WHATEVER", pitchInHz + "");
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);

        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher, "Audio Dispatcher").start();


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
        t = new Thread() {
            public void run() {
                // set process priority
//                setPriority(Thread.MAX_PRIORITY);
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
        };
        t.start();
    }

    public void stopTone() {
        isRunning = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t = null;
    }


}
