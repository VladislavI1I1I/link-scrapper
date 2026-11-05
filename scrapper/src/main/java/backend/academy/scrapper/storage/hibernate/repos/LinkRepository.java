package backend.academy.scrapper.storage.hibernate.repos;

import backend.academy.scrapper.storage.hibernate.model.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUrl(String url);

    @Query("SELECT l.url FROM Link l WHERE l.id > :lastId ORDER BY l.id ASC")
    List<String> findUrlsByIdGreaterThan(Long lastId, Pageable pageable);

    @Query("SELECT l.url FROM Link l ORDER BY l.id ASC")
    List<String> findAllUrls(Pageable pageable);
}
