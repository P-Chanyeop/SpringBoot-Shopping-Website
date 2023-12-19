package com.example.shop.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("상품 등록 페이지 권한 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")      // ADMIN 역할을 가진 user객체를 가지고 테스트 수행
    public void itemFormTest() throws Exception{
        // ADMIN 계정으로 admin 권한이 있어야 들어갈 수 있는 상품 등록 페이지 접속
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/item/new"))
                .andDo(print())
                .andExpect(status().isOk());    // status code, 즉 상태 코드가 발생하지 않는다면 테스트 성공
    }


    @Test
    @DisplayName("상품 등록 페이지 일반 회원 접근 테스트")
    @WithMockUser(username = "user", roles = "USER")    // 일반 USER 역할을 가진 user객체를 가지고 테스트 수행
    public void itemFormNotAdminTest() throws Exception{
        // USER 계정으로 admin 권한이 있어야 들어갈 수 있는 상품 등록 페이지 접속
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/item/new"))
                .andDo(print())
                .andExpect(status().isForbidden());     // 상태코드가 403 fobidden 에러, 즉 금지 되어있다는 에러가 발생시 테스트 성공.
    }
}