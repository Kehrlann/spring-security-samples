package wf.garnier.security.samples.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DemoController {

	private final CompanyRepository companyRepository;

	public DemoController(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@GetMapping("/")
	public String companyDetails(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CustomUser user)) {
			return """
					<h1>Demo app</h1>

					<p>
					You are not logged in. To proceed further, please <a href="/login">log in</a>.
					</p>
					""";
		}

		return """
				<h1>Demo app</h1>

				<p>Welcome. You are [%s], from company [%s].</p>

				<p>Go to:</p>

				<ul>
				<li>Company alpha, index page: <a href="/company/alpha">⛵️ alpha index</a></li>
				<li>Company alpha, admin page: <a href="/company/alpha/admin">⛵️ alpha 🔐 admin</a></li>
				<li>Company omega, index page: <a href="/company/omega">🚅 omega index</a></li>
				<li>Company omega, admin page: <a href="/company/omega/admin">🚅 omega 🔐 admin</a></li>
				</ul>

				<p>
				<a href="/logout">Logout</a>
				</p>
				""".formatted(user.getUsername(), user.getCompany().name());
	}

	@GetMapping("/company/{companyId}")
	public String companyDetails(@PathVariable String companyId) {
		var company = companyRepository.findById(companyId);
		return """
				<h1>%s</h1>
				<p>Here is the company details for %s.</p>
				<p><a href="/company/%s/admin">Admin page</a></p>
				<p><a href="/">Home</a></p>
				""".formatted(company.name(), company.name(), companyId);
	}

	@GetMapping("/company/{companyId}/admin")
	public String companyAdmin(@PathVariable String companyId) {
		var company = companyRepository.findById(companyId);
		return """
				<h1>🔐 Admin for %s</h1>
				<p>This is the admin page for %s.</p>
				<p><a href="/company/%s">Index page</a></p>
				<p><a href="/">Home</a></p>
				""".formatted(company.name(), company.name(), companyId);

	}

}
