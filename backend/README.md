# 操作系统实验平台后端

## 运行方式
1. 创建 MySQL 数据库 `os`
2. 执行 `src/main/resources/db/init.sql`
3. 修改 `src/main/resources/application.yml` 中的数据库账号密码
4. 进入 `backend` 目录执行：

```bash
mvn spring-boot:run
```

## 接口示例
- `POST /api/auth/login`
- `GET /api/labs`
- `GET /api/labs/{labId}`
- `GET /api/documents/lab/{labId}`
- `GET /api/progress/{userId}`
- `GET /api/submissions?userId=1&labId=1`
- `POST /api/submissions`
