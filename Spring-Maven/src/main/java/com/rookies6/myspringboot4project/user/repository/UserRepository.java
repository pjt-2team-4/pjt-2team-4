package com.rookies6.myspringboot4project.user.repository;

import com.rookies6.myspringboot4project.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 학번(studentNumber) 검색 -> 이메일(email) 검색으로 변경
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크용
    boolean existsByEmail(String email);
}