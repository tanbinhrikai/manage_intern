package com.rikai.backend.model;

import com.rikai.backend.model.Enum.NodeType;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "roadmap_nodes")
public class RoadmapNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Dùng IDENTITY vì ID là INT AI
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position; // Link tới bảng positions cũ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RoadmapNode parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<RoadmapNode> children;

    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    private String title;
    private String description;
    private String passCondition;
    private String learningOutcome;
    private Integer orderIndex;

    // ... Getters Setters
}