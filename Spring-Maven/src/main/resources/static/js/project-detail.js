document.addEventListener('DOMContentLoaded', () => {
    if (typeof analysisId !== 'undefined' && analysisId) {
        loadProjectFiles(analysisId);
    }
});

async function loadProjectFiles(analysisId) {
    const tree = document.getElementById('projectFileTree');
    if (!tree) return;

    try {
        const files = await ApiService.get(`${API.ANALYSIS}/${analysisId}/files`);
        renderProjectFileTree(files, analysisId);
    } catch (error) {
        console.error(error);
        tree.innerHTML = `<div class="file-tree-error">❌ 파일을 불러오지 못했습니다.</div>`;
    }
}

function renderProjectFileTree(files, analysisId) {
    const tree = document.getElementById('projectFileTree');
    if (!tree) return;
    tree.innerHTML = '';

    if (!files?.length) {
        tree.innerHTML = `<div class="file-tree-empty">등록된 파일이 없습니다.</div>`;
        return;
    }

    const root = buildTree(files);
    root.forEach(node => {
        tree.appendChild(createTreeNode(node, analysisId));
    });
}

function buildTree(files) {
    const root = [];
    files.forEach(file => {
        // ✨ 수정 포인트: relativePath 우선 참조
        const path = file.relativePath || file.filePath || file.path || file.fileName;
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

function createTreeNode(node, analysisId) {
    const wrapper = document.createElement('div');
    wrapper.className = 'project-tree-node';

    if (node.type === 'DIRECTORY') {
        const details = document.createElement('details');
        details.open = true; // 기본적으로 폴더 열어두기
        const summary = document.createElement('summary');
        summary.innerHTML = `📁 ${escapeHtml(node.name)}`;
        details.appendChild(summary);

        const children = document.createElement('div');
        children.className = 'project-tree-children';
        node.children.forEach(child => {
            children.appendChild(createTreeNode(child, analysisId));
        });

        details.appendChild(children);
        wrapper.appendChild(details);
        return wrapper;
    }

    const file = document.createElement('div');
    file.className = 'project-tree-file';
    file.innerHTML = `📄 ${escapeHtml(node.name)}`;
    file.style.cursor = 'pointer';
    
    file.addEventListener('click', () => {
        const fileId = node.file?.fileId || node.file?.id;
        if (fileId) {
            loadFileContent(analysisId, fileId);
        }
    });

    wrapper.appendChild(file);
    return wrapper;
}

async function loadFileContent(analysisId, fileId) {
    try {
        const fileDetail = await ApiService.get(`${API.ANALYSIS}/${analysisId}/files/${fileId}`);
        const contentArea = document.getElementById('fileContent');
        
        if (contentArea) {
            // 소스코드 렌더링
            let html = `<strong>[소스 코드]</strong><br><br>${escapeHtml(fileDetail.content || '내용이 없습니다.')}`;
            
            // 해당 파일의 취약점이 있다면 하단에 표기
            if (fileDetail.vulnerabilities && fileDetail.vulnerabilities.length > 0) {
                html += `\n\n<hr>\n<strong>[🚨 발견된 취약점]</strong>\n`;
                fileDetail.vulnerabilities.forEach(v => {
                    html += `\n- Line ${v.startLine}: [${v.severity}] ${v.type} - ${v.description}`;
                });
            }
            contentArea.innerHTML = html;
        }

    } catch (error) {
        console.error(error);
        alert('파일 내용을 불러오지 못했습니다.');
    }
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    })[m]);
}