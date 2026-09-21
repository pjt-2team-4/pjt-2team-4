# React 컴포넌트 설계서 — 온라인 도서관 시스템

## 문서 정보
| 항목 | 내용 |
|------|------|
| **프로젝트명** | 온라인 도서관 시스템 |
| **작성자** | 홍길동 |
| **작성일** | 2025-05-26 |
| **버전** | v1.0 |
| **검토자** | 김철수 |

---

## 0. 컴포넌트 설계 과정 (도서 목록 화면 기준)

### STEP 1 — 와이어프레임에서 컴포넌트 경계 그리기

UI 화면설계서의 도서 목록 와이어프레임에 사각형을 그려 컴포넌트 경계를 찾습니다.

```
┌─────────────────────────────────────────────────────────────────┐
│ ┌─ Header 컴포넌트 ───────────────────────────────────────────┐ │
│ │ 📚 도서관   [도서목록]  [내대출이력]            [로그인]      │ │
│ └────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ ┌─ BookListPage (페이지 컴포넌트, 상태 소유) ─────────────────┐ │
│ │                                                             │ │
│ │  ┌─ BookSearch ──────────────────────┐                     │ │
│ │  │  [검색 입력창]          [검색 버튼] │ ← 독립적으로 변경    │ │
│ │  └────────────────────────────────────┘                    │ │
│ │                                                             │ │
│ │  ┌─ CategoryFilter ─────────────────────────────────────┐  │ │
│ │  │  [전체] [소설] [IT] [역사] [과학] [기타]               │  │ │
│ │  └──────────────────────────────────────────────────────┘  │ │
│ │                                                             │ │
│ │  ┌─ BookDataGrid ────────────────────────────────────────┐  │ │
│ │  │  도서명 │ 저자 │ 카테고리 │ 재고뱃지 │ 대출버튼        │  │ │
│ │  │  ....   │ ...  │   ...    │  Badge   │  Button        │  │ │
│ │  └──────────────────────────────────────────────────────┘  │ │
│ │                                                             │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│         [LoanDialog — 대출 버튼 클릭 시 나타나는 모달]           │
└─────────────────────────────────────────────────────────────────┘
```

> **분리 근거:**
> - `Header` — 모든 페이지에서 동일하게 쓰임 → 공통 컴포넌트
> - `BookSearch` — 검색창 디자인이 바뀌어도 테이블에 영향 없음 → 분리
> - `CategoryFilter` — 카테고리 종류가 늘어날 수 있음 → 분리
> - `BookDataGrid` — 테이블 컬럼 구조가 복잡 → 별도 파일
> - `LoanDialog` — 모달은 독립 생명주기 → 분리

---

### STEP 2 — 컴포넌트 트리 (계층 구조)

```
BookListPage                           ← 상태 소유자, 자식 컴포넌트 조립
  ├── BookSearch                        ← (Props: onSearch) 검색 입력
  ├── CategoryFilter                    ← (Props: value, onChange) 카테고리 선택
  ├── BookDataGrid                      ← (Props: rows, loading, onLoan) 테이블
  │     └── (내부) StockBadge           ← availableCopies 값으로 색상 결정
  │     └── (내부) LoanButton           ← availableCopies=0 이면 비활성
  └── LoanDialog                        ← (Props: open, bookId, onSuccess) 모달

MyLoanPage                             ← 상태 소유자
  ├── LoanStatusFilter                  ← (Props: value, onChange) 상태 탭
  └── MyLoanDataGrid                    ← (Props: rows, loading, onReturn) 테이블
```

**컴포넌트별 책임 요약:**
| 컴포넌트명 | 파일 위치 | 주요 책임 | 재사용성 |
|------------|-----------|-----------|----------|
| `BookListPage` | `pages/BookList/index.jsx` | 상태 관리, API 호출, 자식 조립 | 낮음 |
| `BookSearch` | `pages/BookList/components/BookSearch.jsx` | 검색어 입력 및 검색 이벤트 발생 | 보통 |
| `CategoryFilter` | `pages/BookList/components/CategoryFilter.jsx` | 카테고리 선택 | 보통 |
| `BookDataGrid` | `pages/BookList/components/BookDataGrid.jsx` | 도서 목록 테이블, 재고 뱃지, 대출 버튼 | 낮음 |
| `LoanDialog` | `pages/BookList/components/LoanDialog.jsx` | 대출 신청 확인 모달 | 보통 |
| `MyLoanPage` | `pages/MyLoan/index.jsx` | 내 대출이력 조회, 반납 신청 | 낮음 |
| `Header` | `components/layout/Header.jsx` | 로고, 메뉴, 로그인 상태 | 높음 |

