# graduation-project-starter

轻量毕业设计通用框架，面向本地演示及同类选题复用。

**当前状态：设计阶段。方案与第一版实施约定已补齐，尚未实现可运行代码。**

## 文档入口

- [框架方案](docs/plan.md)：目标、80% 核心功能门槛、可选业务模块、建设与验收。
- [系统与文件架构](docs/architecture.md)：门户页、管理页、后端、数据库，以及计划目录结构。
- [技术栈](docs/tech-stack.md)：前后端、数据库、登录方式和本地运行关系。

## 新 Agent 开始开发

先阅读 [AGENTS.md](AGENTS.md)，再依次阅读以上方案、架构、技术栈，以及：

- [页面与交互](docs/pages.md)：路由、门户和管理页面、CRUD行为。
- [接口与登录约定](docs/contracts.md)：请求返回格式、Session、CSRF、角色与归属。
- [数据库约定](docs/database.md)：两张表、字段、演示账号和数据。
- [实施与验收](docs/implementation.md)：开发顺序、交付命令、检查清单。

第一版仅实现公共底座和“示例记录”CRUD，暂不加入账号维护页面、注册、上传、图表或订单等可选功能。收到实施任务后按这些约定编写代码；文档中的运行命令是待实现的交付标准，当前还不能运行。

## 系统组成

```mermaid
flowchart TB
  P[门户页：用户端] --> B[Spring Boot 后端]
  A[管理页：管理端] --> B
  B --> D[(MySQL 数据库)]
```

门户页和管理页放在一个 Vue 工程中，以不同布局和路由组织；共用一个后端服务和一个数据库。

## 核心原则

核心功能以当前 10 个已分析项目中至少 8 个实际使用为准：登录、布局与菜单、列表查询、CRUD、请求处理、后端业务和数据库持久化。未来项目覆盖率持续统计。

上传、图表、公告、评论、订单、预约、审核等按选题加入。第一版计划实现公共底座和一个完整 CRUD 示例。

## 技术栈

Vue 3 + JavaScript + Vite + Element Plus + Vue Router + Axios + Pinia；Java 17 + Spring Boot 3.5.x + MyBatis-Plus + Spring Security Session；MySQL 8.x。具体依赖版本在实现时锁定并验证。
