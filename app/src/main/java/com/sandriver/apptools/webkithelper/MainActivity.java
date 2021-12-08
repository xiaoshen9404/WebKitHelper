package com.sandriver.apptools.webkithelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sandriver.apptools.webkit.WebKitHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebKitHelper webKitHelper = new WebKitHelper();
        webKitHelper.embedWebKit(this, findViewById(R.id.fl_container));
        webKitHelper.loadUrl("https://www.baidu.com");
    }
}