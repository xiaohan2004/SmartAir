# SmartAir 后端 API 文档

## 目录

1. [认证 API](#认证-api)
2. [用户 API](#用户-api)
3. [航班 API](#航班-api)
4. [订单 API](#订单-api)
5. [会话 API](#会话-api)
6. [系统日志 API](#系统日志-api)
7. [提示模板 API](#提示模板-api)
8. [知识库 API](#知识库-api)
9. [仪表盘 API](#仪表盘-api)
10. [管理员用户 API](#管理员用户-api)

## 认证 API

### 1. 用户登录

- **URL**: `/api/auth/login`
- **方法**: `POST`
- **描述**: 用户登录接口，根据用户名、密码和角色进行认证。

**请求参数**:

```json
{
  "username": "user123",
  "password": "password123",
  "role": "user" // 可选值：user(普通用户), service(客服), admin(管理员)
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoidXNlcjEyMyIsInVzZXJUeXBlIjoxLCJleHAiOjE2MTYxNTE2MDB9.xYB1tGmZtZJ94xGCjZ-EF9gX5a_QMYQ"
}
```

**失败响应**:

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

### 2. 发送邮箱验证码

- **URL**: `/api/auth/sendVerificationCode`
- **方法**: `POST`
- **描述**: 发送验证码到用户邮箱，用于注册或重置密码。

**请求参数**:

```json
{
  "email": "user@example.com",
  "type": "register" // 可选值：register(注册), reset(重置密码)
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 400,
  "message": "邮箱不合法",
  "data": null
}
```

### 3. 用户注册

- **URL**: `/api/auth/register`
- **方法**: `POST`
- **描述**: 用户注册接口，需先获取邮箱验证码。

**请求参数**:

```json
{
  "user": {
    "username": "newuser",
    "password": "password123",
    "email": "user@example.com",
    "phone": "13800138000",
    "realName": "张三",
    "idCard": "110101199001011234"
  },
  "code": "123456" // 邮箱验证码
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "data": null
}
```

### 4. 重置密码

- **URL**: `/api/auth/resetPassword`
- **方法**: `POST`
- **描述**: 重置用户密码，需先获取邮箱验证码。

**请求参数**:

```json
{
  "username": "user123",
  "email": "user@example.com",
  "newPassword": "newpassword123",
  "code": "123456" // 邮箱验证码
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "data": null
}
```

## 用户 API

### 1. 获取用户信息

- **URL**: `/api/user/{id}`
- **方法**: `GET`
- **描述**: 获取指定ID的用户详细信息。

**路径参数**:
- `id`: 用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "phone": "13800138000",
    "realName": "张三",
    "idCard": "110101199001011234",
    "userType": 1,
    "memberLevel": 2
  }
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

### 2. 更新用户信息

- **URL**: `/api/user/{id}`
- **方法**: `PUT`
- **描述**: 更新指定ID的用户信息（不包括密码）。

**路径参数**:
- `id`: 用户ID

**请求参数**:

```json
{
  "email": "newemail@example.com",
  "phone": "13900139000",
  "realName": "李四",
  "idCard": "110101199002022345"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "用户信息更新成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

### 3. 修改密码

- **URL**: `/api/user/modifyPassword`
- **方法**: `POST`
- **描述**: 修改用户密码，需提供旧密码进行验证。

**请求参数**:

```json
{
  "username": "user123",
  "oldPassword": "password123",
  "newPassword": "newpassword123"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 401,
  "message": "旧密码错误",
  "data": null
}
```

## 航班 API

### 1. 获取航班列表

- **URL**: `/api/flight/list`
- **方法**: `GET`
- **描述**: 获取所有航班信息列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "flightNo": "CA1234",
      "airline": "中国国际航空",
      "departureCity": "北京",
      "arrivalCity": "上海",
      "scheduledDepartureTime": "2023-12-01T10:00:00",
      "scheduledArrivalTime": "2023-12-01T12:30:00",
      "aircraftType": "波音737",
      "price": 1200.00,
      "createdAt": "2023-11-01T09:45:00",
      "updatedAt": "2023-11-01T09:45:00"
    },
    {
      "id": 2,
      "flightNo": "MU5678",
      "airline": "东方航空",
      "departureCity": "广州",
      "arrivalCity": "深圳",
      "scheduledDepartureTime": "2023-12-02T14:30:00",
      "scheduledArrivalTime": "2023-12-02T15:30:00",
      "aircraftType": "空客A320",
      "price": 800.00,
      "createdAt": "2023-11-01T10:20:00",
      "updatedAt": "2023-11-01T10:20:00"
    }
  ]
}
```

### 2. 获取航班详情

- **URL**: `/api/flight/{id}`
- **方法**: `GET`
- **描述**: 获取指定ID的航班详细信息。

**路径参数**:
- `id`: 航班ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "flightNo": "CA1234",
    "airline": "中国国际航空",
    "departureCity": "北京",
    "arrivalCity": "上海",
    "scheduledDepartureTime": "2023-12-01T10:00:00",
    "scheduledArrivalTime": "2023-12-01T12:30:00",
    "aircraftType": "波音737",
    "price": 1200.00,
    "createdAt": "2023-11-01T09:45:00",
    "updatedAt": "2023-11-01T09:45:00"
  }
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "航班不存在",
  "data": null
}
```

### 3. 根据航班号查询航班

- **URL**: `/api/flight/no/{flightNo}`
- **方法**: `GET`
- **描述**: 根据航班号获取航班信息。

**路径参数**:
- `flightNo`: 航班号

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "flightNo": "CA1234",
    "airline": "中国国际航空",
    "departureCity": "北京",
    "arrivalCity": "上海",
    "scheduledDepartureTime": "2023-12-01T10:00:00",
    "scheduledArrivalTime": "2023-12-01T12:30:00",
    "aircraftType": "波音737",
    "price": 1200.00,
    "createdAt": "2023-11-01T09:45:00",
    "updatedAt": "2023-11-01T09:45:00"
  }
}
```

### 4. 搜索航班

- **URL**: `/api/flight/search`
- **方法**: `GET`
- **描述**: 根据出发城市、到达城市和时间范围搜索航班。

**查询参数**:
- `departureCity`: 出发城市
- `arrivalCity`: 到达城市
- `startTime`: 开始时间（ISO 8601格式）
- `endTime`: 结束时间（ISO 8601格式）

**请求示例**:
```
GET /api/flight/search?departureCity=北京&arrivalCity=上海&startTime=2023-12-01T00:00:00&endTime=2023-12-02T00:00:00
```

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "flightNo": "CA1234",
      "airline": "中国国际航空",
      "departureCity": "北京",
      "arrivalCity": "上海",
      "scheduledDepartureTime": "2023-12-01T10:00:00",
      "scheduledArrivalTime": "2023-12-01T12:30:00",
      "aircraftType": "波音737",
      "price": 1200.00,
      "createdAt": "2023-11-01T09:45:00",
      "updatedAt": "2023-11-01T09:45:00"
    },
    {
      "id": 3,
      "flightNo": "CA1236",
      "airline": "中国国际航空",
      "departureCity": "北京",
      "arrivalCity": "上海",
      "scheduledDepartureTime": "2023-12-01T16:00:00",
      "scheduledArrivalTime": "2023-12-01T18:30:00",
      "aircraftType": "波音787",
      "price": 1500.00,
      "createdAt": "2023-11-01T11:30:00",
      "updatedAt": "2023-11-01T11:30:00"
    }
  ]
}
```

### 5. 根据航空公司查询航班

- **URL**: `/api/flight/airline/{airline}`
- **方法**: `GET`
- **描述**: 获取指定航空公司的航班列表。

**路径参数**:
- `airline`: 航空公司名称

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "flightNo": "CA1234",
      "airline": "中国国际航空",
      "departureCity": "北京",
      "arrivalCity": "上海",
      "scheduledDepartureTime": "2023-12-01T10:00:00",
      "scheduledArrivalTime": "2023-12-01T12:30:00",
      "aircraftType": "波音737",
      "price": 1200.00,
      "createdAt": "2023-11-01T09:45:00",
      "updatedAt": "2023-11-01T09:45:00"
    }
  ]
}
```

### 6. 添加航班（管理员）

- **URL**: `/api/flight`
- **方法**: `POST`
- **描述**: 添加新航班信息。

**请求参数**:

```json
{
  "flightNo": "CA9876",
  "airline": "中国国际航空",
  "departureCity": "成都",
  "arrivalCity": "北京",
  "scheduledDepartureTime": "2023-12-05T08:00:00",
  "scheduledArrivalTime": "2023-12-05T10:45:00",
  "aircraftType": "空客A330",
  "price": 1800.00
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "添加航班成功",
  "data": {
    "id": 10,
    "flightNo": "CA9876",
    "airline": "中国国际航空",
    "departureCity": "成都",
    "arrivalCity": "北京",
    "scheduledDepartureTime": "2023-12-05T08:00:00",
    "scheduledArrivalTime": "2023-12-05T10:45:00",
    "aircraftType": "空客A330",
    "price": 1800.00,
    "createdAt": "2023-11-15T14:30:00",
    "updatedAt": "2023-11-15T14:30:00"
  }
}
```

### 7. 更新航班信息（管理员）

- **URL**: `/api/flight/{id}`
- **方法**: `PUT`
- **描述**: 更新指定ID的航班信息。

**路径参数**:
- `id`: 航班ID

**请求参数**:

```json
{
  "flightNo": "CA9876",
  "price": 1650.00,
  "scheduledDepartureTime": "2023-12-05T08:30:00",
  "scheduledArrivalTime": "2023-12-05T11:15:00"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "更新航班成功",
  "data": {
    "id": 10,
    "flightNo": "CA9876",
    "airline": "中国国际航空",
    "departureCity": "成都",
    "arrivalCity": "北京",
    "scheduledDepartureTime": "2023-12-05T08:30:00",
    "scheduledArrivalTime": "2023-12-05T11:15:00",
    "aircraftType": "空客A330",
    "price": 1650.00,
    "createdAt": "2023-11-15T14:30:00",
    "updatedAt": "2023-11-15T15:20:00"
  }
}
```

### 8. 删除航班（管理员）

- **URL**: `/api/flight/{id}`
- **方法**: `DELETE`
- **描述**: 删除指定ID的航班。

**路径参数**:
- `id`: 航班ID

**成功响应**:

```json
{
  "code": 200,
  "message": "删除航班成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "航班不存在",
  "data": null
}
```

## 订单 API

### 1. 获取订单详情

- **URL**: `/api/order/{id}`
- **方法**: `GET`
- **描述**: 获取指定ID的订单详细信息。

**路径参数**:
- `id`: 订单ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "flightId": 1,
    "seatNo": "A12",
    "status": 1,
    "createdAt": "2023-11-10T15:30:00",
    "updatedAt": "2023-11-10T15:30:00"
  }
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "订单不存在",
  "data": null
}
```

### 2. 获取用户订单列表

- **URL**: `/api/order/user/{userId}`
- **方法**: `GET`
- **描述**: 获取指定用户的所有订单列表。

**路径参数**:
- `userId`: 用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "flightId": 1,
      "seatNo": "A12",
      "status": 1,
      "createdAt": "2023-11-10T15:30:00",
      "updatedAt": "2023-11-10T15:30:00"
    },
    {
      "id": 5,
      "userId": 1,
      "flightId": 3,
      "seatNo": "B08",
      "status": 2,
      "createdAt": "2023-11-12T09:15:00",
      "updatedAt": "2023-11-12T14:25:00"
    }
  ]
}
```

### 3. 获取用户订单详情列表

- **URL**: `/api/order/user/{userId}/detail`
- **方法**: `GET`
- **描述**: 获取指定用户的所有订单详情列表，包含航班和用户信息。

**路径参数**:
- `userId`: 用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "flightId": 1,
      "seatNo": "A12",
      "status": 1,
      "createdAt": "2023-11-10T15:30:00",
      "updatedAt": "2023-11-10T15:30:00",
      "user": {
        "id": 1,
        "username": "user123",
        "email": "user@example.com",
        "phone": "13800138000",
        "realName": "张三",
        "idCard": "110101199001011234",
        "userType": 1,
        "memberLevel": 2
      },
      "flight": {
        "id": 1,
        "flightNo": "CA1234",
        "airline": "中国国际航空",
        "departureCity": "北京",
        "arrivalCity": "上海",
        "scheduledDepartureTime": "2023-12-01T10:00:00",
        "scheduledArrivalTime": "2023-12-01T12:30:00",
        "aircraftType": "波音737",
        "price": 1200.00
      }
    }
  ]
}
```

### 4. 获取航班订单列表

- **URL**: `/api/order/flight/{flightId}`
- **方法**: `GET`
- **描述**: 获取指定航班的所有订单列表。

**路径参数**:
- `flightId`: 航班ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "flightId": 1,
      "seatNo": "A12",
      "status": 1,
      "createdAt": "2023-11-10T15:30:00",
      "updatedAt": "2023-11-10T15:30:00"
    },
    {
      "id": 3,
      "userId": 2,
      "flightId": 1,
      "seatNo": "A13",
      "status": 1,
      "createdAt": "2023-11-10T16:45:00",
      "updatedAt": "2023-11-10T16:45:00"
    }
  ]
}
```

