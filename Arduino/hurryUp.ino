#include <stdlib.h>
//  설정 - 키이벤트를 발생하는 문턱값. 무게중심의 좌표값. (범위 : -1.0 ~ 1.0)
//        문턱값을 넘기면 키가 눌리게 됨. 0 에 가까울수록 민감하게 동작.
const float THRESHOLD_COP_forth   = -0.05;    // 앞 쏠림. Y가 이 값보다 커야 키이벤트 발생.
const float THRESHOLD_COP_back    = -0.2;  // 뒤 쏠림. Y가 이 값보다 작아야 키이벤트 발생.
const float THRESHOLD_COP_left    = -0.22;  // 좌 쏠림. X가 이 값보다 작아야 키이벤트 발생.
const float THRESHOLD_COP_right   = 0.22;   // 우 쏠림. X가 이 값보다 커야 키이벤트 발생.
 
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

int sensorNum[35]={
  10,12,14,98,16,18,20,99,
  5,6,7,8,9,11,13,15,17,19,21,22,23,24,25,99,
  0,1,2,3,4,98,26,27,28,29,30
}; 

int tempArr[31];
int standard[31];

int mode = 0; //0: 초기값 설정, 1: 자세 측정
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
  
  for(int i=0;i<31;i++) {
    standard[i] = 0;
    tempArr[i] = 0;
  }
}

void loop() {
  int j = 0;
  int max1 = 0;
  int max2 = 1;
  int sensor = 0;

  while(Serial.available()) { //모드 변경 명령
    income += (char)Serial.read();
    mode = atoi(income);
    digitalWrite(LED_pin,HIGH);
    delay(250);
    digitalWrite(LED_pin,LOW);
    income = "";
  }
  position = 0;
  xPosition = 0;
  yPosition = 0;

  for (int i=0; i<35; i++){
    sensor = 0;
    if(sensorNum[i]==98){
      //Serial.print("  ");
    }else if(sensorNum[i]==99){
      //Serial.println("");
    }else{
      //Serial.print("[");
      sensor = readMux(sensorNum[i]);
      //Serial.print(sensor);
      //Serial.print("]");
      tempArr[j++] = sensor;
    }
    delay(1);
  } //방석 센서값 추출
  

  if(mode == 0) { //0: 초기값 설정
    count++;
    Serial.println(count);
    for(int i=0;i<31;i++) {
      standard[i] = standard[i] + tempArr[i];
    }
    if(count == 10) { //10초간 초기값 설정
      count = 0;
      mode = 1;
      for(int i=0;i<31;i++) {
        standard[i] = standard[i]/count; //초기값 업데이트
      }
      //초기값에 따른 조정
      st_cop_vertical = vertical(standard);
      st_cop_horizon = horizon(standard);
      st_vPivot = st_cop_vertical + 0.275; //Y조정값
      st_hPivot = -st_cop_horizon; //X조정값
    }
    digitalWrite(LED_pin,HIGH);
    delay(1000);
    digitalWrite(LED_pin,LOW);
  }
  else if(mode == 1) { //자세 측정
    position = 0; 
    //앞뒤 측정 
    float raw_vertical = vertical(tempArr); //조정 전 Y무게중심값
    float cop_vertical = raw_vertical + st_vPivot; //초기값에 따른 무게중심값 조정

    int sum_vertical = tempArr[0]+tempArr[1]+tempArr[2]+tempArr[3]+tempArr[4]
                    +tempArr[10]+tempArr[12]+tempArr[14]+tempArr[16]+tempArr[18]+tempArr[20]
                    +tempArr[26]+tempArr[27]+tempArr[28]+tempArr[29]+tempArr[30];
   
    if( sum_vertical > THRESHOLD_SUM_VERT) { //값이 유효한지 확인
       if(THRESHOLD_COP_forth < cop_vertical) {
        yPosition = 1; //앞으로 쏠림
      }
    else if(cop_vertical < THRESHOLD_COP_back) {
       yPosition = 2; //뒤로 쏠림
      }
    }
    //좌우 측정
    int sum_row_2nd =  tempArr[5]+tempArr[6]+tempArr[7]+tempArr[8]+tempArr[9]
                       +tempArr[11]+tempArr[13]+tempArr[15]+tempArr[17]+tempArr[19]
                       +tempArr[21]+tempArr[22]+tempArr[23]+tempArr[24]+tempArr[25];
 
    float raw_horizon = horizon(tempArr); //조정 전 X무게중심값
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

    if(xPosition && !yPosition) {
      position = xPosition;
    }
    else if(!xPosition && yPosition) {
      position = yPosition + 2;
    }
    else if(xPosition && (yPosition == 2)) {
      position = 5; //x값이 존재하고 무게중심이 뒤로 가 있다면 다리를 꼬았다고 판단
    }

    if((position == -1)&&(awayCnt < 10)) { //10초이상 무효값이 입력될 경우 자리를 비웠다고 판단
      position = oldPosition;
      awayCnt++;
    }
    
    Serial.println(position);
    oldPosition = position;

    if(position > 0) {
      //analogWrite(LED_pin, 127);// 최대 : 127
      digitalWrite(LED_pin,HIGH);
      Serial.print("Led ON  ");
    }
    else if(position == 0) {
      //analogWrite(LED_pin, 0);
      digitalWrite(LED_pin,LOW);
      Serial.print("Led OFF  ");
    }
    else if(position == -1) {
      digitalWrite(LED_pin,LOW);
      Serial.print("Away ");
    }
 
    //  무게 중심 계산값을 출력하여 확인하기.
    Print_XY(cop_horizon, cop_vertical);  
    /*Serial.print("%d,",position);
    for(int i=0;i<31;i++) {
        Serial.print(tempArr[i]);
        if(i!=30) {
            Serial.print(",");
        }
        else {
            Serial.println();
        }
    }*/
    delay(1000);
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

void Print_XY(float x, float y) {
  Serial.print("x= ");
  Serial.print(x);
  Serial.print(", y= ");
  Serial.println(y);  
}

float vertical(int* sensor) { //무게중심 Y값 계산
  int sum_row_1st = sensor[10]+sensor[12]+sensor[14]+sensor[16]+sensor[18]+sensor[20];
 
  int sum_row_3rd =   sensor[0]+sensor[1]+sensor[2]+sensor[3]+sensor[4]
                    +sensor[26]+sensor[27]+sensor[28]+sensor[29]+sensor[30];
 
  int sum_vertical = sum_row_1st + sum_row_3rd;
 
  double avg_row_1st = sum_row_1st / 6;
  double avg_row_3rd = sum_row_3rd / 10;
 
  //  무게 중심의 Y 좌표 계산
  float cop_vertical = 0.0;
  if( 0 < sum_vertical) {
    //cop_vertical = (avg_row_1st * (1) + avg_row_3rd * (-1)) / (avg_row_1st + avg_row_3rd);
    cop_vertical = (avg_row_1st * (-1) + avg_row_3rd * (1)) / (avg_row_1st + avg_row_3rd);
  }
  return cop_vertical;
}

float horizon(int* sensor) { //무게 중심 X값 계산
  int sum_row_2nd =  sensor[5]+sensor[6]+sensor[7]+sensor[8]+sensor[9]
                    +sensor[11]+sensor[13]+sensor[15]+sensor[17]+sensor[19]
                    +sensor[21]+sensor[22]+sensor[23]+sensor[24]+sensor[25];
 
  //  센서 2 번째 줄의 15개 센서의 측정값에 위치별 가중치(-7~7))를 부여하여 더합니다. 
  //  그것을 7로 나눠서 좌표 범위를 -1~1 로 한정합니다.
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
