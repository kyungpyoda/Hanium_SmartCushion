int En0 = 7;  //  Low enabled
int En1 = 6;  //  Low enabled


int S0  = 5;
int S1  = 4;
int S2  = 3;
int S3  = 2;


int SIG_pin = A3;

int LED_pin = 8;
int sensorNum[35]={
  10,12,14,98,16,18,20,99,
  5,6,7,8,9,11,13,15,17,19,21,22,23,24,25,99,
  0,1,2,3,4,98,26,27,28,29,30
}; 
int tempArr[31] = {0,};


void setup() {
  Serial.begin(115200);
  pinMode(En0, OUTPUT);
  pinMode(En1, OUTPUT);

  pinMode(S0, OUTPUT);
  pinMode(S1, OUTPUT);
  pinMode(S2, OUTPUT);
  pinMode(S3, OUTPUT);

  pinMode(LED_pin,OUTPUT);
}

void loop() {
  int j = 0;
  for (int i=0; i<35; i++){
    if(sensorNum[i]==98){
      Serial.print("  ");
    }else if(sensorNum[i]==99){
      Serial.println("");
    }else{
      Serial.print("[");
      Serial.print(readMux(sensorNum[i]));
      Serial.print("]");
      tempArr[++j] = readMux(sensorNum[i]);
    }
    delay(1);
  }
  Serial.println("");
  Serial.println("========================================================");
  Serial.println("");
  digitalWrite(LED_pin,LOW);
  delay(500);
  digitalWrite(LED_pin,HIGH);
}


int readMux(int channel) {
  int controlPin[] = {S0, S1, S2, S3, En0, En1};

  int muxChannel[32][6] = {
    {0, 0, 0, 0, 0, 1}, //channel 0
    {0, 0, 0, 1, 0, 1}, //channel 1
    {0, 0, 1, 0, 0, 1}, //channel 2
    {0, 0, 1, 1, 0, 1}, //channel 3
    {0, 1, 0, 0, 0, 1}, //channel 4
    {0, 1, 0, 1, 0, 1}, //channel 5
    {0, 1, 1, 0, 0, 1}, //channel 6
    {0, 1, 1, 1, 0, 1}, //channel 7
    {1, 0, 0, 0, 0, 1}, //channel 8
    {1, 0, 0, 1, 0, 1}, //channel 9
    {1, 0, 1, 0, 0, 1}, //channel 10
    {1, 0, 1, 1, 0, 1}, //channel 11
    {1, 1, 0, 0, 0, 1}, //channel 12
    {1, 1, 0, 1, 0, 1}, //channel 13
    {1, 1, 1, 0, 0, 1}, //channel 14
    {1, 1, 1, 1, 0, 1}, //channel 15
    {0, 0, 0, 0, 1, 0}, //channel 16
    {0, 0, 0, 1, 1, 0}, //channel 17
    {0, 0, 1, 0, 1, 0}, //channel 18
    {0, 0, 1, 1, 1, 0}, //channel 19
    {0, 1, 0, 0, 1, 0}, //channel 20
    {0, 1, 0, 1, 1, 0}, //channel 21
    {0, 1, 1, 0, 1, 0}, //channel 22
    {0, 1, 1, 1, 1, 0}, //channel 23
    {1, 0, 0, 0, 1, 0}, //channel 24
    {1, 0, 0, 1, 1, 0}, //channel 25
    {1, 0, 1, 0, 1, 0}, //channel 26
    {1, 0, 1, 1, 1, 0}, //channel 27
    {1, 1, 0, 0, 1, 0}, //channel 28
    {1, 1, 0, 1, 1, 0}, //channel 29
    {1, 1, 1, 0, 1, 0}, //channel 30
    {1, 1, 1, 1, 1, 0} //channel 31
  };

  //loop through the 6 sig
  for (int i = 0; i < 6; i ++) {
    digitalWrite(controlPin[i], muxChannel[channel][i]);
  }

  //read the value at the SIG pin
  int val = analogRead(SIG_pin);

  //return the value
  return val;
}

int verticalBal(int Arr[31]) {
  int l1=0;
  int 12=0;
  int l3=0;
  for(int i=0;i<31;i++) {
    if()
  }
}
