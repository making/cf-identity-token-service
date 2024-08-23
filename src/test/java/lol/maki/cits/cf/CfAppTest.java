package lol.maki.cits.cf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CfAppTest {

	@Test
	void getUsername() {
		CfApp cfApp = CfApp
			.of("4b84793c-f3ea-4a55-92b7-942726aac163:6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3:02756191-d869-4806-9717-a6eec5142e8a");
		assertThat(cfApp.orgGuid()).isEqualTo("4b84793c-f3ea-4a55-92b7-942726aac163");
		assertThat(cfApp.spaceGuid()).isEqualTo("6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3");
		assertThat(cfApp.appGuid()).isEqualTo("02756191-d869-4806-9717-a6eec5142e8a");
	}

}