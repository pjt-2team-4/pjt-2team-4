let selectedFiles = [];

document.addEventListener('DOMContentLoaded', () => {
    const project = document.getElementById('projectId');
    if (!project) return;
    
    // 분석 ID(또는 프로젝트 ID) 변경 시 하위 파일 트리 로드
    project.addEventListener('change', loadAnalysisFiles);

    // 전체 선택 / 해제 버튼 이벤트
    document.getElementById('selectAllFilesBtn')
        ?.addEventListener('click', () => setAllFiles(true));

    document.getElementById('unselectAllFilesBtn')
        ?.addEventListener('click', () => setAllFiles(false));

    // 분석 실행 버튼 이벤트
    document.getElementById('analyzeBtn')
        ?.addEventListener('click', executeAnalysis);
});

/**
 * 1. 특정 분석(Analysis ID)에 속한 파일 목록 로드
 * GET /api/v1/analyses/{analysisId}/files
 */
async function loadAnalysisFiles(event) {
    const analysisId = event.target.value;
    const tree = document.getElementById('fileTree');

    if (!tree) return;

    selectedFiles = [];
    updateSelectedCount();

    if (!analysisId) {
        tree.innerHTML = `
            <div class="file-tree-empty">
                분석 대상을 선택하세요.
            </div>
        `;
        return;
    }

    tree.innerHTML = `
        <div class="file-tree-loading">
            📂 DB에서 하위 파일들을 불러오는 중...
        </div>
    `;

    try {
        // 우리가 만든 File 목록 조회 API 호출
        const files = await ApiService.get(`${API.ANALYSIS}/${analysisId}/files`);
        renderAnalysisTree(files);

    } catch (error) {
        console.error("❌ 파일 목록 로드 실패:", error);
        const serverMsg = error.response?.data?.message || error.message || '알 수 없는 오류';
        
        tree.innerHTML = `
            <div class="file-tree-error">
                ❌ 파일 로드 실패<br>
                <small style="color: #d9534f; font-weight: bold;">${escapeHtml(serverMsg)}</small>
            </div>
        `;
    }
}

/**
 * 파일 트리 렌더링
 */
function renderAnalysisTree(files) {
    const tree = document.getElementById('fileTree');
    if (!tree) return;
    
    tree.innerHTML = '';

    if (!files?.length) {
        tree.innerHTML = `
            <div class="file-tree-empty">
                등록된 하위 파일이 없습니다.
            </div>
        `;
        return;
    }

    buildTree(files).forEach(node =>
        tree.appendChild(createAnalysisNode(node))
    );
}

function createAnalysisNode(node) {
    const wrapper = document.createElement('div');

    if (node.type === 'DIRECTORY') {
        const details = document.createElement('details');
        // 폴더는 기본적으로 열려있게 설정
        details.open = true; 
        const summary = document.createElement('summary');

        summary.innerHTML = `📁 ${escapeHtml(node.name)}`;

        const children = document.createElement('div');
        node.children.forEach(child => {
            children.appendChild(createAnalysisNode(child));
        });

        details.append(summary, children);
        wrapper.appendChild(details);

        return wrapper;
    }

    const label = document.createElement('label');
    label.className = 'file-check-label';

    // 체크박스의 value에 fileId를 담아둡니다.
    label.innerHTML = `
        <input 
            type="checkbox" 
            class="analysis-file-checkbox" 
            value="${node.file.fileId}">
        📄 ${escapeHtml(node.name)}
    `;

    label.querySelector('input').addEventListener('change', updateSelectedCount);
    wrapper.appendChild(label);

    return wrapper;
}

/**
 * 2. 분석 실행 요청 (기존 파일들을 읽어와서 새 분석 생성)
 * POST /api/v1/analyses
 */
async function executeAnalysis(event) {
    if (event) event.preventDefault();

    const projectIdInput = document.getElementById('projectId');
    const languageInput = document.getElementById('language');

    const sourceAnalysisId = projectIdInput ? projectIdInput.value : '';
    const language = languageInput ? languageInput.value : 'JAVA';
    const fileIds = [...document.querySelectorAll('.analysis-file-checkbox:checked')]
        .map(checkbox => Number(checkbox.value));

    if (!sourceAnalysisId) return alert('분석 대상을 선택해주세요.');
    if (!fileIds.length) return alert('분석할 하위 파일을 최소 1개 이상 선택해주세요.');

    const button = document.getElementById('analyzeBtn');
    if (button) {
        button.disabled = true;
        button.textContent = '파일 내용 읽는 중...';
    }

    showAnalysisLoading();

    try {
        // [핵심 로직] 1. 체크된 파일들의 '상세 내용(content)'을 DB에서 각각 불러옵니다.
        const filePromises = fileIds.map(fileId => 
            ApiService.get(`${API.ANALYSIS}/${sourceAnalysisId}/files/${fileId}`)
        );
        const fileDetails = await Promise.all(filePromises);

        // 2. 백엔드가 요구하는 새로운 DTO 스펙에 맞게 데이터 조립
        const payloadFiles = fileDetails.map(detail => ({
            filePath: detail.relativePath,
            content: detail.content
        }));

        const originalTitle = projectIdInput.options[projectIdInput.selectedIndex].text;

        const payload = {
            title: `[선택분석] ${originalTitle}`, // 기존 이름에 꼬리표를 달아 새 분석 생성
            language: language,
            files: payloadFiles
        };

        if (button) button.textContent = '분석 엔진 가동 중...';
        console.log("🚀 분석 실행 Payload:", payload);

        // 3. 백엔드로 새로운 분석 요청 (POST)
        const result = await ApiService.post(API.ANALYSIS, payload);

        // 4. 상태 폴링 시작 (새로 발급받은 analysisId 사용)
        await pollAnalysisStatus(result.analysisId || result.id);

    } catch (error) {
        console.error("❌ 분석 요청 실패:", error);
        const serverMsg = error.response?.data?.message || error.message || '알 수 없는 오류';
        alert(`🚨 분석 요청 실패:\n${serverMsg}`);
        resetUI();
    }
}

