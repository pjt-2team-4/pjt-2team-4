package com.rookies6.myspringboot4project.user.controller;

import com.rookies6.myspringboot4project.user.dto.StudentDTO;
import com.rookies6.myspringboot4project.user.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// StudentController 클래스
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentDTO.Response>> getAllStudents() {
        List<StudentDTO.Response> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO.Response> getStudentById(
            @PathVariable Long id) {

        StudentDTO.Response student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/number/{studentNumber}")
    public ResponseEntity<StudentDTO.Response> getStudentByStudentNumber(
            @PathVariable String studentNumber) {

        StudentDTO.Response student =
                studentService.getStudentByStudentNumber(studentNumber);

        return ResponseEntity.ok(student);
    }

    @PostMapping
    public ResponseEntity<StudentDTO.Response> createStudent(
            @Valid @RequestBody StudentDTO.Request request) {

        StudentDTO.Response createdStudent =
                studentService.createStudent(request);

        return ResponseEntity.ok(createdStudent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO.Response> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO.Request request) {

        StudentDTO.Response updatedStudent =
                studentService.updateStudent(id, request);

        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {

        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }
}