### 5. 创建订单

- **URL**: `/api/order`
- **方法**: `POST`
- **描述**: 创建新的航班订单。

**请求参数**:

```json
{
  "userId": 1,
  "flightId": 2,
  "seatNo": "C15"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "id": 10,
    "userId": 1,
    "flightId": 2,
    "seatNo": "C15",
    "status": 1,
    "createdAt": "2023-11-15T11:20:00",
    "updatedAt": "2023-11-15T11:20:00"
  }
}
```

### 6. 取消订单

- **URL**: `/api/order/{id}/cancel`
- **方法**: `PUT`
- **描述**: 取消指定ID的订单。

**路径参数**:
- `id`: 订单ID

**成功响应**:

```json
{
  "code": 200,
  "message": "订单取消成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "订单不存在",
  "data": null
}
```

### 7. 获取所有订单列表（管理员）

- **URL**: `/api/order/admin/list`
- **方法**: `GET`
- **描述**: 管理员获取所有订单列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "flightId": 1,
      "seatNo": "A12",
      "status": 1,
      "createdAt": "2023-11-10T15:30:00",
      "updatedAt": "2023-11-10T15:30:00"
    },
    {
      "id": 2,
      "userId": 3,
      "flightId": 4,
      "seatNo": "D20",
      "status": 1,
      "createdAt": "2023-11-10T16:15:00",
      "updatedAt": "2023-11-10T16:15:00"
    }
  ]
}
```

## 会话 API

### 1. 获取会话详情（索引信息）

- **URL**: `/api/conversation/{uuid}`
- **方法**: `GET`
- **描述**: 获取指定UUID的会话索引信息。

**路径参数**:
- `uuid`: 会话UUID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "conversationUuid": "550e8400-e29b-41d4-a716-446655440000",
    "lastMessage": "您好，请问有什么可以帮到您？",
    "status": 1,
    "serviceUserId": null,
    "updatedAt": "2023-11-16T09:30:00"
  }
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "会话不存在",
  "data": null
}
```