---

### STEP 3 — 상태 위치 결정

> "이 상태를 누가 필요로 하는가?"를 기준으로 결정했습니다.

| 상태 | 위치 결정 | 결정 근거 |
|------|-----------|-----------|
| `keyword` | 로컬 `useState` (BookListPage) | BookListPage 내부에서만 사용. 서버 무관한 UI 입력값 |
| `category` | 로컬 `useState` (BookListPage) | BookListPage 내부에서만 사용. URL 파라미터 동기화 불필요 |
| `isLoanDialogOpen` | 로컬 `useState` (BookListPage) | 모달 열림/닫힘은 UI 상태. 전역 관리 불필요 |
| `selectedBook` | 로컬 `useState` (BookListPage) | LoanDialog에 전달하기 위해 공통 부모(BookListPage)에 위치 |
| `books` | `bookStore` (Zustand) | API에서 가져온 서버 데이터. 검색/필터/페이지 변경 시 갱신 필요 |
| `loans` | `loanStore` (Zustand) | API에서 가져온 서버 데이터 |
| `token` | `authStore` (Zustand) | Header, BookListPage, LoanDialog 등 여러 곳에서 접근 필요 |
| `statusFilter` | 로컬 `useState` (MyLoanPage) | MyLoanPage 내 클라이언트 필터링용. 서버 요청 없이 처리 |

---

### STEP 4 — Props 흐름 다이어그램

```
BookListPage (상태 소유)
    │
    │  데이터 (↓)                         이벤트 콜백 (↑)
    │  ─────────────────────────────────────────────────────
    ├─→ BookSearch
    │     books, loading (직접 전달 안 함)    onSearch(keyword) ←─┐
    │                                                            │
    │     [사용자가 검색어 입력 후 Enter]─────────────────────────┘
    │
    ├─→ CategoryFilter
    │     value={category}                   onChange(cat) ←─────┐
    │                                                            │
    │     [사용자가 카테고리 탭 클릭]──────────────────────────────┘
    │
    ├─→ BookDataGrid
    │     rows={books}                       onLoan(book) ←──────┐
    │     loading={loading}                                      │
    │     pageNo, totalPages                                     │
    │     onPageChange                                           │
    │                                                            │
    │     [대출 버튼 클릭]───────────────────────────────────────┘
    │       (BookListPage에서 로그인 체크 후 LoanDialog 열기)
    │
    └─→ LoanDialog
          open={isLoanDialogOpen}            onSuccess() ←───────┐
          bookId={selectedBook.id}                               │
          bookTitle, author                                      │
                                                                 │
          [대출 신청 확인 클릭 → API 성공]────────────────────────┘
```

---

## 컴포넌트 1: BookListPage (도서 목록 페이지)

### 1.1 컴포넌트 개요
| 항목 | 내용 |
|------|------|
| **컴포넌트명** | `BookListPage` |
| **위치** | `src/pages/BookList/index.jsx` |
| **타입** | 페이지 컴포넌트 (상태 소유자) |
| **복잡도** | 복합 (100줄 이상) |
| **상태 관리** | Zustand (`bookStore`, `authStore`) + 로컬 상태 |

### 1.2 기능 요구사항
- [x] 도서 목록 조회 (서버사이드 페이지네이션)
- [x] 키워드 검색 (도서명, 저자)
- [x] 카테고리 필터링 (전체/소설/IT/역사/과학/기타)
- [x] 대출 신청 버튼 (로그인 시 활성화, 비로그인 시 로그인 유도)
- [x] 로딩 / 에러 / 빈 데이터 상태 처리

### 1.3 Props 인터페이스
> `BookListPage`는 라우터에서 직접 렌더링하는 페이지 컴포넌트이므로 Props를 받지 않습니다.

