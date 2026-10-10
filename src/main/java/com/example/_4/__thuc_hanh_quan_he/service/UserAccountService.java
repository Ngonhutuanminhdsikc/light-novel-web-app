package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.RoleRepository;
import com.example._4.__thuc_hanh_quan_he.Repository.UserAccountRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Role;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserAccountService {


    UserAccountRepository userAccountRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public UserAccount createUser(UserAccount user) {

        user.setPassWord(passwordEncoder.encode(user.getPassWord()));

        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Chưa có Role USER trong Database"));

        user.setRole(userRole);

        user.setStatus(UserAccount.Status.ACTIVE);

        return userAccountRepository.save(user);
    }


    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    public UserAccount getUserById(int id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + id));
    }

    @Transactional
    public UserAccount updateUser(int id, UserAccount details) {
        UserAccount user = getUserById(id);

        user.setEmail(details.getEmail());
        user.setFullName(details.getFullName());
        user.setBio(details.getBio());

        user.setAvatarUrl(details.getAvatarUrl());
        user.setBannerUrl(details.getBannerUrl());

        return userAccountRepository.save(user);
    }

    @Transactional
    public void changeStatus(int id, UserAccount.Status status) {
        UserAccount user = getUserById(id);
        user.setStatus(status);
        userAccountRepository.save(user);
    }

    public void deleteUser(int id) {
        userAccountRepository.deleteById(id);
    }

    public UserAccount findByUserName(String username) {
        return userAccountRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    public UserAccount findByEmail(String email) {
        return userAccountRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {

        String encodedPassword = passwordEncoder.encode(newPassword);

        userAccountRepository.updatePasswordNative(email, encodedPassword);

        System.out.println("DEBUG:  lệnh UPDATE trực tiếp cho email: " + email);
    }

    public List<UserAccount> searchUsers(String keyword) {
        return userAccountRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }
    public long count() {
        return userAccountRepository.count();
    }

    public UserAccount findByUsername(String username) {
        return userAccountRepository.findByUserName(username).orElse(null);
    }

}
