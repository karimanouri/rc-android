package com.cps.rotationdetector;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ResponseListener implements Runnable{
    private static final int ackPacketSize = 2;
    byte[] receive = new byte[100];
    private DatagramSocket socket;
    private PacketMan packetMan;
    public ResponseListener(DatagramSocket socket, PacketMan packetMan){
        this.socket = socket;
        this.packetMan = packetMan;
    }
    public void run(){
        DatagramPacket dpPacket = new DatagramPacket(receive, receive.length);
        byte[] ackPack = new byte[ackPacketSize];
        while (true) {
            try {
                socket.receive(dpPacket);
                ackPack[0] = receive[0];
                ackPack[1] = receive[1];
                packetMan.ackedPacket(ackPack);
            } catch (Exception ex) { }
        }
    }
}