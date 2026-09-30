/**
 * 첨부 파일 목록을 기반으로 총 코드 줄 수 및 예상 소요 시간(초)을 계산합니다.
 * @param {Array<{filePath: string, content: string, lineCount?: number}>} files 
 * @returns {{ totalLines: number, estimatedSeconds: number }}
 */
export const calculateEstimatedTime = (files = []) => {
    if (!files || files.length === 0) {
        return { totalLines: 0, estimatedSeconds: 0 };
    }

    // 총 소스코드 라인 수 계산
    const totalLines = files.reduce((sum, file) => {
        const lines = file.lineCount ?? (file.content ? file.content.split('\n').length : 0);
        return sum + lines;
    }, 0);

    // [계산 공식] 기본 준비 3초 + 파일당 2초 + 100줄당 1초 (최소 5초 보장)
    const estimatedSeconds = Math.max(
        5,
        3 + (files.length * 2) + Math.ceil(totalLines / 100)
    );

    return { totalLines, estimatedSeconds };
};