package org.jan.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingHistoryRepository extends JpaRepository<RatingHistory, Long> {
    /**
     * Last {@code pageable.getPageSize()} data points for the given player, oldest first.
     * Use {@code PageRequest.of(0, 100)} for the sparkline chart.
     */
    @Query("SELECT r FROM RatingHistory r WHERE r.user = :user ORDER BY r.recordedAt ASC")
    List<RatingHistory> findByUserOrderByRecordedAtAsc(
            @Param("user") User user, Pageable pageable);
}
