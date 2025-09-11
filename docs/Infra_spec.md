1. AWS Cloud 기준 작성
 (ROUTE53역할)
www.example.com 같은 도메인 이름을 IP 주소로 변환해줌.

 EC2 서버, ALB, CloudFront 같은 AWS 리소스에 도메인 연결할 때 필요.

EC2, S3, ELB, CloudFront, API Gateway 같은 AWS 서비스에 바로 연결 가능.

Route53이 지정한 엔드포인트를 주기적으로 체크해서 장애가 발생하면 자동으로 다른 리소스로 라우팅.




 (ALB 역할)
예를 들어 gyuseok.com 으로 요청시 “gyuseok.com = ALB 주소” 를 알려주는 역할.여기서 IP를 직접 주는 게 아니라, 보통은 ALB의 DNS 이름 (예: my-alb-123456.ap-northeas-2.elb.amazoaws.com)을 리턴함.

 ALB는 DNS가 안내해준 트래픽을 받아서, 실제 백엔드 서버로 분산 처리

이 그림에서는 로드벨런싱 역할이 주가 되어 사용할 것으로 작성함


 (WAF 역할)
ALB 랑 한 세트 ALB 가 로드밸런싱 헬스체크 등 서버로 분배를 처리한다면 WAF 는 스크립트 형식의 룰 설정을통해서 보안처리를 할 수 있

공격 패턴 차단

SQL Injection, XSS(스크립트 삽입) 같은 웹 공격 탐지 & 차단.

AWS에서 제공하는 관리형 룰셋(Microsoft, Owasp Top 10 대응) 사용 가능.

IP 기반 차단/허용

특정 국가(GeoIP), 특정 IP 대역 블록.

예: “중국, 러시아 IP는 차단” 같은 정책.

Rate Limiting (속도 제한)

같은 IP에서 너무 많은 요청이 오면 일정 횟수 이상 차단.

디도스(DDoS) 완화.

커스텀 룰 작성

특정 URI, 헤더, 쿼리 파라미터 조건으로 룰 만들 수 있음.예: /admin 경로는 특정 IP에서만 접근 허용.


 (WEB 역할)
콘텐츠 처리 및 WAS 로 전달



 (WAS역할)
콘서트 예약 어플리케이션 실 구현

 (REDIS 역할)
AWS 에서 redis 를 제공하는지 처음알게 됨① 캐시(Cache)
자주 조회하는 데이터를 Redis에 저장해두고 DB 접근 줄이기.
② 세션 저장
사용자가 로그인하면 세션 정보를 Redis에 저장.

서버가 여러 대(WAS 멀티 인스턴스)일 때 세션을 공유할 수 있음.

③ 분산 락
여러 서버/스레드가 동시에 같은 자원에 접근할 때 충돌 방지.

예: 동시에 두 명이 같은 좌석 예약하는 상황 → Redis 락으로 한 명만 성공하게 제어.

④ 큐/스트림
Redis Pub/Sub, Stream 기능으로 간단한 메시지 브로커 역할.

⑤ Rate 
특정 사용자가 너무 많은 요청을 보내면 Redis 카운터로 제한.