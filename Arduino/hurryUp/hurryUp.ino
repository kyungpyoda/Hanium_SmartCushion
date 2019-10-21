
//  설정 - 키이벤트를 발생하는 문턱값. 무게중심의 좌표값. (범위 : -1.0 ~ 1.0)
//        문턱값을 넘기면 키가 눌리게 됨. 0 에 가까울수록 민감하게 동작.
const float THRESHOLD_COP_forth   = 0.3;    // 앞 쏠림. Y가 이 값보다 커야 키이벤트 발생. 0.3
const float THRESHOLD_COP_back    = -0.85;  // 뒤 쏠림. Y가 이 값보다 작아야 키이벤트 발생. -0.85
const float THRESHOLD_COP_left    = -0.11;  // 좌 쏠림. X가 이 값보다 작아야 키이벤트 발생. -0.22
const float THRESHOLD_COP_right   = 0.11;   // 우 쏠림. X가 이 값보다 커야 키이벤트 발생. 0.22
 
//----------------------------------------------
//  무효 판정 문턱값. 측정값이 아래 문턱값보다 낮으면 무시함.
const int THRESHOLD_SUM_row_1st   = 60;   // 1st row, front row
const int THRESHOLD_SUM_row_3rd   = 60;   // 3rd row, back row
const int THRESHOLD_SUM_row_2nd   = 105;  // 2nd row, left and right
const int THRESHOLD_SUM_VERT      = 60; // SUM? PRESSURE?

int En0 = 7;  //  Low enabled
int En1 = 6;  //  Low enabled

int S0  = 5;
int S1  = 4;
int S2  = 3;
int S3  = 2;

int SIG_pin = A3;
int LED_pin = 8;
int Motor_pin = 9;

int tempArr[31];
int standard[31];
int cushion[31];

int mode = 0; //0: 초기값 설정, 1: 자세 측정
int vibrate = 4; //3: 진동 비활성화, 4: 진동 활성화
int out = 0; //Arduino -> App, 2: 측성완료,
int del = 1000; //default = 1000, game = 100 
int count = -1;

int position = 0; //-1: 자리 비움, 0: 정상, 1: 좌로 쏠림, 2: 우로 쏠림, 3: 앞으로 쏠림, 4: 뒤로 쏠림, 5: 다리 꼼
int xPosition = 0; //-1: 값 무시, 0: 정상, 1: 앞으로 쏠림, 2: 뒤로 쏠림
int yPosition = 0; //-1: 값 무시, 0: 정상, 1: 좌로 쏠림, 2: 우로 쏠림
int oldPosition = 0; 
int awayCnt = 0; //자리비움카운트

char* income = "";
float st_cop_vertical = 0;
float st_cop_horizon = 0;
float st_vPivot = 0;
float st_hPivot = 0;

void setup() {
  Serial.begin(115200);
  pinMode(En0, OUTPUT);
  pinMode(En1, OUTPUT);

  pinMode(S0, OUTPUT);
  pinMode(S1, OUTPUT);
  pinMode(S2, OUTPUT);
  pinMode(S3, OUTPUT);

  pinMode(LED_pin,OUTPUT);
  pinMode(Motor_pin,OUTPUT);
  
  for(int i=0;i<31;i++) { //변수 초기화
    standard[i] = 0;
    tempArr[i] = 0;
    cushion[i] = 0;
  }
}

