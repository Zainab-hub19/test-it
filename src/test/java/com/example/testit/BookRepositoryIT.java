package com.example.testit;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;


@DataJpaTest
public class BookRepositoryIT {

    @Autowired TestEntityManager em;
    @Autowired BookRepository bookRepository;


    private Book livre(String titre, Status status, Member emprunteur) {
        Book b = new Book(titre, "auteur");
        b.setStatus(status);
        b.setBorrower(emprunteur);
        return em.persist(b);
    }

    @Test
    void findByMemberAndStatusNeRetourneQueLesLivresDuMembreAvecCeStatut() {
        // Given : deux membres et plusieurs livres
        Member alice = em.persist(new Member("Zai"));
        Member bob = em.persist(new Member("Ka"));
        Book aliceEnCours = livre("A1", Status.EMPRUNTE, alice);
        livre("A2", Status.RENDU, alice);
        livre("B1", Status.EMPRUNTE, bob);
        livre("Libre", Status.DISPONIBLE, null);
        em.flush();
        em.clear();

        // When
        List<Book> result = bookRepository.findByMemberAndStatus(alice.getId(), Status.EMPRUNTE);

        // Then : seulement A1
        assertThat(result).extracting(Book::getId).containsExactly(aliceEnCours.getId());
    }


}
