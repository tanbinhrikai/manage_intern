package com.rikai.backend.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "positions")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title" , nullable = false)
    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    Department department;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @OneToMany(mappedBy = "position")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Set<Intern> interns;
}