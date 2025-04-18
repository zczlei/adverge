# AdVerge Spring Boot 后端服务

AdVerge 后端服务是一个基于 Spring Boot 的广告聚合平台后端系统，用于管理和优化多个广告平台的广告投放。

## 技术栈

- **框架**: Spring Boot
- **构建工具**: Gradle / Maven (双构建支持)
- **数据库**: MySQL
- **缓存**: Redis
- **模板引擎**: Thymeleaf (用于管理后台)
- **安全**: Spring Security
- **API文档**: Swagger/OpenAPI

## 主要功能

1. **广告平台管理**
   - 支持多个主流广告平台的接入
   - 统一的广告请求和响应接口
   - 广告平台配置管理

2. **广告单元管理**
   - 广告位创建和配置
   - 广告单元状态监控
   - 广告投放策略设置

3. **应用管理**
   - 应用信息管理
   - 应用级别的广告配置
   - 应用数据统计

4. **数据统计**
   - 广告展示和点击数据统计
   - 收益数据分析
   - 实时数据监控

5. **管理后台**
   - 平台配置管理
   - 数据可视化
   - 用户权限管理

## 项目结构

```
springboot-backend/
├── src/main/java/com/adverge/backend/
│   ├── config/          # 配置类
│   │   ├── AdNetworkConfig.java      # 广告网络配置
│   │   ├── RedisConfig.java          # Redis配置
│   │   ├── RestTemplateConfig.java   # HTTP客户端配置
│   │   ├── SecurityConfig.java       # 安全配置
│   │   ├── ThymeleafConfig.java      # 模板引擎配置
│   │   └── WebConfig.java            # Web配置
│   │
│   ├── controller/      # 控制器
│   │   ├── AdController.java         # 广告请求处理
│   │   ├── AdUnitController.java     # 广告单元管理
│   │   ├── AppController.java        # 应用管理
│   │   ├── CompatibilityController.java  # 兼容性检查
│   │   ├── ConfigController.java     # 配置管理
│   │   ├── EventController.java      # 事件处理
│   │   ├── PlatformManagementController.java  # 平台管理
│   │   ├── StatController.java       # 数据统计
│   │   └── admin/                    # 管理后台控制器
│   │       ├── AdUnitAdminController.java
│   │       ├── AppAdminController.java
│   │       └── PlatformAdminController.java
│   │
│   ├── dto/            # 数据传输对象
│   │   ├── AdEventDto.java           # 广告事件DTO
│   │   ├── AdRequest.java            # 广告请求DTO
│   │   ├── AdResponse.java           # 广告响应DTO
│   │   ├── AdUnitRequest.java        # 广告单元请求DTO
│   │   ├── AdUnitResponse.java       # 广告单元响应DTO
│   │   ├── AppRequest.java           # 应用请求DTO
│   │   ├── BidResponse.java          # 竞价响应DTO
│   │   ├── PlatformResponse.java     # 平台响应DTO
│   │   └── TrackRequest.java         # 追踪请求DTO
│   │
│   ├── model/          # 数据模型
│   │   ├── AdUnit.java               # 广告单元模型
│   │   ├── App.java                  # 应用模型
│   │   ├── Config.java               # 配置模型
│   │   ├── GeoData.java              # 地理位置数据
│   │   ├── Metrics.java              # 指标数据
│   │   ├── Platform.java             # 平台模型
│   │   └── UserData.java             # 用户数据
│   │
│   ├── repository/     # 数据访问层
│   │   ├── AdUnitRepository.java     # 广告单元仓库
│   │   ├── AppRepository.java        # 应用仓库
│   │   ├── ConfigRepository.java     # 配置仓库
│   │   └── MetricsRepository.java    # 指标仓库
│   │
│   ├── service/        # 业务逻辑层
│   │   ├── AdNetworkManager.java     # 广告网络管理器
│   │   ├── AdNetworkService.java     # 广告网络服务接口
│   │   ├── AdService.java            # 广告服务
│   │   ├── AdUnitService.java        # 广告单元服务
│   │   ├── AppService.java           # 应用服务
│   │   ├── ConfigService.java        # 配置服务
│   │   ├── EventService.java         # 事件服务
│   │   ├── SecurityService.java      # 安全服务
│   │   └── impl/                     # 服务实现
│   │       ├── AbstractAdNetworkService.java  # 抽象广告网络服务
│   │       ├── AdColonyServiceImpl.java       # AdColony实现
│   │       ├── AdNetworkManagerImpl.java      # 广告网络管理器实现
│   │       ├── AdServiceImpl.java             # 广告服务实现
│   │       ├── AdUnitServiceImpl.java         # 广告单元服务实现
│   │       ├── AppLovinServiceImpl.java       # AppLovin实现
│   │       ├── AppServiceImpl.java            # 应用服务实现
│   │       ├── BigoAdsServiceImpl.java        # BigoAds实现
│   │       ├── ChartboostServiceImpl.java     # Chartboost实现
│   │       ├── ConfigServiceImpl.java         # 配置服务实现
│   │       ├── EventServiceImpl.java          # 事件服务实现
│   │       ├── FyberServiceImpl.java          # Fyber实现
│   │       ├── InMobiServiceImpl.java         # InMobi实现
│   │       ├── IronSourceServiceImpl.java     # IronSource实现
│   │       ├── MahimetaServiceImpl.java       # Mahimeta实现
│   │       ├── MintegralServiceImpl.java      # Mintegral实现
│   │       ├── SecurityServiceImpl.java       # 安全服务实现
│   │       ├── TopOnServiceImpl.java          # TopOn实现
│   │       ├── UnityAdsServiceImpl.java       # UnityAds实现
│   │       └── VungleServiceImpl.java         # Vungle实现
│   │
│   ├── security/       # 安全相关
│   │   └── RequestSignatureInterceptor.java   # 请求签名拦截器
│   │
│   └── AdVergeApplication.java       # 应用入口
│
├── src/main/resources/
│   ├── application.yml               # 应用配置
│   └── templates/                    # Thymeleaf模板
│       └── admin/                    # 管理后台模板
│           ├── index.html            # 首页
│           ├── platforms/            # 平台管理页面
│           │   ├── form.html         # 平台表单
│           │   └── list.html         # 平台列表
│
├── build.gradle                      # Gradle构建配置
└── pom.xml                          # Maven构建配置
```

