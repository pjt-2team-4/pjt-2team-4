package com.rookies6.myspringboot4project.sec.codefile.controller;

import com.rookies6.myspringboot4project.sec.codefile.dto.CodeFileDTO;
import com.rookies6.myspringboot4project.sec.codefile.service.CodeFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code-file")
@RequiredArgsConstructor
public class CodeFileController {

    private final CodeFileService codeFileService;

    @PostMapping
    public ResponseEntity<CodeFileDTO.Response> createCodeFile(@Valid @RequestBody CodeFileDTO.Request request) {
        return ResponseEntity.ok(codeFileService.createCodeFile(request));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CodeFileDTO.Response>> getCodeFilesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(codeFileService.getCodeFilesByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeFileDTO.Response> getCodeFileById(@PathVariable Long id) {
        return ResponseEntity.ok(codeFileService.getCodeFileById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CodeFileDTO.Response> updateCodeFile(
            @PathVariable Long id, @Valid @RequestBody CodeFileDTO.Request request) {
        return ResponseEntity.ok(codeFileService.updateCodeFile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCodeFile(@PathVariable Long id) {
        codeFileService.deleteCodeFile(id);
        return ResponseEntity.ok().build();
    }
}