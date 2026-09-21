package com.rookies6.myspringboot4project.user.service;

import com.rookies6.myspringboot4project.user.dto.StudentDTO;
import com.rookies6.myspringboot4project.user.entity.Student;
import com.rookies6.myspringboot4project.user.entity.StudentDetail;
import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.user.repository.StudentDetailRepository;
import com.rookies6.myspringboot4project.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentDetailRepository studentDetailRepository;

    public List<StudentDTO.Response> getAllStudents() {

        return studentRepository.findAllWithStudentDetail()
                .stream()
                .map(StudentDTO.Response::fromEntity)
                .toList();
    }

    public StudentDTO.Response getStudentById(Long id) {

        Student student = studentRepository
                .findByIdWithStudentDetail(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "Student",
                                "id",
                                id
                        )
                );

        return StudentDTO.Response.fromEntity(student);
    }

    public StudentDTO.Response getStudentByStudentNumber(
            String studentNumber) {

        Student student = studentRepository
                .findByStudentNumber(studentNumber)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "Student",
                                "student number",
                                studentNumber
                        )
                );

        return StudentDTO.Response.fromEntity(student);
    }

    @Transactional
    public StudentDTO.Response createStudent(StudentDTO.Request request) {

        // 1. 학번 중복 검사
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(
                    ErrorCode.STUDENT_NUMBER_DUPLICATE,
                    request.getStudentNumber()
            );
        }

        // 2. Student 생성
        Student student = Student.builder()
                .name(request.getName())
                .studentNumber(request.getStudentNumber())
                .build();

        // 3. StudentDetail이 전달된 경우
        if (request.getDetailRequest() != null) {

            StudentDTO.StudentDetailDTO detailRequest =
                    request.getDetailRequest();

            // 이메일 중복 검사
            if (detailRequest.getEmail() != null
                    && !detailRequest.getEmail().isBlank()
                    && studentDetailRepository.existsByEmail(
                            detailRequest.getEmail())) {

                throw new BusinessException(
                        ErrorCode.EMAIL_DUPLICATE,
                        detailRequest.getEmail()
                );
            }

            // 전화번호 중복 검사
            if (detailRequest.getPhoneNumber() != null
                    && !detailRequest.getPhoneNumber().isBlank()
                    && studentDetailRepository.existsByPhoneNumber(
                            detailRequest.getPhoneNumber())) {

                throw new BusinessException(
                        ErrorCode.PHONE_NUMBER_DUPLICATE,
                        detailRequest.getPhoneNumber()
                );
            }

            // StudentDetail 생성
            StudentDetail studentDetail = StudentDetail.builder()
                    .address(detailRequest.getAddress())
                    .phoneNumber(detailRequest.getPhoneNumber())
                    .email(detailRequest.getEmail())
                    .dateOfBirth(detailRequest.getDateOfBirth())
                    .student(student)              // Owner
                    .build();

            // 양방향 관계 설정
            student.setStudentDetail(studentDetail);
        }

        // cascade = ALL
        // Student 저장 시 StudentDetail도 함께 저장
        Student savedStudent = studentRepository.save(student);

        return StudentDTO.Response.fromEntity(savedStudent);
    }



    // 학생 수정

    @Transactional
    public StudentDTO.Response updateStudent(
            Long id,
            StudentDTO.Request request) {

        // 1. 기존 학생 조회
        Student student = studentRepository
                .findByIdWithStudentDetail(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "Student",
                                "id",
                                id
                        )
                );

        // 2. 학번 중복 검사
        if (!student.getStudentNumber()
                .equals(request.getStudentNumber())
                && studentRepository.existsByStudentNumber(
                        request.getStudentNumber())) {

            throw new BusinessException(
                    ErrorCode.STUDENT_NUMBER_DUPLICATE,
                    request.getStudentNumber()
            );
        }

        // 3. 기본 학생 정보 수정
        student.setName(request.getName());
        student.setStudentNumber(request.getStudentNumber());

        // 4. 상세정보 수정
        if (request.getDetailRequest() != null) {

            // ★ Request가 아님
            StudentDTO.StudentDetailDTO detailRequest =
                    request.getDetailRequest();

            StudentDetail currentDetail =
                    student.getStudentDetail();

            // 5. 이메일 중복 검사
            String newEmail = detailRequest.getEmail();

            if (newEmail != null
                    && !newEmail.isBlank()
                    && (currentDetail == null
                    || !newEmail.equals(currentDetail.getEmail()))
                    && studentDetailRepository.existsByEmail(newEmail)) {

                throw new BusinessException(
                        ErrorCode.EMAIL_DUPLICATE,
                        newEmail
                );
            }

            // 6. 전화번호 중복 검사
            String newPhoneNumber =
                    detailRequest.getPhoneNumber();

            if (newPhoneNumber != null
                    && !newPhoneNumber.isBlank()
                    && (currentDetail == null
                    || !newPhoneNumber.equals(
                            currentDetail.getPhoneNumber()))
                    && studentDetailRepository
                    .existsByPhoneNumber(newPhoneNumber)) {

                throw new BusinessException(
                        ErrorCode.PHONE_NUMBER_DUPLICATE,
                        newPhoneNumber
                );
            }

            // 7. 기존 상세정보가 없으면 새로 생성
            if (currentDetail == null) {

                currentDetail = StudentDetail.builder()
                        .student(student)       // Owner
                        .build();

                student.setStudentDetail(currentDetail);
            }

            // 8. 상세정보 수정
            currentDetail.setAddress(
                    detailRequest.getAddress()
            );

            currentDetail.setPhoneNumber(
                    detailRequest.getPhoneNumber()
            );

            currentDetail.setEmail(
                    detailRequest.getEmail()
            );

            currentDetail.setDateOfBirth(
                    detailRequest.getDateOfBirth()
            );
        }

        // Dirty Checking
        return StudentDTO.Response.fromEntity(student);
    }



    // 학생 삭제
    @Transactional
    public void deleteStudent(Long id) {

        if (!studentRepository.existsById(id)) {

            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "Student",
                    "id",
                    id
            );
        }

        studentRepository.deleteById(id);
    }
}
