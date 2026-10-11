# 接口、认证与权限约定

## 统一格式

除登录表单外，写请求为 JSON，响应均为 JSON。HTTP 200 表示成功，创建也用200；统一返回：
```json
{"code":0,"message":"成功","data":null}
```

错误响应同结构，data=null。HTTP/code一致：400参数不合法，401未登录或账号密码错误，403无权限或CSRF不合法，404记录不存在，409业务冲突，500未知异常。错误不暴露堆栈、SQL和密码。

分页数据：records、total、page、size；列表项包括id/title/description/status/ownerId/ownerUsername/createdAt/updatedAt。ID使用MySQL自增BIGINT，JSON序列化为字符串，避免JS精度问题；total/page/size为数字。日期输出 yyyy-MM-dd'T'HH:mm:ss，采用本地 Asia/Shanghai 业务时间，前后端一致。

## 身份与CSRF

使用Spring Security formLogin，处理路径 /api/auth/login。Axios以 application/x-www-form-urlencoded 提交 username/password（URLSearchParams）；成功和失败处理器输出统一JSON，不使用默认HTML页面或302跳转。这是唯一表单编码接口。

| 方法与路径 | 权限 | 返回或输入 |
|---|---|---|
| GET /api/auth/csrf | 匿名允许 | data={token,headerName} |
| POST /api/auth/login | 匿名允许，仍需CSRF | username/password；成功data为当前用户 |
| GET /api/auth/me | 已登录 | data={id,username,displayName,role} |
| POST /api/auth/logout | 已登录且CSRF有效 | 使会话失效，返回data=null |

CSRF使用HttpSessionCsrfTokenRepository及默认Xor处理器。csrf接口读取框架提供的CsrfToken，返回其token/headerName，前端把返回token用于POST/PUT/DELETE。不要自己读取session私有属性或混用cookie原始token和masked token。

首次加载请求csrf；调用me，未登录作为正常初始化状态。登录成功与退出后重新请求csrf，因为认证可能清除旧token。token仅存内存。Spring Security登录成功将认证保存到会话，并提供默认session fixation防护；不要只在Controller手动setAuthentication。

Session Cookie默认JSESSIONID、HttpOnly、SameSite=Lax，本地HTTP Secure=false；会话空闲30分钟超时。Vite /api代理到127.0.0.1:8080，浏览器从http://localhost:5173访问。Axios baseURL=/api、withCredentials=true；所有请求通过代理，不直接访问8080。

GET匿名可读csrf；其余业务API要求登录，/api/admin/**要求ADMIN。禁用默认HTTP Basic和HTML登录页。对未知API路径登录后返回404。CSRF过滤发生在认证之前时，缺少CSRF的写请求可返回403；未登录GET业务应返回401。不自动重放非幂等写请求。

参考官方说明，并在实施时使用匹配Spring Security版本：
- [CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [会话管理](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)

## 示例记录API

门户前缀 /api/records：USER、ADMIN访问，只处理当前用户记录。
管理前缀 /api/admin/records：仅ADMIN，处理全部记录。
两套前缀采用相同方法：

| 方法与后缀 | 行为 |
|---|---|
| GET 空后缀 | 分页查询 |
| GET /{id} | 详情 |
| POST 空后缀 | 新增 |
| PUT /{id} | 修改标题、描述、状态 |
| DELETE /{id} | 物理删除 |

查询参数page默认1，size默认10、最大100；小于1或大于范围返回400。keyword可选最多100字，status可选ENABLED/DISABLED；默认created_at DESC,id DESC，不接受任意SQL排序。关键词使用参数化标题包含查询，LIKE元字符作为文字转义。

新增/修改只接收title、description、status；title去首尾空格后1—100字，description最多2000字，缺省空字符串；新增status缺省ENABLED，修改要求有效status。未知请求字段返回400，包括ownerId，归属由会话决定。

新增/修改返回完整记录；删除返回data=null。不存在返回404；门户访问他人记录也返回404，避免泄漏记录存在性。管理端不限制归属。列表在数据库查询层施加owner_id条件，不在前端过滤。

后端安全处理器、校验异常、业务异常与未知异常均遵循统一格式；记录有用的异常日志。未知异常统一提示“服务暂时不可用”。
