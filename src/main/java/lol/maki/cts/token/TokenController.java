package lol.maki.cts.token;

import java.util.Map;

import lol.maki.cts.cf.CfApp;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {
	@PostMapping(path = "/token")
	public Map<String, String> token(@AuthenticationPrincipal CfApp cfApp) {
		// TODO JWT
		return Map.of("sub", cfApp.getUsername(),
				"org_guid", cfApp.orgGuid(),
				"space_guid", cfApp.spaceGuid(),
				"app_guid", cfApp.appGuid());
	}
}
