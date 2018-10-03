/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author shirshak
 */

public class TCPDataClient {
    public static File[] files;
    public static Queue<Object> q;
    public static Scanner threads;
    public static void main(String[] args){
        try{
            int nc = 1,i;
            Runnable sendFile = new SendFiles();
            System.out.println("Welcome to the program");
            System.out.println("Please enter the number of concurrency.");
            threads = new Scanner(System.in);
            nc = threads.nextInt();
            
            JFileChooser jfc = new JFileChooser();
            jfc.setMultiSelectionEnabled(true);
            jfc.showOpenDialog(null);
            files = jfc.getSelectedFiles();
            q = new LinkedList<>(Arrays.asList(files));
            System.out.println(java.time.LocalTime.now());   
            for(i=1;i<=nc;i++){
                new Thread(sendFile).start();
            }
        }
        finally{
            threads.close();
        }
    }
    public void removeItem(){
        q.remove();
    }
    public Queue currentItem(){
        return(this.q);
    }
}
class SendFiles implements Runnable{
    public SendFiles(){
    }

    @Override
    public synchronized void run() {
      try {
          TCPDataClient tcpc = new TCPDataClient();
          while((tcpc.currentItem().peek())!=null){
              List<File> files = new ArrayList(tcpc.currentItem());
              SendFiles obj = new SendFiles();
              Socket obj_client = new Socket(InetAddress.getByName("127.0.0.1"), 5252);
              DataInputStream din = new DataInputStream(obj_client.getInputStream());
              DataOutputStream dout = new DataOutputStream(obj_client.getOutputStream());
              File target_file = files.get(0);
              dout.write(obj.CreateDataPacket("124".getBytes("UTF8"), target_file.getName().getBytes("UTF8")));
              dout.flush();
              RandomAccessFile rw = new RandomAccessFile(target_file, "r");
              long current_file_pointer = 0;
              boolean loop_break = false;
              while (true) {
                  if (din.read() == 2) {
                      byte[] cmd_buff = new byte[3];
                      din.read(cmd_buff, 0, cmd_buff.length);
                      byte[] received_buff = obj.ReadStream(din);
                      switch (Integer.parseInt(new String(cmd_buff))) {
                          case 125:
                              current_file_pointer = Long.valueOf(new String(received_buff));
                              int buff_len = (int) (rw.length() - current_file_pointer < 65536 ? rw.length() - current_file_pointer : 65536);
                              byte[] temp_buff = new byte[buff_len];
                              if (current_file_pointer != rw.length()) {
                                  rw.seek(current_file_pointer);
                                  rw.read(temp_buff, 0, temp_buff.length);
                                  dout.write(obj.CreateDataPacket("126".getBytes("UTF8"), temp_buff));
                                  dout.flush();
                                  System.out.println("Uploaded: " + ((float) current_file_pointer / rw.length()) * 100 + " %");
                              } else {
                                  loop_break = true;
                              }
                              break;
                      }
                  }
                  if (loop_break == true) {
                      System.out.println("Stop Server Informed");
                      dout.write(obj.CreateDataPacket("127".getBytes("UTF8"), "close".getBytes("UTF8")));
                      dout.flush();
                      obj_client.close();
                      System.out.println("Client Socket closed");
                      tcpc.removeItem();
                      break;
                  }
              }
          }
        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPDataClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPDataClient.class.getName()).log(Level.SEVERE, null, ex);
        }
      System.out.println(java.time.LocalTime.now());  
    }
    
        private byte[] CreateDataPacket(byte[] cmd,byte[] data){
        byte[] packet = null;
        try {
            byte[] initialize = new byte[1];
            byte[] seperator = new byte[1];
            initialize[0]=2;seperator[0]=4;
            byte[] data_length = String.valueOf(data.length).getBytes("UTF8");
            packet = new byte[initialize.length+cmd.length+seperator.length+data_length.length+data.length];
            System.arraycopy(initialize,0,packet,0,initialize.length);
            System.arraycopy(cmd,0,packet,initialize.length,cmd.length);
            System.arraycopy(data_length,0,packet,initialize.length+cmd.length,data_length.length);
            System.arraycopy(seperator,0,packet,initialize.length+data_length.length+cmd.length,seperator.length);
            System.arraycopy(data,0,packet,initialize.length+data_length.length+seperator.length+cmd.length,data.length);
            
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TCPDataClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packet;
        
    }
    private byte[] ReadStream(DataInputStream din){
        byte[] data_buff = null;
        try {
            int b = 0;
            String  buff_length = "";
            while((b=din.read())!=4){
                buff_length += (char)b;
            }
            int data_length = Integer.parseInt(buff_length);
            data_buff = new byte[data_length];
            int byte_read=0,byte_offset=0;
            while(byte_offset<data_length){
                byte_read = din.read(data_buff,byte_offset,data_length-byte_offset);
                byte_offset += byte_read;
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(TCPDataClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data_buff;
    }
    
}
