package kr.hhplus.be.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class SimpleTest {

    @Test
    void 기본_테스트() {
        // Given
        String message = "Hello World";
        
        // When
        String result = message.toUpperCase();
        
        // Then
        assertEquals("HELLO WORLD", result);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        
        System.out.println("✅ 기본 테스트 성공: " + result);
    }

    @Test
    void 숫자_계산_테스트() {
        // Given
        int a = 10;
        int b = 20;
        
        // When
        int sum = a + b;
        int multiply = a * b;
        
        // Then
        assertEquals(30, sum);
        assertEquals(200, multiply);
        assertTrue(sum > 0);
        assertTrue(multiply > 0);
        
        System.out.println("✅ 숫자 계산 테스트 성공: " + sum + ", " + multiply);
    }

    @Test
    void 리스트_테스트() {
        // Given
        java.util.List<String> list = new java.util.ArrayList<>();
        
        // When
        list.add("첫번째");
        list.add("두번째");
        list.add("세번째");
        
        // Then
        assertEquals(3, list.size());
        assertEquals("첫번째", list.get(0));
        assertEquals("세번째", list.get(2));
        assertFalse(list.isEmpty());
        
        System.out.println("✅ 리스트 테스트 성공: " + list);
    }
}
