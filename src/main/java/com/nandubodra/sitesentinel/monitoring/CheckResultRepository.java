package com.nandubodra.sitesentinel.monitoring;

import com.nandubodra.sitesentinel.website.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    Optional<CheckResult> findTopByWebsiteOrderByCheckedAtDesc(Website website);
    List<CheckResult> findTop20ByWebsiteOrderByCheckedAtDesc(Website website);
    Page<CheckResult> findByWebsiteOrderByCheckedAtDesc(Website website, Pageable pageable);

    List<CheckResult> findByWebsiteAndCheckedAtBetween(Website website, Instant start, Instant end);
}
