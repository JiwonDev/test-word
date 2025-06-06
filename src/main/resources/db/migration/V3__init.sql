-- article_content 테이블
CREATE TABLE IF NOT EXISTS article_content
(
    id                       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content                  TEXT                     NOT NULL,
    title                    VARCHAR(255)             NOT NULL,
    author                   VARCHAR(255),
    url                      VARCHAR(255),
    source                   VARCHAR(255),
    summary                  TEXT,
    forbidden_term_id_counts JSONB                    NOT NULL DEFAULT '{}',
    created_by               VARCHAR(255)             NOT NULL DEFAULT 'anonymous',
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    modified_by              VARCHAR(255)             NOT NULL DEFAULT 'anonymous',
    modified_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_article_content_title ON article_content (title);
CREATE INDEX idx_article_content_author ON article_content (author);
CREATE INDEX idx_article_content_url ON article_content (url);

-- tag 테이블
CREATE TABLE IF NOT EXISTS tag
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255)             NOT NULL UNIQUE,
    created_by  VARCHAR(255)             NOT NULL DEFAULT 'anonymous',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    modified_by VARCHAR(255)             NOT NULL DEFAULT 'anonymous',
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_tag_name ON tag (name);

-- map_article_tag 테이블
CREATE TABLE IF NOT EXISTS map_article_tag
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES article_content (id) ON DELETE CASCADE,
    tag_id     BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE
);

CREATE INDEX idx_map_article_tag_article_id ON map_article_tag (article_id);
CREATE INDEX idx_map_article_tag_tag_id ON map_article_tag (tag_id);
CREATE UNIQUE INDEX uq_map_article_tag ON map_article_tag (article_id, tag_id);
