package backend.academy.scrapper.storage.hibernate.repos;

import backend.academy.scrapper.storage.hibernate.model.Link;
import backend.academy.scrapper.storage.hibernate.model.User;
import backend.academy.scrapper.storage.hibernate.model.UserLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLinkRepository extends JpaRepository<UserLink, Long> {
    boolean existsByUserAndLink(User user, Link link);

    List<UserLink> findByUser(User user);

    @Modifying
    @Query("DELETE FROM UserLink ul WHERE ul.user = :user AND ul.link = :link")
    int deleteByUserAndLink(@Param("user") User user, @Param("link") Link link);

    @Query("SELECT ul.user.id FROM UserLink ul WHERE ul.link = :link")
    List<Long> findSubscriberIdsByLink(@Param("link") Link link);
}
