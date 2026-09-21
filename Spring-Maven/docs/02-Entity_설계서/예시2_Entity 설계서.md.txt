# Entity 설계서

## 문서 정보

| 항목 | 내용 |
|------|------|
| **프로젝트명** | 온라인 도서관 시스템 (Library Management System) |
| **작성자** | 홍길동, 김영희, 이철수 |
| **작성일** | 2025-05-26 |
| **버전** | v1.0 |

---

## 1. Entity 설계 개요

### 1.1 설계 목적

JPA Entity 클래스 설계를 통해 객체-관계 매핑(ORM)을 정의하고,
도서관 비즈니스 도메인(도서, 회원, 대출, 카테고리)을 코드로 표현하여
유지보수가 용이한 시스템을 구축한다.

### 1.2 설계 원칙

- **단일 책임 원칙**: 하나의 Entity는 하나의 비즈니스 개념만 표현
- **캡슐화**: 비즈니스 로직을 Entity 내부 메서드로 구현
- **불변성**: setter 최소화, 비즈니스 메서드로만 상태 변경
- **연관관계 최소화**: 필요한 관계만 매핑하여 복잡도 감소

### 1.3 기술 스택

- **ORM**: Spring Data JPA 3.4.6
- **데이터베이스**: MariaDB 10.11
- **검증**: Bean Validation 2.0
- **감사 기능**: Spring Data JPA Auditing (`@CreatedDate`, `@LastModifiedDate`)

---

## 2. Entity 목록

### 2.1 Entity 분류 매트릭스

| Entity명 | 유형 | 비즈니스 중요도 | 기술적 복잡도 | 연관관계 수 | 우선순위 |
|----------|------|----------------|---------------|-------------|----------|
| **Member** | 핵심 | 높음 | 중간 | 1개 (Loan) | 1순위 |
| **Book** | 핵심 | 높음 | 중간 | 2개 (Loan, Category) | 1순위 |
| **Loan** | 핵심 | 높음 | 높음 | 2개 (Member, Book) | 1순위 |
| **Category** | 지원 | 중간 | 낮음 | 1개 (Book) | 2순위 |

### 2.2 공통 Base Entity

