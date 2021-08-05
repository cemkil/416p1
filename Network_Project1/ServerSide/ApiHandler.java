
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;


import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;

public class ApiHandler {
    private  final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private  byte[] handleAPOD(String date) {
        // create a client
        System.out.println(date);
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create("https://api.nasa.gov/planetary/apod?api_key=XC4zGyzD9rathZRzJqQ7XUTSv3csXjEY3YSK28gv&date=" + date.split(" ")[1])).build();

        HttpResponse<String> response;
        byte[] sizeArr;
        byte[] msg;
        int size;
        try {

                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response);
                JSONObject obj = new JSONObject(response.body());
                String imgURL;
                try{ imgURL = obj.getString("url");} catch (JSONException e) {
                    imgURL = obj.getString("hdurl");
                }

                retrieveImage(imgURL);
                BufferedImage image = ImageIO.read(new File("img.jpg"));

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", byteArrayOutputStream);
                size = byteArrayOutputStream.size();
                sizeArr = ByteBuffer.allocate(4).putInt(size).array();
                msg = byteArrayOutputStream.toByteArray();

            return Converters.createMessageByte('1','6',sizeArr,msg);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private  byte[] handleInsight() {
        // create a client
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create("https://api.nasa.gov/insight_weather/?api_key=XC4zGyzD9rathZRzJqQ7XUTSv3csXjEY3YSK28gv&feedtype=json&ver=1.0"))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(response.body());
            return Converters.createMessageByte('1','4',obj.toString());

        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public  byte[] handleApiMessage(byte[] msg){
        if(Converters.messageByteToType(msg)=='0'){
            return handleInsight();
        }else{
            return handleAPOD(Converters.messageBytetoMessage(msg));
        }
    }

    public  void retrieveImage(String img) {

        try {
            URL url = new URL(img);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream("img.jpg");
            byte[] b = new byte[2000000];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            System.out.println("could not retrieve image");
        }
    }

    public byte[] hashify(byte[] msg){
        String msgStr = Converters.messageBytetoMessage(msg);
        return Converters.createMessageByte('1','7', String.valueOf(msgStr.hashCode()));
    }
}
