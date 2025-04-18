# AdVerge Spring Boot 后端

## 项目说明

这是一个使用Spring Boot框架开发的广告服务后端，用于替代原有的Node.js实现。

## 技术栈

- **Spring Boot**: 核心框架
- **Spring Data MongoDB**: MongoDB数据库访问
- **Spring Data Redis**: Redis缓存支持
- **Spring Kafka**: Kafka消息队列集成
- **Spring Security**: 安全框架
- **Lombok**: 简化Java代码
- **Maven**: 项目管理

## 目录结构

```
springboot-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── adverge/
│   │   │           └── backend/
│   │   │               ├── config/      # 配置类
│   │   │               ├── controller/  # 控制器
│   │   │               ├── dto/         # 数据传输对象
│   │   │               ├── model/       # 数据模型
│   │   │               ├── repository/  # 数据仓库
│   │   │               ├── security/    # 安全相关
│   │   │               ├── service/     # 服务层
│   │   │               │   └── impl/    # 服务实现
│   │   │               ├── util/        # 工具类
│   │   │               └── AdVergeApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── application.yml          # 应用配置
│   │       └── application-prod.yml     # 生产环境配置
│   └── test/                            # 测试代码
├── pom.xml                              # Maven配置
└── README.md                            # 本文档
```

## 核心功能

1. **广告竞价请求**
   - 接收应用的广告请求
   - 向多个广告平台发送竞价请求
   - 选择最高价格的广告返回

2. **数据追踪**
   - 记录广告展示
   - 记录广告点击
   - 统计收益数据

3. **安全机制**
   - 请求签名验证
   - API访问控制

## 构建运行

### 环境要求

- JDK 11+
- Maven 3.6+
- MongoDB
- Redis
- Kafka

### 编译构建

```bash
mvn clean package
```

### 运行应用

```bash
java -jar target/adverge-backend-1.0.0.jar
```

### 环境变量配置

- `MONGODB_URI`: MongoDB连接字符串
- `REDIS_HOST`: Redis主机地址
- `REDIS_PORT`: Redis端口
- `REDIS_PASSWORD`: Redis密码（可选）
- `KAFKA_SERVERS`: Kafka服务器地址
- `JWT_SECRET`: JWT密钥
- `DISABLE_SIGNATURE_CHECK`: 禁用签名检查（开发环境使用）

## API接口

### 广告请求

```
GET /api/ad/{adUnitId}
```

### 竞价请求

```
POST /api/bid/{adUnitId}
```

### 记录展示

```
POST /api/track/impression/{adId}
```

### 记录点击

```
POST /api/track/click/{adId}
```

## 与Node.js版本的区别

1. 使用Spring Boot框架代替Express
2. 使用Maven管理依赖而非npm
3. 更严格的类型系统和数据验证
4. 更好的并发处理能力
5. 更完善的依赖注入机制

## 未来计划

- 添加监控和报警功能
- 优化竞价逻辑
- 添加更多广告平台集成
- 实现A/B测试框架
- 增加管理后台API 