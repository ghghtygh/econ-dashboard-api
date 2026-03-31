-- alert_rules에 severity, message 컬럼 추가 (프론트엔드 연동용)
ALTER TABLE alert_rules ADD COLUMN severity VARCHAR(20) NOT NULL DEFAULT 'warning';
ALTER TABLE alert_rules ADD COLUMN message VARCHAR(500) NOT NULL DEFAULT '';
