import { Link } from "react-router-dom";
 
function NotFoundPage() {
    return (
        <div className="page">
            <div className="not-found">
                <h2>없는 주소입니다</h2>
                <p>주소를 다시 확인해 주세요.</p>
                <Link to="/">학생 목록으로 돌아가기</Link>
            </div>
        </div>
    );
}
 
export default NotFoundPage;
