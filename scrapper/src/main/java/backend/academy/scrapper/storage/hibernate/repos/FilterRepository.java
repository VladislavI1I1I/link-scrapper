package backend.academy.scrapper.storage.hibernate.repos;

import backend.academy.scrapper.storage.hibernate.model.Filter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    Optional<Filter> findByFilter(String filter);
}
