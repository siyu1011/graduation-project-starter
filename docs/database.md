# 数据库与演示数据约定

仅第一版所需两张表。下述为明确DDL规格；实现时写入database/schema.sql。库名graduation_starter，MySQL 8、InnoDB、utf8mb4。应用不自动建表或清空数据。

```sql
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  CONSTRAINT ck_user_role CHECK (role IN ('ADMIN','USER')),
  CONSTRAINT ck_user_enabled CHECK (enabled IN (0,1))
);

CREATE TABLE demo_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description VARCHAR(2000) NOT NULL DEFAULT '',
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  owner_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_record_owner_created (owner_id,created_at,id),
  KEY idx_record_created (created_at,id),
  CONSTRAINT fk_record_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id),
  CONSTRAINT ck_record_status CHECK (status IN ('ENABLED','DISABLED'))
);
```

role映射ROLE_ADMIN/ROLE_USER，字段为单角色；enabled=0禁止登录。实体密码字段不可返回。记录采用物理删除，第一版无软删除、附件、分类及审计历史。实体字段对应ownerId等camelCase，SQL字段snake_case。

## 演示数据

database/demo-data.sql 使用固定演示ID，面向空的新建库导入，不自动重复导入或删除：
- id=1，admin / Admin123!，显示名“演示管理员”，ADMIN；
- id=2，user / User123!，显示名“演示用户”，USER；
- id=3，user2 / User123!，显示名“第二用户”，USER；
- id=4，disabled / Disabled123!，enabled=0，USER，用于拒绝登录测试。
- 25条记录：admin5条、user12条、user2共8条；覆盖ENABLED与DISABLED，含不同标题及描述，用于分页、筛选和归属演示。

密码必须实际生成BCrypt哈希并验证matches，不把上述明文存入password_hash，不填写未验证的占位哈希。这些公开账号仅用于本地演示。

默认数据库由用户明确新建，schema.sql只建表，demo-data.sql只写演示数据。README先说明选择graduation_starter再导入；不提供自动DROP DATABASE流程。

## 配置

后端环境变量：DB_URL（默认jdbc:mysql://localhost:3306/graduation_starter，配置明确字符集/时区）、DB_USERNAME、DB_PASSWORD；SERVER_PORT默认8080。数据库密码不进入仓库。默认业务时区Asia/Shanghai。

README说明CREATE DATABASE、导入两个SQL的顺序与命令，提醒MySQL不同版本约束行为，以应用校验同时保障有效枚举。连接失败给出清楚日志。

## 扩展

后续模块建立独立业务表，复用sys_user作为必要归属；不得把demo_record当作所有选题的万能业务表。增加字段或表时补配套SQL及文档。

## 实现与测试说明

schema.sql与demo-data.sql已提供；演示哈希由实际BCrypt编码器生成，自动测试验证四个公开密码。第6条标题含100%_以验证LIKE元字符按文字处理。

JDBC默认URL在README与application.yml固定字符集、UTC+08:00连接及会话时区。应用业务时区为Asia/Shanghai。MySQL8.0.16以前可能不执行CHECK；应用校验始终生效。

集成测试仅允许独立graduation_starter_test库，由用户先导入原始演示数据；测试事务回滚，不自动清库。