### 2. 获取会话详情（包含完整消息内容）

- **URL**: `/api/conversation/{uuid}/content`
- **方法**: `GET`
- **描述**: 获取指定UUID的会话完整消息内容。

**路径参数**:
- `uuid`: 会话UUID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "60f7b5e9a33d2b4e10b4e123",
    "conversationUuid": "550e8400-e29b-41d4-a716-446655440000",
    "userId": 1,
    "messages": [
      {
        "speaker": "user",
        "text": "您好，我想查询航班信息",
        "timestamp": "2023-11-16T09:28:00"
      },
      {
        "speaker": "system",
        "text": "您好，请问有什么可以帮到您？",
        "timestamp": "2023-11-16T09:30:00"
      }
    ],
    "metadata": {
      "sessionStart": "2023-11-16T09:28:00",
      "sessionEnd": "2023-11-16T09:30:00"
    }
  }
}
```

### 3. 获取用户会话列表（索引信息）

- **URL**: `/api/conversation/user/{userId}`
- **方法**: `GET`
- **描述**: 获取指定用户的所有会话索引列表。

**路径参数**:
- `userId`: 用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440000",
      "lastMessage": "您好，请问有什么可以帮到您？",
      "status": 1,
      "serviceUserId": null,
      "updatedAt": "2023-11-16T09:30:00"
    },
    {
      "id": 5,
      "userId": 1,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440005",
      "lastMessage": "感谢您的咨询，再见！",
      "status": 3,
      "serviceUserId": 10,
      "updatedAt": "2023-11-14T15:45:00"
    }
  ]
}
```

