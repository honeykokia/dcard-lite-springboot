package org.example.demo.board.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "boards",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_boards_name", columnNames = {"name"})
        },
        indexes = {
                @Index(name = "idx_boards_name", columnList = "name")
        }
)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false, updatable = false)
    private Long boardId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Board() {
        // for JPA
    }

    public Board(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.name != null) {
            this.name = this.name.trim();
        }
        if (this.description != null) {
            this.description = this.description.trim();
        }
    }

    public Long getBoardId() {
        return boardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
