package wf.garnier.spring.security.samples.multitenant.authentication;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByUsername(String username);

}
