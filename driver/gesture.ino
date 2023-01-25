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
  if (flexion[THUMB_IND]  >= ANALOG_MAX*FINGER_PINCHED_PERC &&
      flexion[INDEX_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[MIDDLE_IND] <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[RING_IND]   <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[PINKY_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC)
  {
    return 1;
  }
  return 0;
}

bool indexFingerGesture(int *flexion){
  if (flexion[THUMB_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[INDEX_IND]  >= ANALOG_MAX*FINGER_PINCHED_PERC &&
      flexion[MIDDLE_IND] <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[RING_IND]   <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[PINKY_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC)
  {
    return 1;
  }
  return 0;
}

bool middleFingerGesture(int *flexion){
  if (flexion[THUMB_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[INDEX_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[MIDDLE_IND] >= ANALOG_MAX*FINGER_PINCHED_PERC &&
      flexion[RING_IND]   <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[PINKY_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC)
  {
    return 1;
  }
  return 0;
}

bool ringFingerGesture(int *flexion){
  if (flexion[THUMB_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[INDEX_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[MIDDLE_IND] <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[RING_IND]   >= ANALOG_MAX*FINGER_PINCHED_PERC &&
      flexion[PINKY_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC)
  {
    return 1;
  }
  return 0;
}

bool pinkieFingerGesture(int *flexion){
  if (flexion[THUMB_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[INDEX_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[MIDDLE_IND] <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[RING_IND]   <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[PINKY_IND]  >= ANALOG_MAX*FINGER_PINCHED_PERC)
  {
    return 1;
  }
  return 0;
}

bool neutralGesture(int *flexion){
  if (flexion[THUMB_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[INDEX_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[MIDDLE_IND] <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[RING_IND]   <= ANALOG_MAX*FINGER_STRETCHED_PERC &&
      flexion[PINKY_IND]  <= ANALOG_MAX*FINGER_STRETCHED_PERC)
  {
    return 1;
  }
  return 0;
}