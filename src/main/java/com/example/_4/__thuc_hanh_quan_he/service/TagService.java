package com.example._4.__thuc_hanh_quan_he.service;

import com.example._4.__thuc_hanh_quan_he.Repository.TagRepository;
import com.example._4.__thuc_hanh_quan_he.entity.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TagService {

    TagRepository tagRepository;

    public Tag create(Tag tag){
        return tagRepository.save(tag);
    }

    public Tag getById(int id){
        return tagRepository.findById(id).orElseThrow(() -> new RuntimeException("không thấy nhãn"));
    }

    public List<Tag> getAll(){
        return tagRepository.findAll();
    }

    public Tag update(int id,Tag tagDetails){
        Tag tag = getById(id);
        tag.setName(tagDetails.getName());
        tag.setSlug(tagDetails.getSlug());
        return tagRepository.save(tag);
    }

    public void delete(int id){
        tagRepository.deleteById(id);
    }

    public Tag getBySlug(String slug) {
        return tagRepository.findBySlug(slug);
    }

    public List<Tag> searchByName(String keyword) {
        return tagRepository.findByNameContainingIgnoreCase(keyword);
    }
    public long count() {
        return tagRepository.count();
    }
}
