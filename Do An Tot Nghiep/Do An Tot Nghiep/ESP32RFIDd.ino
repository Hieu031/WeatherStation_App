#define BLYNK_TEMPLATE_ID "TMPL6SedSAdLZ"
#define BLYNK_TEMPLATE_NAME "Smart LockDoor"
#define BLYNK_AUTH_TOKEN "ImqXQkGSDnIULq_jmO3DrEr0ST4pp1fb"

#include <WiFi.h>
#include <BlynkSimpleEsp32.h>
#include <SPI.h>
#include <MFRC522.h>
#include <EEPROM.h>
#include <Keypad.h>
#include <LiquidCrystal_I2C.h>
// Định nghĩa chân kết nối RFID và khóa cửa
#define SS_PIN  5  // Chân SDA
#define RST_PIN 27  // Chân RST
#define LOCK_PIN 13 // Chân D13 điều khiển khóa cửa
#define buzzer 17
#define button 25
// Cấu hình Keypad 4x4
const byte ROWS = 4; // 4 hàng
const byte COLS = 3; // 4 cột
char keys[ROWS][COLS] = {
  {'1', '2', '3'},
  {'4', '5', '6'},
  {'7', '8', '9'},
  {'*', '0', '#'}
};
byte rowPins[ROWS] = {33, 32, 27, 16};  // Kết nối các chân hàng của keypad
byte colPins[COLS] = {15, 14, 12};  // Kết nối các chân cột của keypad

LiquidCrystal_I2C lcd(0x27, 16, 2);
MFRC522 mfrc522(SS_PIN, RST_PIN);
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);
// Thông tin WiFi
char auth[] = BLYNK_AUTH_TOKEN;
char ssid[] = "ZET Friday";
char pass[] = "Zet8888@";
// Biến lưu trữ mật khẩu
String currentPassword = "8888";
String inputPassword = "";
String newPassword = "";
String oldPassword = "";
bool changePasswordMode = false;
bool enterPasswordMode = false;
bool confirmNewPasswordMode = false;
unsigned long pressStartTime = 0; // Biến lưu thời gian bắt đầu nhấn phím
bool isHashPressed = false;       // Kiểm tra trạng thái phím #
bool resetPasswordMode = false;
String defaultPassword = "8888";
// Chân ảo của Blynk
#define V10_TERMINAL V10
#define V11_TERMINAL V11
#define V12_INPUT_TEXT V12
#define V13_BUTTON_ADD V13
#define V14_BUTTON_REMOVE V14
#define V15_BUTTON_CLEAR_ALL V15
#define V16_DOOR_STATE V16
#define V17_BUTTON_CLEAR_HISTORY V17
#define V18_BUTTON_OPEN_DOOR V18
#define V19_INPUT_TEXT V19

// Biến lưu tên người dùng
String inputName = "";
String deleteName = "";
bool verifyOldPasswordMode = false; 
// Biến trạng thái thêm và xóa thẻ
bool addCardMode = false;
bool removeCardMode = false;

// Biến lưu trạng thái khóa cửa
bool doorOpen = false;

// Biến đếm số lần quẹt thẻ chưa đăng ký
int unregisteredSwipeCount = 0;

// Thời gian bắt đầu chặn quẹt thẻ
unsigned long blockSwipeStartTime = 0;

// Khởi tạo Terminal widget
WidgetTerminal terminal(V10_TERMINAL);
WidgetTerminal terminalList(V11_TERMINAL);

void setup() {
  // Khởi tạo Serial Monitor
  Serial.begin(115200);
  // Khởi tạo kết nối WiFi
  Blynk.begin(auth, ssid, pass);
  // Khởi tạo SPI bus
  SPI.begin();
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("SMART LOCK HAUI");
  lcd.setCursor(0,1);
  lcd.print("PRESS*ENTER PASS");
  // Khởi tạo module RFID
  mfrc522.PCD_Init();
  // Khởi tạo EEPROM
  EEPROM.begin(512);
  // Khởi tạo chân khóa cửa
  pinMode(button, INPUT_PULLUP);
  pinMode(LOCK_PIN, OUTPUT);
  pinMode(buzzer, OUTPUT);
  digitalWrite(LOCK_PIN, LOW); // Khóa cửa mặc định tắt

  // Hiển thị các thẻ đã đăng ký
  displayRegisteredCards();
}

