package com.yeonhx03.marketbriefapi.briefing.persistence;

import com.yeonhx03.marketbriefapi.briefing.domain.Briefing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BriefingRepository extends JpaRepository<Briefing, Long> {

    Optional<Briefing> findFirstByOrderByCreatedAtDescIdDesc();
}
