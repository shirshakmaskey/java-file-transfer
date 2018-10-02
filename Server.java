import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shirshak
 */
class TCPDataServer {
    public static void main(String[] args){
        try {
            ServerSocket server_socket = new ServerSocket(5252);
            while(true){
                new Thread(new ClientWorker(server_socket.accept())).start();
                
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPDataServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ClientWorker implements Runnable{
    
    private Socket target_socket;
    private DataInputStream din;
    private DataOutputStream dout;
    
    public ClientWorker(Socket receive_socket){
        try {
            target_socket = receive_socket;
            din = new DataInputStream(target_socket.getInputStream());
            dout = new DataOutputStream(target_socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void run() {
        RandomAccessFile rw = null;
        long current_file_pointer = 0;
        boolean loop_break = false;
        while(true){
            byte[] initialize = new byte[1];
            try {
                din.read(initialize,0,initialize.length);
                if(initialize[0] == 2){
                    byte[] cmd_buff = new byte[3];
                    din.read(cmd_buff,0,cmd_buff.length);
                    byte[] received_data = ReadStream();
                    switch(Integer.parseInt(new String(cmd_buff))){
                        case 124:
                            rw = new RandomAccessFile("/home/shirshak/Downloads/"+new String(received_data),"rw");
                            dout.write(CreateDataPacket("125".getBytes("UTF8"),String.valueOf(current_file_pointer).getBytes("UTF8")));
                            dout.flush();
                            break;
                        case 126:
                            rw.seek(current_file_pointer);
                            rw.write(received_data);
                            current_file_pointer = rw.getFilePointer();
                            System.out.println("Downloaded: "+((float)current_file_pointer)/1048576+" MB");
                            dout.write(CreateDataPacket("125".getBytes("UTF8"), String.valueOf(current_file_pointer).getBytes("UTF8")));
                            dout.flush();
                            break;
                        case 127:
                            if("close".equals(new String(received_data))){
                                loop_break = true;
                            }
                            break;
                    }
                }
                if(loop_break == true){
                    target_socket.close();
                    System.out.println("Server Socket is closed.");
                    break;
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    private byte[] ReadStream(){
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
            Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data_buff;
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
            Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packet;
        
    }
    
}
