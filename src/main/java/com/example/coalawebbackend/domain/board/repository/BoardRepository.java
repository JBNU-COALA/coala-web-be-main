package com.example.coalawebbackend.domain.board.repository;

import com.example.coalawebbackend.domain.board.entity.Board;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {


    @Query("""
    SELECT b
    FROM Board b
    WHERE (:isActive IS NULL OR b.isActive = :isActive)
""")
    List<Board> findByIsActiveCondition(@Param("isActive") Boolean isActive);

}