### 1.4 로컬 상태 (useState)
| 상태명 | 타입 | 초기값 | 용도 | 위치 선택 근거 |
|--------|------|--------|------|----------------|
| `keyword` | `string` | `""` | 검색 입력창 현재 값 | BookListPage 내부에서만 사용 |
| `category` | `string` | `"전체"` | 선택된 카테고리 | 이 컴포넌트 안에서만 사용 |
| `selectedBook` | `Book \| null` | `null` | LoanDialog에 전달할 선택 도서 | BookListPage와 LoanDialog의 공통 부모 |
| `isLoanDialogOpen` | `boolean` | `false` | LoanDialog 열림 여부 | UI 상태, 전역 불필요 |

### 1.5 Zustand 전역 상태 (bookStore, authStore)
| 상태/액션 | 스토어 | 이 컴포넌트에서의 용도 |
|-----------|--------|----------------------|
| `books` | `bookStore` | BookDataGrid의 rows에 전달 |
| `loading` | `bookStore` | BookDataGrid의 loading에 전달 |
| `pageNo` | `bookStore` | BookDataGrid의 현재 페이지 |
| `totalPages` | `bookStore` | 전체 페이지 수 계산 |
| `fetchBooks(params)` | `bookStore` | 마운트 시, 검색/필터/페이지 변경 시 |
| `token` | `authStore` | null이면 대출 버튼 클릭 시 로그인 페이지로 이동 |

### 1.6 API 연동
| API | 메서드 | 호출 시점 | 파라미터 |
|-----|--------|-----------|---------|
| `/api/books` | `GET` | 마운트 시, 검색/필터/페이지 변경 시 | `keyword`, `category`, `page=0`, `size=10` |

### 1.7 구현 코드
```jsx
// src/pages/BookList/index.jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBookStore } from '@/store/bookStore';
import { useAuthStore } from '@/store/authStore';
import BookDataGrid from './components/BookDataGrid';
import BookSearch from './components/BookSearch';
import CategoryFilter from './components/CategoryFilter';
import LoanDialog from './components/LoanDialog';
import { toast } from 'sonner';

export default function BookListPage() {

  // ── 1. 로컬 상태 ─────────────────────────────────────────────
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('전체');
  const [selectedBook, setSelectedBook] = useState(null);
  const [isLoanDialogOpen, setIsLoanDialogOpen] = useState(false);

  // ── 2. Zustand 전역 상태 ──────────────────────────────────────
  const { books, loading, pageNo, totalPages, fetchBooks } = useBookStore();
  const { token } = useAuthStore();
  const navigate = useNavigate();

  // ── 3. 데이터 초기 로드 (마운트 1회) ─────────────────────────
  useEffect(() => {
    fetchBooks({ keyword: '', category: '전체', page: 0 });
  }, []);

  // ── 4. 이벤트 핸들러 ──────────────────────────────────────────
  // 검색: BookSearch에서 onSearch(keyword) 콜백으로 받음
  const handleSearch = (kw) => {
    setKeyword(kw);
    fetchBooks({ keyword: kw, category, page: 0 }); // 페이지 0으로 리셋
  };

  // 카테고리 변경: CategoryFilter에서 onChange(cat) 콜백으로 받음
  const handleCategoryChange = (cat) => {
    setCategory(cat);
    fetchBooks({ keyword, category: cat, page: 0 }); // 페이지 0으로 리셋
  };

  // 페이지 변경: BookDataGrid에서 onPageChange(page) 콜백으로 받음
  const handlePageChange = (newPage) => {
    fetchBooks({ keyword, category, page: newPage });
  };

  // 대출 버튼: BookDataGrid에서 onLoan(book) 콜백으로 받음
  const handleLoanClick = (book) => {
    if (!token) {
      toast.error('로그인이 필요합니다');
      navigate('/login');
      return;
    }
    setSelectedBook(book);
    setIsLoanDialogOpen(true);
  };

  // ── 5. 렌더링 ─────────────────────────────────────────────────
  return (
    <div className="container mx-auto py-6 space-y-4">
      <h1 className="text-2xl font-bold">📚 도서 목록</h1>

      {/* 검색 + 필터 */}
      <div className="flex gap-2">
        <BookSearch onSearch={handleSearch} />
        <CategoryFilter value={category} onChange={handleCategoryChange} />
      </div>

      {/* 도서 테이블 — Props로 데이터와 콜백 전달 */}
      <BookDataGrid
        rows={books}
        loading={loading}
        pageNo={pageNo}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        onLoan={handleLoanClick}          {/* 자식 이벤트 → 부모 핸들러 */}
      />

      {/* 대출 신청 모달 — selectedBook이 있을 때만 렌더링 */}
      {selectedBook && (
        <LoanDialog
          open={isLoanDialogOpen}
          onOpenChange={setIsLoanDialogOpen}
          bookId={selectedBook.id}
          bookTitle={selectedBook.title}
          author={selectedBook.author}
        />
      )}
    </div>
  );
}
```

