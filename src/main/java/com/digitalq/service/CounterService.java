package com.digitalq.service;

import com.digitalq.model.Counter;
import com.digitalq.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;

    public List<Counter> findAll() {
        return counterRepository.findAll();
    }

    public List<Counter> findActive() {
        return counterRepository.findByActiveTrue();
    }

    public Counter findById(Long id) {
        return counterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + id));
    }

    @Transactional
    public Counter createCounter(String name, String serviceType) {
        if (counterRepository.existsByName(name)) {
            throw new IllegalArgumentException("Counter name already exists: " + name);
        }

        Counter counter = Counter.builder()
                .name(name)
                .serviceType(serviceType)
                .active(true)
                .paused(false)
                .build();

        return counterRepository.save(counter);
    }

    @Transactional
    public Counter updateCounter(Long id, String name, String serviceType) {
        Counter counter = findById(id);
        counter.setName(name);
        counter.setServiceType(serviceType);
        return counterRepository.save(counter);
    }

    @Transactional
    public void deactivateCounter(Long id) {
        Counter counter = findById(id);
        counter.setActive(false);
        counterRepository.save(counter);
    }

    @Transactional
    public void activateCounter(Long id) {
        Counter counter = findById(id);
        counter.setActive(true);
        counterRepository.save(counter);
    }

    @Transactional
    public void pauseCounter(Long id) {
        Counter counter = findById(id);
        counter.setPaused(true);
        counterRepository.save(counter);
    }

    @Transactional
    public void resumeCounter(Long id) {
        Counter counter = findById(id);
        counter.setPaused(false);
        counterRepository.save(counter);
    }

    @Transactional
    public void setCurrentToken(Long id, String tokenNumber) {
        Counter counter = findById(id);
        counter.setCurrentToken(tokenNumber);
        counterRepository.save(counter);
    }
}
