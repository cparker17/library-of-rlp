package com.parker.rlp.models;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @NotBlank(message = "Street address required.")
    private String streetAddress;

    @NotNull
    @NotBlank(message = "City required.")
    private String city;

    @NotNull
    @NotBlank(message = "State required.")
    private String state;

    @NotNull
    @NotBlank(message = "Zip code required.")
    private String zip;
}
