-- 基于大模型的个性化资源生成与学习多智能体系统 - 数据库脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS ai_study DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_study;

-- 1. 学生基础表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    age INT DEFAULT NULL COMMENT '年龄',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
    gender TINYINT DEFAULT NULL COMMENT '性别 0女 1男 2保密',
    major VARCHAR(64) DEFAULT NULL COMMENT '大学专业',
    school VARCHAR(128) DEFAULT NULL COMMENT '学校',
    grade VARCHAR(16) DEFAULT NULL COMMENT '年级',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生基础表';

-- 2. 学习画像表
CREATE TABLE IF NOT EXISTS student_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '画像ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    knowledge_base TEXT COMMENT '知识基础',
    cognitive_style VARCHAR(255) DEFAULT NULL COMMENT '认知风格',
    error_prone_points TEXT COMMENT '易错点偏好',
    learning_goals TEXT COMMENT '学习目标',
    major_direction VARCHAR(64) DEFAULT NULL COMMENT '专业方向',
    learning_pace VARCHAR(255) DEFAULT NULL COMMENT '学习节奏',
    resource_preference VARCHAR(255) DEFAULT NULL COMMENT '资源偏好',
    weak_points TEXT COMMENT '薄弱知识点',
    profile_json LONGTEXT COMMENT '完整画像JSON',
    version INT DEFAULT 1 COMMENT '画像版本',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习画像表';

