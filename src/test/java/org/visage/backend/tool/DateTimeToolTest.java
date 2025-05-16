package org.visage.backend.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class DateTimeToolTest {

    @Resource
    private OpenAiChatModel chatModel;

    @Test
    public void toolUsed() {
        String response = ChatClient.create(chatModel)
                .prompt("明天的日期是什么？不知道的话也不需要提供假设性的答案")
                .tools(new DateTimeTool())
                .call()
                .content();
        System.out.println("Response: " + response);
    }

    @Test
    public void toolUnUsed() {
        String response = ChatClient.create(chatModel)
                .prompt("明天的日期是什么？不知道的话也不需要提供假设性的答案")
                .call()
                .content();
        System.out.println("Response: " + response);
    }

    @Test
    public void sendEmail() {
        String response = ChatClient.create(chatModel)
                .prompt("帮我在明天发封邮件给:472493922@qq.com")
                .tools(new DateTimeTool())
                .call()
                .content();
        System.out.println("Response: " + response);
    }
}
