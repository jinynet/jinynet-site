-- =============================================
-- 个人平台项目数据库初始化脚本
-- PostgreSQL DDL
-- =============================================

-- 系统设置表
CREATE TABLE system_settings (
    id INT8 PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE system_settings IS '系统设置表';
COMMENT ON COLUMN system_settings.id IS '主键ID';
COMMENT ON COLUMN system_settings.setting_key IS '设置键名，用于标识配置项';
COMMENT ON COLUMN system_settings.setting_value IS '设置值，存储配置内容';
COMMENT ON COLUMN system_settings.setting_type IS '设置类型：string/text/number/boolean/json';
COMMENT ON COLUMN system_settings.description IS '设置项描述说明';
COMMENT ON COLUMN system_settings.category IS '配置分类：site/seo/security/other';
COMMENT ON COLUMN system_settings.created_at IS '创建时间';
COMMENT ON COLUMN system_settings.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_system_settings_key ON system_settings(setting_key);
CREATE INDEX idx_system_settings_category ON system_settings(category);

-- ----------------------------
-- 个人基本信息表
-- ----------------------------
CREATE TABLE user_info (
    id INT8 PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    title VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    location VARCHAR(100),
    summary TEXT,
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_info IS '个人基本信息表';
COMMENT ON COLUMN user_info.id IS '主键ID';
COMMENT ON COLUMN user_info.name IS '姓名';
COMMENT ON COLUMN user_info.nickname IS '昵称/笔名';
COMMENT ON COLUMN user_info.avatar IS '头像路径';
COMMENT ON COLUMN user_info.title IS '职位/头衔';
COMMENT ON COLUMN user_info.email IS '邮箱地址';
COMMENT ON COLUMN user_info.phone IS '手机号码';
COMMENT ON COLUMN user_info.location IS '所在城市';
COMMENT ON COLUMN user_info.summary IS '个人简介';
COMMENT ON COLUMN user_info.bio IS '详细介绍';
COMMENT ON COLUMN user_info.created_at IS '创建时间';
COMMENT ON COLUMN user_info.updated_at IS '更新时间';

-- ----------------------------
-- 技能表
-- ----------------------------
CREATE TABLE user_skills (
    id INT8 PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    level INT NOT NULL,
    description VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_skills IS '技能表';
COMMENT ON COLUMN user_skills.id IS '主键ID';
COMMENT ON COLUMN user_skills.name IS '技能名称';
COMMENT ON COLUMN user_skills.category IS '技能分类：frontend/backend/database/tools/other';
COMMENT ON COLUMN user_skills.level IS '技能等级（1-5）';
COMMENT ON COLUMN user_skills.description IS '技能描述';
COMMENT ON COLUMN user_skills.sort_order IS '排序顺序';
COMMENT ON COLUMN user_skills.created_at IS '创建时间';
COMMENT ON COLUMN user_skills.updated_at IS '更新时间';

CREATE INDEX idx_user_skills_category ON user_skills(category);
CREATE INDEX idx_user_skills_sort_order ON user_skills(sort_order);

-- ----------------------------
-- 联系方式表
-- ----------------------------
CREATE TABLE user_contact (
    id INT8 PRIMARY KEY,
    contact_type VARCHAR(50) NOT NULL,
    contact_value VARCHAR(255) NOT NULL,
    display_name VARCHAR(50),
    icon VARCHAR(50),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_contact IS '联系方式表';
COMMENT ON COLUMN user_contact.id IS '主键ID';
COMMENT ON COLUMN user_contact.contact_type IS '联系方式类型：email/phone/github/linkedin/wechat/website/other';
COMMENT ON COLUMN user_contact.contact_value IS '联系方式值';
COMMENT ON COLUMN user_contact.display_name IS '显示名称';
COMMENT ON COLUMN user_contact.icon IS '图标名称';
COMMENT ON COLUMN user_contact.sort_order IS '排序顺序';
COMMENT ON COLUMN user_contact.created_at IS '创建时间';
COMMENT ON COLUMN user_contact.updated_at IS '更新时间';

CREATE INDEX idx_user_contact_type ON user_contact(contact_type);
CREATE INDEX idx_user_contact_sort_order ON user_contact(sort_order);

-- ----------------------------
-- 教育经历表
-- ----------------------------
CREATE TABLE user_education (
    id INT8 PRIMARY KEY,
    school_name VARCHAR(100) NOT NULL,
    major VARCHAR(100),
    degree VARCHAR(20),
    start_date DATE NOT NULL,
    end_date DATE,
    description VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_education IS '教育经历表';
COMMENT ON COLUMN user_education.id IS '主键ID';
COMMENT ON COLUMN user_education.school_name IS '学校名称';
COMMENT ON COLUMN user_education.major IS '专业';
COMMENT ON COLUMN user_education.degree IS '学历：bachelor/master/doctor/other';
COMMENT ON COLUMN user_education.start_date IS '开始日期';
COMMENT ON COLUMN user_education.end_date IS '结束日期（NULL表示在读）';
COMMENT ON COLUMN user_education.description IS '描述/备注';
COMMENT ON COLUMN user_education.sort_order IS '排序顺序';
COMMENT ON COLUMN user_education.created_at IS '创建时间';
COMMENT ON COLUMN user_education.updated_at IS '更新时间';

CREATE INDEX idx_user_education_sort_order ON user_education(sort_order);
CREATE INDEX idx_user_education_start_date ON user_education(start_date DESC);

-- ----------------------------
-- 工作经验表
-- ----------------------------
CREATE TABLE user_work (
    id INT8 PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    position VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    description VARCHAR(500),
    achievements TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_work IS '工作经验表';
COMMENT ON COLUMN user_work.id IS '主键ID';
COMMENT ON COLUMN user_work.company_name IS '公司名称';
COMMENT ON COLUMN user_work.position IS '职位';
COMMENT ON COLUMN user_work.start_date IS '开始日期';
COMMENT ON COLUMN user_work.end_date IS '结束日期（NULL表示当前在职）';
COMMENT ON COLUMN user_work.description IS '工作描述';
COMMENT ON COLUMN user_work.achievements IS '主要成就';
COMMENT ON COLUMN user_work.sort_order IS '排序顺序';
COMMENT ON COLUMN user_work.created_at IS '创建时间';
COMMENT ON COLUMN user_work.updated_at IS '更新时间';

CREATE INDEX idx_user_work_sort_order ON user_work(sort_order);
CREATE INDEX idx_user_work_start_date ON user_work(start_date DESC);

-- ----------------------------
-- 文章分类表
-- ----------------------------
CREATE TABLE article_category (
    id INT8 PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE article_category IS '文章分类表';
COMMENT ON COLUMN article_category.id IS '主键ID';
COMMENT ON COLUMN article_category.name IS '分类名称';
COMMENT ON COLUMN article_category.slug IS '分类别名';
COMMENT ON COLUMN article_category.description IS '分类描述';
COMMENT ON COLUMN article_category.sort_order IS '排序顺序';
COMMENT ON COLUMN article_category.created_at IS '创建时间';
COMMENT ON COLUMN article_category.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_article_category_slug ON article_category(slug);
CREATE INDEX idx_article_category_sort_order ON article_category(sort_order);

-- ----------------------------
-- 文章标签表
-- ----------------------------
CREATE TABLE article_tag (
    id INT8 PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    description VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE article_tag IS '文章标签表';
COMMENT ON COLUMN article_tag.id IS '主键ID';
COMMENT ON COLUMN article_tag.name IS '标签名称';
COMMENT ON COLUMN article_tag.slug IS '标签别名';
COMMENT ON COLUMN article_tag.color IS '标签颜色';
COMMENT ON COLUMN article_tag.description IS '标签描述';
COMMENT ON COLUMN article_tag.sort_order IS '排序顺序';
COMMENT ON COLUMN article_tag.created_at IS '创建时间';
COMMENT ON COLUMN article_tag.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_article_tag_slug ON article_tag(slug);
CREATE INDEX idx_article_tag_sort_order ON article_tag(sort_order);

-- ----------------------------
-- 文章表
-- ----------------------------
CREATE TABLE article (
    id INT8 PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    content TEXT,
    excerpt VARCHAR(500),
    category_id BIGINT,
    cover_image VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    author_id BIGINT,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE article IS '文章表';
COMMENT ON COLUMN article.id IS '主键ID';
COMMENT ON COLUMN article.title IS '文章标题';
COMMENT ON COLUMN article.slug IS '文章别名（URL友好）';
COMMENT ON COLUMN article.content IS '文章内容（Markdown格式）';
COMMENT ON COLUMN article.excerpt IS '文章摘要';
COMMENT ON COLUMN article.category_id IS '分类ID';
COMMENT ON COLUMN article.cover_image IS '封面图片路径';
COMMENT ON COLUMN article.status IS '状态：draft/published/private';
COMMENT ON COLUMN article.view_count IS '阅读量';
COMMENT ON COLUMN article.like_count IS '点赞数';
COMMENT ON COLUMN article.author_id IS '作者ID';
COMMENT ON COLUMN article.published_at IS '发布时间';
COMMENT ON COLUMN article.created_at IS '创建时间';
COMMENT ON COLUMN article.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_article_slug ON article(slug);
CREATE INDEX idx_article_category_id ON article(category_id);
CREATE INDEX idx_article_status ON article(status);
CREATE INDEX idx_article_published_at ON article(published_at DESC);
CREATE INDEX idx_article_view_count ON article(view_count DESC);

-- ----------------------------
-- 文章标签关联表
-- ----------------------------
CREATE TABLE article_tag_mapping (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (article_id, tag_id)
);

COMMENT ON TABLE article_tag_mapping IS '文章标签关联表';
COMMENT ON COLUMN article_tag_mapping.article_id IS '文章ID';
COMMENT ON COLUMN article_tag_mapping.tag_id IS '标签ID';
COMMENT ON COLUMN article_tag_mapping.created_at IS '创建时间';

CREATE INDEX idx_article_tag_mapping_article_id ON article_tag_mapping(article_id);
CREATE INDEX idx_article_tag_mapping_tag_id ON article_tag_mapping(tag_id);

-- ----------------------------
-- 项目技术栈表
-- ----------------------------
CREATE TABLE project_stack (
    id INT8 PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    description VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE project_stack IS '项目技术栈表';
COMMENT ON COLUMN project_stack.id IS '主键ID';
COMMENT ON COLUMN project_stack.name IS '技术栈名称';
COMMENT ON COLUMN project_stack.category IS '技术分类：language/framework/database/tools';
COMMENT ON COLUMN project_stack.icon IS '图标名称';
COMMENT ON COLUMN project_stack.color IS '显示颜色';
COMMENT ON COLUMN project_stack.description IS '技术描述';
COMMENT ON COLUMN project_stack.sort_order IS '排序顺序';
COMMENT ON COLUMN project_stack.created_at IS '创建时间';
COMMENT ON COLUMN project_stack.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_project_stack_name ON project_stack(name);
CREATE INDEX idx_project_stack_category ON project_stack(category);
CREATE INDEX idx_project_stack_sort_order ON project_stack(sort_order);

-- ----------------------------
-- 项目表
-- ----------------------------
CREATE TABLE project (
    id INT8 PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    content TEXT,
    cover_image VARCHAR(255),
    project_url VARCHAR(255),
    repo_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    start_date DATE,
    end_date DATE,
    role VARCHAR(50),
    contribution VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE project IS '项目表';
COMMENT ON COLUMN project.id IS '主键ID';
COMMENT ON COLUMN project.name IS '项目名称';
COMMENT ON COLUMN project.slug IS '项目别名（URL友好）';
COMMENT ON COLUMN project.description IS '项目描述';
COMMENT ON COLUMN project.content IS '项目详细介绍';
COMMENT ON COLUMN project.cover_image IS '封面图片路径';
COMMENT ON COLUMN project.project_url IS '项目链接';
COMMENT ON COLUMN project.repo_url IS '代码仓库地址（支持GitHub/GitLab/Gitee等）';
COMMENT ON COLUMN project.status IS '状态：active/completed/paused';
COMMENT ON COLUMN project.start_date IS '开始日期';
COMMENT ON COLUMN project.end_date IS '结束日期（NULL表示进行中）';
COMMENT ON COLUMN project.role IS '项目角色';
COMMENT ON COLUMN project.contribution IS '项目贡献描述';
COMMENT ON COLUMN project.sort_order IS '排序顺序';
COMMENT ON COLUMN project.created_at IS '创建时间';
COMMENT ON COLUMN project.updated_at IS '更新时间';
COMMENT ON COLUMN project.published IS '是否发布（true=发布，false=草稿）';

CREATE UNIQUE INDEX uk_project_slug ON project(slug);
CREATE INDEX idx_project_status ON project(status);
CREATE INDEX idx_project_sort_order ON project(sort_order);

-- ----------------------------
-- 项目技术栈关联表
-- ----------------------------
CREATE TABLE project_stack_mapping (
    project_id BIGINT NOT NULL,
    stack_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, stack_id)
);

COMMENT ON TABLE project_stack_mapping IS '项目技术栈关联表';
COMMENT ON COLUMN project_stack_mapping.project_id IS '项目ID';
COMMENT ON COLUMN project_stack_mapping.stack_id IS '技术栈ID';
COMMENT ON COLUMN project_stack_mapping.created_at IS '创建时间';

CREATE INDEX idx_project_stack_mapping_project_id ON project_stack_mapping(project_id);
CREATE INDEX idx_project_stack_mapping_stack_id ON project_stack_mapping(stack_id);

-- ----------------------------
-- 文件分类表
-- ----------------------------
CREATE TABLE file_category (
    id INT8 PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE file_category IS '文件分类表';
COMMENT ON COLUMN file_category.id IS '主键ID';
COMMENT ON COLUMN file_category.name IS '分类名称';
COMMENT ON COLUMN file_category.code IS '分类编码';
COMMENT ON COLUMN file_category.description IS '分类描述';
COMMENT ON COLUMN file_category.icon IS '分类图标';
COMMENT ON COLUMN file_category.sort_order IS '排序顺序';
COMMENT ON COLUMN file_category.created_at IS '创建时间';
COMMENT ON COLUMN file_category.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_file_category_code ON file_category(code);
CREATE INDEX idx_file_category_sort_order ON file_category(sort_order);

-- ----------------------------
-- 文件元数据表
-- 遵循 x-file-storage 推荐的数据库表结构
-- 同时支持视频文件（file_type='video'时使用视频专用字段）
-- ----------------------------
CREATE TABLE file_metadata (
    id INT8 PRIMARY KEY,
    -- 存储属性
    url VARCHAR(1000),
    path VARCHAR(500),
    filename VARCHAR(500),
    original_filename VARCHAR(500),
    file_ext VARCHAR(20),
    file_size BIGINT,
    content_type VARCHAR(100),
    platform VARCHAR(50),
    base_path VARCHAR(500),
    -- 分类与描述
    file_type VARCHAR(20) NOT NULL DEFAULT 'other',   -- video/image/document/archive/other
    category_id BIGINT,
    category_name VARCHAR(100),
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT false,
    -- 哈希与关联
    hash_info TEXT,
    object_id VARCHAR(100),
    object_type VARCHAR(50),
    -- JSON 元数据
    metadata TEXT,
    user_metadata TEXT,
    attr TEXT,
    -- 媒体属性（值视 fileType 而定）
    title VARCHAR(500),
    slug VARCHAR(255),
    cover_url VARCHAR(500),
    hls_url VARCHAR(500),
    source_url VARCHAR(500),
    duration INT,
    width INT,
    height INT,
    file_format VARCHAR(20),
    video_type VARCHAR(20),
    tags VARCHAR(500),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    coin_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    video_status VARCHAR(20),
    published_at TIMESTAMP,
    sort_weight INT DEFAULT 0,
    origin_file_id BIGINT,
    transcoding_progress INT,
    error_message TEXT,
    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE file_metadata IS '文件元数据表';
COMMENT ON COLUMN file_metadata.id IS '主键ID';
COMMENT ON COLUMN file_metadata.url IS '文件访问URL';
COMMENT ON COLUMN file_metadata.path IS '存储路径';
COMMENT ON COLUMN file_metadata.filename IS '文件名';
COMMENT ON COLUMN file_metadata.original_filename IS '原始文件名';
COMMENT ON COLUMN file_metadata.file_ext IS '文件扩展名';
COMMENT ON COLUMN file_metadata.file_size IS '文件大小（字节）';
COMMENT ON COLUMN file_metadata.content_type IS 'MIME类型';
COMMENT ON COLUMN file_metadata.platform IS '存储平台';
COMMENT ON COLUMN file_metadata.base_path IS '基础路径';
COMMENT ON COLUMN file_metadata.file_type IS '文件类型：video/image/document/archive/other';
COMMENT ON COLUMN file_metadata.category_id IS '分类ID';
COMMENT ON COLUMN file_metadata.category_name IS '分类名称';
COMMENT ON COLUMN file_metadata.description IS '文件描述';
COMMENT ON COLUMN file_metadata.is_public IS '是否公开（true=公开，false=私有）';
COMMENT ON COLUMN file_metadata.hash_info IS '哈希信息（JSON格式）';
COMMENT ON COLUMN file_metadata.object_id IS '关联对象ID';
COMMENT ON COLUMN file_metadata.object_type IS '关联对象类型';
COMMENT ON COLUMN file_metadata.metadata IS '元数据（JSON格式）';
COMMENT ON COLUMN file_metadata.user_metadata IS '用户自定义元数据（JSON格式）';
COMMENT ON COLUMN file_metadata.attr IS '扩展属性（JSON格式）';
COMMENT ON COLUMN file_metadata.title IS '标题';
COMMENT ON COLUMN file_metadata.slug IS '别名';
COMMENT ON COLUMN file_metadata.cover_url IS '封面图片URL';
COMMENT ON COLUMN file_metadata.hls_url IS 'HLS播放地址';
COMMENT ON COLUMN file_metadata.source_url IS '源文件地址';
COMMENT ON COLUMN file_metadata.duration IS '时长（秒）';
COMMENT ON COLUMN file_metadata.width IS '宽度（像素）';
COMMENT ON COLUMN file_metadata.height IS '高度（像素）';
COMMENT ON COLUMN file_metadata.file_format IS '文件格式';
COMMENT ON COLUMN file_metadata.video_type IS '视频类型';
COMMENT ON COLUMN file_metadata.tags IS '标签';
COMMENT ON COLUMN file_metadata.view_count IS '浏览量';
COMMENT ON COLUMN file_metadata.like_count IS '点赞数';
COMMENT ON COLUMN file_metadata.coin_count IS '投币数';
COMMENT ON COLUMN file_metadata.favorite_count IS '收藏数';
COMMENT ON COLUMN file_metadata.video_status IS '视频状态';
COMMENT ON COLUMN file_metadata.published_at IS '发布时间';
COMMENT ON COLUMN file_metadata.sort_weight IS '排序权重';
COMMENT ON COLUMN file_metadata.origin_file_id IS '源文件ID';
COMMENT ON COLUMN file_metadata.transcoding_progress IS '转码进度';
COMMENT ON COLUMN file_metadata.error_message IS '错误信息';
COMMENT ON COLUMN file_metadata.created_at IS '创建时间';
COMMENT ON COLUMN file_metadata.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_file_url ON file_metadata(url);
CREATE INDEX idx_file_type ON file_metadata(file_type);
CREATE INDEX idx_file_category ON file_metadata(category_id);
CREATE INDEX idx_file_object ON file_metadata(object_id, object_type);

-- 上传任务表（替代 Redis Hash）
CREATE TABLE file_upload_task (
    id INT8 PRIMARY KEY,
    upload_id VARCHAR(64) NOT NULL UNIQUE,
    business_id BIGINT,
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_md5 VARCHAR(64) NOT NULL DEFAULT '',
    chunk_size BIGINT DEFAULT 5242880,
    total_chunks INT NOT NULL,
    uploaded_chunks INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'INIT',           -- INIT/UPLOADING/PAUSED/MERGING/COMPLETED/CANCELLED/FAILED
    platform VARCHAR(50),
    file_info_json TEXT,
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE file_upload_task IS '文件上传任务表';
COMMENT ON COLUMN file_upload_task.id IS '主键ID';
COMMENT ON COLUMN file_upload_task.upload_id IS '上传唯一标识ID';
COMMENT ON COLUMN file_upload_task.business_id IS '业务ID';
COMMENT ON COLUMN file_upload_task.file_name IS '文件名';
COMMENT ON COLUMN file_upload_task.file_size IS '文件大小（字节）';
COMMENT ON COLUMN file_upload_task.file_md5 IS '文件MD5值';
COMMENT ON COLUMN file_upload_task.chunk_size IS '分片大小（字节）';
COMMENT ON COLUMN file_upload_task.total_chunks IS '总分片数';
COMMENT ON COLUMN file_upload_task.uploaded_chunks IS '已上传分片数';
COMMENT ON COLUMN file_upload_task.status IS '状态：INIT/UPLOADING/PAUSED/MERGING/COMPLETED/CANCELLED/FAILED';
COMMENT ON COLUMN file_upload_task.platform IS '存储平台';
COMMENT ON COLUMN file_upload_task.file_info_json IS '文件信息（JSON格式）';
COMMENT ON COLUMN file_upload_task.error_message IS '错误信息';
COMMENT ON COLUMN file_upload_task.created_at IS '创建时间';
COMMENT ON COLUMN file_upload_task.updated_at IS '更新时间';

CREATE INDEX idx_task_md5_status ON file_upload_task(file_md5, status);
CREATE INDEX idx_task_business ON file_upload_task(business_id);

-- 分片记录表
CREATE TABLE file_chunk (
    id INT8 PRIMARY KEY,
    upload_id VARCHAR(64) NOT NULL,
    filename VARCHAR(500),
    file_size BIGINT,
    chunk_number INT NOT NULL,
    chunk_size BIGINT,
    total_chunks INT,
    chunk_md5 VARCHAR(64),
    status VARCHAR(20) DEFAULT 'uploading',
    path VARCHAR(500),
    user_id BIGINT,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE file_chunk IS '文件分片记录表';
COMMENT ON COLUMN file_chunk.id IS '主键ID';
COMMENT ON COLUMN file_chunk.upload_id IS '上传ID';
COMMENT ON COLUMN file_chunk.filename IS '文件名';
COMMENT ON COLUMN file_chunk.file_size IS '文件大小（字节）';
COMMENT ON COLUMN file_chunk.chunk_number IS '分片序号';
COMMENT ON COLUMN file_chunk.chunk_size IS '分片大小（字节）';
COMMENT ON COLUMN file_chunk.total_chunks IS '总分片数';
COMMENT ON COLUMN file_chunk.chunk_md5 IS '分片MD5值';
COMMENT ON COLUMN file_chunk.status IS '分片状态：uploading/completed/failed';
COMMENT ON COLUMN file_chunk.path IS '分片存储路径';
COMMENT ON COLUMN file_chunk.user_id IS '用户ID';
COMMENT ON COLUMN file_chunk.completed_at IS '完成时间';
COMMENT ON COLUMN file_chunk.created_at IS '创建时间';
COMMENT ON COLUMN file_chunk.updated_at IS '更新时间';

CREATE UNIQUE INDEX uk_file_chunk_number ON file_chunk(upload_id, chunk_number);
CREATE INDEX idx_file_chunk_upload ON file_chunk(upload_id);
