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



