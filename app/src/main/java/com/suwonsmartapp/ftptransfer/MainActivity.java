package com.suwonsmartapp.ftptransfer;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.transfer_button).setOnClickListener(this);


        mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.txt");

        if (!mFile.exists()) {
            try {
                mFile.createNewFile();

                FileWriter fileWriter = new FileWriter(mFile.getName(), true);

                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("test test");
                bufferedWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        new DataSend(this).execute("www.readonly.co.kr", "21", mFile.getAbsolutePath(), "readonly", "seo1347518", "true");
    }
}
