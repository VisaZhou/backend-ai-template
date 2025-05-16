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

## 向量数据库 Milvus

下载 Milvus 的 docker-compose 文件

并且把 etcd，minio，milvusdb 镜像地址修改为阿里云私库地址

添加 attu 可视化界面的镜像

```bash
curl -L https://github.com/milvus-io/milvus/releases/download/v2.4.0/milvus-standalone-docker-compose.yml -o docker-compose.yml
```

启动服务 Milvus 服务
```yaml

```bash
docker-compose up -d
docker ps
```

打开可视化界面
```bash
http://localhost:3000
```

## MCP 服务端
需要引入,因为目前 webflux 存在bug
```xml
<!-- Spring AI mcp-server-webflux -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```
application配置
```yaml
    # mcp服务端配置
    mcp:
      server:
        enabled: true # 是否启用 mcp 服务端
        stdio: false # 是否启用标准输入输出
        name: mcp-server # 用于识别的服务器名称
        version: 1.0.0 # 服务器版本
        type: sync # 服务器类型（同步/异步）
        sse-message-endpoint: /mcp/messages # 客户端用来发送消息的 Web 传输的自定义 SSE 消息端点路径
        sse-endpoint: /sse #用于 Web 传输的自定义 SSE 端点路径
```
注册tool
```java
@Configuration
public class McpWebFluxServiceConfig {

    @Bean
    public ToolCallbackProvider dateTools(DateTimeTool dateTimeTool) {
        return MethodToolCallbackProvider.builder().toolObjects(dateTimeTool).build();
    }
}
```

## MCP 客户端
查看 backend-ai-client 项目，需要引入以下mcp包,同时同样也要引入大模型
```xml
        <!-- Spring AI mcp-client -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```
application配置
```yaml
    # MCP 客户端配置
    mcp:
      client:
        enabled: true # 是否启用 mcp 客户端
        name: mcp-client # 用于识别的客户端名称
        version: 1.0.0 # 客户端版本
        initialized: true # 是否在创建时初始化客户端
        type: sync # 客户端类型（同步/异步）
        toolcallback:
          enabled: true # 启用/禁用 MCP 工具回调与 Spring AI 工具执行框架的集成
        sse:
          connections:
            server1:
              url: http://127.0.0.1:8000 # 服务器端 SSE 连接地址
```
调用mcp
```java
    @Resource
    private OpenAiChatModel chatModel;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    void contextLoads() {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
        ChatClient.CallResponseSpec call = chatClient.prompt("帮我调用工具，在明天发封邮件给:472493922@qq.com").call();
        String content = call.content();
        System.out.println(content);
    }
```

