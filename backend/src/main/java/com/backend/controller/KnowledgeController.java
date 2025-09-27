package com.backend.controller;

import com.backend.entity.KnowledgeDocument;
import com.backend.service.KnowledgeService;
import com.backend.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public Result addKnowledge(@RequestBody KnowledgeDocument doc) {
        KnowledgeDocument saved = knowledgeService.addKnowledge(doc.getTitle(), doc.getContent(), doc.getTags());
        return Result.success(saved);
    }

    @GetMapping("/search")
    public Result search(@RequestParam String keyword) {
        return Result.success(knowledgeService.searchByTitle(keyword));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable String id, @RequestBody KnowledgeDocument newDoc) {
        knowledgeService.updateContentById(id, newDoc);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        knowledgeService.deleteById(id);
        return Result.success();
    }

    @GetMapping
    public Result listAll() {
        return Result.success(knowledgeService.listAll());
    }
}
