# 智慧面试评分系统

一个面向政府、事业单位、国企的面试全流程数字化管理平台，实现"三盲"抽签、无纸化评分、成绩自动合成等核心功能。

## 🛠 技术栈

- **Frontend**: Vue 3 + Vite + Element Plus + Pinia
- **Backend**: Spring Boot 2.7 + MyBatis Plus + JDK 11
- **Database**: MySQL 8.0

## 🚀 启动指南 (How to Run)

1. 确保 Docker Desktop 已启动
2. 在项目根目录执行：

```bash
docker compose up --build
```

3. 等待所有容器启动完成（首次启动可能需要几分钟下载依赖）

## 🔗 服务地址 (Services)
项目propmt要求的依赖版本和Maven版本配置，请清空docker镜像再启动，如遇下载失败，请检查网络连接！！！
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8000/api
- **Swagger文档**: http://localhost:8000/api/swagger-ui.html
- **Database**: localhost:3306 (user: root / pass: root123456)

## 🧪 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 考官1 | examiner1 | 123456 |
| 考官2 | examiner2 | 123456 |
| 工作人员 | staff1 | 123456 |

## 📦 功能模块

### 1. 项目管理
- 创建、编辑、删除面试项目
- 配置笔试/面试成绩权重
- 设置去除最高最低分规则
- 项目状态流转管理

### 2. 考生管理
- 考生信息录入与维护
- 考生签到功能
- 笔试成绩导入
- 面试状态跟踪

### 3. 考官管理
- 考官信息维护
- 考官专业领域记录
- 考官考场分配

### 4. 考场管理
- 考场信息配置
- 考官人数设置
- 容纳人数设置

### 5. 三盲抽签
- **考官抽签**: 随机分配考官到各考场
- **考生抽签**: 随机确定考生面试顺序
- **职位顺序抽签**: 随机确定职位面试顺序
- 一键执行三盲抽签
- 抽签结果查看与导出

### 6. 现场评分
- 考官在线打分
- 实时分数展示
- 面试计时功能
- 评语录入

### 7. 成绩管理
- 自动计算最终成绩
- 去掉最高最低分
- 笔试面试加权计算
- 成绩排名生成
- 成绩发布功能

## 🗄 数据库设计

系统包含以下核心数据表：

- `sys_user` - 系统用户表
- `interview_project` - 面试项目表
- `position` - 职位表
- `exam_room` - 考场表
- `examiner` - 考官表
- `candidate` - 考生表
- `score_item` - 评分项表
- `examiner_score` - 考官评分表
- `final_score` - 最终成绩表
- `draw_record` - 抽签记录表
- `operation_log` - 操作日志表

## 🔧 开发说明

### 本地开发

**后端**:
```bash
cd backend
mvn spring-boot:run
```

**前端**:
```bash
cd frontend
npm install
npm run dev
```

### 目录结构

```
taskId576/
├── docker-compose.yml      # Docker编排配置
├── README.md               # 项目说明
├── mysql/
│   └── init.sql           # 数据库初始化脚本
├── backend/
│   ├── Dockerfile         # 后端Docker配置
│   ├── pom.xml           # Maven配置
│   ├── settings.xml      # Maven镜像配置
│   └── src/              # Java源代码
└── frontend/
    ├── Dockerfile        # 前端Docker配置
    ├── nginx.conf        # Nginx配置
    ├── package.json      # 前端依赖
    └── src/              # Vue源代码
```

## 📝 API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取当前用户信息

### 项目接口
- `GET /api/projects` - 获取项目列表
- `POST /api/projects` - 创建项目
- `PUT /api/projects/{id}` - 更新项目
- `DELETE /api/projects/{id}` - 删除项目

### 考生接口
- `GET /api/candidates` - 获取考生列表
- `POST /api/candidates/{id}/check-in` - 考生签到

### 抽签接口
- `POST /api/draw/triple-blind` - 执行三盲抽签
- `GET /api/draw/results` - 获取抽签结果

### 评分接口
- `POST /api/scores` - 提交评分
- `GET /api/scores/ranking` - 获取成绩排名
- `POST /api/scores/publish` - 发布成绩

## 🎨 界面预览

系统采用现代化UI设计，支持响应式布局：

- 渐变色彩搭配
- 卡片式布局
- 数据可视化展示
- 流畅的交互动效

## 📄 License

MIT License
