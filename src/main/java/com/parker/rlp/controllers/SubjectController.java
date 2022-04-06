package com.parker.rlp.controllers;

import com.parker.rlp.models.books.Subject;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SubjectController {
    @Autowired
    SubjectService subjectService;

    @Autowired
    BookCaseService bookCaseService;

    @RequestMapping("/subject")
    public String addSubject(@ModelAttribute Subject subject) {
        subjectService.addSubject(subject);
        return "redirect:/dashboard";
    }
}
