bool grabGesture(int *flexion){
  return (flexion[PINKY_IND] + flexion[RING_IND] + flexion[MIDDLE_IND] + flexion[INDEX_IND]) / 4 <= ANALOG_MAX/2 ? 0:1;
}

bool pinchGesture(int *flexion){
  return (flexion[INDEX_IND] + flexion[THUMB_IND]) / 2 <= ANALOG_MAX/2 ? 0:1;
}

bool triggerGesture(int *flexion){
  return flexion[INDEX_IND]<=(ANALOG_MAX/2)?0:1;
}

bool thumbGesture(int *flexion){
  return flexion[THUMB_IND]<=(ANALOG_MAX*FINGER_VALID_PERC)?0:1;
}

bool indexFingerGesture(int *flexion){
  return flexion[INDEX_IND]<=(ANALOG_MAX*FINGER_VALID_PERC)?0:1;
}

bool middleFingerGesture(int *flexion){
  return flexion[MIDDLE_IND]<=(ANALOG_MAX*FINGER_VALID_PERC)?0:1;
}

bool ringFingerGesture(int *flexion){
  return flexion[RING_IND]<=(ANALOG_MAX*FINGER_VALID_PERC)?0:1;
}

bool pinkieFingerGesture(int *flexion){
  return flexion[PINKY_IND]<=(ANALOG_MAX*FINGER_VALID_PERC)?0:1;
}