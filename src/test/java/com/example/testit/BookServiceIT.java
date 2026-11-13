package com.example.testit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import com.example.testit.service.BookService;
import org.assertj.core.api.Assertions;


@SpringBootTest
public class BookServiceIT {

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void nettoyer() {
        bookRepository.deleteAll();   // les livres d'abord (clé étrangère vers members)
        memberRepository.deleteAll();
    }

    @Test
    void emprunterPuisRendreUnLivre() {
        // given : un membre et un livre disponible
        var alice = memberRepository.save(new Member("Zai"));
        var book = bookService.createBook("Dune", "Ka");

        // when : emprunt, puis on relit en base
        bookService.borrowBook(book.getId(), alice.getId());
        var apresEmprunt = bookRepository.findById(book.getId()).orElseThrow();

        // then (1) : le livre est EMPRUNTE en base
        Assertions.assertThat(apresEmprunt.getStatus()).isEqualTo(Status.EMPRUNTE);
        Assertions.assertThat(bookService.findByMemberId(alice.getId())).hasSize(1);

        // when : retour, puis on relit en base
        bookService.returnBook(book.getId(), alice.getId());
        var apresRetour = bookRepository.findById(book.getId()).orElseThrow();

        // then (2) : le livre est RENDU en base
        Assertions.assertThat(apresRetour.getStatus()).isEqualTo(Status.RENDU);
    }




}
