package ovh.gorillahack.steganoapp.domain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import ovh.gorillahack.steganoapp.R;
import ovh.gorillahack.steganoapp.utils.Utils;

public class MainActivity extends AppCompatActivity {

    RelativeLayout layout;

    Button encode;
    Button decode;
    Button info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        layout = (RelativeLayout) findViewById(R.id.activity_main_layout);

        Utils.changeBackgroundColor(getSharedPreferences(Utils.PREFS_NAME, 0), layout);

        encode = (Button) findViewById(R.id.encodeBT);
        decode = (Button) findViewById(R.id.decodeBT);
        info = (Button) findViewById(R.id.infoBT);
    }

    public void launchEncodeActivity(View v) {
        Intent intent = new Intent(this, EncodeActivity.class);
        startActivity(intent);
    }

    public void launchDecodeActivity(View v) {
        Intent intent = new Intent(this, DecodeActivity.class);
        startActivity(intent);
    }

    public void launchSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void showInfo(View v) {
        Utils.buildTextViewPopUp(this, getString(R.string.info), getString(R.string.informations));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.changeBackgroundColor(getSharedPreferences(Utils.PREFS_NAME, 0), layout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            launchSettingsActivity();
        }

        return super.onOptionsItemSelected(item);
    }
}
