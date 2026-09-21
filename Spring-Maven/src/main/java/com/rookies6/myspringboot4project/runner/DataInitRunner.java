package com.rookies6.myspringboot4project.runner;

import com.rookies6.myspringboot4project.entity.Student;
import com.rookies6.myspringboot4project.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitRunner implements CommandLineRunner {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        log.info("Starting data initialization...");

        if (studentRepository.count() == 0) {
            createStudents();
        } else {
            log.info("Students already exist. Skipping data initialization.");
        }

        log.info("Data initialization completed successfully");
    }

    private void createStudents() {
        log.info("Creating students...");

        Student student1 = createStudent("Alice Johnson", "CS001");
        Student student2 = createStudent("Bob Smith", "CS002");

        Student student3 = createStudent("Charlie Brown", "EE001");
        Student student4 = createStudent("Diana Wilson", "EE002");

        Student student5 = createStudent("Edward Davis", "ME001");

        Student student6 = createStudent("Fiona Garcia", "BA001");
        Student student7 = createStudent("George Martinez", "BA002");

        Student student8 = createStudent("Helen Lee", "CS003");

        List<Student> students = studentRepository.saveAll(
                List.of(
                        student1,
                        student2,
                        student3,
                        student4,
                        student5,
                        student6,
                        student7,
                        student8
                )
        );

        log.info("Created {} students", students.size());
    }

    private Student createStudent(String name, String studentNumber) {
        return Student.builder()
                .name(name)
                .studentNumber(studentNumber)
                .build();
    }
}
