package com.rookies6.myspringboot4project.user.repository;

import com.rookies6.myspringboot4project.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
        SELECT s
        FROM Student s
        LEFT JOIN FETCH s.studentDetail
        """)
    List<Student> findAllWithStudentDetail();

    @Query("""
        SELECT s
        FROM Student s
        LEFT JOIN FETCH s.studentDetail
        WHERE s.id = :id
        """)
    Optional<Student> findByIdWithStudentDetail(@Param("id") Long id);

    @Query("""
        SELECT s
        FROM Student s
        LEFT JOIN FETCH s.studentDetail
        WHERE s.studentNumber = :studentNumber
        """)
    Optional<Student> findByStudentNumber(
            @Param("studentNumber") String studentNumber
    );

    boolean existsByStudentNumber(String studentNumber);
}
