
public class AuthenticationClient {
    private String token;
    public String getToken() {
        return token;
    }

    public byte[] authenticationBytes(String message) {
        byte[] byteMessage = Converters.createMessageByte('0', '0', message);
        return byteMessage;
    }

    public boolean isAuthenticated(byte[] serverMessage) {
        if (Converters.messageByteToType(serverMessage) == '3') {
            setToken(Converters.messageBytetoMessage(serverMessage));
            return true;
        } else {
            return false;
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String addToken(String message) {
      return message+ " "+ token;
    }

}
