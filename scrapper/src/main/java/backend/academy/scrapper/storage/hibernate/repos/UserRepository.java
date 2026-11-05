package backend.academy.scrapper.storage.hibernate.repos;

import backend.academy.scrapper.storage.hibernate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
