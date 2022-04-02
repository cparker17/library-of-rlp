package com.parker.rlp.models;

import com.parker.rlp.models.books.Book;
import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank(message = "Username required.")
    @Column(unique = true)
    private String username;

    @NotNull
    @NotBlank(message = "Password required.")
    private String password;

    @NotNull
    @NotBlank(message = "First name required.")
    private String firstName;

    @NotNull
    @NotBlank(message = "Last name required.")
    private String lastName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_address_id")
    private Address address;

    @Column(unique = true)
    @NotBlank(message = "Email required.")
    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private List<Book> books;

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<RentalHistory> rentalHistoryList;

    public void addBook(Book book) {
        if (books.isEmpty()) {
            books = new ArrayList<>();
        }
        books.add(book);
    }

    @ManyToOne(cascade = CascadeType.MERGE)
    private Role role;

    @Column(nullable = false)
    private boolean isAccountNonExpired = true;

    @Column(nullable = false)
    private boolean isAccountNonLocked = true;

    @Column(nullable = false)
    private boolean isCredentialsNonExpired = true;

    @Column(nullable = false)
    private boolean isEnabled = true;

    public void addRentalHistory(RentalHistory rentalHistory) {
        if (rentalHistoryList == null) {
            rentalHistoryList = new ArrayList<>();
        } else {
            rentalHistoryList.add(rentalHistory);
        }
    }
}

