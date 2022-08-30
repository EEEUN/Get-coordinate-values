package com.example.uitest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.media.metrics.Event;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    String TAG = "log#####";

    View mView;
    TextView xyView;
    Button readButton, touchButton;

    boolean isWrite = true;
    String filePath, fileName;
    float curX, curY;
    File dir, file;

    List<String> lineList = new ArrayList<String>();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = findViewById(R.id.view);
        xyView = findViewById(R.id.coordinates);
        readButton = findViewById(R.id.read_button);
        touchButton = findViewById(R.id.touch_button);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);

        filePath = Environment.getExternalStorageDirectory().toString();
        fileName = "logFile.txt";
        dir = new File(filePath + "/logFolder");
        file = new File(dir, fileName);

        // 파일 접근, 쓰기 권한 요청
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }

        // '저장된 좌표값 출력' 버튼 클릭
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xyView.setText("");
                onFileRead();
            }
        });

        // 파일 내용 출력과 클릭 시뮬레이션을 위한 Handler, Runnable 생성
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                float x, y;
                String string;

                string = lineList.get(count).toString();
                String[] arr = string.split(", ");
                x = Float.parseFloat(arr[0]);
                y = Float.parseFloat(arr[1]);

                Log.d(TAG, "run: x, y : " + x + ", " + y);
                simulateClick(x, y);
                count++;

                Log.d(TAG, "run: lineList " + lineList);
                if(count < lineList.size()) {
                    handler.postDelayed(this, 1000);
                }
                else {
                    lineList.clear();
                    count = 0;
                }
            }
        };

        // '좌표값대로 오토 터치' 버튼 클릭
        touchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    while((line = reader.readLine()) != null) {
                        lineList.add(line);
                    }
                    reader.close();
                    handler.postDelayed(runnable, 1000);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 버튼1 클릭
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 버튼1 클릭됨");
            }
        });

        // 버튼2 클릭
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 버튼2 클릭됨");
            }
        });

        // 버튼3 클릭
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 버튼3 클릭됨");
            }
        });

        // 버튼4 클릭
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 버튼4 클릭됨");
            }
        });

        // 버튼5 클릭
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 버튼5 클릭됨");
            }
        });
    }

    /**
     * 앱 내 화면을 터치하면 터치한 좌표값을 얻어 파일에 저장하는 onFileWrite()을 실행합니다.
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        curX = event.getX();  //눌린 곳의 X좌표
        curY = event.getY();  //눌린 곳의 Y좌표

        // 화면 눌렀다 뗐을 때 좌표값 출력
        if(action == event.ACTION_UP) {
            xyView.setText(curX + ", " + curY);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(isWrite) {
                        onFileWrite();
                    }
                }
            }).start();
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 화면 터치 좌표값을 기기 내 /storage/emulated/0/logFolder 에 logFile.txt 파일로 저장합니다.
     */
    private void onFileWrite() {
        String contents = xyView.getText().toString() + "\n";

        Log.d(TAG, "onFileWrite: contents : " + contents);
        Log.d(TAG, "onFileWrite: path : " + Environment.getExternalStorageDirectory() + "/logFolder");

        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                Log.d(TAG, "WriteTextFile: 파일이 존재하지 않음");
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, true); // 계속 이어서 쓸 수 있음
            writer.write(contents);
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * logFile.txt 파일에 저장되어 있는 좌표값을 로그로 출력합니다.
     */
    private void onFileRead() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "onFileRead: " + line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 사용자가 직접 화면을 터치하는 Click 동작을 시뮬레이션합니다. (자동 터치 기능)
     */
    private void simulateClick(float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[1];
        MotionEvent.PointerProperties pp1 = new MotionEvent.PointerProperties();
        pp1.id = 0;
        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
        properties[0] = pp1;
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
        pc1.x = x;
        pc1.y = y;
        pc1.pressure = 1;
        pc1.size = 1;
        pointerCoords[0] = pc1;
        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, 1, properties,
                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
        dispatchTouchEvent(motionEvent);

        motionEvent = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, 1, properties,
                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
        dispatchTouchEvent(motionEvent);

        Log.d(TAG, "simulateClick: x, y " + pc1.x + ", " + pc1.y);
    }
}