---

## 컴포넌트 2: BookDataGrid (도서 목록 테이블)

### 2.1 컴포넌트 개요
| 항목 | 내용 |
|------|------|
| **컴포넌트명** | `BookDataGrid` |
| **위치** | `src/pages/BookList/components/BookDataGrid.jsx` |
| **타입** | 기능 컴포넌트 |
| **복잡도** | 보통 (50-100줄) |
| **상태 관리** | 상태 없음 — Props만 사용하는 순수 표시 컴포넌트 |

### 2.2 기능 요구사항
- [x] MUI DataGrid로 도서 목록 테이블 렌더링
- [x] 재고 수량에 따른 Badge 색상 (초록 / 빨강)
- [x] 재고 0권 시 대출 버튼 비활성화 및 "품절" 표시
- [x] 서버사이드 페이지네이션

### 2.3 Props 흐름 다이어그램
```
BookListPage (부모)
    │  rows={books}              ← 도서 목록 배열 전달 (↓)
    │  loading={loading}         ← 로딩 상태 전달 (↓)
    │  pageNo={pageNo}           ← 현재 페이지 전달 (↓)
    │  totalPages={totalPages}   ← 전체 페이지 수 전달 (↓)
    │  onPageChange={fn}         ← 페이지 변경 콜백 전달 (↓)
    │  onLoan={fn}               ← 대출 버튼 콜백 전달 (↓)
    ↓
BookDataGrid
    │
    │  [페이지 변경] → onPageChange(newPage) →───────→ 부모 (↑)
    │  [대출 버튼 클릭] → onLoan(book)       →───────→ 부모 (↑)
```

### 2.4 Props 인터페이스
| Props명 | 타입 | 필수여부 | 기본값 | 설명 |
|---------|------|----------|--------|------|
| `rows` | `Book[]` | 필수 | — | 테이블에 표시할 도서 목록 |
| `loading` | `boolean` | 선택 | `false` | true이면 Skeleton 표시 |
| `pageNo` | `number` | 필수 | — | 현재 페이지 번호 (0-index) |
| `totalPages` | `number` | 필수 | — | 전체 페이지 수 |
| `onPageChange` | `(page: number) => void` | 필수 | — | 페이지 이동 버튼 클릭 시 |
| `onLoan` | `(book: Book) => void` | 필수 | — | 대출 버튼 클릭 시 (부모가 로그인 체크) |

### 2.5 로컬 상태
> 상태 없음 — 모든 데이터는 Props로 전달받으며, 이벤트는 콜백 Props로 부모에게 전달합니다.

