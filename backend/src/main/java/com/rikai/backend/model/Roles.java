package com.rikai.backend.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Roles {
    @Id
    @Column(name = "role_name", nullable = false)
    String roleName;

    @Column(name = "description")
    String description;

    @OneToMany(mappedBy = "role")
    Set<Users> users;
}
