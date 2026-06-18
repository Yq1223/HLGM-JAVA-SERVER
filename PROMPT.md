# 薅羊毛信息社区后台 — 完整开发提示词

## 项目概述

使用 Java（Spring Boot 2.7 + MyBatis-Plus + MySQL）开发一个微信小程序后台，用于「薅羊毛信息」的社区化管理。

---

## 技术栈要求

- **框架**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.5
- **数据库**：MySQL 5.7+ / 8.0+
- **鉴权**：JWT（jjwt 0.11.5）
- **工具库**：Hutool 5.8.25
- **构建工具**：Maven 3.6+
- **Java 版本**：Java 8+
- **⚠️ 不使用 Lombok**（避免注解处理器在部分环境失效）

---

## 一、用户角色与权限

- **游客（未登录）**：可以分页浏览已上线的羊毛信息列表，但无法查看详情。
- **普通用户（通过微信登录后）**：可查看详情，可发布新的羊毛信息；仅能编辑或删除自己发布的信息。
- **管理员**：拥有所有信息的管理权限，包括审核、编辑、删除、上线与下线等操作。

---

## 二、核心业务流程

1. **微信登录**：通过 `wx.login` 获取 openid，后台生成 JWT 作为登录凭证，同时返回用户角色、积分等信息。
2. **信息发布**：任何登录用户均可发布，发布时需填写标题、内容、分类、来源链接、**领取步骤**，发布后状态为「待审核」，仅自己和管理员可见。
3. **管理员审核**：审核通过后信息进入「已上线」状态，系统自动给发布者增加 1 积分，并记录积分变动日志；若不通过，可标记为「审核驳回」并附驳回理由。
4. **信息管理**：
   - 普通用户修改自己的信息后，状态重置为待审核，并等待管理员重新审核。
   - 管理员可对任意信息直接执行上线/下线/删除操作，无需再次审核。
5. **积分与兑换**：用户可用累计积分兑换商品，需实现积分扣减、兑换记录保存，并保证并发安全。

---

## 三、数据库表结构

### 3.1 用户表 t_user

```sql
CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid`      VARCHAR(64)  NOT NULL COMMENT '微信openid',
    `nickname`    VARCHAR(64)  DEFAULT '' COMMENT '昵称',
    `avatar_url`  VARCHAR(512) DEFAULT '' COMMENT '头像URL',
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色: 0=普通用户, 1=管理员',
    `points`      INT          NOT NULL DEFAULT 0 COMMENT '积分余额',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 3.2 羊毛信息表 t_wool_info

```sql
CREATE TABLE `t_wool_info` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT        NOT NULL COMMENT '发布者ID',
    `title`         VARCHAR(128)  NOT NULL COMMENT '标题',
    `content`       TEXT          NOT NULL COMMENT '详细内容',
    `category`      VARCHAR(32)   DEFAULT '' COMMENT '分类',
    `source_url`    VARCHAR(512)  DEFAULT '' COMMENT '来源链接',
    `claim_steps`   TEXT          DEFAULT NULL COMMENT '领取步骤',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=待审核, 1=已上线, 2=审核驳回, 3=已下线',
    `reject_reason` VARCHAR(256)  DEFAULT '' COMMENT '驳回理由',
    `view_count`    INT           NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='羊毛信息表';
```

### 3.3 积分变动记录表 t_points_log

```sql
CREATE TABLE `t_points_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `change_type`   TINYINT      NOT NULL COMMENT '变动类型: 1=发布奖励, 2=兑换扣减, 3=管理员调整',
    `change_value`  INT          NOT NULL COMMENT '变动值(正数增加, 负数减少)',
    `before_points` INT          NOT NULL COMMENT '变动前积分',
    `after_points`  INT          NOT NULL COMMENT '变动后积分',
    `remark`        VARCHAR(128) DEFAULT '' COMMENT '备注',
    `biz_id`        BIGINT       DEFAULT NULL COMMENT '关联业务ID',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变动记录表';
