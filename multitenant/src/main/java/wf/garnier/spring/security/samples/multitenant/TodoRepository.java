package wf.garnier.spring.security.samples.multitenant;

import org.springframework.data.repository.ListCrudRepository;

interface TodoRepository extends ListCrudRepository<Todo, Long> {
}
