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
				<p><a href="http://blue.127.0.0.1.nip.io:%s/todo">Go to blue</a></p>
				<p><a href="/logout">Logout</a></p>
				<h2>Some interesting curl commands:</h2>

				<p>Red user on red tenant, works</p>
				<pre>
				curl -vw "\\n" http://red.127.0.0.1.nip.io:8080/todo --user red-user:password
				</pre>

				<p>Blue user on blue tenant, works</p>
				<pre>
				curl -vw "\\n" http://blue.127.0.0.1.nip.io:8080/todo --user blue-user:password
				</pre>

				<p>Red user on blue tenant, fails because there is no red-user in the blue schema</p>
				<pre>
				curl -vw "\\n" http://blue.127.0.0.1.nip.io:8080/todo --user red-user:password
				</pre>

				<p>Blue user on red tenant, fails because there is no red-user in the red schema</p>
				<pre>
				curl -vw "\\n" http://red.127.0.0.1.nip.io:8080/todo --user blue-user:password
				</pre>
				""".formatted(port, port);
	}

	@EventListener
	void onApplicationEvent(final ServletWebServerInitializedEvent event) {
		this.port = event.getWebServer().getPort();
	}

}
