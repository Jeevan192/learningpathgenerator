package com.example.learningpathgenerator.repository;

import com.example.learningpathgenerator.entity.GamificationProfile;
import com.example.learningpathgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GamificationProfileRepository extends JpaRepository<GamificationProfile, Long> {
    Optional<GamificationProfile> findByUser(User user);

    @Query("SELECT g FROM GamificationProfile g ORDER BY g.totalPoints DESC")
    List<GamificationProfile> findTopByOrderByTotalPointsDesc(int limit);

    int countByTotalPointsGreaterThan(Integer points);
}