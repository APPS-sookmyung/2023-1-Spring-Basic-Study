# 6주차

### 스프링 DB 접근 기술

- JPA
    - 기존의 반복 코드는 물론이고, 기본적인 SQL도 직접 만들어서 실행
    - SQL과 데이터 중심의 설계에서 객체 중심의 설계로 패러다임을 전환
    - 개발 생산성을 크게 높일 수 있음
    
    build.gradle 파일에 JPA 관련 라이브러리 추가 
    
    ```html
    //	implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    ```
    
    spring-boot-starter-data-jpa 는 내부에 jdbc 관련 라이브러리를 포함한다. 따라서 jdbc는 제거 
    
    스프링 부트에 JPA 설정 추가
    
    ```html
    // resources/application.properties
    spring.jpa.show-sql=true
    spring.jpa.hibernate.ddl-auto=none
    ```
    
    - show-sql : JPA가 생성하는 SQL을 출력
    - ddl-auto : JPA는 테이블을 자동으로 생성하는 기능을 제공하는데 none 를 사용하면 해당 기능을 끔
        - create 를 사용하면 엔티티 정보를 바탕으로 테이블도 직접 생성
    - JPA 엔티티 매핑
        
        ```html
        package hello.hellospring.domain;
        
        import javax.persistence.Entity;
        import javax.persistence.GeneratedValue;
        import javax.persistence.GenerationType;
        import javax.persistence.Id;
        
        @Entity
        public class Member {
        
            @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
            private String name;
        
            public Long getId() {
                return id;
            }
        
            public void setId(Long id) {
                this.id = id;
            }
        
            public String getName() {
                return name;
            }
        
            public void setName(String name) {
                this.name = name;
            }
        }
        ```
        
    - JPA 회원 리포지토리
        
        ```html
        package hello.hellospring.repository;
        import hello.hellospring.domain.Member;
        import javax.persistence.EntityManager;
        import java.util.List;
        import java.util.Optional;
        
        public class JpaMemberRepository implements MemberRepository {
            
            private final EntityManager em; // JPA 사용하기 위해서
            
            public JpaMemberRepository(EntityManager em) {
                this.em = em;
            }
            
            public Member save(Member member) {
                em.persist(member);
                return member;
            }
            
            public Optional<Member> findById(Long id) {
                Member member = em.find(Member.class, id);
                return Optional.ofNullable(member);
            }
            
            public List<Member> findAll() {
                return em.createQuery("select m from Member m", Member.class)
                        .getResultList();
            }
            
            public Optional<Member> findByName(String name) {
                List<Member> result = em.createQuery("select m from Member m where m.name = :name", Member.class)
                        .setParameter("name", name)
                        .getResultList();
                return result.stream().findAny();
            }
        }
        ```
        
        "select m from Member m”: JPQL, 객체를 대상으로 쿼리를 날림
        
    - 서비스 계층에 트랜잭션 추가
        
        ```html
        package hello.hellospring;
        
        import hello.hellospring.repository.JdbcTemplateMemberRepository;
        import hello.hellospring.repository.JpaMemberRepository;
        import hello.hellospring.repository.MemberRepository;
        import hello.hellospring.service.MemberService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        
        import javax.persistence.EntityManager;
        import javax.sql.DataSource;
        @Configuration
        public class SpringConfig {
        
            private EntityManager em;
        
            @Autowired
            public SpringConfig(EntityManager em) {
                this.em = em;
            }
        
            @Bean
            public MemberService memberService() {
                return new MemberService(memberRepository());
            }
            @Bean
            public MemberRepository memberRepository() {
        // return new MemoryMemberRepository();
        //        return new JdbcMemberRepository(dataSource);
        //        return new JdbcTemplateMemberRepository(dataSource);
                return new JpaMemberRepository();
            }
        }
        ```
        
    - org.springframework.transaction.annotation.Transactional 를 사용
    - 스프링은 해당 클래스의 메서드를 실행할 때 트랜잭션을 시작하고, 메서드가 정상 종료되면 트랜잭션을 커밋, 만약 런타임 예외가 발생하면 롤백
    - JPA를 통한 모든 데이터 변경은 트랜잭션 안에서 실행
