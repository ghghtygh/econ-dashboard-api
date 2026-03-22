-- 원자재 지표 추가 (이슈 #33)
-- 천연가스, 구리, 밀, 콩 (Yahoo Finance 소스)

INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
('Natural Gas', 'NG=F', 'COMMODITY', 'USD/MMBtu', 'YAHOO', '천연가스 선물'),
('Copper', 'HG=F', 'COMMODITY', 'USD/lb', 'YAHOO', '구리 선물 (Dr. Copper, 경기 선행지표)'),
('Wheat', 'ZW=F', 'COMMODITY', 'USd/bu', 'YAHOO', '밀 선물'),
('Soybeans', 'ZS=F', 'COMMODITY', 'USd/bu', 'YAHOO', '콩(대두) 선물');