void loop() {
  Blynk.run();
  // Kiểm tra nếu người dùng nhập bằng keypad
  char key = keypad.getKey();
  if (key) {
    handleKeypadInput(key);
  }
  if (digitalRead(button) == LOW) { // Nếu nút được nhấn (trạng thái LOW do pull-up)
    tone(buzzer, 2000, 200);
    openDoor();
  }
  // Kiểm tra thời gian chặn quẹt thẻ
  if (blockSwipeStartTime != 0 && millis() - blockSwipeStartTime >= 10000) {
    blockSwipeStartTime = 0;
    unregisteredSwipeCount = 0;
  }

  // Kiểm tra thẻ RFID
  if (blockSwipeStartTime == 0 && mfrc522.PICC_IsNewCardPresent() && mfrc522.PICC_ReadCardSerial()) {
    unsigned long startTime = millis();
    
    String cardUID = "";
    for (byte i = 0; i < mfrc522.uid.size; i++) {
      cardUID += String(mfrc522.uid.uidByte[i] < 0x10 ? "0" : "");
      cardUID += String(mfrc522.uid.uidByte[i], HEX);
    }
    cardUID.toUpperCase();

    // Hiển thị UID thẻ lên Terminal
    terminal.println("Thẻ được quẹt: " + cardUID);
    terminal.flush();
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("THE DA QUET");
    delay(500);

    // Buzzer kêu beep 0.2 giây
    tone(buzzer, 2000, 200);

    if (addCardMode) {
addCard(cardUID);
      addCardMode = false;
    } else if (removeCardMode) {
      removeCard(cardUID);
      removeCardMode = false;
    } else {
      // Kiểm tra và xử lý thẻ quẹt
      handleCard(cardUID);
    }

    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();

    unsigned long endTime = millis();
    terminal.println("--------------------");
    terminal.flush();
  }
}
void showMainMenu() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("SMART LOCK HAUI");
  lcd.setCursor(0, 1);
  lcd.print("PRESS*ENTER PASS");
}

