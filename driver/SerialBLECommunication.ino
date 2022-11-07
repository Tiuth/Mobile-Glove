//#if MOBILE_GLOVE

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

class BleServerCallbacks: public BLEServerCallbacks {
    bool* status;
  
    public:
      BleServerCallbacks(bool* stat) {
        status = stat;
      }
  
    void onConnect(BLEServer* pServer) {
      *status = true;
    };

    // start advertising again on disconnect so we can reconnect to the device
    void onDisconnect(BLEServer* pServer) {
      BLEDevice::startAdvertising();
      *status = false;
    }
};

class BTSerialCommunication : public ICommunication {
private:
    bool m_isOpen;
    BLECharacteristic *pCharacteristic;
    
  public:
    BTSerialCommunication() {
      m_isOpen = false;
    }

    bool isOpen(){
      return m_isOpen;
    }

    void start(){
      BLEDevice::init("Phone Glove");
      BLEServer *pServer = BLEDevice::createServer();
      //pass m_isOpen here so it will be set according to the connection status
      pServer->setCallbacks(new BleServerCallbacks(&m_isOpen));
      BLEService *pService = pServer->createService(SERVICE_UUID);
      pCharacteristic = pService->createCharacteristic(
                                              CHARACTERISTIC_UUID,
                                              BLECharacteristic::PROPERTY_READ |
                                              BLECharacteristic::PROPERTY_WRITE |
                                              BLECharacteristic::PROPERTY_NOTIFY |
                                              BLECharacteristic::PROPERTY_INDICATE
                                          );
      //create a Desriptor for notifications with the standard CCCD_UUID
      BLEDescriptor *notifyDescriptor = new BLEDescriptor(CCCD_UUID, 8);
      //need to add write permissions since Android has to write to the descriptor to enable notifications
      notifyDescriptor->setAccessPermissions(ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE);
      pCharacteristic->addDescriptor(notifyDescriptor);
  
      pCharacteristic->setValue(std::toString(UNDEFINED_GESTURE));
      pService->start();
      // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
      BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
      pAdvertising->addServiceUUID(SERVICE_UUID);
      pAdvertising->setScanResponse(true);
      pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
      pAdvertising->setMinPreferred(0x12);
      BLEDevice::startAdvertising();
    }

    void output(char* data){
      pCharacteristic->setValue(data);
      pCharacteristic->notify();
    }

    bool readData(char* input){
      return pCharacteristic->getValue()
    }
};