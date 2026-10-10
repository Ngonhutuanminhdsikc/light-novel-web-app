package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.UserAccountRepository;
import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetailsService implements UserDetailsService {

    UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

        return User.withUsername(user.getUserName())
                .password(user.getPassWord())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleName())))
                .disabled(user.getStatus() == UserAccount.Status.BANNED)
                .build();
    }
}