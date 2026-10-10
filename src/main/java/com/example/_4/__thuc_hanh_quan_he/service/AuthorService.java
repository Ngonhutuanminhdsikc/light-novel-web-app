package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.AuthorRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Author;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthorService {

    AuthorRepository authorRepository;

    public Author create(Author author) { return authorRepository.save(author); }
    public List<Author> getAll() { return authorRepository.findAll(); }
    public Author getById(int id) {
        return authorRepository.findById(id).orElseThrow(() -> new RuntimeException("Không thấy tác giả"));
    }
    public Author update(int id, Author details) {
        Author author = getById(id);
        author.setName(details.getName());
        author.setDescription(details.getDescription());
        return authorRepository.save(author);
    }
    public void delete(int id) { authorRepository.deleteById(id); }

    public List<Author> searchByName(String keyword) {
        return authorRepository.findByNameContainingIgnoreCase(keyword);
    }
    public long count() {
        return authorRepository.count();
    }

    public Author findByName(String name) {
        return authorRepository.findByName(name);
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }
}
