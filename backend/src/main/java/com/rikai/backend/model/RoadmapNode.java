package com.rikai.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rikai.backend.model.Enum.DifficultyLevel;
import com.rikai.backend.model.Enum.NodeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "roadmap_nodes")
public class RoadmapNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id")
    @JsonBackReference("roadmap_nodes")
    Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference("parent_children")
    RoadmapNode parent;

    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @OrderBy("orderIndex ASC")
    @JsonManagedReference
    List<RoadmapNode> children;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    NodeType nodeType;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "pass_condition", columnDefinition = "TEXT")
    String passCondition;

    @Column(name = "learning_outcome", columnDefinition = "TEXT")
    String learningOutcome;

    @Column(name = "estimated_hours")
    Double estimatedHours;

    @Column(name = "order_index")
    Integer orderIndex;

    @Column(name = "assessment_method")
    String assessmentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    DifficultyLevel difficulty;

    @Column(name = "is_expanded")
    @Builder.Default
    Boolean isExpanded = false;

    @ManyToMany
    @JoinTable(
            name = "roadmap_node_tags",
            joinColumns = @JoinColumn(name = "node_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    Set<Tag> tags = new HashSet<>();

    @Column(name = "created_at")
    Instant createdAt;

    @Column(name = "updated_at")
    Instant updatedAt;
}
