import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class udpServer {
    public static void main(String[] args) throws IOException {
//        File destfile = new File("hello.txt");
//        FileOutputStream fos = new FileOutputStream(destfile);
//        BufferedOutputStream bos = new BufferedOutputStream(fos);

        DatagramSocket ds = new DatagramSocket(8888);// open port to listen
        byte[] receive = new byte[1024];
        ByteBuffer buff = ByteBuffer.wrap(receive);
        DatagramPacket DpReceive = null;
        int lastPacketReceived = 0;

        while (true) {
            System.out.println("Server is awaiting packets...");
            DpReceive = new DatagramPacket(receive, receive.length); // create appropriate sized data packet
            ds.receive(DpReceive);// retrieve data
            String msg = new String(DpReceive.getData(), DpReceive.getOffset(), DpReceive.getLength());// to format the bytes back into strings
            String currentMessageSet = "";
            int currentSeqNum = DpReceive.getData()[0];
            System.out.println("Received currentSeqNum is " + currentSeqNum);

            if(currentSeqNum == lastPacketReceived+1) {//if this is the next packet, then append to string
                currentMessageSet = currentMessageSet.concat(msg.substring(1));//append the text without the sequence number
                lastPacketReceived = currentSeqNum;

                if ((currentSeqNum+1) % 4 == 0 && currentSeqNum!= 0) {//time to deal with ack
                    byte[] ackData = new byte[1024];
                    DatagramPacket ack = new DatagramPacket(ackData, ackData.length, DpReceive.getAddress(), DpReceive.getPort());
                    ds.send(ack);
                    System.out.println("Sent ack");
        //            bos.write(msg);// We can add this later so we don't need to continue rewriting
                }
            }
            else {
                    System.out.println("Missed a packet, deleting current round");
                    currentMessageSet = "";
                    currentSeqNum = currentSeqNum-(currentSeqNum%4);
                }
//            System.out.println("after round "+ currentSeqNum +"currentMessageSet is " + currentMessageSet);
            buff.clear();
            buff.rewind(); //reset buffer
        }
    }
    };
