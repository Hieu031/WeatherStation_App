#include <DHT.h>
#include <Firebase_ESP_Client.h>
#include <WiFi.h>
#include <time.h>

/* ======= CONFIG ============ */
#define DHTPIN 14
#define DHTTYPE DHT22
#define CO_SENSOR_PIN 34
#define RAIN_SENSOR_PIN 33
#define led_rain 22
#define led_co 23
#define led_dht22 21
#define led_sharp 19

/* DHT sensor object */
DHT dht(DHTPIN, DHTTYPE);

/* Dust sensor init parameters */
int measurePin = 32;
int ledPower = 16;
int samplingTime = 280;
int sleepTime = 9620;
const float K = 0.05; // Use the typical sensitivity in units of V per 100ug/m3. 

/* id and password wifi */
const char* ssid = "MyHieuB";
const char* password = "tamsotam";

/* Firebase config data */
#define API_KEY         "AIzaSyBdYfutaORWiNzbZccND8Rhh0kTIeuj1SQ"
#define DATABASE_URL    "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app"
#define USER_EMAIL      "nth280102@gmail.com"
#define USER_PASSWORD   "Matkhau0123@"

/* firebase object */	
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

/* Global variables to handler data sensor */
unsigned long lastSend = 0;
unsigned long prevMillis = 0;
const long interval = 5000; // para to delay 5 senconds 

/* Variables for dust sensor timer */
unsigned long dust_prev_micros = 0;
unsigned long voMeasuredTotal = 0;
int voCount = 0;
const int totalSamples = 100;
float current_dust_density = 0;

/* ========== Data struct includes data sensors ============ */
struct SensorData {
  float temperature;
  float humidity;
  float co;
  float rain;
  float dust;
  bool valid; // Flag to check if data is valid
};
SensorData currentData;

