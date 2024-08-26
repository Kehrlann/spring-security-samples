package wf.garnier.spring.security.samples.multitenant;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import wf.garnier.spring.security.samples.multitenant.authentication.User;

class TenantVerificationFilter extends OncePerRequestFilter {

	private final boolean enforceTenant;

	// Used to verify the user is logged in when making this request
	private final AuthenticationTrustResolverImpl trustResolver = new AuthenticationTrustResolverImpl();

	public TenantVerificationFilter(boolean enforceTenant) {
		this.enforceTenant = enforceTenant;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!trustResolver.isAuthenticated(authentication)) {
			// If the user is not logged in, we cannot know the tenant for which this
			// connection is authorized, as we have no user info, and no tenant ID.
			// Security is delegated to the rest of the filter chain, which will check
			// whether the user needs to be authenticated to access this page.
			// This is useful for anonymous access to a public page.
			filterChain.doFilter(request, response);
			return;
		}

		if (!(authentication.getPrincipal() instanceof User user)) {
			// Should never happen, we only have the `User` type for our users.
			throw new ServletException("This should never happen");
		}

		var subdomain = request.getServerName().split("\\.")[0];
		if (!subdomain.equals(user.getTenantId())) {
			var errorMessage = "Invalid tenant, trying to access [%s] but user [%s] is in tenant [%s]"
				.formatted(subdomain, user.getUsername(), user.getTenantId());
			System.out.println("~~~~~~~~> " + errorMessage);
			if (this.enforceTenant) {
				// From org.springframework.security.access, not java.nio.file !
				throw new AccessDeniedException(errorMessage);
			}
		}
		else {
			var successMessage = "Valid tenant, trying to access [%s], user [%s] is in tenant [%s]".formatted(subdomain,
					user.getUsername(), user.getTenantId());
			System.out.println("~~~~~~~~> " + successMessage);
		}

		filterChain.doFilter(request, response);
	}

}
