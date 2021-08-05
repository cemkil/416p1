import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class ApiClientHandler {
    private char type;

    public byte[] createRequestMessage(String message, char phase){
        switch (message.split(" ")[0]) {
            case "0":
                type = '0';
                break;
            case "1":
                type = '1';
                break;
            default:
                type = '1';
                break;
        }
        return Converters.createMessageByte(phase, type, message);
    }
    public String handleResponse(byte[] msg, String userName){
        if(type == '1'){
            try {
                ByteArrayInputStream stream= new ByteArrayInputStream(msg);
                BufferedImage image = ImageIO.read(stream);
                ImageIO.write(image, "jpg", new File(userName+".jpg"));
                return "Image reached";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }else{
            JSONObject obj = new JSONObject(new String(msg));
            String av = String.valueOf(obj.getJSONObject(obj.keySet().stream().filter(i -> i.startsWith("8")).findAny().get()).getJSONObject("PRE").getFloat("av"));
            return av;

        }

    }
    public boolean hashChecker(byte[] msg, byte[] hashMsg){
        String hashStr = Converters.messageBytetoMessage(hashMsg);
        String str = new String(msg);
        if(String.valueOf(str.hashCode()).equalsIgnoreCase(hashStr) && Converters.messageByteToType(hashMsg) == '7'){
            System.out.println("hash equality");
            return true;
        }
        System.out.println("hashes are different");
        return false;
    }
}
