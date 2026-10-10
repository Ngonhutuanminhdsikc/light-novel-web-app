package com.example._4.__thuc_hanh_quan_he.Repository;

import com.example._4.__thuc_hanh_quan_he.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    // Tìm kiếm theo tên
    List<Author> findByNameContainingIgnoreCase(String name);

    Author findByName(String name);
}
