package com.parker.rlp.services.impl;

import com.parker.rlp.models.books.Subject;
import com.parker.rlp.repositories.SubjectRepository;
import com.parker.rlp.services.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {
    @Autowired
    SubjectRepository subjectRepository;

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }
}