### 4. 获取用户会话列表（包含完整消息内容）

- **URL**: `/api/conversation/user/{userId}/contents`
- **方法**: `GET`
- **描述**: 获取指定用户的所有会话完整内容列表。

**路径参数**:
- `userId`: 用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "60f7b5e9a33d2b4e10b4e123",
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440000",
      "userId": 1,
      "messages": [
        {
          "speaker": "user",
          "text": "您好，我想查询航班信息",
          "timestamp": "2023-11-16T09:28:00"
        },
        {
          "speaker": "system",
          "text": "您好，请问有什么可以帮到您？",
          "timestamp": "2023-11-16T09:30:00"
        }
      ],
      "metadata": {
        "sessionStart": "2023-11-16T09:28:00",
        "sessionEnd": "2023-11-16T09:30:00"
      }
    }
  ]
}
```

### 5. 获取客服会话列表

- **URL**: `/api/conversation/service/{serviceUserId}`
- **方法**: `GET`
- **描述**: 获取指定客服的所有已转人工会话列表。

**路径参数**:
- `serviceUserId`: 客服用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 3,
      "userId": 2,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440003",
      "lastMessage": "好的，稍等，我帮您查询一下。",
      "status": 2,
      "serviceUserId": 10,
      "updatedAt": "2023-11-16T10:15:00",
      "user": {
        "id": 2,
        "username": "user456",
        "email": "user456@example.com",
        "phone": "13900139000",
        "realName": "李四",
        "userType": 1,
        "memberLevel": 1
      },
      "serviceUser": {
        "id": 10,
        "username": "service001",
        "email": "service001@example.com",
        "phone": "13600136000",
        "realName": "王五",
        "userType": 2
      }
    }
  ]
}
```

