package com.rookies6.myspringboot4project.sec.analysis.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:analysiscreation;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.boot.admin.client.enabled=false"
})
class AnalysisCreationApiTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private UserRepository users;

    @Autowired
    private AnalysisRequestRepository analyses;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void ensureUser() {
        if (!users.existsById(1L)) {
            users.save(User.builder().email("analysis-create@example.com").password("hash").build());
        }
    }

    @Test
    void acceptsRequestWithDocumentedEnvelopeAndAppliesScanOptions() throws Exception {
        String body = """
                {"title":"sample","scanOptions":{"detectHardcodedSecret":false},
                 "files":[{"relativePath":"src/Main.java",
                           "content":"String password = \\"abc123\\";"}]}
                """;
        HttpResponse<String> response = post(body);
        JsonNode json = mapper.readTree(response.body());

        assertThat(response.statusCode()).isEqualTo(202);
        assertThat(json.path("success").asBoolean()).isTrue();
        assertThat(json.path("message").asText()).isEqualTo("분석 요청이 접수되었습니다");
        JsonNode data = json.path("data");
        assertThat(data.path("analysisId").isIntegralNumber()).isTrue();
        assertThat(data.path("title").asText()).isEqualTo("sample");
        assertThat(data.path("status").asText()).isEqualTo("PENDING");
        assertThat(data.path("totalFiles").asInt()).isEqualTo(1);
        assertThat(data.path("estimatedDurationSeconds").asInt()).isEqualTo(20);
        assertThat(data.path("createdAt").isTextual()).isTrue();

        long id = data.path("analysisId").asLong();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline
                && analyses.findById(id).orElseThrow().getStatus() != AnalysisStatus.COMPLETED) {
            Thread.sleep(50);
        }
        var saved = analyses.findById(id).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(saved.getTotalFindings()).isZero();
        assertThat(saved.getEstimatedDurationSeconds()).isNull();
    }

    @Test
    void rejectsEmptyListAndTooManyFilesWithDocumentedCodes() throws Exception {
        assertError(post("{\"title\":\"empty\",\"files\":[]}"), 400, "EMPTY_FILE_LIST");
        StringBuilder files = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            if (i > 0) {
                files.append(',');
            }
            files.append("{\"relativePath\":\"src/F").append(i)
                    .append(".java\",\"content\":\"class F {}\"}");
        }
        assertError(post("{\"title\":\"many\",\"files\":[" + files + "]}"),
                413, "FILE_COUNT_EXCEEDED");
    }

    @Test
    void rejectsDuplicatePathAndUnsupportedExtension() throws Exception {
        String file = "{\"relativePath\":\"src/A.java\",\"content\":\"class A {}\"}";
        assertError(post("{\"title\":\"duplicate\",\"files\":[" + file + "," + file + "]}"),
                400, "DUPLICATE_FILE_PATH");
        assertError(post("{\"title\":\"unsupported\",\"files\":[{\"relativePath\":\"a.png\",\"content\":\"x\"}]}"),
                400, "UNSUPPORTED_LANGUAGE");
    }

    @Test
    void rejectsOversizedUtf8ContentAndInvalidTitle() throws Exception {
        String content = "가".repeat(35_000);
        String body = mapper.writeValueAsString(new RequestBody("large",
                List.of(new FileBody("src/A.java", content))));
        assertError(post(body), 413, "FILE_SIZE_EXCEEDED");

        List<FileBody> files = IntStream.range(0, 6)
                .mapToObj(i -> new FileBody("src/F" + i + ".java", "가".repeat(30_000)))
                .toList();
        assertError(post(mapper.writeValueAsString(new RequestBody("total", files))),
                413, "CODE_SIZE_EXCEEDED");

        assertError(post("{\"title\":\"\",\"files\":[{\"relativePath\":\"src/A.java\",\"content\":\"class A {}\"}]}"),
                400, "VALIDATION_ERROR");
    }

    @Test
    void enablesAllScannerTypesWhenOptionsAreAbsent() throws Exception {
        String body = """
                {"title":"default-options","files":[{"relativePath":"src/Secret.java",
                "content":"String password = \\"abc123\\";"}]}
                """;
        long id = mapper.readTree(post(body).body()).path("data").path("analysisId").asLong();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline
                && analyses.findById(id).orElseThrow().getStatus() != AnalysisStatus.COMPLETED) {
            Thread.sleep(50);
        }
        var saved = analyses.findById(id).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(saved.getTotalFindings()).isPositive();
    }

    private HttpResponse<String> post(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/analyses"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void assertError(HttpResponse<String> response, int status, String code) throws Exception {
        JsonNode json = mapper.readTree(response.body());
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(json.path("success").asBoolean()).isFalse();
        assertThat(json.path("errorCode").asText()).isEqualTo(code);
    }

    private record RequestBody(String title, List<FileBody> files) {
    }

    private record FileBody(String relativePath, String content) {
    }
}
