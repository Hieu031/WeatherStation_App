#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <DHT.h>
#include <ArduinoJson.h>
#include <time.h>

// ================== CONFIG ==================
#define DHTPIN 14
#define DHTTYPE DHT22
#define CO_SENSOR_PIN 34
#define RAIN_SENSOR_PIN 33
#define led_rain 22
#define led_co 23
#define led_dht22 21
#define led_sharp 19

// Dust sensor
int measurePin = 32;
int ledPower = 16;
int samplingTime = 280;
int sleepTime = 9620; 
const float K = 0.05; // Use the typical sensitivity in units of V per 100ug/m3. 

const char* ssid = "MyHieuB";
const char* password = "tamsotam";

// Firebase configuration
// NOTE: Please double-check these details in your Firebase Console.
#define API_KEY       "AIzaSyDUXtnwlte9QCjZXA_7QxoM37_h8xp8Acc"
#define DATABASE_URL    "https://datasensor-64cfe-default-rtdb.asia-southeast1.firebasedatabase.app"
#define USER_EMAIL      "nth280102@gmail.com"
#define USER_PASSWORD   "Matkhau0123@"

// Firebase objects
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

Firebase_ESP_Client firebase;

// Sensor object
DHT dht(DHTPIN, DHTTYPE);

// ================== NEW GLOBAL VARIABLES ==================
// Variables for data sending timer
unsigned long prevMillis = 0;
const long interval = 5000; // send data every 5 seconds

// Variables for dust sensor timer
unsigned long dust_prev_micros = 0;
unsigned long voMeasuredTotal = 0;
int voCount = 0;
const int totalSamples = 100;
float current_dust_density = 0;

// Data storage struct
struct SensorData {
  float temperature;
  float humidity;
  float co;
  int rain;
  float dust;
  bool valid;   
};
SensorData currentData;

