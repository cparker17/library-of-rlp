package com.parker.rlp;

import com.parker.rlp.models.books.Subject;
import com.parker.rlp.models.users.Address;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.users.Role;
import com.parker.rlp.models.users.User;
import com.parker.rlp.repositories.BookRepository;
import com.parker.rlp.repositories.RoleRepository;
import com.parker.rlp.repositories.SubjectRepository;
import com.parker.rlp.repositories.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.parker.rlp.models.users.Role.Roles.ROLE_ADMIN;
import static com.parker.rlp.models.users.Role.Roles.ROLE_USER;

@SpringBootApplication
public class RlpApplication extends SpringBootServletInitializer {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(RlpApplication.class);
    }

    @Bean
    public CommandLineRunner loadInitialData() {
        return (args) -> {
            if (roleRepository.findAll().isEmpty()) {
                Role USER = new Role(ROLE_USER);
                roleRepository.save(USER);
                Role ADMIN = new Role(ROLE_ADMIN);
                roleRepository.save(ADMIN);
            }

            if (userRepository.findAll().isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .firstName("Rick")
                        .lastName("Parker")
                        .address(Address.builder()
                                .streetAddress("3206 Patapsco Rd")
                                .city("Finksburg")
                                .state("MD")
                                .zip("21048")
                                .build())
                        .email("rk21048@gmail.com")
                        .role(roleRepository.findRoleById(2L))
                        .build();
                userRepository.save(admin);
            }

            if (subjectRepository.findAll().isEmpty()) {
                try {
                    FileInputStream file = new FileInputStream(new File("/Users/chrisparker/Documents/" +
                            "rlp/src/main/resources/static/subjectData.xlsx"));
                    XSSFWorkbook workbook = new XSSFWorkbook(file);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    List<Subject> subjectsToPersist = new ArrayList<>();
                    ArrayList<String> rowData;
                    for (Row row : sheet) {
                        rowData = new ArrayList<>();
                        for (Cell cell : row) {
                            rowData.add(cell.getStringCellValue());
                        }
                        Subject subject = Subject.builder().name(rowData.get(0)).build();
                        subjectsToPersist.add(subject);
                    }
                    subjectRepository.saveAll(subjectsToPersist);
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (bookRepository.findAll().isEmpty()) {
                try {
                    FileInputStream file = new FileInputStream(new File("/Users/chrisparker/Documents/" +
                            "rlp/src/main/resources/static/bookData.xlsx"));
                    XSSFWorkbook workbook = new XSSFWorkbook(file);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    List<Book> booksToPersist = new ArrayList<>();
                    ArrayList<String> rowData;
                    for (Row row : sheet) {
                        rowData = new ArrayList<>();
                        for (Cell cell : row) {
                            rowData.add(cell.getStringCellValue());
                        }
                        Book book = Book.builder()
                                .title(rowData.get(0))
                                .author(rowData.get(1))
                                .isbn(rowData.get(2))
                                .subject(subjectRepository.findByName(rowData.get(3)))
                                .height(Double.valueOf(rowData.get(4))).depth(Double.valueOf(rowData.get(5)))
                                .thickness(Double.valueOf(rowData.get(6)))
                                .imageFile(rowData.get(7))
                                .dateAdded(LocalDate.now())
                                .isCheckedOut(false)
                                .build();
                        booksToPersist.add(book);

                    }
                    bookRepository.saveAll(booksToPersist);
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}