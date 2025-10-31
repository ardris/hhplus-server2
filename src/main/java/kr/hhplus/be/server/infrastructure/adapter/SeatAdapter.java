package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.repository.SeatRepository;

/**
 * 좌석 관련 포트를 구현하는 어댑터입니다.
 * 기존 SeatRepository를 사용하여 도메인 레이어와 기존 Repository를 연결합니다.
 * 
 * NOTE: 레거시 구현체로, 현재는 JpaSeatAdapter를 사용합니다.
 * 빈으로 등록하지 않기 위해 @Repository 어노테이션을 제거했습니다.
 */
// @Repository - 레거시, 사용 안 함
public class SeatAdapter implements SeatPort {
    private final SeatRepository seatRepository;

    public SeatAdapter(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public boolean isSeatAvailable(String seatId) {
        // 기존 Repository를 통해 좌석 가용성 확인
        return seatRepository.isSeatAvailable(seatId);
    }

    @Override
    public void holdSeat(String seatId, String reservationId) {
        // 기존 Repository를 통해 좌석 임시 배정
        seatRepository.holdSeat(seatId, reservationId);
    }

    @Override
    public void sellSeat(String seatId) {
        // 기존 Repository를 통해 좌석 판매 완료
        seatRepository.sellSeat(seatId);
    }

    @Override
    public void releaseSeat(String seatId) {
        // 기존 Repository를 통해 좌석 해제
        seatRepository.releaseSeat(seatId);
    }
}
