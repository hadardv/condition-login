#  Mobile Security App – Android Access Challenge

A fun and creative Android security app that demonstrates conditional access using sensors, MLKit, media, and location.  
Access is granted only after completing **5 challenges**.

##  Features

| Challenge | Description |
|----------|-------------|
|  Dynamic Password | User must enter a password based on real-time device data: `battery% * 2 + cityNameLength + imageCount` |
|  Movement Detection | Using the device's accelerometer to detect if the user moved |
|  Smile Detection | Uses MLKit Face Detection to check if the user is smiling |
|  Recent Photo Check | User must have taken a photo within the last hour |
| 🎙 Voice Command | User must say the secret phrase `"סומסום יפתח"` |

---

##  Tech Stack

- **Language**: Java
- **UI**: RecyclerView + XML Layouts
- **Libraries/SDKs**:
  - [Google ML Kit](https://developers.google.com/ml-kit/vision/face-detection) – for smile detection
  - [Android SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer) – for voice command
  - [FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) – for location-based password
  - [MediaStore](https://developer.android.com/reference/android/provider/MediaStore) – to access gallery
  - Sensors: `TYPE_ACCELEROMETER`

---

##  How It Works

1. The app displays a list of 5 access conditions.
2. Each condition has a button or trigger to test it.
3. Once all 5 are fulfilled, the "Login" button is enabled.
4. Clicking login redirects to a success screen 🎉

---

##  Screenshots
![1000000158](https://github.com/user-attachments/assets/32e36208-ba5e-4201-b165-e06ea9af1013)
![1000000160](https://github.com/user-attachments/assets/94e9bf3c-8879-4d91-ae23-7f88b11680c7)
![1000000161](https://github.com/user-attachments/assets/c76d7d6f-876f-4741-ba31-2d5c12707604)




---

##  Permissions Required

- `ACCESS_FINE_LOCATION` – for detecting city name
- `CAMERA` – for taking a photo
- `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` – for checking gallery photos
- `RECORD_AUDIO` – for voice recognition

---

##  Author

Developed by [Hadar David](https://github.com/yourusername)  



