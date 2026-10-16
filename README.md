# graduation-project-starter

轻量毕业设计通用框架：门户、管理页、会话登录及真实MySQL示例记录CRUD。第一版源码已实现，实际验证见[验证记录](docs/verification.md)。

## 环境

Java 17、Node 24.15.0、MySQL 8.x；Maven Wrapper固定Maven 3.9.13，无需全局Maven或Docker。
固定依赖：Spring Boot 3.5.16、Spring Security 6.5.11（Boot管理）、MyBatis-Plus 3.5.17；Vue 3.5.43、Vue Router 4.6.4、Pinia 4.0.3、Axios 1.20.0、Element Plus 2.14.7、Vite 8.3.4。

一个Vue工程，一个Maven后端模块，包名 `com.example.starter`。门户与管理页面分目录，共享请求和组件。

## 新建数据库

在仓库根目录执行以下PowerShell命令。先确认目标库需要新建且尚不存在；应用不会自动建表、导入或清空数据。

```powershell
mysql -u root -p --default-character-set=utf8mb4 --execute="CREATE DATABASE graduation_starter CHARACTER SET utf8mb4;"
mysql -u root -p --default-character-set=utf8mb4 --database=graduation_starter --execute="source database/schema.sql"
mysql -u root -p --default-character-set=utf8mb4 --database=graduation_starter --execute="source database/demo-data.sql"
```

使用你的MySQL管理账号；mysql不在PATH时用绝对路径调用，自定义连接可添加 `--host`、`--port`。脚本只面向空的新建库，重复导入会报错，不提供自动删除或重置流程。运行账号需两张表的SELECT/INSERT/UPDATE/DELETE权限。

| 账号 | 密码 | 身份 |
|---|---|---|
| admin | Admin123! | 管理员 |
| user | User123! | 普通用户 |
| user2 | User123! | 第二用户 |
| disabled | Disabled123! | 停用，不能登录 |

SQL存储已验证BCrypt哈希；这些公开账号仅用于本地演示。25条记录归属为admin5条、user12条、user2共8条。

## 启动

在一个PowerShell终端配置环境变量并启动后端：

```powershell
$env:DB_USERNAME="你的数据库账号"
$env:DB_PASSWORD="你的数据库密码"
$env:DB_URL="jdbc:mysql://localhost:3306/graduation_starter?characterEncoding=UTF-8&connectionTimeZone=%2B08:00&forceConnectionTimeZoneToSession=true"
cd backend
.\mvnw.cmd spring-boot:run
```

配置模板在 `backend/.env.example`，仅供参考，不会自动加载；实际密码只用环境变量。后端默认8080；可用SERVER_PORT修改，修改后同步Vite代理配置。

另一个终端：

```powershell
cd frontend
npm ci
npm run dev
```

打开[本地门户](http://localhost:5173)。Vite5173启用strictPort，端口占用会报错。API统一通过Vite代理8080，请统一使用localhost:5173，避免混用localhost与127.0.0.1导致Cookie不共享。

管理员默认进入管理首页，普通用户进入我的记录。门户仅展示自己的记录，管理端可处理全部记录；管理员在门户仍只查看自己的记录，新建记录始终归属当前账号。

## 构建和测试

```powershell
cd frontend
npm run test
npm run build
cd ../backend
.\mvnw.cmd test
.\mvnw.cmd package
cd ..
java -jar backend/target/graduation-project-starter.jar
```

JAR同样需要数据库环境变量。Linux/macOS首次克隆后执行 `chmod +x backend/mvnw`，在backend目录使用 `./mvnw`。前端产物在frontend/dist，不嵌入后端；本地演示使用Vite和后端两个进程。

未配置测试库时，默认后端测试运行安全和密码检查，8项MySQL测试明确跳过。完整测试需显式新建独立库，并导入原始演示数据：

```powershell
# 仓库根目录；仅对尚不存在的新建测试库执行
mysql -u root -p --default-character-set=utf8mb4 --execute="CREATE DATABASE graduation_starter_test CHARACTER SET utf8mb4;"
mysql -u root -p --default-character-set=utf8mb4 --database=graduation_starter_test --execute="source database/schema.sql"
mysql -u root -p --default-character-set=utf8mb4 --database=graduation_starter_test --execute="source database/demo-data.sql"
$env:TEST_DB_URL="jdbc:mysql://localhost:3306/graduation_starter_test?characterEncoding=UTF-8&connectionTimeZone=%2B08:00&forceConnectionTimeZoneToSession=true"
$env:TEST_DB_USERNAME="你的测试库账号"
$env:TEST_DB_PASSWORD="你的测试库密码"
cd backend
.\mvnw.cmd test
```

测试严格只允许graduation_starter_test。业务改动通过事务回滚，不清空演示库或测试库；自增ID出现间隔属于正常行为。测试库不要用于手动业务操作。环境模板见backend/src/test/resources/test-env.example。

## 认证与接口

Session Cookie使用HttpOnly、SameSite=Lax，本地HTTP的Secure=false；空闲30分钟失效。后端重启需要重新登录，业务数据仍保留。

启动通过me恢复身份，不读取localStorage角色；CSRF仅存内存，登录和退出后刷新。登录为表单编码，其余写入JSON。401引导登录，403提示错误；写请求失败不自动重放。

返回 `{code,message,data}`，BIGINT ID为字符串。业务时间Asia/Shanghai，格式 `yyyy-MM-dd'T'HH:mm:ss`；JDBC使用UTC+08:00，避免依赖MySQL命名时区表。完整协议见[接口约定](docs/contracts.md)。

实现参考匹配版本的[Spring Security 6.5 CSRF](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html)、[表单登录](https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/passwords/form.html)及[MyBatis-Plus分页](https://baomidou.com/en/plugins/pagination/)。

## 排查与限制

- 无法连接服务：检查后端日志、数据库配置和端口，然后点击“重新连接”。后端启动成功不代表已连接数据库，首次登录或业务请求会触发连接。
- CSRF失效：刷新或手动再次提交；下一次提交获取新Token，不重放上次请求。
- MySQL建议8.0.16及以上，较早8.0版本可能不执行CHECK；应用始终校验字段和有效枚举。
- npm离线配置导致安装失败时，请允许访问registry或准备完整缓存；提交的锁文件使用官方registry。
- 当前完整引入Element Plus，生产构建提示较大UI包；本地功能不受影响。
- 当前范围为本地演示，未提供生产部署、注册、账号维护、上传、图表和选题专属业务。

## 复用

完整开发流程、各阶段产出与验收条件见[毕业设计开发 SOP](docs/starter-development-sop.md)，包含从选题、需求、视觉与计划到业务开发、测试和交付的执行步骤及可复用指令。

保留认证、请求封装、统一返回和布局；替换modules.example、门户与管理业务页面及配套SQL。新选题建立自己的业务表和专属规则，准备演示数据并验证完整流程，不把demo_record作为万能业务表。

项目名称集中在frontend/src/config.js，菜单保持静态配置。同步维护接口、数据库、README与验证记录。

设计文档：[范围](docs/plan.md)、[架构](docs/architecture.md)、[技术栈](docs/tech-stack.md)、[页面](docs/pages.md)、[数据库](docs/database.md)、[实施验收](docs/implementation.md)。
