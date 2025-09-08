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

// Define Firebase config
#define API_KEY         "AIzaSyBdYfutaORWiNzbZccND8Rhh0kTIeuj1SQ"
#define DATABASE_URL    "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app"
// #define API_KEY         "AIzaSyDUXtnwlte9QCjZXA_7QxoM37_h8xp8Acc"
// #define DATABASE_URL    "https://datasensor-64cfe-default-rtdb.asia-southeast1.firebasedatabase.app" // URL project in Firebase Homepage 
#define USER_EMAIL      "nth280102@gmail.com"
#define USER_PASSWORD   "Matkhau0123@"

// Firebase objects
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

Firebase_ESP_Client firebase;

// Sensor object
DHT dht(DHTPIN, DHTTYPE);

// Global variables
unsigned long preMillis = 0;
const long interval = 5000; // send data every 5 seconds

// ================== DATA STRUCT ==================
struct SensorData {
  float temperature;
  float humidity;
  float co;
  int rain;
  float dust;
  bool valid;   // Flag to check if data is valid 
};

// ================== TIME MODULE ==================
void getTime() {
  // Set NTP, timezone of VietNam (UTC+7)
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov"); // UTC+7
  Serial.println("Sychronizing real time NTP...");

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
    Serial.println("\nCan't sychronize NTP! Use millis() to get time.");
  } else {
    Serial.println("\nSychronize time NTP successfully!");
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
SensorData readSensors() {
  SensorData data;
  data.valid = true; // Default to valid

  // Read data from sensor DHT22
  data.temperature = random(0, 101);
  data.humidity = random(0, 101);
  // float t = dht.readTemperature();
  // float h = dht.readHumidity(); 
  // if (isnan(h) || isnan(t)){
  //   Serial.println("Reading data from DHT22 error!")
  //   data.valid = false;
  // }
  // else
  // {
  //   data.temperture = t;
  //   data.humidity = h;
  // }

  // Read data from sensor CO sensor
  // int analogValue = analogRead(CO_SENSOR_PIN);
  // if (analogValue >= 0 || analogValue <= 4095) {
  //   data.co = analogValue * (3.3 / 4095) * 200; // ppm
  // } 
  // else 
  // {
  //   Serial.println("Sensor CO reading error!");
  //   data.valid = false;
  // }
  data.co = random(300, 1200); // pretend from 300-1200 ppm

  // Rain sensor
  // int rainVal = digitalRead(RAIN_SENSOR_PIN);
  // if (rainVal == 0 || rainVal == 1) {
  //   data.rain = rainVal;
  // } 
  // else 
  // {
  //   Serial.println("Rain sensor reading error!");
  //   data.valid = false;
  // }
  data.rain = random(0, 2); // 0 or 1

  // Read data from Dust sensor (100 samples)
  // unsigned long voMeasuredTotal = 0;
  // int voCount = 0;
  // while (voCount <= 100) {
  //   digitalWrite(ledPower, LOW); // Turn on IR Led
  //   delayMicroseconds(samplingTime); // Delay 0.28ms for the sensor to stabilize
  //   int voMeasured = analogRead(measurePin); // Read adc about 0.1ms
  //   digitalWrite(ledPower, HIGH);  // Turn off Led
  //   delayMicroseconds(sleepTime);  // Delay 9.26ms

  //   if (voMeasured < 0 || voMeasured > 4095) {
  //     Serial.println("Fine dust sensor reading error!");
  //     data.valid = false;
  //     break;
  //   }  // Handle data sensor error
  //   voMeasuredTotal += voMeasured; // Sum times get samples
  //   voCount++; // Count times get samples
  // }
  // // Check condition and convert data
  // if (data.valid) {
  //   float voAvg = 1.0 * voMeasuredTotal / 100.0; // Calculate average value
  //   float calcVoltage = voAvg / 4095.0 * 3.3;  // Analog: 4095, Vcc: 3.3V
  //   data.dust = calcVoltage / K * 100.0; // µg/m³
  // }
  data.dust = random (10, 101);

  return data;
}

// ================== ACTUATOR MODULE ==================
void updateWarningLEDs(SensorData data) {
  // Turn on LED when data exceeds the warning threshold
  digitalWrite(led_dht22, (data.temperature > 35) ? HIGH : LOW);
  digitalWrite(led_co, (data.co > 800) ? HIGH : LOW);
  digitalWrite(led_rain, (data.rain == 0) ? HIGH : LOW);
  digitalWrite(led_sharp, (data.dust > 35.5) ? HIGH : LOW);
}

// ================== LOG MODULE ==================
void printSensorData(SensorData data) {
  Serial.printf("%.1f°C | %.1f%% | Rain: %d | Dust: %.1f µg/m³ | CO: %.0f ppm\n",
                data.temperature, data.humidity, data.rain, data.dust, data.co);
}

// ================== FIREBASE MODULE ==================
void sendDataToFirebase(SensorData data) {
  String timestamp = getTimeStamp();
  String path = "/WeatherHistory/" + timestamp;

  FirebaseJson json;
  json.set("Temperature", data.temperature);
  json.set("Humidity", data.humidity);
  json.set("Rain", data.rain);
  json.set("CO", data.co);
  json.set("Dust", data.dust);

  Serial.printf("Sending data to Firebase path: %s\n", path.c_str());
  bool ok = Firebase.RTDB.setJSON(&fbdo, path.c_str(), &json);

  if (ok) {
    Serial.printf("Send data to Firebase [%s] OK!\n", timestamp.c_str());
    printSensorData(data);
  } 
  else 
  {
    Serial.println("Firebase Error: " + fbdo.errorReason());
  }
}

// ================== SETUP ==================
void setup() {
  Serial.begin(115200);

  // Initialize sensors and pins
  dht.begin();
  pinMode(ledPower, OUTPUT);
  pinMode(CO_SENSOR_PIN, INPUT);
  pinMode(RAIN_SENSOR_PIN, INPUT);
  pinMode(led_co, OUTPUT);
  pinMode(led_rain, OUTPUT);
  pinMode(led_dht22, OUTPUT);
  pinMode(led_sharp, OUTPUT);

  // Connect to WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wifi: ");
  Serial.println(ssid);

  int wifiTries = 0;
  while(WiFi.status() != WL_CONNECTED){
    delay(300);
    Serial.print(".");
    wifiTries++;
    if(wifiTries > 30) {
      Serial.println("\n Can't connect to Wifi. Check SSID/Password again!");
      return;
    }
  }
  Serial.println("\nConnected to WiFi!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  // Sync NTP time
  getTime();

  // Initialize Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  firebase.begin(&config, &auth);
  firebase.reconnectWiFi(true);
  Serial.println("Initializing connect to Firebase...");

  int firebaseTries = 0;
  while(!Firebase.ready()){
    delay(100);
    Serial.print('#');
    firebaseTries++;
    if(firebaseTries > 100){
      Serial.println("\nCant't connect! Check config again!");
      return;
    } 
  }
  Serial.println("\nFirebase is ready!");
}

// ================== LOOP ==================
void loop() {
  Serial.println("\nThe new loop start...");
  SensorData sensor = readSensors();
    if (!sensor.valid) {
      Serial.println("Skip loop because of error of sensor data!");
      return;
    }
  updateWarningLEDs(sensor);
  sendDataToFirebase(sensor);
  delay(1000);
}
