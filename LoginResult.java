package dto;

public class LoginResult extends ServiceResult {
    private String token;
    private UserDTO user;

    public LoginResult(int status, String message, String token, UserDTO user) {
        super(status, message);
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserDTO getUser() {
        return user;
    }
}
