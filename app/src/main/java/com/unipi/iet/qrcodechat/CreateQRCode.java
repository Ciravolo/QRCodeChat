package com.unipi.iet.qrcodechat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class CreateQRCode extends AppCompatActivity {

    ImageView imageViewQRCode;
    String keyString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qrcode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_qrcode);
        setSupportActionBar(toolbar);

        imageViewQRCode = (ImageView) findViewById(R.id.imageViewQRCode);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            keyString= null;
        } else {
            keyString = extras.getString("randomkey");
        }

        Log.i("qr code value:", keyString+"="+UserDetails.username);

        setQRCode(keyString+"="+UserDetails.username);

    }

    public void setQRCode(String qrText){

        QRCodeWriter writer = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = writer.encode(qrText, BarcodeFormat.QR_CODE, 1024, 1024);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageViewQRCode.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }



}
