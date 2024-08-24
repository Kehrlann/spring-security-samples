package wf.garnier.spring.security.samples.multitenant.todo;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import wf.garnier.spring.security.samples.multitenant.authentication.User;

@RestController
class TodoController {

	private final TodoRepository todoRepository;

	public TodoController(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	@GetMapping({ "/todo", "/secured/todo" })
	public String listTodos(HttpServletRequest request, @AuthenticationPrincipal User user) {
		var todos = todoRepository.findAll()
			.stream()
			.map(t -> "<li>%s</li>".formatted(t.text()))
			.collect(Collectors.joining("\n"));
		return """
				<h1>Todo list</h1>
				<p>Current domain: <strong>%s</strong></p>
				<p>Logged in as: <strong>%s</strong></p>
				<p>Session ID: <strong>%s</strong></p>
				<ul>
				    %s
				</ul>
				<p><a href="/">Go to index</a></p>
				<br><br>
				<p><em>PSA: don't display sensitive credentials, like the session ID, on your webpage.</em></p>
				""".formatted(request.getServerName(), user.getUsername(), request.getSession().getId(), todos);
	}

}
