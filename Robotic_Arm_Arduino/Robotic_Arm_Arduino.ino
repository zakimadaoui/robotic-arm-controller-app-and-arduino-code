/*
 * Copyright 2019 Zakaria Madaoui. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <Servo.h>

// Declaring all servos
Servo servoA;
Servo servoB;
Servo servoC;
Servo servoD;

// input data
char inChar ;
int inAngle ;

int counter = 0 ;


void setup() {

  //interupt button stuff
  pinMode(2,INPUT);
  attachInterrupt(0,buttonPressed,RISING);

  // Attaching and initializing all servos
  servoA.attach(7);
  servoB.attach(8);
  servoC.attach(9);
  servoD.attach(10);

  // Setting up the ARM
  servoA.write(10);
  servoB.write(90);
  servoC.write(10);
  servoD.write(90);

  //Beging thr serial communication at 9600 baud rate
  Serial.begin(9600);
  
  // For Assuring communication with BT device
  Serial.write(2);
  Serial.write(2);

}

void loop() {

  if(Serial.available()>0){
    // Counter for knowing the position of 
    //the received data
    counter ++ ; 

    // Getting and managing data
    if      (counter == 1){
      inChar = (char) Serial.read();
      }
    
    else if (counter == 2){
      inAngle = (int) Serial.read();
      moveServo(inChar,inAngle);
      counter=0 ;
      }

       
    }
}

//Controlling all servos from this function
void moveServo(char c, int a){

  switch(c){
    case 'a': servoA.write(a); break;
    case 'b': servoB.write(a); break;
    case 'c': servoC.write(a); break;
    case 'd': servoD.write(a); break;
    }
  
  }

void buttonPressed(){
  Serial.write("hi");
}
