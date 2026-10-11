# 实施顺序与验收

当前状态：第一版源码已实现。以下清单作为持续验收标准；本次实际检查结果见 verification.md。

## 实施步骤

1. 工程骨架：frontend与backend，固定依赖、package-lock.json、Maven Wrapper、.gitignore；配置Vite代理、数据库环境变量。将所选Node版本写入.node-version或.nvmrc，package.json engines保持一致。
2. 数据库：按database.md提供schema.sql和demo-data.sql，生成有效BCrypt哈希；确认演示数据归属与数量。
3. 登录闭环：CSRF、formLogin、me、logout、JSON错误处理；验证刷新恢复和退出失效。
4. 示例后端：实现门户归属限制和管理CRUD，共用Service，分页、校验及返回字段严格符合contracts。
5. 门户与管理页：按pages实现路由、布局、列表、弹窗及详情，完成真实数据库CRUD。
6. 检查、文档：完成下表、更新README及验证记录，说明环境和实际结果。

按完整链路推进，不同时添加可选业务。每步完成后使用已有代码继续，不覆盖用户改动。

## 标准命令

在工程实现后必须提供：
- frontend：npm ci；npm run dev（5173，strictPort=true）；npm run build。
- backend：Windows .\\mvnw.cmd test、.\\mvnw.cmd spring-boot:run、.\\mvnw.cmd package；其他平台对应./mvnw。
- 运行已打包后端：java -jar backend/target/graduation-project-starter.jar，配置Maven finalName匹配。
- README提供PowerShell配置DB_USERNAME、DB_PASSWORD的方法；无需Docker或Redis。
- 前端构建产物不强制嵌入后端；本地演示用Vite与后端两进程即可。

上述命令现已提供。前端Node固定24.15.0。构建成功不等于MySQL运行验证成功。

## 必须验收

| 场景 | 预期 |
|---|---|
| 匿名GET记录/管理接口 | 401 JSON |
| 错密码或禁用账号登录 | 401，不返回用户资料 |
| 三个正常账号登录 | 成功，角色与默认首页正确 |
| 缺少/无效CSRF写请求 | 403；无业务数据变更 |
| 登录后刷新页面 | me恢复用户，无假登录 |
| 普通用户打开管理页/API | 页面403；有效会话/CSRF的管理API403 |
| 门户列表 | 仅自己的记录，关键词、状态和分页正确 |
| 用户读取/修改/删除他人记录 | 404；其他用户数据不变 |
| ADMIN管理列表与CRUD | 全部记录可查，创建归属ADMIN |
| 新增、修改、详情、删除 | 保存后刷新仍正确，删除确认可取消 |
| 非法字段、枚举、长度、分页 | 400，不写入错误数据 |
| 未知ID/路径 | 404，未知路径无HTML错误页 |
| 退出后携旧会话请求业务GET | 401；前端回门户首页 |
| 后端重启 | 重新登录；已有数据库业务仍在 |
| 完整演示 | 用户新增修改 → 管理员处理 → 用户刷新查看 → 删除 |
| 页面状态 | 加载、空数据、网络错误、404与403可理解 |

账号1和2测试时用独立浏览器上下文或先退出，避免同浏览器共享Cookie造成角色混淆。CSRF失败测试与业务授权测试分开；后者需有效CSRF，避免测试错误原因。

## 测试安排

JUnit/Spring Boot Test与spring-security-test覆盖会话认证、CSRF、角色及归属，使用实际SecurityFilterChain。配置独立测试数据源，不触碰演示库；MySQL行为通过明确配置的独立测试库验证，测试环境配置示例提交而密码不提交。不要要求额外Docker才能测试。

前端执行构建并检查两端完整演示；无需先搭庞大端到端平台，可用现有浏览器工具进行人工式验证。测试环境缺失可完成构建和可用测试，但数据库行为保持“未验证”，报告阻碍。

提交docs/verification.md记录日期、环境版本、实际命令、结果和未验证项。不要预填通过，不用“代码看起来正确”代替运行证据。

## 下一选题的复用

复制基线 → 替换example模块与业务表 → 增加专属规则和门户/管理页面 → 准备演示数据 → 验证完整流程。API、会话、返回与目录约定继续复用。
