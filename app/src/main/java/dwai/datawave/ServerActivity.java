package dwai.datawave;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidanimations.library.attention.PulseAnimator;

import java.io.IOException;
import java.nio.ByteBuffer;


public class ServerActivity extends ActionBarActivity {

    private String ENCODER_DATA = "It worked oh my gosh! I love to drink waffles and data wave is so awesome I can't believe it! ";
    protected FSKConfig mConfig;
    protected FSKEncoder mEncoder;
    protected FSKDecoder mDecoder;

    protected AudioTrack mAudioTrack;

    protected AudioRecord mRecorder;
    protected int mBufferSize = 0;

    protected Runnable mRecordFeed = new Runnable() {

        @Override
        public void run() {

            while (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                short[] data = new short[mBufferSize / 2]; //the buffer size is in bytes

                // gets the audio output from microphone to short array samples
                mRecorder.read(data, 0, mBufferSize / 2);

                mDecoder.appendSignal(data);
            }
        }
    };

    protected Runnable mDataFeeder = new Runnable() {

        @Override
        public void run() {
            byte[] data = ENCODER_DATA.getBytes();

            if (data.length > FSKConfig.ENCODER_DATA_BUFFER_SIZE) {
                //chunk data

                byte[] buffer = new byte[FSKConfig.ENCODER_DATA_BUFFER_SIZE];

                ByteBuffer dataFeed = ByteBuffer.wrap(data);

                while (dataFeed.remaining() > 0) {

                    if (dataFeed.remaining() < buffer.length) {
                        buffer = new byte[dataFeed.remaining()];
                    }

                    dataFeed.get(buffer);
                    findViewById(R.id.transmitting_server_text).setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.Pulse).duration(1000).playOn(findViewById(R.id.transmitting_server_text));
                    mEncoder.appendData(buffer);

                    try {
                        Thread.sleep(100); //wait for encoder to do its job, to avoid buffer overflow and data rejection
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mEncoder.appendData(data);
            }
        }
    };

    private String allText = "";

    private void createDecoderCallback() {

        mDecoder = new FSKDecoder(mConfig, new FSKDecoder.FSKDecoderCallback() {
            @Override
            public void decoded(byte[] newData) {
                final String text = new String(newData);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("hz", text);
                        findViewById(R.id.receiving_server_text).setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.Pulse).duration(1000).playOn(findViewById(R.id.receiving_server_text));
                    }
                });
            }
        });
    }

    private void serveData() {

    }


    private String removeNonAscii(String text) {
        String output = text.replaceAll("[^\\p{ASCII}]", "");
        return output;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        try {
            mConfig = new FSKConfig(FSKConfig.SAMPLE_RATE_44100, FSKConfig.PCM_16BIT, FSKConfig.CHANNELS_MONO, FSKConfig.SOFT_MODEM_MODE_4, FSKConfig.THRESHOLD_10P);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        createDecoderCallback();
        createListenForDecode();
        createEncodeCallback();
    }

    private void createEncodeCallback() {
        mEncoder = new FSKEncoder(mConfig, new FSKEncoder.FSKEncoderCallback() {

            @Override
            public void encoded(byte[] pcm8, short[] pcm16) {
                if (mConfig.pcmFormat == FSKConfig.PCM_8BIT) {
                    //8bit buffer is populated, 16bit buffer is null

                    mAudioTrack.write(pcm8, 0, pcm8.length);

                } else if (mConfig.pcmFormat == FSKConfig.PCM_16BIT) {
                    //16bit buffer is populated, 8bit buffer is null

                    mAudioTrack.write(pcm16, 0, pcm16.length);

                }
            }
        });
    }


    private void createListenForDecode() {
        //make sure that the settings of the recorder match the settings of the decoder
        //most devices cant record anything but 44100 samples in 16bit PCM format...
        mBufferSize = AudioRecord.getMinBufferSize(FSKConfig.SAMPLE_RATE_44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        //scale up the buffer... reading larger amounts of data
        //minimizes the chance of missing data because of thread priority
        mBufferSize *= 10;

        //again, make sure the recorder settings match the decoder settings
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, FSKConfig.SAMPLE_RATE_44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);

        if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            mRecorder.startRecording();

            //start a thread to read the audio data
            Thread thread = new Thread(mRecordFeed);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();

            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, 1024,
                    AudioTrack.MODE_STREAM);

            mAudioTrack.play();
        } else {
            Log.i("FSKDecoder", "Please check the recorder settings, something is wrong!");
        }
    }

    @Override
    protected void onDestroy() {
        mDecoder.stop();

        mEncoder.stop();

        mAudioTrack.stop();
        mAudioTrack.release();

        super.onDestroy();
    }

}
