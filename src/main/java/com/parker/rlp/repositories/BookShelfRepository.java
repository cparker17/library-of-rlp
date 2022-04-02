package com.parker.rlp.repositories;

import com.parker.rlp.models.books.BookShelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookShelfRepository extends JpaRepository<BookShelf, Long> {
}