void loop() {
  int sensor = 0;

  while(Serial.available()) { //모드 변경 명령
    income += (char)Serial.read();
    switch(atoi(income)) {
      case 0:
      case 1:
        mode = atoi(income); //초기값 설정 명령
        del = 1000;
        break;
      case 3: //진동 OFF
        vibrate = 3;
        break;
      case 4: //진동 ON
        vibrate = 4;
        break;
      case 5:
        del = 100;
        break;
    }
    digitalWrite(LED_pin,LOW);
    delay(250);
    digitalWrite(LED_pin,HIGH);
    income = "";
  }
  position = 0;
  xPosition = 0;
  yPosition = 0;

  //방석 센서값 추출
  for(int i=0;i<33;i++) {
    cushion[i] = readMux(i);
  }
  

  if(mode == 0) { //0: 초기값 설정
    count++;
    for(int i=0;i<31;i++) {
      tempArr[i] = tempArr[i] + cushion[i];
    }
    if(count == 10) { //10초간 초기값 설정
      mode = 1;
      for(int i=0;i<31;i++) {
        standard[i] = tempArr[i]/count; //초기값 업데이트
        tempArr[i] = 0;
      }
      //초기값에 따른 조정
      st_cop_vertical = vertical(standard);
      st_cop_horizon = horizon(standard);
      st_vPivot = st_cop_vertical - (THRESHOLD_COP_forth + THRESHOLD_COP_back)/2; //Y조정값
      st_hPivot = -st_cop_horizon; //X조정값
      count = 0;
      //pushStandard(); //2: 초기값 설정 완료 전송
    }
    digitalWrite(LED_pin,HIGH);
    delay(1000);
    digitalWrite(LED_pin,LOW);
  }
  else if(mode == 1) { //자세 측정
    position = 0; 
    //앞뒤 측정 
    float raw_vertical = vertical(cushion); //조정 전 Y무게중심값
    float cop_vertical = raw_vertical + st_vPivot; //초기값에 따른 무게중심값 조정

    int sum_vertical = cushion[0]+cushion[1]+cushion[2]+cushion[3]+cushion[4]
                    +cushion[10]+cushion[12]+cushion[14]+cushion[16]+cushion[18]+cushion[20]
                    +cushion[26]+cushion[27]+cushion[28]+cushion[29]+cushion[30];
   
    if( sum_vertical > THRESHOLD_SUM_VERT) { //값이 유효한지 확인
       if(THRESHOLD_COP_forth < cop_vertical) {
        yPosition = 1; //앞으로 쏠림
      }
    else if(cop_vertical < THRESHOLD_COP_back) {
       yPosition = 2; //뒤로 쏠림
      }
    }
    //좌우 측정
    int sum_row_2nd =  cushion[5]+cushion[6]+cushion[7]+cushion[8]+cushion[9]
                       +cushion[11]+cushion[13]+cushion[15]+cushion[17]+cushion[19]
                       +cushion[21]+cushion[22]+cushion[23]+cushion[24]+cushion[25];
 
    float raw_horizon = horizon(cushion); //조정 전 X무게중심값
    float cop_horizon = raw_horizon + st_hPivot; // 초기값에 따른 무게중심값 조정
  
    if(sum_row_2nd > THRESHOLD_SUM_row_2nd) { //값이 유효한지 확인
      if(cop_horizon < THRESHOLD_COP_left) {
        xPosition = 1; //좌로 쏠림
      }
      else if(THRESHOLD_COP_right < cop_horizon) {
       xPosition = 2; //우로 쏠림
      } 
    }
    else if(sum_vertical < THRESHOLD_SUM_VERT) {//자리비움 확인
       position = -1;
    }

    float twisted = twist(cushion);
    if((twisted < -80) || (twisted > 80)) { //다리꼬기를 최우선
      position = 5;
    }
    else if(xPosition && !yPosition) {
      position = xPosition;
    }
    else if(!xPosition && yPosition) {
      position = yPosition + 2;
    }
    else if(xPosition && (yPosition == 2)) { //좌우를 앞뒤보다 우선시 한다.
      position = xPosition;
    }

    if((position == -1)&&(awayCnt < 10)) { //10초이상 무효값이 입력될 경우 자리를 비웠다고 판단
      position = oldPosition; //10초 이하는 이전 값을 사용
      awayCnt++;
    }
    
    oldPosition = position; //oldPosition에 이전 값 저장

    if(position > 0) {//wrong position
      digitalWrite(LED_pin,HIGH);
      if(vibrate == 4) {
        //analogWrite(LED_pin, 127);// 최대 : 127
      }
    }
    else if(position == 0) { //right position
      digitalWrite(LED_pin,LOW);
      if(vibrate == 4) {
        //analogWrite(LED_pin, 0);// 최대 : 127
      }
    }
    else if(position == -1) { //away
      if(vibrate == 4) {
        //analogWrite(LED_pin,0);
      }
      digitalWrite(LED_pin,LOW);
    }
 
    Serial.print(position);
    /*for(int i=0;i<31;i++) {
      Serial.print("   ");
      Serial.print(cushion[i]);
    }*/
    Serial.println();
    delay(del);
  }
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

float vertical(int* sensor) { //무게중심 Y값 계산
  int sum_row_1st = sensor[10]+sensor[12]+sensor[14]+sensor[16]+sensor[18]+sensor[20];
 
  int sum_row_3rd =   sensor[0]+sensor[1]+sensor[2]+sensor[3]+sensor[4]
                    +sensor[26]+sensor[27]+sensor[28]+sensor[29]+sensor[30];
  
  double avg_row_1st = sum_row_1st / 6;
  double avg_row_3rd = sum_row_3rd / 10;
  
  if(avg_row_1st < THRESHOLD_SUM_row_1st) {
    avg_row_1st = 0;
  }
  
  if(avg_row_3rd < THRESHOLD_SUM_row_3rd) {
    avg_row_3rd = 0;
  }
  
  int sum_vertical = sum_row_1st + sum_row_3rd;
 
  //  무게 중심의 Y 좌표 계산
  float cop_vertical = 0.0;
  if( 0 < sum_vertical) {
    cop_vertical = (avg_row_1st * (1) + avg_row_3rd * (-1)) / (avg_row_1st + avg_row_3rd);
    //cop_vertical = (avg_row_1st * (-1) + avg_row_3rd * (1)) / (avg_row_1st + avg_row_3rd);
  }
  return cop_vertical;
}

float horizon(int* sensor) { //무게 중심 X값 계산
  int sum_row_2nd =  sensor[5]+sensor[6]+sensor[7]+sensor[8]+sensor[9]
                    +sensor[11]+sensor[13]+sensor[15]+sensor[17]+sensor[19]
                    +sensor[21]+sensor[22]+sensor[23]+sensor[24]+sensor[25];
 
  int sum_wp_horizon = (  (-7)*sensor[5]+(-6)*sensor[6]+(-5)*sensor[7]
                          +(-4)*sensor[8]+(-3)*sensor[9]+(-2)*sensor[11]
                          +(-1)*sensor[13]+(0)*sensor[15]
                          +(1)*sensor[17]+(2)*sensor[19]+(3)*sensor[21]
                          +(4)*sensor[22]+(5)*sensor[23]+(6)*sensor[24]
                          +(7)*sensor[25] ) / 7.0; // divide 7.0 ==> unitize. (-7.0~7.0)
 
  float cop_horizon = 0.0;
  //  무게 중심의 X 좌표 계산
  if(0 < sum_row_2nd) {
    cop_horizon = sum_wp_horizon / (double)sum_row_2nd;
  }
  return cop_horizon;
}

float twist(int* sensor) {
  float sum_row_1st_left = sensor[10] + sensor[12] + sensor[14];
  float sum_row_1st_right = sensor[16] + sensor[18] + sensor[20];

  float cop_1st = (sum_row_1st_right - sum_row_1st_left)/6;
  return cop_1st; //left => -, right => +
}

void pushStandard() {
  Serial.print("2,");
  for(int i = 0;i<31;i++) {
    Serial.print(standard[i]);
    if(i != 30) {
     Serial.print(","); 
    }
    else {
      Serial.println();
    }
  }
}
