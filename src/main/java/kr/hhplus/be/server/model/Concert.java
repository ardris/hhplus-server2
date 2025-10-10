package kr.hhplus.be.server.model;

import java.time.LocalDate;
import java.util.*;

/**
 * 콘서트 정보를 관리하는 도메인 모델 (Performance 기반으로 확장)
 * 
 * 콘서트 예약 서비스에서 콘서트의 기본 정보를 관리하는 클래스입니다.
 * 이제 Performance와 Venue를 통해 실제 공연 정보를 관리합니다.
 */
public class Concert {
    
    private String concertId;        // 콘서트 고유 식별자
    private String concertName;      // 콘서트 이름
    private LocalDate concertPeriodStart;   // 콘서트 기간 시작일
    private LocalDate concertPeriodEnd;     // 콘서트 기간 종료일
    private boolean isActive;        // 콘서트 활성화 상태 (true: 예약 가능, false: 예약 불가)
    
    /**
     * 콘서트 객체를 생성합니다.
     * 
     * @param concertId 콘서트 고유 식별자
     * @param concertName 콘서트 이름
     * @param concertPeriodStart 콘서트 기간 시작일
     * @param concertPeriodEnd 콘서트 기간 종료일
     * 
     * 이유: 새로운 콘서트를 등록할 때 호출됩니다.
     * 생성 시 isActive는 기본적으로 true로 설정되어 예약이 가능한 상태가 됩니다.
     * 실제 공연 정보는 Performance 테이블에서 관리됩니다.
     */
    // 콘서트 객체 생성
    public Concert(String concertId, String concertName, LocalDate concertPeriodStart, LocalDate concertPeriodEnd) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertPeriodStart = concertPeriodStart;
        this.concertPeriodEnd = concertPeriodEnd;
        this.isActive = true;  // 기본적으로 활성화 상태로 생성
    }
    
    /**
     * 콘서트 기간이 예약 가능한지 확인합니다.
     * 
     * @return true: 예약 가능 (오늘 이후), false: 예약 불가 (과거 날짜)
     * 
     * 이유: 사용자가 예약 가능한 콘서트 목록을 조회할 때 사용됩니다.
     * 과거 날짜의 콘서트는 예약할 수 없으므로, 오늘 날짜 이후의 콘서트만 예약 가능합니다.
     * 오늘 날짜도 포함하여 당일 예약도 가능하도록 했습니다.
     */
    // 콘서트 기간 예약 가능 여부 확인
    public boolean isConcertPeriodAvailable() {
        return concertPeriodEnd.isAfter(LocalDate.now()) || concertPeriodEnd.isEqual(LocalDate.now());
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
    
    public LocalDate getConcertPeriodStart() {
        return concertPeriodStart;
    }
    
    public void setConcertPeriodStart(LocalDate concertPeriodStart) {
        this.concertPeriodStart = concertPeriodStart;
    }
    
    public LocalDate getConcertPeriodEnd() {
        return concertPeriodEnd;
    }
    
    public void setConcertPeriodEnd(LocalDate concertPeriodEnd) {
        this.concertPeriodEnd = concertPeriodEnd;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}
