package com.example.foodorderingfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LanguageActivity extends AppCompatActivity {

    private Button english1,chinese1;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        english1 = findViewById(R.id.englishword);
        chinese1 = findViewById(R.id.chineseword);
        backBtn = findViewById(R.id.backbutton);

        LanguageManager lang = new LanguageManager(this);

        english1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lang.updateResource("en");
                Toast.makeText(LanguageActivity.this,"Successfully Change Apps Language.",Toast.LENGTH_LONG).show();
            }
        });

        chinese1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lang.updateResource("zh");
                Toast.makeText(LanguageActivity.this,"Successfully Change Apps Language.",Toast.LENGTH_LONG).show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LanguageActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });

    }
}