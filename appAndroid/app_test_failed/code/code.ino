// #include <DHT.h>
// #include <Firebase_ESP_Client.h>
// #include <WiFi.h>
 
// #define DHTPIN 14
// #define DHTTYPE DHT22
// #define CO_SENSOR_PIN 34
// #define RAIN_SENSOR_PIN 33
// #define led_rain 22
// #define led_co 23
// #define led_dht22 21
// #define led_sharp 19
// DHT dht(DHTPIN, DHTTYPE);
// const char* ssid = "MyHieuB";
// const char* password = "tamsotam";

// // #define API_KEY "AIzaSyBv5Bw1SRLYYUOsJHK3DuQW8gmCsmKC6Bo"
// // #define DATABASE_URL "https://test-42ef2-default-rtdb.asia-southeast1.firebasedatabase.app"
// // Define Firebase config
// #define API_KEY         "AIzaSyBdYfutaORWiNzbZccND8Rhh0kTIeuj1SQ"
// #define DATABASE_URL    "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app"
// // #define API_KEY         "AIzaSyDUXtnwlte9QCjZXA_7QxoM37_h8xp8Acc"
// // #define DATABASE_URL    "https://datasensor-64cfe-default-rtdb.asia-southeast1.firebasedatabase.app" // URL project in Firebase Homepage 
// #define USER_EMAIL      "nth280102@gmail.com"
// #define USER_PASSWORD   "Matkhau0123@"

// FirebaseData fbdo;
// FirebaseAuth auth;
// FirebaseConfig config;

// Firebase_ESP_Client firebase;

//  //Dust sensor parameter  
//  int measurePin = 32;  
//  int ledPower = 16;  
//  int samplingTime = 280;  
//  int deltaTime = 100;  
//  int sleepTime = 9620;  
    
//  float voMeasured = 0;  
//  float calcVoltage = 0;  
//  float dustDensity = 0.0;  
//  unsigned long voMeasuredTotal = 0;  
//  int voCount = 0; //biến lấy mẫu  
//  // Use the typical sensitivity in units of V per 100ug/m3.  
//  const float K = 0.05;    
//  //Delay time  
//  unsigned long preMillis = 0;  
//  unsigned long sendDataPrevMillis = 0;
//  const long interval = 1000; // 10 seconds = 10 * 1000(=1s)  

// int count = 0;
// bool signupOK = false;
//  void setup() {   
//    Serial.begin(115200); 
//    dht.begin(); 
//   //  config.database_url = DATABASE_URL;
//   //  config.api_key = API_KEY; 
//    pinMode(ledPower,OUTPUT); 
//    pinMode(CO_SENSOR_PIN, INPUT);
//    pinMode(RAIN_SENSOR_PIN, INPUT); 
//    pinMode(led_co, OUTPUT);
//    pinMode(led_rain,OUTPUT);
//    pinMode(led_dht22,OUTPUT);
//    pinMode(led_sharp,OUTPUT);
//    WiFi.begin(ssid, password);
//   Serial.print("Connecting to WiFi...");
//   while (WiFi.status() != WL_CONNECTED) {
//     delay(500);
//     Serial.print(".");
//   }
//   Serial.println("Connected to WiFi!");
//   /* Sign up */
//   // if (Firebase.signUp(&config, &auth, "", "")){
//   //   Serial.println("ok");
//   //   signupOK = true;
//   // }
//   // else{
//   //   Serial.printf("%s\n", config.signer.signupError.message.c_str());
//   // }

//   // /* Assign the callback function for the long running token generation task */
//   // Firebase.begin(&config, &auth);
//   // Firebase.reconnectWiFi(true);
//   config.api_key = API_KEY;
//   config.database_url = DATABASE_URL;

//   auth.user.email = USER_EMAIL;
//   auth.user.password = USER_PASSWORD;

//   firebase.begin(&config, &auth);
//   firebase.reconnectWiFi(true);
//   Serial.println("Initializing connect to Firebase...");

//   int firebaseTries = 0;
//   while(!Firebase.ready()){
//     delay(100);
//     Serial.print('#');
//     firebaseTries++;
//     if(firebaseTries > 100){
//       Serial.println("\nCant't connect! Check config again!");
//       return;
//     } 
//   }
//   Serial.println("\nFirebase is ready!");
//  }  
   
