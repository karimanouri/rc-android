package com.cps.rotationdetector;

import android.util.Log;

class PacketMan{
    private Packet lastPack = null;
    private int lastAckSeq;
    private int seqNumber = 0;

    byte[] createPacket(int speed, int angle, PacketMode mode){
        speed = Math.max(speed,1);
        speed = Math.min(speed,4);
        angle = Math.max(angle,0);
        angle = Math.min(angle,180);
        Packet curPack = new Packet(speed, angle, mode, seqNumber);
        if(lastPack != null){
            if(curPack.equals(lastPack)){
                return null;
            }
        }
        lastPack = curPack;
        seqNumber++;
        return curPack.getBytes();
    }
    void ackedPacket(byte[] ackPacket){
        lastAckSeq = ackPacket[0] & 0xFF;
        lastAckSeq = lastAckSeq*2 + ((ackPacket[1]>>7)&0x01);
        Log.v("PacketMan", "acked: " + lastAckSeq);
    }
    byte[] intervalRun(){
        if (lastAckSeq != lastPack.getSeqNumber())
            if (System.currentTimeMillis() - lastPack.getTimeStamp() > 5)
                return lastPack.getBytes();
        return null;
    }
}