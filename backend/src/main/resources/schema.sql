-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `smartair`;

-- 切换数据库
USE `smartair`;

-- 用户表：user
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(100),
  `phone` VARCHAR(20),
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `id_card` VARCHAR(30) COMMENT '身份证号',
  `user_type` INT NOT NULL DEFAULT 1 COMMENT '用户类型：1-普通用户，2-客服人员，3-系统管理员',
  `member_level` INT DEFAULT 1 COMMENT '会员等级：1-普通会员，2-白银会员，3-黄金会员，4-白金会员，5-钻石会员',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础信息表';

-- 航班信息表：flight
CREATE TABLE IF NOT EXISTS `flight` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `flight_no` VARCHAR(20) NOT NULL COMMENT '航班号',
  `airline` VARCHAR(50) NOT NULL COMMENT '航空公司',
  `departure_city` VARCHAR(50) NOT NULL COMMENT '出发城市',
  `arrival_city` VARCHAR(50) NOT NULL COMMENT '到达城市',
  `scheduled_departure_time` DATETIME NOT NULL COMMENT '计划出发时间',
  `scheduled_arrival_time` DATETIME NOT NULL COMMENT '计划到达时间',
  `aircraft_type` VARCHAR(30) COMMENT '飞机型号',
  `price` DECIMAL(10,2) NOT NULL COMMENT '航班价格',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='航班基础信息表';

-- 航班订单表：flight_order
CREATE TABLE IF NOT EXISTS `flight_order` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单编号',
  `user_id` BIGINT NOT NULL,
  `flight_id` BIGINT NOT NULL,
  `seat_no` VARCHAR(10) COMMENT '座位号',
  `status` INT DEFAULT 1 COMMENT '订单状态：1-已完成(completed)，2-已取消(cancelled)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_flight` FOREIGN KEY (`flight_id`) REFERENCES `flight`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户航班订单记录';

-- 会话索引表：conversation_index
CREATE TABLE IF NOT EXISTS `conversation_index` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `conversation_uuid` VARCHAR(64) NOT NULL UNIQUE,
  `last_message` TEXT,
  `status` INT DEFAULT 1 COMMENT '会话状态：1-活跃(active)、2-已转人工(transferred)、3-已关闭(closed)',
  `service_user_id` BIGINT COMMENT '处理客服ID，转人工时有效',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_conversation_service` FOREIGN KEY (`service_user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话索引信息表';

-- 系统日志表：system_log
CREATE TABLE IF NOT EXISTS `system_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `message` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';
