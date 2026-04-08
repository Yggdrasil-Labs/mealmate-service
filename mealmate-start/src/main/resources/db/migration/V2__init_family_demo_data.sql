INSERT INTO family_profile (
    id,
    family_name,
    family_code,
    status,
    region,
    meal_goal_json,
    remark,
    created_by,
    updated_by,
    deleted
)
SELECT
    1000000000001,
    'Demo Family',
    'FAM_DEMO_001',
    'ENABLED',
    'Shanghai',
    '{"weekday":"quick","weekend":"balanced"}',
    'Seed data for local development and integration verification.',
    'flyway',
    'flyway',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM family_profile WHERE id = 1000000000001
);

INSERT INTO family_member (
    id,
    family_id,
    name,
    role_type,
    gender,
    birthday,
    region,
    target_type,
    avatar_url,
    sort_no,
    status,
    created_by,
    updated_by,
    deleted
)
SELECT
    1000000000101,
    1000000000001,
    'Yang Dad',
    'ADULT',
    'MALE',
    DATE '1988-03-12',
    'Shanghai',
    'BALANCED',
    NULL,
    1,
    'ACTIVE',
    'flyway',
    'flyway',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM family_member WHERE id = 1000000000101
);

INSERT INTO family_member (
    id,
    family_id,
    name,
    role_type,
    gender,
    birthday,
    region,
    target_type,
    avatar_url,
    sort_no,
    status,
    created_by,
    updated_by,
    deleted
)
SELECT
    1000000000102,
    1000000000001,
    'Yang Mom',
    'ADULT',
    'FEMALE',
    DATE '1990-07-21',
    'Shanghai',
    'HEALTH_MANAGEMENT',
    NULL,
    2,
    'ACTIVE',
    'flyway',
    'flyway',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM family_member WHERE id = 1000000000102
);

INSERT INTO family_member (
    id,
    family_id,
    name,
    role_type,
    gender,
    birthday,
    region,
    target_type,
    avatar_url,
    sort_no,
    status,
    created_by,
    updated_by,
    deleted
)
SELECT
    1000000000103,
    1000000000001,
    'Yang Baby',
    'BABY',
    'UNKNOWN',
    DATE '2023-11-05',
    'Shanghai',
    'BALANCED',
    NULL,
    3,
    'ACTIVE',
    'flyway',
    'flyway',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM family_member WHERE id = 1000000000103
);
