# 技术栈


| 部分 | 技术 | 选择理由 |
|---|---|---|
| 前端 | Vue 3 + JavaScript + Vite | 与现有项目接近，页面开发直接 |
| 页面组件 | Element Plus | 复用菜单、表格、表单和弹窗 |
| 路由 | Vue Router 4 | 用户与管理页面导航 |
| 请求 | Axios | API 请求与错误处理统一 |
| 共享状态 | Pinia，仅保存当前用户等必要状态 | 页面列表、表单保持局部状态 |
| 后端 | Java 17 + Spring Boot 3.5.x + Maven | 单工程、单服务，沿用现有 Java 项目经验 |
| 数据访问 | MyBatis-Plus 3.5.x Boot 3 starter | 简化普通 CRUD，复杂查询明确写 SQL |
| 登录 | Spring Security + HttpSession + BCrypt | 后端会话及访问检查，密码哈希 |
| 数据库 | MySQL 8.x | 真实保存业务数据，现有项目均使用 MySQL |

具体依赖版本在搭建时固定并验证。Spring Boot 3.5 系列需要至少 Java 17：[官方系统要求](https://docs.spring.io/spring-boot/3.5/system-requirements.html)。[Vue 官方状态管理说明](https://vuejs.org/guide/scaling-up/state-management.html)、[Element Plus](https://element-plus.org/en-US/component/overview.html)、[MyBatis-Plus](https://baomidou.com/en/) 为相关能力依据。

前端默认 5173，后端默认 8080。Vite 代理 `/api` 到后端，使用相对请求地址和会话 Cookie。请求封装处理 CSRF Token、未登录及错误反馈。会话保存在后端内存，重启后重新登录；业务数据保存在数据库。基础环境只有前端、后端、MySQL。


## 第一版固定版本

Spring Boot 3.5.16（管理Security 6.5.11）；MyBatis-Plus starter与jsqlparser 3.5.17。Node 24.15.0；Vue 3.5.43、Vite 8.3.4、plugin-vue 6.0.10、Element Plus 2.14.7、Vue Router 4.6.4、Axios 1.20.0、Pinia 4.0.3。Maven Wrapper 3.3.4固定Maven 3.9.13。锁文件与pom是实际版本来源。

JDBC连接使用connectionTimeZone=%2B08:00并固定数据库会话时区，应用业务时区仍为Asia/Shanghai；无需加载MySQL命名时区表。
