package kr.hhplus.be.server.model;

import java.time.LocalDate;
import java.util.*;

/**
 * 콘서트 정보를 관리하는 도메인 모델
 * 
 * 콘서트 예약 서비스에서 콘서트의 기본 정보와 좌석 생성을 담당하는 클래스입니다.
 * 콘서트 ID, 이름, 날짜, 총 좌석 수, 티켓 가격 등의 정보를 관리하고,
 * 해당 콘서트의 좌석 번호를 자동으로 생성하는 기능을 제공합니다.
 */
public class Concert {
    
    private String concertId;        // 콘서트 고유 식별자
    private String concertName;      // 콘서트 이름
    private LocalDate concertDate;   // 콘서트 개최 날짜
    private int totalSeatCount;      // 총 좌석 수 (1~50번 좌석) 과제 설정
    private BigDecimal ticketPrice;  // 티켓 가격 (BigDecimal 사용 이유: 정확한 금액 계산을 위해)
    private boolean isActive;        // 콘서트 활성화 상태 (true: 예약 가능, false: 예약 불가)
    
    /**
     * 콘서트 객체를 생성합니다.
     * 
     * @param concertId 콘서트 고유 식별자
     * @param concertName 콘서트 이름
     * @param concertDate 콘서트 개최 날짜
     * @param totalSeatCount 총 좌석 수
     * @param ticketPrice 티켓 가격
     * 
     * 이유: 새로운 콘서트를 등록할 때 호출됩니다.
     * 생성 시 isActive는 기본적으로 true로 설정되어 예약이 가능한 상태가 됩니다.
     * 나중에 콘서트를 비활성화하려면 setActive(false)를 호출하면 됩니다.
     */
    // 콘서트 객체 생성
    public Concert(String concertId, String concertName, LocalDate concertDate, int totalSeatCount, BigDecimal ticketPrice) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertDate = concertDate;
        this.totalSeatCount = totalSeatCount;
        this.ticketPrice = ticketPrice;
        this.isActive = true;  // 기본적으로 활성화 상태로 생성
    }
    
    /**
     * 콘서트의 좌석 번호 목록을 생성합니다.
     * 
     * @return 좌석 번호 문자열 리스트 (예: ["1", "2", "3", ..., "50"])
     * 
     * 이유: 콘서트 등록 시 해당 콘서트의 모든 좌석을 Seat 객체로 생성하기 위해 사용됩니다.
     * 1번부터 totalSeatCount까지의 좌석 번호를 문자열로 변환하여 반환합니다.
     * 예를 들어, 50석 콘서트라면 ["1", "2", "3", ..., "50"] 리스트를 반환합니다.
     */
    // 좌석 번호 목록 생성
    public List<String> generateSeatNumbers() {
        List<String> seatNumbers = new ArrayList<>();
        for (int i = 1; i <= totalSeatCount; i++) {
            seatNumbers.add(String.valueOf(i));
        }
        return seatNumbers;
    }
    
    /**
     * 콘서트 날짜가 예약 가능한지 확인합니다.
     * 
     * @return true: 예약 가능 (오늘 이후), false: 예약 불가 (과거 날짜)
     * 
     * 이유: 사용자가 예약 가능한 콘서트 목록을 조회할 때 사용됩니다.
     * 과거 날짜의 콘서트는 예약할 수 없으므로, 오늘 날짜 이후의 콘서트만 예약 가능합니다.
     * 오늘 날짜도 포함하여 당일 예약도 가능하도록 했습니다.
     */
    // 콘서트 날짜 예약 가능 여부 확인
    public boolean isConcertDateAvailable() {
        return concertDate.isAfter(LocalDate.now()) || concertDate.isEqual(LocalDate.now());
    }
    
    // Getters and Setters
    public String getConcertId() {
        return concertId;
    }
    
    public void setConcertId(String concertId) {
        this.concertId = concertId;
    }
    
    public String getConcertName() {
        return concertName;
    }
    
    public void setConcertName(String concertName) {
        this.concertName = concertName;
    }
    
    public LocalDate getConcertDate() {
        return concertDate;
    }
    
    public void setConcertDate(LocalDate concertDate) {
        this.concertDate = concertDate;
    }
    
    public int getTotalSeatCount() {
        return totalSeatCount;
    }
    
    public void setTotalSeatCount(int totalSeatCount) {
        this.totalSeatCount = totalSeatCount;
    }
    
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }
    
    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}
