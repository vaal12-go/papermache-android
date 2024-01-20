package com.example.papermache2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;



public class MainActivity extends AppCompatActivity {
    private boolean showKeyAsPassword = false;
    private int decipheredTextSize = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = findViewById(R.id.scanButton);

        Activity activityContext = this;
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTONS", "User tapped the Supabutton");
                //Check documentation on :
                //https://github.com/journeyapps/zxing-android-embedded/tree/master
                new IntentIntegrator(activityContext)
                        .initiateScan();
            }
        });

        Button copyButton = findViewById(R.id.copyButton);

        copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView decipheredTextView = findViewById(R.id.decipheredTextView);
//                Toast.makeText(getBaseContext(), "Copy function is not implemented yet", Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Paper-mache",
                        decipheredTextView.getText());
                clipboard.setPrimaryClip(clip);
            }//public void onClick(View v) {
        });//copyButton.setOnClickListener(new View.OnClickListener() {
        Button showKeyButton = findViewById(R.id.buttonShowKey);
        showKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Buttons", "ShowKeybutton pressed");
                TextView keyTextView = findViewById(R.id.keyTextView);

                showKeyAsPassword = !showKeyAsPassword;
                //https://copyprogramming.com/howto/how-to-show-or-hide-text-in-a-password-field
                keyTextView.setInputType(showKeyAsPassword ?
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
            }
        });//showKeyButton.setOnClickListener(new View.OnClickListener() {

        Button zoomInButton = findViewById(R.id.buttonZoomIn);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Log.d("BUTTON", "ZoomIn button pressed");
                TextView decipheredTextView = findViewById(R.id.decipheredTextView);

                decipheredTextSize = decipheredTextSize+5;
                decipheredTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, decipheredTextSize);
            }
        });//showKeyButton.setOnClickListener(new View.OnClickListener() {

        Button zoomOutButton = (Button) findViewById(R.id.buttonZoomOut);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Log.d("BUTTON", "ZoomOut button pressed");
                TextView decipheredTextView = (TextView) findViewById(R.id.decipheredTextView);

                decipheredTextSize = decipheredTextSize-5;
                decipheredTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, decipheredTextSize);
            }
        });//showKeyButton.setOnClickListener(new View.OnClickListener() {

    }//protected void onCreate(Bundle savedInstanceState) {

    private void decryptString(String cipheredText) {
//        Log.d("BUTTONS", "User tapped the CopyButton pressed");
        TextView keyTextView = (TextView) findViewById(R.id.keyTextView);
        String key = keyTextView.getText().toString();
//        Log.d("BUTTONS", "Have key text:"+key);

//                key="qwe1";
        byte[] stretchedKey;
        try {
            stretchedKey = CipherRoutines.StretchKey(key);
            CipherRoutines.LogByteArray(stretchedKey);
        } catch (NoSuchAlgorithmException e) {
//            Log.d("Exception", "No SHA512 algorythm");
            throw new RuntimeException(e);
        }

//        String base64Cipher = "Ks/w8Kc7vMKCm38DzZl2RZRzfNGuaPwDfoUVLyRomEc=";//"qwe1 ciphered with key qwe1
//        base64Cipher = "6ZGOs00k9rAVvQnp9rLaw9AzWOOPluzTkmb8Wpc6Jv2GHM6acw5WRP4BQQS6lPhU";// qwe1 sdfgsdf sdfgsdfgsdf ciphered with key qwe1
        // Receiving side
        byte[] data = Base64.decode(cipheredText, Base64.DEFAULT);
//                    String text = new String(data, "UTF-8");

//        Log.d("BUTTONS", "Length of decoded base 64 array:"+Integer.toString(data.length));
//        CipherRoutines.LogByteArray(data);

        byte[] iv_bytes = Arrays.copyOfRange(data, 0, 16);

        byte[] cipheredText_bytes = Arrays.copyOfRange(data, 16, data.length);
//        CipherRoutines.LogByteArray(iv_bytes);
//        CipherRoutines.LogByteArray(cipheredText_bytes);

        byte[] deciphered_bytes = CipherRoutines.decrypt(cipheredText_bytes, stretchedKey, iv_bytes);

//        Log.d("DECRYPT", "Deciphered bytes");
//        CipherRoutines.LogByteArray(deciphered_bytes);
        String deciphered_string = new String(deciphered_bytes);
//        Log.d("DECRYPT:", deciphered_string);

        TextView decipheredTextView = (TextView) findViewById(R.id.decipheredTextView);
        decipheredTextView.setText(deciphered_string);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else { //if (intentResult.getContents() == null) {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                String str2Decrypt = intentResult.getContents();
//                Log.d("QRCODE", "Received string to decrypt:"+str2Decrypt);
//                Log.d("QRCODE", "Length of received string to decrypt:"+Integer.toString(str2Decrypt.length()));
                String formatName = intentResult.getFormatName();
//                Log.d("QRCODE", "Formatname:"+formatName);
                if(!formatName.equals("QR_CODE")) {//Different types of QRCodes: https://www.qrcode-tiger.com/different-types-of-qr-codes
                    Toast.makeText(getBaseContext(), "Error scanning QR code. This is not type QR_CODE, but:"+formatName, Toast.LENGTH_LONG).show();
                } else {
                    if(str2Decrypt.length() < 17) {
                        Toast.makeText(getBaseContext(), "Error scanning QR code. Very short code received:"+str2Decrypt, Toast.LENGTH_LONG).show();
                    } else {
                        decryptString(str2Decrypt);
                    }
                }//if(formatName!="QR_CODE") {
//                messageText.setText(intentResult.getContents());
//                messageFormat.setText(intentResult.getFormatName());
            } //} else { //if (intentResult.getContents() == null) {
        } else { //if (intentResult != null) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }//protected void onActivityResult(int requestCode, int resultCode, Intent data) {
}//public class MainActivity extends AppCompatActivity {