package backend.academy.scrapper.storage.hibernate.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_ids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
}
