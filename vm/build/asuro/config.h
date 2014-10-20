//
// config.h
//
// NanoVM configuration file for the Asuro
//

#ifndef CONFIG_H
#define CONFIG_H

// cpu related setup
#define ATMEGA8
#define ASURO                  // compiles some special code e.g. in uart.c
#define CLOCK 8000000

// uart setup
#define UART_BITRATE 2400
#define UART_BUFFER_BITS 5     // 32 bytes buffer (min. req for loader)

#define CODESIZE 512
#define HEAPSIZE 768

// avr specific native init routines
#define NATIVE_INIT  native_asuro_init()

// vm setup
#undef STACK_CHECK      // enable check if method returns empty stack
#define ARRAY            // enable arrays
#define SWITCH           // support switch instruction
#define INHERITANCE      // support for inheritance

// native setup
#define STDIO            // enable native stdio support

// marker used to indicate, that this item is stored in eeprom
#define NVMFILE_FLAG     0x8000

#endif // CONFIG_H
