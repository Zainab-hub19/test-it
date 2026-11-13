Prénom : Zainab
Nom : Karim
## Bug 1 : borrowBook cherche les mauvais emprunts en cours
- **Test qui l'a révélé** : BookServiceTest.emprunteUnLivreDisponibleEtPrevientLeMembre
- **Message** : PotentialStubbingProblem (le test stubbait findByMemberAndStatus(10, EMPRUNTE), le service appelait avec RENDU)
- **Cause** : dans BookService.borrowBook, la vérification utilisait Status.RENDU au lieu de Status.EMPRUNTE
- **Code ou test ?** Le code. Le README dit qu'on refuse l'emprunt si le membre a déjà un livre EMPRUNTE.

Bug 3 — XML Liquibase invalide (SAXParseException)

    Trace : SAXParseException ← XMLChangeLogSAXParser au démarrage de Spring.

    Cause : Attribut obligatoire constraintName manquant sur <addForeignKeyConstraint>.

    Correction : Ajout de constraintName="fk_members_referent" dans 001-create-members-table.xml et constraintName="fk_book_borrower" dans 002-create-book-table.xml.

Bug 3 — Types et noms incompatibles en SQL (JdbcSQLSyntaxErrorException)

    Trace : Values of types "UUID" and "BIGINT" are not comparable sur referent_id.

    Cause : Dans 001-create-members-table.xml, referent_id était en uuid alors que members.id est en bigint, et la colonne user (mot-clé réservé SQL) ne correspondait pas à username dans Member.

    Correction : Passage de referent_id à bigint et renommage de user en username dans 001 (et correction du nom de contrainte dupliqué dans 002).

Bug 4 — Désynchronisation JPA / Base (SchemaManagementException)

    Trace : Schema validation: missing table [member].

    Cause : Liquibase crée la table members avec autoIncrement, tandis que Member cherchait member et Book utilisait GenerationType.SEQUENCE.

    Correction : Ajout de @Table(name = "members") sur Member.java et passage à GenerationType.IDENTITY sur Book.id.

### Bug 5 — Session Hibernate fermée dans `BookService` (`LazyInitializationException`)
* **Trace :** `org.hibernate.LazyInitializationException` lors de l'appel à `BookService.returnBook` (ligne 49 de `BookServiceIT`).
* **Cause :** Dans `Book.java` et `Member.java`, les relations `borrower` et `referent` sont en `FetchType.LAZY`. Comme `BookService` n'a pas d'annotation `@Transactional`, la session Hibernate se ferme immédiatement après `bookRepository.findById(bookId)`, empêchant le chargement de `member.getReferent()`.
* **Correction :** Ajout de `@Transactional` (et de l'import `org.springframework.transaction.annotation.Transactional`) au-dessus de la classe `BookService` dans `src/main/java/com/example/testit/service/BookService.java`.

Bug 6 — Bugs dans BookController (AssertionError)

    Trace : AssertionError sur $.author (puis status().isOk() lors de l'emprunt) dans BookControllerIT.

    Cause : createBook passait (request.title, request.title) au lieu de request.author, et POST /books/{id}/borrow appelait bookService.returnBook(...) au lieu de bookService.borrowBook(...).

    Correction : Passage de request.author dans createBook et appel à bookService.borrowBook(id, memberId) dans borrowBook.

Bug 7 — Erreur 500 lors de la sérialisation JSON d'un livre emprunté (GET /books/{id})

    Trace : Status expected:<200> but was:<500> à la ligne 56 de BookControllerIT.

    Cause : Une fois le livre emprunté, Book.borrower est un proxy Hibernate FetchType.LAZY (avec une relation bidirectionnelle vers Member.books), ce qui fait échouer la sérialisation Jackson en JSON hors transaction.

    Correction : Ajout de @JsonIgnore (ou @JsonIgnoreProperties) sur Book.borrower dans Book.java.

### Bug 8 — Anomalies dans `BookService.borrowBook` (`PotentialStubbingProblem` & vérification de statut)
* **Trace :** `PotentialStubbingProblem` à la ligne 55 de `BookServiceTest` lors de l'appel à `findByMemberAndStatus` avec `Status.RENDU`.
* **Cause :** Trois erreurs dans `BookService.borrowBook` :
  1. La vérification de disponibilité testait `book.getStatus() == Status.EMPRUNTE` au lieu de `book.getStatus() != Status.DISPONIBLE` (permettant d'emprunter un livre encore au statut `RENDU`).
  2. La recherche des emprunts en cours filtrait sur `Status.RENDU` au lieu de `Status.EMPRUNTE`.
  3. L'envoi de la notification par e-mail via `mailService.sendMail(...)` après l'emprunt était manquant.
* **Correction :** Remplacement de la condition par `book.getStatus() != Status.DISPONIBLE`, interrogation de `findByMemberAndStatus(memberId, Status.EMPRUNTE)`, et ajout de l'appel à `mailService.sendMail(...)` avant `return saved;`.