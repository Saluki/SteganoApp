package ovh.gorillahack.steganoapp.domain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ovh.gorillahack.steganoapp.R;
import ovh.gorillahack.steganoapp.algorithm.SteganoEncoder;
import ovh.gorillahack.steganoapp.exceptions.SteganoEncodeException;
import ovh.gorillahack.steganoapp.utils.Utils;

public class EncodeActivity extends AppCompatActivity {

    RelativeLayout layout;

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PICTURE = 2;

    protected EditText messageMultipleInput;
    protected TextView countMessageLabel;

    String messageToEncode = "";
    Bitmap pictureChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_pic);
        layout = (RelativeLayout) findViewById(R.id.choose_pic_layout);

        Utils.changeBackgroundColor(getSharedPreferences(Utils.PREFS_NAME, 0), layout);

        messageMultipleInput = (EditText) findViewById(R.id.messageMultipleInput);
        countMessageLabel = (TextView) findViewById(R.id.countMessageLabel);

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int messageLength = messageMultipleInput.length();
                countMessageLabel.setText(messageLength+" / 1024");

                if( messageLength>1024 ) {
                    messageMultipleInput.setTextColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        messageMultipleInput.addTextChangedListener(inputWatcher);
    }

    public void launchCamera(View view) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void showGallery(View view) {

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType(Utils.INTENT_IMAGE_TYPE);

        Intent chooserIntent = Intent.createChooser(getIntent(), "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(getApplicationContext(), R.string.choosepic_result_failed, Toast.LENGTH_LONG).show();
            Log.e("Activity result error:", "result code: " + resultCode + " request code:" + requestCode);
            return;
        }

        if (requestCode == PICK_IMAGE) {

            if (data == null || data.getData() == null) {
                Utils.buildTextViewPopUp(this, getString(R.string.global_error_occurred), getString(R.string.error));
                Log.e("ChoosePicActivity", "Data format was null");
                return;
            }

            Uri uri = data.getData();
            try {
                this.pictureChosen = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                Log.e("ChoosePicActivity", "Could not retrieve media: " + e.getMessage());
                return;
            }

        } else if (requestCode == TAKE_PICTURE) {

            Bundle extras = data.getExtras();
            pictureChosen = (Bitmap) extras.get("data");

        } else {
            Utils.buildTextViewPopUp(this, getString(R.string.global_error_occurred), getString(R.string.error));
            Log.e("ChoosePicActivity", "Bad request code given: " + requestCode);
            return;
        }

        try {
            encodeMessageInBitmap();
        }
        catch(SteganoEncodeException e) {

            // TODO Error feedback
            Log.e("EncodeActivity", "Could not encode message: "+e.getMessage(), e);
            return;
        }

        Intent mainIntent = new Intent(this, ovh.gorillahack.steganoapp.domain.MainActivity.class);
        startActivity(mainIntent);
    }

    protected void encodeMessageInBitmap() throws SteganoEncodeException {

        messageToEncode = messageMultipleInput.getText().toString();
        SteganoEncoder encoder = new SteganoEncoder(pictureChosen);
        String timeStamp = Utils.getCurrentTimeStamp();

        MediaStore.Images.Media.insertImage(getContentResolver(), pictureChosen, timeStamp + ".jpg", null);
        encoder.encode(messageToEncode);
    }

    //https://stackoverflow.com/questions/10903754/input-text-dialog-android
    /*public void buildEditTextPopUp() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.messageToEnc);

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.encode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                messageToEncode = input.getText().toString();
                SteganoEncoder encoder = new SteganoEncoder(pictureChosen);
                String timeStamp = Utils.getCurrentTimeStamp();
                MediaStore.Images.Media.insertImage(getContentResolver(), pictureChosen, timeStamp + ".jpg", null);
                Utils.buildTextViewPopUp(builder.getContext(), getString(R.string.info), getString(R.string.image_encrypt_succ));

                try {
                    encoder.encode(messageToEncode);
                } catch (Exception e) {
                    Utils.buildTextViewPopUp(builder.getContext(), getString(R.string.global_error_occurred), getString(R.string.error) + e.getMessage());
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }*/
}