/* === Time module to get time for history ======== */
void getTime() {
  /* Set NTP, timezone VietNam (UTC+7) */
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov"); // UTC +7
  Serial.println("Sychronizing real time NTP...");

  /* Wait until get real time successfully */
  time_t now = time(nullptr);
  int retry = 0;
  while(now < 8 * 3600 * 2 && retry < 30) {
    delay(500);
    Serial.print(".");
    now = time(nullptr);
    retry++;
  }

  if (now < 8 * 3600 * 2) {
    Serial.println("\nCan't sychronize NTP! use millis() to get time.");
  }
  else
  {
    Serial.println("Sychronize time NTP successfully!");
    struct tm timeinfo;
    localtime_r(&now, &timeinfo);
    Serial.printf("Current time: %02d-%02d-%04d %02d:%02d:%02d\n",
                  timeinfo.tm_mday, timeinfo.tm_mon + 1, timeinfo.tm_year + 1900,
                  timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
  }
}

/* ====== Get time for firebase ========== */
String getTimeStamp() {
  time_t now;
  struct tm timeinfo;
  time(&now);
  localtime_r(&now, &timeinfo);

  char timeKey[32];
  strftime(timeKey, sizeof(timeKey), "%Y%m%d_%H%M%S", &timeinfo);
  return String(timeKey);
}

SensorData readSensor() {
  SensorData data;
  data.valid = true; // Default

  /* Read data from Sensor */
  /* ===== DHT22 ===== */
  float t = dht.readTemperature();
  float h = dht.readHumidity();
  if (isnan(t) || isnan(h)) {
    Serial.println("Read data from DHT22 error!");
    data.valid = false;
  } 
  else 
  {
    data.temperature = t;
    data.humidity = h;
  }

  /* ===== CO sensor ===== */
  int analogValue = analogRead(CO_SENSOR_PIN);
  if (analogValue >= 0 || analogValue <= 4095) {
    data.co = analogValue * (3.3 / 4095) * 200; // cal ppm
  }
  else 
  {
    Serial.println("Read data from sensor CO error!");
    data.valid = false;
  }

  /* ===== Rain sensor ===== */
  int rainVal = digitalRead(RAIN_SENSOR_PIN);
  if (rainVal == 0 || rainVal == 1) {
    data.rain = rainVal;
  }
  else 
  {
    Serial.println("Read data from rain sensor error!");
    data.valid = false;
  }

  /* ===== DUST Sensor (100 samples) ===== */
  data.dust = current_dust_density;

  return data;
}

/* ==== ACTUATOR WARNING LED ============ */
void updateWarningLEDs(SensorData data) {
  /* Turn on LED when data exceeds the warning threshold */
  digitalWrite(led_dht22, (data.temperature > 35) ? HIGH : LOW);
  digitalWrite(led_co, (data.co > 800) ? HIGH : LOW);
  digitalWrite(led_rain, (data.rain == 0) ? HIGH : LOW);
  digitalWrite(led_sharp, (data.dust > 35.5) ? HIGH : LOW); 
}

/* ======= LOG DATA SENSOR ================ */
void printSensorData(SensorData data) {
  Serial.printf("Temperature: %.1f°C | Humidity: %.1f%% | Rain: %s | Dust: %.1f µg/m³ | CO: %.0f ppm\n",
  data.temperature, data.humidity, (data.rain == 0 ? "Yes" : "No"), data.dust, data.co
  );
}

/* ============= SEND FIREBASE =============== */
void sendDataToFirebase(SensorData data) {
  String timestamp = getTimeStamp();
  String basePath  = "/WeatherHistory/" + timestamp;

  /* Send data with realtime */
  if(!Firebase.RTDB.setString(&fbdo, basePath + "/Temperature", String(data.temperature))) {
    Serial.printf("Temperature err: %s\n", fbdo.errorReason().c_str());
  }
  if(!Firebase.RTDB.setString(&fbdo, basePath + "/Humidity", String(data.humidity))){
    Serial.printf("Humidity err: %s\n", fbdo.errorReason().c_str());
  }
  if(!Firebase.RTDB.setString(&fbdo, basePath + "/Rain", String(data.rain))){
    Serial.printf("Rain err: %s\n", fbdo.errorReason().c_str());
  }
  if(!Firebase.RTDB.setString(&fbdo, basePath + "/CO", String(data.co))){
    Serial.printf("CO err: %s\n", fbdo.errorReason().c_str());
  }
  if(!Firebase.RTDB.setString(&fbdo, basePath + "/Dust", String(data.dust))){
    Serial.printf("Dust err: %s\n", fbdo.errorReason().c_str());
  }

  /* Send data current time*/
  if (!Firebase.RTDB.setString(&fbdo, "Temperature", String(data.temperature))) {
    Serial.printf("Temperature err: %s\n", fbdo.errorReason().c_str());
  }
  if (!Firebase.RTDB.setString(&fbdo, "Humidity", String(data.humidity))) {
    Serial.printf("Humidity err: %s\n", fbdo.errorReason().c_str());
  }
  if (!Firebase.RTDB.setString(&fbdo,   "Rain", String(data.rain))) {
    Serial.printf("Rain err: %s\n", fbdo.errorReason().c_str());
  } 
  if (!Firebase.RTDB.setString(&fbdo, "CO", String(data.co))) {
    Serial.printf("Co Value err: %s\n", fbdo.errorReason().c_str());
  }
  if (!Firebase.RTDB.setString(&fbdo, "Dust", String(data.dust))) {
    Serial.printf("Dust Density err: %s\n", fbdo.errorReason().c_str());
  }

  /* Log data at Serial Monitor */
  Serial.println("Data sent to Firebase (as String) successfully!");
}
/* ======== SETUP FOR ESP ============ */
void setup() {
  Serial.begin(115200);
  
  /* Init sensors and pins */
  dht.begin();
  pinMode(ledPower, OUTPUT);
  pinMode(CO_SENSOR_PIN, INPUT);
  pinMode(RAIN_SENSOR_PIN, INPUT);
  pinMode(led_co, OUTPUT);
  pinMode(led_rain, OUTPUT);
  pinMode(led_dht22, OUTPUT);
  pinMode(led_sharp, OUTPUT);

  /* Connect to Wifi */
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

  /* Get real time */
  getTime();

  /* Initalize firebase */
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  Serial.print("Waiting Firebase ready");
  int tries = 0;
  while (!Firebase.ready()) {
    delay(100);
    Serial.print(".");
    if (++tries > 100) { 
      Serial.println("\nFirebase not ready, check config!"); 
      break; 
    }
  }
  Serial.println("\nFirebase ready!");
}

void loop() {
  /* TASK 1: Non-blocking dust sensor reading */
  unsigned long currentMicros = micros();
  if (currentMicros - dust_prev_micros  >= (unsigned long)samplingTime + sleepTime) {
    dust_prev_micros = currentMicros;

    digitalWrite(ledPower, LOW);
    delayMicroseconds(samplingTime);
    int voMeasured = analogRead(measurePin);
    digitalWrite(ledPower, HIGH);

    if (voMeasured >= 0 && voMeasured <= 4095) {
      voMeasuredTotal += voMeasured;
      voCount++;
    }
    else 
    {
      Serial.println("Error reading data from dust sensor!");
    }

    if (voCount >= totalSamples) {
      float voAvg = (float)voMeasuredTotal / (float)totalSamples;
      float calcVoltage = voAvg / 4095 * 3.3;
      current_dust_density = calcVoltage / K * 100.0;

      /* Reset variables to start a new cycle */
      voCount = 0;
      voMeasuredTotal = 0;
    }
  }

  /* Task 2: Read other sensors and send data */
  unsigned long currentMillis = millis();
  if (currentMillis - prevMillis >= interval) {
    prevMillis = currentMillis;

    currentData = readSensor();
    if (!currentData.valid) {
      Serial.println("Skipping loop due to invalid sensor data!");
      return;
    }
  }

  /* Assign the dust value was calculated from task 1 */
  currentData.dust = current_dust_density;

  updateWarningLEDs(currentData);
  printSensorData(currentData);
  sendDataToFirebase(currentData);

}




