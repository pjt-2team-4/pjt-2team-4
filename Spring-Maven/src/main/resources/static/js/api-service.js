// js/api-service.js
const ApiService = {
    async post(url, data) {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
        return await response.json();
    },
    
    async get(url) {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
        return await response.json();
    },

    async put(url, data) {
        const response = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
        const text = await response.text(); 
        return text ? JSON.parse(text) : {};
    },

    async delete(url) {
        const response = await fetch(url, { method: 'DELETE' });
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
        const text = await response.text(); 
        return text ? JSON.parse(text) : { message: "삭제 성공" };
    }
};
const requestData = {
    projectId: 1,
    filePath: "src/services/UseService.java", // 파일 경로
    fileName: "UseService.java",             // 👈 빠져있던 파일 이름 추가!
    language: "java",
    content: "seServiseServiseServiseServi",
    fileSizeBytes: new Blob(["seServiseServiseServiseServi"]).size
};