/**
 * 3. 분석 진행 상태 폴링
 */
async function pollAnalysisStatus(newAnalysisId) {
    try {
        const status = await ApiService.get(`${API.ANALYSIS}/${newAnalysisId}/status`);

        updateAnalysisStatus(status.status);

        if (status.status === 'COMPLETED') {
            const result = await ApiService.get(`${API.ANALYSIS}/${newAnalysisId}`);
            renderResults(result);
            return;
        }

        if (status.status === 'FAILED') {
            const failReason = status.errorMessage || '알 수 없는 이유로 실패했습니다.';
            alert(`🚨 분석 실패:\n${failReason}`);
            resetUI();
            return;
        }

        // 2초마다 상태 재확인
        setTimeout(() => pollAnalysisStatus(newAnalysisId), 2000);

    } catch (error) {
        console.error("❌ 상태 폴링 중 에러:", error);
        resetUI();
    }
}

/* =========================================
   UI 제어 및 유틸리티 함수들
========================================= */

function setAllFiles(checked) {
    document.querySelectorAll('.analysis-file-checkbox').forEach(cb => {
        cb.checked = checked;
    });
    updateSelectedCount();
}

function updateSelectedCount() {
    const count = document.querySelectorAll('.analysis-file-checkbox:checked').length;
    const countEl = document.getElementById('selectedFileCount');
    if (countEl) countEl.textContent = count;
}

function showAnalysisLoading() {
    const resultContent = document.getElementById('resultContent');
    if (resultContent) resultContent.style.display = 'none';
    
    const placeholder = document.getElementById('resultPlaceholder');
    if (placeholder) {
        placeholder.style.display = 'block';
        placeholder.innerHTML = '⏳ 백엔드 분석 파이프라인을 실행 중입니다...';
    }
}

function updateAnalysisStatus(statusText) {
    const placeholder = document.getElementById('resultPlaceholder');
    if (placeholder) {
        placeholder.innerHTML = `⏳ 분석 진행 중... (현재 서버 상태: <strong>${statusText}</strong>)`;
    }
}

function resetUI() {
    const button = document.getElementById('analyzeBtn');
    if (button) {
        button.disabled = false;
        button.textContent = '분석 실행';
    }
    
    const placeholder = document.getElementById('resultPlaceholder');
    if (placeholder) {
        placeholder.innerHTML = '분석할 파일을 선택한 후 <strong>[분석 실행]</strong> 버튼을 눌러주세요.';
        placeholder.style.display = 'block';
    }
    
    const resultContent = document.getElementById('resultContent');
    if (resultContent) resultContent.style.display = 'none';
}

function renderResults(result) {
    resetUI(); 
    
    const placeholder = document.getElementById('resultPlaceholder');
    const resultContent = document.getElementById('resultContent');
    
    if (placeholder) placeholder.style.display = 'none';
    if (resultContent) resultContent.style.display = 'block';

    const resId = document.getElementById('resId');
    const resTotal = document.getElementById('resTotal');
    const resHigh = document.getElementById('resHigh');

    if (resId) resId.textContent = result.analysisId || result.id || '-';
    if (resTotal) resTotal.textContent = result.totalCount || 0;
    if (resHigh) resHigh.textContent = result.highCount || 0;

    const tbody = document.getElementById('vulnTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    const vulnerabilities = result.vulnerabilities || [];

    if (vulnerabilities.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align: center; padding: 20px;">
                    🎉 발견된 취약점이 없습니다. 안전한 코드입니다!
                </td>
            </tr>
        `;
        return;
    }

    vulnerabilities.forEach(vuln => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${vuln.startLine || '-'}</td>
            <td>${escapeHtml(vuln.type || '알 수 없음')}</td>
            <td>
                <span style="font-weight: bold; color: ${vuln.severity === 'HIGH' ? 'red' : (vuln.severity === 'MEDIUM' ? 'orange' : 'green')}">
                    ${escapeHtml(vuln.severity || '-')}
                </span>
            </td>
            <td>
                <strong>${escapeHtml(vuln.description || '-')}</strong>
                <br>
                <small style="color: #6b7280;">📁 파일: ${escapeHtml(vuln.fileName)}</small>
                ${vuln.codeSnippet ? `<pre style="background:#f8f9fa; padding:10px; margin-top:5px; border-radius:4px;"><code>${escapeHtml(vuln.codeSnippet)}</code></pre>` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function buildTree(files) {
    const root = [];
    files.forEach(file => {
        const path = file.relativePath || file.filePath || file.fileName;
        if (!path) return;
        const parts = path.split('/').filter(Boolean);
        let current = root;

        parts.forEach((part, index) => {
            let node = current.find(n => n.name === part);
            if (!node) {
                node = {
                    name: part,
                    type: index === parts.length - 1 ? 'FILE' : 'DIRECTORY',
                    file: index === parts.length - 1 ? file : null,
                    children: []
                };
                current.push(node);
            }
            current = node.children;
        });
    });
    sortTree(root);
    return root;
}

function sortTree(nodes) {
    nodes.sort((a, b) => {
        if (a.type !== b.type) return a.type === 'DIRECTORY' ? -1 : 1;
        return a.name.localeCompare(b.name);
    });
    nodes.forEach(node => sortTree(node.children));
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}