```

### 3.4 兑换商品表 t_exchange_goods

```sql
CREATE TABLE `t_exchange_goods` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(128)  NOT NULL COMMENT '商品名称',
    `description` VARCHAR(512)  DEFAULT '' COMMENT '商品描述',
    `image_url`   VARCHAR(512)  DEFAULT '' COMMENT '商品图片',
    `points_cost` INT           NOT NULL COMMENT '兑换所需积分',
    `stock`       INT           NOT NULL DEFAULT 0 COMMENT '库存数量',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=下架, 1=上架',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换商品表';
```

### 3.5 兑换记录表 t_exchange_record

```sql
CREATE TABLE `t_exchange_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
    `goods_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `goods_name`  VARCHAR(128)  NOT NULL COMMENT '商品名称(冗余)',
    `points_cost` INT           NOT NULL COMMENT '消耗积分',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=已取消, 1=已兑换',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换记录表';
```

---

## 四、接口设计

### 统一返回格式

```json
{
  "code": 0,
  "msg": "success",
  "data": { ... }
}
```

### 认证方式

需登录的接口在 Header 中传递：`Authorization: Bearer <token>`

### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 微信登录 | 公开 |
| GET | /api/wool/list | 已上线信息列表（分页，支持keyword搜索） | 公开 |
| GET | /api/wool/detail/{id} | 信息详情 | 需登录 |
| POST | /api/wool/publish | 发布信息 | 需登录 |
| PUT | /api/wool/update/{id} | 修改自己的信息 | 需登录 |
| DELETE | /api/wool/delete/{id} | 删除自己的信息 | 需登录 |
| GET | /api/wool/mine | 我的信息列表 | 需登录 |
| GET | /api/admin/wool/list | 管理员查询所有信息 | 管理员 |
| POST | /api/admin/wool/audit/{id} | 审核信息（通过/驳回） | 管理员 |
| PUT | /api/admin/wool/online/{id} | 上线信息 | 管理员 |
| PUT | /api/admin/wool/offline/{id} | 下线信息 | 管理员 |
| DELETE | /api/admin/wool/delete/{id} | 删除任意信息 | 管理员 |
| GET | /api/points/balance | 查询积分余额 | 需登录 |
| GET | /api/points/log | 积分变动记录 | 需登录 |
| GET | /api/exchange/goods | 可兑换商品列表 | 公开 |
| POST | /api/exchange/do | 兑换商品 | 需登录 |
| GET | /api/exchange/records | 我的兑换记录 | 需登录 |

---

## 五、技术实现要点

### 5.1 禁止使用 Lombok

所有实体类、DTO、VO 必须手写 getter/setter，日志使用手动声明 Logger：

```java
// ❌ 错误写法（不要用 Lombok）
@Data
@Slf4j
public class User { ... }

// ✅ 正确写法（手写 getter/setter + Logger）
public class User {
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}

@Service
public class SomeServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(SomeServiceImpl.class);
}
```

**原因**：Lombok 注解处理器在部分环境下不生效（IDE 未安装插件、Maven 编译器未配置注解处理器等），导致编译报大量 `找不到符号` 错误。去掉 Lombok 可以保证任何环境都能直接编译通过。

### 5.2 MyBatis-Plus Wrapper 使用规范

```java
// ❌ 错误：LambdaQueryWrapper 没有 setSql 方法
woolInfoMapper.update(null, new LambdaQueryWrapper<WoolInfo>()
    .eq(WoolInfo::getId, id)
    .setSql("view_count = view_count + 1"));

// ✅ 正确：使用 UpdateWrapper
UpdateWrapper<WoolInfo> uw = new UpdateWrapper<>();
uw.eq("id", id).setSql("view_count = view_count + 1");
woolInfoMapper.update(null, uw);
```

**原因**：MyBatis-Plus 3.5.5 中，`setSql` 方法只存在于 `UpdateWrapper`，不存在于 `LambdaQueryWrapper`。

### 5.3 JDBC 连接字符编码

```yaml
# ❌ 错误：JDBC 驱动不识别 utf8mb4
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wool_db?characterEncoding=utf8mb4

