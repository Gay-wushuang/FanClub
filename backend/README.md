# v0.0代码功能说明

## 项目结构

### com.example

##### anno：日志注解接口

##### aop：面向切面编程函数包

##### filter：拦截器

##### utils：开发工具（JWT升级版，OSS等）

##### exception：全局异常处理

##### exceptions：定义异常类型

##### controller：控制器层

##### service：服务层

##### mapper：DAO数据交互层

##### bilibili：B站API集成
- **pojo**：B站API响应模型
- **service**：B站API服务实现
- **controller**：B站API接口控制器

### resources

##### com/example/mapper：mybatis数据库查询xml

##### application.yaml：配置文件

##### logback.xml：日志配置

## B站直播间数据检测

### 可检测的数据

#### 1. 房间基本信息（/api/v1/bilibili/room/init）

| 字段 | 类型 | 含义 |
|------|------|------|
| room_id | long | 真实房间ID |
| short_id | long | 房间短号 |
| uid | long | 主播UID |
| live_status | int | 直播状态（0:未开播, 1:直播中, 2:轮播） |
| live_time | long | 开播时间戳（已处理异常值） |
| live_time_valid | boolean | 时间戳有效性标记 |
| is_hidden | boolean | 是否隐藏 |
| is_locked | boolean | 是否锁定 |
| is_portrait | boolean | 是否竖屏 |
| encrypted | boolean | 是否加密 |
| pwd_verified | boolean | 是否验证密码 |

#### 2. 房间状态信息（/api/v1/bilibili/room/stats）

| 字段 | 类型 | 含义 |
|------|------|------|
| room_id | long | 真实房间ID |
| uid | long | 主播UID |
| live_status | int | 直播状态（0:未开播, 1:直播中, 2:轮播） |
| live_time | long | 开播时间戳（已处理异常值） |
| live_time_valid | boolean | 时间戳有效性标记 |
| need_p2p | int | 是否需要P2P |
| room_shield | int | 房间屏蔽等级 |
| is_sp | int | 是否为特殊房间 |
| special_type | int | 特殊类型 |

### 直播状态说明

- **0**: 未开播/暂停
- **1**: 直播中
- **2**: 轮播状态（非实时直播）

### 异常处理

- **live_time异常值**：负数时间戳已修正为0，并添加live_time_valid=false标记
- **网络请求异常**：返回友好的错误信息
- **参数错误**：返回参数验证错误

### API调用示例

```bash
# 获取房间初始化信息
curl http://localhost:8080/api/v1/bilibili/room/init?shortId=1838214834

# 获取房间状态信息
curl http://localhost:8080/api/v1/bilibili/room/stats?roomId=1838214834
```