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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    final static int  NO_OF_STRETCHING_ROUNDS = 128;

    private boolean showKeyAsPassword = false;

    private int decipheredTextSize = 12;
    static void LogByteArray(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < arr.length; i++) {
            sb.append(Integer.toString(arr[i] & 0xff)+", ");
        }
        sb.append(" ]");
        Log.d("LogByteArray", sb.toString());
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] IV) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedText = cipher.doFinal(cipherText);
            return decryptedText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static byte[] StretchKey(String key)  throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        int i=1;
        byte[] digest = md.digest(key.getBytes());
        while (i<NO_OF_STRETCHING_ROUNDS) {
            digest = md.digest(digest);
            i++;
        }

//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < digest.length; i++) {
//            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
//        }
//        Log.d("BUTTONS", "Have SHA512:"+sb.toString());
        return Arrays.copyOfRange(digest, 0, 32);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = (Button) findViewById(R.id.scanButton);

        Activity activityContext = this;
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTONS", "User tapped the Supabutton");
                new IntentIntegrator(activityContext)
                        .initiateScan();
            }
        });

        Button copyButton = (Button) findViewById(R.id.copyButton);

        copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                TextView decipheredTextView = (TextView) findViewById(R.id.decipheredTextView);

//                Toast.makeText(getBaseContext(), "Copy function is not implemented yet", Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(activityContext.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Paper-mache",
                        decipheredTextView.getText());
                clipboard.setPrimaryClip(clip);

            }//public void onClick(View v) {
        });//copyButton.setOnClickListener(new View.OnClickListener() {

        Button showKeyButton = (Button) findViewById(R.id.buttonShowKey);
        showKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Buttons", "ShowKeybutton pressed");
                TextView keyTextView = (TextView) findViewById(R.id.keyTextView);

                showKeyAsPassword = !showKeyAsPassword;
                //https://copyprogramming.com/howto/how-to-show-or-hide-text-in-a-password-field
                keyTextView.setInputType(showKeyAsPassword ?
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
            }
        });//showKeyButton.setOnClickListener(new View.OnClickListener() {

        Button zoomInButton = (Button) findViewById(R.id.buttonZoomIn);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTON", "ZoomIn button pressed");
                TextView decipheredTextView = (TextView) findViewById(R.id.decipheredTextView);

                decipheredTextSize = decipheredTextSize+5;
                decipheredTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, decipheredTextSize);
            }
        });//showKeyButton.setOnClickListener(new View.OnClickListener() {

        Button zoomOutButton = (Button) findViewById(R.id.buttonZoomOut);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTON", "ZoomOut button pressed");
                TextView decipheredTextView = (TextView) findViewById(R.id.decipheredTextView);

                decipheredTextSize = decipheredTextSize-5;
                decipheredTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, decipheredTextSize);
            }
        });//showKeyButton.setOnClickListener(new View.OnClickListener() {

    }//protected void onCreate(Bundle savedInstanceState) {

    private void decryptString(String cipheredText) {
        Log.d("BUTTONS", "User tapped the CopyButton pressed");
        TextView keyTextView = (TextView) findViewById(R.id.keyTextView);
        String key = keyTextView.getText().toString();
        Log.d("BUTTONS", "Have key text:"+key);

//                key="qwe1";
        byte[] stretchedKey;
        try {
            stretchedKey = StretchKey(key);
            LogByteArray(stretchedKey);
        } catch (NoSuchAlgorithmException e) {
            Log.d("Exception", "No SHA512 algorythm");
            throw new RuntimeException(e);
        }

//        String base64Cipher = "Ks/w8Kc7vMKCm38DzZl2RZRzfNGuaPwDfoUVLyRomEc=";//"qwe1 ciphered with key qwe1
//        base64Cipher = "6ZGOs00k9rAVvQnp9rLaw9AzWOOPluzTkmb8Wpc6Jv2GHM6acw5WRP4BQQS6lPhU";// qwe1 sdfgsdf sdfgsdfgsdf ciphered with key qwe1
        // Receiving side
        byte[] data = Base64.decode(cipheredText, Base64.DEFAULT);
//                    String text = new String(data, "UTF-8");
//                    Log.d

        Log.d("BUTTONS", "Length of decoded base 64 array:"+Integer.toString(data.length));
        LogByteArray(data);

        byte[] iv_bytes = Arrays.copyOfRange(data, 0, 16);

        byte[] cipheredText_bytes = Arrays.copyOfRange(data, 16, data.length);
        LogByteArray(iv_bytes);
        LogByteArray(cipheredText_bytes);

        byte[] deciphered_bytes = decrypt(cipheredText_bytes, stretchedKey, iv_bytes);

        Log.d("DECRYPT", "Deciphered bytes");
        LogByteArray(deciphered_bytes);
        String deciphered_string = new String(deciphered_bytes);
        Log.d("DECRYPT:", deciphered_string);

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
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                String str2Decrypt = intentResult.getContents();
                Log.d("QRCODE", "Received string to decrypt:"+str2Decrypt);
                Log.d("QRCODE", "Length of received string to decrypt:"+Integer.toString(str2Decrypt.length()));
                String formatName = intentResult.getFormatName();
                Log.d("QRCODE", "Formatname:"+formatName);
                if(!formatName.equals("QR_CODE")) {//Different types of QRCodes: https://www.qrcode-tiger.com/different-types-of-qr-codes
                    Toast.makeText(getBaseContext(), "Error scanning QR code. This is not type QR_CODE, but:"+formatName, Toast.LENGTH_LONG).show();
                } else {
                    if(str2Decrypt.length() < 17) {
                        Toast.makeText(getBaseContext(), "Error scanning QR code. Very short code received:"+str2Decrypt, Toast.LENGTH_LONG).show();
                    } else {
                        decryptString(str2Decrypt);
                    }
                }//if(formatName!="QR_CODE") {

//


//                messageText.setText(intentResult.getContents());
//                messageFormat.setText(intentResult.getFormatName());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}