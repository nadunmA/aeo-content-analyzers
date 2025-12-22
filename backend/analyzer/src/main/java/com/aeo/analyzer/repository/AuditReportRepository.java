package com.aeo.analyzer.repository;

import com.aeo.analyzer.model.AuditReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditReportRepository extends MongoRepository<AuditReport, String> {

    //Pagination - memory efficient
    Page<AuditReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<AuditReport> findAllByOrderByCreatedAtDesc();

    //Security user own reports only
    List<AuditReport> findByUserIdOrderByCreatedAtDesc(String userId);

    //User reports with pagination
    Page<AuditReport> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    //Filter type
    List<AuditReport> findByTypeOrderByCreatedAtDesc(String type);

    //Date range query
    List<AuditReport> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    //Rate limiting - count user reports
    long countByUserId(String userId);

    //Rate limiting - count user recent reports
    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);

    //Add @Transactional for delete operations
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime date);

    //custom query
    @Query("{ 'userId': ?0, 'status': 'completed' }")
    List<AuditReport> findCompletedReportsByUser(String userId);

    //delete by user id
    @Transactional
    void deleteByUserId(String userId);

    //find recent reports
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<AuditReport> findRecentReports(LocalDateTime since);

    //count by status
    long countByStatus(String status);

    //find by status
    List<AuditReport> findByStatusOrderByCreatedAtDesc(String status);

}
