/**
 * js/user-api.js
 */

const STATUS_TEXTS = {
    200: "OK", 201: "Created", 204: "No Content",
    400: "Bad Request", 401: "Unauthorized", 403: "Forbidden",
    404: "Not Found", 405: "Method Not Allowed", 409: "Conflict", 422: "Unprocessable Entity",
    500: "Internal Server Error", 502: "Bad Gateway", 503: "Service Unavailable"
};

function getStatusText(status) {
    return STATUS_TEXTS[status] || "";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function showResult(resultId, response) {
    const result = document.getElementById(resultId);
    const status = response.status;
    const statusText = getStatusText(status) || response.statusText;
    let data = null;

    try {
        const text = await response.text();
        if (text) {
            try { data = JSON.parse(text); } 
            catch { data = text; }
        }
    } catch (error) {
        data = null;
    }

    result.className = "result";
    if (status >= 200 && status < 300) {
        result.classList.add("success");
    } else if (status >= 400 && status < 500) {
        result.classList.add("client-error");
    } else if (status >= 500) {
        result.classList.add("server-error");
    }

    let html = `<div>${status >= 200 && status < 300 ? "🟢" : "🔴"} ${status} ${statusText}</div>`;

    if (data !== null) {
        const formattedData = typeof data === "object" ? JSON.stringify(data, null, 2) : data;
        html += `<pre class="result-detail">${escapeHtml(formattedData)}</pre>`;
    }
    result.innerHTML = html;
}

function showNetworkError(resultId, error) {
    const result = document.getElementById(resultId);
    result.className = "result network-error";
    result.innerHTML = `<div>🟣 네트워크 오류</div><pre class="result-detail">${escapeHtml(error.message)}</pre>`;
}

/* =========================
 * GET /api/user (전체 회원 조회)
 * ========================= */
async function getAllUsers() {
    const result = document.getElementById("result-all");
    result.className = "result waiting";
    result.style.display = "block"; 
    result.innerHTML = "<div style='text-align:center; padding: 20px;'>⏳ 요청 중...</div>";

    try {
        const response = await fetch("/api/user", { method: "GET" });
        const data = await response.json();

        if (response.ok) {
            result.className = "result success";

            if (!Array.isArray(data) || data.length === 0) {
                result.innerHTML = `
                    <div style="margin-bottom: 15px;">🟢 ${response.status} ${getStatusText(response.status)}</div>
                    <div class="result-detail" style="text-align: center; padding: 30px;">등록된 회원이 없습니다.</div>
                `;
                return;
            }

            // 회원 정보에 맞게 테이블 축소 (ID, 이메일, 관리)
            let tableHtml = `
                <div class="result-status" style="margin-bottom: 15px;">🟢 ${response.status} ${getStatusText(response.status)}</div>
                <table class="result-table" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background-color: #f8f9fa; border-bottom: 2px solid #ddd;">
                            <th style="padding: 12px; text-align: left;">ID</th>
                            <th style="padding: 12px; text-align: left;">이메일</th>
                            <th style="padding: 12px; text-align: center;">관리</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            data.forEach(user => {
                tableHtml += `
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 12px;">${escapeHtml(user.id ?? "")}</td>
                        <td style="padding: 12px;"><strong>${escapeHtml(user.email ?? "")}</strong></td>
                        <td class="action-cell" style="padding: 12px; text-align: center;">
                            <a href="/edit-user/${user.id}" class="row-button edit-button" style="text-decoration:none;">수정</a>
                            <button type="button" class="row-button delete-row-button" onclick="deleteUserById(${user.id})">삭제</button>
                        </td>
                    </tr>
                `;
            });

            tableHtml += `</tbody></table>`;
            result.innerHTML = tableHtml;
        } else {
            await showResult("result-all", response);
        }
    } catch (error) {
        showNetworkError("result-all", error);
    }
}

/* =========================
 * GET /api/user/{id}
 * ========================= */
async function searchById(event) {
    event.preventDefault();
    const id = document.getElementById("userId").value;
    if (!id) return;

    const result = document.getElementById("result-id");
    result.className = "result waiting";
    result.innerHTML = "⏳ 요청 중...";

    try {
        const response = await fetch(`/api/user/${encodeURIComponent(id)}`, { method: "GET" });
        await showResult("result-id", response);
    } catch (error) {
        showNetworkError("result-id", error);
    }
}

/* =========================
 * GET /api/user/email/{email}
 * ========================= */
async function searchByEmail(event) {
    event.preventDefault();
    const email = document.getElementById("userEmail").value;
    if (!email) return;

    const result = document.getElementById("result-email");
    result.className = "result waiting";
    result.innerHTML = "⏳ 요청 중...";

    try {
        const response = await fetch(`/api/user/email/${encodeURIComponent(email)}`, { method: "GET" });
        await showResult("result-email", response);
    } catch (error) {
        showNetworkError("result-email", error);
    }
}

/* =========================
 * DELETE /api/user/{id}
 * ========================= */
async function deleteUserById(id) {
    if (!confirm(`회원 ID ${id}를 삭제하시겠습니까?`)) return;

    try {
        const response = await fetch(`/api/user/${encodeURIComponent(id)}`, { method: "DELETE" });

        if (response.ok) {
            alert(`회원 ID ${id}가 삭제되었습니다.`);
            getAllUsers(); // 갱신
        } else {
            let errorData = "";
            try { errorData = await response.text(); } catch (e) { }
            alert(`${response.status} ${getStatusText(response.status)}\n${errorData}`);
        }
    } catch (error) {
        alert(`네트워크 오류: ${error.message}`);
    }
}