## 支持的广告平台

- AdColony
- AppLovin
- Bigo Ads
- Chartboost
- Fyber
- InMobi
- IronSource
- Mahimeta
- Mintegral
- TopOn
- Unity Ads
- Vungle

## 快速开始

### 环境要求

- JDK 17 或更高版本
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+ 或 Gradle 7.0+

### 配置

1. 数据库配置（application.yml）:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/adverge
    username: your_username
    password: your_password
```

2. Redis配置:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### 构建和运行

使用 Maven:
```bash
cd springboot-backend
mvn clean install
java -jar target/adverge-backend.jar
```

或使用 Gradle:
```bash
cd springboot-backend
gradle clean build
java -jar build/libs/adverge-backend.jar
```

### 访问

- API接口: http://localhost:8080/api
- 管理后台: http://localhost:8080/admin
- API文档: http://localhost:8080/swagger-ui.html

## API 文档

主要API端点:

- `/api/ad`: 广告请求和响应
- `/api/app`: 应用管理
- `/api/platform`: 平台管理
- `/api/stats`: 数据统计
- `/api/events`: 事件追踪

详细的API文档可以在运行项目后通过Swagger UI查看。

## 安全性

- 所有API请求需要进行签名验证
- 管理后台采用基于角色的访问控制
- 支持API访问频率限制
- 数据传输采用HTTPS加密

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情 