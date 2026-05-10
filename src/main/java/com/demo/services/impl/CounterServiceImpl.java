package com.demo.services.impl;

import com.demo.entities.Counter;
import com.demo.repositories.CounterRepository;
import com.demo.services.CounterService;
import com.demo.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounterServiceImpl implements CounterService {
    private final CounterRepository counterRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    //Initialize counters
    @EventListener(ApplicationReadyEvent.class)
    public void initializeGlobalCounter() {
        initializeCounter(AppConstants.COUNTER_KEY);
    }

    @Override
    public Long getCounter(String key) {
        //Get counter from redis or from database
        Object cached = redisTemplate.opsForValue().get(key);
        if(cached != null){
            return Long.parseLong(cached.toString());
        }

        Long counter = counterRepository.findValueByName(key);
        redisTemplate.opsForValue().set(key, counter);

        return counter;
    }

    @Override
    @Transactional
    public Long incrementCounter(String key) {
        // Get counter from Redis.
        Long currentCounter = redisTemplate.opsForValue().increment(key);

        // if Redis was empty and started from 1, restore from DB first
        if (currentCounter == 1) {
            Long dbCounter = counterRepository.findValueByName(key);
            if (dbCounter != null && dbCounter > 1) {
                // Redis was reset, restore correct value
                redisTemplate.opsForValue().set(key, dbCounter + 1);
                currentCounter = dbCounter + 1;
            }
        }

        setCounterInDatabase(key, currentCounter);
        return currentCounter;
    }

    //-------------------------- Helper methods --------------------------------------
    private void initializeCounter(String key){
        //Initialize cache from DB.
        Long DBCounter = counterRepository.findValueByName(key);
        if(DBCounter == null){
            counterRepository.save(new Counter(key, 0L));
            DBCounter = 0L;
        }

        //Update redis with the value in database.
        redisTemplate.opsForValue().set(key, DBCounter);
    }

    private void setCounterInDatabase(String key, Long counter) {
        counterRepository.updateValueByName(key, counter);
    }
}
