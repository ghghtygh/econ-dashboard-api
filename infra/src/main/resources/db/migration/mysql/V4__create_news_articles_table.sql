-- 경제 뉴스 테이블
CREATE TABLE news_articles (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(500)  NOT NULL,
    summary      TEXT,
    url          VARCHAR(1000) NOT NULL UNIQUE,
    source       VARCHAR(500),
    author       VARCHAR(500),
    image_url    VARCHAR(1000),
    category     VARCHAR(50)   NOT NULL,
    published_at TIMESTAMP     NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_news_articles_category ON news_articles (category);
CREATE INDEX idx_news_articles_published_at ON news_articles (published_at);
