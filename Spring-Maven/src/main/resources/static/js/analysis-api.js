async function executeAnalysis(event) {
    event.preventDefault(); // 폼 기본 제출 동작 방지

    const language = document.getElementById('language').value;
    const code = document.getElementById('code').value;
    const analyzeBtn = document.getElementById('analyzeBtn');

    // UI 로딩 상태 변경
    analyzeBtn.disabled = true;
    analyzeBtn.innerText = "분석 중...";
    document.getElementById('resultPlaceholder').style.display = 'flex';
    document.getElementById('resultPlaceholder').innerHTML = '<span style="color:#3498db;">AI가 코드를 분석하고 있습니다. 잠시만 기다려주세요... ⏳</span>';
    document.getElementById('resultContent').style.display = 'none';

    try {
        const response = await fetch('/api/analysis', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ language: language, code: code })
        });
        
        const result = await response.json();

        // 💡 수정됨: result.success 대신 HTTP 상태 코드로 검증 (기존 DefaultExceptionAdvice 호환)
        if (response.ok) {
            // 성공 시 화면 렌더링
            renderResults(result);
        } else {
            // 400 Validation Error (ValidationErrorResponse 포맷 처리)
            if (response.status === 400 && result.errors) {
                const errorMessages = Object.entries(result.errors)
                    .map(([field, msg]) => `- ${msg}`)
                    .join('\n');
                alert("입력 오류:\n" + errorMessages);
            } 
            // 404, 500 등 Business Exception (ErrorObject 포맷 처리)
            else {
                alert("오류 발생: " + (result.message || "알 수 없는 오류가 발생했습니다."));
            }
            resetUI();
        }
    } catch (error) {
        console.error("API 호출 에러:", error);
        alert("서버와 통신하는 중 문제가 발생했습니다.");
        resetUI();
    }
}

function renderResults(data) {
    // 요약 데이터 세팅
    document.getElementById('resId').innerText = data.analysisId;
    document.getElementById('resTotal').innerText = data.totalCount;
    document.getElementById('resHigh').innerText = data.highCount;

    const tbody = document.getElementById('vulnTableBody');
    tbody.innerHTML = '';

    if (!data.vulnerabilities || data.vulnerabilities.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding: 30px;">발견된 취약점이 없습니다. 👏</td></tr>`;
    } else {
        // 테이블 행 동적 생성
        data.vulnerabilities.forEach(vuln => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>Line ${vuln.lineNumber}</strong></td>
                <td>${vuln.type}</td>
                <td><span class="severity-${vuln.severity.toLowerCase()}">${vuln.severity}</span></td>
                <td>
                    <p style="margin-bottom:8px;">${vuln.description}</p>
                    <div style="background-color: #f1f2f6; padding:8px; border-left:3px solid #e1b12c; margin-bottom:8px;">
                        <strong>💡 AI 설명:</strong> ${vuln.aiExplanation}
                    </div>
                    <strong>개선 후 코드:</strong>
                    <div class="code-block">${vuln.afterCode}</div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // 영역 표시 토글
    document.getElementById('resultPlaceholder').style.display = 'none';
    document.getElementById('resultContent').style.display = 'block';

    // 버튼 원상복구
    const analyzeBtn = document.getElementById('analyzeBtn');
    analyzeBtn.disabled = false;
    analyzeBtn.innerText = "분석 실행 (POST)";
}

function resetUI() {
    document.getElementById('resultPlaceholder').style.display = 'flex';
    document.getElementById('resultPlaceholder').innerText = '오류가 발생하여 분석을 완료하지 못했습니다.';
    document.getElementById('resultContent').style.display = 'none';
    
    const analyzeBtn = document.getElementById('analyzeBtn');
    analyzeBtn.disabled = false;
    analyzeBtn.innerText = "분석 실행 (POST)";
}