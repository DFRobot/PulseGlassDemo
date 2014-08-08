#include "Arduino.h"
#include "TimerOne.h"
#include "Adafruit_NeoPixel.h"
#include "PlainProtocol.h"

#define LEDPin  A4
#define LEDLength   2


Adafruit_NeoPixel strip = Adafruit_NeoPixel(LEDLength, LEDPin, NEO_GRB + NEO_KHZ800);

PlainProtocol myPulseGlass(Serial);

volatile int rate[10];                    // used to hold last ten IBI values
volatile unsigned long sampleCounter = 0;          // used to determine pulse timing
volatile unsigned long lastBeatTime = 0;           // used to find the inter beat interval
volatile int P =512;                      // used to find peak in pulse wave
volatile int T = 512;                     // used to find trough in pulse wave
volatile int thresh = 512;                // used to find instant moment of heart beat
volatile int amp = 100;                   // used to hold amplitude of pulse waveform
volatile boolean firstBeat = true;        // used to seed rate array so we startup with reasonable BPM
volatile boolean secondBeat = true;       // used to seed rate array so we startup with reasonable BPM
volatile int Signal;                // holds the incoming raw data

uint8_t LEDRed=0, LEDGreen=0, LEDBlue=0;

//  VARIABLES
int pulsePin = 0;                 // Pulse Sensor purple wire connected to analog pin 0
int blinkPin = 13;                // pin to blink led at each beat
                                  //int fadePin = 5;                  // pin to do fancy classy fading blink at each beat
int fadeRate = 0;                 // used to fade LED on with PWM on fadePin


// these variables are volatile because they are used during the interrupt service routine!
volatile int BPM;                   // used to hold the pulse rate
volatile int IBI = 600;             // holds the time between beats, the Inter-Beat Interval
volatile boolean Pulse = false;     // true when pulse wave is high, false when it's low
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.

void readPulseISR(){
    Signal = analogRead(pulsePin);              // read the Pulse Sensor
    sampleCounter += 2;                         // keep track of the time in mS with this variable
    int N = sampleCounter - lastBeatTime;       // monitor the time since the last beat to avoid noise
    
    //  find the peak and trough of the pulse wave
    if(Signal < thresh && N > (IBI/5)*3){       // avoid dichrotic noise by waiting 3/5 of last IBI
        if (Signal < T){                        // T is the trough
            T = Signal;                         // keep track of lowest point in pulse wave
        }
    }
    
    if(Signal > thresh && Signal > P){          // thresh condition helps avoid noise
        P = Signal;                             // P is the peak
    }                                        // keep track of highest point in pulse wave
    
    //  NOW IT'S TIME TO LOOK FOR THE HEART BEAT
    // signal surges up in value every time there is a pulse
    if (N > 250){                                   // avoid high frequency noise
        if ( (Signal > thresh) && (Pulse == false) && (N > (IBI/5)*3) ){
            Pulse = true;                               // set the Pulse flag when we think there is a pulse
            digitalWrite(blinkPin,HIGH);                // turn on pin 13 LED
            IBI = sampleCounter - lastBeatTime;         // measure time between beats in mS
            lastBeatTime = sampleCounter;               // keep track of time for next pulse
            
            if(firstBeat){                         // if it's the first time we found a beat, if firstBeat == TRUE
                firstBeat = false;                 // clear firstBeat flag
                return;                            // IBI value is unreliable so discard it
            }
            if(secondBeat){                        // if this is the second beat, if secondBeat == TRUE
                secondBeat = false;                 // clear secondBeat flag
                for(int i=0; i<=9; i++){         // seed the running total to get a realisitic BPM at startup
                    rate[i] = IBI;
                }
            }
            
            // keep a running total of the last 10 IBI values
            word runningTotal = 0;                   // clear the runningTotal variable
            
            for(int i=0; i<=8; i++){                // shift data in the rate array
                rate[i] = rate[i+1];              // and drop the oldest IBI value
                runningTotal += rate[i];          // add up the 9 oldest IBI values
            }
            
            rate[9] = IBI;                          // add the latest IBI to the rate array
            runningTotal += rate[9];                // add the latest IBI to runningTotal
            runningTotal /= 10;                     // average the last 10 IBI values
            BPM = 60000/runningTotal;               // how many beats can fit into a minute? that's BPM!
            QS = true;                              // set Quantified Self flag
                                                    // QS FLAG IS NOT CLEARED INSIDE THIS ISR
        }
    }
    
    if (Signal < thresh && Pulse == true){     // when the values are going down, the beat is over
        digitalWrite(blinkPin,LOW);            // turn off pin 13 LED
        Pulse = false;                         // reset the Pulse flag so we can do it again
        amp = P - T;                           // get amplitude of the pulse wave
        thresh = amp/2 + T;                    // set thresh at 50% of the amplitude
        P = thresh;                            // reset these for next time
        T = thresh;
    }
    
    if (N > 2500){                             // if 2.5 seconds go by without a beat
        thresh = 512;                          // set thresh default
        P = 512;                               // set P default
        T = 512;                               // set T default
        lastBeatTime = sampleCounter;          // bring the lastBeatTime up to date
        firstBeat = true;                      // set these to avoid noise
        secondBeat = true;                     // when we get the heartbeat back
    }

}



