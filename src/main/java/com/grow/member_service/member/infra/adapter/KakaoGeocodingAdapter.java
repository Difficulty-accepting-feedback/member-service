// package com.grow.member_service.member.infra.adapter;
//
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpHeaders;
// import org.springframework.stereotype.Component;
// import org.springframework.util.LinkedMultiValueMap;
// import org.springframework.util.MultiValueMap;
// import org.springframework.web.reactive.function.client.WebClient;
//
// import com.grow.member_service.member.application.port.GeocodingPort;
//
// import lombok.RequiredArgsConstructor;
//
// 추후 확장을 고려해 남겨놓음
// @Component
// @RequiredArgsConstructor
// public class KakaoGeocodingAdapter implements GeocodingPort {
//
// 	private final WebClient webClient = WebClient.builder().build();
//
// 	@Value("${kakao.local.rest-key}")
// 	private String restKey;
//
// 	@Value("${kakao.local.base-url:https://dapi.kakao.com}")
// 	private String baseUrl;
//
// 	@Override
// 	public LatLng geocodeRegion(String regionText) {
// 		if (regionText == null || regionText.isBlank()) {
// 			throw new IllegalArgumentException("regionText must not be blank");
// 		}
//
// 		MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
// 		q.add("query", regionText);
//
// 		var resp = webClient.get()
// 			.uri(uri -> uri.scheme("https").host(baseUrl.replace("https://",""))
// 				.path("/v2/local/search/address.json")
// 				.queryParams(q).build())
// 			.header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restKey)
// 			.retrieve()
// 			.bodyToMono(AddressResponse.class)
// 			.block();
//
// 		if (resp == null || resp.documents == null || resp.documents.length == 0) {
// 			throw new IllegalStateException("No geocoding result for: " + regionText);
// 		}
//
// 		var doc = resp.documents[0];
// 		double lng = Double.parseDouble(doc.x);
// 		double lat = Double.parseDouble(doc.y);
// 		return new LatLng(lat, lng);
// 	}
//
// 	record AddressResponse(Document[] documents) {}
// 	record Document(String x, String y) {}
// }