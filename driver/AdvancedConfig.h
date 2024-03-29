//Advanced settings, only for the pros XD

#define LOOP_TIME 4 //How much time between data sends (ms), set to 0 for a good time :)
#define CALIBRATION_LOOPS -1//How many loops should be calibrated. Set to -1 to always be calibrated.

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CCCD_UUID "00002902-0000-1000-8000-00805f9b34fb"

//gesture defines
#define NEUTRAL_GESTURE 99
#define THUMB_GESTURE 0
#define INDEX_GESTURE 1
#define MIDDLE_GESTURE 2
#define RING_GESTURE 3
#define PINKIE_GESTURE 4

#define FINGER_PINCHED_PERC 0.8
#define FINGER_STRETCHED_PERC 0.5

//Mode defines
#define VR_GLOVE 0
#define MOBILE_GLOVE 1

//Comm defines, no touchy
#define COMM_SERIAL 0   
#define COMM_BTSERIAL 1 

//Encoding
#define ENCODING 1
#define ENCODE_LEGACY 0
#define ENCODE_ALPHA  1

//Finger indeces (not used for legacy)
#define PINKY_IND 4
#define RING_IND 3
#define MIDDLE_IND 2
#define INDEX_IND 1
#define THUMB_IND 0

//Automatically set ANALOG_MAX depending on the microcontroller
#if defined(__AVR__)
#define ANALOG_MAX 1023
#elif defined(ESP32)
#define ANALOG_MAX 4095 
#endif


//ANALOG_MAX OVERRIDE:
//uncomment and set as needed (only touch if you know what you are doing)
//#define ANALOG_MAX 4095 

#ifndef ANALOG_MAX
#error "This board doesn't have an auto ANALOG_MAX assignment, please set it manually by uncommenting ANALOG_MAX OVERRIDE!"
#endif

//Filtering and clamping analog inputs
#define CLAMP_ANALOG_MAP true //clamp the mapped analog values from 0 to ANALOG_MAX

// Enable and set min and max to match your sensor's expected raw value range
// This discards any spurious values outside of the useful range
#define CLAMP_FLEXION false  //clamp the raw flexion values
#define CLAMP_MIN 0  //the minimum value from the flexion sensors
#define CLAMP_MAX ANALOG_MAX  //the maximum value from the flexion sensors

// You must install RunningMedian library to use this feature
// https://www.arduino.cc/reference/en/libraries/runningmedian/
#define ENABLE_MEDIAN_FILTER false //use the median of the previous values, helps reduce noise
#define MEDIAN_SAMPLES 20
