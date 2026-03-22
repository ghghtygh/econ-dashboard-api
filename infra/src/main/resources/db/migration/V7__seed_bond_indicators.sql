-- 채권 지표 확장 (이슈 #32)
-- 장단기 금리차 (10Y-2Y Spread), 한국 국채 10년물

INSERT INTO indicators (name, symbol, category, unit, source, description) VALUES
-- 장단기 금리차 (FRED 제공 계산된 스프레드)
('US 10Y-2Y Yield Spread', 'T10Y2Y', 'BOND', '%', 'FRED', '미국 장단기 금리차 (10년물 - 2년물, 경기침체 선행지표)'),

-- 한국 국채 10년물
('Korea 10Y Government Bond', 'IRLTLT01KRM156N', 'BOND', '%', 'FRED', '한국 국채 10년물 수익률');
