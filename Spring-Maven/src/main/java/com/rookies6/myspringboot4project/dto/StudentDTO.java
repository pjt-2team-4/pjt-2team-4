package com.rookies6.myspringboot4project.dto;

import com.rookies6.myspringboot4project.entity.Student;
import com.rookies6.myspringboot4project.entity.StudentDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

public class StudentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다.")
        private String name;

        @NotBlank(message = "학번은 필수입니다.")
        @Size(max = 20, message = "학번은 20자를 초과할 수 없습니다.")
        private String studentNumber;

        @Valid
        private StudentDetailDTO detailRequest;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;

        @NotBlank(message = "이름은 필수입니다.")
        @Size(
                max = 100,
                message = "이름은 100자를 초과할 수 없습니다."
        )
        private String name;

        @NotBlank(message = "학번은 필수입니다.")
        @Size(max = 20, message = "학번은 20자를 초과할 수 없습니다.")
        @Pattern(
                regexp = "^[A-Z]{2}\\d{3}$",
                message = "학번 형식이 올바르지 않습니다."
        )
        private String studentNumber;

        private StudentDetailDTO.Response detail;

        public static Response fromEntity(Student student) {

            StudentDetail detail = student.getStudentDetail();

            return Response.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .studentNumber(student.getStudentNumber())
                    .detail(
                            detail != null
                                    ? StudentDetailDTO.Response.fromEntity(detail)
                                    : null
                    )
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentDetailDTO {

        private String address;

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        private String phoneNumber;

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        private LocalDate dateOfBirth;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {

            private Long id;
            private String address;
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(
                    regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$",
                    message = "전화번호 형식이 올바르지 않습니다."
            )
            private String phoneNumber;

            @Email(message = "이메일 형식이 올바르지 않습니다.")
            private String email;

            private LocalDate dateOfBirth;

            public static Response fromEntity(StudentDetail detail) {
                return Response.builder()
                        .id(detail.getId())
                        .address(detail.getAddress())
                        .phoneNumber(detail.getPhoneNumber())
                        .email(detail.getEmail())
                        .dateOfBirth(detail.getDateOfBirth())
                        .build();
            }
        }
    }
}