-- 3. 画像维度明细表
CREATE TABLE IF NOT EXISTS profile_dimension (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维度ID',
    profile_id BIGINT NOT NULL COMMENT '画像ID',
    dimension_name VARCHAR(32) NOT NULL COMMENT '维度名称',
    dimension_value TEXT COMMENT '维度值',
    confidence_score DECIMAL(3,2) DEFAULT 0.80 COMMENT '置信度 0-1',
    source VARCHAR(32) DEFAULT 'chat' COMMENT '来源 chat/assessment/behavior',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_profile_id (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像维度明细表';

-- 4. 学习资源表
CREATE TABLE IF NOT EXISTS learning_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型 document/mindmap/exercise/codecase/reading/video',
    title VARCHAR(255) NOT NULL COMMENT '资源标题',
    content LONGTEXT COMMENT '资源内容',
    content_url VARCHAR(500) DEFAULT NULL COMMENT '资源链接',
    tags VARCHAR(255) DEFAULT NULL COMMENT '标签',
    difficulty_level VARCHAR(16) DEFAULT NULL COMMENT '难度 easy/medium/hard',
    subject VARCHAR(64) DEFAULT NULL COMMENT '学科',
    knowledge_points VARCHAR(500) DEFAULT NULL COMMENT '关联知识点',
    ai_model VARCHAR(32) DEFAULT 'deepseek-chat' COMMENT '生成模型',
    generation_params JSON DEFAULT NULL COMMENT '生成参数',
    status VARCHAR(16) DEFAULT 'generated' COMMENT '状态 generated/approved/rejected',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_student_id (student_id),
    INDEX idx_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资源表';

-- 5. 学习路径表
CREATE TABLE IF NOT EXISTS learning_path (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '路径ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    path_name VARCHAR(128) NOT NULL COMMENT '路径名称',
    description TEXT COMMENT '路径描述',
    target_goal TEXT COMMENT '目标',
    estimated_duration INT DEFAULT NULL COMMENT '预计时长(小时)',
    status VARCHAR(16) DEFAULT 'active' COMMENT '状态 active/paused/completed',
    progress_percent INT DEFAULT 0 COMMENT '进度百分比',
    ai_plan_json LONGTEXT COMMENT 'AI规划JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径表';

-- 6. 路径步骤表
CREATE TABLE IF NOT EXISTS path_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '步骤ID',
    path_id BIGINT NOT NULL COMMENT '路径ID',
    step_order INT NOT NULL COMMENT '步骤顺序',
    title VARCHAR(255) NOT NULL COMMENT '步骤标题',
    description TEXT COMMENT '步骤描述',
    resource_id BIGINT DEFAULT NULL COMMENT '关联资源ID',
    step_type VARCHAR(32) DEFAULT 'study' COMMENT '类型 study/exercise/review/assessment',
    status VARCHAR(16) DEFAULT 'pending' COMMENT '状态 pending/in_progress/completed',
    duration_minutes INT DEFAULT NULL COMMENT '预计时长(分钟)',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_path_id (path_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路径步骤表';

-- 7. 测验题目表
CREATE TABLE IF NOT EXISTS quiz_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    question_type VARCHAR(32) NOT NULL COMMENT '题型 single_choice/multiple_choice/fill_blank/essay/code',
    subject VARCHAR(64) DEFAULT NULL COMMENT '学科',
    knowledge_point VARCHAR(128) DEFAULT NULL COMMENT '知识点',
    difficulty VARCHAR(16) DEFAULT 'medium' COMMENT '难度',
    question_text TEXT NOT NULL COMMENT '题目内容',
    options JSON DEFAULT NULL COMMENT '选项JSON',
    correct_answer TEXT COMMENT '正确答案',
    explanation TEXT COMMENT '解析',
    ai_generated TINYINT DEFAULT 1 COMMENT '是否AI生成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测验题目表';

-- 8. 测验记录表
CREATE TABLE IF NOT EXISTS quiz_attempt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    assessment_id BIGINT DEFAULT NULL COMMENT '评估ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    student_answer TEXT COMMENT '学生答案',
    is_correct TINYINT DEFAULT NULL COMMENT '是否正确 0错 1对',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '得分',
    ai_feedback TEXT COMMENT 'AI反馈',
    time_spent_seconds INT DEFAULT NULL COMMENT '耗时(秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测验记录表';

-- 9. 学习行为表
CREATE TABLE IF NOT EXISTS learning_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行为ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    behavior_type VARCHAR(32) NOT NULL COMMENT '行为类型 view_resource/complete_step/ask_question/submit_quiz',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    target_type VARCHAR(32) DEFAULT NULL COMMENT '目标类型',
    duration_seconds INT DEFAULT NULL COMMENT '时长(秒)',
    detail JSON DEFAULT NULL COMMENT '详情',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习行为表';

-- 10. 对话历史表
CREATE TABLE IF NOT EXISTS conversation_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '对话ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    message_role VARCHAR(16) NOT NULL COMMENT '角色 system/user/assistant',
    message_content TEXT NOT NULL COMMENT '消息内容',
    agent_type VARCHAR(32) DEFAULT 'tutor' COMMENT '智能体类型 profile/resource/path/tutor/assessment',
    tokens_used INT DEFAULT NULL COMMENT '消耗token数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_id (student_id),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';

-- 11. 评估结果表
CREATE TABLE IF NOT EXISTS assessment_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评估ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    assessment_type VARCHAR(32) NOT NULL COMMENT '评估类型 diagnostic/progress/final',
    total_score DECIMAL(5,2) DEFAULT NULL COMMENT '总分',
    max_score DECIMAL(5,2) DEFAULT NULL COMMENT '满分',
    score_percent INT DEFAULT NULL COMMENT '得分百分比',
    mastery_level VARCHAR(16) DEFAULT NULL COMMENT '掌握程度 beginner/intermediate/advanced/expert',
    weak_areas TEXT COMMENT '薄弱领域',
    strong_areas TEXT COMMENT '优势领域',
    ai_analysis TEXT COMMENT 'AI分析',
    improvement_suggestions TEXT COMMENT '改进建议',
    status VARCHAR(16) DEFAULT 'completed' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估结果表';

-- 12. 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(64) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化数据
INSERT INTO student (username, password, nickname, email) VALUES
('admin', '123456', '管理员', 'admin@studyai.com'),
('student1', '123456', '学生一', 's1@studyai.com');

-- 如果 student 表已存在，请执行以下 ALTER TABLE 语句添加新字段
ALTER TABLE student ADD COLUMN age INT DEFAULT NULL COMMENT '年龄' AFTER avatar;
ALTER TABLE student ADD COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '联系方式' AFTER age;
ALTER TABLE student ADD COLUMN gender TINYINT DEFAULT NULL COMMENT '性别 0女 1男 2保密' AFTER phone;
ALTER TABLE student ADD COLUMN major VARCHAR(64) DEFAULT NULL COMMENT '大学专业' AFTER gender;
ALTER TABLE student ADD COLUMN school VARCHAR(128) DEFAULT NULL COMMENT '学校' AFTER major;
ALTER TABLE student ADD COLUMN grade VARCHAR(16) DEFAULT NULL COMMENT '年级' AFTER school;

-- 13. 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识库ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    name VARCHAR(128) NOT NULL COMMENT '知识库名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '知识库描述',
    content LONGTEXT NOT NULL COMMENT '知识库文本内容',
    file_name VARCHAR(255) DEFAULT NULL COMMENT '原始上传文件名',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';


