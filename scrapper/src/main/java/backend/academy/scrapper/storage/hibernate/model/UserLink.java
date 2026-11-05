package backend.academy.scrapper.storage.hibernate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "links_users",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "link_id"})})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    public UserLink(User user, Link link) {
        this.user = user;
        this.link = link;
    }
}
