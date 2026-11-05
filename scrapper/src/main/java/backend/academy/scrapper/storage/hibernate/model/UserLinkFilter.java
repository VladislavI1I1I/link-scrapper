package backend.academy.scrapper.storage.hibernate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "links_filters",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "link_id", "filter_id"})})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLinkFilter {
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
    @JoinColumn(name = "filter_id", nullable = false)
    private Filter filter;

    public UserLinkFilter(Link link, User user, Filter filter) {
        this.link = link;
        this.user = user;
        this.filter = filter;
    }
}