### 6. 获取所有已转人工的会话

- **URL**: `/api/conversation/transferred`
- **方法**: `GET`
- **描述**: 获取所有已转人工的会话列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 3,
      "userId": 2,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440003",
      "lastMessage": "好的，稍等，我帮您查询一下。",
      "status": 2,
      "serviceUserId": 10,
      "updatedAt": "2023-11-16T10:15:00"
    }
  ]
}
```

### 7. 获取用户活跃会话

- **URL**: `/api/conversation/user/{userId}/active`
- **方法**: `GET`
- **描述**: 获取指定用户的当前活跃会话。

**路径参数**:
- `userId`: 用户ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "conversationUuid": "550e8400-e29b-41d4-a716-446655440000",
    "lastMessage": "您好，请问有什么可以帮到您？",
    "status": 1,
    "serviceUserId": null,
    "updatedAt": "2023-11-16T09:30:00"
  }
}
```

### 8. 创建新会话（JSON 请求体）

- **URL**: `/api/conversation`
- **方法**: `POST`
- **描述**: 创建新的会话，使用 JSON 请求体。

**请求参数**:

```json
{
  "userId": 1,
  "initialMessage": "您好，我想查询航班信息"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "会话创建成功",
  "data": {
    "id": 10,
    "userId": 1,
    "conversationUuid": "550e8400-e29b-41d4-a716-446655440010",
    "lastMessage": "您好，我想查询航班信息",
    "status": 1,
    "serviceUserId": null,
    "updatedAt": "2023-11-16T11:20:00"
  }
}
```

### 9. 创建新会话（请求参数）

- **URL**: `/api/conversation/start`
- **方法**: `POST`
- **描述**: 创建新的会话，使用请求参数。

**请求参数**:
- `userId`: 用户ID
- `userInput`: 用户输入的初始消息

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "index": {
      "id": 10,
      "userId": 1,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440010",
      "lastMessage": "您好，我想查询航班信息",
      "status": 1,
      "serviceUserId": null,
      "updatedAt": "2023-11-16T11:20:00"
    },
    "content": {
      "id": "60f7b5e9a33d2b4e10b4e456",
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440010",
      "userId": 1,
      "messages": [
        {
          "speaker": "user",
          "text": "您好，我想查询航班信息",
          "timestamp": "2023-11-16T11:20:00"
        }
      ],
      "metadata": {
        "sessionStart": "2023-11-16T11:20:00",
        "sessionEnd": null
      }
    }
  }
}
```

### 10. 追加消息到会话

- **URL**: `/api/conversation/{uuid}/append`
- **方法**: `POST`
- **描述**: 向指定会话追加新消息。

**路径参数**:
- `uuid`: 会话UUID

**请求参数**:
- `speaker`: 发言者（例如："user", "system", "service"）
- `text`: 消息内容

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 500,
  "message": "消息追加失败",
  "data": null
}
```

### 11. 转接会话到客服

