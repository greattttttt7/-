/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80300
 Source Host           : localhost:3306
 Source Schema         : os

 Target Server Type    : MySQL
 Target Server Version : 80300
 File Encoding         : 65001

 Date: 20/05/2026 15:08:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for document
-- ----------------------------
DROP TABLE IF EXISTS `document`;
CREATE TABLE `document`  (
  `doc_id` int NOT NULL AUTO_INCREMENT COMMENT '文档唯一ID',
  `lab_id` int NOT NULL COMMENT '关联实验ID',
  `doc_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档标题',
  `doc_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '指导书内容',
  `doc_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档存储路径',
  `doc_task` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `doc_principle` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `doc_target` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`doc_id`) USING BTREE,
  INDEX `lab_id`(`lab_id` ASC) USING BTREE,
  CONSTRAINT `document_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `lab` (`lab_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of document
-- ----------------------------
INSERT INTO `document` VALUES (1, 1, '中断处理实验指导书', '本实验包含四个主要任务，分别涉及系统调用处理、定时器初始化、时钟中断设置和中断处理逻辑。', 'docs/lab1_guide.pdf', '1. 实现系统调用处理\n2. 实现定时器中断初始化\n3. 实现设置下一次时钟中断\n4. 实现时钟中断处理', 'RISC-V架构使用scause寄存器标识中断/异常类型，最高位为1表示中断，为0表示异常。时钟中断属于Supervisor级中断，需要通过SIE_STIE位启用。', '理解中断处理流程，掌握系统调用和时钟中断的实现方法');
INSERT INTO `document` VALUES (2, 2, '内存管理实验指导书', '本实验包含五个主要任务，分别涉及物理页面分配、物理页面释放、页表遍历、页表映射创建和页表递归释放。', 'docs/lab2_guide.pdf', '1. 实现物理页面分配（kalloc）\n2. 实现物理页面释放（kfree）\n3. 实现页表遍历（walk）\n4. 实现页表映射创建（mappages）\n5. 实现页表递归释放（freewalk）', 'RISC-V Sv39 采用三级页表结构，虚拟地址分为 9+9+9+12 位。物理页面通过链表管理，页表项（PTE）包含物理地址和权限标志。', '理解内存管理核心机制，掌握物理页面分配和页表操作的实现方法');
INSERT INTO `document` VALUES (3, 3, '进程调度实验指导书', '本实验包含五个主要任务，分别涉及调度器主循环、进程让出CPU、进程创建(fork)、等待子进程(wait)和进程退出(exit)。', 'docs/lab3_guide.pdf', '1. 实现调度器主循环（scheduler）\n2. 实现让出CPU（yield）\n3. 实现进程创建（fork）\n4. 实现等待子进程（wait）\n5. 实现进程退出（exit）', '进程调度是操作系统的核心功能，负责决定哪个进程获得CPU执行。uCore使用队列管理就绪进程，通过上下文切换实现进程切换。', '理解进程调度机制，掌握调度器、fork、wait、exit等核心系统调用的实现方法');
INSERT INTO `document` VALUES (4, 4, '文件系统实验指导书', '本实验包含八个主要任务，涉及磁盘块分配与释放、块映射、inode读写、目录操作、缓冲区管理和管道通信。', 'docs/lab4_guide.pdf', '1. 实现磁盘块分配（balloc）\n2. 实现磁盘块释放（bfree）\n3. 实现块映射（bmap）\n4. 实现inode数据读取（readi）\n5. 实现inode数据写入（writei）\n6. 实现目录查找（dirlookup）\n7. 实现目录链接（dirlink）\n8. 实现缓冲区获取（bget）\n9. 实现管道分配（pipealloc）\n10. 实现管道读写（pipewrite/piperead）', '文件系统是操作系统负责管理和存储文件数据的组件。本实验涵盖：位图管理磁盘块、inode索引节点、目录结构、缓冲区缓存LRU策略、管道IPC机制。', '理解文件系统底层实现，掌握磁盘块管理、inode操作、目录管理、缓冲区缓存和管道通信的实现方法');
INSERT INTO `document` VALUES (5, 5, '同步互斥实验指导书', '本实验旨在帮助学生理解操作系统中同步互斥机制的实现原理，包括互斥锁、信号量和条件变量的基本概念和实现方法。', 'documents/lab5_guide.md', '1. 实现互斥锁的加锁和解锁操作\n2. 实现信号量的P/V操作\n3. 实现条件变量的等待和通知机制', '同步互斥是操作系统中保证并发程序正确执行的重要机制。互斥锁用于保护临界区，信号量用于资源计数，条件变量用于线程间的条件同步。', '掌握同步互斥机制的基本原理和实现方法，理解自旋锁和阻塞锁的区别，学会使用信号量和条件变量解决并发问题。');

-- ----------------------------
-- Table structure for lab
-- ----------------------------
DROP TABLE IF EXISTS `lab`;
CREATE TABLE `lab`  (
  `lab_id` int NOT NULL AUTO_INCREMENT COMMENT '实验唯一ID',
  `lab_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实验编号（如Lab1）',
  `lab_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实验名称',
  `lab_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '实验简介',
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '难度：简单/中等/困难',
  `estimated_time` int NULL DEFAULT NULL COMMENT '预计耗时（分钟）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实验分类：基础实验/综合实验',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`lab_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lab
-- ----------------------------
INSERT INTO `lab` VALUES (1, 'Lab1', '中断处理实验', '本实验旨在帮助学生理解RISC-V架构下的中断处理机制，包括系统调用处理、时钟中断初始化、定时器设置和中断处理流程。', '中等', 180, '基础实验', '2026-05-19 10:17:28');
INSERT INTO `lab` VALUES (2, 'Lab2', '内存管理实验', '本实验旨在帮助学生理解操作系统内存管理机制，包括物理页面分配与释放、页表遍历、页表映射创建和页表递归释放。', '中等', 240, '基础实验', '2026-05-19 10:38:52');
INSERT INTO `lab` VALUES (3, 'Lab3', '进程调度实验', '本实验旨在帮助学生理解操作系统进程调度机制，包括调度器主循环、进程让出CPU、进程创建(fork)、等待子进程(wait)和进程退出(exit)。', '中等', 240, '基础实验', '2026-05-19 10:57:27');
INSERT INTO `lab` VALUES (4, 'Lab4', '文件系统实验', '本实验旨在帮助学生理解操作系统文件系统的实现机制，包括磁盘块管理、inode操作、目录管理、缓冲区缓存和管道通信。', '中等', 300, '基础实验', '2026-05-19 14:31:48');
INSERT INTO `lab` VALUES (5, 'Lab5', '同步互斥实验', '学习操作系统中同步互斥机制的实现，包括互斥锁、信号量和条件变量。', '中等', 120, '基础实验', '2026-05-19 15:25:49');

-- ----------------------------
-- Table structure for lab_progress
-- ----------------------------
DROP TABLE IF EXISTS `lab_progress`;
CREATE TABLE `lab_progress`  (
  `progress_id` int NOT NULL AUTO_INCREMENT COMMENT '进度记录ID',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `lab_id` int NOT NULL COMMENT '关联实验ID',
  `status` enum('未开始','进行中','已完成') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '未开始' COMMENT '实验状态',
  `score` float NULL DEFAULT NULL COMMENT '实验成绩',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`progress_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `lab_id` ASC) USING BTREE COMMENT '用户-实验唯一进度',
  INDEX `lab_id`(`lab_id` ASC) USING BTREE,
  CONSTRAINT `lab_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `lab_progress_ibfk_2` FOREIGN KEY (`lab_id`) REFERENCES `lab` (`lab_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lab_progress
-- ----------------------------

-- ----------------------------
-- Table structure for submission
-- ----------------------------
DROP TABLE IF EXISTS `submission`;
CREATE TABLE `submission`  (
  `sub_id` int NOT NULL AUTO_INCREMENT COMMENT '提交记录ID',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `lab_id` int NOT NULL COMMENT '关联实验ID',
  `code_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提交的代码内容',
  `commit_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Git提交哈希值',
  `run_result` enum('Pass','Fail') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '运行结果：通过/失败',
  `run_time` float NULL DEFAULT NULL COMMENT '运行耗时（秒）',
  `pass_count` int NULL DEFAULT NULL COMMENT '测试通过用例数',
  `fail_count` int NULL DEFAULT NULL COMMENT '测试失败用例数',
  `total_count` int NULL DEFAULT NULL COMMENT '测试总用例数',
  `result_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '测试详细结果',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`sub_id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `lab_id`(`lab_id` ASC) USING BTREE,
  CONSTRAINT `submission_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `submission_ibfk_2` FOREIGN KEY (`lab_id`) REFERENCES `lab` (`lab_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submission
-- ----------------------------

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`  (
  `task_id` int NOT NULL AUTO_INCREMENT COMMENT '任务唯一ID',
  `lab_id` int NOT NULL COMMENT '关联实验ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `file_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标代码文件路径',
  `answer_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '正确答案文件路径',
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `lab_id`(`lab_id` ASC) USING BTREE,
  CONSTRAINT `task_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `lab` (`lab_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO `task` VALUES (1, 1, '系统调用处理', 'D:\\Desktop\\test\\hollow\\hollow\\lab1\\trap.c', 'D:\\Desktop\\test\\answer\\answer\\lab1\\trap.c');
INSERT INTO `task` VALUES (2, 1, '定时器初始化、设置下一次时钟中断', 'D:\\Desktop\\test\\hollow\\hollow\\lab1\\timer.c', 'D:\\Desktop\\test\\answer\\answer\\lab1\\timer.c');
INSERT INTO `task` VALUES (4, 1, '时钟中断处理', 'D:\\Desktop\\test\\hollow\\hollow\\lab1\\trap2.c', 'D:\\Desktop\\test\\answer\\answer\\lab1\\trap2.c');
INSERT INTO `task` VALUES (5, 2, '物理页面释放、物理页面分配', 'D:\\Desktop\\test\\hollow\\hollow\\lab2\\kalloc.c', 'D:\\Desktop\\test\\answer\\answer\\lab2\\kalloc.c');
INSERT INTO `task` VALUES (7, 2, '页表遍历、页表映射创建、页表递归释放', 'D:\\Desktop\\test\\hollow\\hollow\\lab2\\vm.c', 'D:\\Desktop\\test\\answer\\answer\\lab2\\vm.c');
INSERT INTO `task` VALUES (10, 3, '调度器主循环、让出CPU、进程创建(fork)、等待子进程(wait)、进程退出(exit)', 'D:\\Desktop\\test\\hollow\\hollow\\lab3\\proc.c', 'D:\\Desktop\\test\\answer\\answer\\lab3\\proc.c');
INSERT INTO `task` VALUES (15, 4, '磁盘块分配、磁盘块释放、块映射、inode数据读取、inode数据写入、目录查找、目录链接', 'D:\\Desktop\\test\\hollow\\hollow\\lab4\\fs.c', 'D:\\Desktop\\test\\answer\\answer\\lab4\\fs.c');
INSERT INTO `task` VALUES (22, 4, '缓冲区获取', 'D:\\Desktop\\test\\hollow\\hollow\\lab4\\bio.c', 'D:\\Desktop\\test\\answer\\answer\\lab4\\bio.c');
INSERT INTO `task` VALUES (23, 4, '管道分配、管道写、管道读', 'D:\\Desktop\\test\\hollow\\hollow\\lab4\\pipe.c', 'D:\\Desktop\\test\\answer\\answer\\lab4\\pipe.c');
INSERT INTO `task` VALUES (26, 5, '实现互斥锁加锁、实现互斥锁解锁、实现信号量V操作、实现信号量P操作、实现条件变量通知、实现条件变量等待', 'D:\\Desktop\\test\\hollow\\hollow\\lab5\\sync.c', 'D:\\Desktop\\test\\answer\\answer\\lab5\\sync.c');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码（加密存储）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'aaa', '123', NULL, '2026-05-19 16:14:36');

SET FOREIGN_KEY_CHECKS = 1;
