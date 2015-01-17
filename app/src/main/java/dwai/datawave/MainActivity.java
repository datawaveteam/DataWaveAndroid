package dwai.datawave;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.nio.charset.Charset;


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
        new AudioIn();
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

    private void playSound() {

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
//                genTone();
//                handler.post(new Runnable() {
//
//                    public void run() {
//                        playSound();
//                    }
//                });
            }
        });
        thread.start();
    }


    private class AudioIn extends Thread {
        private boolean stopped    = false;
        private final int SAMPLE_SIZE = 44100;

        private AudioIn() {

            start();
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            AudioRecord recorder = null;
            short[][]   buffers  = new short[256][160];
            int         ix       = 0;

            try {

                int N = AudioRecord.getMinBufferSize(SAMPLE_SIZE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
                int microphoneBufferSize = AudioTrack.getMinBufferSize(SAMPLE_SIZE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_SIZE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        N*10);

                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,SAMPLE_SIZE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,microphoneBufferSize,AudioTrack.MODE_STREAM);
                audioTrack.play();
                recorder.startRecording();

                // ... loop

                while(!stopped) {
                    short[] buffer = buffers[ix++ % buffers.length];

                    N = recorder.read(buffer,0,buffer.length);

                    audioTrack.write(buffer,0,buffer.length);

                }
            } catch(Throwable x) {
                Log.d("TTAAAG","Error reading voice audio",x);
            } finally {
                close();
            }
        }

        private void close() {
            stopped = true;
        }

    }


}
