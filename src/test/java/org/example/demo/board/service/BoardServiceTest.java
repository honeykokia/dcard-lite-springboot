package org.example.demo.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.example.demo.board.dto.ListBoardsResponse;
import org.example.demo.board.entity.Board;
import org.example.demo.board.repository.BoardRepository;
import org.example.demo.user.exception.ValidationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class BoardServiceTest {

    private BoardRepository boardRepository;
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardRepository = mock(BoardRepository.class);
        boardService = new BoardService(boardRepository);
    }

    @Test
    void uc01_listBoards_success_no_keyword() {
        Board a = new Board("原神", "desc1");
        Board b = new Board("程式", "desc2");
        Page<Board> page = new PageImpl<>(List.of(a, b), PageRequest.of(0, 20), 2);
        when(boardRepository.findAll(any(PageRequest.class))).thenReturn(page);

        ListBoardsResponse res = boardService.listBoards(1, 20, null);

        assertEquals(1, res.getPage());
        assertEquals(20, res.getPageSize());
        assertEquals(2, res.getTotal());
        assertEquals(2, res.getItems().size());
    }

    @Test
    void uc04_page_invalid_throws_validation() {
        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> boardService.listBoards(0, 20, null));
        assertEquals("PAGE_INVALID", ex.getCode());
    }

    @Test
    void uc05_pageSize_invalid_throws_validation() {
        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> boardService.listBoards(1, 101, null));
        assertEquals("PAGE_SIZE_INVALID", ex.getCode());
    }

    @Test
    void uc06_repository_throws_internal_error() {
        when(boardRepository.findAll(any(PageRequest.class))).thenThrow(mock(DataAccessException.class));
        assertThrows(RuntimeException.class, () -> boardService.listBoards(1, 20, null));
    }
}
