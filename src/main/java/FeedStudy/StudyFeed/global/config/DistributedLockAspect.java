package FeedStudy.StudyFeed.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class DistributedLockAspect {
    
    private final RedissonClient redissonClient;

    @Around("@annotation(lockAnn)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributeLock lockAnn) throws Throwable {
        String lockName = buildKey(joinPoint, lockAnn);
        RLock lock = redissonClient.getLock(lockName);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(lockAnn.waitTime(), lockAnn.leaseTime(), lockAnn.timeUnit());
            if(!acquired) {
                log.warn("락 획득 실패: {}", lockName);
                throw new IllegalStateException("Failed to acquire lock: " + lockName);
            }
            log.info("✅ 락 획득 성공: {}", lockName);
            return joinPoint.proceed();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("🔓 락 해제 완료: {}", lockName);
            }
        }
    }

    private String buildKey(ProceedingJoinPoint pjp, DistributeLock ann) {
        Object[] args = pjp.getArgs();
        int idx = ann.argIndex();
        String prefix = ann.keyPrefix();

        if (idx < 0 || idx >= args.length) {
            throw new IllegalArgumentException("@DistributedLock argIndex 범위 오류");
        }
        Object v = args[idx];
        if (v == null) {
            throw new IllegalArgumentException("@DistributedLock 대상 파라미터가 null 입니다.");
        }

        String methodName = ((MethodSignature) pjp.getSignature()).getMethod().getName();
        String key = prefix + v;

        // 디버깅용 로그
        log.debug("Acquire lock. method={}, key={}", methodName, key);
        return key;
    }


}
