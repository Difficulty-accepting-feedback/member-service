INSERT INTO challenge (challenge_id, name, description, point)
VALUES
    (1001, '첫 로그인', 'GROW에 첫 로그인 완료', 10),
    (1003, '휴대폰 인증', '휴대폰 번호 인증 완료', 50),
    (1004, '위치 설정', '지역/주소 설정 완료', 50),
    (4001, '첫 유료 결제', '첫 결제 성공', 200),
    (4002, '구독 시작', '구독형 상품 첫 결제', 200)
ON DUPLICATE KEY UPDATE
                     name = VALUES(name),
                     description = VALUES(description),
                     point = VALUES(point);

# MERGE INTO challenge (challenge_id, name, description, point)
#     KEY(challenge_id)
#     VALUES (1001, '첫 로그인', 'GROW에 첫 로그인 완료', 10),
#     (1003, '휴대폰 인증', '휴대폰 번호 인증 완료', 50),
#     (1004, '위치 설정', '지역/주소 설정 완료', 50),
#     (4001, '첫 유료 결제', '첫 결제 성공', 200),
#     (4002, '구독 시작', '구독형 상품 첫 결제', 200);