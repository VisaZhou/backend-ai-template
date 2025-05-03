package org.visage.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.visage.backend.pojo.req.GenerateQuestionReq;import org.visage.backend.pojo.res.ActorFilmAndYearRes;
import org.visage.backend.pojo.res.GenerateQuestionRes;import org.visage.backend.service.DeepseekStructureService;
import org.visage.backend.util.R;
import java.util.List;

@Tag(name = "deepseek-结构化输入输出", description = "deepseek-结构化输入输出")
@RestController
@RequestMapping("/deepseek/structure")
public class DeepseekStructureController {

    @Resource
    private DeepseekStructureService deepseekStructureService;

    @GetMapping("/getFilmsByActor")
    @Operation(summary = "根据演员姓名获取它的三部电影名称", description = "根据演员姓名获取它的三部电影名称")
    public R<List<String>> getFilmsByActor(@RequestParam(value = "name", defaultValue = "赵露思") String name) {
        return R.ok(deepseekStructureService.getFilmsByActor(name));
    }

    @GetMapping("/getFilmsByActorAndSum")
    @Operation(summary = "根据演员姓名获取它的N部电影名称", description = "根据演员姓名获取它的N部电影名称")
    public R<List<String>> getFilmsByActorAndSum(@RequestParam(value = "name", defaultValue = "赵露思") String name, @RequestParam(value = "sum", defaultValue = "3") Integer sum) {
        return R.ok(deepseekStructureService.getFilmsByActorAndSum(name, sum));
    }

    @GetMapping("/getFilmsAndYearsByActorAndSum")
    @Operation(summary = "根据演员姓名获取它的N部电影名称和对应的发行年份", description = "根据演员姓名获取它的N部电影名称和对应的发行年份")
    public R<List<ActorFilmAndYearRes>> getFilmsAndYearsByActorAndSum(@RequestParam(value = "name", defaultValue = "赵露思") String name, @RequestParam(value = "sum", defaultValue = "3") Integer sum) {
        return R.ok(deepseekStructureService.getFilmsAndYearsByActorAndSum(name, sum));
    }

    @PostMapping("/generateQuestion")
    @Operation(summary = "习题生成器", description = "习题生成器")
    public R<GenerateQuestionRes> generateQuestion(@RequestBody GenerateQuestionReq generateQuestionReq) {
        return R.ok(deepseekStructureService.generateQuestion(generateQuestionReq));
    }
}
