 /**
* Title:		Sage_Redmond_Assignment
* C File        rythmGameFinal.c    	
* Platform:     PICmicro PIC16F877A @ 4 Mhz				
*
* Date:			08/12/2022
*
* Function:		A simple rythm game involving a button and the LCD display.
*               The goal is to cover the ldr when the character is below a set of 3 blocks
*               The potentiamter is used to pick the difficulty between 1&5
*               When lifes = 0, the message "you lose" and "Press reset" are printed to Putty,
*               while an animation plays on the screen.
*               The inturupt timer is used to check the condition of the ldr and determine if
*               it has been covered. If its a bang, the speaker plays
*
*/
//======================Configuration Bits========================
#pragma config FOSC = XT        // Oscillator Selection bits (XT oscillator)
#pragma config WDTE = OFF       // Watchdog Timer Enable bit (WDT disabled)
#pragma config PWRTE = OFF      // Power-up Timer Enable bit (PWRT disabled)
#pragma config BOREN = OFF      // Brown-out Reset Enable bit (BOR disabled)
#pragma config LVP = OFF        // Low-Voltage (Single-Supply) In-Circuit Serial Programming Enable bit (RB3 is digital I/O, HV on MCLR must be used for programming)
#pragma config CPD = OFF        // Data EEPROM Memory Code Protection bit (Data EEPROM code protection off)
#pragma config WRT = OFF        // Flash Program Memory Write Enable bits (Write protection off; all program memory may be written to by EECON control)
#pragma config CP = OFF         // Flash Program Memory Code Protection bit (Code protection off)

//===========================Headers==============================
#include <xc.h>
#include <stdio.h>      //Standard I/O header file
#include "ee302lcd.h"   //required for LCD
#include "I2C_EE302.h"

//========================System Freq.============================
#ifndef _XTAL_FREQ
 #define _XTAL_FREQ 4000000 
#endif 

//========================Definitions============================
#define SW1 RB0			// Assign Label SW1 to PortB bit 0 (RB0) (Basic I/O)
#define SPK1	RC2	    //bit 2 of PORTC, which is set as output by defualt (Basic I/O)
#define BANGTHEDRUM 'n' //criteria is 110. Anything above this is a bang
                        // n = 0x6E
// ========================Globals===============================
typedef unsigned char Uint8; //Size 1 byte, values 0-255
int currentScore = 0;
Uint8 BLOCK = 0b11111111;   // black block
Uint8 enemy = 0b11101111;   // ö
Uint8 goal = 0b11111100;    // yen symbol
int enemyPosition = 15;     // end of LCD
int gameLevel = 0;
int frameRate = 0;
int gtemp = 0;
Uint8 lvl[1];               // has to be a global. If inside superloop, rapid fill mem
Uint8 gOut[2];              // for the score
Uint8 lastValue;
Uint8 eepromMessage[14];
Uint8 lifes = '3';
Uint8 newLine[] = {'\r','\n',' ',' ',' ',' ',' ',' ',' '}; //spaces used to "clear" eeprom

// ======================Prototypes=============================
void setup(void);		            // Declare setup function
void loop(void);                    // Declare loop function
void data2LCD(void);	            // Declare data to LCD function
void lcdTitle(void);	            // Declare title to LCD function
Uint8 pollADC(void);                // using the ADC
void clear_lvl(void);               // clear the lvl array
//Game specific
void setupBoard(void);              // display initial message
void board(void);                   // update look of board, including score and lives
void enemyBlocks(void);             // behaviour of the enemy
void ldrButton(void);               // defines what happens when the ldr is covered
void difficultySelect(void);    	// uses potentiameter to select difficulty between 1 and 5
void hitSound(void);                // plays sound on successful hit
void youLose(void);                 // writes to eeprom, then to uart
void uartTransmiter(const char* p); // transmits message through uart
void lostGameScreen(void);          // displays lost game screen
void clearEEpromOut(void);          // clears the massage to be sent to eeprom

//==============================Main=================================
void main(void){
    i2c_init();
	setup();
	lcdTitle();
	setupBoard();
    difficultySelect();
    ADCON0 = 0x51;//set the ADC to port AN2 for ldr
    TRISA=0x04;         //set PORT A bit 3 as input (LDR on AN2))
    //Superloop-------------------
	loop();
}

