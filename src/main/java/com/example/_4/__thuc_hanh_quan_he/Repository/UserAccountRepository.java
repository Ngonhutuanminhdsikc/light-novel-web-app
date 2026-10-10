package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    // Kiểm tra xem tên đăng nhập đã có trong DB chưa
    boolean existsByUserName(String userName);

    // Tìm người dùng theo tên đăng nhập
    Optional<UserAccount> findByUserName(String userName);

    // Tìm kiếm người dùng bằng Email
    Optional<UserAccount> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user_account SET pass_word = :newPassword WHERE email = :email", nativeQuery = true)
    void updatePasswordNative(@Param("email") String email, @Param("newPassword") String newPassword);

    List<UserAccount> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);


}
