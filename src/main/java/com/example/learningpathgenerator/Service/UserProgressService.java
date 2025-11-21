package com.example.learningpathgenerator.Service;

import com.example.learningpathgenerator.entity.UserProgress;
import com.example.learningpathgenerator.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;

    public Optional<UserProgress> getUserProgress(String username) {
        log.info("Fetching progress for user: {}", username);
        return userProgressRepository.findByUsername(username);
    }

    @Transactional
    public UserProgress saveProgress(UserProgress progress) {
        log.info("Saving progress for user: {}", progress.getUsername());

        Optional<UserProgress> existing = userProgressRepository.findByUsername(progress.getUsername());

        if (existing.isPresent()) {
            UserProgress existingProgress = existing.get();
            existingProgress.setCompletedModules(progress.getCompletedModules());
            existingProgress.setCurrentModule(progress.getCurrentModule());
            existingProgress.setOverallProgress(progress.getOverallProgress());
            existingProgress.setTotalModules(progress.getTotalModules());
            return userProgressRepository.save(existingProgress);
        }

        return userProgressRepository.save(progress);
    }

    @Transactional
    public void resetProgress(String username) {
        log.info("Resetting progress for user: {}", username);
        userProgressRepository.deleteByUsername(username);
    }

    public boolean hasProgress(String username) {
        return userProgressRepository.existsByUsername(username);
    }
}