//  void loop() {   
//   // float humidity = dht.readHumidity();
//   // float temperature = dht.readTemperature();
//   // int analogValue = analogRead(CO_SENSOR_PIN);
//   // float coValue = convertToPPM(analogValue);
//   // int rainValue = digitalRead(RAIN_SENSOR_PIN);
//   float humidity = random(0, 101);
//   float temperature = random(0, 101);
//   float coValue = random(300, 1200);
//   float rainValue = (0, 1);
//   //   unsigned long currentMillis = millis();   
//   //   if ( currentMillis - preMillis >= interval ){  
//   //    preMillis = currentMillis;  
//   //  // Xử lý lấy 100 mẫu và tính trung bình  
//   //    voCount = 0;  
//   //    voMeasuredTotal = 0;  
//   //    while (voCount <= 100){  
//   //     digitalWrite(ledPower,LOW);      // Bật IR LED  
//   //     delayMicroseconds(samplingTime);   //Delay 0.28ms  
//   //     voMeasured = analogRead(measurePin); // Đọc giá trị ADC V0 mất khoảng 0.1ms  
//   //     digitalWrite(ledPower,HIGH);     // Tắt LED  
//   //     delayMicroseconds(sleepTime);     //Delay 9.62ms    
//   //     voMeasuredTotal += voMeasured;    // Tính tổng lần lấy mẫu  
//   //     voCount ++;              // Đếm số lần lấy mẫu     
//   //    }  
//   //    voMeasured = 1.0 * voMeasuredTotal/100; //Tính trung bình  
//   //  //****************************  
      
//   //    calcVoltage = voMeasured/1024*5;   //Tính điện áp Vcc của cảm biến (5.0 hoặc 3.3)  
//   //    dustDensity = calcVoltage/K*100.0;
//   //   }
//   float dustDensity = random(10, 101);
//   if (isnan(humidity) || isnan(temperature)) {
//       Serial.println("Failed to read from DHT sensor!");
//   } 
//   else 
//   {

//     Serial.print("Temperature: ");
//     Serial.print(temperature,0);
//     Serial.println(" *C");
//     Serial.print("Humidity: ");
//     Serial.print(humidity,0);
//     Serial.println(" %\t");
//     Serial.print("Rain Value: ");
//     Serial.println(rainValue);
//     Serial.print("CO Value: ");
//     Serial.println(coValue,0);
//     Serial.print("Dust Density: ");
//     Serial.println(dustDensity,1);}
//     Serial.println("---------------------------");  
//   // String temperatureStr = String(temperature);
//   // String humidityStr = String(humidity);
//   // String dustDensityStr = String(dustDensity);
//   // String coValueStr = String(coValue);
//   // String rainValueStr = String(rainValue);
//   //Hien thi trang thai led tuong ung
//   // Kiểm tra nhiệt độ
//   if (temperature > 35) {
//     digitalWrite(led_dht22, HIGH);
//   } else {
//     digitalWrite(led_dht22, LOW);
//   }

//   // Kiểm tra nồng độ CO
//   if (coValue > 800) {
//     digitalWrite(led_co, HIGH);
//   } else {
//     digitalWrite(led_co, LOW);
//   }

//   // Kiểm tra mưa
//   if (rainValue == 0) {
//     digitalWrite(led_rain, HIGH);
//   } else {
//     digitalWrite(led_rain, LOW);
//   }

//   // Kiểm tra nồng độ bụi
//   if (dustDensity > 35.5) {
//     digitalWrite(led_sharp, HIGH);
//   } else {
//     digitalWrite(led_sharp, LOW);
//   }
//   // Chuyển đổi thành chuỗi
//   String dustDensityStr = String(dustDensity,1);
//   String temperatureStr = String(temperature,0);
//   String humidityStr = String(humidity,0);
//   String coValueStr = String(coValue,0);
//   String rainValueStr = String(rainValue);

//   //Send data to Firebase IoT Tram Thoi Tiet
//   Firebase.RTDB.setString(&fbdo, "Temperature", temperatureStr);
//   Firebase.RTDB.setString(&fbdo, "Humidity", humidityStr);
//   Firebase.RTDB.setString(&fbdo, "Rain", rainValueStr);
//   Firebase.RTDB.setString(&fbdo, "Co Value", coValueStr);
//   Firebase.RTDB.setString(&fbdo, "Dust Density", dustDensityStr);

//   delay(10000);
//   }
// //   float convertToPPM(int analogValue) {
// //     // Giả định cảm biến MP135 có output từ 0 đến 5V tương ứng với 0 đến 1000 ppm
// //     return (analogValue * (3.3 / 4096)*200);
// //     // return (analogValue / 4096.0) * 1000.0;
// // }

#include <DHT.h>
#include <Firebase_ESP_Client.h>
#include <WiFi.h>

#define DHTPIN 14
#define DHTTYPE DHT22
#define CO_SENSOR_PIN 34
#define RAIN_SENSOR_PIN 33
#define led_rain 22
#define led_co 23
#define led_dht22 21
#define led_sharp 19
DHT dht(DHTPIN, DHTTYPE);

const char* ssid = "MyHieuB";
const char* password = "tamsotam";

#define API_KEY         "AIzaSyBdYfutaORWiNzbZccND8Rhh0kTIeuj1SQ"
#define DATABASE_URL    "https://autosar01-default-rtdb.asia-southeast1.firebasedatabase.app"
#define USER_EMAIL      "nth280102@gmail.com"
#define USER_PASSWORD   "Matkhau0123@"

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// Dust sensor vars (giữ nguyên nếu cần)
int measurePin = 32;
int ledPower   = 16;

unsigned long lastSend = 0;

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
  int   rainValue   = random(0, 2);       // 0 hoặc 1  (sửa bug (0,1))
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




