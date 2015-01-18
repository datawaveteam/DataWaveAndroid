package dwai.datawave;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SplashScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

    }

    public void pressedClient(View v) {
        startActivity(new Intent(SplashScreen.this,MainActivity.class));
    }

    public void pressedServer(View v){
        startActivity(new Intent(SplashScreen.this,ServerActivity.class));
    }

}
