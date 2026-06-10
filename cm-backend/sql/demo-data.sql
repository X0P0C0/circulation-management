-- ============================================================
-- 配件流转管理系统 演示数据
-- ============================================================

USE cm_db;

-- 清空数据（保留admin用户和分类）
DELETE FROM operation_log;
DELETE FROM flow_record;
DELETE FROM accessory;
DELETE FROM worker;

-- 重置自增
ALTER TABLE accessory AUTO_INCREMENT = 1;
ALTER TABLE flow_record AUTO_INCREMENT = 1;
ALTER TABLE worker AUTO_INCREMENT = 1;
ALTER TABLE operation_log AUTO_INCREMENT = 1;

-- 师傅
INSERT INTO worker (name, job_no, phone) VALUES
('张建国', 'W001', '13800001111'),
('李明', 'W002', '13800002222'),
('王强', 'W003', '13800003333');

-- 工件：在库 15个
INSERT INTO accessory (barcode, item_code, category_id, status, operator, create_time) VALUES
('*0401715DL', 'AC20260525-001', 1, 1, 'admin', '2026-05-25 09:00:00'),
('*0401715DL', 'AC20260525-002', 1, 1, 'admin', '2026-05-25 09:01:00'),
('*0401715DL', 'AC20260525-003', 1, 1, 'admin', '2026-05-25 09:02:00'),
('6612998', 'AC20260525-004', 2, 1, 'admin', '2026-05-25 10:00:00'),
('6612998', 'AC20260525-005', 2, 1, 'admin', '2026-05-25 10:01:00'),
('PCB-2024-A', 'AC20260525-006', 3, 1, 'admin', '2026-05-25 11:00:00'),
('PCB-2024-A', 'AC20260525-007', 3, 1, 'admin', '2026-05-25 11:01:00'),
('PCB-2024-A', 'AC20260525-008', 3, 1, 'admin', '2026-05-25 11:02:00'),
('MOTOR-370', 'AC20260525-009', 4, 1, 'admin', '2026-05-25 12:00:00'),
('MOTOR-370', 'AC20260525-010', 4, 1, 'admin', '2026-05-25 12:01:00'),
('SEAL-080', 'AC20260526-001', 5, 1, 'admin', '2026-05-26 09:00:00'),
('SEAL-080', 'AC20260526-002', 5, 1, 'admin', '2026-05-26 09:01:00'),
('SEAL-080', 'AC20260526-003', 5, 1, 'admin', '2026-05-26 09:02:00'),
('TOOL-SET-A', 'AC20260526-004', 6, 1, 'admin', '2026-05-26 10:00:00'),
('TOOL-SET-A', 'AC20260526-005', 6, 1, 'admin', '2026-05-26 10:01:00');

-- 工件：已出库 8个
INSERT INTO accessory (barcode, item_code, category_id, status, worker_id, operator, create_time) VALUES
('*0401715DL', 'AC20260524-001', 1, 2, 1, 'admin', '2026-05-24 09:00:00'),
('*0401715DL', 'AC20260524-002', 1, 2, 1, 'admin', '2026-05-24 09:01:00'),
('6612998', 'AC20260524-003', 2, 2, 2, 'admin', '2026-05-24 10:00:00'),
('6612998', 'AC20260524-004', 2, 2, 2, 'admin', '2026-05-24 10:01:00'),
('PCB-2024-A', 'AC20260524-005', 3, 2, 3, 'admin', '2026-05-24 11:00:00'),
('MOTOR-370', 'AC20260524-006', 4, 2, 1, 'admin', '2026-05-24 12:00:00'),
('SEAL-080', 'AC20260524-007', 5, 2, 2, 'admin', '2026-05-24 13:00:00'),
('TOOL-SET-A', 'AC20260524-008', 6, 2, 3, 'admin', '2026-05-24 14:00:00');

-- 工件：已完成 4个
INSERT INTO accessory (barcode, item_code, category_id, status, worker_id, operator, create_time) VALUES
('*0401715DL', 'AC20260523-001', 1, 3, 1, 'admin', '2026-05-23 09:00:00'),
('*0401715DL', 'AC20260523-002', 1, 3, 2, 'admin', '2026-05-23 09:01:00'),
('6612998', 'AC20260523-003', 2, 3, 3, 'admin', '2026-05-23 10:00:00'),
('PCB-2024-A', 'AC20260523-004', 3, 3, 1, 'admin', '2026-05-23 11:00:00');

-- 工件：寄回厂家 2个
INSERT INTO accessory (barcode, item_code, category_id, status, is_high_value, remark, operator, create_time) VALUES
('MOTOR-370', 'AC20260522-001', 4, 4, 1, '客户取消，高价值寄回厂家退款', 'admin', '2026-05-22 09:00:00'),
('MOTOR-370', 'AC20260522-002', 4, 4, 1, '客户取消，高价值寄回厂家退款', 'admin', '2026-05-22 09:01:00');

-- 工件：旧件待返厂 3个
INSERT INTO accessory (barcode, item_code, category_id, status, related_item_code, remark, operator, create_time) VALUES
('*0401715DL', 'AC20260521-001', 1, 5, 'AC20260523-001', '张建国换下的旧遥控器', 'admin', '2026-05-21 10:00:00'),
('*0401715DL', 'AC20260521-002', 1, 5, 'AC20260523-002', '李明换下的旧遥控器', 'admin', '2026-05-21 10:01:00'),
('6612998', 'AC20260521-003', 2, 5, 'AC20260523-003', '王强换下的旧传感器', 'admin', '2026-05-21 11:00:00');

