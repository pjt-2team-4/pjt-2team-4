package com.rookies6.myspringboot4project.user.dto;

import com.rookies6.myspringboot4project.common.validation.DynamicSize;
import com.rookies6.myspringboot4project.user.entity.Student;
import com.rookies6.myspringboot4project.user.entity.StudentDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

public class StudentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "이름은 필수입니다.")
        @DynamicSize(
                maxProperty = "student.name.max-length",
                message = "이름의 최대 길이를 초과했습니다."
        )
        private String name;

        @NotBlank(message = "학번은 필수입니다.")
        @DynamicSize(
                maxProperty = "student.number.max-length",
                message = "학번의 최대 길이를 초과했습니다."
        )
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

        private String name;

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
        @DynamicSize(
                maxProperty = "student.phone.max-length",
                message = "전화번호가 너무 깁니다."
        )
        @Pattern(
                regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        private String phoneNumber;

        @DynamicSize(
                maxProperty = "student.email.max-length",
                message = "이메일이 너무 깁니다."
        )
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

            private String phoneNumber;

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