### 2.6 구현 코드
```jsx
// src/pages/BookList/components/BookDataGrid.jsx
import { DataGrid } from '@mui/x-data-grid';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';

// 컬럼 정의를 함수로 분리: onLoan 콜백을 클로저로 캡처
const buildColumns = (onLoan) => [
  { field: 'title',    headerName: '도서명',   flex: 2, minWidth: 150 },
  { field: 'author',   headerName: '저자',     flex: 1, minWidth: 100 },
  { field: 'category', headerName: '카테고리', width: 100 },
  {
    field: 'availableCopies',
    headerName: '재고',
    width: 90,
    renderCell: ({ row }) => (
      // availableCopies에 따라 Badge 색상 조건부 변경
      <Badge variant={row.availableCopies > 0 ? 'success' : 'destructive'}>
        {row.availableCopies > 0 ? `●${row.availableCopies}권` : '○품절'}
      </Badge>
    ),
  },
  {
    field: 'action',
    headerName: '대출',
    width: 90,
    sortable: false,
    renderCell: ({ row }) => (
      <Button
        size="sm"
        disabled={row.availableCopies === 0}   // 재고 0 → 비활성
        onClick={() => onLoan(row)}             // 부모 콜백 호출
      >
        {row.availableCopies > 0 ? '대출' : '품절'}
      </Button>
    ),
  },
];

export default function BookDataGrid({
  rows, loading, pageNo, totalPages, onPageChange, onLoan
}) {
  return (
    <DataGrid
      rows={rows}
      columns={buildColumns(onLoan)}
      loading={loading}
      // 서버사이드 페이지네이션: 클라이언트가 직접 정렬/필터 안 함
      paginationMode="server"
      rowCount={totalPages * 10}
      paginationModel={{ page: pageNo, pageSize: 10 }}
      onPaginationModelChange={(model) => onPageChange(model.page)}
      disableRowSelectionOnClick
      autoHeight
    />
  );
}
```

---

## 컴포넌트 3: LoanDialog (대출 신청 모달)

### 3.1 컴포넌트 개요
| 항목 | 내용 |
|------|------|
| **컴포넌트명** | `LoanDialog` |
| **위치** | `src/pages/BookList/components/LoanDialog.jsx` |
| **타입** | 기능 컴포넌트 (모달) |
| **복잡도** | 보통 (50-100줄) |
| **상태 관리** | 로컬 `isLoading` + Zustand `loanStore` |

### 3.2 기능 요구사항
- [x] 선택된 도서 정보 표시 (도서명, 저자)
- [x] 반납 예정일 자동 계산 (오늘 + 14일)
- [x] 대출 신청 API 호출 (`POST /api/loans`)
- [x] 성공 → 토스트 메시지 + 모달 닫힘
- [x] 실패 → 토스트 에러 메시지

### 3.3 Props 흐름 다이어그램
```
BookListPage (부모)
    │  open={isLoanDialogOpen}          ← 모달 열림 제어 (↓)
    │  onOpenChange={setIsLoanDialog..} ← 닫기 콜백 (↓)
    │  bookId={selectedBook.id}         ← 대출할 도서 ID (↓)
    │  bookTitle={selectedBook.title}   ← 표시용 도서명 (↓)
    │  author={selectedBook.author}     ← 표시용 저자 (↓)
    ↓
LoanDialog
    │
    │  [취소 버튼] → onOpenChange(false) ────→ 부모 (↑)
    │  [대출 신청 성공] → onOpenChange(false) → 부모 (↑)
    │  (내부에서 loanStore.createLoan(bookId) 직접 호출)
```

### 3.4 Props 인터페이스
| Props명 | 타입 | 필수여부 | 기본값 | 설명 |
|---------|------|----------|--------|------|
| `open` | `boolean` | 필수 | — | 모달 표시 여부 |
| `onOpenChange` | `(open: boolean) => void` | 필수 | — | 모달 열기/닫기 제어 |
| `bookId` | `number` | 필수 | — | 대출 신청할 도서 ID |
| `bookTitle` | `string` | 필수 | — | 모달에 표시할 도서명 |
| `author` | `string` | 필수 | — | 모달에 표시할 저자명 |

### 3.5 로컬 상태 (useState)
| 상태명 | 타입 | 초기값 | 용도 | 위치 선택 근거 |
|--------|------|--------|------|----------------|
| `isLoading` | `boolean` | `false` | API 호출 중 버튼 비활성화 | 이 모달 안에서만 사용 |

### 3.6 Zustand 연결 (loanStore)
| 액션 | 타입 | 용도 |
|------|------|------|
| `createLoan(bookId)` | `(id: number) => Promise` | 대출 신청 API 호출 |

