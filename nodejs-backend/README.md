# AdVerge Backend Service

AdVerge 后端服务提供广告聚合平台的管理功能，包括应用管理、广告位管理、广告平台配置等。

## 功能特性

- 应用管理
  - 创建、编辑、删除应用
  - 配置应用基本信息
  - 应用状态管理

- 广告位管理
  - 创建、编辑、删除广告位
  - 配置广告位信息
  - 关联应用
  - 广告位状态管理

- 广告平台配置
  - 管理多个广告平台
  - 配置平台密钥和ID
  - 平台状态管理

- 数据统计
  - 广告展示统计
  - 点击率分析
  - 收益统计

## 技术栈

- Node.js
- Express.js
- MongoDB
- Redis
- JWT
- Webpack
- Jest

## 快速开始

### 1. 环境要求

- Node.js 14+
- MongoDB 4.4+
- Redis 6.0+
- npm 6+

### 2. 安装依赖

```bash
npm install
```

### 3. 配置环境变量

复制 `.env.example` 到 `.env` 并修改配置：

```bash
cp .env.example .env
```

### 4. 启动服务

开发环境：
```bash
npm run dev
```

生产环境：
```bash
npm start
```

## 项目结构

```
src/
├── config/         # 配置文件
├── controllers/    # 控制器
├── models/         # 数据模型
├── routes/         # 路由
├── services/       # 服务层
├── utils/          # 工具函数
├── views/          # 视图模板
├── app.js          # 应用入口
└── index.js        # 服务器入口
```

## API 文档

### 应用管理

- `GET /api/apps` - 获取应用列表
- `POST /api/apps` - 创建新应用
- `GET /api/apps/{id}` - 获取应用详情
- `PUT /api/apps/{id}` - 更新应用信息
- `DELETE /api/apps/{id}` - 删除应用

### 广告位管理

- `GET /api/ad-units` - 获取广告位列表
- `POST /api/ad-units` - 创建新广告位
- `GET /api/ad-units/{id}` - 获取广告位详情
- `PUT /api/ad-units/{id}` - 更新广告位信息
- `DELETE /api/ad-units/{id}` - 删除广告位

### 广告平台配置

- `GET /api/config` - 获取配置信息
- `PUT /api/config` - 更新配置信息

## 测试

运行测试：
```bash
npm test
```

## 部署

1. 构建项目：
```bash
npm run build
```

2. 启动服务：
```bash
npm start
```

## 注意事项

1. 确保数据库连接正确配置
2. 定期备份数据库
3. 注意保护敏感信息（如API密钥）
4. 监控系统资源使用情况
5. 及时更新依赖包版本

## 技术支持

如有问题，请联系技术支持：
- 邮箱：support@adverge.com
- 电话：400-xxx-xxxx 