package org.visage.backend;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.visage.backend.pojo.res.ActorFilmRes;
import java.util.Map;import static org.junit.jupiter.api.Assertions.assertEquals;import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class BackendAiTemplateApplicationTests {

    @Resource
    private OpenAiChatModel chatModel;


    @Test
    void testCompleteFlow() {
        // 创建转换器
        BeanOutputConverter<ActorFilmRes> converter = new BeanOutputConverter<>(ActorFilmRes.class);

        // 构建提示
        String template = """
            Generate the filmography of 3 movies for {actor}.
            {format}
            """;

        Prompt prompt = new PromptTemplate(template,
                Map.of("actor", "Tom Hanks", "format", converter.getFormat()))
                .create();

        // 调用模型
        Generation generation = chatModel.call(prompt).getResult();

        // 转换结果
        ActorFilmRes result = converter.convert(generation.getOutput().getText());

        // 验证基本结构
        assertNotNull(result.getActor());
        assertEquals(3, result.getMovies().size());
    }
}
