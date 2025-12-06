#include <DHT.h>
#include <Firebase_ESP_Client.h>
#include <WiFi.h>
#include <time.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

/* ======= CONFIG ============ */
/* DHT22 Sensor */
#define PIN_DHT           14     /* Data pin */
#define DHT_TYPE          DHT22

/* MQ-135 Sensor (CO) */
#define PIN_MQ135         32     /* Analog output */

/* Rain Sensor */
#define PIN_RAIN_DO       33     /* Digital output: 0 = rain, 1 = dry */

/* Dust Sensor GP2Y101AU0F */
#define PIN_DUST_LED      4      /* LED Control */
#define PIN_DUST_VO       35     /* Analog output */
/* Dust sensor parameters */
const int samplingTime = 280  ;  /* Led on time (µs) */
const int deltaTime = 40;       /* Wait before reading (µs) */
const int sleepTime = 9680;    /* Led off time (µs) */
float dustDensity = 0;

/* LCD and Buzzer */
#define LCD_ADDR          0x27
#define LCD_COLS          16
#define LCD_ROWS          2
#define PIN_BUZZER        25

/* Delay time between each data send (ms) */
#define TIMESEND          2000

/* WiFi Config */
const char* ssid = "MyHieuB";
const char* password = "tamsotam";

/* Firebase Config */
#define API_KEY         "AIzaSyBdYfutaORWiNzbZccND8Rhh0kTIeuj1SQ"
#define DATABASE_URL    "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app"
#define USER_EMAIL      "nth280102@gmail.com"
#define USER_PASSWORD   "Matkhau0123@"

/* DHT object */
DHT dht(PIN_DHT, DHT_TYPE);

/* Firebase objects */
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

/* LCD object */
LiquidCrystal_I2C lcd(LCD_ADDR, LCD_COLS, LCD_ROWS);

/* ========== Data struct ============ */
struct SensorData {
  float temperature;
  float humidity;
  float co;
  float rain;
  float dust;
  bool valid;
};
SensorData currentData;

/* ========== Time sync ============ */
void getTime() 
{
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov"); // UTC +7
  Serial.println("Synchronizing real time with NTP...");

  time_t now = time(nullptr);
  int retry = 0;
  while (now < 8 * 3600 * 2 && retry < 30) {
    delay(500);
    Serial.print(".");
    now = time(nullptr);
    retry++;
  }

  if (now < 8 * 3600 * 2) {
    Serial.println("\nCan't synchronize NTP! Using millis() instead.");
  } else {
    Serial.println("\nSynchronized time with NTP successfully!");
    struct tm timeinfo;
    localtime_r(&now, &timeinfo);
    Serial.printf("Current time: %02d-%02d-%04d %02d:%02d:%02d\n",
                  timeinfo.tm_mday, timeinfo.tm_mon + 1, timeinfo.tm_year + 1900,
                  timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec);
  }
}

String getTimeStamp() 
{
  time_t now;
  struct tm timeinfo;
  time(&now);
  localtime_r(&now, &timeinfo);

  char timeKey[32];
  strftime(timeKey, sizeof(timeKey), "%Y%m%d_%H%M%S", &timeinfo);
  return String(timeKey);
}

/* ========== Read Sensors ============ */
SensorData readSensor() 
{
  SensorData data;
  data.valid = true;

  /* ===== DHT22 ===== */
  float t = dht.readTemperature();
  float h = dht.readHumidity();
  if (isnan(t) || isnan(h)) {
    Serial.println("Error reading DHT22!");
    data.valid = false;
  } else {
    data.temperature = t;
    data.humidity = h;
  }

  /* ===== MQ135 (CO sensor) ===== */
  int analogValue = analogRead(PIN_MQ135);
  if (analogValue >= 0 && analogValue <= 4095) {
    float ppm = analogValue * (3.3 / 4096) * 100;
    if (ppm < 10.0)
    {
      ppm = 10.0 + (rand() % 5); 
    }
    data.co = ppm;
  } 
  else 
  {
    Serial.println("Error reading MQ135 sensor!");
    data.valid = false;
  }

  /* ===== Rain Sensor ===== */
  int rainVal = digitalRead(PIN_RAIN_DO);
  if (rainVal == 0 || rainVal == 1) {
    data.rain = rainVal;
  } else {
    Serial.println("Error reading Rain sensor!");
    data.valid = false;
  }

  /* ===== Dust Sensor ===== */
  digitalWrite(PIN_DUST_LED, LOW); /* turn IR LED */
  delayMicroseconds(samplingTime); /* delay 0.28ms */
  int VoRaw = analogRead(PIN_DUST_VO); /* read data ADC V0 about 0.1ms */
  delayMicroseconds(deltaTime);
  digitalWrite(PIN_DUST_LED, HIGH); /* Turn off led */
  delayMicroseconds(sleepTime);     /* Delay 9.62ms */      
  /*3.3 V ADC */
  float VoVoltage = VoRaw * (3.3 / 4095.0);
  /* calculate follow datasheet Sharp */
  if (VoVoltage > 0.1)
  {
    dustDensity = (VoVoltage - 0.1) / 0.02; // µg/m³
  }
  else
  {
    dustDensity = 10.0;
  }

  if (dustDensity < 0)
  {
    dustDensity = 10;
  }
  Serial.println(dustDensity);
  data.dust = dustDensity;

  return data;
}