- **URL**: `/api/conversation/{uuid}/transfer`
- **方法**: `PUT`
- **描述**: 将会话转接给指定客服。

**路径参数**:
- `uuid`: 会话UUID

**请求参数**:

```json
{
  "serviceUserId": 10
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "会话转接成功",
  "data": null
}
```

### 12. 关闭会话

- **URL**: `/api/conversation/{uuid}/close`
- **方法**: `PUT`
- **描述**: 关闭指定的会话。

**路径参数**:
- `uuid`: 会话UUID

**成功响应**:

```json
{
  "code": 200,
  "message": "会话关闭成功",
  "data": null
}
```

### 13. 删除会话

- **URL**: `/api/conversation/{id}`
- **方法**: `DELETE`
- **描述**: 删除指定的会话（同时删除索引和内容）。

**路径参数**:
- `id`: 会话UUID

**成功响应**:

```json
{
  "code": 200,
  "message": "会话删除成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 500,
  "message": "会话删除失败",
  "data": null
}
```

### 14. 获取所有会话列表（管理员）

- **URL**: `/api/conversation/admin/list`
- **方法**: `GET`
- **描述**: 管理员获取所有会话列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440000",
      "lastMessage": "您好，请问有什么可以帮到您？",
      "status": 1,
      "serviceUserId": null,
      "updatedAt": "2023-11-16T09:30:00"
    },
    {
      "id": 2,
      "userId": 2,
      "conversationUuid": "550e8400-e29b-41d4-a716-446655440002",
      "lastMessage": "感谢您的咨询，再见！",
      "status": 3,
      "serviceUserId": null,
      "updatedAt": "2023-11-15T14:25:00"
    }
  ]
}
```

### 已废弃的接口

以下接口已被废弃，但为保持兼容性而保留：

#### 获取用户会话列表（MongoDB 内容）

- **URL**: `/deprecated/mongodb/api/conversation/user/{userId}`
- **方法**: `GET`
- **描述**: 获取指定用户的所有会话内容列表（已废弃，请使用 `/api/conversation/user/{userId}/contents`）。

#### 创建新会话（MongoDB）

- **URL**: `/deprecated/mongodb/api/conversation/start`
- **方法**: `POST`
- **描述**: 创建新的会话（已废弃，请使用 `/api/conversation/start`）。

#### 追加消息到会话（MongoDB）

- **URL**: `/deprecated/mongodb/api/conversation/{uuid}/append`
- **方法**: `POST`
- **描述**: 向指定会话追加新消息（已废弃，请使用 `/api/conversation/{uuid}/append`）。

#### 删除会话（MongoDB）

- **URL**: `/deprecated/mongodb/api/conversation/{id}`
- **方法**: `DELETE`
- **描述**: 删除指定的会话（已废弃，请使用 `/api/conversation/{id}`）。

## 系统日志 API

### 1. 获取日志列表（管理员）

- **URL**: `/api/admin/log/list`
- **方法**: `GET`
- **描述**: 获取所有系统日志列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "operatorId": 3,
      "className": "com.backend.controller.AuthController",
      "methodName": "login",
      "executionTime": 125,
      "message": "用户登录成功: admin",
      "createdAt": "2023-11-16T08:30:00"
    },
    {
      "id": 2,
      "operatorId": 1,
      "className": "com.backend.controller.FlightController",
      "methodName": "searchFlights",
      "executionTime": 87,
      "message": "用户搜索航班: 北京 - 上海",
      "createdAt": "2023-11-16T08:45:00"
    }
  ]
}
```

### 2. 根据时间范围查询日志（管理员）

- **URL**: `/api/admin/log/search`
- **方法**: `GET`
- **描述**: 根据时间范围查询系统日志。

**查询参数**:
- `startTime`: 开始时间（ISO 8601格式）
- `endTime`: 结束时间（ISO 8601格式）

