package com.example._4.__thuc_hanh_quan_he.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@Table(name = "ROLE")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    int id;

    @Column(name = "ROLE_NAME",unique = true,nullable = false,length = 50)
    String roleName;

    @Column(name = "DESCRIPTION",length = 225)
    String description;

}
