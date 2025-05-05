package org.visage.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@Tag(name = "deepseek-聊天模型", description = "deepseek-聊天模型")
@RestController
@RequestMapping("/deepseek/chat")
public class DeepseekChatController {

    @Resource
    private OpenAiChatModel chatModel;

    @GetMapping("/ai/generate")
    @Operation(summary = "直接生成文本，非流式响应", description = "直接生成文本，非流式响应")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式生成文本", description = "使用 SSE 实时返回生成结果")
    @ApiResponse(responseCode = "200", description = "流式响应", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE))
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }

    @GetMapping(value = "/ai/memory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "聊天记忆", description = "先存在内存（ConcurrentHashMap）中，后期可改为 JDBC 存储")
    @ApiResponse(responseCode = "200", description = "流式响应", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE))
    public Flux<ChatResponse> memory(@RequestParam(value = "message", defaultValue = "我叫周徐进") String message) {
        // Create a memory instance
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        String conversationId = "007";
        UserMessage userMessage = new UserMessage(message);
        chatMemory.add(conversationId, userMessage);
        Prompt prompt = new Prompt(chatMemory.get(conversationId));
        return chatModel.stream(prompt);
    }
}
