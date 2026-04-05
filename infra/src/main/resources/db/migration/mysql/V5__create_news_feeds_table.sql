-- 뉴스 피드 관리 테이블
CREATE TABLE news_feeds (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL,
    url         VARCHAR(1000) NOT NULL UNIQUE,
    category    VARCHAR(50)   NOT NULL,
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기본 RSS 피드 시드 데이터
INSERT INTO news_feeds (name, url, category) VALUES
('S&P 500 News', 'https://feeds.finance.yahoo.com/rss/2.0/headline?s=^GSPC&region=US&lang=en-US', 'STOCK'),
('Dow Jones News', 'https://feeds.finance.yahoo.com/rss/2.0/headline?s=^DJI&region=US&lang=en-US', 'STOCK'),
('EUR/USD News', 'https://feeds.finance.yahoo.com/rss/2.0/headline?s=EURUSD=X&region=US&lang=en-US', 'FOREX'),
('Bitcoin News', 'https://feeds.finance.yahoo.com/rss/2.0/headline?s=BTC-USD&region=US&lang=en-US', 'CRYPTO'),
('Gold News', 'https://feeds.finance.yahoo.com/rss/2.0/headline?s=GC=F&region=US&lang=en-US', 'COMMODITY'),
('US Treasury News', 'https://feeds.finance.yahoo.com/rss/2.0/headline?s=^TNX&region=US&lang=en-US', 'BOND');
