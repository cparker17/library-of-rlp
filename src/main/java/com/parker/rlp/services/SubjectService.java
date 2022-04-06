package com.parker.rlp.services;

import com.parker.rlp.models.books.Subject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SubjectService {
    List<Subject> getAllSubjects();
    void addSubject(Subject subject);
}