**请求示例**:
```
GET /api/admin/log/search?startTime=2023-11-15T00:00:00&endTime=2023-11-16T00:00:00
```

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 5,
      "operatorId": 3,
      "className": "com.backend.controller.FlightController",
      "methodName": "addFlight",
      "executionTime": 156,
      "message": "管理员添加新航班: MU5678",
      "createdAt": "2023-11-15T10:20:00"
    }
  ]
}
```

### 3. 添加日志

- **URL**: `/api/admin/log`
- **方法**: `POST`
- **描述**: 添加新的系统日志。

**请求参数**:

```json
{
  "message": "用户修改个人信息"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "日志添加成功",
  "data": null
}
```

### 4. 清理指定时间之前的日志（管理员）

- **URL**: `/api/admin/log/clean`
- **方法**: `DELETE`
- **描述**: 清理指定时间之前的系统日志。

**请求参数**:

```json
{
  "beforeTime": "2023-11-01T00:00:00"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "成功清理 56 条日志",
  "data": 56
}
```

### 5. 获取日志详情（管理员）

- **URL**: `/api/admin/log/{id}`
- **方法**: `GET`
- **描述**: 获取指定ID的日志详细信息。

**路径参数**:
- `id`: 日志ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "operatorId": 3,
    "className": "com.backend.controller.AuthController",
    "methodName": "login",
    "executionTime": 125,
    "message": "用户登录成功: admin",
    "createdAt": "2023-11-16T08:30:00"
  }
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "日志不存在",
  "data": null
}
```

## 提示模板 API

### 1. 创建新模板

- **URL**: `/api/prompt`
- **方法**: `POST`
- **描述**: 创建新的提示模板。

**请求参数**:

```json
{
  "name": "flight_query",
  "template": "您好，请问您想查询从{departure}到{arrival}的航班信息吗？",
  "description": "航班查询提示模板"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "60f7b5e9a33d2b4e10b4e789",
    "name": "flight_query",
    "template": "您好，请问您想查询从{departure}到{arrival}的航班信息吗？",
    "description": "航班查询提示模板",
    "createdAt": "2023-11-20T09:30:00",
    "updatedAt": "2023-11-20T09:30:00"
  }
}
```

### 2. 按名称查询模板

- **URL**: `/api/prompt/name/{name}`
- **方法**: `GET`
- **描述**: 根据模板名称查询提示模板。

**路径参数**:
- `name`: 模板名称

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "60f7b5e9a33d2b4e10b4e789",
    "name": "flight_query",
    "template": "您好，请问您想查询从{departure}到{arrival}的航班信息吗？",
    "description": "航班查询提示模板",
    "createdAt": "2023-11-20T09:30:00",
    "updatedAt": "2023-11-20T09:30:00"
  }
}
```

**失败响应**:

```json
{
  "code": 500,
  "message": "模板不存在",
  "data": null
}
```

### 3. 查询所有模板

- **URL**: `/api/prompt`
- **方法**: `GET`
- **描述**: 获取所有提示模板列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "60f7b5e9a33d2b4e10b4e789",
      "name": "flight_query",
      "template": "您好，请问您想查询从{departure}到{arrival}的航班信息吗？",
      "description": "航班查询提示模板",
      "createdAt": "2023-11-20T09:30:00",
      "updatedAt": "2023-11-20T09:30:00"
    },
    {
      "id": "60f7b5e9a33d2b4e10b4e790",
      "name": "order_cancel",
      "template": "您确定要取消订单{orderId}吗？",
      "description": "订单取消确认提示模板",
      "createdAt": "2023-11-20T10:15:00",
      "updatedAt": "2023-11-20T10:15:00"
    }
  ]
}
```

### 4. 更新模板内容

- **URL**: `/api/prompt/{id}`
- **方法**: `PUT`
- **描述**: 更新指定ID的提示模板内容。

**路径参数**:
- `id`: 模板ID

**请求参数**:
模板内容字符串

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 5. 删除模板

- **URL**: `/api/prompt/{id}`
- **方法**: `DELETE`
- **描述**: 删除指定ID的提示模板。

**路径参数**:
- `id`: 模板ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

## 知识库 API

### 1. 添加知识文档

- **URL**: `/api/knowledge`
- **方法**: `POST`
- **描述**: 添加新的知识文档。

**请求参数**:

```json
{
  "title": "如何查询航班信息",
  "content": "1. 登录系统\n2. 点击航班查询\n3. 输入出发地和目的地\n4. 选择日期\n5. 点击搜索",
  "tags": ["航班", "查询", "使用指南"]
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "60f7b5e9a33d2b4e10b4e800",
    "title": "如何查询航班信息",
    "content": "1. 登录系统\n2. 点击航班查询\n3. 输入出发地和目的地\n4. 选择日期\n5. 点击搜索",
    "tags": ["航班", "查询", "使用指南"],
    "createdAt": "2023-11-21T09:30:00",
    "updatedAt": "2023-11-21T09:30:00"
  }
}
```

