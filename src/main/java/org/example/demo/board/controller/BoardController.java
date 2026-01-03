package org.example.demo.board.controller;

import org.example.demo.board.dto.ListBoardsResponse;
import org.example.demo.board.service.BoardService;
import org.example.demo.user.exception.ValidationFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/boards")
    public ResponseEntity<ListBoardsResponse> listBoards(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        // parameter validation at controller boundary so service may not be invoked on invalid input
        if (page < 1) {
            throw new ValidationFailedException("PAGE_INVALID");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new ValidationFailedException("PAGE_SIZE_INVALID");
        }
        if (keyword != null) {
            String trimmed = keyword.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 50) {
                throw new ValidationFailedException("KEYWORD_INVALID");
            }
        }

        ListBoardsResponse res = boardService.listBoards(page, pageSize, keyword);
        return ResponseEntity.ok(res);
    }
}
