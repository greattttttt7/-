-- 操作系统实验平台数据库初始化脚本

CREATE DATABASE IF NOT EXISTS os DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE os;

DROP TABLE IF EXISTS submission;
DROP TABLE IF EXISTS lab_progress;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS document;
DROP TABLE IF EXISTS lab;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password VARCHAR(100) NOT NULL COMMENT '登录密码（加密存储）',
    name VARCHAR(50) COMMENT '学生姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE lab (
    lab_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '实验唯一ID',
    lab_code VARCHAR(20) NOT NULL COMMENT '实验编号（如Lab1）',
    lab_name VARCHAR(100) NOT NULL COMMENT '实验名称',
    lab_desc TEXT COMMENT '实验简介',
    difficulty VARCHAR(20) COMMENT '难度：简单/中等/困难',
    estimated_time INT COMMENT '预计耗时（分钟）',
    category VARCHAR(50) COMMENT '实验分类：基础实验/综合实验',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验表';

CREATE TABLE document (
    doc_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '文档唯一ID',
    lab_id INT NOT NULL COMMENT '关联实验ID',
    doc_title VARCHAR(100) COMMENT '文档标题',
    doc_content TEXT COMMENT '实验内容',
    doc_task TEXT COMMENT '实验任务',
    doc_principle TEXT COMMENT '实验原理',
    doc_target TEXT COMMENT '实验目的',
    doc_path VARCHAR(200) COMMENT '文档存储路径',
    CONSTRAINT fk_document_lab FOREIGN KEY (lab_id) REFERENCES lab(lab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验文档表';

CREATE TABLE task (
    task_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '任务唯一ID',
    lab_id INT NOT NULL COMMENT '关联实验ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    file_path VARCHAR(200) COMMENT '目标代码文件绝对路径',
    answer_path VARCHAR(200) COMMENT '标准答案文件绝对路径',
    line_start INT COMMENT '任务起始行号',
    line_end INT COMMENT '任务结束行号',
    todo_content TEXT COMMENT '填空任务提示内容',
    CONSTRAINT fk_task_lab FOREIGN KEY (lab_id) REFERENCES lab(lab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验任务表';

CREATE TABLE lab_progress (
    progress_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '进度记录ID',
    user_id INT NOT NULL COMMENT '关联用户ID',
    lab_id INT NOT NULL COMMENT '关联实验ID',
    status ENUM('未开始','进行中','已完成') DEFAULT '未开始' COMMENT '实验状态',
    score FLOAT COMMENT '实验成绩',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_lab (user_id, lab_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_progress_lab FOREIGN KEY (lab_id) REFERENCES lab(lab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验进度表';

CREATE TABLE submission (
    sub_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '提交记录ID',
    user_id INT NOT NULL COMMENT '关联用户ID',
    lab_id INT NOT NULL COMMENT '关联实验ID',
    code_content TEXT COMMENT '提交的代码文件路径',
    commit_hash VARCHAR(64) COMMENT 'Git提交哈希值',
    run_result ENUM('Pass','Fail') NOT NULL COMMENT '运行结果：通过/失败',
    run_time FLOAT COMMENT '运行耗时（秒）',
    pass_count INT COMMENT '测试通过用例数',
    fail_count INT COMMENT '测试失败用例数',
    total_count INT COMMENT '测试总用例数',
    result_detail TEXT COMMENT '测试详细结果',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    CONSTRAINT fk_submission_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_submission_lab FOREIGN KEY (lab_id) REFERENCES lab(lab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码提交与测试结果表';

INSERT INTO user (username, password, name) VALUES
('student1', 'e10adc3949ba59abbe56e057f20f883e', '张三');

INSERT INTO lab (lab_code, lab_name, lab_desc, difficulty, estimated_time, category) VALUES
('Lab1', '进程管理与调度', '理解进程创建、状态转换和调度队列。', '中等', 120, '基础实验'),
('Lab2', '内存分配算法', '理解空闲区管理与内存回收。', '中等', 120, '基础实验'),
('Lab3', '虚拟存储管理', '理解页表、缺页中断和页面置换。', '进阶', 180, '基础实验'),
('Lab4', '文件系统设计', '理解目录树、文件分配与元数据管理。', '进阶', 180, '基础实验'),
('Lab5', '设备驱动程序', '理解驱动程序、中断与 I/O 请求队列。', '进阶', 180, '基础实验');

INSERT INTO document (lab_id, doc_title, doc_content, doc_task, doc_principle, doc_target, doc_path) VALUES
(1, 'Lab1 指导书', 'Lab1 实验内容', 'Lab1 实验任务', 'Lab1 实验原理', 'Lab1 实验目的', '/docs/lab1.md'),
(2, 'Lab2 指导书', 'Lab2 实验内容', 'Lab2 实验任务', 'Lab2 实验原理', 'Lab2 实验目的', '/docs/lab2.md'),
(3, 'Lab3 指导书', 'Lab3 实验内容', 'Lab3 实验任务', 'Lab3 实验原理', 'Lab3 实验目的', '/docs/lab3.md'),
(4, 'Lab4 指导书', 'Lab4 实验内容', 'Lab4 实验任务', 'Lab4 实验原理', 'Lab4 实验目的', '/docs/lab4.md'),
(5, 'Lab5 指导书', 'Lab5 实验内容', 'Lab5 实验任务', 'Lab5 实验原理', 'Lab5 实验目的', '/docs/lab5.md');

INSERT INTO task (lab_id, task_name, file_path, answer_path, line_start, line_end, todo_content) VALUES
(1, 'Lab1 任务1', 'D:/Desktop/test/code_editor/lab1/task1.py', 'D:/Desktop/test/code_editor/lab1/task1.answer.py', 1, 20, '完成进程调度逻辑'),
(1, 'Lab1 任务2', 'D:/Desktop/test/code_editor/lab1/task2.py', 'D:/Desktop/test/code_editor/lab1/task2.answer.py', 1, 20, '完成进程状态切换逻辑'),
(2, 'Lab2 任务1', 'D:/Desktop/test/code_editor/lab2/task1.py', 'D:/Desktop/test/code_editor/lab2/task1.answer.py', 1, 20, '完成内存分配逻辑'),
(2, 'Lab2 任务2', 'D:/Desktop/test/code_editor/lab2/task2.py', 'D:/Desktop/test/code_editor/lab2/task2.answer.py', 1, 20, '完成空闲块合并逻辑'),
(3, 'Lab3 任务1', 'D:/Desktop/test/code_editor/lab3/task1.py', 'D:/Desktop/test/code_editor/lab3/task1.answer.py', 1, 20, '完成页面置换逻辑'),
(3, 'Lab3 任务2', 'D:/Desktop/test/code_editor/lab3/task2.py', 'D:/Desktop/test/code_editor/lab3/task2.answer.py', 1, 20, '完成缺页处理逻辑'),
(4, 'Lab4 任务1', 'D:/Desktop/test/code_editor/lab4/task1.py', 'D:/Desktop/test/code_editor/lab4/task1.answer.py', 1, 20, '完成文件系统逻辑'),
(4, 'Lab4 任务2', 'D:/Desktop/test/code_editor/lab4/task2.py', 'D:/Desktop/test/code_editor/lab4/task2.answer.py', 1, 20, '完成目录管理逻辑'),
(5, 'Lab5 任务1', 'D:/Desktop/test/code_editor/lab5/task1.py', 'D:/Desktop/test/code_editor/lab5/task1.answer.py', 1, 20, '完成驱动请求处理逻辑'),
(5, 'Lab5 任务2', 'D:/Desktop/test/code_editor/lab5/task2.py', 'D:/Desktop/test/code_editor/lab5/task2.answer.py', 1, 20, '完成中断回调逻辑');

INSERT INTO lab_progress (user_id, lab_id, status, score) VALUES
(1, 1, '已完成', 95),
(1, 2, '已完成', 90),
(1, 3, '进行中', 0),
(1, 4, '未开始', 0),
(1, 5, '未开始', 0);

INSERT INTO submission (user_id, lab_id, code_content, commit_hash, run_result, run_time, pass_count, fail_count, total_count, result_detail) VALUES
(1, 1, 'D:/Desktop/test/code_editor/lab1/task1.py', 'demo-lab1', 'Pass', 0.02, 1, 0, 1, '自动测试通过'),
(1, 2, 'D:/Desktop/test/code_editor/lab2/task1.py', 'demo-lab2', 'Pass', 0.03, 1, 0, 1, '自动测试通过');
