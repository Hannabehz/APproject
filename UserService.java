package service;

import entity.BankInfo;
import entity.User;
import DAO.UserDAO;
import dto.*;
import io.jsonwebtoken.JwtException;
import org.mindrot.jbcrypt.BCrypt;
import util.JwtUtil;

import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    public ServiceResult save(UserDTO userDTO) {
        if (userDTO.getFullName() == null || userDTO.getPhone() == null ||
                userDTO.getPassword() == null || userDTO.getRole() == null ||
                userDTO.getAddress() == null) {
            return new ServiceResult(400, "Required fields are missing");
        }


        if (!userDTO.getRole().matches("buyer|seller|courier")) {
            return new ServiceResult(400, "Invalid role");
        }


        if (userDTO.getBankInfo() != null &&
                (userDTO.getBankInfo().getBankName() == null ||
                        userDTO.getBankInfo().getAccountNumber() == null)) {
            return new ServiceResult(400, "Bank info incomplete");
        }


        if (userDTO.getEmail() != null &&
                !userDTO.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return new ServiceResult(400, "Invalid email format");
        }


        if (userDAO.isPhoneTaken(userDTO.getPhone())) {
            return new ServiceResult(409, "Phone number already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setPassword(BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt()));
        user.setRole(userDTO.getRole());
        user.setAddress(userDTO.getAddress());
        user.setProfileImageBase64(userDTO.getProfileImageBase64());

        if (userDTO.getBankInfo() != null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBankName(userDTO.getBankInfo().getBankName());
            bankInfo.setAccountNumber(userDTO.getBankInfo().getAccountNumber());
            user.setBankInfo(bankInfo);
        }

        // Save user
        try {
            userDAO.save(user);
            String token = JwtUtil.generateToken(user.getId().toString());
            return new RegistrationResult(200, "User registered successfully", user.getId().toString(), token);
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server Error: " + e.getMessage());
        }
    }


        public ServiceResult login(String phone, String password) {
            if (phone == null || password == null) {
                return new ServiceResult(400, "Phone and password are required");
            }

            Optional<User> userOptional = userDAO.findByPhone(phone);
            if (!userOptional.isPresent()) {
                return new ServiceResult(401, "Unauthorized: Invalid phone or password");
            }

            User user = userOptional.get();
            if (user.getPassword() == null) {
                return new ServiceResult(500, "Internal server error: User password is null");
            }
            if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
                return new ServiceResult(500, "Internal server error: User password is not hashed");
            }
            if (!BCrypt.checkpw(password, user.getPassword())) {
                return new ServiceResult(401, "Unauthorized: Invalid password");
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setFullName(user.getFullName());
            userDTO.setPhone(user.getPhone());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());
            userDTO.setAddress(user.getAddress());
            userDTO.setProfileImageBase64(user.getProfileImageBase64());
            if (user.getBankInfo() != null) {
                BankInfoDTO bankInfoDTO = new BankInfoDTO();
                bankInfoDTO.setBankName(user.getBankInfo().getBankName());
                bankInfoDTO.setAccountNumber(user.getBankInfo().getAccountNumber());
                userDTO.setBankInfo(bankInfoDTO);
            }
            String token = JwtUtil.generateToken(user.getId().toString());
            return new LoginResult(200, "User logged in successfully", token, userDTO);
        }

    public ServiceResult logout(String token) {
        try {
            String userId = JwtUtil.validateToken(token);
            if (userId == null) {
                return new ServiceResult(401, "Unauthorized: Invalid or expired token");
            }
            return new ServiceResult(200, "User logged out successfully");
        } catch (JwtException e) {
            return new ServiceResult(401, "Unauthorized: " + e.getMessage());
        }
    }

    public ServiceResult getProfile(String token) {
        try {
            System.out.println("Validating token: " + token); // لاگ توکن
            String userId = JwtUtil.validateToken(token);
            System.out.println("Extracted userId: " + userId); // لاگ userId
            if (userId == null) {
                return new ServiceResult(401, "Unauthorized: Invalid or expired token");
            }

            Optional<User> userOptional = userDAO.findById(UUID.fromString(userId));
            System.out.println("User found: " + userOptional.isPresent()); // لاگ نتیجه جستجو
            if (userOptional.isEmpty()) {
                return new ServiceResult(401, "Unauthorized: User not found");
            }

            User user = userOptional.get();
            UserDTO userDTO = new UserDTO();
            userDTO.setFullName(user.getFullName());
            userDTO.setPhone(user.getPhone());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());
            userDTO.setAddress(user.getAddress());
            userDTO.setProfileImageBase64(user.getProfileImageBase64());
            if (user.getBankInfo() != null) {
                BankInfoDTO bankInfoDTO = new BankInfoDTO();
                bankInfoDTO.setBankName(user.getBankInfo().getBankName());
                bankInfoDTO.setAccountNumber(user.getBankInfo().getAccountNumber());
                userDTO.setBankInfo(bankInfoDTO);
            }
            return new ProfileResult(200, userDTO);
        } catch (JwtException e) {
            return new ServiceResult(401, "Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: " + e.getMessage());
        }
    }

    public ServiceResult updateProfile(String token, UserDTO userDTO) {
        // Validate JWT token
        try {

            String userId = JwtUtil.validateToken(token);
            Optional<User> userOptional = userDAO.findById(UUID.fromString(userId));
            if (userId == null) {
                return new ServiceResult(401, "Unauthorized: Invalid or expired token");
            }
            if (userOptional.isEmpty()) {
                return new ServiceResult(401, "Unauthorized: User not found");
            }
            if (userDTO.getFullName() == null || userDTO.getPhone() == null ||
                    userDTO.getRole() == null || userDTO.getAddress() == null) {
                return new ServiceResult(400, "Invalid input: Required fields are missing");
            }

            if (!userDTO.getRole().matches("buyer|seller|courier")) {
                return new ServiceResult(400, "Invalid input: Invalid role");
            }

            if (userDTO.getBankInfo() != null &&
                    (userDTO.getBankInfo().getBankName() == null ||
                            userDTO.getBankInfo().getAccountNumber() == null)) {
                return new ServiceResult(400, "Invalid input: Bank info incomplete");
            }

            if (userDTO.getEmail() != null &&
                    !userDTO.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return new ServiceResult(400, "Invalid input: Invalid email format");
            }

            Optional<User> existingUser = userDAO.findByPhone(userDTO.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().toString().equals(userId)) {
                return new ServiceResult(400, "Invalid input: Phone number already exists");
            }

            // Update user
            User user = userOptional.get();
            user.setFullName(userDTO.getFullName());
            user.setPhone(userDTO.getPhone());
            user.setEmail(userDTO.getEmail());
            user.setRole(userDTO.getRole());
            user.setAddress(userDTO.getAddress());
            user.setProfileImageBase64(userDTO.getProfileImageBase64());
            if (user.getPassword() == null) {
                // جلوگیری از نال شدن رمز عبور
                return new ServiceResult(400, "Invalid input: Password cannot be null");
            }
            if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
                String hashedPassword = BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt());
                user.setPassword(hashedPassword);
            }
            if (userDTO.getBankInfo() != null) {
                BankInfo bankInfo = new BankInfo();
                bankInfo.setBankName(userDTO.getBankInfo().getBankName());
                bankInfo.setAccountNumber(userDTO.getBankInfo().getAccountNumber());
                user.setBankInfo(bankInfo);
            } else {
                user.setBankInfo(null);
            }

            userDAO.update(user);

            UserDTO responseDTO = new UserDTO();
            responseDTO.setFullName(user.getFullName());
            responseDTO.setPhone(user.getPhone());
            responseDTO.setEmail(user.getEmail());
            responseDTO.setRole(user.getRole());
            responseDTO.setAddress(user.getAddress());
            responseDTO.setProfileImageBase64(user.getProfileImageBase64());
            responseDTO.setBankInfo(user.getBankInfo() != null ? new BankInfoDTO(user.getBankInfo().getBankName(), user.getBankInfo().getAccountNumber()) : null);

            return new ProfileResult(200, responseDTO);

        } catch (JwtException e) {
            return new ServiceResult(401, "Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            return new ServiceResult(500, "Internal server error: " + e.getMessage());
        }
    }

}