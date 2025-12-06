package com.example.lumental.util;

import com.example.lumental.dto.AnalysisRequestDto;
import com.example.lumental.dto.DataPointDto;
import com.example.lumental.dto.SleepSegmentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Slf4j
@Component
public class HealthKitParser {

    private static final List<DateTimeFormatter> POSSIBLE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"),   // +0900
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX"), // +09:00
            DateTimeFormatter.ISO_OFFSET_DATE_TIME                  // 2021-10-29T00:10:16+09:00
    );

    private OffsetDateTime parseDateSafely(String str) {
        for (DateTimeFormatter fmt : POSSIBLE_FORMATS) {
            try {
                return OffsetDateTime.parse(str, fmt);
            } catch (Exception ignore) {}
        }
        log.warn("⚠️ 날짜 파싱 실패, skip: {}", str);
        return null;
    }


    /**
     * ZIP → (HealthKit XML 추출) → XML 파싱 → AnalysisRequestDto 생성
     */
    public AnalysisRequestDto parseZipToRequest(Long userId, File zipFile) throws Exception {

        // 1) ZIP 해제
        File xmlFile = extractHealthKitXml(zipFile);
        //Path unzipDir = unzip(zipFile);

        if (xmlFile == null) {
            throw new RuntimeException("HealthKit 내보내기 XML 파일을 찾지 못했습니다.");
        }

        ParsedData parsed = parseXml(xmlFile);

        // null 방지: 빈 리스트라도 보냄
        return AnalysisRequestDto.builder()
                .userId(userId)
                .heartRate(parsed.heartRate() != null ? parsed.heartRate() : List.of())
                .hrv(parsed.hrv() != null ? parsed.hrv() : List.of())
                .steps(parsed.steps() != null ? parsed.steps() : List.of())
                .sleep(parsed.sleep() != null ? parsed.sleep() : List.of())
                .build();
    }

    /**
     * ZIP 내부에서 apple_health_export/내보내기.xml 또는 export.xml 을 찾아 temp 파일로 추출
     */
    private File extractHealthKitXml(File zipFile) throws Exception {
        ZipFile zip = new ZipFile(zipFile, StandardCharsets.UTF_8);
        File tempDir = Files.createTempDirectory("hk_extract").toFile();
        List<File> xmlCandidates = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        //File tempZip = File.createTempFile("hk_", ".zip");
        // zipFile.transferTo(tempZip);

        //ZipFile zip = new ZipFile(tempZip, StandardCharsets.UTF_8);
        //File tempDir = Files.createTempDirectory("hk_extract").toFile();

        //List<File> xmlCandidates = new ArrayList<>();

        //Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            String lower = name.toLowerCase();

            log.info("📄 ZIP entry: {}", name);

            if (entry.isDirectory()) {
                log.info("📁 디렉터리 skip: {}", name);
                continue;
            }

            // 불필요 파일 제외
            if (lower.contains("export_cda")) {
                log.info("🚫 export_cda.xml 무시: {}", name);
                continue;
            }

            // 🔥 모든 xml 파일을 후보에 추가
            if (lower.endsWith(".xml")) {

                File out = new File(tempDir, UUID.randomUUID() + "_" +
                        Paths.get(name).getFileName());

                try (InputStream is = zip.getInputStream(entry);
                     OutputStream os = new FileOutputStream(out)) {
                    is.transferTo(os);
                }

                log.info("📌 XML 후보 추가 → {} (size={})", out.getName(), out.length());
                xmlCandidates.add(out);
            }
        }

        zip.close();

        // 🔥 후보 중 가장 큰 파일을 "내보내기 XML"로 간주
        File best = xmlCandidates.stream()
                .max(Comparator.comparingLong(File::length))
                .orElse(null);

        if (best != null) {
            log.info("✅ 최종 선택된 XML: {} (size={})", best.getName(), best.length());
        }

        return best;
    }


    /**
     * XML 전체를 훑으면서 날짜별로 버킷팅 후,
     * 가장 데이터가 많은 날짜(heartRate+hrv+steps+sleep 레코드 수 기준)를 선택
     */
    public ParsedData parseXml(File xmlFile) throws Exception {

        Map<LocalDate, DayData> dayBuckets = new HashMap<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        InputStream is = new FileInputStream(xmlFile);
        XMLStreamReader reader = factory.createXMLStreamReader(is);

        while (reader.hasNext()) {
            int event = reader.next();

            if (event != XMLStreamConstants.START_ELEMENT) continue;
            if (!reader.getLocalName().equals("Record")) continue;

            String type = reader.getAttributeValue(null, "type");
            String value = reader.getAttributeValue(null, "value");
            String start = reader.getAttributeValue(null, "startDate");
            String end = reader.getAttributeValue(null, "endDate");

            if (start == null || type == null) continue;

            OffsetDateTime ts = parseDateSafely(start);
            if (ts == null) continue;

            // 날짜별로 bucket
            LocalDate date = ts.toLocalDate();
            dayBuckets.putIfAbsent(date, new DayData());
            DayData bucket = dayBuckets.get(date);

            switch (type) {
                case "HKQuantityTypeIdentifierHeartRate" -> bucket.heartRate.add(
                        DataPointDto.builder()
                                .ts(ts.toString())
                                .value(Double.parseDouble(value))
                                .build()
                );

                case "HKQuantityTypeIdentifierHeartRateVariabilitySDNN" -> bucket.hrv.add(
                        DataPointDto.builder()
                                .ts(ts.toString())
                                .value(Double.parseDouble(value))
                                .build()
                );

                case "HKQuantityTypeIdentifierStepCount" -> bucket.steps.add(
                        DataPointDto.builder()
                                .ts(ts.toString())
                                .value(Double.parseDouble(value))
                                .build()
                );

                case "HKCategoryTypeIdentifierSleepAnalysis" -> bucket.sleep.add(
                        SleepSegmentDto.builder()
                                .start(start)
                                .end(end)
                                .stage(value)
                                .duration(1)
                                .build()
                );
            }
        }

        // ❗ 핵심: XML 내에서 가장 최신(LocalDate) 선택
        LocalDate latestDate = dayBuckets.keySet().stream()
                .max(LocalDate::compareTo)
                .orElse(null);

        if (latestDate == null) {
            log.warn("⚠ XML에서 데이터 날짜를 찾지 못했습니다.");
            return new ParsedData(List.of(), List.of(), List.of(), List.of());
        }

        DayData best = dayBuckets.get(latestDate);

        log.info("📅 가장 최근 날짜 선택: {}", latestDate);

        return new ParsedData(
                best.heartRate,
                best.hrv,
                best.steps,
                best.sleep
        );
    }


    /**
     * 내부 버킷용 클래스 (날짜 1개에 대한 데이터 모음)
     */
    static class DayData {
        List<DataPointDto> heartRate = new ArrayList<>();
        List<DataPointDto> hrv = new ArrayList<>();
        List<DataPointDto> steps = new ArrayList<>();
        List<SleepSegmentDto> sleep = new ArrayList<>();
    }

    /**
     * parseXml 결과를 담는 레코드
     * → record 이기 때문에 heartRate(), hrv(), steps(), sleep() accessor가 자동 생성됨
     */
    public record ParsedData(
            List<DataPointDto> heartRate,
            List<DataPointDto> hrv,
            List<DataPointDto> steps,
            List<SleepSegmentDto> sleep
    ) {}
}