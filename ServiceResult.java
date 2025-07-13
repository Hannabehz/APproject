package dto;

public class ServiceResult {
    private int status;
    private String message;
    public ServiceResult(int status, String message) {
        this.status = status;
        this.message = message;
    }
    public int getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
}
