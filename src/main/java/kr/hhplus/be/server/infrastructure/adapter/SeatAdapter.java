package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.repository.SeatRepository;
import org.springframework.stereotype.Repository;

/**
 * 좌석 관련 포트를 구현하는 어댑터입니다.
 * 기존 SeatRepository를 사용하여 도메인 레이어와 기존 Repository를 연결합니다.
 */
@Repository
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
