package dwai.datawave;

import android.content.Intent;
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
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends ActionBarActivity {
    private String ENCODER_DATA = "It worked oh my gosh! I love to drink waffles and data wave is so awesome I can't believe it! ";
    private void createUrlEnterListener() {
        final EditText edittext = (EditText) findViewById(R.id.urlEditText);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Log.d("Tag", "some words here for sleep deprived stefan...");
                    editTextPressedEnter((EditText) v);
                }
                return false;
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    public void editTextPressedEnter(EditText view) {
        Log.d("tag", "this is working just fine.");
        ENCODER_DATA = view.getText().toString();
        Intent intent = new Intent(MainActivity.this,ClientActivity.class);
        startActivity(intent);
    }
}
