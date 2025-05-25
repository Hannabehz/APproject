package dto;

import Entity.userProfile;

public class userdto{
    private String name;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    private userProfile profile;
    public userdto(){}
    public userdto(String name, String lastName, String phoneNumber, String email, String password) {
        profile = new userProfile();
        this.name = name;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;

    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return name;
    }
}
