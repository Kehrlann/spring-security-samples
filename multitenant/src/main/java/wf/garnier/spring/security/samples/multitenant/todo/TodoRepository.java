package wf.garnier.spring.security.samples.multitenant.todo;

import org.springframework.data.repository.ListCrudRepository;

interface TodoRepository extends ListCrudRepository<Todo, Long> {

}