// ================== TIME MODULE ==================
void getTime() {
  // Configure NTP, Vietnam timezone (UTC+7)
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  Serial.println("Synchronizing real-time NTP...");
  
  // Wait until we have a real time
  time_t now = time(nullptr);
  int retry = 0;
  while (now < 8 * 3600 * 2 && retry < 30) {
    delay(500);
    Serial.print(".");
    now = time(nullptr);
    retry++;
  }
  
  if (now < 8 * 3600 * 2) {
    Serial.println("\nCan't synchronize NTP! Using millis() for time.");
  } else {
    Serial.println("\nNTP synchronization successful!");
    struct tm timeinfo;
    localtime_r(&now, &timeinfo);
    Serial.printf("Current time: %02d-%02d-%04d %02d:%02d:%02d\n",
                  timeinfo.tm_mday, timeinfo.tm_mon + 1, timeinfo.tm_year + 1900,
                  timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
  }
}

String getTimeStamp() {
  time_t now;
  struct tm timeinfo;
  time(&now);
  localtime_r(&now, &timeinfo);
  char timeKey[32];
  strftime(timeKey, sizeof(timeKey), "%Y%m%d_%H%M%S", &timeinfo);
  return String(timeKey);
}

// ================== SENSOR MODULE ==================
// This function only reads non-blocking sensors
SensorData readNonBlockingSensors() {
  SensorData data;
  data.valid = true; 

  // DHT22
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  if (isnan(h) || isnan(t)) {
    Serial.println("Error reading data from DHT22 sensor!");
    data.valid = false;
  } else {
    data.temperature = t;
    data.humidity = h;
  }

  // CO sensor
  int analogValue = analogRead(CO_SENSOR_PIN);
  if (analogValue >= 0 && analogValue <= 4095) {
    data.co = analogValue * (3.3 / 4095.0) * 200;
  } else {
    Serial.println("Error reading data from CO sensor!");
    data.valid = false;
  }

  // Rain sensor
  int rainVal = digitalRead(RAIN_SENSOR_PIN);
  if (rainVal == 0 || rainVal == 1) {
    data.rain = rainVal;
  } else {
    Serial.println("Error reading data from rain sensor!");
    data.valid = false;
  }

  // Assign the dust value that was calculated from the non-blocking loop
  data.dust = current_dust_density;

  return data;
}

// ================== ACTUATOR MODULE ==================
void updateWarningLEDs(SensorData data) {
  // Turn on LED when data exceeds the warning threshold
  digitalWrite(led_dht22, (data.temperature > 35) ? HIGH : LOW);
  digitalWrite(led_co, (data.co > 800) ? HIGH : LOW);
  digitalWrite(led_rain, (data.rain == 0) ? HIGH : LOW); // Assuming 0 means it's raining
  digitalWrite(led_sharp, (data.dust > 35.5) ? HIGH : LOW);
}

// ================== LOG MODULE ==================
void printSensorData(SensorData data) {
  Serial.printf("Temperature: %.1f°C | Humidity: %.1f%% | Rain: %s | Dust: %.1f µg/m³ | CO: %.0f ppm\n",
                data.temperature, data.humidity, (data.rain == 0 ? "Yes" : "No"), data.dust, data.co);
}

// ================== FIREBASE MODULE ==================
void sendDataToFirebase(SensorData data) {
  String timestamp = getTimeStamp();
  String path = "/WeatherHistory/" + timestamp;
  FirebaseJson json;
  json.set("Temperature", data.temperature);
  json.set("Humidity", data.humidity);
  json.set("Rain", data.rain);
  json.set("CO Value", data.co);
  json.set("Dust Density", data.dust);
  Serial.printf("Sending data to Firebase at path: %s\n", path.c_str());
  bool ok = Firebase.RTDB.setJSON(&fbdo, path.c_str(), &json);
  if (ok) {
    Serial.printf("Data sent to Firebase successfully!\n");
  } else {
    Serial.println("Firebase Error: " + fbdo.errorReason());
  }
}

// ================== SETUP FUNCTION ==================
void setup() {
  Serial.begin(115200);
  dht.begin();
  pinMode(ledPower, OUTPUT);
  pinMode(CO_SENSOR_PIN, INPUT);
  pinMode(RAIN_SENSOR_PIN, INPUT);
  pinMode(led_co, OUTPUT);
  pinMode(led_rain, OUTPUT);
  pinMode(led_dht22, OUTPUT);
  pinMode(led_sharp, OUTPUT);

  // Connect to WiFi
  Serial.print("Connecting to Wifi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  int wifiTries = 0;
  while(WiFi.status() != WL_CONNECTED){
    delay(300);
    Serial.print(".");
    wifiTries++;
    if(wifiTries > 30) {
      Serial.println("\nUnable to connect to Wifi. Please check SSID/Password!");
      return;
    }
  }
  Serial.println("\nSuccessfully connected to WiFi!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  getTime();

  // Initialize Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Serial.println("Attempting to initialize Firebase...");
  firebase.begin(&config, &auth);
  Serial.println("firebase.begin() function has been called.");
  
  if (!Firebase.ready()) {
    Serial.println("\nFirebase initialization failed! Please check your API Key, Database URL, and login details.");
    Serial.println("Firebase Error: " + fbdo.errorReason());
    return;
  }
  Serial.println("\nFirebase is ready!");
}

// ================== LOOP FUNCTION ==================
void loop() {
  // --- TASK 1: Non-blocking dust sensor reading ---
  unsigned long currentMicros = micros();
  // The total time for each sample is samplingTime + sleepTime
  if (currentMicros - dust_prev_micros >= (unsigned long)samplingTime + sleepTime) {
    dust_prev_micros = currentMicros;

    digitalWrite(ledPower, LOW);
    delayMicroseconds(samplingTime);
    int voMeasured = analogRead(measurePin);
    digitalWrite(ledPower, HIGH);

    if (voMeasured >= 0 && voMeasured <= 4095) {
      voMeasuredTotal += voMeasured;
      voCount++;
    } else {
      Serial.println("Error reading data from dust sensor!");
    }

    if (voCount >= totalSamples) {
      float voAvg = (float)voMeasuredTotal / (float)totalSamples;
      float calcVoltage = voAvg / 4095.0 * 3.3;
      current_dust_density = calcVoltage / K * 100.0;
      
      // Reset variables to start a new cycle
      voCount = 0;
      voMeasuredTotal = 0;
    }
  }

  // --- TASK 2: Read other sensors and send data ---
  unsigned long currentMillis = millis();
  if (currentMillis - prevMillis >= interval) {
    prevMillis = currentMillis;

    currentData = readNonBlockingSensors();
    if (!currentData.valid) {
      Serial.println("Skipping loop due to invalid sensor data!");
      return;
    }
    
    // Assign the dust value that was calculated from task 1
    currentData.dust = current_dust_density;

    updateWarningLEDs(currentData);
    printSensorData(currentData);
    sendDataToFirebase(currentData);
  }
}
