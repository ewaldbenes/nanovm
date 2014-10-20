//
//  NanoVM, a tiny java VM for the Atmel AVR family
//  Copyright (C) 2005 by Till Harbaum <Till@Harbaum.org>
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 

//
//  native_impl.c
//

#include "types.h"
#include "debug.h"
#include "config.h"
#include "error.h"

#include "vm.h"
#include "nvmfile.h"
#include "native.h"
#include "native_impl.h"
#include "stack.h"

#ifdef AVR
#include "native_avr.h"
#endif

#ifdef LCD
#include "native_lcd.h"
#endif

#ifdef ASURO
#include "native_asuro.h"
#endif

#ifdef STDIO
#include "native_stdio.h"
#endif

void native_java_lang_object_invoke(u08_t mref) {
  if(mref == NATIVE_METHOD_INIT) {
    /* ignore object constructor ... */
    stack_pop();  // pop object reference
  } else 
    error(ERROR_NATIVE_UNKNOWN_METHOD);
}
  
void native_new(u16_t mref) {
  if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_STRINGBUFFER) {
    // create empty stringbuf object and push reference onto stack
    stack_push(NVM_TYPE_HEAP | heap_alloc(FALSE, 1));
  } else 
    error(ERROR_NATIVE_UNKNOWN_CLASS);
}

void native_invoke(u16_t mref) {
  // check for native classes/methods
  if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_OBJECT) {
    native_java_lang_object_invoke(NATIVE_ID2METHOD(mref));
#if defined(STDIO)
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_PRINTSTREAM) {
    native_java_io_printstream_invoke(NATIVE_ID2METHOD(mref));
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_INPUTSTREAM) {
    native_java_io_inputstream_invoke(NATIVE_ID2METHOD(mref));
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_STRINGBUFFER) {
    native_java_lang_stringbuffer_invoke(NATIVE_ID2METHOD(mref));
#endif
#if defined(AVR) && !defined(ASURO)
    // the avr specific classes 
    // (not used in asuro, although its avr based)
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_AVR) {
    native_avr_avr_invoke(NATIVE_ID2METHOD(mref));
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_PORT) {
    native_avr_port_invoke(NATIVE_ID2METHOD(mref));
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_TIMER) {
    native_avr_timer_invoke(NATIVE_ID2METHOD(mref));
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_ADC) {
    native_avr_adc_invoke(NATIVE_ID2METHOD(mref));
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_PWM) {
    native_avr_pwm_invoke(NATIVE_ID2METHOD(mref));
#endif
#if defined(LCD)
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_LCD) {
    native_lcd_invoke(NATIVE_ID2METHOD(mref));
#endif
#if defined(ASURO)
    // the asuro specific classes
  } else if(NATIVE_ID2CLASS(mref) == NATIVE_CLASS_ASURO) {
    native_asuro_invoke(NATIVE_ID2METHOD(mref));
#endif
  } else 
    error(ERROR_NATIVE_UNKNOWN_CLASS);
}

