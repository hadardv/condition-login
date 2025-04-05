package com.example.exe1mobilesecurity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exe1mobilesecurity.adapters.ConditionAdapter;
import com.example.exe1mobilesecurity.models.Condition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ConditionAdapter adapter;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean movementDetected = false;
    private boolean screenWasOff = false;
    private boolean screenAwakened  = false;
    private Button loginButton;



    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null) {
                        for (String match : matches) {
                            if (match.trim().equals("סומסום יפתח")) {
                                adapter.updateCondition(4, true); // 5th condition
                                checkAllConditions();
                                Toast.makeText(this, "✨ הסיסמה זוהתה בהצלחה!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toast.makeText(this, "לא זיהיתי את הסיסמה, נסה שוב", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        loginButton = findViewById(R.id.loginButton);
        loginButton.setEnabled(false); // Initially disabled

        RecyclerView recyclerView = findViewById(R.id.conditionsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new Condition("You may only enter in 00:00", false));
        conditionList.add(new Condition("Prove you alive.. move!", false));
        conditionList.add(new Condition("Turn the screen off... then awaken it to proceed.", false));
        conditionList.add(new Condition("You must have taken a photo in the last hour.", false));
        conditionList.add(new Condition("Phrase spoken: סומסום יפתח", false));

        adapter = new ConditionAdapter(conditionList);
        recyclerView.setAdapter(adapter);

        if(isMidnight()) {
            adapter.updateCondition(0,true);
            checkAllConditions();

        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager != null) {
             accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, filter);

        if(hasRecentPhoto()) {
            adapter.updateCondition(3,true);
            checkAllConditions();

        }

        Button speakButton = findViewById(R.id.speakButton);
        speakButton.setOnClickListener(v -> startSpeechRecognition());


    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !movementDetected) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > 3) {
                movementDetected = true;
                adapter.updateCondition(1, true);
                checkAllConditions();

            }
        }
    }

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                screenWasOff = true;
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (screenWasOff && !screenAwakened) {
                    screenAwakened = true;
                    adapter.updateCondition(2, true);
                    checkAllConditions();

                }
            }
        }
    };


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void requestPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        permissions.add(Manifest.permission.RECORD_AUDIO);

        ActivityCompat.requestPermissions(
                this,
                permissions.toArray(new String[0]),
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] permissionStatuses) {
        super.onRequestPermissionsResult(requestCode, permissions, permissionStatuses);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissionStatuses[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " not granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean isMidnight() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return hour == 15 && minute == 44;
    }

    private boolean hasRecentPhoto() {
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000); //TimeStamp of one hour ago
        String[] requestedFields  = {MediaStore.Images.Media.DATE_ADDED};
        String filterCondition = MediaStore.Images.Media.DATE_ADDED + " > ?";
        String[] filterValues = { String.valueOf(oneHourAgo / 1000)};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                requestedFields ,
                filterCondition,
                filterValues,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
        boolean recentPhotoFound = false;
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                recentPhotoFound = true;
            }
            cursor.close();
        }
        return recentPhotoFound;
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "אמור את המשפט הסודי...");

        try{
            speechLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAllConditions() {
        boolean allMet = true;
        for (Condition condition : adapter.getConditions()) {
            if (!condition.isMet()) {
                allMet = false;
                break;
            }
        }
        loginButton.setEnabled(allMet);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SuccessActivity.class);
            startActivity(intent);
            finish();
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        unregisterReceiver(screenReceiver);
    }

}


