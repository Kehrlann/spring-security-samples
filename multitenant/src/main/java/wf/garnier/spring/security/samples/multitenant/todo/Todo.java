package wf.garnier.spring.security.samples.multitenant.todo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String text;

	public Todo() {

	}

	public Long id() {
		return id;
	}

	public String text() {
		return text;
	}

}
