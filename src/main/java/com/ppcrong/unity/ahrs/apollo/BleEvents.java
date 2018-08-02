package com.ppcrong.unity.ahrs.apollo;

/**
 * The BLE events for notify
 */

public class BleEvents {

    public static class NotifyAhrsMoveEvent {

        private float x;
        private float y;
        private float z;

        // Apollo MUST: no-arg ctr
        public NotifyAhrsMoveEvent() {
        }

        public NotifyAhrsMoveEvent(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return x + "," + y + "," + z;
        }
    }

    public static class NotifyAhrsRotateEvent {

        /*
         * Quaternion components x,y,z,w
         */
        private float x;
        private float y;
        private float z;
        private float w;

        // Apollo MUST: no-arg ctr
        public NotifyAhrsRotateEvent() {
        }

        public NotifyAhrsRotateEvent(float x, float y, float z, float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public String toString() {
            return x + "," + y + "," + z + "," + w;
        }
    }
}
