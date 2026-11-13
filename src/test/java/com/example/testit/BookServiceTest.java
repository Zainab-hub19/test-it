package com.example.testit;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import com.example.testit.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock BookRepository bookRepository;
    @Mock MemberRepository memberRepository;
    @Mock MailService mailService;
    @InjectMocks BookService bookService;

    private Book livre(Long id, Status status) {
    Book b = new Book("Antigone", "JSP");
    b.setId(id);
    b.setStatus(status);
    return b;
}

    private Member membre(Long id, String nom) {
    Member m = new Member(nom);
    m.setId(id);
    return m;
    }

    @Test
    void emprunteUnLivreDisponibleEtPrevientLeMembre() {
        // Given : un livre DISPONIBLE, un membre, aucun emprunt en cours
        Book book = livre(1L, Status.DISPONIBLE);
        Member alice = membre(10L, "zai");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(alice));
        when(bookRepository.findByMemberAndStatus(10L, Status.EMPRUNTE)).thenReturn(List.of());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Book result = bookService.borrowBook(1L, 10L);

        // Then
        assertThat(result.getStatus()).isEqualTo(Status.EMPRUNTE);
        assertThat(result.getBorrower()).isSameAs(alice);
        verify(mailService).sendMail(eq(alice), anyString(), anyString());
        verifyNoMoreInteractions(mailService);
    }
}


