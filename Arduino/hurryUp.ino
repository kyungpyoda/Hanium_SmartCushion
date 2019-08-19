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

int tempArr[31];
int avgArr[6]; //{tl,tr,ml,mr,bl,br}

int tl[3];
int tr[3];
int ml[8];
int mr[8];
int bl[5];
int br[5];

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
  int max1 = 0;
  int max2 = 1;
  int sensor = 0;
  initial();
  
  for (int i=0; i<35; i++){
    sensor = 0;
    if(sensorNum[i]==98){
      Serial.print("  ");
    }else if(sensorNum[i]==99){
      Serial.println("");
    }else{
      Serial.print("[");
      sensor = readMux(sensorNum[i]);
      Serial.print(sensor);
      Serial.print("]");
      tempArr[j++] = sensor;
    }
    delay(1);
  }
  
  Serial.println("");
  Serial.println("========================================================");
  Serial.println("");

  section(tempArr);
 
  avgArr[0] = avg(tl,3);
  avgArr[1] = avg(tr,3);
  avgArr[2] = avg(ml,8);
  avgArr[3] = avg(mr,8);
  avgArr[4] = avg(bl,5);
  avgArr[5] = avg(br,5);
  
  for(int k=0;k<6;k++) {
    Serial.print(avgArr[k]);
    if(avgArr[max1]<avgArr[k]) {
      max2 = max1;
      max1 = k;
    }
    Serial.print("   ");
  } //최대값 구하기
  for(int l=0;l<6;l++) {
    if((l!=max1)&&(avgArr[max2]<avgArr[l])) {
      max2 = l;
    }
  }
  Serial.println();
  Serial.print(max1);
  Serial.print("   ");
  Serial.print(max2);
  Serial.println();

  if(((max1==0)&&(max2==1)) || ((max1==1)&&(max2==0))) {
    digitalWrite(LED_pin,LOW);
  }
  else {
   digitalWrite(LED_pin,HIGH); 
  }
  delay(500);
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

void initial() {
  for(int i=0;i<31;i++) { //tempArr
    tempArr[i] = 0;
  }
  for(int j=0;j<3;j++) { //top
    tl[j] = 0;
    tr[j] = 0;
  }
  for(int k=0;k<8;k++) { //mid
    ml[k] = 0;
    mr[k] = 0;
  }
  for(int l=0;l<5;l++) { //bot
    bl[l] = 0;
    br[l] = 0;
  }
  for(int n=0;n<6;n++) {
    avgArr[n] = 0;
  }
}

void section(int Arr[]) {
  int topL = 0;
  int topR = 0;
  int midL = 0;
  int midR = 1;
  int botL = 0;
  int botR = 0;
  for(int i=0;i<31;i++) {
    if((i>=0)&&(i<=4)) {
      bl[botL++] = Arr[i];
    }
    else if(((i>=5)&&(i<=9))||(i==11)||(i==13)) {
      ml[midL++] = Arr[i];
    }
    else if(((i>=21)&&(i<=25))||(i==17)||(i==19)) {
      mr[midR++] = Arr[i];
    }
    else if((i>=10)&&(i<=14)&&(i%2==0)) {
      tl[topL++] = Arr[i];
    }
    else if((i>=16)&&(i<=20)&&(i%2==0)) {
      tr[topR++] = Arr[i];
    }
    else if((i>=26)&&(i<=30)) {
      br[botR++] = Arr[i];
    }
    else if(i==15) {
      ml[7] = Arr[i];
      mr[0] = Arr[i];
    }
  }
}

int avg(int* Arr,int s) {
  int sum=0;
  for(int i=0;i<s;i++) {
    sum += Arr[i];
  }
  return sum/s;
}
