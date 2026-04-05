-- 거시경제 지표 추가 (FRED 데이터 소스)

UPDATE indicators SET name = 'US CPI (All Urban)', description = '미국 소비자물가지수 (전 도시 소비자)' WHERE symbol = 'CPIAUCSL';
UPDATE indicators SET description = '미국 GDP 성장률' WHERE symbol = 'GDP';

INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
('US Core CPI', 'CPILFESL', 'MACRO', 'Index', 'FRED', '미국 근원 소비자물가지수 (식품·에너지 제외)'),
('Korea CPI', 'KORCPIALLMINMEI', 'MACRO', 'Index', 'FRED', '한국 소비자물가지수'),
('US Unemployment Rate', 'UNRATE', 'MACRO', '%', 'FRED', '미국 실업률 (U-3)'),
('Korea Unemployment Rate', 'LMUNRRTTKRIQ156S', 'MACRO', '%', 'FRED', '한국 실업률'),
('US PCE Price Index', 'PCEPI', 'MACRO', 'Index', 'FRED', '미국 개인소비지출 물가지수'),
('US Core PCE Price Index', 'PCEPILFE', 'MACRO', 'Index', 'FRED', '미국 근원 PCE 물가지수 (식품·에너지 제외)'),
('ISM Manufacturing PMI', 'MANEMP', 'MACRO', 'Index', 'FRED', 'ISM 제조업 고용지수'),
('ISM Non-Manufacturing NMI', 'NMFCI', 'MACRO', 'Index', 'FRED', 'NFCI 금융상황지수'),
('Michigan Consumer Sentiment', 'UMCSENT', 'MACRO', 'Index', 'FRED', '미시간대 소비자심리지수');
