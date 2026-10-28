package com.nandubodra.sitesentinel.incident;

import com.nandubodra.sitesentinel.auth.UserAccount;
import com.nandubodra.sitesentinel.website.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findFirstByWebsiteAndStatusInAndFingerprintOrderByLastSeenAtDesc(
            Website website, List<IncidentStatus> status, String fingerprint);

    Page<Incident> findByWebsiteOwnerOrderByLastSeenAtDesc(UserAccount owner, Pageable pageable);

    Optional<Incident> findByIdAndWebsiteOwner(Long id, UserAccount owner);

    long countByWebsiteOwnerAndStatusIn(UserAccount owner, List<IncidentStatus> status);
}
