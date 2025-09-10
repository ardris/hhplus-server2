package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.User;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자 정보를 관리하는 인메모리 저장소
 */
public class UserRepository {
    
    private final Map<String, User> userStore = new ConcurrentHashMap<>();
    
    public User save(User user) {
        userStore.put(user.getUserId(), user);
        return user;
    }
    
    public Optional<User> findByUserId(String userId) {
        return Optional.ofNullable(userStore.get(userId));
    }
    
    public User findOrCreateUser(String userId) {
        return userStore.computeIfAbsent(userId, User::new);
    }
    
    public void update(User user) {
        userStore.put(user.getUserId(), user);
    }
    
    public boolean existsByUserId(String userId) {
        return userStore.containsKey(userId);
    }
    
    public void deleteByUserId(String userId) {
        userStore.remove(userId);
    }
}
