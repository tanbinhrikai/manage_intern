package com.rikai.backend.model;

import com.rikai.backend.model.Enum.CriteriaCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "evaluation_criteria")
public class EvaluationCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    CriteriaCategory category;

    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    /**
     * Trọng số của tiêu chí (mặc định = 1)
     */
    @Column(name = "weight", precision = 5, scale = 2)
    @Builder.Default
    BigDecimal weight = BigDecimal.ONE;

    /**
     * Tham chiếu đến tiêu chí cha (null nếu là tiêu chí gốc/main criteria)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    EvaluationCriteria parent;

    /**
     * Danh sách các tiêu chí con
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<EvaluationCriteria> children = new ArrayList<>();

    /**
     * Thứ tự hiển thị trong cùng cấp
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    Integer displayOrder = 0;

    /**
     * Trạng thái hoạt động của tiêu chí
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    /**
     * Danh sách định nghĩa mô tả cho từng mức điểm
     */
    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<CriteriaScoreDefinition> scoreDefinitions = new ArrayList<>();

    /**
     * Helper method để kiểm tra tiêu chí có phải là tiêu chí cha (main criteria) không
     */
    public boolean isMainCriteria() {
        return parent == null;
    }

    /**
     * Helper method để kiểm tra tiêu chí có phải là tiêu chí con (sub-criteria) không
     */
    public boolean isSubCriteria() {
        return parent != null;
    }
}
