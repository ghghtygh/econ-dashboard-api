-- 채권 지표 확장

INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
('US 10Y-2Y Yield Spread', 'T10Y2Y', 'BOND', '%', 'FRED', '미국 장단기 금리차 (10년물 - 2년물, 경기침체 선행지표)'),
('Korea 10Y Government Bond', 'IRLTLT01KRM156N', 'BOND', '%', 'FRED', '한국 국채 10년물 수익률');
