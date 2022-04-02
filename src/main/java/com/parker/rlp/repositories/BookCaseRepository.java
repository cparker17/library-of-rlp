package com.parker.rlp.repositories;

import com.parker.rlp.models.books.BookCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookCaseRepository extends JpaRepository<BookCase, Long> {
}
