/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
    Ported to Arduino ESP32 by Evandro Copercini
    updates by chegewara
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CCCD_UUID "00002902-0000-1000-8000-00805f9b34fb"

char value = '0';
BLECharacteristic *pCharacteristic;

class BleServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
    };

    void onDisconnect(BLEServer* pServer) {
      BLEDevice::startAdvertising();
    }
};

void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");

  BLEDevice::init("Phone Glove");
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new BleServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_NOTIFY |
                                         BLECharacteristic::PROPERTY_INDICATE
                                       );
  BLEDescriptor *notifyDescriptor = new BLEDescriptor(CCCD_UUID, 8);
  notifyDescriptor->setAccessPermissions(ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE);
  pCharacteristic->addDescriptor(notifyDescriptor);

  pCharacteristic->setValue(&value);
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  // put your main code here, to run repeatedly:
  delay(2000);
  if (++value > '9') 
    value = '0';
  pCharacteristic->setValue(&value);
  pCharacteristic->notify();
}
