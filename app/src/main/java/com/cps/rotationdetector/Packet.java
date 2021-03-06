package com.cps.rotationdetector;

public class Packet {
    private int angle,speed;
    private PacketMode mode;
    private long timeStamp;
    private int seqNumber;
    //speed 1-4 angle 0-180
    Packet(int speed, int angle, PacketMode mode, int seqNumber){
        this.speed = speed-1;
        this.angle = angle/3;
        this.mode  = mode;
        this.seqNumber = seqNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Packet))
            return false;
        Packet other = (Packet)obj;
        return this.speed == other.speed && this.angle == other.angle && this.mode == other.mode;
    }

    byte[] getBytes(){
        byte[] return_value = new byte[4];
        seqNumber       = seqNumber % 512;
        return_value[0] = (byte) 170;
        return_value[1] = (byte)((speed<<6) + (angle&0x3F));
        return_value[2] = (byte)((mode.ordinal()<<5) + (seqNumber/16));
        return_value[3] = (byte)(((seqNumber%16)<<4) + 10);
        timeStamp = System.currentTimeMillis();
        return return_value;
    }

    long getTimeStamp() {
        return timeStamp;
    }

    int getSeqNumber() {
        return seqNumber;
    }

    static String byte_to_str(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}