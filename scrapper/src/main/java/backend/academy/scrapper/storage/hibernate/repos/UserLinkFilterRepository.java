package backend.academy.scrapper.storage.hibernate.repos;

import backend.academy.scrapper.storage.hibernate.model.Filter;
import backend.academy.scrapper.storage.hibernate.model.Link;
import backend.academy.scrapper.storage.hibernate.model.User;
import backend.academy.scrapper.storage.hibernate.model.UserLinkFilter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLinkFilterRepository extends JpaRepository<UserLinkFilter, Long> {

    @Query("SELECT f.filter FROM UserLinkFilter ulf JOIN ulf.filter f WHERE ulf.user = :user AND ulf.link = :link")
    List<String> findFiltersByUserAndLink(@Param("user") User user, @Param("link") Link link);

    boolean existsByUserAndLinkAndFilter(User user, Link link, Filter filter);
}
