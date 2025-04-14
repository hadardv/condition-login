package com.example.exe1mobilesecurity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.pm.PackageManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exe1mobilesecurity.adapters.ConditionAdapter;
import com.example.exe1mobilesecurity.models.Condition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

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
    private Button loginButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri photoUri;
    private File photoFile;



    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null) {
                        for (String match : matches) {
                            if (match.trim().equals("סומסום יפתח")) {
                                adapter.updateCondition(4, true); // 5th condition
                                checkAllConditions();
                                Toast.makeText(this, "הסיסמה זוהתה בהצלחה!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toast.makeText(this, "לא זיהיתי את הסיסמה, נסה שוב", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    detectSmileFromPhoto(photoFile);
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
        conditionList.add(new Condition( "Enter password: (battery * 2) + cityNameLength + imageCount", false));
        conditionList.add(new Condition("Prove you alive.. move!", false));
        conditionList.add(new Condition("Smile to the camera!", false));
        conditionList.add(new Condition("You must have taken a photo in the last hour.", false));
        conditionList.add(new Condition("Phrase spoken: סומסום יפתח", false));

        adapter = new ConditionAdapter(conditionList);
        recyclerView.setAdapter(adapter);



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager != null) {
             accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            }
        }

        if(hasRecentPhoto()) {
            adapter.updateCondition(3,true);
            checkAllConditions();

        }

        Button speakButton = findViewById(R.id.speakButton);
        speakButton.setOnClickListener(v -> startSpeechRecognition());
        Button smileButton = findViewById(R.id.smileButton);
        smileButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }
        });

        Button passwordButton = findViewById(R.id.passwordButton);
        passwordButton.setOnClickListener(v -> dynamicPassword());




    }

    private void dynamicPassword() {
        getCityNameFromLocation(cityName ->  {
            int battery = getBatteryPercentage();
            Log.d("pttt", String.valueOf(battery));
            int doubledBattery = battery * 2;

            int cityLength = cityName.replace(" ","").length();
            Log.d("pttt", cityName);

            int imageCount = getImageCount();
            Log.d("pttt", String.valueOf(imageCount));


            String password = "" + doubledBattery + cityLength + imageCount;
            EditText input = new EditText(this);

            new AlertDialog.Builder(this)
                    .setTitle("Enter Password")
                    .setMessage("Enter your dynamic password")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String entered = input.getText().toString().trim();
                        if (entered.equals(password)) {
                            adapter.updateCondition(0, true); // תנאי ראשון
                            checkAllConditions();
                            Toast.makeText(this, "סיסמה נכונה ✅", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "סיסמה שגויה ❌", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private int getBatteryPercentage() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra("level", -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra("scale", -1) : -1;
        if (level == -1 || scale == -1) return 0;
        return (int) ((level / (float) scale) * 100);
    }

    private int getImageCount() {
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            detectSmileFromPhoto(photoFile);
        }
    }

    private void detectSmileFromPhoto(File file) {
        InputImage image;
        try {
            image = InputImage.fromFilePath(this, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        FaceDetector detector = FaceDetection.getClient(options);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    for (Face face : faces) {
                        if (face.getSmilingProbability() != null &&
                                face.getSmilingProbability() > 0.8) {


                            adapter.updateCondition(2, true);
                            checkAllConditions();
                            Toast.makeText(this, "😊 חיוך יפה!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Toast.makeText(this, "😐 לא זיהינו חיוך.. נסה שנית!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                });
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

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);


        ActivityCompat.requestPermissions(
                this,
                permissions.toArray(new String[0]),
                PERMISSION_REQUEST_CODE
        );
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

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = File.createTempFile("smile_photo", ".jpg", getCacheDir());
                photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (permissionStatuses.length > 0 && permissionStatuses[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCityNameFromLocation(Consumer<String> callback) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                String city = addresses.get(0).getLocality();
                                callback.accept(city != null ? city : "Unknown");
                            } else {
                                callback.accept("Unknown");
                            }
                        } catch (IOException e) {
                            callback.accept("Unknown");
                        }
                    } else {
                        callback.accept("Unknown");
                    }
                })
                .addOnFailureListener(e -> callback.accept("Unknown"));
    }

}


