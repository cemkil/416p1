import java.nio.ByteBuffer;
import java.util.Arrays;

public class Converters {

    public static int messageByteToSize(byte[] msg) {
        byte[] sizeB = Arrays.copyOfRange(msg, 2, 6);
        int size = ByteBuffer.wrap(sizeB).getInt();
        return size;
    }

    public static String messageBytetoMessage(byte[] msg) {
        byte[] message = Arrays.copyOfRange(msg, 6, 6 + messageByteToSize(msg));
        return new String(message);
    }

    public static char messageByteToPhase(byte[] msg) {
        return (char) msg[0];
    }

    public static char messageByteToType(byte[] msg) {
        return (char) msg[1];
    }

    public static byte[] createMessageByte (char phase, char type, String message){
            int size = message.length();
            byte[] byteSize = ByteBuffer.allocate(4).putInt(size).array();
            byte[] byteInfo = new byte[2];
            byteInfo[0] = (byte) phase;
            byteInfo[1] = (byte) type;
            byte[] byteStr = message.getBytes();
            
            //create mesagetype
            byte[] byteMessage = new byte[byteInfo.length + byteSize.length + byteStr.length];
            System.arraycopy(byteInfo, 0, byteMessage, 0, byteInfo.length);
            System.arraycopy(byteSize, 0, byteMessage, byteInfo.length, byteSize.length);
            System.arraycopy(byteStr, 0, byteMessage, byteInfo.length + byteSize.length, byteStr.length);
            
            return byteMessage; 
    }
    public static byte[] createMessageByte (char phase, char type, byte[] byteSize, byte[] byteStr){
        byte[] byteInfo = new byte[2];
        byteInfo[0] = (byte) phase;
        byteInfo[1] = (byte) type;

        //create mesagetype
        byte[] byteMessage = new byte[byteInfo.length + byteSize.length + byteStr.length];
        System.arraycopy(byteInfo, 0, byteMessage, 0, byteInfo.length);
        System.arraycopy(byteSize, 0, byteMessage, byteInfo.length, byteSize.length);
        System.arraycopy(byteStr, 0, byteMessage, byteInfo.length + byteSize.length, byteStr.length);

        return byteMessage;
    }
}