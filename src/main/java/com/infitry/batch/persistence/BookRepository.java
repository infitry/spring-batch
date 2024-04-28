package com.infitry.batch.persistence;

import com.infitry.batch.persistence.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Long, BookEntity> {
}
