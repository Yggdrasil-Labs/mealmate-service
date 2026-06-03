-- UC3: 生成周计划相关表

CREATE TABLE weekly_meal_plan (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    family_id          BIGINT       NOT NULL,
    week_start_date    DATE         NOT NULL,
    week_end_date      DATE         NOT NULL,
    status             VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
    plan_source        VARCHAR(16)  NOT NULL DEFAULT 'MANUAL',
    rule_snapshot_json JSON         DEFAULT NULL,
    remark             VARCHAR(255) DEFAULT NULL,
    generated_time     DATETIME     DEFAULT NULL,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by         BIGINT       NOT NULL DEFAULT 0,
    updated_by         BIGINT       NOT NULL DEFAULT 0,
    deleted            BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_family_week (family_id, week_start_date, deleted),
    KEY idx_family_status_deleted (family_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE meal_plan_item (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    plan_id        BIGINT       NOT NULL,
    meal_date      DATE         NOT NULL,
    meal_type      VARCHAR(16)  NOT NULL,
    recipe_id      BIGINT       NOT NULL,
    crowd_type     VARCHAR(32)  DEFAULT NULL,
    is_weight_loss TINYINT      NOT NULL DEFAULT 0,
    is_baby_meal   TINYINT      NOT NULL DEFAULT 0,
    duplicate_flag TINYINT      NOT NULL DEFAULT 0,
    sort_order     INT          NOT NULL DEFAULT 0,
    remark         VARCHAR(255) DEFAULT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     BIGINT       NOT NULL DEFAULT 0,
    updated_by     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_plan_date_type (plan_id, meal_date, meal_type),
    KEY idx_recipe_id (recipe_id),
    KEY idx_plan_crowd (plan_id, crowd_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prep_plan (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    plan_id        BIGINT       NOT NULL,
    push_status    VARCHAR(16)  NOT NULL DEFAULT 'INIT',
    generated_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark         VARCHAR(255) DEFAULT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     BIGINT       NOT NULL DEFAULT 0,
    updated_by     BIGINT       NOT NULL DEFAULT 0,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_plan_id (plan_id),
    KEY idx_push_status_deleted (push_status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prep_plan_item (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    prep_plan_id    BIGINT       NOT NULL,
    ingredient_name VARCHAR(64)  NOT NULL,
    quantity        DECIMAL(10,2) DEFAULT NULL,
    unit            VARCHAR(16)  DEFAULT NULL,
    storage_method  VARCHAR(64)  DEFAULT NULL,
    priority        VARCHAR(16)  NOT NULL DEFAULT 'NORMAL',
    task_status     VARCHAR(16)  NOT NULL DEFAULT 'TODO',
    remark          VARCHAR(255) DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      BIGINT       NOT NULL DEFAULT 0,
    updated_by      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_prep_plan (prep_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shopping_item (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    plan_id        BIGINT        NOT NULL,
    ingredient_name VARCHAR(64)  NOT NULL,
    total_quantity DECIMAL(10,2) DEFAULT NULL,
    unit           VARCHAR(16)   DEFAULT NULL,
    purchased_flag TINYINT       NOT NULL DEFAULT 0,
    sort_no        INT           NOT NULL DEFAULT 0,
    remark         VARCHAR(255)  DEFAULT NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     BIGINT        NOT NULL DEFAULT 0,
    updated_by     BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_plan (plan_id),
    KEY idx_plan_purchase (plan_id, purchased_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
