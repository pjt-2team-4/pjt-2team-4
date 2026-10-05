// feat: 분석 진행 상황 인메모리 저장소 추가(폴링에 보여줄 휘발성 진행 정보 보관) - 풀링 값 단위테스트 지행
package com.rookies6.myspringboot4project.sec.analysis.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AnalysisProgressStoreTest {

    @Test
    void storesProgressAndKeepsOnlyFiveLogs() {
        AnalysisProgressStore store = new AnalysisProgressStore();

        store.start(1L, 2);
        store.updateFile(1L, "Login.java", 1);
        store.addFindings(1L, 3);

        for (int i = 1; i <= 6; i++) {
            store.log(1L, "log-" + i);
        }

        var snapshot = store.get(1L).orElseThrow();
        assertThat(snapshot.currentFile()).isEqualTo("Login.java");
        assertThat(snapshot.totalFiles()).isEqualTo(2);
        assertThat(snapshot.processedFiles()).isEqualTo(1);
        assertThat(snapshot.findingsSoFar()).isEqualTo(3);
        assertThat(snapshot.recentLogs())
                .extracting(AnalysisProgressStore.LogLine::message)
                .containsExactly("log-2", "log-3", "log-4", "log-5", "log-6");

        store.clear(1L);
        assertThat(store.get(1L)).isEmpty();
    }
}