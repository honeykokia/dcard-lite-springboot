package org.example.demo.board.repository;

import org.example.demo.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // findAll(Pageable pageable) is inherited from JpaRepository

}
