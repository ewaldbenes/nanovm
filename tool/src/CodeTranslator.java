//
//  NanoVMTool, Converter and Upload Tool for the NanoVM
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
//  Parts of this tool are based on public domain code written by Kimberley
//  Burchett: http://www.kimbly.com/code/classfile/
//

public class CodeTranslator {

  // parameter bytes for each of the 256 instructions (-1 = not implemented)
  final static int[] PARAMETER_BYTES = {
 // 00  01  02  03  04  05  06  07  08  09  0a  0b  0c  0d  0e  0f
     0, -1,  0,  0,  0,  0,  0,  0,  0, -1, -1,  0,  0,  0, -1, -1, // 00
     1,  2,  1, -1, -1,  1, -1,  1, -1, -1,  0,  0,  0,  0, -1, -1, // 10
    -1, -1,  0,  0,  0,  0, -1, -1, -1, -1,  0,  0,  0,  0,  0, -1, // 20
     0, -1, -1,  0, -1, -1,  1, -1,  1, -1, -1,  0,  0,  0,  0, -1, // 30

    -1, -1, -1,  0,  0,  0,  0, -1, -1, -1, -1,  0,  0,  0,  0,  0, // 40
    -1,  0, -1, -1,  0, -1, -1,  0,  0,  0, -1, -1,  0, -1, -1, -1, // 50
     0, -1,  0, -1,  0, -1,  0, -1,  0, -1,  0, -1,  0, -1,  0, -1, // 60
     0, -1,  0, -1,  0, -1,  0, -1,  0, -1,  0, -1,  0, -1,  0, -1, // 70

     0, -1,  0, -1,  2, -1,  0, -1, -1, -1, -1,  0, -1, -1, -1, -1, // 80
    -1, -1,  0, -1, -1,  0,  0, -1, -1,  2,  2,  2,  2,  2,  2,  2, // 90
     2,  2,  2,  2,  2, -1, -1,  2, -1, -1,  0, -1,  0, -1,  0, -1, // a0
    -1,  0,  2,  2,  2,  2,  2,  2,  2, -1, -1,  2,  1, -1,  0, -1, // b0

    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // c0
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // d0
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // e0
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // f0
  };

  // some java bytecode instructions
  final static int OP_NOP           = 0x00;
  final static int OP_ACONST_NULL   = 0x01;
  final static int OP_ICONST_0      = 0x03;
  final static int OP_SIPUSH        = 0x11;
  final static int OP_LDC           = 0x12;
  final static int OP_ILOAD_0       = 0x1a;
  final static int OP_ILOAD_1       = 0x1b;
  final static int OP_ILOAD_2       = 0x1c;
  final static int OP_ILOAD_3       = 0x1d;
  final static int OP_ALOAD_0       = 0x2a;
  final static int OP_ALOAD_1       = 0x2b;
  final static int OP_ALOAD_2       = 0x2c;
  final static int OP_ALOAD_3       = 0x2d;
  final static int OP_ISTORE        = 0x36;
  final static int OP_ASTORE        = 0x3a;
  final static int OP_ISTORE_0      = 0x3b;
  final static int OP_ISTORE_1      = 0x3c;
  final static int OP_ISTORE_2      = 0x3d;
  final static int OP_ISTORE_3      = 0x3e;
  final static int OP_ASTORE_0      = 0x4b;
  final static int OP_ASTORE_1      = 0x4c;
  final static int OP_ASTORE_2      = 0x4d;
  final static int OP_ASTORE_3      = 0x4e;
  final static int OP_I2B           = 0x91;
  final static int OP_I2C           = 0x92;
  final static int OP_I2S           = 0x93;
  final static int OP_TABLESWITCH   = 0xaa;
  final static int OP_GETSTATIC     = 0xb2;
  final static int OP_PUTSTATIC     = 0xb3;
  final static int OP_GETFIELD      = 0xb4;
  final static int OP_PUTFIELD      = 0xb5;
  final static int OP_INVOKEVIRTUAL = 0xb6;
  final static int OP_INVOKESPECIAL = 0xb7;
  final static int OP_INVOKESTATIC  = 0xb8;
  final static int OP_NEW           = 0xbb;
  
