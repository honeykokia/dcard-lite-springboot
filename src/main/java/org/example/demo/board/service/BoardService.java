package org.example.demo.board.service;

import java.util.List;
import java.util.stream.Collectors;

import org.example.demo.board.dto.BoardItem;
import org.example.demo.board.dto.ListBoardsResponse;
import org.example.demo.board.entity.Board;
import org.example.demo.board.repository.BoardRepository;
import org.example.demo.common.exception.InternalErrorException;
import org.example.demo.user.exception.ValidationFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public ListBoardsResponse listBoards(int page, int pageSize, String keyword) {
        if (page < 1) {
            throw new ValidationFailedException("PAGE_INVALID");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new ValidationFailedException("PAGE_SIZE_INVALID");
        }
        String kw = null;
        if (keyword != null) {
            kw = keyword.trim();
            if (kw.isBlank()) {
                kw = null;
            } else if (kw.length() > 50) {
                throw new ValidationFailedException("KEYWORD_INVALID");
            }
        }

        try {
            PageRequest pr = PageRequest.of(page - 1, pageSize);
            Page<Board> pageRes;
            if (kw != null) {
                pageRes = boardRepository.findByNameContainingIgnoreCase(kw, pr);
            } else {
                pageRes = boardRepository.findAll(pr);
            }

            List<BoardItem> items = pageRes.getContent().stream()
                    .map(b -> new BoardItem(b.getBoardId(), b.getName(), b.getDescription()))
                    .collect(Collectors.toList());

            return new ListBoardsResponse(page, pageSize, pageRes.getTotalElements(), items);
        } catch (DataAccessException ex) {
            throw new InternalErrorException();
        }
    }
}