//==============================Setup=================================
void setup(void){
    //ADC===================================================
    ADCON0 = 0x41;      // fosc/8, selecting Pot (AN0) and turning on the ADC
    ADCON1 = 0x02;      // left justified, fosc, configured for Vref+ and Vref- to be Vdd & Vss
                        // with AN4-AN0 set as anologue inputs
    //General=============================================
    Lcd8_Init();		// Required initialisation of LCD to 8-bit mode
    TRISB=0x01;			// Set PORTB bit 0 as input (switch 1)
    TRISA=0x01;         // set PORT A bit 3 as input (LDR on AN2))
    
    //Interupts & Timers=======================================
    INTCON 	= 0xC0;
    PIR1    = 0x00;
    PIE1    = 0x01;     // enabled interupt flag
    T1CON   = 0x09;     // Prescalor 1:1, Oscilator off, Internal Clock (Fosc/4),
                        // enable: xx00 1x01
    
    //EEPROM & UART=======================================
    TRISC 	= 0xD8;		// RC6 and RC7 must be configured as inputs to enable the UART
						// RC4 and RC3 high from I2C_init 
                        // RC2 set as an output still
	TXSTA 	= 0x24;		// Set TXEN and BRGH
	RCSTA 	= 0x90;		// Enable serial port by setting SPEN bit
	SPBRG	= 0x19;		// Select 9600 baud rate.
}
//===========================Functions===================================
void loop(void){
    for(;;){data2LCD();} //run forever
}

void setupBoard(){
    int i;
    
    Lcd8_Clear();
    Lcd8_Write_String(" ");
    for(i = 0; i<3;i++){Lcd8_Write_Char(BLOCK);} //write "Drum" as 3 black boxs
        
    Lcd8_Write_String("  Scr:");
    Lcd8_Set_Cursor(2,0);
    Lcd8_Write_Char(goal);
    Lcd8_Write_String("  SW1 start");
   
    while(SW1 == 1) continue;//wait until switch one is pressed
}

void difficultySelect(void){
    __delay_ms(100); //to give switch 1 a chance to un-click
    Lcd8_Clear();
    Lcd8_Set_Cursor(1,0);
    Lcd8_Write_String("Pot Select Lvl");
    Lcd8_Set_Cursor(2,0);
    Lcd8_Write_String("Level: ");
    
    while(SW1 == 1){
        Uint8 potValue = pollADC();
        gameLevel = (potValue / 63) + 1; // never be 0. (X/63)+1 = {1,2,3,4,5}, where X = {0,...,255}
                                         // implicit conversion from unsiged char (1 byte) to int (2 bytes) 
        clear_lvl();
        sprintf(lvl, "%d", gameLevel);   //put int gameLevel, formated as an int (%d), into string lvl
        Lcd8_Set_Cursor(2,7);
        Lcd8_Write_String(lvl);
    }
    frameRate = 300 / gameLevel;        //lowest = 300ms delay, highest = 60ms
}

void data2LCD(void){
    board();
    enemyBlock();
    gtemp = frameRate;
    for(;gtemp > 0; gtemp--){__delay_ms(1);} //delay macro doesn't take variables
}

void board(void){
   int i;
   Lcd8_Clear();
   Lcd8_Set_Cursor(1,0);
   Lcd8_Write_String(" ");
   for(i = 0; i<3;i++){Lcd8_Write_Char(BLOCK);}
   Lcd8_Write_String(" Scr:");
   sprintf(gOut, "%d", currentScore);
   Lcd8_Write_String(gOut);
   Lcd8_Set_Cursor(1,12);
   Lcd8_Write_String(" L:");
   Lcd8_Write_Char(lifes);
   
   Lcd8_Set_Cursor(2,0);
   Lcd8_Write_Char(goal);
}

void enemyBlock(void){
    // behaviour: moves across bottom row towards X (2,0). If at it X, lifes goes down by 1. 
    // when 0, game over
    Lcd8_Set_Cursor(2,enemyPosition);
    Lcd8_Write_Char(enemy);
    enemyPosition--;
    if(enemyPosition == 0){
        enemyPosition = rand()%7 + 7;   	//enemy can spawn from cell 7 to cell 14
        lifes -= 1;
    }
    
    if(lifes == '0'){
        youLose();
        lostGameScreen();
    }
}

void youLose(void){
    //using I2C to transmit to eeprom, then reading back and transmitting result through UART
    write_string(0x00, 0x01, "You Lose!  ");
    read_string(0x00,0x01,eepromMessage, 12);
    uartTransmiter(eepromMessage);
    clearEEpromOut();
            
    write_string(0x00, 0x01, newLine);
    read_string(0x00,0x01,eepromMessage, 12);
    uartTransmiter(eepromMessage);
    clearEEpromOut();
            
    write_string(0x00, 0x01, "Press Reset");
    read_string(0x00,0x01,eepromMessage, 12);
    uartTransmiter(eepromMessage);
    clearEEpromOut();
}

