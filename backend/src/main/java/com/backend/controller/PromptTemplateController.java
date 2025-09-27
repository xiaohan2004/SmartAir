package com.backend.controller;

import com.backend.common.Result;
import com.backend.entity.PromptTemplate;
import com.backend.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService promptService;

    // 创建新模板
    @PostMapping
    public Result create(@RequestBody PromptTemplate pt) {
        return Result.success(promptService.createTemplate(
                pt.getName(), pt.getTemplate(), pt.getDescription()));
    }

    // 按模板名查询
    @GetMapping("/name/{name}")
    public Result getByName(@PathVariable String name) {
        return promptService.getByName(name)
                .map(Result::success)
                .orElseGet(() -> Result.error("模板不存在"));
    }

    // 查询所有模板
    @GetMapping
    public Result listAll() {
        return Result.success(promptService.listAll());
    }

    // 更新模板
    @PutMapping("/{id}")
    public Result update(@PathVariable String id, @RequestBody PromptTemplate newTemplate) {
        promptService.updateTemplate(id, newTemplate);
        return Result.success();
    }

    // 删除模板
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        promptService.deleteTemplate(id);
        return Result.success();
    }
}