void colorHSV(float hue, float saturation, float value){
    float colorF,colorP,colorQ,colorT;
    int colorH;
    
    float temp=hue/60;
    colorH=(int)(temp);
    colorF=temp-colorH;
    colorP=value*(1-saturation);
    colorQ=value*(1-colorF*saturation);
    colorT=value*(1-(1-colorF)*saturation);
    
    switch (colorH) {
        case 0:
            LEDRed=value;
            LEDGreen=colorT;
            LEDBlue=colorP;
            break;
        case 1:
            LEDRed=colorQ;
            LEDGreen=value;
            LEDBlue=colorP;
            break;
        case 2:
            LEDRed=colorP;
            LEDGreen=value;
            LEDBlue=colorT;
            break;
        case 3:
            LEDRed=colorP;
            LEDGreen=colorQ;
            LEDBlue=value;
            break;
        case 4:
            LEDRed=colorT;
            LEDGreen=colorP;
            LEDBlue=value;
            break;
        case 5:
            LEDRed=value;
            LEDGreen=colorP;
            LEDBlue=colorQ;
            break;
        default:
            break;
    }
    
}


void setup() {
    
    myPulseGlass.begin(115200);
    Timer1.initialize(2000);
    Timer1.attachInterrupt(readPulseISR);
    strip.begin();
    strip.show();
}


void loop() {
    
    int BPMToHue;

    
    static unsigned long ledRefreshTimer=millis();
    if (millis()-ledRefreshTimer>=20) {
        
        ledRefreshTimer=millis();
        fadeRate -= BPM/8;
        fadeRate = constrain(fadeRate, 0, 255);
        
        BPMToHue=constrain(BPM, 40, 120);
        
        if (BPMToHue<65) {
            BPMToHue=map(BPMToHue, 40, 65, 240, 200);
        }
        else if (BPMToHue>=65 && BPMToHue<95){
            BPMToHue=map(BPMToHue, 65, 85, 200, 80);
        }
        else{
            BPMToHue=map(BPMToHue, 85, 120, 80, 0);
        }
      
        //65~85
        
        colorHSV(BPMToHue, 1.0, fadeRate);
        strip.setPixelColor(0, LEDRed, LEDGreen, LEDBlue);
        strip.setPixelColor(1, LEDRed, LEDGreen, LEDBlue);
        
        strip.show();
    }
    
    
    if (QS == true){                       // Quantified Self flag is true when arduino finds a heartbeat
        myPulseGlass.write("QS");
        myPulseGlass.write("BPM",BPM);
        fadeRate = 255;                  // Set 'fadeRate' Variable to 255 to fade LED with pulse
        QS = false;                      // reset the Quantified Self flag for next time
    }

}
