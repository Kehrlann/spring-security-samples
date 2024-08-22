package wf.garnier.spring.security.samples.multitenant;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class WelcomeController {

	private int port = 8080;

	@GetMapping("/")
	String index() {
		return """
				<h1>Todo application</h1>
				<p><a href="http://red.127.0.0.1.nip.io:%s/todo">Go to red</a></p>
				<p><a href="http://black.127.0.0.1.nip.io:%s/todo">Go to black</a></p>
				<p><a href="/logout">Logout</a></p>
				<h2>Some interesting curl commands:</h2>

				<p>Red user on red tenant, works</p>
				<pre>
				curl -vw "\\n" http://red.127.0.0.1.nip.io:8080/todo --user red-user:password
				</pre>

				<p>Black user on black tenant, works</p>
				<pre>
				curl -vw "\\n" http://black.127.0.0.1.nip.io:8080/todo --user black-user:password
				</pre>

				<p>Red user on black tenant, fails because there is no red-user in the black schema</p>
				<pre>
				curl -vw "\\n" http://black.127.0.0.1.nip.io:8080/todo --user red-user:password
				</pre>

				<p>Black user on red tenant, fails because there is no red-user in the red schema</p>
				<pre>
				curl -vw "\\n" http://red.127.0.0.1.nip.io:8080/todo --user black-user:password
				</pre>
				""".formatted(port, port);
	}

	@EventListener
	void onApplicationEvent(final ServletWebServerInitializedEvent event) {
		this.port = event.getWebServer().getPort();
	}

}
