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
const float K = 0.05;

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
const long interval = 5000; // para to delay 5 senconds 

/* Data struct includes data sensors */
struct SensorData {
  float temperature;
  float humidity;
  float co;
  float rain;
  float dust;
  bool valid; // Flag to check if data is valid
}

/* Time module to get time for history */
void getTime() {
  /* Set NTP, timezone VietNam (UTC+7) */
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov"); // UTC +7
  Serial.println("Sychronizing real time NTP...");

  /* Wait until get real time successfully */
  time_t now = time(null_ptr);
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
    Serail.println(""Sychronize time NTP successfully!");
    struct tm timeinfo;
    localtime_r(&now, &timeinfo);
    Serial.printf("Current time: %02d-%02d-%04d %02d:%02d:%02d\n");
  }

}


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

  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi...");
  while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); }
  Serial.println(" connected!");

  // DÙNG DUY NHẤT instance toàn cục Firebase
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
    if (++tries > 100) { Serial.println("\nFirebase not ready, check config!"); break; }
  }
  Serial.println("\nFirebase ready!");
}

void loop() {
  // ====== DỮ LIỆU GIẢ LẬP ĐỂ TEST ======
  float humidity    = random(0, 101);
  float temperature = random(0, 101);
  float coValue      = random(300, 1200);
  int   rainValue   = random(0, 2);       // 0 hoặc 1 
  float dust        = random(10, 101);

  // ====== LED cảnh báo ======
  digitalWrite(led_dht22, (temperature > 35) ? HIGH : LOW);
  digitalWrite(led_co,    (coValue > 800)     ? HIGH : LOW);
  digitalWrite(led_rain,  (rainValue == 0)   ? HIGH : LOW);
  digitalWrite(led_sharp, (dust > 35.5)      ? HIGH : LOW);

  // ====== GỬI LÊN FIREBASE THEO 1 JSON ======
  if (Firebase.ready() && millis() - lastSend > 10000) {
    lastSend = millis();

    if (!Firebase.RTDB.setString(&fbdo, "Temperature", String(temperature,0)))
    Serial.printf("Temperature err: %s\n", fbdo.errorReason().c_str());

  if (!Firebase.RTDB.setString(&fbdo, "Humidity", String(humidity,0)))
    Serial.printf("Humidity err: %s\n", fbdo.errorReason().c_str());

  if (!Firebase.RTDB.setString(&fbdo,   "Rain", String(rainValue)))
    Serial.printf("Rain err: %s\n", fbdo.errorReason().c_str());

  if (!Firebase.RTDB.setString(&fbdo, "CO", String(coValue,0)))
    Serial.printf("Co Value err: %s\n", fbdo.errorReason().c_str());

  if (!Firebase.RTDB.setString(&fbdo, "Dust", String(dust,1)))
    Serial.printf("Dust Density err: %s\n", fbdo.errorReason().c_str());
  }
}