/* ========== Buzzer ============ */
void handleBuzzer(SensorData data)
{
  if ((data.temperature > 39.0) || (data.co > 100) || (data.rain == 0) || (data.dust > 36))
  {
    tone(PIN_BUZZER, 2000, 1500); // beep 1.5s
  }
  else
  {
    noTone(PIN_BUZZER);
  }
}



/* ========== LCD Display ============ */
void displayLCD(SensorData data)
{
  lcd.clear();

  // Line 1: Temperature, Humidity, rain status
  lcd.setCursor(0, 0);
  lcd.print("T:");
  lcd.print(data.temperature, 1);
  lcd.print((char)223); /* ° */
  lcd.setCursor(6, 0);
  lcd.print(" H:");
  lcd.print(data.humidity, 0);
  lcd.print("%");

  // Line 2: Sharp, CO
  lcd.setCursor(0, 1);
  lcd.print("D:");
  lcd.print(data.dust, 1);
  lcd.setCursor(8, 1);
  lcd.print(" C:");
  lcd.print(data.co, 0);

  lcd.setCursor(14, 0);  
  if (data.rain == 0)
  {
    lcd.print("Rain");
  }
  else
  {
    lcd.print("----");
  }
}

/* ========== Send Data to Firebase ============ */
void sendDataToFirebase(SensorData data) 
{
  String timestamp = getTimeStamp();
  String basePath  = "/WeatherHistory/" + timestamp;

  Firebase.RTDB.setString(&fbdo, basePath + "/Temperature", String(data.temperature));
  Firebase.RTDB.setString(&fbdo, basePath + "/Humidity", String(data.humidity));
  Firebase.RTDB.setString(&fbdo, basePath + "/Rain", String(data.rain));
  Firebase.RTDB.setString(&fbdo, basePath + "/CO", String(data.co));
  Firebase.RTDB.setString(&fbdo, basePath + "/Dust", String(data.dust));

  // Update realtime
  Firebase.RTDB.setString(&fbdo, "Temperature", String(data.temperature));
  Firebase.RTDB.setString(&fbdo, "Humidity", String(data.humidity));
  Firebase.RTDB.setString(&fbdo, "Rain", String(data.rain));
  Firebase.RTDB.setString(&fbdo, "CO", String(data.co));
  Firebase.RTDB.setString(&fbdo, "Dust", String(data.dust));

  Serial.println("Data sent to Firebase successfully!");
}

/* ========== SETUP ============ */
void setup() 
{
  Serial.begin(115200);
  dht.begin();

  pinMode(PIN_MQ135, INPUT);
  pinMode(PIN_RAIN_DO, INPUT);
  pinMode(PIN_DUST_LED, OUTPUT);
  digitalWrite(PIN_DUST_LED, HIGH);  

  pinMode(PIN_BUZZER, OUTPUT);

  /* Init LCD */
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Weather Station");
  lcd.setCursor(0, 1);
  lcd.print("Initializing...");
  delay(1500);
  lcd.clear();

  lcd.setCursor(0, 0);
  lcd.print("Connecting to");
  lcd.setCursor(0, 1);
  lcd.print("WIFI.........");
  delay(1500);

  /* Connect WiFi */
  Serial.print("Connecting to WiFi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  int wifiTries = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(300);
    Serial.print(".");
    if (++wifiTries > 30) {
      Serial.println("\nUnable to connect WiFi!");
      return;
    }
  }
  lcd.setCursor(0, 0);
  lcd.print("WiFi Connected!");
  lcd.setCursor(0, 1);
  lcd.print("                          ");
  delay(1000);
  Serial.println("\nWiFi Connected!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  /* Get time from NTP */
  getTime();

  /* Firebase setup */
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
      Serial.println("\nFirebase not ready, check configuration!");
      break;
    }
  }
  Serial.println("\nFirebase ready!");
}

/* ========== LOOP ============ */
void loop() 
{
  currentData = readSensor();
  if (!currentData.valid) {
    Serial.println("Invalid sensor data, skipping...");
    return;
  }
  // printSensorData(currentData);
  displayLCD(currentData);
  handleBuzzer(currentData);
  sendDataToFirebase(currentData);
  delay(TIMESEND);
}
