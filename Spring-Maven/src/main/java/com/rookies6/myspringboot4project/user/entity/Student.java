package com.rookies6.myspringboot4project.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String studentNumber;

    @OneToOne(
            fetch = FetchType.LAZY,
            mappedBy = "student",
            cascade = CascadeType.ALL
    )
    private StudentDetail studentDetail;
}
