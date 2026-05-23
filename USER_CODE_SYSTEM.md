# 用户代码管理系统

## 系统架构

### 目录结构
```
D:\Desktop\test\
├── code_editor/                    # 用户代码存储目录
│   └── user_[userId]/              # 用户专属目录
│       ├── lab1/
│       │   ├── trap.c
│       │   ├── timer.c
│       │   └── trap2.c
│       ├── lab2/
│       │   ├── kalloc.c
│       │   └── vm.c
│       ├── lab3/
│       │   └── proc.c
│       ├── lab4/
│       │   ├── fs.c
│       │   ├── bio.c
│       │   └── pipe.c
│       └── lab5/
│           └── sync.c
└── hollow/                         # 代码模板目录
    └── hollow/
        ├── lab1/
        ├── lab2/
        ├── lab3/
        ├── lab4/
        └── lab5/
```

## 核心功能

### 1. 用户注册时自动初始化代码目录

当用户注册成功时，系统会自动：
- 在 `D:\Desktop\test\code_editor\` 下创建 `user_[userId]` 目录
- 从 `D:\Desktop\test\hollow\hollow\` 复制所有 lab 文件夹和 .c 模板文件到用户目录

**实现位置**: `UserServiceImpl.register()`

### 2. 代码保存/运行逻辑

**接口**: `POST /api/submissions/judge`

**流程**:
1. 接收参数: `user_id`, `lab_id`, `file_name`, `code_content`
2. 定位文件: `D:\Desktop\test\code_editor\user_[userId]\lab[x]\[fileName].c`
3. 覆盖写入: 使用原子操作覆盖文件内容
4. 保存路径: 将文件绝对路径存入 `submission.code_content`

**实现位置**: `SubmissionServiceImpl.updateJudgeResult()`

### 3. 代码读取逻辑

**接口**: `GET /api/user-code/load?userId={userId}&labId={labId}&fileName={fileName}`

**流程**:
1. 检查用户代码文件是否存在
   - 存在: 从用户目录读取最新代码
   - 不存在: 从模板目录读取空白代码
2. 返回代码内容给前端

**实现位置**: `UserCodeServiceImpl.loadUserCode()`

## API 接口

### UserCodeController

#### 1. 加载用户代码
```
GET /api/user-code/load
参数: userId, labId, fileName
返回: { success, codeContent, filePath, exists }
```

#### 2. 保存用户代码
```
POST /api/user-code/save
参数: userId, labId, fileName
Body: codeContent (String)
返回: { success, message, filePath }
```

#### 3. 获取文件路径
```
GET /api/user-code/path
参数: userId, labId, fileName
返回: { success, userFilePath, templateFilePath, exists }
```

#### 4. 初始化用户目录
```
POST /api/user-code/initialize/{userId}
返回: { success, message }
```

## 配置文件

**application.yml**:
```yaml
code-editor:
  base-path: D:/Desktop/test/code_editor
  template-path: D:/Desktop/test/hollow/hollow
  user-folder-prefix: user_
```

## 技术特性

### 高并发安全
- 使用 `synchronized` 锁保护文件写入操作
- 采用原子文件移动操作确保数据一致性
- 使用临时文件 + 原子移动模式防止写入失败导致数据损坏

### 健壮性
- 完整的参数校验和异常处理
- 自动创建不存在的目录
- 支持原子移动和非原子移动两种模式
- 详细的日志记录

### NIO 文件操作
- 使用 `java.nio.file.Files` 进行文件操作
- 使用 `StandardCharsets.UTF_8` 确保编码正确
- 使用 `StandardCopyOption.ATOMIC_MOVE` 实现原子操作

## 使用示例

### 前端调用示例

```javascript
// 加载代码
async function loadCode(userId, labId, fileName) {
  const response = await fetch(
    `/api/user-code/load?userId=${userId}&labId=${labId}&fileName=${fileName}`
  );
  return await response.json();
}

// 保存代码
async function saveCode(userId, labId, fileName, codeContent) {
  const response = await fetch(
    `/api/user-code/save?userId=${userId}&labId=${labId}&fileName=${fileName}`,
    {
      method: 'POST',
      body: codeContent,
      headers: { 'Content-Type': 'text/plain' }
    }
  );
  return await response.json();
}
```

## 注意事项

1. **文件路径格式**: 统一使用正斜杠 `/` 存储到数据库
2. **文件覆盖**: 每次保存都会完全覆盖之前的内容
3. **模板优先**: 用户首次访问时自动加载模板代码
4. **目录初始化**: 用户注册时自动完成，无需手动操作
5. **并发控制**: 同一文件的并发写入会被串行化处理

## 系统状态

- 后端服务器: http://localhost:8080/
- 前端服务器: http://localhost:5174/
- 数据库: MySQL (localhost:3306/os)