package br.com.meli.fresh.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "buyers")
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Buyer extends User {

    public Buyer(String id, String name, String email, String password) {
        super.setEmail(email);
        super.setId(id);
        super.setName(name);
        super.setPassword(password);
    }
}