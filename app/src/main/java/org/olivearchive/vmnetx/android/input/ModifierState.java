/**
 * Copyright (C) 2014-2015 Carnegie Mellon University
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package org.olivearchive.vmnetx.android.input;

import java.util.Map;
import java.util.TreeMap;

class ModifierState {
    private static class Device implements Comparable<Device> {
        private static enum Type {
            HARDWARE,
            ON_SCREEN_BUTTONS,
        };

        private final Type type;
        private final int id;

        private Device(Type type, int id) {
            this.type = type;
            this.id = id;
        }

        public int hashCode() {
            return type.ordinal() * 10007 + id;
        }

        public boolean equals(Object o) {
            if (o instanceof Device) {
                Device d = (Device) o;
                return d.type == type && d.id == id;
            }
            return false;
        }

        public int compareTo(Device o) {
            if (type != o.type)
                return type.compareTo(o.type);
            return Integer.valueOf(id).compareTo(o.id);
        }
    }

    static class DeviceState {
        private int modifiers = 0;

        int get() {
            return modifiers;
        }

        void press(int modifier) {
            modifiers |= modifier;
        }

        void release(int modifier) {
            modifiers &= ~modifier;
        }

        // returns true if modifier is now set
        boolean toggle(int modifier) {
            modifiers ^= modifier;
            return (modifiers & modifier) != 0;
        }

        void set(int modifiers) {
            this.modifiers = modifiers;
        }

        void clear() {
            modifiers = 0;
        }
    }

    private static final Device DEVICE_ON_SCREEN_BUTTONS =
            new Device(Device.Type.ON_SCREEN_BUTTONS, 0);

    private Map<Device, DeviceState> states =
            new TreeMap<Device, DeviceState>();

    DeviceState getDeviceState(int id) {
        return getDeviceState(new Device(Device.Type.HARDWARE, id));
    }

    DeviceState getOnScreenButtonState() {
        return getDeviceState(DEVICE_ON_SCREEN_BUTTONS);
    }

    private DeviceState getDeviceState(Device device) {
        DeviceState state = states.get(device);
        if (state == null) {
            state = new DeviceState();
            states.put(device, state);
        }
        return state;
    }

    int getModifiers() {
        int modifiers = 0;
        for (DeviceState state : states.values()) {
            modifiers |= state.get();
        }
        return modifiers;
    }
}