void handleKeypadInput(char key) {
  if (key == '*') {
    // Khi người dùng nhấn *, kích hoạt chế độ nhập mật khẩu
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("NHAP PASSWORD:");
    enterPasswordMode = true;
    inputPassword = "";
  } else if (enterPasswordMode) {
    if (key == '#') {
      tone(buzzer, 5000, 200);
      // Khi người dùng nhấn # để kết thúc nhập mật khẩu
      if (inputPassword == currentPassword) {
        // Mật khẩu đúng, mở cửa
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("PASSWORD CORRECT");
        terminal.println("Cửa đã được mở bằng mật khẩu");
        tone(buzzer, 5000, 200);
        openDoor();
        delay(2000);  
        showMainMenu();  
      }
      else if (inputPassword == "1414") {
        // Nhập mã "1414" để reset mật khẩu
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("SECRET PASS:");
        resetPasswordMode = true;  // Kích hoạt chế độ reset mật khẩu
        inputPassword = "";
      }
       else if (inputPassword == "6868") {
        // Nhập mã "6868" để đổi mật khẩu
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("ENTER OLD PASS:");
        verifyOldPasswordMode = true;  // Kích hoạt chế độ kiểm tra mật khẩu cũ
        inputPassword = "";
      } else {
        // Mật khẩu sai
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("PASS INCORRECT!");
        terminal.println("Mật khẩu bị nhập sai");
        tone(buzzer, 5000, 200);
        delay(2000);  
        showMainMenu();  
      }
      enterPasswordMode = false;
    } else {
      // Thêm ký tự vào mật khẩu
      inputPassword += key;
      lcd.setCursor(0, 1);
      lcd.print(inputPassword);
    }
  } else if (resetPasswordMode) {
      if (key == '#') {
        if (inputPassword == "22555588") {
              // Mật khẩu đúng, reset hệ thống về mặc định
              currentPassword = defaultPassword;
              lcd.clear();
              lcd.setCursor(0, 0);
              lcd.print("SYSTEM RESET");
              terminal.println("Mật khẩu đã được reset về mặc định.");
              tone(buzzer, 5000, 200);
              delay(2000);
              showMainMenu();
          } else {
              // Mật khẩu không đúng
              lcd.clear();
              lcd.setCursor(0, 0);
              lcd.print("PASS INCORRECT!");
              terminal.println("Mật khẩu nhập sai.");
              tone(buzzer, 5000, 200);
              delay(2000);
              showMainMenu();
            }
            resetPasswordMode = false;
          }
        else {
        // Thêm ký tự vào mật khẩu cũ
          inputPassword += key;
          lcd.setCursor(0, 1);
          lcd.print(inputPassword);
        }}
  else if (verifyOldPasswordMode) {
    if (key == '#') {
      // Khi người dùng nhấn # để kết thúc nhập mật khẩu cũ
      if (inputPassword == currentPassword) {
        // Mật khẩu cũ đúng, cho phép đổi mật khẩu
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("ENTER NEW PASS:");
        changePasswordMode = true;
        verifyOldPasswordMode = false;  // Tắt chế độ kiểm tra mật khẩu cũ
        inputPassword = "";
      } else {
        // Mật khẩu cũ không đúng
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("OLD PASS WRONG!");
        terminal.println("Mật khẩu cũ không khớp");
        tone(buzzer, 5000, 200);
        delay(2000);
        showMainMenu();
        verifyOldPasswordMode = false;  // Tắt chế độ kiểm tra mật khẩu cũ
      }
    } else {
      // Thêm ký tự vào mật khẩu cũ
      inputPassword += key;
      lcd.setCursor(0, 1);
      lcd.print(inputPassword);
    }
  } else if (changePasswordMode) {
    if (key == '#') {
      // Khi người dùng nhấn # để kết thúc nhập mật khẩu mới
      currentPassword = inputPassword;
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("PASSWORD CHANGED");
      terminal.println("Mật khẩu đã được thay đổi");
      terminal.println("Mật khẩu mới: " + inputPassword);
      tone(buzzer, 5000, 200);
      delay(2000);  
      showMainMenu();  
      changePasswordMode = false;
    } else {
      // Thêm ký tự vào mật khẩu mới
      inputPassword += key;
      lcd.setCursor(0, 1);
      lcd.print(inputPassword);
    }
  }
  
}
void handleCard(String cardUID) {
  // Tìm thẻ trong EEPROM
  int address = findCardAddress(cardUID);
  if (address == -1) {
    terminal.println("Thẻ chưa được đăng ký.");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("UNREGISTER CARD");
    tone(buzzer, 5000, 200);
    delay(2000);  
    showMainMenu();  
    digitalWrite(LOCK_PIN, LOW); // Tắt khóa cửa

    unregisteredSwipeCount++;
    if (unregisteredSwipeCount >= 3) {
      // Buzzer kêu beep cảnh báo trong 3 giây
      tone(buzzer, 5000, 5000);

      // Chặn quẹt thẻ trong 10 giây
      blockSwipeStartTime = millis();
      terminal.println("Cảnh báo cố tình xâm nhập khóa 10 giây");
      for (int i = 10; i >= 0; i--) {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("BLOCKED ");
        lcd.print(i);
        lcd.print("s");
        delay(1000);}
      showMainMenu();  
    }
  } else {
    terminal.println("Thẻ đã đăng ký: " + readNameFromEEPROM(address));
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("CARD UID: "+ readNameFromEEPROM(address));
    tone(buzzer, 5000, 200);
    delay(1000);
    openDoor(); // Mở khóa cửa
    unregisteredSwipeCount = 0; // Reset số lần quẹt thẻ không hợp lệ
  }
  terminal.flush();
}