### 3.7 구현 코드
```jsx
// src/pages/BookList/components/LoanDialog.jsx
import { useState } from 'react';
import { addDays, format } from 'date-fns';
import {
  Dialog, DialogContent, DialogHeader,
  DialogTitle, DialogFooter
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { useLoanStore } from '@/store/loanStore';
import { toast } from 'sonner';

export default function LoanDialog({
  open, onOpenChange, bookId, bookTitle, author
}) {
  // ── 1. 로컬 상태 ─────────────────────────────
  const [isLoading, setIsLoading] = useState(false);

  // ── 2. Zustand 액션 ──────────────────────────
  const { createLoan } = useLoanStore();

  // ── 3. 반납 예정일 계산 (오늘 + 14일) ─────────
  const dueDate = format(addDays(new Date(), 14), 'yyyy-MM-dd');

  // ── 4. 대출 신청 처리 ─────────────────────────
  const handleConfirm = async () => {
    setIsLoading(true);
    try {
      await createLoan(bookId);
      toast.success('대출 신청이 완료되었습니다');
      onOpenChange(false);          // 성공 → 모달 닫기 (부모에게 위임)
    } catch (err) {
      // 서버 에러 메시지 우선, 없으면 기본 메시지
      toast.error(err?.response?.data?.message || '대출 신청에 실패했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  // ── 5. 렌더링 ────────────────────────────────
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>대출 신청</DialogTitle>
        </DialogHeader>

        <div className="space-y-3 py-2">
          <p>📖 <strong>도서명:</strong> {bookTitle}</p>
          <p>✍ <strong>저자:</strong> {author}</p>
          <div className="rounded-md bg-blue-50 p-3 text-sm">
            <p>대출 기간: <strong>14일</strong></p>
            <p>반납 예정일: <strong>{dueDate}</strong></p>
          </div>
          <p className="text-xs text-yellow-600">
            ⚠ 반납일을 초과하면 연체료가 부과됩니다.
          </p>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            취소
          </Button>
          <Button onClick={handleConfirm} disabled={isLoading}>
            {isLoading ? '신청 중...' : '대출 신청'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
```

---

## 컴포넌트 4: MyLoanPage (내 대출이력 페이지)

### 4.1 컴포넌트 개요
| 항목 | 내용 |
|------|------|
| **컴포넌트명** | `MyLoanPage` |
| **위치** | `src/pages/MyLoan/index.jsx` |
| **타입** | 페이지 컴포넌트 |
| **복잡도** | 복합 (100줄 이상) |
| **상태 관리** | Zustand (`loanStore`) + 로컬 상태 |

### 4.2 Props 인터페이스
> 페이지 컴포넌트이므로 Props 없음

### 4.3 로컬 상태 (useState)
| 상태명 | 타입 | 초기값 | 용도 | 위치 선택 근거 |
|--------|------|--------|------|----------------|
| `statusFilter` | `string` | `"ALL"` | 상태 탭 선택 값 | MyLoanPage 내 클라이언트 필터링, 전역 불필요 |

### 4.4 Zustand 연결 (loanStore)
| 상태/액션 | 용도 |
|-----------|------|
| `loans` | 대출이력 테이블의 rows 데이터 |
| `loading` | 로딩 스피너 조건부 렌더링 |
| `fetchMyLoans()` | 마운트 시 내 대출이력 API 호출 |
| `returnBook(loanId)` | 반납 버튼 클릭 시 API 호출 |

### 4.5 상태별 Badge 색상 매핑
| 상태값 | Badge variant | 텍스트 | 반납 버튼 |
|--------|---------------|--------|-----------|
| `REQUESTED` | `warning` (노란색) | 승인대기 | - |
| `APPROVED` | `default` (파란색) | 대출승인 | - |
| `BORROWED` | `success` (초록색) | 대출중 | 표시 |
| `RETURNED` | `secondary` (회색) | 반납완료 | - |
| `OVERDUE` | `destructive` (빨간색) | 연체 | 표시 |
| `CANCELLED` | `secondary` (회색) | 취소됨 | - |