void uartTransmiter(const char* p){
	while (*p != '\0'){			// while string does not equal Null character.
		while (!TXIF);			// wait until TXREG empty.
		TXREG = *p;				// load TXREG with character from string pointed to
		p++;					// by p, then increment p.
	}
}

void read_string(unsigned char address_hi,unsigned char address_lo, unsigned char data[], int length){
    int i;						// Declare i as an integer variable

    i2c_start();				// send Start Condition
    i2c_write(0xa0);			// write Control Byte (A2,A1,A0 all low, R/W = 0)
    i2c_write(address_hi);		// write high byte of address 
    i2c_write(address_lo);		// write low byte of address 
    i2c_repStart();				// send reStart Condition
    i2c_write(0xa1);			// write Control Byte (A2,A1,A0 all low, R/W = 1)
    
    for(i=0; i<length-1;i++){data[i]=i2c_read(1);}	// sequential read with ACK until length-1

	i++;
	data[i]=i2c_read(0);		// read final byte followed by a NACK
		
    i2c_stop();					// send Stop condition

}

void write_string(unsigned char address_hi,unsigned char address_lo, const char* ptr){
    i2c_start();				// send Start Condition
    i2c_write(0xa0);			// write Control Byte (A2,A1,A0 all low, R/W = 0)
    i2c_write(address_hi);		// write high byte of address 
    i2c_write(address_lo);		// write low byte of address 
   while(*ptr !='\0'){
		i2c_write(*ptr);		// sequential write of data until end of string
		ptr++;
   }
   i2c_stop();					// send Stop condition
   __delay_ms(5);				// necessary 5ms delay for write to propagate
}

void clearEEpromOut(){
    for(int i = 0; i < 15; i++){eepromMessage[i] = 0x00;} //set all cells of array to NULL
}

void lostGameScreen(){
    int i;
    Lcd8_Clear();
    
    Lcd8_Set_Cursor(1,0);
    Lcd8_Write_String("");
    Lcd8_Set_Cursor(1,0);
    Lcd8_Write_String(" ");
    
    for(;;){                    //infinite loop. Will not break out. Program needs to be reset, as prompted
        Lcd8_Clear();
        Lcd8_Set_Cursor(1,1);
        Lcd8_Write_String("+  You Lose  *");
        Lcd8_Set_Cursor(2,6);
        Lcd8_Write_String("0_o");
        __delay_ms(100);
        Lcd8_Clear();
        Lcd8_Set_Cursor(1,1);
        Lcd8_Write_String("*  You Lose  +");
        Lcd8_Set_Cursor(2,6);
        Lcd8_Write_String("o_0");
        __delay_ms(100);
    }    
}

void lcdTitle(void){
	Lcd8_Write_String("SageRedmond");		
	Lcd8_Set_Cursor(2,0);				
	Lcd8_Write_String("Rhythm Game");

    __delay_ms(500);
}

void clear_lvl(void){
    lvl[0] = 0x00;
}

void ldrButton(void){
    Uint8 adcVoltage;
    adcVoltage = pollADC();
    if(adcVoltage != lastValue){ //check if there has been a change since last time
      //resistance goes down in darkness. Highest ADC = 255 in decimal
        if(adcVoltage > BANGTHEDRUM){ //LDR needs to be covered for a "bang" on the drum
            if(enemyPosition == 1 || enemyPosition == 2 || enemyPosition == 3){
               currentScore += 1;
               hitSound();
            }
            enemyPosition = rand()%7 + 7;
        }
    }
    lastValue=adcVoltage;
}

void hitSound(void){
    int i;
    for(i = 50; i > 0; i--){ //turn up i to increase volume
        SPK1 = 1;
        __delay_ms(0.5);		//0.5ms delay
        SPK1 = 0;
        __delay_ms(0.5);		//0.5ms delay
    }
}

Uint8 pollADC(void){
    Uint8 adcValue;
    __delay_us(20);             //delay so ADRESH can properly clear
    GO_nDONE = 1;               //start reading of adc
    while(GO_nDONE) continue;   // when reading down, store in ADRESH
    adcValue = ADRESH;
    return adcValue;
}

void __interrupt()		        // Interrupt identifier
isr(void)                       // Interrupt function
{
    if(TMR1IF == 1){
    
        TMR1IF = 0;             // clear interrupt flag, ready for next interrupt
        
        ldrButton();            // with prescalar set as 1:1, there are 15 interupts/sec
                                // thus LDR is checked 15 times a sec.
    }
}