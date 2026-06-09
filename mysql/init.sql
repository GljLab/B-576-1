-- 面试评分系统数据库初始化脚本
-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS interview_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE interview_system;

-- =============================================
-- 1. 系统用户表
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，EXAMINER-考官，STAFF-工作人员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    avatar VARCHAR(255) COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- =============================================
-- 2. 面试项目表
-- =============================================
DROP TABLE IF EXISTS interview_project;
CREATE TABLE interview_project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    project_code VARCHAR(50) UNIQUE COMMENT '项目编码',
    organizer VARCHAR(200) COMMENT '组织单位',
    interview_date DATE COMMENT '面试日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    location VARCHAR(200) COMMENT '面试地点',
    description TEXT COMMENT '项目描述',
    written_weight DECIMAL(5,2) DEFAULT 50.00 COMMENT '笔试成绩权重(%)',
    interview_weight DECIMAL(5,2) DEFAULT 50.00 COMMENT '面试成绩权重(%)',
    remove_highest TINYINT DEFAULT 1 COMMENT '是否去掉最高分：0-否，1-是',
    remove_lowest TINYINT DEFAULT 1 COMMENT '是否去掉最低分：0-否，1-是',
    score_precision TINYINT DEFAULT 2 COMMENT '成绩精度（小数位数）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-准备中，1-抽签中，2-面试中，3-已结束',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_code (project_code),
    INDEX idx_status (status),
    INDEX idx_interview_date (interview_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试项目表';

-- =============================================
-- 3. 职位表
-- =============================================
DROP TABLE IF EXISTS position;
CREATE TABLE position (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '职位ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    position_name VARCHAR(200) NOT NULL COMMENT '职位名称',
    position_code VARCHAR(50) COMMENT '职位编码',
    department VARCHAR(200) COMMENT '招聘部门',
    recruit_num INT DEFAULT 1 COMMENT '招聘人数',
    requirements TEXT COMMENT '任职要求',
    interview_order INT COMMENT '面试顺序（抽签后）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_position_code (position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位表';

-- =============================================
-- 4. 考场表
-- =============================================
DROP TABLE IF EXISTS exam_room;
CREATE TABLE exam_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考场ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    room_name VARCHAR(100) NOT NULL COMMENT '考场名称',
    room_code VARCHAR(50) COMMENT '考场编码',
    location VARCHAR(200) COMMENT '考场位置',
    capacity INT DEFAULT 20 COMMENT '容纳人数',
    examiner_count INT DEFAULT 7 COMMENT '考官人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-面试中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_room_code (room_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考场表';

-- =============================================
-- 5. 考官表
-- =============================================
DROP TABLE IF EXISTS examiner;
CREATE TABLE examiner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考官ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    user_id BIGINT COMMENT '关联用户ID',
    examiner_name VARCHAR(50) NOT NULL COMMENT '考官姓名',
    examiner_code VARCHAR(50) COMMENT '考官编号',
    phone VARCHAR(20) COMMENT '联系电话',
    organization VARCHAR(200) COMMENT '所属单位',
    title VARCHAR(100) COMMENT '职称/职务',
    expertise VARCHAR(500) COMMENT '专业领域',
    room_id BIGINT COMMENT '分配考场ID（抽签后）',
    seat_no INT COMMENT '座位号（抽签后）',
    is_chief TINYINT DEFAULT 0 COMMENT '是否主考官：0-否，1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_room_id (room_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考官表';

-- =============================================
-- 6. 考生表
-- =============================================
DROP TABLE IF EXISTS candidate;
CREATE TABLE candidate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考生ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    position_id BIGINT COMMENT '报考职位ID',
    candidate_name VARCHAR(50) NOT NULL COMMENT '考生姓名',
    id_card VARCHAR(20) COMMENT '身份证号',
    ticket_no VARCHAR(50) COMMENT '准考证号',
    phone VARCHAR(20) COMMENT '联系电话',
    gender TINYINT COMMENT '性别：0-女，1-男',
    photo VARCHAR(255) COMMENT '照片URL',
    written_score DECIMAL(6,2) COMMENT '笔试成绩',
    interview_order INT COMMENT '面试序号（抽签后）',
    room_id BIGINT COMMENT '分配考场ID',
    check_in_time DATETIME COMMENT '签到时间',
    check_in_status TINYINT DEFAULT 0 COMMENT '签到状态：0-未签到，1-已签到，2-缺考',
    interview_status TINYINT DEFAULT 0 COMMENT '面试状态：0-待面试，1-面试中，2-已完成',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_position_id (position_id),
    INDEX idx_ticket_no (ticket_no),
    INDEX idx_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考生表';

-- =============================================
-- 7. 评分项目表（评分维度）
-- =============================================
DROP TABLE IF EXISTS score_item;
CREATE TABLE score_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分项ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    item_name VARCHAR(100) NOT NULL COMMENT '评分项名称',
    item_code VARCHAR(50) COMMENT '评分项编码',
    max_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '满分值',
    weight DECIMAL(5,2) DEFAULT 100.00 COMMENT '权重(%)',
    description TEXT COMMENT '评分说明',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分项目表';

-- =============================================
-- 8. 评分模板表
-- =============================================
DROP TABLE IF EXISTS score_item_template;
CREATE TABLE score_item_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(50) UNIQUE COMMENT '模板编码',
    description TEXT COMMENT '模板描述',
    item_count INT DEFAULT 0 COMMENT '评分项数量',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统预置：0-否，1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_code (template_code),
    INDEX idx_is_system (is_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分模板表';

-- =============================================
-- 9. 评分模板详情表
-- =============================================
DROP TABLE IF EXISTS score_item_template_detail;
CREATE TABLE score_item_template_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    item_name VARCHAR(100) NOT NULL COMMENT '评分项名称',
    item_code VARCHAR(50) COMMENT '评分项编码',
    max_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '满分值',
    weight DECIMAL(5,2) DEFAULT 0.00 COMMENT '权重(%)',
    description TEXT COMMENT '评分说明',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分模板详情表';

-- =============================================
-- 10. 考官评分表
-- =============================================
DROP TABLE IF EXISTS examiner_score;
CREATE TABLE examiner_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    candidate_id BIGINT NOT NULL COMMENT '考生ID',
    examiner_id BIGINT NOT NULL COMMENT '考官ID',
    room_id BIGINT COMMENT '考场ID',
    score_item_id BIGINT COMMENT '评分项ID（NULL表示汇总记录）',
    score DECIMAL(6,2) COMMENT '分数（该评分项的原始分数）',
    weighted_score DECIMAL(6,2) COMMENT '加权分数',
    total_score DECIMAL(6,2) COMMENT '总分（汇总记录时使用）',
    comment TEXT COMMENT '评语',
    is_valid TINYINT DEFAULT 1 COMMENT '是否有效：0-无效（被去掉的最高/最低分），1-有效',
    submit_time DATETIME COMMENT '提交时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未提交，1-已提交，2-已确认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_candidate_examiner_item (candidate_id, examiner_id, score_item_id),
    INDEX idx_project_id (project_id),
    INDEX idx_candidate_id (candidate_id),
    INDEX idx_examiner_id (examiner_id),
    INDEX idx_score_item_id (score_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考官评分表';

-- =============================================
-- 11. 考生最终成绩表
-- =============================================
DROP TABLE IF EXISTS final_score;
CREATE TABLE final_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    candidate_id BIGINT NOT NULL UNIQUE COMMENT '考生ID',
    position_id BIGINT COMMENT '职位ID',
    written_score DECIMAL(6,2) COMMENT '笔试成绩',
    interview_raw_score DECIMAL(6,2) COMMENT '面试原始分（去掉最高最低后平均）',
    interview_score DECIMAL(6,2) COMMENT '面试成绩（加权后）',
    total_score DECIMAL(6,2) COMMENT '综合总成绩',
    position_rank INT COMMENT '职位排名',
    overall_rank INT COMMENT '总排名',
    is_pass TINYINT DEFAULT 0 COMMENT '是否通过：0-否，1-是',
    publish_status TINYINT DEFAULT 0 COMMENT '发布状态：0-未发布，1-已发布',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_position_id (position_id),
    INDEX idx_total_score (total_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考生最终成绩表';

-- =============================================
-- 12. 抽签记录表
-- =============================================
DROP TABLE IF EXISTS draw_record;
CREATE TABLE draw_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    draw_type VARCHAR(20) NOT NULL COMMENT '抽签类型：EXAMINER-考官抽签，CANDIDATE-考生抽签，POSITION-职位顺序抽签',
    draw_batch VARCHAR(50) COMMENT '抽签批次',
    target_id BIGINT COMMENT '抽签对象ID（考官/考生/职位）',
    target_name VARCHAR(100) COMMENT '抽签对象名称',
    original_info VARCHAR(500) COMMENT '原始信息',
    result_info VARCHAR(500) COMMENT '抽签结果',
    result_order INT COMMENT '结果序号',
    room_id BIGINT COMMENT '分配考场ID',
    draw_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    is_locked TINYINT DEFAULT 1 COMMENT '是否锁定：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_draw_type (draw_type),
    INDEX idx_target_id (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽签记录表';

-- =============================================
-- 13. 操作日志表
-- =============================================
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    project_id BIGINT COMMENT '所属项目ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    module VARCHAR(50) COMMENT '操作模块',
    method VARCHAR(200) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time BIGINT COMMENT '执行时间(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 14. 系统配置表
-- =============================================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    description VARCHAR(500) COMMENT '配置说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入管理员账号（密码：123456，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', '13800138000', 'ADMIN', 1),
('examiner1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '张考官', '13800138001', 'EXAMINER', 1),
('examiner2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '李考官', '13800138002', 'EXAMINER', 1),
('staff1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '王工作人员', '13800138003', 'STAFF', 1);

-- 插入示例面试项目
INSERT INTO interview_project (project_name, project_code, organizer, interview_date, start_time, end_time, location, description, written_weight, interview_weight, status) VALUES
('2026年长沙市公务员录用面试', 'CS2026001', '长沙市人力资源和社会保障局', '2026-03-15', '08:30:00', '17:30:00', '长沙市人才服务中心', '2026年度长沙市公务员录用考试面试环节，采用结构化面试方式。', 50.00, 50.00, 0),
('2026年市直事业单位公开招聘面试', 'CS2026002', '长沙市人事考试院', '2026-04-20', '09:00:00', '17:00:00', '长沙市教育考试院', '市直事业单位公开招聘工作人员面试，包含专业知识测试和综合面试。', 40.00, 60.00, 0);

-- 插入示例职位
INSERT INTO position (project_id, position_name, position_code, department, recruit_num, requirements) VALUES
(1, '综合管理岗', 'ZW001', '市政府办公厅', 2, '本科及以上学历，行政管理、公共管理等相关专业'),
(1, '财务审计岗', 'ZW002', '市审计局', 1, '本科及以上学历，会计、审计、财务管理等相关专业，具有会计从业资格证'),
(1, '法律事务岗', 'ZW003', '市司法局', 2, '本科及以上学历，法学专业，通过国家司法考试'),
(1, '信息技术岗', 'ZW004', '市大数据中心', 3, '本科及以上学历，计算机、软件工程等相关专业'),
(2, '教育管理岗', 'ZW005', '市教育局', 2, '本科及以上学历，教育学相关专业');

-- 插入示例考场
INSERT INTO exam_room (project_id, room_name, room_code, location, capacity, examiner_count) VALUES
(1, '第一考场', 'ROOM001', 'A栋3楼301室', 30, 7),
(1, '第二考场', 'ROOM002', 'A栋3楼302室', 30, 7),
(1, '第三考场', 'ROOM003', 'A栋3楼303室', 30, 7),
(2, '第一考场', 'ROOM004', 'B栋2楼201室', 25, 5);

-- 插入示例考官
INSERT INTO examiner (project_id, examiner_name, examiner_code, phone, organization, title, expertise) VALUES
(1, '王建国', 'KG001', '13900139001', '市人社局', '处长', '人力资源管理'),
(1, '李明华', 'KG002', '13900139002', '市委组织部', '副处长', '组织人事'),
(1, '张红梅', 'KG003', '13900139003', '市政府办', '主任科员', '行政管理'),
(1, '刘志强', 'KG004', '13900139004', '市纪委', '科长', '纪检监察'),
(1, '陈晓东', 'KG005', '13900139005', '湖南大学', '教授', '公共管理'),
(1, '赵丽娟', 'KG006', '13900139006', '中南大学', '副教授', '人才测评'),
(1, '周伟民', 'KG007', '13900139007', '市发改委', '副处长', '经济管理');

-- 插入示例考生（部分考生已签到）
INSERT INTO candidate (project_id, position_id, candidate_name, id_card, ticket_no, phone, gender, written_score, check_in_status, check_in_time) VALUES
(1, 1, '张三', '430102199501011234', '2026001001', '15800158001', 1, 75.50, 1, NOW()),
(1, 1, '李四', '430102199602022345', '2026001002', '15800158002', 1, 72.00, 1, NOW()),
(1, 1, '王五', '430102199703033456', '2026001003', '15800158003', 1, 78.50, 1, NOW()),
(1, 2, '赵六', '430102199504044567', '2026001004', '15800158004', 0, 80.00, 1, NOW()),
(1, 2, '钱七', '430102199605055678', '2026001005', '15800158005', 1, 76.50, 1, NOW()),
(1, 3, '孙八', '430102199706066789', '2026001006', '15800158006', 0, 82.00, 1, NOW()),
(1, 3, '周九', '430102199807077890', '2026001007', '15800158007', 1, 79.00, 1, NOW()),
(1, 3, '吴十', '430102199908088901', '2026001008', '15800158008', 0, 77.50, 0, NULL),
(1, 4, '郑十一', '430102200009099012', '2026001009', '15800158009', 1, 85.00, 0, NULL),
(1, 4, '冯十二', '430102200110100123', '2026001010', '15800158010', 1, 81.50, 0, NULL);

-- 插入评分项目
INSERT INTO score_item (project_id, item_name, item_code, max_score, weight, description, sort_order) VALUES
(1, '综合分析能力', 'ITEM001', 100, 25, '考察考生对问题的分析、归纳、判断能力', 1),
(1, '言语表达能力', 'ITEM002', 100, 20, '考察考生语言表达的准确性、流畅性、逻辑性', 2),
(1, '应变能力', 'ITEM003', 100, 20, '考察考生在压力情境下的反应和处理能力', 3),
(1, '计划组织协调能力', 'ITEM004', 100, 20, '考察考生组织、协调、计划的能力', 4),
(1, '举止仪表', 'ITEM005', 100, 15, '考察考生的外在形象和行为举止', 5);

-- 插入评分模板
INSERT INTO score_item_template (template_name, template_code, description, item_count, is_system, status, create_user_id) VALUES
('通用7项评分模板', 'TPL_7ITEM_GENERAL', '适用于各类结构化面试的通用7项评分体系', 7, 1, 1, 1),
('简化5项评分模板', 'TPL_5ITEM_SIMPLE', '适用于快速面试的简化5项评分体系', 5, 1, 1, 1),
('专业技术岗评分模板', 'TPL_TECHNICAL', '适用于专业技术岗位的6项评分体系', 6, 1, 1, 1),
('管理岗评分模板', 'TPL_MANAGEMENT', '适用于管理岗位的7项评分体系', 7, 1, 1, 1);

-- 插入通用7项评分模板详情
INSERT INTO score_item_template_detail (template_id, item_name, item_code, max_score, weight, description, sort_order) VALUES
(1, '综合分析能力', 'TPL_ANALYSIS', 100, 20, '考察考生对事物的分析、归纳、判断和推理能力', 1),
(1, '言语表达能力', 'TPL_EXPRESSION', 100, 15, '考察考生语言表达的准确性、流畅性、逻辑性和感染力', 2),
(1, '应变能力', 'TPL_ADAPTABILITY', 100, 15, '考察考生在压力和突发情况下的反应能力和情绪稳定性', 3),
(1, '计划组织协调能力', 'TPL_PLANNING', 100, 15, '考察考生对活动的策划、组织、协调和资源调配能力', 4),
(1, '人际交往意识与技巧', 'TPL_COMMUNICATION', 100, 15, '考察考生的人际沟通能力、团队合作意识和处理冲突的能力', 5),
(1, '自我情绪控制', 'TPL_EMOTION', 100, 10, '考察考生在压力情境下的情绪管理和自我控制能力', 6),
(1, '举止仪表', 'TPL_APPEARANCE', 100, 10, '考察考生的外在形象、着装礼仪和行为举止', 7);

-- 插入简化5项评分模板详情
INSERT INTO score_item_template_detail (template_id, item_name, item_code, max_score, weight, description, sort_order) VALUES
(2, '综合分析能力', 'TPL_ANALYSIS', 100, 25, '考察考生对问题的分析、归纳、判断能力', 1),
(2, '言语表达能力', 'TPL_EXPRESSION', 100, 20, '考察考生语言表达的准确性、流畅性、逻辑性', 2),
(2, '应变能力', 'TPL_ADAPTABILITY', 100, 20, '考察考生在压力情境下的反应和处理能力', 3),
(2, '计划组织协调能力', 'TPL_PLANNING', 100, 20, '考察考生组织、协调、计划的能力', 4),
(2, '举止仪表', 'TPL_APPEARANCE', 100, 15, '考察考生的外在形象和行为举止', 5);

-- 插入专业技术岗评分模板详情
INSERT INTO score_item_template_detail (template_id, item_name, item_code, max_score, weight, description, sort_order) VALUES
(3, '专业知识水平', 'TPL_TECH_KNOWLEDGE', 100, 25, '考察考生对本专业基础理论和专业知识的掌握程度', 1),
(3, '专业实践能力', 'TPL_TECH_PRACTICE', 100, 25, '考察考生运用专业知识解决实际问题的能力', 2),
(3, '综合分析能力', 'TPL_ANALYSIS', 100, 15, '考察考生对技术问题的分析和判断能力', 3),
(3, '学习创新能力', 'TPL_INNOVATION', 100, 15, '考察考生的学习能力、创新意识和新技术应用能力', 4),
(3, '言语表达能力', 'TPL_EXPRESSION', 100, 10, '考察考生技术沟通和方案阐述的表达能力', 5),
(3, '举止仪表', 'TPL_APPEARANCE', 100, 10, '考察考生的外在形象和职业素养', 6);

-- 插入管理岗评分模板详情
INSERT INTO score_item_template_detail (template_id, item_name, item_code, max_score, weight, description, sort_order) VALUES
(4, '综合分析能力', 'TPL_ANALYSIS', 100, 18, '考察考生对管理问题的分析、归纳、判断能力', 1),
(4, '计划组织协调能力', 'TPL_PLANNING', 100, 18, '考察考生对工作的策划、组织、协调能力', 2),
(4, '决策能力', 'TPL_DECISION', 100, 15, '考察考生在复杂情况下的决策能力和决策魄力', 3),
(4, '领导与沟通能力', 'TPL_LEADERSHIP', 100, 15, '考察考生的领导力、团队管理和沟通协调能力', 4),
(4, '应变能力', 'TPL_ADAPTABILITY', 100, 12, '考察考生在管理情境中的应变和危机处理能力', 5),
(4, '言语表达能力', 'TPL_EXPRESSION', 100, 12, '考察考生的演讲、汇报和说服能力', 6),
(4, '举止仪表', 'TPL_APPEARANCE', 100, 10, '考察考生的管理者形象和气质风度', 7);

-- 插入系统配置
INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('interview_time_limit', '15', 'INTERVIEW', '单个考生面试时长限制（分钟）'),
('thinking_time', '1', 'INTERVIEW', '考生思考时间（分钟）'),
('answer_time', '14', 'INTERVIEW', '考生答题时间（分钟）'),
('warning_time', '2', 'INTERVIEW', '答题剩余提醒时间（分钟）'),
('score_decimal_places', '2', 'SCORE', '成绩保留小数位数'),
('auto_remove_extreme', '1', 'SCORE', '是否自动去掉最高最低分：0-否，1-是'),
('system_name', '智慧面试评分系统', 'SYSTEM', '系统名称'),
('system_version', 'V1.0.0', 'SYSTEM', '系统版本');

SET FOREIGN_KEY_CHECKS = 1;
