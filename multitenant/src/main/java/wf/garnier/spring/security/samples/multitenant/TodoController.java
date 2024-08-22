package wf.garnier.spring.security.samples.multitenant;

import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TodoController {

    private final TodoRepository todoRepository;

    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping("")
    public Stream<TodoResponse> listTodos() {
        return todoRepository.findAll().stream().map(t -> new TodoResponse(t.id(), t.text()));
    }


    record TodoResponse(long id, String text) {

    }
}
