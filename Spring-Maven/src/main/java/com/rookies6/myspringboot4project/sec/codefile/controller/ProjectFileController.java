package com.rookies6.myspringboot4project.sec.projectfile.controller;

import com.rookies6.myspringboot4project.sec.projectfile.dto.ProjectFileDTO;
import com.rookies6.myspringboot4project.sec.projectfile.service.ProjectFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-file")
@RequiredArgsConstructor
public class ProjectFileController {

    private final ProjectFileService projectFileService;

    @PostMapping
    public ResponseEntity<ProjectFileDTO.Response> createProjectFile(@Valid @RequestBody ProjectFileDTO.Request request) {
        return ResponseEntity.ok(projectFileService.createProjectFile(request));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectFileDTO.Response>> getProjectFilesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectFileService.getProjectFilesByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectFileDTO.Response> getProjectFileById(@PathVariable Long id) {
        return ResponseEntity.ok(projectFileService.getProjectFileById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectFileDTO.Response> updateProjectFile(
            @PathVariable Long id, @Valid @RequestBody ProjectFileDTO.Request request) {
        return ResponseEntity.ok(projectFileService.updateProjectFile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectFile(@PathVariable Long id) {
        projectFileService.deleteProjectFile(id);
        return ResponseEntity.ok().build();
    }
}