import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AuthModule {
    private int tryLeft = 3;
    private boolean isUserName = true;
    private ArrayList<String> userList;
    private String username;
    private String password;

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    private boolean isAuthenticated = false;

    public boolean AuthenticateUser(String username, String password) {
        userList = new ArrayList<>();
        ReadFile(userList);
        for (String line : userList) {
            String[] user = line.split(" ");
            if (username.equals(user[0]) && password.equals(user[1])) {
                return true;
            }
        }

        userList.clear();
        return false;
    }

    public void ReadFile(ArrayList<String> users) {
        try {
            File myObj = new File("Users.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                users.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public boolean checkValidToken(byte[] msg, String ip) {
        String message = Converters.messageBytetoMessage(msg);
        String[] msgArr = message.split(" ");
        if (msgArr.length < 2) {
            return false;
        }
        try {
            File myObj = new File("Users.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] UserInfo = data.split(" ");
                if (UserInfo[2].equals(ip) && msgArr[msgArr.length - 1].equals(UserInfo[4])) {
                    return true;
                }
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }

    public byte[] processMessage(byte[] buffer, String ip, String port) {
        int size = Converters.messageByteToSize(buffer);
        char phase = Converters.messageByteToPhase(buffer);
        char type = Converters.messageByteToType(buffer);
        String user = Converters.messageBytetoMessage(buffer);
        if (isUserName) {
            isUserName = !isUserName;
            username = user;
            return Converters.createMessageByte('0', '1', "Enter Password: ");

        } else {
            password = user;
            if (AuthenticateUser(username, password)) {
                String token = username.substring(0, 3) + "44";
                writeToken(username, ip, port, token);
                setAuthenticated(true);
                return Converters.createMessageByte('0', '3', token);
            } else {
                if (tryLeft == 1) {
                    return Converters.createMessageByte('0', '2', "User does not exist");
                }
                tryLeft--;
                isUserName = !isUserName;
                return Converters.createMessageByte('0', '2', "Incorrect password. Enter the Username: ");
            }

        }

    }

    public void writeToken(String userName, String ip, String port, String token) {
        try {
            File file = new File("Users.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", newText = "";
            while ((line = reader.readLine()) != null) {
                String[] lineContent = line.split(" ");
                if (lineContent[0].equals(userName)) {
                    line = lineContent[0] + " " + lineContent[1] + " " + ip + " " + port + " " + token;
                }
                newText += line + "\r\n";

            }
            reader.close();
            // replace a word in a file
            // String newtext = oldtext.replaceAll("drink", "Love");

            // To replace a line in a file

            FileWriter writer = new FileWriter("Users.txt");
            writer.write(newText);
            writer.close();
        } catch (

        IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
