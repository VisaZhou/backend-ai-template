## Spring AI 支持的版本
```txt
Spring Boot 3.4.x
```

## Spring AI 流式返回
告诉 Spring 用 SSE 格式返回。

如果需要使用 SSE 需要引入 spring-boot-starter-webflux

spring-ai-starter-model-openai 自带 webflux 依赖

```java
@GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ChatResponse> generateStream();
```
