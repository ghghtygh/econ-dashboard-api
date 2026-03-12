-- 주요 경제 지표 마스터 데이터
INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
-- 주식
('S&P 500', '^GSPC', 'STOCK', 'USD', 'YAHOO', 'S&P 500 지수'),
('NASDAQ Composite', '^IXIC', 'STOCK', 'USD', 'YAHOO', '나스닥 종합 지수'),
('KOSPI', '^KS11', 'STOCK', 'KRW', 'YAHOO', '코스피 지수'),

-- 외환
('USD/KRW', 'USDKRW=X', 'FOREX', 'KRW', 'YAHOO', '달러/원 환율'),
('EUR/USD', 'EURUSD=X', 'FOREX', 'USD', 'YAHOO', '유로/달러 환율'),

-- 암호화폐
('Bitcoin', 'bitcoin', 'CRYPTO', 'USD', 'COINGECKO', '비트코인'),
('Ethereum', 'ethereum', 'CRYPTO', 'USD', 'COINGECKO', '이더리움'),

-- 거시경제
('US GDP Growth Rate', 'GDP', 'MACRO', '%', 'FRED', '미국 GDP 성장률'),
('US CPI', 'CPIAUCSL', 'MACRO', 'Index', 'FRED', '미국 소비자물가지수'),
('Korea Base Rate', 'BOK_BASE_RATE', 'MACRO', '%', 'BOK', '한국 기준금리'),

-- 채권
('US 10Y Treasury', '^TNX', 'BOND', '%', 'YAHOO', '미국 10년물 국채 수익률'),
('US 2Y Treasury', '^IRX', 'BOND', '%', 'YAHOO', '미국 2년물 국채 수익률'),

-- 원자재
('Gold', 'GC=F', 'COMMODITY', 'USD', 'YAHOO', '금 선물'),
('WTI Crude Oil', 'CL=F', 'COMMODITY', 'USD', 'YAHOO', 'WTI 원유 선물');
