package dto;

public class RegistrationResult extends ServiceResult {
    private String token;
    private String userId;
    public RegistrationResult(int status, String message, String token,String userId) {
        super(status, message);
        this.token = token;
        this.userId =userId;
    }
    public String getUserId() {
        return userId;
    }
    public String getToken() {
        return token;
    }
}
