package com.example.testit;

import com.example.testit.adapter.member.CurrentMemberServiceFake;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import com.example.testit.model.Member;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIT {

    @Autowired MockMvc mvc;
    @Autowired CurrentMemberServiceFake currentMember;
    @Autowired BookRepository bookRepository;
    @Autowired MemberRepository memberRepository;

    Member alice;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        alice = memberRepository.save(new Member("alice"));
    }

    @Test
    void creerUnLivrePuisLEmprunter() throws Exception {
        // given / when : création du livre
        String json = mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Dune\",\"author\":\"Herbert\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.author").value("Herbert"))
                .andExpect(jsonPath("$.status").value("DISPONIBLE"))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.id")).longValue();

        // when : alice est connectée et emprunte
        currentMember.setCurrent(alice.getId());
        mvc.perform(post("/books/{id}/borrow", id))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMPRUNTE"));

        // then : on relit le livre
        mvc.perform(get("/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMPRUNTE"));

        // then : un second emprunt est refusé
        mvc.perform(post("/books/{id}/borrow", id))
                .andExpect(status().isBadRequest());
    }
}