-- 工件：已售卖 3个
INSERT INTO accessory (barcode, item_code, category_id, status, operator, create_time) VALUES
('SEAL-080', 'AC20260520-001', 5, 6, 'admin', '2026-05-20 09:00:00'),
('TOOL-SET-A', 'AC20260520-002', 6, 6, 'admin', '2026-05-20 10:00:00'),
('PCB-2024-A', 'AC20260520-003', 3, 6, 'admin', '2026-05-20 11:00:00');

-- 流转记录
INSERT INTO flow_record (accessory_id, item_code, barcode, flow_type, operator, create_time) VALUES
(1, 'AC20260525-001', '*0401715DL', 1, 'admin', '2026-05-25 09:00:00'),
(4, 'AC20260525-004', '6612998', 1, 'admin', '2026-05-25 10:00:00'),
(6, 'AC20260525-006', 'PCB-2024-A', 1, 'admin', '2026-05-25 11:00:00'),
(9, 'AC20260525-009', 'MOTOR-370', 1, 'admin', '2026-05-25 12:00:00'),
(11, 'AC20260526-001', 'SEAL-080', 1, 'admin', '2026-05-26 09:00:00'),
(14, 'AC20260526-004', 'TOOL-SET-A', 1, 'admin', '2026-05-26 10:00:00');

INSERT INTO flow_record (accessory_id, item_code, barcode, flow_type, to_worker_id, to_worker_name, operator, create_time) VALUES
(16, 'AC20260524-001', '*0401715DL', 2, 1, '张建国', 'admin', '2026-05-24 09:00:00'),
(17, 'AC20260524-002', '*0401715DL', 2, 1, '张建国', 'admin', '2026-05-24 09:01:00'),
(18, 'AC20260524-003', '6612998', 2, 2, '李明', 'admin', '2026-05-24 10:00:00'),
(19, 'AC20260524-004', '6612998', 2, 2, '李明', 'admin', '2026-05-24 10:01:00'),
(20, 'AC20260524-005', 'PCB-2024-A', 2, 3, '王强', 'admin', '2026-05-24 11:00:00'),
(21, 'AC20260524-006', 'MOTOR-370', 2, 1, '张建国', 'admin', '2026-05-24 12:00:00'),
(22, 'AC20260524-007', 'SEAL-080', 2, 2, '李明', 'admin', '2026-05-24 13:00:00'),
(23, 'AC20260524-008', 'TOOL-SET-A', 2, 3, '王强', 'admin', '2026-05-24 14:00:00');

INSERT INTO flow_record (accessory_id, item_code, barcode, flow_type, to_worker_id, to_worker_name, operator, create_time) VALUES
(24, 'AC20260523-001', '*0401715DL', 3, 1, '张建国', 'admin', '2026-05-23 09:00:00'),
(25, 'AC20260523-002', '*0401715DL', 3, 2, '李明', 'admin', '2026-05-23 09:01:00'),
(26, 'AC20260523-003', '6612998', 3, 3, '王强', 'admin', '2026-05-23 10:00:00'),
(27, 'AC20260523-004', 'PCB-2024-A', 3, 1, '张建国', 'admin', '2026-05-23 11:00:00');

INSERT INTO flow_record (accessory_id, item_code, barcode, flow_type, remark, operator, create_time) VALUES
(28, 'AC20260522-001', 'MOTOR-370', 5, '客户取消，高价值寄回厂家退款', 'admin', '2026-05-22 09:00:00'),
(29, 'AC20260522-002', 'MOTOR-370', 5, '客户取消，高价值寄回厂家退款', 'admin', '2026-05-22 09:01:00'),
(30, 'AC20260521-001', '*0401715DL', 1, '张建国换下的旧遥控器', 'admin', '2026-05-21 10:00:00'),
(31, 'AC20260521-002', '*0401715DL', 1, '李明换下的旧遥控器', 'admin', '2026-05-21 10:01:00'),
(32, 'AC20260521-003', '6612998', 1, '王强换下的旧传感器', 'admin', '2026-05-21 11:00:00');

INSERT INTO flow_record (accessory_id, item_code, barcode, flow_type, customer_name, customer_phone, operator, create_time) VALUES
(33, 'AC20260520-001', 'SEAL-080', 6, '赵先生', '13900001111', 'admin', '2026-05-20 09:00:00'),
(34, 'AC20260520-002', 'TOOL-SET-A', 6, '钱女士', '13900002222', 'admin', '2026-05-20 10:00:00'),
(35, 'AC20260520-003', 'PCB-2024-A', 6, NULL, NULL, 'admin', '2026-05-20 11:00:00');

-- 操作日志
INSERT INTO operation_log (action_type, content, related_barcode, operator, create_time) VALUES
('配件入库', '批量入库遥控器', '*0401715DL', 'admin', '2026-05-25 09:00:00'),
('配件出库', '出库遥控器给张建国', '*0401715DL', 'admin', '2026-05-24 09:00:00'),
('配件出库', '出库传感器给李明', '6612998', 'admin', '2026-05-24 10:00:00'),
('配件归还', '张建国归还遥控器', '*0401715DL', 'admin', '2026-05-23 09:00:00'),
('配件售卖', '售卖密封件给赵先生', 'SEAL-080', 'admin', '2026-05-20 09:00:00');