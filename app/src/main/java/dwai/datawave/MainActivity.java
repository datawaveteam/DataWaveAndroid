package dwai.datawave;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.nio.charset.Charset;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText urlEditText = ((EditText)findViewById(R.id.urlEditText));
        urlEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    editTextPressedEnter(urlEditText);
                    return true;
                }
                return false;
            }
        });
    }

    private void editTextPressedEnter(EditText editText){
        String url = editText.getText().toString();
        byte[] urlBytes = url.getBytes(Charset.forName("US-ASCII"));
        byte[][] urlBits = getBits(urlBytes);




    }
    private byte[][] getBits(byte[] bytes){
        final int AMOUNT_BITS_IN_ASCII = 7;
        byte[][] bits = new byte[7][bytes.length];
        for(int i = 0; i < bytes.length;i++){
            for(int j = 0; j < AMOUNT_BITS_IN_ASCII;j++){
                bits[i][j] = WaveUtil.getBit(j,bytes[i]);
            }
        }
        return bits;
    }





}
