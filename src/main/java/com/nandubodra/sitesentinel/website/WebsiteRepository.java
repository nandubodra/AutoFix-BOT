package com.nandubodra.sitesentinel.website;

import com.nandubodra.sitesentinel.auth.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WebsiteRepository extends JpaRepository<Website, Long> {
    List<Website> findByOwnerOrderByCreatedAtDesc(UserAccount owner);
    Optional<Website> findByIdAndOwner(Long id, UserAccount owner);

    @Query("""
            select w from Website w
            where w.monitoringEnabled = true
              and w.verified = true
              and (w.claimUntil is null or w.claimUntil < :now)
              and w.nextCheckAt <= :now
            order by w.nextCheckAt asc
            """)
    List<Website> findDueCandidates(@Param("now") Instant now, Pageable pageable);

    @Modifying
    @Query("""
            update Website w
            set w.claimUntil = :claimUntil
            where w.id = :websiteId
              and w.monitoringEnabled = true
              and w.verified = true
              and (w.claimUntil is null or w.claimUntil < :now)
              and w.nextCheckAt <= :now
            """)
    int claimIfDue(@Param("websiteId") Long websiteId, @Param("now") Instant now, @Param("claimUntil") Instant claimUntil);

    @Modifying
    @Query("update Website w set w.claimUntil = null where w.id = :websiteId")
    void releaseClaim(@Param("websiteId") Long websiteId);

    Page<Website> findByOwner(UserAccount owner, Pageable pageable);
}
