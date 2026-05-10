package com.demo.services;

public interface CounterService {
    Long getCounter(String key);
    Long incrementCounter(String key);
}
