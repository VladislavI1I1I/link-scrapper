package backend.academy.scrapper.storage.hibernate.repos;

import backend.academy.scrapper.storage.hibernate.model.Link;
import backend.academy.scrapper.storage.hibernate.model.Tag;
import backend.academy.scrapper.storage.hibernate.model.User;
import backend.academy.scrapper.storage.hibernate.model.UserLinkTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLinkTagRepository extends JpaRepository<UserLinkTag, Long> {
    @Query("SELECT t.tag FROM UserLinkTag ult JOIN ult.tag t WHERE ult.user = :user AND ult.link = :link")
    Optional<String> findTagByUserAndLink(@Param("user") User user, @Param("link") Link link);

    boolean existsByUserAndLinkAndTag(User user, Link link, Tag tag);
}
