import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer {
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    public static final int DEFAULT_SERVER_PORT = 4444;
    private Socket s;

    protected DataInputStream is;
    protected DataOutputStream os;

    protected DataInputStream fileIs;
    protected DataOutputStream fileOs;
    private ApiClientHandler request;
    protected String serverAddress;
    protected int serverPort;
    private char phase = '0';
    private char type = '0';
    private byte[] buffer;
    private byte[] hashBuffer;
    private AuthenticationClient authenticationClient;
    Socket fileSocket;

    /**
     *
     * @param address IP address of the server, if you are running the server on the
     *                same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port) {
        serverAddress = address;
        serverPort = port;

    }

    /**
     * Establishes a socket connection to the server that is identified by the
     * serverAddress and the serverPort
     */
    public void Connect() {
        try {
            s = new Socket(serverAddress, serverPort);
            // br= new BufferedReader(new InputStreamReader(System.in));
            /*
             * Read and write buffers on the socket
             */
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
            authenticationClient = new AuthenticationClient();

        } catch (IOException e) {
            // e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    /**
     * sends the message String to the server and retrives the answer
     * 
     * @param message input message string to the server
     * @return the received server answer
     */
    public String SendForAnswer(String message) {
        String response = new String();
        try {
            byte[] byteMessage;
            if (phase == '0') {
                byteMessage = authenticationClient.authenticationBytes(message);
                os.write(byteMessage);
                os.flush();

                byte[] buffer = new byte[100];
                is.read(buffer, 0, buffer.length);
                response = Converters.messageBytetoMessage(buffer);
                if (authenticationClient.isAuthenticated(buffer)) {
                    os.write("ready".getBytes());
                    os.flush();
                    buffer = new byte[100];
                    is.read(buffer, 0, buffer.length);
                    int socket = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, 4)).getInt();
                    fileSocket = new Socket(serverAddress, socket);
                    fileIs = new DataInputStream(fileSocket.getInputStream());
                    fileOs = new DataOutputStream(fileSocket.getOutputStream());
                    phase = '1';
                    response = "Success";
                    request = new ApiClientHandler();
                }
            } else {
                String msgStr = authenticationClient.addToken(message);
                byteMessage = request.createRequestMessage(msgStr, phase);
                os.write(byteMessage);
                os.flush();
                buffer = new byte[2000000];
                hashBuffer = new byte[2000000];
                byte[] info = new byte[2];
                byte[] siz = new byte[4];
                fileIs.read(info,0,2);
                fileIs.read(siz,0,4);
                int sizee = ByteBuffer.wrap(siz).getInt();
                System.out.println("test"+sizee);
                buffer = fileIs.readNBytes(sizee);
                is.read(hashBuffer);
                /*
                System.out.println("");
                System.out.println(Converters.messageByteToSize(buffer));
                System.out.println("");
                byte[] test = Arrays.copyOfRange(buffer, Converters.messageByteToSize(buffer) - 94, Converters.messageByteToSize(buffer)+6);
                for (byte ad : test) {
                    System.out.print(ad);
                }*/
                if(request.hashChecker(buffer,hashBuffer)){
                    response = request.handleResponse(buffer, authenticationClient.getToken());

                }else{
                    response = request.handleResponse(buffer, authenticationClient.getToken());
                    os.write(byteMessage);
                    os.flush();
                    System.out.println("error in file");
                }

                
                
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return response;
    }

    /**
     * Disconnects the socket and closes the buffers
     */
    public void Disconnect() {
        try {
            is.close();
            os.close();
            // br.close();
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
