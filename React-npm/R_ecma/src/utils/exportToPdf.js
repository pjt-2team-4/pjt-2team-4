import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';

/**
 * 특정 DOM 엘리먼트를 캡처하여 PDF 파일로 다운로드합니다.
 * @param {string} elementId - PDF로 변환할 HTML 엘리먼트의 ID
 * @param {string} fileName - 저장될 PDF 파일 이름
 */
export const downloadPdfReport = async (elementId, fileName = '보안_분석_리포트.pdf') => {
    const input = document.getElementById(elementId);
    if (!input) {
        alert('PDF로 출력할 영역을 찾을 수 없습니다.');
        return;
    }

    try {
        // 1. html2canvas로 해당 DOM 요소를 이미지 캔버스로 변환 (고해상도 scale 적용)
        const canvas = await html2canvas(input, {
            scale: 2, // 캔버스 해상도 2배 향상
            useCORS: true, // 외부 이미지 CORS 허용
            logging: false,
            backgroundColor: '#ffffff'
        });

        // 2. 캔버스 이미지 데이터 추출
        const imgData = canvas.toDataURL('image/png');

        // 3. A4 종이 규격 설정 (단위: mm)
        const pdf = new jsPDF('p', 'mm', 'a4');
        const imgWidth = 210; // A4 가로 너비 (mm)
        const pageHeight = 297; // A4 세로 높이 (mm)
        
        // 비율에 맞춘 이미지 세로 높이 계산
        const imgHeight = (canvas.height * imgWidth) / canvas.width;
        let heightLeft = imgHeight;
        let position = 0;

        // 4. 첫 페이지 추가
        pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
        heightLeft -= pageHeight;

        // 5. 내용이 A4 한 페이지를 넘어가면 다중 페이지 생성
        while (heightLeft > 0) {
            position = heightLeft - imgHeight;
            pdf.addPage();
            pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
            heightLeft -= pageHeight;
        }

        // 6. PDF 파일 다운로드 실행
        pdf.save(fileName);

    } catch (error) {
        console.error('PDF 생성 실패:', error);
        alert('PDF 보고서 생성 중 오류가 발생했습니다.');
    }
};