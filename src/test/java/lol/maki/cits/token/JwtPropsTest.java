package lol.maki.cits.token;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropsTest {

	@Test
	void loadCertsFromClasspath() {
		JwtProps props = new JwtProps("test", "classpath:rsa/public_key", "classpath:rsa/private_key", List.of("test"));
		assertThat(props.publicKey()).isNotNull();
		assertThat(props.privateKey()).isNotNull();
	}

	@Test
	void loadCertsFromBase64() {
		JwtProps props = new JwtProps("test",
				"base64:LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFsdWVHWVJwaS9UN1Z0KzlxclVxUwp3aXJUd1FCYWxqVTR6ajVsSXEwdGpsSWtwaTdmOEhHcFRTUVZ1a3lmNzFwYWlOenFCMkRyQzBqN1IyejJ5aXU5CjE1ckRidk5OOVd3cXNTdXRwVG84SHQ1Y0NzQ3Q2bDhONHR5d29RcTJuS0xBU0ZSYmFuSytxam43K3g1OUpxWSsKMkNhSzJyTzZYUEhIRjlsSGRicUJzTUdHdGxPQlFDeDhqOWVxTGlVc21UWGtMUzJVWmdIQnRpMG9mbXZURUF1awppRzExalZMUmpGd0JtTW1yVi9RNndlWXhxZWhoZUdHSUF3MVJ3a2ZUMkV0eFBIcGJXVmRKYUNtLzMyd2dTNmtWCnl5Zk5PK2FGLzdFNHBSdHA5b2FDd0xHUlpUWWlSV2RnbXE3LytoRnVhTWl5Q0ZDSzh3UmM4TmZEUGsvT2FhNW0KSndJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==",
				"base64:LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2QUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktZd2dnU2lBZ0VBQW9JQkFRQ1c1NFpoR21MOVB0VzMKNzJxdFNwTENLdFBCQUZxV05Uak9QbVVpclMyT1VpU21MdC93Y2FsTkpCVzZUSi92V2xxSTNPb0hZT3NMU1B0SApiUGJLSzczWG1zTnU4MDMxYkNxeEs2MmxPandlM2x3S3dLM3FYdzNpM0xDaENyYWNvc0JJVkZ0cWNyNnFPZnY3CkhuMG1wajdZSm9yYXM3cGM4Y2NYMlVkMXVvR3d3WWEyVTRGQUxIeVAxNm91SlN5Wk5lUXRMWlJtQWNHMkxTaCsKYTlNUUM2U0liWFdOVXRHTVhBR1l5YXRYOURyQjVqR3A2R0Y0WVlnRERWSENSOVBZUzNFOGVsdFpWMGxvS2IvZgpiQ0JMcVJYTEo4MDc1b1gvc1RpbEcybjJob0xBc1pGbE5pSkZaMkNhcnYvNkVXNW95TElJVUlyekJGencxOE0rClQ4NXBybVluQWdNQkFBRUNnZ0VBSjB4Vi8zV3FyNzZzVTRGQzY2d08rZ2ZESzNEWEptVG56VFlNaW5KRStkcEUKZHc2Zi9QTHNudzAxcDFXTEZQOFhzcXF0TzR5dXlUcVJUYkYvdDNpYXNNbzUrT3Zkc25zZnh4SFQzOEtCbzV4TwpwZkkxbUdKMnNFeStwc0d1ZXE0Ym12cDA4QXkvd0g3bk15ZG1laWt6Rm9FN0NlZGlNaEE0dytvdXBPY1VIUjV5ClQ2Mmc4bEhtOFhWUHB3Z0podGh6d1g4L3pxL2JWWm5Cb2UvTElqeko4blllWndFeEtpWlYrUTZOcTdQTE82VGYKZ2lucThjVUhlb1M4dStWR0JGeStHRzI5OGhZNFAzS3kwU3dscmFISUFMNWdtSG5Sc2Zudk5hYTc2QnF6YVlFbQo1TEw4dUhTdXA0ZUU3ZlVhamVSdVZnRi9vTG8zL3hxcU1yYlh2bVRkZ1FLQmdRRFJMUDJuWU4zK1VRZEp6QnlCCkRQelUzMEliSGZidjdxWVpZUGJaQkZNZnMycWQ3bTFxZkUyclBTbjVtRVRPMTI5ZlpjR1h0ZHRnWnd1OUZRZlMKMmVaZ2k0OStiOHFCRkdYWEMxQ0RJTTVUV2daM3QvZ2cwaXhUeUhYNUJhZ1BTa2JNdDl3QmRGSVZ6VUU5cEdkagpVQVk3a0Y5cE9NK1pkS0haS3RNRU1Hcnl3UUtCZ1FDNHJ6MlNlWFg3d2V3dkxjTzVHL2NrWFY2VHNtVXRaOStDClFlWmJ4TGFjYys2WVByMHphZUc1OHpmTG9tbnhvYmJISHZJak5USjdqZmFWQ3RGdFp1NXM5M3ArbU1KUHZBdEcKTEhUbzBISEVYbmNCM1R1M1ZaTHFlbDU0SFBsV2R3MjRHRG9OY2duN0Uyb3VxNXl2bVR0NkcxM0FCK2dxREdDNwo1cjRqQkxYYTV3S0JnSDlTOTROdXZ6SVNlSEUvUVFwOHNWbVNIRmNOUWthQjZiRUJDTURJbFNCeXdhOG9kb21lCjZGZ1hmdmxpNmw5Tlc0bWlFdGtaNVZNazVreE9CTmtmc2MrS28rbStlbkZmelMyOHdXRlFFc2RCd1RZV1VYOEkKNzlwLzV1K1J0ZDY0dzZmUk1xWGYvQ2N4TFVrc1RaSnlINWthSkRtVVR5bUZpUTc3dGo2U2lrY0JBb0dBVzJINgpxWHVJTWFabTQ0RlZrMGFQSjJNNzVRRWtweTc2blUxV1dwb2ZjWks3d0lGcXhRSGpPMHRwbEUwczl1NzVyZFZXCklxMno2UWI1dUEyK1NzL2ZzZk5TblVtZy9Dc2p1UGxPYjlTVU5vSTNpS2liWDcvc0RqRzJoT0J4VDZOaEduS0MKWnh6Q2E0WFR5SHdKNzZsMmtKang3bzc3QmJOU09pZVdkV1ZqUm9rQ2dZQkwraW5ReUtkRXAwUlhSeVdEeHBBMgpsaWlYUDV3VEt2NHdtV0FpbFBXeG9LRXBnMzc2cEpjb1N2RTRmQklzWFhzZVlhdUtiUzZGRnBZNy8ranJoazJEClVPRTQwcGhsK2dTLzNhOGh0WTZFeG9tT3BqOXVFRmU2N2h1NnVsN0hNM21NUVZyMUlnV3NNR3ltMFFYTjd1MkkKY0xLakZYOHNRd05lemhWZXpCK1ozdz09Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K",
				List.of("test"));
		assertThat(props.publicKey()).isNotNull();
		assertThat(props.privateKey()).isNotNull();
	}

	@Test
	void loadCertsFromUrl() {
		JwtProps props = new JwtProps("test",
				"https://github.com/making/demo-jwt/raw/master/src/main/resources/public.pem",
				"https://github.com/making/demo-jwt/raw/master/src/main/resources/private_key.pem", List.of("test"));
		assertThat(props.publicKey()).isNotNull();
		assertThat(props.privateKey()).isNotNull();
	}

}