void openDoor() {
  digitalWrite(LOCK_PIN, HIGH); // Mở khóa cửa
  doorOpen = true;
  Blynk.virtualWrite(V16_DOOR_STATE, 1); // Đồng bộ trạng thái cửa với chân ảo V16
  for (int i = 3; i >= 0; i--) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("DOOR OPENDED");
    tone(buzzer, 5000, 200);
    lcd.setCursor(0,1);
    lcd.print("CLOSE IN: ");
    lcd.print(i);
    lcd.print("s");
    delay(1000);}
   // Để khóa cửa mở trong 3 giây  
  showMainMenu();  
  closeDoor(); // Đóng khóa cửa
}

void closeDoor() {
  digitalWrite(LOCK_PIN, LOW); // Tắt khóa cửa
  doorOpen = false;
  Blynk.virtualWrite(V16_DOOR_STATE, 0); // Đồng bộ trạng thái cửa với chân ảo V16
}

int findCardAddress(String cardUID) {
  for (int i = 0; i < EEPROM.length(); i += 40) {
    if (readUIDFromEEPROM(i) == cardUID) {
      return i;
    }
  }
  return -1;
}

String readUIDFromEEPROM(int address) {
  String uid = "";
  for (int i = 0; i < 10; i++) {
    char readChar = EEPROM.read(address + i);
    if (readChar != 255) {
      uid += readChar;
    }
  }
  return uid;
}

String readNameFromEEPROM(int address) {
  String name = "";
  for (int i = 10; i < 40; i++) {
    char readChar = EEPROM.read(address + i);
    if (readChar != 255) {
      name += readChar;
    }
  }
  return name;
}

BLYNK_WRITE(V12_INPUT_TEXT) {
  inputName = param.asString();
}

BLYNK_WRITE(V19_INPUT_TEXT) {
  deleteName = param.asString();
}

BLYNK_WRITE(V13_BUTTON_ADD) {
  if (param.asInt() == 1) { // Kiểm tra nếu nút V13 được nhấn
    if (inputName.length() > 0) {
      terminal.println("Nhấn nút và chờ thẻ để thêm với tên: " + inputName);
      lcd.clear();
      lcd.setCursor(0, 0);
     lcd.print("ADD CARD....");
     tone(buzzer, 5000, 200);
      terminal.flush();
      addCardMode = true;
    } else {
      terminal.println("Vui lòng nhập tên.");
      terminal.flush();
    }
  }
}

void addCard(String cardUID) {
  // Kiểm tra nếu thẻ đã tồn tại
  int address = findCardAddress(cardUID);
  if (address != -1) {
    terminal.println("Thẻ đã tồn tại: " + readNameFromEEPROM(address));
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("EXISTED CARD!");
  } else {
    // Lưu UID và tên vào EEPROM
    saveCardToEEPROM(cardUID, inputName);
    terminal.println("Thẻ " + cardUID + " đã được lưu với tên " + inputName);
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("SAVED CARD!");
    displayRegisteredCards(); // Cập nhật danh sách thẻ đã đăng ký
  }
  terminal.flush();
  delay(2000); 
  showMainMenu(); 
}

void saveCardToEEPROM(String cardUID, String name) {
  int address = findFreeEEPROMAddress();
  for (int i = 0; i < 10; i++) {
    EEPROM.write(address + i, i < cardUID.length() ? cardUID[i] : 255);
  }
  for (int i = 10; i < 40; i++) {
    EEPROM.write(address + i, (i - 10 < name.length()) ? name[i - 10] : 255);
  }
  EEPROM.commit();
}

int findFreeEEPROMAddress() {
  for (int i = 0; i < EEPROM.length(); i += 40) {
    if (EEPROM.read(i) == 255) {
      return i;
    }
  }
  return -1;
}

