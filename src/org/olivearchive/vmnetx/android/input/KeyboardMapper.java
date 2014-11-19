/*
   Android Keyboard Mapping

   Copyright 2013 Thinstuff Technologies GmbH, Author: Martin Fleisz

   This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
   If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/


package org.olivearchive.vmnetx.android.input;

import android.view.KeyEvent;

public class KeyboardMapper
{
    // interface that gets called for input handling
    public interface KeyProcessingListener {
        abstract void processVirtualKey(int virtualKeyCode, boolean down);
        abstract void processUnicodeKey(int unicodeKey);
    }

    private KeyProcessingListener listener = null;

    private static final int[] keymapAndroid;

    final static int VK_LBUTTON = 0x01;
    final static int VK_RBUTTON = 0x02;
    final static int VK_CANCEL = 0x03;
    final static int VK_MBUTTON = 0x04;
    final static int VK_XBUTTON1 = 0x05;
    final static int VK_XBUTTON2 = 0x06;
    final static int VK_BACK = 0x08;
    final static int VK_TAB     = 0x09;
    final static int VK_CLEAR = 0x0C;
    final static int VK_RETURN = 0x0D;
    final static int VK_SHIFT = 0x10;
    final static int VK_CONTROL = 0x11;
    final static int VK_MENU = 0x12;
    final static int VK_PAUSE = 0x13;
    final static int VK_CAPITAL = 0x14;
    final static int VK_KANA = 0x15;
    final static int VK_HANGUEL = 0x15;
    final static int VK_HANGUL = 0x15;
    final static int VK_JUNJA = 0x17;
    final static int VK_FINAL = 0x18;
    final static int VK_HANJA = 0x19;
    final static int VK_KANJI = 0x19;
    final static int VK_ESCAPE = 0x1B;
    final static int VK_CONVERT = 0x1C;
    final static int VK_NONCONVERT = 0x1D;
    final static int VK_ACCEPT = 0x1E;
    final static int VK_MODECHANGE = 0x1F;
    final static int VK_SPACE = 0x20;
    final static int VK_PRIOR = 0x21;
    final static int VK_NEXT = 0x22;
    final static int VK_END     = 0x23;
    final static int VK_HOME = 0x24;
    final static int VK_LEFT = 0x25;
    final static int VK_UP = 0x26;
    final static int VK_RIGHT = 0x27;
    final static int VK_DOWN = 0x28;
    final static int VK_SELECT = 0x29;
    final static int VK_PRINT = 0x2A;
    final static int VK_EXECUTE = 0x2B;
    final static int VK_SNAPSHOT = 0x2C;
    final static int VK_INSERT = 0x2D;
    final static int VK_DELETE = 0x2E;
    final static int VK_HELP = 0x2F;
    final static int VK_KEY_0 = 0x30;
    final static int VK_KEY_1 = 0x31;
    final static int VK_KEY_2 = 0x32;
    final static int VK_KEY_3 = 0x33;
    final static int VK_KEY_4 = 0x34;
    final static int VK_KEY_5 = 0x35;
    final static int VK_KEY_6 = 0x36;
    final static int VK_KEY_7 = 0x37;
    final static int VK_KEY_8 = 0x38;
    final static int VK_KEY_9 = 0x39;
    final static int VK_KEY_A = 0x41;
    final static int VK_KEY_B = 0x42;
    final static int VK_KEY_C = 0x43;
    final static int VK_KEY_D = 0x44;
    final static int VK_KEY_E = 0x45;
    final static int VK_KEY_F = 0x46;
    final static int VK_KEY_G = 0x47;
    final static int VK_KEY_H = 0x48;
    final static int VK_KEY_I = 0x49;
    final static int VK_KEY_J = 0x4A;
    final static int VK_KEY_K = 0x4B;
    final static int VK_KEY_L = 0x4C;
    final static int VK_KEY_M = 0x4D;
    final static int VK_KEY_N = 0x4E;
    final static int VK_KEY_O = 0x4F;
    final static int VK_KEY_P = 0x50;
    final static int VK_KEY_Q = 0x51;
    final static int VK_KEY_R = 0x52;
    final static int VK_KEY_S = 0x53;
    final static int VK_KEY_T = 0x54;
    final static int VK_KEY_U = 0x55;
    final static int VK_KEY_V = 0x56;
    final static int VK_KEY_W = 0x57;
    final static int VK_KEY_X = 0x58;
    final static int VK_KEY_Y = 0x59;
    final static int VK_KEY_Z = 0x5A;
    final static int VK_LWIN = 0x5B;
    final static int VK_RWIN = 0x5C;
    final static int VK_APPS = 0x5D;
    final static int VK_SLEEP = 0x5F;
    final static int VK_NUMPAD0    = 0x60;
    final static int VK_NUMPAD1    = 0x61;
    final static int VK_NUMPAD2    = 0x62;
    final static int VK_NUMPAD3    = 0x63;
    final static int VK_NUMPAD4    = 0x64;
    final static int VK_NUMPAD5    = 0x65;
    final static int VK_NUMPAD6    = 0x66;
    final static int VK_NUMPAD7    = 0x67;
    final static int VK_NUMPAD8    = 0x68;
    final static int VK_NUMPAD9    = 0x69;
    final static int VK_MULTIPLY = 0x6A;
    final static int VK_ADD = 0x6B;
    final static int VK_SEPARATOR = 0x6C;
    final static int VK_SUBTRACT = 0x6D;
    final static int VK_DECIMAL = 0x6E;
    final static int VK_DIVIDE = 0x6F;
    final static int VK_F1 = 0x70;
    final static int VK_F2 = 0x71;
    final static int VK_F3 = 0x72;
    final static int VK_F4 = 0x73;
    final static int VK_F5 = 0x74;
    final static int VK_F6 = 0x75;
    final static int VK_F7 = 0x76;
    final static int VK_F8 = 0x77;
    final static int VK_F9 = 0x78;
    final static int VK_F10 = 0x79;
    final static int VK_F11 = 0x7A;
    final static int VK_F12 = 0x7B;
    final static int VK_F13 = 0x7C;
    final static int VK_F14 = 0x7D;
    final static int VK_F15 = 0x7E;
    final static int VK_F16 = 0x7F;
    final static int VK_F17 = 0x80;
    final static int VK_F18 = 0x81;
    final static int VK_F19 = 0x82;
    final static int VK_F20 = 0x83;
    final static int VK_F21 = 0x84;
    final static int VK_F22 = 0x85;
    final static int VK_F23 = 0x86;
    final static int VK_F24 = 0x87;
    final static int VK_NUMLOCK = 0x90;
    final static int VK_SCROLL = 0x91;
    final static int VK_LSHIFT = 0xA0;
    final static int VK_RSHIFT = 0xA1;
    final static int VK_LCONTROL = 0xA2;
    final static int VK_RCONTROL = 0xA3;
    final static int VK_LMENU = 0xA4;
    final static int VK_RMENU = 0xA5;
    final static int VK_BROWSER_BACK = 0xA6;
    final static int VK_BROWSER_FORWARD = 0xA7;
    final static int VK_BROWSER_REFRESH = 0xA8;
    final static int VK_BROWSER_STOP = 0xA9;
    final static int VK_BROWSER_SEARCH = 0xAA;
    final static int VK_BROWSER_FAVORITES = 0xAB;
    final static int VK_BROWSER_HOME = 0xAC;
    final static int VK_VOLUME_MUTE = 0xAD;
    final static int VK_VOLUME_DOWN = 0xAE;
    final static int VK_VOLUME_UP = 0xAF;
    final static int VK_MEDIA_NEXT_TRACK = 0xB0;
    final static int VK_MEDIA_PREV_TRACK = 0xB1;
    final static int VK_MEDIA_STOP = 0xB2;
    final static int VK_MEDIA_PLAY_PAUSE = 0xB3;
    final static int VK_LAUNCH_MAIL = 0xB4;
    final static int VK_LAUNCH_MEDIA_SELECT = 0xB5;
    final static int VK_LAUNCH_APP1 = 0xB6;
    final static int VK_LAUNCH_APP2 = 0xB7;
    final static int VK_OEM_1 = 0xBA;
    final static int VK_OEM_SEMICOLON = 0xBA;
    final static int VK_OEM_EQUALS = 0xBB;    
    final static int VK_OEM_COMMA = 0xBC;
    final static int VK_OEM_MINUS = 0xBD;
    final static int VK_OEM_PERIOD = 0xBE;
    final static int VK_OEM_2 = 0xBF;
    final static int VK_OEM_3 = 0xC0;
    final static int VK_ABNT_C1 = 0xC1;
    final static int VK_ABNT_C2 = 0xC2;
    final static int VK_OEM_4 = 0xDB;
    final static int VK_OEM_5 = 0xDC;
    final static int VK_OEM_6 = 0xDD;
    final static int VK_OEM_7 = 0xDE;
    final static int VK_OEM_8 = 0xDF;
    final static int VK_OEM_102 = 0xE2;
    final static int VK_PROCESSKEY = 0xE5;
    final static int VK_PACKET = 0xE7;
    final static int VK_ATTN = 0xF6;
    final static int VK_CRSEL = 0xF7;
    final static int VK_EXSEL = 0xF8;
    final static int VK_EREOF = 0xF9;
    final static int VK_PLAY = 0xFA;
    final static int VK_ZOOM = 0xFB;
    final static int VK_NONAME = 0xFC;
    final static int VK_PA1    = 0xFD;
    final static int VK_OEM_CLEAR = 0xFE;
    final static int VK_EXT_KEY = 0x00000100;

    // Indicates we should add shift to the event.
    private static final int KEY_FLAG_SHIFT = 0x20000000;

    static {
        keymapAndroid = new int[256];

        keymapAndroid[KeyEvent.KEYCODE_0] = VK_KEY_0;
        keymapAndroid[KeyEvent.KEYCODE_1] = VK_KEY_1;
        keymapAndroid[KeyEvent.KEYCODE_2] = VK_KEY_2;
        keymapAndroid[KeyEvent.KEYCODE_3] = VK_KEY_3;
        keymapAndroid[KeyEvent.KEYCODE_4] = VK_KEY_4;
        keymapAndroid[KeyEvent.KEYCODE_5] = VK_KEY_5;
        keymapAndroid[KeyEvent.KEYCODE_6] = VK_KEY_6;
        keymapAndroid[KeyEvent.KEYCODE_7] = VK_KEY_7;
        keymapAndroid[KeyEvent.KEYCODE_8] = VK_KEY_8;
        keymapAndroid[KeyEvent.KEYCODE_9] = VK_KEY_9;

        keymapAndroid[KeyEvent.KEYCODE_A] = VK_KEY_A;
        keymapAndroid[KeyEvent.KEYCODE_B] = VK_KEY_B;
        keymapAndroid[KeyEvent.KEYCODE_C] = VK_KEY_C;
        keymapAndroid[KeyEvent.KEYCODE_D] = VK_KEY_D;
        keymapAndroid[KeyEvent.KEYCODE_E] = VK_KEY_E;
        keymapAndroid[KeyEvent.KEYCODE_F] = VK_KEY_F;
        keymapAndroid[KeyEvent.KEYCODE_G] = VK_KEY_G;
        keymapAndroid[KeyEvent.KEYCODE_H] = VK_KEY_H;
        keymapAndroid[KeyEvent.KEYCODE_I] = VK_KEY_I;
        keymapAndroid[KeyEvent.KEYCODE_J] = VK_KEY_J;
        keymapAndroid[KeyEvent.KEYCODE_K] = VK_KEY_K;
        keymapAndroid[KeyEvent.KEYCODE_L] = VK_KEY_L;
        keymapAndroid[KeyEvent.KEYCODE_M] = VK_KEY_M;
        keymapAndroid[KeyEvent.KEYCODE_N] = VK_KEY_N;
        keymapAndroid[KeyEvent.KEYCODE_O] = VK_KEY_O;
        keymapAndroid[KeyEvent.KEYCODE_P] = VK_KEY_P;
        keymapAndroid[KeyEvent.KEYCODE_Q] = VK_KEY_Q;
        keymapAndroid[KeyEvent.KEYCODE_R] = VK_KEY_R;
        keymapAndroid[KeyEvent.KEYCODE_S] = VK_KEY_S;
        keymapAndroid[KeyEvent.KEYCODE_T] = VK_KEY_T;
        keymapAndroid[KeyEvent.KEYCODE_U] = VK_KEY_U;
        keymapAndroid[KeyEvent.KEYCODE_V] = VK_KEY_V;
        keymapAndroid[KeyEvent.KEYCODE_W] = VK_KEY_W;
        keymapAndroid[KeyEvent.KEYCODE_X] = VK_KEY_X;
        keymapAndroid[KeyEvent.KEYCODE_Y] = VK_KEY_Y;
        keymapAndroid[KeyEvent.KEYCODE_Z] = VK_KEY_Z;

        keymapAndroid[KeyEvent.KEYCODE_DEL] = VK_BACK;
        keymapAndroid[KeyEvent.KEYCODE_ENTER] = VK_RETURN;
        keymapAndroid[KeyEvent.KEYCODE_SPACE] = VK_SPACE;
        keymapAndroid[KeyEvent.KEYCODE_SHIFT_LEFT] = VK_LSHIFT;
        keymapAndroid[KeyEvent.KEYCODE_SHIFT_RIGHT] = VK_RSHIFT;

        keymapAndroid[KeyEvent.KEYCODE_DPAD_DOWN] = VK_DOWN | VK_EXT_KEY;
        keymapAndroid[KeyEvent.KEYCODE_DPAD_LEFT] = VK_LEFT | VK_EXT_KEY;
        keymapAndroid[KeyEvent.KEYCODE_DPAD_RIGHT] = VK_RIGHT | VK_EXT_KEY;
        keymapAndroid[KeyEvent.KEYCODE_DPAD_UP] = VK_UP | VK_EXT_KEY;

        keymapAndroid[92] = VK_PRIOR | VK_EXT_KEY;
        keymapAndroid[93] = VK_NEXT | VK_EXT_KEY;
        keymapAndroid[111] = VK_ESCAPE;
        keymapAndroid[112] = VK_DELETE | VK_EXT_KEY;
        keymapAndroid[113] = VK_LCONTROL;
        keymapAndroid[114] = VK_RCONTROL;
        keymapAndroid[115] = VK_CAPITAL;
        keymapAndroid[116] = VK_SCROLL;
        keymapAndroid[120] = VK_PRINT;
        keymapAndroid[121] = VK_PAUSE;
        keymapAndroid[122] = VK_HOME | VK_EXT_KEY;
        keymapAndroid[123] = VK_END | VK_EXT_KEY;
        keymapAndroid[124] = VK_INSERT | VK_EXT_KEY;
        keymapAndroid[131] = VK_F1;
        keymapAndroid[132] = VK_F2;
        keymapAndroid[133] = VK_F3;
        keymapAndroid[134] = VK_F4;
        keymapAndroid[135] = VK_F5;
        keymapAndroid[136] = VK_F6;
        keymapAndroid[137] = VK_F7;
        keymapAndroid[138] = VK_F8;
        keymapAndroid[139] = VK_F9;
        keymapAndroid[140] = VK_F10;
        keymapAndroid[141] = VK_F11;
        keymapAndroid[142] = VK_F12;
        keymapAndroid[143] = VK_NUMLOCK;
        
        keymapAndroid[KeyEvent.KEYCODE_TAB] = VK_TAB;

        keymapAndroid[KeyEvent.KEYCODE_COMMA] = VK_OEM_COMMA;
        keymapAndroid[KeyEvent.KEYCODE_PERIOD] = VK_OEM_PERIOD;
        keymapAndroid[KeyEvent.KEYCODE_MINUS] = VK_OEM_MINUS;
        keymapAndroid[KeyEvent.KEYCODE_SEMICOLON] = VK_OEM_SEMICOLON;
        keymapAndroid[KeyEvent.KEYCODE_PLUS] = VK_ADD;
        keymapAndroid[KeyEvent.KEYCODE_EQUALS] = VK_OEM_EQUALS;

        keymapAndroid[KeyEvent.KEYCODE_APOSTROPHE] = VK_OEM_7;

        keymapAndroid[KeyEvent.KEYCODE_BACKSLASH] = VK_OEM_5;
        keymapAndroid[KeyEvent.KEYCODE_GRAVE] = VK_OEM_3;    
        keymapAndroid[KeyEvent.KEYCODE_LEFT_BRACKET] = VK_OEM_4;        
        keymapAndroid[KeyEvent.KEYCODE_RIGHT_BRACKET] = VK_OEM_6;        

        keymapAndroid[KeyEvent.KEYCODE_BACK] = VK_ESCAPE;
        
        keymapAndroid[KeyEvent.KEYCODE_SLASH] = VK_OEM_2;
        keymapAndroid[KeyEvent.KEYCODE_AT] = VK_KEY_2 | KEY_FLAG_SHIFT;
        keymapAndroid[KeyEvent.KEYCODE_POUND] = VK_KEY_3 | KEY_FLAG_SHIFT;
        keymapAndroid[KeyEvent.KEYCODE_STAR] = VK_KEY_8 | KEY_FLAG_SHIFT;
    }

    public void setKeyProcessingListener(KeyProcessingListener listener)  {
        this.listener = listener;
    }

    public boolean processAndroidKeyEvent(KeyEvent event) {
        switch(event.getAction())
        {
            // we only process down events
            case KeyEvent.ACTION_UP:
            {
                return false;
            }            
            
            case KeyEvent.ACTION_DOWN:
            {    
                // if a modifier is pressed we will send a VK event (if possible) so that key combinations will be 
                // recognized correctly. Otherwise we will send the unicode key. At the end we will reset all modifiers
                // and notifiy our listener.
                int vkcode = getVirtualKeyCode(event.getKeyCode());
                //android.util.Log.e("KeyMapper", "VK KeyCode is: " + vkcode);
                if ((vkcode & KEY_FLAG_SHIFT) != 0){
                    vkcode = vkcode & ~KEY_FLAG_SHIFT;
                    listener.processVirtualKey(VK_LSHIFT, true);
                    listener.processVirtualKey(vkcode, true);
                    listener.processVirtualKey(vkcode, false);                                        
                    listener.processVirtualKey(VK_LSHIFT, false);
                // if we got a valid vkcode send it - except for letters/numbers if a modifier is active
                } else if (vkcode > 0 && (event.getMetaState() & (KeyEvent.META_ALT_ON | KeyEvent.META_SHIFT_ON | KeyEvent.META_SYM_ON)) == 0) {
                    listener.processVirtualKey(vkcode, true);
                    listener.processVirtualKey(vkcode, false);
                }
                else if(event.isShiftPressed() && vkcode != 0)
                {
                    listener.processVirtualKey(VK_LSHIFT, true);
                    listener.processVirtualKey(vkcode, true);
                    listener.processVirtualKey(vkcode, false);                                        
                    listener.processVirtualKey(VK_LSHIFT, false);
                }
                else if(event.getUnicodeChar() != 0) 
                    listener.processUnicodeKey(event.getUnicodeChar());
                else
                    return false;
                             
                return true;
            }

            case KeyEvent.ACTION_MULTIPLE:
            {
                String str = event.getCharacters();
                for(int i = 0; i < str.length(); i++)
                    listener.processUnicodeKey(str.charAt(i));
                return true;
            }
            
            default:
                break;                
        }
        return false;
    }
    
    private int getVirtualKeyCode(int keycode) {
        if(keycode >= 0 && keycode <= 0xFF)
            return keymapAndroid[keycode];
        return 0;
    }
}

