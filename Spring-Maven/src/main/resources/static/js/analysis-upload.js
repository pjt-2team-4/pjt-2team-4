document.addEventListener('DOMContentLoaded', () => {
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('fileInput');
    const fileList = document.getElementById('fileList');
    const filePreviewContainer = document.getElementById('filePreviewContainer');
    const fileCountBadge = document.getElementById('fileCountBadge');
    const submitBtn = document.getElementById('submitBtn');

    let selectedFiles = []; // 업로드 대기 중인 파일 배열

    // ==========================================
    // 1. 드래그 앤 드롭 및 파일 선택 이벤트 바인딩 (안전한 방어 처리)
    // ==========================================
    if (dropzone && fileInput) {
        dropzone.addEventListener('click', () => fileInput.click());

        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropzone.addEventListener(eventName, preventDefaults, false);
            document.body.addEventListener(eventName, preventDefaults, false);
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        ['dragenter', 'dragover'].forEach(eventName => {
            dropzone.addEventListener(eventName, () => dropzone.classList.add('dragover'), false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropzone.addEventListener(eventName, () => dropzone.classList.remove('dragover'), false);
        });

        // 드롭 시 파일 처리
        dropzone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            const files = dt.files;
            handleFiles(files);
        });

        fileInput.addEventListener('change', (e) => {
            handleFiles(e.target.files);
            fileInput.value = ''; // 동일한 파일 재선택 가능하도록 초기화
        });
    }

    // ==========================================
    // 2. 파일 검증 (1차 클라이언트 검증)
    // ==========================================
    function handleFiles(files) {
        const newFiles = Array.from(files);

        for (let file of newFiles) {
            // BR-A002: 20개 제한
            if (selectedFiles.length >= 20) {
                alert("파일은 최대 20개까지만 업로드할 수 있습니다.");
                break;
            }
            
            // BR-A003: 100KB 제한 (100 * 1024 bytes = 102400)
            if (file.size > 102400) {
                alert(`[${file.name}] 파일은 100KB를 초과하여 제외되었습니다.`);
                continue;
            }

            // 중복 파일 체크 (경로 기준)
            const path = file.webkitRelativePath || file.name;
            const isDuplicate = selectedFiles.some(f => (f.webkitRelativePath || f.name) === path);
            
            if (!isDuplicate) {
                selectedFiles.push(file);
            }
        }
        renderFileList();
    }

    // ==========================================
    // 3. UI 렌더링 (대기 목록 표시)
    // ==========================================
    function renderFileList() {
        if (!filePreviewContainer || !fileList) return;

        if (selectedFiles.length === 0) {
            filePreviewContainer.style.display = 'none';
            return;
        }

        filePreviewContainer.style.display = 'block';
        if (fileCountBadge) fileCountBadge.textContent = `${selectedFiles.length} / 20`;
        fileList.innerHTML = '';

        selectedFiles.forEach((file, index) => {
            const li = document.createElement('li');
            li.className = 'file-preview-item';
            
            const path = file.webkitRelativePath || file.name;
            const sizeKB = (file.size / 1024).toFixed(1);

            li.innerHTML = `
                <div class="file-preview-info">
                    <span class="file-name">${escapeHtml(path)}</span>
                    <span class="file-size">${sizeKB} KB</span>
                </div>
                <button type="button" class="remove-file-btn" data-index="${index}">✖</button>
            `;
            fileList.appendChild(li);
        });

        // 삭제 버튼 이벤트 연결
        document.querySelectorAll('.remove-file-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation(); // 드롭존 클릭 방지
                const removeIndex = parseInt(this.getAttribute('data-index'));
                selectedFiles.splice(removeIndex, 1);
                renderFileList();
            });
        });
    }

    // ==========================================
    // 4. 파일 텍스트 변환 및 서버 전송 (제출 버튼 이벤트)
    // ==========================================
    if (submitBtn) {
        submitBtn.addEventListener('click', async () => {
            const titleInput = document.getElementById('title');
            const languageInput = document.getElementById('language');

            const title = titleInput ? titleInput.value.trim() : '';
            const language = languageInput ? languageInput.value : '';

            if (!title) return alert('분석 제목을 입력해주세요.');
            if (!language) return alert('분석 언어를 선택해주세요.');
            if (selectedFiles.length === 0) return alert('최소 1개 이상의 파일을 첨부해주세요.');

            const originalText = submitBtn.textContent;
            submitBtn.textContent = '파일 분석 및 전송 중...';
            submitBtn.disabled = true;

            try {
                const fileDataPromises = selectedFiles.map(file => {
                    return new Promise((resolve, reject) => {
                        const reader = new FileReader();
                        reader.onload = (e) => {
                            resolve({
                                filePath: file.webkitRelativePath || file.name,
                                content: e.target.result
                            });
                        };
                        reader.onerror = () => reject(`파일 읽기 실패: ${file.name}`);
                        reader.readAsText(file);
                    });
                });

                const parsedFiles = await Promise.all(fileDataPromises);

                const payload = {
                    title: title,
                    language: language,
                    files: parsedFiles
                };

                console.log("🚀 API 전송 Payload:", payload);

                // 서버 요청
                const response = await ApiService.post('/api/v1/analyses', payload);
                
                alert('분석 요청이 성공적으로 접수되었습니다!');
                window.location.href = `/analyses/${response.analysisId}`;

            } catch (error) {
                console.error('❌ [Backend Error] Upload/Analysis Request Failed:', error);
                
                // 백엔드 에러코드 및 메시지 정밀 추출
                let errorDetail = error.message || '알 수 없는 오류';
                if (error.response && error.response.data) {
                    const data = error.response.data;
                    const code = data.code ? `[${data.code}] ` : '';
                    const msg = data.message || JSON.stringify(data);
                    errorDetail = `${code}${msg}`;
                } else if (error.data) {
                    const data = error.data;
                    const code = data.code ? `[${data.code}] ` : '';
                    const msg = data.message || JSON.stringify(data);
                    errorDetail = `${code}${msg}`;
                }
                
                alert(`🚨 분석 요청 실패 (서버 에러):\n${errorDetail}`);
            } finally {
                submitBtn.textContent = originalText;
                submitBtn.disabled = false;
            }
        });
    } else {
        console.warn("⚠️ 경고: 'submitBtn' ID를 가진 버튼을 찾을 수 없습니다.");
    }
});

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}