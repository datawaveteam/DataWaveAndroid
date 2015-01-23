package dwai.datawave;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends ActionBarActivity {
    public static  String ENCODER_DATA = "It worked oh my gosh! kjshfd kjbsdf kjbsadfk jbsadf hbsdf jhbsadfj bsadfjb sadjfb sdajfbsdajf  jnkjnsdfkjbsadkjfjbaskdjf ksdjbf ksjdbfkasdjbf ksjdbf kjsbddfkj dsb";

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

    TextView rootWebView;
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
    String allText ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootWebView = ((TextView) findViewById(R.id.rootWebView));

        /// INIT FSK CONFIG

        try {
            mConfig = new FSKConfig(FSKConfig.SAMPLE_RATE_44100, FSKConfig.PCM_16BIT, FSKConfig.CHANNELS_MONO, FSKConfig.SOFT_MODEM_MODE_4, FSKConfig.THRESHOLD_20P);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        /// INIT FSK DECODER

        mDecoder = new FSKDecoder(mConfig, new FSKDecoder.FSKDecoderCallback() {

            @Override
            public void decoded(byte[] newData) {

                final String text = new String(newData);

                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("The text", text);
                        allText += text;
                        rootWebView.setText(allText);
                    }
                });
            }
        });

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
        } else {
            Log.i("FSKDecoder", "Please check the recorder settings, something is wrong!");
        }

        /// INIT FSK ENCODER

        mEncoder = new FSKEncoder(mConfig, new FSKEncoder.FSKEncoderCallback() {

            @Override
            public void encoded(byte[] pcm8, short[] pcm16) {
                if (mConfig.pcmFormat == FSKConfig.PCM_8BIT) {
                    //8bit buffer is populated, 16bit buffer is null

                    mAudioTrack.write(pcm8, 0, pcm8.length);

                    mDecoder.appendSignal(pcm8);
                } else if (mConfig.pcmFormat == FSKConfig.PCM_16BIT) {
                    //16bit buffer is populated, 8bit buffer is null

                    mAudioTrack.write(pcm16, 0, pcm16.length);

                }
            }
        });

        ///

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 1024,
                AudioTrack.MODE_STREAM);

        mAudioTrack.play();

        findViewById(R.id.goButtonURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editText = ((EditText)findViewById(R.id.urlEditText)).getText().toString();
                ENCODER_DATA = editText;
                new Thread(mDataFeeder).start();
            }
        });
        ///

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


