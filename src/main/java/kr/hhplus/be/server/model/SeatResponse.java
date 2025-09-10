package kr.hhplus.be.server.model;

/**
 * 좌석 정보 응답 모델
 * 좌석 조회 시 사용
 */
public class SeatResponse {
    private String seatId;
    private int seatNumber;
    private String status;
    
    public SeatResponse() {}
    
    public SeatResponse(String seatId, int seatNumber, String status) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.status = status;
    }
    
    public String getSeatId() {
        return seatId;
    }
    
    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }
    
    public int getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
