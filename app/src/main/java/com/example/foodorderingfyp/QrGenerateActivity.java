package com.example.foodorderingfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QrGenerateActivity extends AppCompatActivity {

    private Button generateQrCodeNow;
    private TextView qrCodeText;
    private ImageView qrCodeIV,qrgenerateBackBtn;
    private TextInputEditText dataEdit;
    private QRGEncoder qrgEncoder;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generate);

        generateQrCodeNow = (Button) findViewById(R.id.generateQrCodeNow);
        qrCodeText = (TextView) findViewById(R.id.textQrHeader);
        qrCodeIV = (ImageView) findViewById(R.id.QrCodeImage);
        dataEdit = (TextInputEditText) findViewById(R.id.idEditData);
        qrgenerateBackBtn = findViewById(R.id.qrgenerate_back);
        qrgenerateBackBtn.setOnClickListener(v -> onBackPressed());


        generateQrCodeNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = dataEdit.getText().toString();

                if(data.isEmpty()){
                    Toast.makeText(QrGenerateActivity.this, "Please enter amount.", Toast.LENGTH_SHORT).show();

                }
                else{
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point point = new Point();
                    display.getSize(point);
                    int width = point.x;
                    int height = point.y;
                    int dimen = width<height ? width:height;
                    dimen = dimen = 3/4;

                    qrgEncoder = new QRGEncoder(dataEdit.getText().toString(),null, QRGContents.Type.TEXT,dimen);
                    try{
                        bitmap  = qrgEncoder.encodeAsBitmap();
                        qrCodeText.setVisibility(View.GONE);
                        qrCodeIV.setImageBitmap(bitmap);

                    }catch(WriterException e){
                        e.printStackTrace();
                    }

                }
            }
        });
    }
}