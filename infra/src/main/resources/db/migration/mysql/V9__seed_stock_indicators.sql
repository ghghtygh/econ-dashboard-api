-- 주식 시장 지표 추가

INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
('CBOE VIX', '^VIX', 'STOCK', 'Index', 'YAHOO', 'CBOE 변동성 지수 (시장 공포 지표)'),
('KOSDAQ Composite', '^KQ11', 'STOCK', 'Index', 'YAHOO', '코스닥 종합지수'),
('Dow Jones Industrial', '^DJI', 'STOCK', 'Index', 'YAHOO', '다우존스 산업평균지수'),
('Russell 2000', '^RUT', 'STOCK', 'Index', 'YAHOO', '러셀 2000 소형주 지수');