### 4.6 구현 코드
```jsx
// src/pages/MyLoan/index.jsx
import { useState, useEffect } from 'react';
import { useLoanStore } from '@/store/loanStore';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { DataGrid } from '@mui/x-data-grid';
import { toast } from 'sonner';

const STATUS_TABS = [
  { value: 'ALL',       label: '전체' },
  { value: 'BORROWED',  label: '대출중' },
  { value: 'OVERDUE',   label: '연체' },
  { value: 'RETURNED',  label: '반납완료' },
  { value: 'REQUESTED', label: '승인대기' },
];

const BADGE_MAP = {
  REQUESTED: { variant: 'warning',     label: '승인대기' },
  APPROVED:  { variant: 'default',     label: '대출승인' },
  BORROWED:  { variant: 'success',     label: '대출중' },
  RETURNED:  { variant: 'secondary',   label: '반납완료' },
  OVERDUE:   { variant: 'destructive', label: '연체' },
  CANCELLED: { variant: 'secondary',   label: '취소됨' },
};

export default function MyLoanPage() {

  // ── 1. 로컬 상태 ─────────────────────────────
  const [statusFilter, setStatusFilter] = useState('ALL');

  // ── 2. Zustand ───────────────────────────────
  const { loans, loading, fetchMyLoans, returnBook } = useLoanStore();

  // ── 3. 마운트 시 데이터 로드 ──────────────────
  useEffect(() => {
    fetchMyLoans();
  }, []);

  // ── 4. 클라이언트 사이드 필터링 ───────────────
  // 서버에 요청하지 않고 이미 받아온 loans를 프론트에서 필터링
  const filteredLoans = statusFilter === 'ALL'
    ? loans
    : loans.filter((l) => l.status === statusFilter);

  // ── 5. 반납 처리 ──────────────────────────────
  const handleReturn = async (loanId) => {
    if (!window.confirm('반납 신청하시겠습니까?')) return;
    try {
      await returnBook(loanId);
      toast.success('반납 신청이 완료되었습니다');
    } catch {
      toast.error('반납 신청에 실패했습니다');
    }
  };

  // ── 6. DataGrid 컬럼 정의 ─────────────────────
  const columns = [
    { field: 'bookTitle',  headerName: '도서명',    flex: 2 },
    { field: 'loanDate',   headerName: '대출일',    width: 120 },
    { field: 'dueDate',    headerName: '반납예정일', width: 120 },
    {
      field: 'status', headerName: '상태', width: 110,
      renderCell: ({ value }) => {
        const { variant, label } = BADGE_MAP[value] ?? {};
        return <Badge variant={variant}>{label}</Badge>;
      },
    },
    {
      field: 'action', headerName: '액션', width: 90, sortable: false,
      renderCell: ({ row }) =>
        ['BORROWED', 'OVERDUE'].includes(row.status) ? (
          <Button size="sm" variant="outline"
                  onClick={() => handleReturn(row.id)}>
            반납
          </Button>
        ) : null,
    },
  ];

  // ── 7. 렌더링 ─────────────────────────────────
  return (
    <div className="container mx-auto py-6 space-y-4">
      <h1 className="text-2xl font-bold">📋 내 대출이력</h1>

      {/* 상태 탭 필터 */}
      <Tabs value={statusFilter} onValueChange={setStatusFilter}>
        <TabsList>
          {STATUS_TABS.map((t) => (
            <TabsTrigger key={t.value} value={t.value}>
              {t.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      {/* 빈 데이터 처리 */}
      {!loading && filteredLoans.length === 0 && (
        <p className="text-center text-neutral-500 py-12">
          해당 상태의 대출이력이 없습니다.
        </p>
      )}

      {/* 대출이력 테이블 */}
      <DataGrid
        rows={filteredLoans}
        columns={columns}
        loading={loading}
        autoHeight
        disableRowSelectionOnClick
      />
    </div>
  );
}
```

---

## Zustand 스토어 설계

### bookStore.js
```javascript
// src/store/bookStore.js
import { create } from 'zustand';
import { fetchBooksApi } from '@/api/bookApi';

export const useBookStore = create((set) => ({
  // ── 상태 ──────────────────────────────────
  books: [],
  loading: false,
  pageNo: 0,
  totalPages: 0,

  // ── 액션 ──────────────────────────────────
  fetchBooks: async ({ keyword = '', category = '전체', page = 0 } = {}) => {
    set({ loading: true });
    try {
      const res = await fetchBooksApi({ keyword, category, page, size: 10 });
      set({
        books: res.data.content,
        pageNo: res.data.page.number,
        totalPages: res.data.page.totalPages,
      });
    } catch (err) {
      console.error('도서 목록 조회 실패:', err);
    } finally {
      set({ loading: false });
    }
  },
}));
```