  static int unsigned(int i) {
    if(i<0) return i + 256;
    return i;
  }

  static byte signed(int i) {
    if(i>127) return (byte)(i - 256);
    return (byte)i;
  }

  public static void translate(ClassInfo classInfo, byte[] code) {
    // process all code bytes
    for(int i=0;i<code.length;i++) {
      int cmd = unsigned(code[i]);

      if(PARAMETER_BYTES[cmd] < 0) {
	System.out.println("Unsupported byte code " +
			   Integer.toHexString(cmd));
	System.exit(-1);
      }

      if(cmd == OP_LDC) { // load from constant pool (e.g. strings)
	int index = unsigned(code[i+1]);
	System.out.print("ldc #" + index);
	code[i+1] = signed(classInfo.getConstPool().
			   constantRelocate(index));
      }
 
      if((cmd == OP_GETFIELD)||(cmd == OP_PUTFIELD)) {
	int index = 256 * unsigned(code[i+1]) + unsigned(code[i+2]);
	System.out.print("get/putfield #"+ index);
	index = classInfo.getConstPool().constantRelocate(index);
	code[i+1] = signed(index>>8);
	code[i+2] = signed(index&0xff);
      }

      if((cmd == OP_GETSTATIC)||(cmd == OP_PUTSTATIC)) {
	int index = 256 * unsigned(code[i+1]) + unsigned(code[i+2]);
	System.out.print("get/putstatic #" + index);
	index = classInfo.getConstPool().constantRelocate(index);
	code[i+1] = signed(index>>8);
	code[i+2] = signed(index&0xff);

	// getstatic usually uses a reference to the object in question.
	// if the object is native, then this is just an id, that is to
	// be directly pushed onto the stack, thus we replace the getstatic
	// instruction with a push instruction
	if((cmd == OP_GETSTATIC)&&((index>>8) >= NativeMapper.lowestNativeId)) 
	  code[i] = signed(OP_SIPUSH);
      }
      
      if((cmd == OP_INVOKEVIRTUAL)||(cmd == OP_INVOKESPECIAL)||
	 (cmd == OP_INVOKESTATIC)) {
	int index = 256 * unsigned(code[i+1]) + unsigned(code[i+2]);
	System.out.print("invoke #" + index);
	index = classInfo.getConstPool().constantRelocate(index);
	code[i+1] = signed(index>>8);
	code[i+2] = signed(index&0xff);
     }
 
      if(cmd == OP_NEW) {
	int index = 256 * unsigned(code[i+1]) + unsigned(code[i+2]);
	System.out.print("new #" + index);
	index = classInfo.getConstPool().constantRelocate(index);
	code[i+1] = signed(index>>8);
	code[i+2] = signed(index&0xff);
      }

      if(cmd == OP_TABLESWITCH) {
	System.out.println("tableswitch");
      }
      
      // code translations to reduce number of instructions
      if(cmd == OP_ASTORE)      code[i] = signed(OP_ISTORE);
      if(cmd == OP_ASTORE_0)    code[i] = signed(OP_ISTORE_0);
      if(cmd == OP_ASTORE_1)    code[i] = signed(OP_ISTORE_1);
      if(cmd == OP_ASTORE_2)    code[i] = signed(OP_ISTORE_2);
      if(cmd == OP_ASTORE_3)    code[i] = signed(OP_ISTORE_3);
      if(cmd == OP_ACONST_NULL) code[i] = signed(OP_ICONST_0);
      if(cmd == OP_ALOAD_0)     code[i] = signed(OP_ILOAD_0);
      if(cmd == OP_ALOAD_1)     code[i] = signed(OP_ILOAD_1);
      if(cmd == OP_ALOAD_2)     code[i] = signed(OP_ILOAD_2);
      if(cmd == OP_ALOAD_3)     code[i] = signed(OP_ILOAD_3);

      // we don't need these conversions, since ints, bytes and
      // shorts are the same internal type
      if(cmd == OP_I2B)         code[i] = signed(OP_NOP);
      if(cmd == OP_I2C)         code[i] = signed(OP_NOP);
      if(cmd == OP_I2S)         code[i] = signed(OP_NOP);

      i += PARAMETER_BYTES[cmd];
    }
  }
}
