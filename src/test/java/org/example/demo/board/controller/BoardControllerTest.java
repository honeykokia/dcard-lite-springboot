package org.example.demo.board.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.example.demo.board.dto.BoardItem;
import org.example.demo.board.dto.ListBoardsResponse;
import org.example.demo.board.service.BoardService;
import org.example.demo.common.api.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BoardController.class)
@Import({GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @Test
    void ct01_getBoards_success_returns_200() throws Exception {
        ListBoardsResponse res = new ListBoardsResponse(1, 20, 2, List.of(
                new BoardItem(1L, "原神", "desc"),
                new BoardItem(2L, "程式", "desc2")
        ));
        when(boardService.listBoards(anyInt(), anyInt(), nullable(String.class))).thenReturn(res);

        mockMvc.perform(get("/boards").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void ct03_page_zero_returns_400_page_invalid() throws Exception {
        mockMvc.perform(get("/boards?page=0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("PAGE_INVALID"))
                .andExpect(jsonPath("$.path").value("/boards"));
    }
}
