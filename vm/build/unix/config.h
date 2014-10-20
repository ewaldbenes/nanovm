//
// config.h
//

#ifndef CONFIG_H
#define CONFIG_H

#define CODESIZE 32768
#define HEAPSIZE 768

#define WDT_NO_STATISTICS  // all eeprom required for uvmfile

// define this if you don't want to use stdin/stdout, but
// a named pipe (e.g. to test the loader code):
// #define UART_PORT "/dev/ttyq0"

#define STACK_CHECK      // enable check if method returns empty stack
#define ARRAY            // enable arrays
#define SWITCH           // support switch instruction
#define INHERITANCE      // support for inheritance

#define STDIO            // enable native stdio support

// marker used to indicate, that this item is stored in eeprom
#define NVMFILE_FLAG       0x40000000

#define NVM_USE_FLOAT
#define NVM_USE_32BIT_WORD

#endif // CONFIG_H
