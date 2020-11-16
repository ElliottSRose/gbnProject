import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Sender {

    private static int windowSize = 4;
    private static int currentSeqNum = 0;// maybe this should be 1
    private static int totalPacketsSent = 0;
    private static long start = 0;
    private static long end = 0;
    private static int max = 4095;
    private static int userNum;
    private static int testNum;
    private static int packetLoss;
    private static int startIndex;
    private static int endIndex;
    private static byte[] data;
    private static ByteBuffer buf;
    private static int lostSeqNum;

    static int packetLossSim() {
        //user inputs number 0-99
        Scanner reader = new Scanner(System.in);
        do {
            System.out.println("Please enter a number from 0-99:");
            userNum = Integer.parseInt(reader.nextLine());
            if (userNum < 0 || userNum > 99) {
                System.out.println("Invalid. Please enter a number from 0-99: \n");
                testNum = -1;   //wrong input = loop again
            } else if (userNum >= 0 && userNum <= 99) {
                return userNum;
            }
        } while (userNum >= 0 && userNum <= 99 || testNum == -1);
        return userNum;
    }

    static int getWindowSize(String ErrorProtocol) {
        if (ErrorProtocol == "GBN") {
            return 4;
        }
        else {
            return 1;
        }
    }

    static byte[] setupPacket(int max, int currentSeqNum, byte[] totalBytes) {
        startIndex = max * currentSeqNum;
        endIndex = startIndex + max;
        data = new byte[4095];
        data = Arrays.copyOfRange(totalBytes, startIndex, endIndex); //get bytes for the current packet from totalBytes
        byte[] seqNum = {(byte) currentSeqNum};
        byte[] destination = new byte[data.length + seqNum.length];
        System.arraycopy(seqNum, 0, destination, 0, seqNum.length);
        System.arraycopy(data, 0, destination, seqNum.length, data.length);
        return destination;
    }
    public static void main(String args[]) throws IOException {
        byte[] totalBytes = Files.readAllBytes(Paths.get("test.txt")); //convert entire file to bytes
        try {//SETUP OF CONNECTION AND FILE
            DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(200); //arbitrary milliseconds
//            start = System.nanoTime(); //start the timer
            while (true) {//BEGIN SENDING DATA
                int pseudoNum = new Random(System.currentTimeMillis()).nextInt(); //pseudonumber generated using random seed set to current system time
                int eachRoundCompare = currentSeqNum + windowSize;
                userNum = packetLossSim();//user inputs number 0-99

                while (currentSeqNum < eachRoundCompare) {  //CHECK IF WINDOW SIZE HAS BEEN SENT
                    byte [] destination = setupPacket(max, currentSeqNum, totalBytes);
                    System.out.println("destination.length = " + destination.length);
                    System.out.println("currentSeqNum = " + currentSeqNum);
                    DatagramPacket pkt = new DatagramPacket(destination,4096, InetAddress.getLocalHost(), 8888);

                    if (pseudoNum < 100) { // SIMULATE LOSS ELSE SEND
                        ++packetLoss; //keep count of total packet losses
                        lostSeqNum = currentSeqNum;
                    } else { //SEND PACKETS
                        ds.send(pkt);
                        ++currentSeqNum;
                        ++totalPacketsSent;
                        if (currentSeqNum==eachRoundCompare) { //if end of round check for ack
                            try {
                                byte[] ackBytes = new byte[200]; //arbitrary number for ACK bytes
                                DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length); //create new Datagram packet for ACK coming in -- need to fill in parameters
                                ds.receive(ack);
                            } catch (SocketTimeoutException e) {
                                System.out.println("Timeout error, resend packets from: " + lostSeqNum);
                            }
                        }
                    if (totalBytes.length < currentSeqNum * 4096) {
                        System.out.println("Packets sent: " + totalPacketsSent);
                        System.out.println("Lost packets: " + packetLoss);
                        //                    end = System.nanoTime(); //end the timer -- unreachable statement error
                        //                    System.out.println("Elapsed time: " + (end - start));
                        return;
                    }    }} }


                }
        finally {
            System.out.println("Goodbye!");
        }

}};