### loanStore.js
```javascript
// src/store/loanStore.js
import { create } from 'zustand';
import { getMyLoansApi, createLoanApi, returnBookApi } from '@/api/loanApi';

export const useLoanStore = create((set, get) => ({
  // ── 상태 ──────────────────────────────────
  loans: [],
  loading: false,

  // ── 액션 ──────────────────────────────────
  fetchMyLoans: async () => {
    set({ loading: true });
    try {
      const res = await getMyLoansApi();
      set({ loans: res.data });
    } finally {
      set({ loading: false });
    }
  },

  createLoan: async (bookId) => {
    // 에러는 호출한 컴포넌트(LoanDialog)에서 catch
    const res = await createLoanApi(bookId);
    return res.data; // { loanId, dueDate, status }
  },

  returnBook: async (loanId) => {
    await returnBookApi(loanId);
    get().fetchMyLoans(); // 반납 후 목록 새로고침
  },
}));
```

### authStore.js
```javascript
// src/store/authStore.js
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { loginApi } from '@/api/authApi';

export const useAuthStore = create(
  persist(
    (set) => ({
      // ── 상태 ──────────────────────────────
      token: null,
      email: '',

      // ── 액션 ──────────────────────────────
      login: async (email, password) => {
        const res = await loginApi(email, password);
        set({ token: res.data.accessToken, email: res.data.user.email });
      },

      logout: () => {
        set({ token: null, email: '' });
      },
    }),
    {
      name: 'auth-storage',    // localStorage 키
      partialize: (s) => ({ token: s.token, email: s.email }),
    }
  )
);
```

---

## 파일 구조 전체 요약

```
src/
├── pages/
│   ├── BookList/
│   │   ├── index.jsx                  ← BookListPage (상태 소유자)
│   │   └── components/
│   │       ├── BookDataGrid.jsx        ← MUI DataGrid 래퍼 (순수 표시)
│   │       ├── BookSearch.jsx          ← 검색창 (onSearch 콜백 Props)
│   │       ├── CategoryFilter.jsx      ← 카테고리 Select (onChange 콜백 Props)
│   │       └── LoanDialog.jsx          ← 대출 신청 모달
│   ├── MyLoan/
│   │   └── index.jsx                  ← MyLoanPage
│   ├── Login/
│   │   └── index.jsx                  ← LoginPage
│   └── Register/
│       └── index.jsx                  ← RegisterPage
├── components/
│   ├── layout/
│   │   ├── Header.jsx                 ← 공통 헤더 (authStore 연결)
│   │   └── Footer.jsx
│   └── common/
│       ├── LoadingSpinner.jsx
│       └── ErrorMessage.jsx
├── store/
│   ├── bookStore.js                   ← books, fetchBooks
│   ├── loanStore.js                   ← loans, createLoan, returnBook
│   └── authStore.js                   ← token, login, logout (persist)
└── api/
    ├── axiosInstance.js               ← JWT Authorization 헤더 인터셉터
    ├── bookApi.js                     ← fetchBooksApi, fetchBookDetailApi
    ├── loanApi.js                     ← createLoanApi, getMyLoansApi, returnBookApi
    └── authApi.js                     ← loginApi, registerApi
```

---

## 작성 체크리스트

- [x] STEP 1: 와이어프레임에서 컴포넌트 경계를 사각형으로 표시
- [x] STEP 2: 컴포넌트 트리(부모-자식 계층) 작성
- [x] STEP 3: 각 상태가 왜 로컬/전역인지 근거 작성
- [x] STEP 4: Props 흐름 다이어그램 (데이터↓, 이벤트↑) 작성
- [x] 모든 Props의 타입, 필수여부 작성
- [x] API 연동 목록 및 호출 시점 정의
- [x] 로딩 / 에러 / 빈 데이터 3가지 상태 처리 코드 포함
- [x] Zustand 스토어 상태/액션 명세 작성
- [x] 파일 구조 정리