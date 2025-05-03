package org.visage.backend.service;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import cn.hutool.core.collection.CollectionUtil;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.visage.backend.enums.ExerciseTypeEnum;import org.visage.backend.enums.GradeEnum;import org.visage.backend.enums.SubjectEnum;import org.visage.backend.exception.ServiceException;
import org.visage.backend.pojo.req.GenerateQuestionReq;import org.visage.backend.pojo.res.ActorFilmAndYearRes;
import org.visage.backend.pojo.res.ActorFilmRes;import org.visage.backend.pojo.res.GenerateQuestionRes;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DeepseekStructureService {

    @Resource
    private OpenAiChatModel chatModel;

    /**
     * 根据演员姓名：{actor}，生成他所演的 {sum} 部电影的名称。
     * 如果没有找到，返回空列表。
     */
    private ActorFilmRes getActorFilms(String name, int sum) {
        // 创建转换器
        BeanOutputConverter<ActorFilmRes> converter = new BeanOutputConverter<>(ActorFilmRes.class);

        // 模板字符串
        String template = """
        根据演员姓名：{actor}，生成他所演的 {sum} 部电影的名称。
        如果没有找到，返回空列表。
        {format}
        """;

        // 模板参数
        Map<String, Object> params = Map.of(
                "actor", name,
                "sum", sum,
                "format", converter.getFormat()
        );

        // 使用 builder 构建 PromptTemplate
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(params)
                .renderer(StTemplateRenderer.builder().build()) // 明确指定模板渲染器
                .build();

        // 构造 Prompt 对象
        Prompt prompt = promptTemplate.create();

        // 调用模型
        Generation generation = chatModel.call(prompt).getResult();

        // 转换为业务对象
        ActorFilmRes actorFilmRes = converter.convert(Objects.requireNonNull(generation.getOutput().getText()));
        if(BeanUtil.isEmpty(actorFilmRes) || CollectionUtil.isEmpty(actorFilmRes.getMovies())){
            throw new ServiceException("该演员未找到任何电影");
        }
        return actorFilmRes;
    }


    /**
     * 根据演员姓名获取它的三部电影
     *
     * @param name 演员姓名
     * @return 电影列表
     */
    public List<String> getFilmsByActor(String name) {
        ActorFilmRes result = this.getActorFilms(name, 3);
        return result.getMovies();
    }

    /**
     * 根据演员姓名获取它的N部电影
     *
     * @param name 演员姓名
     * @param sum  电影数量
     * @return 电影列表
     */
    public List<String> getFilmsByActorAndSum(String name, int sum) {
        ActorFilmRes result = this.getActorFilms(name, sum);
        return result.getMovies();
    }

    /**
     * flow1：根据演员姓名获取它的N部电影
     * flow2：根据电影名称获取它的发行年份
     *
     * @param name 演员姓名
     * @param sum 电影数量
     * @return 电影名称和发行年份列表
     */
    public List<ActorFilmAndYearRes> getFilmsAndYearsByActorAndSum(String name, Integer sum) {
        ActorFilmRes result = this.getActorFilms(name, sum);
        List<String> filmNameList = result.getMovies();

        // filmNames 逗号隔开
        String filmNames = String.join(",", filmNameList);

        // 创建转换器
        BeanOutputConverter<List<ActorFilmAndYearRes>> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<ActorFilmAndYearRes>>() {}
        );

        // 模板字符串
        String template = """
        电影名称为以下几部：{filmNames}。
        请为每部电影提供名称和发行年份，并以JSON数组格式返回，每个元素包含 filmName 和 year 字段。
        {format}
        """;

        // 模板参数
        Map<String, Object> params = Map.of(
                "filmNames", filmNames,
                "format", converter.getFormat()
        );

        // 使用 builder 构建 PromptTemplate
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(params)
                .renderer(StTemplateRenderer.builder().build()) // 明确指定模板渲染器
                .build();

        // 构造 Prompt 对象
        Prompt prompt = promptTemplate.create();

        Generation generation = chatModel.call(prompt).getResult();

        List<ActorFilmAndYearRes> resultList = converter.convert(Objects.requireNonNull(generation.getOutput().getText()));
        if (CollectionUtil.isEmpty(resultList)) {
            throw new ServiceException("获取电影发行年份失败");
        }

        return resultList;
    }

    public GenerateQuestionRes generateQuestion(GenerateQuestionReq generateQuestionReq) {
        // 创建转换器
        BeanOutputConverter<GenerateQuestionRes> converter = new BeanOutputConverter<>(GenerateQuestionRes.class);



        String subject = SubjectEnum.getNameByValue(generateQuestionReq.getSubject());
        String subjectPrompt = SubjectEnum.getPromptByValue(generateQuestionReq.getSubject());
        String grade = GradeEnum.getNameByValue(generateQuestionReq.getGrade());
        String gradePrompt = GradeEnum.getPromptByValue(generateQuestionReq.getGrade());
        String type = ExerciseTypeEnum.getNameByValue(generateQuestionReq.getType());
        String typePrompt = ExerciseTypeEnum.getPromptByValue(generateQuestionReq.getType());
        String topic = generateQuestionReq.getTopic();


        // 模板字符串
        String template = """
        帮我生成一道习题。
        该习题的类型：{type}，{typePrompt}。
        该习题对应的学科：{subject}，{subjectPrompt}。
        该习题对应的年级：{grade}，{gradePrompt}。
        该习题围绕的主题：{topic}。
        生成的习题以JSON格式返回，每个元素包含 question 和 answer 字符串字段。
        另外单选题和多选题包含选项 options ,options 字段为一个字符串数组,单个选项示例（选项：选项内容），选项只有“A”,“B”,“C”,“D”四个。
        判断题的 answer 只有“正确”或“错误”。
        单选题的 answer 为选项“A”,“B”,“C”,“D”中的一个。
        多选题的 answer 为“A”,“B”,“C”,“D”中的多个，并且以逗号隔开。
        解答题的 answer 为一个字符串，可以是解答过程，也可以是答案，也可以是解题思路。
        {format}
        """;

        // 模板参数
        Map<String, Object> params = Map.of(
                "type", type,
                "typePrompt", typePrompt,
                "subject", subject,
                "subjectPrompt", subjectPrompt,
                "grade", grade,
                "gradePrompt", gradePrompt,
                "topic", topic,
                "format", converter.getFormat()
        );

        // 使用 builder 构建 PromptTemplate
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(params)
                .renderer(StTemplateRenderer.builder().build()) // 明确指定模板渲染器
                .build();

        // 构造 Prompt 对象
        Prompt prompt = promptTemplate.create();

        // 调用模型
        Generation generation = chatModel.call(prompt).getResult();

        // 转换为业务对象
        GenerateQuestionRes generateQuestionRes = converter.convert(Objects.requireNonNull(generation.getOutput().getText()));
        if (BeanUtil.isEmpty(generateQuestionRes)) {
            throw new ServiceException("生成习题失败");
        }
        return generateQuestionRes;
    }}
