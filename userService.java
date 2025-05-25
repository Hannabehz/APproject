package service;

import Entity.Profile;
import Entity.User;
import DAO.userDAO;
import dto.serviceResult;
import dto.userdto;
public class userService {
    public userService() {}
        userDAO userDAO=new userDAO();
        public serviceResult save(userdto dto){
            if(userDAO.isPhoneTaken(dto.getPhoneNumber())){
                return new serviceResult(false,"Phone number already taken");

            }else{
                userDAO.save(new User(dto.getFirstName(),dto.getLastName(),));
            }
        }

}
        String firstName,String lastName, String password, Date createdAt, Profile profile