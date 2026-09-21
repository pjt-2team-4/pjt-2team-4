/**
 * js/student-api.js
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
            try {
                data = JSON.parse(text);
            } catch {
                data = text;
            }
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
    result.innerHTML = `
        <div>🟣 네트워크 오류</div>
        <pre class="result-detail">${escapeHtml(error.message)}</pre>
    `;
}

/* =========================
 * GET /api/user (전체 학생 조회 - 하단 큰 영역에 출력)
 * ========================= */
async function getAllStudents() {
    const result = document.getElementById("result-all");
    result.className = "result waiting";
    result.style.display = "block"; // flex 해제
    result.innerHTML = "<div style='text-align:center; padding: 20px;'>⏳ 요청 중...</div>";

    try {
        const response = await fetch("/api/user", { method: "GET" });
        const data = await response.json();

        if (response.ok) {
            result.className = "result success";

            if (!Array.isArray(data) || data.length === 0) {
                result.innerHTML = `
                    <div style="margin-bottom: 15px;">🟢 ${response.status} ${getStatusText(response.status)}</div>
                    <div class="result-detail" style="text-align: center; padding: 30px;">등록된 학생이 없습니다.</div>
                `;
                return;
            }

            let tableHtml = `
                <div class="result-status" style="margin-bottom: 15px;">🟢 ${response.status} ${getStatusText(response.status)}</div>
                <table class="result-table" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background-color: #f8f9fa; border-bottom: 2px solid #ddd;">
                            <th style="padding: 12px; text-align: left;">ID</th>
                            <th style="padding: 12px; text-align: left;">이름</th>
                            <th style="padding: 12px; text-align: left;">학번</th>
                            <th style="padding: 12px; text-align: left;">주소</th>
                            <th style="padding: 12px; text-align: left;">전화번호</th>
                            <th style="padding: 12px; text-align: left;">이메일</th>
                            <th style="padding: 12px; text-align: left;">생년월일</th>
                            <th style="padding: 12px; text-align: center;">관리</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            data.forEach(student => {
                const address = student.detail?.address || "-";
                const phone = student.detail?.phoneNumber || "-";
                const email = student.detail?.email || "-";
                const dob = student.detail?.dateOfBirth || "-";

                tableHtml += `
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 12px;">${escapeHtml(student.id ?? "")}</td>
                        <td style="padding: 12px;"><strong>${escapeHtml(student.name ?? "")}</strong></td>
                        <td style="padding: 12px;"><span class="student-number">${escapeHtml(student.studentNumber ?? "")}</span></td>
                        <td style="padding: 12px;">${escapeHtml(address)}</td>
                        <td style="padding: 12px;">${escapeHtml(phone)}</td>
                        <td style="padding: 12px;">${escapeHtml(email)}</td>
                        <td style="padding: 12px;">${escapeHtml(dob)}</td>
                        <td class="action-cell" style="padding: 12px; text-align: center;">
                            <a href="/edit-user/${student.id}" class="row-button edit-button" style="text-decoration:none;">수정</a>
                            <button type="button" class="row-button delete-row-button" onclick="deleteStudentById(${student.id})">삭제</button>
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
    const id = document.getElementById("studentId").value;
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
 * GET /api/user/number/{studentNumber}
 * ========================= */
async function searchByStudentNumber(event) {
    event.preventDefault();
    const studentNumber = document.getElementById("studentNumber").value;
    if (!studentNumber) return;

    const result = document.getElementById("result-number");
    result.className = "result waiting";
    result.innerHTML = "⏳ 요청 중...";

    try {
        const response = await fetch(`/api/user/number/${encodeURIComponent(studentNumber)}`, { method: "GET" });
        await showResult("result-number", response);
    } catch (error) {
        showNetworkError("result-number", error);
    }
}

/* =========================
 * DELETE /api/user/{id} (Table Row Button)
 * ========================= */
async function deleteStudentById(id) {
    if (!confirm(`학생 ID ${id}를 삭제하시겠습니까?`)) return;

    try {
        const response = await fetch(`/api/user/${encodeURIComponent(id)}`, { method: "DELETE" });

        if (response.ok) {
            alert(`학생 ID ${id}가 삭제되었습니다.`);
            getAllStudents(); // 삭제 후 전체 목록 자동 갱신
        } else {
            let errorData = "";
            try { errorData = await response.text(); } catch (e) { }
            alert(`${response.status} ${getStatusText(response.status)}\n${errorData}`);
        }
    } catch (error) {
        alert(`네트워크 오류: ${error.message}`);
    }
}