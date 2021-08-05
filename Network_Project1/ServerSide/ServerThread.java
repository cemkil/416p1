import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

class ServerThread extends Thread {
    String authResponse;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected DataInputStream fileSocketIn;
    protected DataOutputStream fileSocketOut;

    public Socket getFileSocket() {
        return fileSocket;
    }

    public void setFileSocket(Socket fileSocket) {
        this.fileSocket = fileSocket;
    }

    protected Socket fileSocket;
    protected Socket s;
    private String line = new String();
    private String lines = new String();
    private byte[] readBuffer;
    private byte[] writeBuffer;
    private byte[] serverResponse;
    private AuthModule authModule;
    private String userMessage;
    private ApiHandler apiHandler;
    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s) {
        this.s = s;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from
     * the client
     */
    public void run() {
        try {
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());
            authModule = new AuthModule();
            authResponse = new String();
            userMessage = new String();
            serverResponse = new byte[2000000];

        } catch (IOException e) {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try {
            while (!Converters.messageBytetoMessage(serverResponse).equalsIgnoreCase("User does not exist")
                    && !userMessage.equalsIgnoreCase("quit")) {
                SocketAddress clientAdress = s.getRemoteSocketAddress();
                readBuffer = new byte[100];
                is.read(readBuffer, 0, readBuffer.length);
                char phase = Converters.messageByteToPhase(readBuffer);
                serverResponse = null;
                if (phase == '0') {

                    serverResponse = authModule.processMessage(readBuffer, clientAdress.toString().split(":")[0],
                            clientAdress.toString().split(":")[1]);
                    os.write(serverResponse);
                    os.flush();
                    if (authModule.isAuthenticated()) {
                        readBuffer = new byte[100];
                        is.read(readBuffer, 0, readBuffer.length);
                        if (new String(Arrays.copyOfRange(readBuffer, 0, 5)).equals("ready")) {
                            ServerSocket serverSocket;

                            try {
                                // int portNo = new Random().nextInt(1000)+5000;
                                int portNo = 5000;
                                serverSocket = new ServerSocket(portNo);
                                System.out.println("Oppened up a data socket on " + Inet4Address.getLocalHost());
                                os.write(ByteBuffer.allocate(4).putInt(portNo).array());
                                while (fileSocket == null) {
                                    ListenAndAccept(serverSocket);
                                }
                                apiHandler = new ApiHandler();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Server class.Constructor exception on oppening a server socket");
                            }
                        }

                    }
                } else {

                    if (authModule.checkValidToken(readBuffer, clientAdress.toString().split(":")[0])) {
                        serverResponse = apiHandler.handleApiMessage(readBuffer);

                        System.out.println("");
                        System.out.println(Converters.messageByteToSize(serverResponse));
                        System.out.println("");
                        byte[] test = Arrays.copyOfRange(serverResponse, serverResponse.length - 100, serverResponse.length);
                        for (byte ad : test) {
                            System.out.print(ad);
                        }
                        System.out.println();
                        System.out.println(Converters.messageByteToSize(serverResponse)+6);
                        fileSocketOut.write(serverResponse);
                        fileSocketOut.flush();
                        os.write(apiHandler.hashify(serverResponse));
                        os.flush();
                    }else{
                    System.out.println("invalid token");
                    }
                }
            }
        } catch (IOException e) {
            line = this.getName(); // reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        } catch (NullPointerException e) {
            line = this.getName(); // reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed  " + e);
        } finally {
            try {
                System.out.println("Closing the connection");
                if (is != null) {
                    is.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (os != null) {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.err.println("Socket Closed");
                }

            } catch (IOException ie) {
                System.err.println("Socket Close Error");
            }
        } // end finally
    }

    private void ListenAndAccept(ServerSocket socket) {
        try {
            fileSocket = socket.accept();
            System.out.println("A connection was established with a client on the address of "
                    + fileSocket.getRemoteSocketAddress());
            fileSocketIn = new DataInputStream(fileSocket.getInputStream());
            fileSocketOut = new DataOutputStream(fileSocket.getOutputStream());
            socket.close();

        }

        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server Class.Connection establishment error inside listen and accept function");
        }
    }

}
