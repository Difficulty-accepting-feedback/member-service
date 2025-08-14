package com.grow.member_service.member.infra.region;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegionCenterResolver {

	private final Map<String, Entry> byLabel;  // "서울특별시 강남구" -> Entry
	private final Map<String, Entry> bySgg;    // "11680" -> Entry

	private static final String JSON_PATH = "region/sgg_centers_map.json";
	private static final String CSV_PATH  = "region/sgg_centers_map.csv";

	private static final Map<String, String> SIDO_SYNONYM = Map.ofEntries(
		Map.entry("서울", "서울특별시"), Map.entry("서울시", "서울특별시"),
		Map.entry("부산", "부산광역시"), Map.entry("대구", "대구광역시"),
		Map.entry("인천", "인천광역시"), Map.entry("광주", "광주광역시"),
		Map.entry("대전", "대전광역시"), Map.entry("울산", "울산광역시"),
		Map.entry("세종", "세종특별자치시"),
		Map.entry("경기", "경기도"), Map.entry("강원", "강원도"),
		Map.entry("충북", "충청북도"), Map.entry("충남", "충청남도"),
		Map.entry("전북", "전라북도"), Map.entry("전남", "전라남도"),
		Map.entry("경북", "경상북도"), Map.entry("경남", "경상남도"),
		Map.entry("제주", "제주특별자치도")
	);

	public RegionCenterResolver(ObjectMapper om) {
		try {
			Map<String, Entry> labelMap;

			// 1) JSON 우선 시도
			Resource jsonRes = new ClassPathResource(JSON_PATH);
			if (jsonRes.exists()) {
				log.info("[RegionCenterResolver] 로딩(JSON): classpath:{}", JSON_PATH);
				try (InputStream in = jsonRes.getInputStream()) {
					String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					Map<String, Map<String, Object>> m = om.readValue(raw, new TypeReference<>() {});
					labelMap = m.entrySet().stream().collect(Collectors.toMap(
						e -> e.getKey(),
						e -> Entry.from(e.getValue())
					));
				}
			} else {
				// 2) CSV 폴백
				Resource csvRes = new ClassPathResource(CSV_PATH);
				if (!csvRes.exists()) {
					throw new FileNotFoundException("classpath:" + JSON_PATH + " / " + CSV_PATH + " 모두 없음");
				}
				log.info("[RegionCenterResolver] 로딩(CSV): classpath:{}", CSV_PATH);
				labelMap = loadFromCsv(csvRes);
			}

			this.byLabel = labelMap;
			this.bySgg = byLabel.values().stream().collect(Collectors.toMap(Entry::sggCode, e -> e, (a,b)->a));
			log.info("[RegionCenterResolver] 로딩 완료: {}개 SGG", byLabel.size());

		} catch (Exception e) {
			log.error("[RegionCenterResolver] 로딩 실패: {}", e.toString(), e);
			throw new IllegalStateException("파일 로드에 실패했습니다.", e);
		}
	}

	public Entry resolve(String rawRegionText) {
		if (rawRegionText == null) return null;
		String norm = rawRegionText.replaceAll("\\s+", " ").trim();
		Entry hit = byLabel.get(norm);
		if (hit != null) return hit;

		String[] toks = norm.split(" ");
		if (toks.length >= 2) {
			String s1 = SIDO_SYNONYM.getOrDefault(toks[0], toks[0]);
			String key = (s1 + " " + String.join(" ", Arrays.copyOfRange(toks, 1, toks.length))).trim();
			return byLabel.get(key);
		}
		return null;
	}

	public Entry resolveBySggCode(String sggCode) {
		if (sggCode == null) return null;
		return bySgg.get(sggCode);
	}

	private Map<String, Entry> loadFromCsv(Resource csvRes) throws IOException {
		Map<String, Entry> map = new LinkedHashMap<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(csvRes.getInputStream(), StandardCharsets.UTF_8))) {
			String header = br.readLine(); // sgg_code,sido_code,sido_name,sig_name,lat,lon,region_key
			if (header == null) throw new IOException("CSV header 없음");
			String line;
			while ((line = br.readLine()) != null) {
				String[] t = line.split(",", -1);
				if (t.length < 7) continue;
				String sggCode = t[0].trim();
				String sido    = t[2].trim();
				String sig     = t[3].trim();
				double lat     = Double.parseDouble(t[4]);
				double lon     = Double.parseDouble(t[5]);
				String key     = t[6].trim(); // "시도 시군구"

				Entry e = new Entry(sggCode, sido, sig, lat, lon);
				map.put(key, e);
			}
		}
		return map;
	}

	public record Entry(String sggCode, String sidoName, String sigName, double lat, double lon) {
		static Entry from(Map<String, Object> v) {
			return new Entry(
				String.valueOf(v.get("sgg_code")),
				String.valueOf(v.get("sido_name")),
				String.valueOf(v.get("sig_name")),
				((Number)v.get("lat")).doubleValue(),
				((Number)v.get("lon")).doubleValue()
			);
		}
	}
}