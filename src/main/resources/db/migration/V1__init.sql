CREATE TABLE IF NOT EXISTS sample_entity
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    created_by  TEXT      NOT NULL DEFAULT 'anonymous',
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    modified_by TEXT      NOT NULL DEFAULT 'anonymous',
    modified_at TIMESTAMP NOT NULL DEFAULT now()
);