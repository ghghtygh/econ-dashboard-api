-- 거시경제 지표 추가 (FRED 데이터 소스)
-- 이슈 #31: CPI, 실업률, PCE, ISM PMI, 미시간 소비자심리지수

-- 기존 CPIAUCSL, GDP 지표 설명 업데이트
UPDATE indicators SET name = 'US CPI (All Urban)', description = '미국 소비자물가지수 (전 도시 소비자)' WHERE symbol = 'CPIAUCSL';
UPDATE indicators SET description = '미국 GDP 성장률' WHERE symbol = 'GDP';

INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
-- 물가 지표
('US Core CPI', 'CPILFESL', 'MACRO', 'Index', 'FRED', '미국 근원 소비자물가지수 (식품·에너지 제외)'),
('Korea CPI', 'KORCPIALLMINMEI', 'MACRO', 'Index', 'FRED', '한국 소비자물가지수'),

-- 고용 지표
('US Unemployment Rate', 'UNRATE', 'MACRO', '%', 'FRED', '미국 실업률 (U-3)'),
('Korea Unemployment Rate', 'LMUNRRTTKRIQ156S', 'MACRO', '%', 'FRED', '한국 실업률'),

-- PCE (Fed가 선호하는 물가 지표)
('US PCE Price Index', 'PCEPI', 'MACRO', 'Index', 'FRED', '미국 개인소비지출 물가지수'),
('US Core PCE Price Index', 'PCEPILFE', 'MACRO', 'Index', 'FRED', '미국 근원 PCE 물가지수 (식품·에너지 제외)'),

-- ISM PMI
('ISM Manufacturing PMI', 'MANEMP', 'MACRO', 'Index', 'FRED', 'ISM 제조업 고용지수'),
('ISM Non-Manufacturing NMI', 'NMFCI', 'MACRO', 'Index', 'FRED', 'NFCI 금융상황지수'),

-- 소비자 심리
('Michigan Consumer Sentiment', 'UMCSENT', 'MACRO', 'Index', 'FRED', '미시간대 소비자심리지수');