BLYNK_WRITE(V14_BUTTON_REMOVE) {
  if (param.asInt() == 1) { // Kiểm tra nếu nút V14 được nhấn
    if (deleteName.length() > 0) {
      terminal.println("Xóa thẻ có tên: " + deleteName);
      deleteCardByName(deleteName);
    } else {
      terminal.println("Nhấn nút và chờ thẻ để xóa.");
      terminal.flush();
      removeCardMode = true;
    }
  }
}

void deleteCardByName(String name) {
  for (int i = 0; i < EEPROM.length(); i += 40) {
    if (readNameFromEEPROM(i) == name) {
      for (int j = 0; j < 40; j++) {
        EEPROM.write(i + j, 255);
      }
      EEPROM.commit();
      terminal.println("Thẻ có tên " + name + " đã được xóa.");
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("CARD" + name + "DELETED !");
      delay(2000); 
      showMainMenu(); 
      displayRegisteredCards(); // Cập nhật danh sách thẻ đã đăng ký
      terminal.flush();
      return;
    }
  }
  terminal.println("Không tìm thấy thẻ có tên " + name + ".");
  terminal.flush();
}

void removeCard(String cardUID) {
  // Xóa thẻ từ EEPROM
  int address = findCardAddress(cardUID);
  if (address != -1) {
    for (int i = 0; i < 40; i++) {
      EEPROM.write(address + i, 255);
    }
    EEPROM.commit();
    terminal.println("Thẻ " + cardUID + " đã được xóa.");
    displayRegisteredCards(); // Cập nhật danh sách thẻ đã đăng ký
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("CARD REMOVED");
  } else {
    terminal.println("Thẻ không tồn tại trong hệ thống.");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("CARD NOT FOUND");
  }
  terminal.flush();
  delay(2000); 
  showMainMenu(); 
}

void displayRegisteredCards() {
  terminalList.clear();
  terminalList.println("Danh sách thẻ đã đăng ký:");
  String registeredUIDs[EEPROM.length() / 40];
  int uidCount = 0;

  for (int i = 0; i < EEPROM.length(); i += 40) {
    if (EEPROM.read(i) != 255) {
      String uid = readUIDFromEEPROM(i);
      bool isDuplicate = false;

      for (int j = 0; j < uidCount; j++) {
        if (registeredUIDs[j] == uid) {
          isDuplicate = true;
          break;
        }
      }

      if (!isDuplicate) {
        registeredUIDs[uidCount] = uid;
        uidCount++;
        String name = readNameFromEEPROM(i);
        terminalList.println("Thẻ: " + uid + " - Tên: " + name);
      }
}
  }
  terminalList.flush();
}

// Xóa toàn bộ danh sách thẻ đã đăng ký khi nhấn nút V15
BLYNK_WRITE(V15_BUTTON_CLEAR_ALL) {
  if (param.asInt() == 1) { // Kiểm tra nếu nút V15 được nhấn
    for (int i = 0; i < EEPROM.length(); i++) {
      EEPROM.write(i, 255);
    }
    EEPROM.commit();
    terminal.println("Tất cả thẻ đã được xóa.");
    displayRegisteredCards();
    terminal.flush();
  }
}

// Xóa toàn bộ lịch sử ở Terminal V10 khi nhấn nút V17
BLYNK_WRITE(V17_BUTTON_CLEAR_HISTORY) {
  if (param.asInt() == 1) { // Kiểm tra nếu nút V17 được nhấn
    terminal.clear();
    terminal.println("Lịch sử đã được xóa.");
    terminal.flush();
  }
}

// Mở cửa trong 3 giây rồi đóng khi nhấn nút V18
BLYNK_WRITE(V18_BUTTON_OPEN_DOOR) {
  if (param.asInt() == 1) { // Kiểm tra nếu nút V18 được nhấn
    openDoor(); // Mở khóa cửa
    terminal.println("Cửa đã mở");
    terminal.flush();
  }
}