# ✅ 正确：JDBC 使用 utf8（数据库表本身仍然是 utf8mb4，不影响存储）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wool_db?characterEncoding=utf8
```

**原因**：Java 的 MySQL JDBC 驱动只认 `utf8` 作为 characterEncoding 参数值，不认 `utf8mb4`。这是 JDBC 驱动层面的限制，和数据库/表实际使用的 utf8mb4 字符集无关。

### 5.4 积分并发安全

积分扣减使用**条件更新**保证并发安全：

```sql
UPDATE t_user SET points = points - #{deduct}
WHERE id = #{id} AND points >= #{deduct}
```

库存扣减同理：

```sql
UPDATE t_exchange_goods SET stock = stock - #{quantity}
WHERE id = #{id} AND stock >= #{quantity}
```

如果 `affected rows = 0`，说明余额/库存不足，抛出异常回滚事务。

### 5.5 鉴权拦截器配置

```java
// 需要登录的接口
registry.addInterceptor(authInterceptor)
    .addPathPatterns("/api/**")
    .excludePathPatterns(
        "/api/auth/login",
        "/api/wool/list",
        "/api/exchange/goods"
    );

// 管理员接口（在 auth 拦截器之后）
registry.addInterceptor(adminInterceptor)
    .addPathPatterns("/api/admin/**");
```

### 5.6 项目目录结构

```
wool-backend/
├── sql/init.sql
├── pom.xml
└── src/main/
    ├── resources/application.yml
    └── java/com/wool/
        ├── WoolApplication.java
        ├── common/          (R, BizException, GlobalExceptionHandler, Constants, WoolStatus, PointsChangeType)
        ├── config/          (WebMvcConfig, MybatisPlusConfig, MyMetaObjectHandler)
        ├── entity/          (User, WoolInfo, PointsLog, ExchangeGoods, ExchangeRecord)
        ├── mapper/          (对应 entity 的 Mapper 接口)
        ├── dto/             (WxLoginDTO, WoolInfoDTO, AuditDTO, ExchangeDTO)
        ├── vo/              (LoginVO, WoolInfoVO, PointsLogVO)
        ├── util/            (JwtUtil)
        ├── interceptor/     (AuthInterceptor, AdminInterceptor)
        ├── service/         (接口 + impl/)
        └── controller/      (Auth, WoolInfo, Admin, Points, Exchange)
```

---

## 六、踩坑总结（编译/运行阶段）

| 序号 | 问题 | 报错信息 | 原因 | 解决方案 |
|------|------|----------|------|----------|
| 1 | Lombok 注解不生效 | `找不到符号: 方法 getCode()/setOpenid()/getNickname()` | Lombok 注解处理器未加载 | 去掉 Lombok，手写 getter/setter |
| 2 | Lombok @Slf4j 不生效 | `找不到符号: 变量 log` | 同上 | 手动声明 `private static final Logger log = LoggerFactory.getLogger(XXX.class);` |
| 3 | LambdaQueryWrapper 无 setSql | `找不到符号: 方法 setSql(java.lang.String)` | MyBatis-Plus 3.5.5 中 setSql 只在 UpdateWrapper 上 | 改用 `UpdateWrapper` |
| 4 | JDBC 字符编码不识别 | `Unsupported character encoding 'utf8mb4'` | Java JDBC 驱动只认 `utf8` | URL 中改为 `characterEncoding=utf8` |
| 5 | 改了代码但没生效 | 同样的错误 | 没有重新编译打包 | `mvn clean package -DskipTests` 后重新 `java -jar` |

---

## 七、发布接口请求示例

```json
POST /api/wool/publish
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "支付宝扫码领红包",
  "content": "打开支付宝扫描二维码，可领取最高99元消费红包...",
  "category": "红包",
  "sourceUrl": "https://www.alipay.com",
  "claimSteps": "1. 打开支付宝APP\n2. 点击扫一扫\n3. 扫描上方二维码\n4. 领取红包后自动存入卡包\n5. 线下付款时自动抵扣"
}
```

---

## 八、部署注意事项

1. **MySQL 建库建表**：执行 `sql/init.sql`
2. **修改 application.yml**：数据库密码、微信 AppID/Secret、JWT 密钥
3. **编译**：`mvn clean package -DskipTests`
4. **运行**：`java -jar target/wool-backend-1.0.0.jar`
5. **创建管理员**：`UPDATE t_user SET role = 1 WHERE openid = 'xxx';`
6. **如果数据库已存在需加字段**：`ALTER TABLE t_wool_info ADD COLUMN claim_steps TEXT DEFAULT NULL COMMENT '领取步骤' AFTER source_url;`
