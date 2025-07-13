package dto;

public class ProfileResult extends ServiceResult {
    private UserDTO user;

    public ProfileResult(int status, UserDTO user) {
        super(status, "");
        this.user = user;
    }

    public UserDTO getUser() {
        return user;
    }
}