### 2. 搜索知识文档

- **URL**: `/api/knowledge/search`
- **方法**: `GET`
- **描述**: 根据关键词搜索知识文档。

**查询参数**:
- `keyword`: 搜索关键词

**请求示例**:
```
GET /api/knowledge/search?keyword=航班
```

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "60f7b5e9a33d2b4e10b4e800",
      "title": "如何查询航班信息",
      "content": "1. 登录系统\n2. 点击航班查询\n3. 输入出发地和目的地\n4. 选择日期\n5. 点击搜索",
      "tags": ["航班", "查询", "使用指南"],
      "createdAt": "2023-11-21T09:30:00",
      "updatedAt": "2023-11-21T09:30:00"
    }
  ]
}
```

### 3. 更新知识文档

- **URL**: `/api/knowledge/{id}`
- **方法**: `PUT`
- **描述**: 更新指定ID的知识文档内容。

**路径参数**:
- `id`: 知识文档ID

**请求参数**:
文档内容字符串

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 4. 删除知识文档

- **URL**: `/api/knowledge/{id}`
- **方法**: `DELETE`
- **描述**: 删除指定ID的知识文档。

**路径参数**:
- `id`: 知识文档ID

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 5. 查询所有知识文档

- **URL**: `/api/knowledge`
- **方法**: `GET`
- **描述**: 获取所有知识文档列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "60f7b5e9a33d2b4e10b4e800",
      "title": "如何查询航班信息",
      "content": "1. 登录系统\n2. 点击航班查询\n3. 输入出发地和目的地\n4. 选择日期\n5. 点击搜索",
      "tags": ["航班", "查询", "使用指南"],
      "createdAt": "2023-11-21T09:30:00",
      "updatedAt": "2023-11-21T09:30:00"
    },
    {
      "id": "60f7b5e9a33d2b4e10b4e801",
      "title": "如何取消订单",
      "content": "1. 登录系统\n2. 进入我的订单\n3. 找到需要取消的订单\n4. 点击取消按钮\n5. 确认取消",
      "tags": ["订单", "取消", "使用指南"],
      "createdAt": "2023-11-21T10:15:00",
      "updatedAt": "2023-11-21T10:15:00"
    }
  ]
}
```

## 仪表盘 API

### 1. 获取仪表盘概览数据

- **URL**: `/api/dashboard/overview`
- **方法**: `GET`
- **描述**: 获取系统仪表盘概览数据。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userCount": 1250,
    "orderCount": 856,
    "flightCount": 120,
    "activeConversations": 15,
    "dailyStats": {
      "newUsers": 25,
      "newOrders": 78,
      "revenue": 45600.00
    },
    "weeklyStats": {
      "userGrowth": 8.5,
      "orderGrowth": 12.3,
      "revenueGrowth": 15.7
    }
  }
}
```

## 管理员用户 API

### 1. 获取用户列表

- **URL**: `/api/admin/user/list`
- **方法**: `GET`
- **描述**: 管理员获取所有用户列表。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "user123",
      "email": "user@example.com",
      "phone": "13800138000",
      "realName": "张三",
      "idCard": "110101199001011234",
      "userType": 1,
      "memberLevel": 2
    },
    {
      "id": 2,
      "username": "admin",
      "email": "admin@example.com",
      "phone": "13900139000",
      "realName": "管理员",
      "idCard": "110101199002022345",
      "userType": 3,
      "memberLevel": null
    }
  ]
}
```

### 2. 新增用户

- **URL**: `/api/admin/user`
- **方法**: `POST`
- **描述**: 管理员添加新用户。

**请求参数**:

```json
{
  "username": "newuser",
  "password": "password123",
  "email": "newuser@example.com",
  "phone": "13800138001",
  "realName": "新用户",
  "idCard": "110101199003033456",
  "userType": 1
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "用户添加成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

### 3. 修改用户信息

- **URL**: `/api/admin/user/{id}`
- **方法**: `PUT`
- **描述**: 管理员修改指定用户信息。

**路径参数**:
- `id`: 用户ID

**请求参数**:

```json
{
  "email": "updated@example.com",
  "phone": "13800138002",
  "realName": "更新用户",
  "idCard": "110101199004044567",
  "userType": 2,
  "password": "newpassword123" // 可选，如果需要修改密码
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "用户信息更新成功",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
``` 