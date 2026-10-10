package com.example._4.__thuc_hanh_quan_he.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TAG")
@Builder
@EqualsAndHashCode(of = "id")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Integer id;

    @NotBlank(message = "Tên thể loại không được để trống!")
    @Size(min = 2, max = 50, message = "Tên thể loại phải từ 2 đến 50 ký tự!")
    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    String name;

    @NotBlank(message = "Slug thể loại không được để trống!")
    @Column(name = "SLUG", nullable = false, unique = true, length = 100)
    String slug;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    Set<Story> stories;
}