- 스프링 데이터 JPA
    - 스프링 데이터 JPA 회원 리포지토리
        
        ```html
        package hello.hellospring.repository;
        
        import hello.hellospring.domain.Member;
        import org.springframework.data.jpa.repository.JpaRepository;
        
        import java.util.Optional;
        
        public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {
        
            @Override
            Optional<Member> findByName(String name);
        }
        ```
        
        스프링 데이터 JPA가 SpringDataJpaMemberRepository 를 스프링 빈으로 자동 등록
        
    - 스프링 데이터 JPA 회원 리포지토리를 사용하도록 스프링 설정 변경
        
        ```html
        package hello.hellospring;
        
        import hello.hellospring.repository.JpaMemberRepository;
        import hello.hellospring.repository.MemberRepository;
        import hello.hellospring.service.MemberService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        
        @Configuration
        public class SpringConfig {
        
            private final MemberRepository memberRepository;
        
            @Autowired
            public SpringConfig(MemberRepository memberRepository) {
                this.memberRepository = memberRepository;
            }
        
            @Bean
            public MemberService memberService() {
                return new MemberService(memberRepository());
            }
        ```
        
    
    스프링 데이터 JPA 제공 클래스
    
    ![Untitled](6%E1%84%8C%E1%85%AE%E1%84%8E%E1%85%A1%20610ec6ec5f39478b8da055a2ee0d2245/Untitled.png)
    
    스프링 데이터 JPA 제공 기능
    
    - 인터페이스를 통한 기본적인 CRUD
    - findByName(), findByEmail() 처럼 메서드 이름 만으로 조회 기능 제공
    - 페이징 기능 자동 제공
    - 참고
        
        실무에서는 JPA와 스프링 데이터 JPA를 기본으로 사용하고, 복잡한 동적 쿼리는 Querydsl이라는
        라이브러리를 사용하면 된다. Querydsl을 사용하면 쿼리도 자바 코드로 안전하게 작성할 수 있고, 동적
        쿼리도 편리하게 작성할 수 있다. 이 조합으로 해결하기 어려운 쿼리는 JPA가 제공하는 네이티브 쿼리를
        사용하거나, 앞서 학습한 스프링 JdbcTemplate를 사용하면 된다.
        

### AOP

- AOP가 필요한 상황
    - 모든 메소드의 호출 시간을 측정하고 싶다면?
    - 공통 관심 사항(cross-cutting concern) vs 핵심 관심 사항(core concern)
    - 회원 가입 시간, 회원 조회 시간을 측정하고 싶다면?
    - MemberService 회원 조회 시간 측정 추가
        
        ```html
        package hello.hellospring.service;
        
        import hello.hellospring.domain.Member;
        import hello.hellospring.repository.MemberRepository;
        import org.springframework.transaction.annotation.Transactional;
        
        import java.util.List;
        import java.util.Optional;
        
        @Transactional
        public class MemberService {
        
            private final MemberRepository memberRepository;
        
            public MemberService(MemberRepository memberRepository) {
                this.memberRepository = memberRepository;
            }
        
            //    회원가입
            public Long join(Member member) {
        
                long start = System.currentTimeMillis();
        
                try {
                    validateDuplicatMember(member);
                    memberRepository.save(member);
                    return member.getId();
                } finally {
                    long finish = System.currentTimeMillis();
                    long timeMs = finish - start;
                    System.out.println("join = " + timeMs + "ms");
                }
            }
        
            private void validateDuplicatMember(Member member) {
                memberRepository.findByName(member.getName())
                        .ifPresent(m -> {
                            throw new IllegalStateException("이미 존재하는 회원입니다.");
                        });
            }
        
            // 전체 회원 조회
            public List<Member> findMembers() {
                long start = System.currentTimeMillis();
                try {
                    return memberRepository.findAll();
                } finally {
                    long finish = System.currentTimeMillis();
                    long timeMs = finish - start;
                    System.out.println("findMembers " + timeMs + "ms");
                }
        
            }
        
            public Optional<Member> findOne(Long memberId) {
                return memberRepository.findById(memberId);
            }
        }
        ```
        
        문제
        
        - 회원가입, 회원 조회에 시간을 측정하는 기능은 핵심 관심 사항이 아니다.
        - 시간을 측정하는 로직은 공통 관심 사항이다.
        - 시간을 측정하는 로직과 핵심 비즈니스의 로직이 섞여서 유지보수가 어렵다.
        - 시간을 측정하는 로직을 별도의 공통 로직으로 만들기 매우 어렵다.
        - 시간을 측정하는 로직을 변경할 때 모든 로직을 찾아가면서 변경해야 한다.
- AOP 적용
    - AOP: Aspect Oriented Programming
    - 공통 관심 사항(cross-cutting concern) vs 핵심 관심 사항(core concern) 분리
        
        → 원하는 곳에 공통 관심 사항 적용
        
    - 시간 측정 AOP 등록
        
        ```html
        package hello.hellospring.aop;
        
        import org.aspectj.lang.ProceedingJoinPoint;
        import org.aspectj.lang.annotation.Around;
        import org.aspectj.lang.annotation.Aspect;
        import org.springframework.stereotype.Component;
        
        @Aspect
        @Component
        public class TimeTracAop {
        
            @Around("execution(* hello.hellospring..*(..))") // 패키지 하위에 다 적용
            public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
                long start = System.currentTimeMillis();
                System.out.println("START: " + joinPoint.toString());
                try {
                    return joinPoint.proceed();
                } finally {
                    long finish =  System.currentTimeMillis();
                    long timeMs = finish - start;
                    System.out.println("END: " + joinPoint.toString() + " " + timeMs + "ms");
        
                }
            }
        }
        ```
        
        해결
        
        - 회원가입, 회원 조회등 핵심 관심사항과 시간을 측정하는 공통 관심 사항을 분리한다.
        - 시간을 측정하는 로직을 별도의 공통 로직으로 만들었다.
        - 핵심 관심 사항을 깔끔하게 유지할 수 있다.
        - 변경이 필요하면 이 로직만 변경하면 된다.
        - 원하는 적용 대상을 선택할 수 있다.