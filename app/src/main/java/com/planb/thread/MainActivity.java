package com.planb.thread;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView console;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        console = findViewById(R.id.console);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print:
                console.setText(ThreadTest.printThread());
                break;
            case R.id.run:
                ThreadTest.runThread();
                break;
            default:
                break;
        }
    }
}