```java
// src/main/java/com/library/entity/common/BaseEntity.java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

---

## 3. 공통 설계 규칙

### 3.1 네이밍 규칙

| 구분 | 규칙 | 예시 | 비고 |
|------|------|------|------|
| **Entity 클래스명** | PascalCase | `Member`, `Book`, `Loan` | 단수형 사용 |
| **테이블명** | snake_case | `members`, `books`, `loans` | 복수형 사용 |
| **컬럼명** | snake_case | `member_id`, `created_at` | 언더스코어 구분 |
| **연관관계 필드** | camelCase | `member`, `book` | 객체 참조명 |
| **boolean 필드** | is + 형용사 | `isActive`, `isOverdue` | 명확한 의미 |

### 3.2 ID 생성 전략

| Entity | 전략 | 이유 |
|--------|------|------|
| **Member** | IDENTITY | MariaDB Auto Increment |
| **Book** | IDENTITY | 단순한 순차 증가 |
| **Loan** | IDENTITY | 대출 순서 보장 |
| **Category** | IDENTITY | 순차 증가 |

---

## 4. 상세 Entity 설계

### 4.1 Member Entity

#### 4.1.1 필드 상세 명세

| 필드명 | 데이터 타입 | 컬럼명 | 제약조건 | 설명 | 비즈니스 규칙 |
|--------|-------------|--------|----------|------|---------------|
| `id` | `Long` | `member_id` | PK, AUTO_INCREMENT | 회원 고유 식별자 | 자동 생성 |
| `memberNumber` | `String` | `member_number` | UNIQUE, NOT NULL, LENGTH(10) | 회원번호 | M + 9자리 숫자 |
| `name` | `String` | `name` | NOT NULL, LENGTH(50) | 실명 | 2~50자 |
| `email` | `String` | `email` | UNIQUE, NOT NULL, LENGTH(100) | 이메일 | 로그인 ID 겸용 |
| `password` | `String` | `password` | NOT NULL, LENGTH(255) | 암호화된 비밀번호 | BCrypt 암호화 |
| `phoneNumber` | `String` | `phone_number` | LENGTH(15) | 전화번호 | 선택 입력 |
| `status` | `MemberStatus` | `status` | NOT NULL, ENUM | 회원 상태 | ACTIVE, SUSPENDED, WITHDRAWN |
| `maxLoanCount` | `Integer` | `max_loan_count` | NOT NULL, DEFAULT(5) | 최대 대출 권수 | 1~10 범위 |

#### 4.1.2 구현 코드 예시

```java
// src/main/java/com/library/entity/Member.java
@Entity
@Table(name = "members", indexes = {
    @Index(name = "idx_member_number", columnList = "member_number"),
    @Index(name = "idx_email",         columnList = "email"),
    @Index(name = "idx_status",        columnList = "status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true, length = 10)
    private String memberNumber;   // M000000001

    @Column(nullable = false, length = 50)
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @Column(nullable = false, length = 255)
    private String password;       // BCrypt 암호화

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "max_loan_count", nullable = false)
    private Integer maxLoanCount = 5;

    // 1:N — 대출이력
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<Loan> loans = new ArrayList<>();

    // ── 비즈니스 메서드 ────────────────────────────────────────
    /** 대출 가능 여부: 활성 상태이고 현재 대출 수가 최대치 미만 */
    public boolean canBorrow() {
        long activeLoanCount = loans.stream()
            .filter(l -> l.getStatus() == LoanStatus.BORROWED
                      || l.getStatus() == LoanStatus.APPROVED)
            .count();
        return status == MemberStatus.ACTIVE
            && activeLoanCount < maxLoanCount;
    }

    /** 회원 상태 변경 (관리자 전용) */
    public void changeStatus(MemberStatus newStatus) {
        this.status = newStatus;
    }

    /** 회원 정보 수정 */
    public void updateProfile(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    /** 비밀번호 변경 (암호화된 값 전달) */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
```

#### 4.1.3 Enum — MemberStatus

```java
public enum MemberStatus {
    ACTIVE,      // 정상 활성
    SUSPENDED,   // 관리자에 의해 정지
    WITHDRAWN    // 탈퇴 처리
}
```

---

### 4.2 Book Entity

#### 4.2.1 필드 상세 명세

| 필드명 | 데이터 타입 | 컬럼명 | 제약조건 | 설명 | 비즈니스 규칙 |
|--------|-------------|--------|----------|------|---------------|
| `id` | `Long` | `book_id` | PK, AUTO_INCREMENT | 도서 고유 식별자 | 자동 생성 |
| `isbn` | `String` | `isbn` | UNIQUE, NOT NULL, LENGTH(13) | ISBN | 13자리 숫자 |
| `title` | `String` | `title` | NOT NULL, LENGTH(200) | 도서명 | 필수 |
| `author` | `String` | `author` | NOT NULL, LENGTH(100) | 저자 | 필수 |
| `publisher` | `String` | `publisher` | LENGTH(100) | 출판사 | 선택 |
| `publicationDate` | `LocalDate` | `publication_date` | NULL 허용 | 출간일 | 선택 |
| `totalCopies` | `Integer` | `total_copies` | NOT NULL, MIN(1) | 총 보유 권수 | 1 이상 |
| `availableCopies` | `Integer` | `available_copies` | NOT NULL, MIN(0) | 대출 가능 권수 | 0 이상 |
| `description` | `String` | `description` | TEXT | 도서 설명 | 선택 |
| `imageUrl` | `String` | `image_url` | LENGTH(500) | 표지 이미지 URL | 선택 |
| `isActive` | `Boolean` | `is_active` | NOT NULL, DEFAULT(true) | 활성 여부 | 소프트 삭제용 |
| `category` | `Category` | `category_id` | FK → categories | 카테고리 | 필수 |

#### 4.2.2 구현 코드 예시

```java
// src/main/java/com/library/entity/Book.java
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_isbn",      columnList = "isbn"),
    @Index(name = "idx_title",     columnList = "title"),
    @Index(name = "idx_author",    columnList = "author"),
    @Index(name = "idx_category",  columnList = "category_id"),
    @Index(name = "idx_available", columnList = "available_copies")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Book extends BaseEntity {

    @Column(nullable = false, unique = true, length = 13)
    @Pattern(regexp = "^\\d{13}$", message = "ISBN은 13자리 숫자여야 합니다")
    private String isbn;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "도서명은 필수입니다")
    private String title;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "저자는 필수입니다")
    private String author;

    @Column(length = 100)
    private String publisher;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "total_copies", nullable = false)
    @Min(value = 1, message = "총 보유 권수는 1 이상이어야 합니다")
    private Integer totalCopies;

    @Column(name = "available_copies", nullable = false)
    @Min(value = 0, message = "대출 가능 권수는 0 이상이어야 합니다")
    private Integer availableCopies;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 1:N — 대출이력
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    // ── 비즈니스 메서드 ────────────────────────────────────────
    /** 대출 가능 여부 */
    public boolean isAvailable() {
        return isActive && availableCopies > 0;
    }

    /** 대출 시 가용 재고 감소 */
    public void decreaseAvailable() {
        if (availableCopies <= 0) {
            throw new IllegalStateException("대출 가능한 재고가 없습니다");
        }
        this.availableCopies--;
    }

    /** 반납 시 가용 재고 증가 */
    public void increaseAvailable() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("재고 수량이 총 보유 권수를 초과할 수 없습니다");
        }
        this.availableCopies++;
    }

    /** 도서 정보 수정 */
    public void update(String title, String author, String publisher,
                       Integer totalCopies, Category category) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.totalCopies = totalCopies;
        this.category = category;
    }

    /** 소프트 삭제 */
    public void deactivate() {
        this.isActive = false;
    }
}
```

---

### 4.3 Loan Entity

#### 4.3.1 필드 상세 명세

| 필드명 | 데이터 타입 | 컬럼명 | 제약조건 | 설명 | 비즈니스 규칙 |
|--------|-------------|--------|----------|------|---------------|
| `id` | `Long` | `loan_id` | PK, AUTO_INCREMENT | 대출 고유 식별자 | 자동 생성 |
| `member` | `Member` | `member_id` | FK → members, NOT NULL | 대출 회원 | 필수 |
| `book` | `Book` | `book_id` | FK → books, NOT NULL | 대출 도서 | 필수 |
| `loanDate` | `LocalDate` | `loan_date` | NOT NULL | 대출 신청일 | 신청 시 자동 설정 |
| `dueDate` | `LocalDate` | `due_date` | NOT NULL | 반납 예정일 | 대출 승인일 + 14일 |
| `returnDate` | `LocalDate` | `return_date` | NULL 허용 | 실제 반납일 | 반납 처리 시 설정 |
| `status` | `LoanStatus` | `status` | NOT NULL, ENUM | 대출 상태 | LoanStatus Enum |

#### 4.3.2 구현 코드 예시

```java
// src/main/java/com/library/entity/Loan.java
@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_loan_member", columnList = "member_id"),
    @Index(name = "idx_loan_book",   columnList = "book_id"),
    @Index(name = "idx_loan_status", columnList = "status"),
    @Index(name = "idx_due_date",    columnList = "due_date")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Loan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.REQUESTED;

    /** 팩토리 메서드 — 대출 신청 생성 */
    public static Loan createLoan(Member member, Book book) {
        Loan loan = new Loan();
        loan.member = member;
        loan.book = book;
        loan.loanDate = LocalDate.now();
        loan.dueDate = LocalDate.now().plusDays(14);
        loan.status = LoanStatus.REQUESTED;
        book.decreaseAvailable();   // 재고 즉시 감소
        return loan;
    }

    // ── 비즈니스 메서드 ────────────────────────────────────────
    /** 연체 여부 */
    public boolean isOverdue() {
        return status == LoanStatus.BORROWED
            && LocalDate.now().isAfter(dueDate);
    }

    /** 연체 일수 */
    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    /** 연체료 계산 (하루 100원) */
    public long calculateOverdueFee() {
        return getOverdueDays() * 100L;
    }

    /** 관리자 대출 승인: REQUESTED → BORROWED */
    public void approve() {
        if (status != LoanStatus.REQUESTED) {
            throw new IllegalStateException("승인 대기 상태만 승인할 수 있습니다");
        }
        this.status = LoanStatus.BORROWED;
        this.dueDate = LocalDate.now().plusDays(14);
    }

    /** 반납 처리: BORROWED/OVERDUE → RETURNED */
    public void returnBook() {
        if (status != LoanStatus.BORROWED && status != LoanStatus.OVERDUE) {
            throw new IllegalStateException("대출 중인 상태만 반납할 수 있습니다");
        }
        this.status = LoanStatus.RETURNED;
        this.returnDate = LocalDate.now();
        book.increaseAvailable();   // 재고 복구
    }

    /** 대출 취소: REQUESTED → CANCELLED */
    public void cancel() {
        if (status != LoanStatus.REQUESTED) {
            throw new IllegalStateException("승인 대기 상태만 취소할 수 있습니다");
        }
        this.status = LoanStatus.CANCELLED;
        book.increaseAvailable();   // 재고 복구
    }
}
```

#### 4.3.3 Enum — LoanStatus

```java
public enum LoanStatus {
    REQUESTED,   // 대출 신청 (관리자 승인 대기)
    APPROVED,    // 관리자 승인 완료
    BORROWED,    // 대출 중
    RETURNED,    // 반납 완료
    OVERDUE,     // 연체 (반납 예정일 초과)
    CANCELLED    // 취소
}
```

---

### 4.4 Category Entity

#### 4.4.1 필드 상세 명세

| 필드명 | 데이터 타입 | 컬럼명 | 제약조건 | 설명 |
|--------|-------------|--------|----------|------|
| `id` | `Long` | `category_id` | PK, AUTO_INCREMENT | 카테고리 고유 ID |
| `name` | `String` | `name` | UNIQUE, NOT NULL, LENGTH(50) | 카테고리명 |
| `description` | `String` | `description` | LENGTH(200) | 설명 |

#### 4.4.2 구현 코드 예시

```java
// src/main/java/com/library/entity/Category.java
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "카테고리명은 필수입니다")
    private String name;

    @Column(length = 200)
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    public static Category create(String name, String description) {
        Category category = new Category();
        category.name = name;
        category.description = description;
        return category;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
```

---

## 5. 데이터베이스 마이그레이션 (Flyway)

```sql
-- V1__init_library_tables.sql

CREATE TABLE categories (
    category_id    BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(50)  NOT NULL UNIQUE,
    description    VARCHAR(200),
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,
    PRIMARY KEY (category_id)
);

CREATE TABLE members (
    member_id      BIGINT       NOT NULL AUTO_INCREMENT,
    member_number  VARCHAR(10)  NOT NULL UNIQUE,
    name           VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(15),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    max_loan_count INT          NOT NULL DEFAULT 5,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,
    PRIMARY KEY (member_id),
    INDEX idx_email (email),
    INDEX idx_status (status)
);

CREATE TABLE books (
    book_id           BIGINT       NOT NULL AUTO_INCREMENT,
    isbn              VARCHAR(13)  NOT NULL UNIQUE,
    title             VARCHAR(200) NOT NULL,
    author            VARCHAR(100) NOT NULL,
    publisher         VARCHAR(100),
    publication_date  DATE,
    total_copies      INT          NOT NULL,
    available_copies  INT          NOT NULL,
    description       TEXT,
    image_url         VARCHAR(500),
    is_active         TINYINT(1)   NOT NULL DEFAULT 1,
    category_id       BIGINT,
    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    PRIMARY KEY (book_id),
    INDEX idx_isbn (isbn),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_available (available_copies),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE loans (
    loan_id      BIGINT      NOT NULL AUTO_INCREMENT,
    member_id    BIGINT      NOT NULL,
    book_id      BIGINT      NOT NULL,
    loan_date    DATE        NOT NULL,
    due_date     DATE        NOT NULL,
    return_date  DATE,
    status       VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    created_at   DATETIME    NOT NULL,
    updated_at   DATETIME    NOT NULL,
    PRIMARY KEY (loan_id),
    INDEX idx_member (member_id),
    INDEX idx_book (book_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 초기 카테고리 데이터
INSERT INTO categories (name, description, created_at, updated_at)
VALUES
    ('소설',   '국내외 소설 작품',        NOW(), NOW()),
    ('IT',     '프로그래밍 및 기술 도서',  NOW(), NOW()),
    ('역사',   '역사 관련 도서',          NOW(), NOW()),
    ('과학',   '과학 및 자연 도서',        NOW(), NOW()),
    ('기타',   '기타 분류',              NOW(), NOW());
```

---

## 6. 작성 체크리스트

- [x] 모든 핵심 Entity (Member, Book, Loan, Category) 설계 완료
- [x] 각 Entity의 필드 명세 (타입, 제약조건, 비즈니스 규칙) 작성
- [x] 비즈니스 메서드 구현 코드 포함
- [x] Enum 정의 (LoanStatus, MemberStatus)
- [x] 연관관계 매핑 (@ManyToOne, @OneToMany) 정의
- [x] Flyway SQL 마이그레이션 스크립트 작성