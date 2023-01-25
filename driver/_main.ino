#define ALWAYS_CALIBRATING CALIBRATION_LOOPS == -1

ICommunication* comm;
int loops = 0;
void setup() {
  #if COMMUNICATION == COMM_SERIAL
    comm = new SerialCommunication();
  #elif COMMUNICATION == COMM_BTSERIAL
    comm = new BTSerialCommunication();
  #endif  
  comm->start();

  setupInputs();

  #if USING_FORCE_FEEDBACK
    setupServoHaptics();  
  #endif
  
}

void loop() {
  if (comm->isOpen()){
    #if USING_CALIB_PIN
    bool calibButton = getButton(PIN_CALIB) != INVERT_CALIB;
    if (calibButton)
      loops = 0;
    #else
    bool calibButton = false;
    #endif
    
    bool calibrate = false;
    if (loops < CALIBRATION_LOOPS || ALWAYS_CALIBRATING){
      calibrate = true;
      loops++;
    }
    
    int* fingerPos = getFingerPositions(calibrate, calibButton);
    bool joyButton = getButton(PIN_JOY_BTN) != INVERT_JOY;

    #if GLOVE_MODE == VR_GLOVE

    #if TRIGGER_GESTURE
    bool triggerButton = triggerGesture(fingerPos);
    #else
    bool triggerButton = getButton(PIN_TRIG_BTN) != INVERT_TRIGGER;
    #endif

    bool aButton = getButton(PIN_A_BTN) != INVERT_A;
    bool bButton = getButton(PIN_B_BTN) != INVERT_B;

    #if GRAB_GESTURE
    bool grabButton = grabGesture(fingerPos);
    #else
    bool grabButton = getButton(PIN_GRAB_BTN) != INVERT_GRAB;
    #endif

    #if PINCH_GESTURE
    bool pinchButton = pinchGesture(fingerPos);
    #else
    bool pinchButton = getButton(PIN_PNCH_BTN) != INVERT_PINCH;
    #endif

    bool menuButton = getButton(PIN_MENU_BTN) != INVERT_MENU;
    
    comm->output(encode(fingerPos, getJoyX(), getJoyY(), joyButton, triggerButton, aButton, bButton, grabButton, pinchButton, calibButton, menuButton));

    #if USING_FORCE_FEEDBACK
      char received[100];
      if (comm->readData(received)){
        int hapticLimits[5];
        decodeData(received, hapticLimits);
        writeServoHaptics(hapticLimits);
      }
    #endif

    #endif // of GLOVE_MODE == VR_GLOVE

    #if GLOVE_MODE == MOBILE_GLOVE

    static int last_gesture = NEUTRAL_GESTURE;
    static int detected_gesture = NEUTRAL_GESTURE;

    if(thumbGesture(fingerPos))
      detected_gesture = THUMB_GESTURE;
    else if(indexFingerGesture(fingerPos))
      detected_gesture = INDEX_GESTURE;
    else if(middleFingerGesture(fingerPos))
      detected_gesture = MIDDLE_GESTURE;
    else if(ringFingerGesture(fingerPos))
      detected_gesture = RING_GESTURE;
    else if(pinkieFingerGesture(fingerPos))
      detected_gesture = PINKIE_GESTURE;
    else if(neutralGesture(fingerPos))
      detected_gesture = NEUTRAL_GESTURE;

    if (detected_gesture != last_gesture)
    {
      comm->output(char(detected_gesture));
      last_gesture = detected_gesture;
    }
    #endif
  }
  delay(LOOP_TIME);
}
