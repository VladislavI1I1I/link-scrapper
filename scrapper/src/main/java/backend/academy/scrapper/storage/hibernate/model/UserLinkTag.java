package backend.academy.scrapper.storage.hibernate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "links_tags",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "link_id", "tag_id"})})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLinkTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public UserLinkTag(Link link, User user, Tag tag) {
        this.link = link;
        this.user = user;
        this.tag = tag;
    }
}
