package com.example.coalawebbackend.common.seed;

import com.example.coalawebbackend.domain.anonymous.service.AnonymousProfileService;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.repository.BoardRepository;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.entity.InfoCategory;
import com.example.coalawebbackend.domain.info.repository.InfoArticleRepository;
import com.example.coalawebbackend.domain.instance.entity.InstanceApplication;
import com.example.coalawebbackend.domain.instance.entity.InstanceAttachedFile;
import com.example.coalawebbackend.domain.instance.entity.InstanceSpec;
import com.example.coalawebbackend.domain.instance.entity.ServiceInquiry;
import com.example.coalawebbackend.domain.instance.repository.InstanceApplicationRepository;
import com.example.coalawebbackend.domain.instance.repository.ServiceInquiryRepository;
import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import com.example.coalawebbackend.domain.memberservice.repository.MemberServiceRepository;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.profile.entity.PublicUserActivityLog;
import com.example.coalawebbackend.domain.profile.entity.PublicUserAward;
import com.example.coalawebbackend.domain.profile.entity.PublicUserProfile;
import com.example.coalawebbackend.domain.profile.repository.PublicUserProfileRepository;
import com.example.coalawebbackend.domain.recruit.entity.RecruitComment;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.entity.RecruitRole;
import com.example.coalawebbackend.domain.recruit.repository.RecruitCommentRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitPostRepository;
import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@RequiredArgsConstructor
public class CoalaDataSeedConfig {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AnonymousProfileService anonymousProfileService;
    private final PublicUserProfileRepository publicUserProfileRepository;
    private final MemberServiceRepository memberServiceRepository;
    private final InstanceApplicationRepository instanceApplicationRepository;
    private final ServiceInquiryRepository serviceInquiryRepository;
    private final InfoArticleRepository infoArticleRepository;
    private final RecruitPostRepository recruitPostRepository;
    private final RecruitCommentRepository recruitCommentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;

    @Value("${app.seed.dev-account.enabled:false}")
    private boolean enabled;

    @Bean
    @Order(20)
    public ApplicationRunner coalaDataSeedRunner() {
        return args -> {
            User systemUser = seedSystemUser();

            // 익명 질문게시판과 예시 스레드는 운영 환경에서도 항상 존재해야 하는 실제 기능이므로
            // 개발용 더미 데이터 시드 스위치(enabled)와 무관하게 항상 보장한다.
            seedQnaBoard(systemUser);

            if (!enabled) {
                return;
            }

            seedBoardsAndPosts(systemUser);
            seedProfiles();
            seedMemberServices();
            seedInstanceApplications();
            seedInfoArticles();
            seedRecruits();
        };
    }

    private User seedSystemUser() {
        return userRepository.findByEmail("seed@jbnu.ac.kr")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("seed@jbnu.ac.kr")
                        .password(passwordEncoder.encode("Seed1234!"))
                        .name("코알라 운영진")
                        .nickname("coala-seed")
                        .birthDate(LocalDate.of(2000, 1, 1))
                        .gender(Gender.PREFER_NOT_TO_SAY)
                        .department("컴퓨터인공지능학부")
                        .lab("코알라")
                        .studentId("20990001")
                        .grade(4)
                        .githubId("coala-seed")
                        .academicStatus(AcademicStatus.ENROLLED)
                        .verified(true)
                        .build()));
    }

    private void seedBoardsAndPosts(User user) {
        Board notice = findOrCreateBoard("공지", "공지사항 게시판", "NORMAL", user);
        Board free = findOrCreateBoard("자유", "자유 게시판", "NORMAL", user);
        Board humor = findOrCreateBoard("유머", "유머 게시판", "NORMAL", user);
        findOrCreateBoard("소식", "정보공유 소식", "NORMAL", user);
        findOrCreateBoard("대회", "정보공유 대회", "NORMAL", user);
        findOrCreateBoard("연구실", "정보공유 연구실", "NORMAL", user);
        findOrCreateBoard("자료", "정보공유 자료", "NORMAL", user);
        findOrCreateBoard("문의사항", "인스턴스 문의사항", "NORMAL", user);
        findOrCreateBoard("모집", "스터디/프로젝트 모집", "RECRUIT", user);

        if (postRepository.count() == 0) {
            postRepository.save(Post.create(
                    "동아리 코알라 이용 가이드와 온보딩 공지",
                    "신규 합류자가 빠르게 적응할 수 있도록 운영진이 정리해 둔 체크리스트와 공지를 모았습니다.",
                    notice,
                    user));
            postRepository.save(Post.create(
                    "2026년 생산성 루틴 공유 스레드",
                    "아침 루틴부터 사이드 프로젝트를 병행하는 방법까지, 멤버들의 실제 스케줄을 공유합니다.",
                    free,
                    user));
            postRepository.save(Post.create(
                    "코딩하다가 새벽 3시에 깨달은 순간들",
                    "콘솔 로그 한 줄 때문에 밤을 새운 경험담과 가벼운 밈을 나누는 스레드입니다.",
                    humor,
                    user));
        }
    }

    private void seedQnaBoard(User user) {
        Board qna = findOrCreateBoard("질문게시판", "개발, 진로, 연구실, 대학원 관련 익명 질문 게시판", "ANONYMOUS", user);
        seedQnaThread(qna);
    }

    private static final String QNA_SEED_POST_TITLE = "개발자 준비 중인데 질문드립니다";

    private void seedQnaThread(Board qnaBoard) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> seedQnaThreadInTransaction(qnaBoard));
    }

    private void seedQnaThreadInTransaction(Board qnaBoard) {
        User author = seedUser("qna-author@jbnu.ac.kr", "질문학생", "qna-author",
                "컴퓨터공학과", "20230001", 2, AcademicStatus.ENROLLED, "qna-author-seed");
        User juniorMentor = seedUser("qna-mentor-junior@jbnu.ac.kr", "코알라멤버1", "qna-mentor-junior",
                "컴퓨터인공지능학부", "20220002", 3, AcademicStatus.ENROLLED, "qna-mentor-junior-seed");
        User seniorMentor = seedUser("qna-mentor-senior@jbnu.ac.kr", "코알라선배", "qna-mentor-senior",
                "컴퓨터인공지능학부", "20190003", null, AcademicStatus.GRADUATED, "qna-mentor-senior-seed");

        // 표시명은 배포 때마다 최신 값으로 맞춰준다 (이미 시드된 환경에서도 갱신되도록).
        anonymousProfileService.updateDisplayName(qnaBoard, author, "익명1");
        anonymousProfileService.updateDisplayName(qnaBoard, juniorMentor, "익명2");
        anonymousProfileService.updateDisplayName(qnaBoard, seniorMentor, "익명3");

        boolean alreadySeeded = postRepository
                .findByBoardBoardIdAndStatusOrderByCreatedAtDesc(qnaBoard.getBoardId(), PostStatus.ACTIVE)
                .stream()
                .anyMatch(post -> post.getTitle().equals(QNA_SEED_POST_TITLE));
        if (alreadySeeded) {
            return;
        }

        Post post = postRepository.save(Post.create(
                QNA_SEED_POST_TITLE,
                "안녕하세요 현재 2학년 재학생입니다. 개발자를 목표로 공부하고 있는데 무엇부터 준비하면 좋을지 잘 모르겠습니다. "
                        + "보통 어떤 순서로 공부하셨는지, 지금부터 하면 좋은 것들이 있다면 알려주실 수 있나요?",
                qnaBoard,
                author));
        LocalDateTime postTime = LocalDate.now().minusDays(1).atTime(9, 0);
        postRepository.backdateTimestamps(post.getPostId(), postTime, postTime);

        LocalDateTime comment1Time = postTime.plusHours(4);
        Comment comment1 = commentRepository.save(Comment.create(
                post, juniorMentor,
                "일단 한 언어를 꾸준히 공부하셨다면 간단한 프로젝트를 하시면 될 것 같아요 웹이든 앱이든 간단한 프로젝트를 만드시면 될듯 합니다."));
        commentRepository.backdateTimestamps(comment1.getId(), comment1Time, comment1Time);

        LocalDateTime comment2Time = comment1Time.plusHours(12);
        Comment comment2 = commentRepository.save(Comment.create(
                post, seniorMentor,
                "음 전 책이나 강의를 보면서 객체지향, 웹 통신 방식, 이후에 MVC구조, CRUD을 먼저 공부한 뒤 프로젝트를 시작하는 방식으로 공부하긴했는데 "
                        + "요즘은 프로젝트를 먼저 시작하는 분들도 많은 것 같긴합니다. 걍 예를 들어 로그인 기능을 만들고 싶다, 게시판을 만들고 싶다 같은 기능을 목표로 잡고 "
                        + "Claude나 Codex 같은 AI에게 구현 방법을 물어보며 개발을 진행하고 그러다 막히는 부분이 생길 때마다 관련 개념을 공부하는 방식으로도 하시는 것 같더라고요 "
                        + "사용자 흐름에 맞춰 필요한 요구사항을 먼저 정리하고 그 요구사항을 구현하는 데 필요한 기술과 구조를 하나씩 익혀가는 방식도 ㄱㅊ아 보입니다."));
        commentRepository.backdateTimestamps(comment2.getId(), comment2Time, comment2Time);

        LocalDateTime comment3Time = comment2Time.plusHours(6);
        Comment comment3 = commentRepository.save(Comment.create(
                post, author,
                "답변 감사합니다! 혹시 백엔드를 희망하려는데 Python할까요 아니면 java할까요"));
        commentRepository.backdateTimestamps(comment3.getId(), comment3Time, comment3Time);

        LocalDateTime comment4Time = comment3Time.plusHours(5);
        Comment comment4 = commentRepository.save(Comment.create(
                post, seniorMentor,
                "요즘은 Cloud나 AI Agent처럼 본인이 어떤 분야를 목표로 할지 먼저 정하시면 되고, 언어 자체는 크게 중요하지 않습니다. "
                        + "만들고 싶은 서비스와 생태계에 맞춰 선택하시면 됩니다.\n\n"
                        + "백엔드라면 Java(Spring Boot), Python(FastAPI, Django), Node.js 정도 있습니다. Python은 AI와 데이터 분야에서 활용도가 높아 "
                        + "최근 수요가 많고, 언어 자체를 잘하면 다양한 기업에서 활용할 수 있어서 좋아요. Node.js는 개발 속도가 빠르고 JavaScript 하나로 프론트와 백엔드 모두 "
                        + "개발할 수 있어 스타트업이나 중소 중견기업에서 많이 사용합니다. Java는 Spring Boot 생태계가 탄탄하고, 전자정부프레임워크를 사용하는 공공사업이나 "
                        + "대기업에서 많이 활용됩니다.\n\n"
                        + "어떤 언어를 선택하든 하나를 제대로 익히면 다른 언어를 배우는 것은 생각보다 어렵지 않습니다. 언어를 자주 바꾸기보다는 하나를 선택해서 프로젝트를 "
                        + "깊이 있게 진행해보시는 것을 추천드립니다."));
        commentRepository.backdateTimestamps(comment4.getId(), comment4Time, comment4Time);
    }

    private User seedUser(String email, String name, String nickname, String department,
                          String studentId, Integer grade, AcademicStatus academicStatus, String githubId) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("Seed1234!"))
                        .name(name)
                        .nickname(nickname)
                        .birthDate(LocalDate.of(2001, 1, 1))
                        .gender(Gender.PREFER_NOT_TO_SAY)
                        .department(department)
                        .studentId(studentId)
                        .grade(grade)
                        .githubId(githubId)
                        .academicStatus(academicStatus)
                        .verified(true)
                        .build()));
    }

    private Board findOrCreateBoard(String name, String description, String type, User user) {
        return boardRepository.findAll().stream()
                .filter(board -> board.getName().equals(name))
                .findFirst()
                .orElseGet(() -> boardRepository.save(Board.createFromBoard(name, description, type, user)));
    }

    private void seedProfiles() {
        if (publicUserProfileRepository.count() > 0) {
            return;
        }

        PublicUserProfile minji = profile("김민지", "김", "mint", "프론트엔드", "3학년", "웹서비스 연구실",
                "minji-dev", "React, 디자인 시스템", "오늘 09:12",
                List.of("coala-dashboard", "design-token-lab"), "minji_dev", "gold", 132, 88, 6420, false);
        minji.addLog(log("log-1-1", "commit", "커뮤니티 배너 슬라이더 컴포넌트 정리", "coala-dashboard",
                "탭형 페이지에서 재사용할 수 있도록 배너 데이터를 분리했습니다.", "오늘", 0));
        minji.addLog(log("log-1-2", "pull-request", "프로필 작성 콘텐츠 필터 개선", "coala-dashboard",
                "게시판, 정보공유, 모집 글을 같은 목록에서 확인하도록 수정했습니다.", "어제", 1));
        minji.addAward(award("award-1-1", "전북대학교 SW 창업 해커톤", "전북대학교 SW중심대학사업단", "대상",
                "2026-04-12", "hackathon", "동아리 프로젝트 대시보드 프로토타입으로 팀 개발과 발표를 진행했습니다.",
                "https://github.com/coala-jbnu/coala-dashboard", 0));
        minji.addAward(award("award-1-2", "코알라 서비스 개선 공모", "코알라", "우수상",
                "2026-02-24", "club", "커뮤니티 정보 구조와 서비스 탭 개편안을 제안했습니다.", null, 1));

        PublicUserProfile seyeon = profile("박세연", "박", "sky", "백엔드", "4학년", "클라우드 시스템 연구실",
                "seyeon-api", "Spring Boot, 배포 자동화", "어제 22:40",
                List.of("instance-api", "deploy-playground"), "seyeon_api", "platinum", 166, 104, 7810, false);
        seyeon.addLog(log("log-2-1", "release", "인스턴스 신청 API v0.3 배포", "instance-api",
                "신청 상태 변경과 관리자 메모 필드를 추가했습니다.", "어제", 0));
        seyeon.addAward(award("award-2-1", "JBNU 클라우드 인프라 챌린지", "전북대학교 컴퓨터인공지능학부", "최우수상",
                "2026-03-18", "competition", "인스턴스 신청, 배포 자동화, Redis 기반 인증 흐름을 설계했습니다.",
                "https://github.com/coala-jbnu/instance-api", 0));

        PublicUserProfile mino = profile("최민호", "최", "amber", "AI 연구", "졸업생", "지능형소프트웨어 연구실",
                "mino-lab", "LLM, 데이터 파이프라인", "2일 전",
                List.of("paper-scout", "dataset-cleaner"), "mino_lab", "silver", 94, 57, 4380, false);
        mino.addLog(log("log-3-1", "note", "논문 요약 자동화 실험 기록 공유", "paper-scout",
                "초록 수집, 키워드 추출, 요약 프롬프트를 비교했습니다.", "2일 전", 0));
        mino.addAward(award("award-3-1", "학부 연구 포스터 세션", "전북대학교 컴퓨터인공지능학부", "장려상",
                "2025-12-05", "research", "논문 요약 자동화와 데이터셋 정제 파이프라인을 발표했습니다.", null, 0));

        PublicUserProfile doyun = profile("이도윤", "이", "slate", "풀스택", "2학년", "웹서비스 연구실",
                "doyun-stack", "Next.js, PostgreSQL", "3일 전",
                List.of("team-finder", "study-mate"), "doyun_stack", "bronze", 61, 42, 3150, true);
        doyun.addLog(log("log-4-1", "commit", "모집 지원 플로우 초안 구현", "team-finder",
                "지원서 입력 폼과 모집 상태 표시를 추가했습니다.", "3일 전", 0));
        doyun.addAward(award("award-4-1", "코알라 프로젝트 데모데이", "코알라", "인기상",
                "2026-04-30", "club", "모집 지원 플로우와 마크다운 지원서 화면을 시연했습니다.", null, 0));

        publicUserProfileRepository.saveAll(List.of(minji, seyeon, mino, doyun));
    }

    private PublicUserProfile profile(String name, String initials, String tone, String role, String grade, String lab,
                                      String githubHandle, String focus, String recentCommit, List<String> repos,
                                      String solvedHandle, String solvedTier, int solvedCount, int commits,
                                      int points, boolean isMe) {
        return PublicUserProfile.builder()
                .name(name)
                .initials(initials)
                .tone(tone)
                .role(role)
                .grade(grade)
                .lab(lab)
                .githubHandle(githubHandle)
                .githubUrl("https://github.com/" + githubHandle)
                .focus(focus)
                .recentCommit(recentCommit)
                .sharedRepos(repos)
                .solvedHandle(solvedHandle)
                .solvedTier(solvedTier)
                .solvedCount(solvedCount)
                .githubCommits(commits)
                .totalPoints(points)
                .isMe(isMe)
                .build();
    }

    private PublicUserActivityLog log(String id, String type, String title, String repository,
                                      String description, String timeLabel, int sortOrder) {
        return PublicUserActivityLog.builder()
                .publicId(id)
                .type(type)
                .title(title)
                .repository(repository)
                .description(description)
                .timeLabel(timeLabel)
                .sortOrder(sortOrder)
                .build();
    }

    private PublicUserAward award(String id, String title, String organizer, String rank, String date,
                                  String category, String description, String credentialUrl, int sortOrder) {
        return PublicUserAward.builder()
                .publicId(id)
                .title(title)
                .organizer(organizer)
                .rank(rank)
                .awardedAt(LocalDate.parse(date))
                .category(category)
                .description(description)
                .credentialUrl(credentialUrl)
                .sortOrder(sortOrder)
                .build();
    }

    private void seedMemberServices() {
        if (memberServiceRepository.count() > 0) {
            return;
        }
        memberServiceRepository.saveAll(List.of(
                memberService("paper-scout", "Paper Scout", "ai", "최민호", "관심 키워드로 논문을 모으고 요약하는 리서치 도구입니다.",
                        "paper-scout.coala.dev", "https://github.com/JBNU-COALA/paper-scout",
                        "",
                        List.of("LLM", "논문", "요약"), "운영중", "연구·논문 스터디", "Public", "2026.03 ~ 운영 중",
                        "키워드 기반으로 논문 후보를 모으고, 팀원이 읽을 자료를 빠르게 선별하는 서비스입니다.",
                        List.of("키워드별 논문 후보 저장", "요약 메모와 읽음 상태 관리", "스터디 공유 링크 생성"),
                        List.of("React", "Node.js", "LLM API", "PostgreSQL")),
                memberService("study-mate", "Study Mate", "learning", "이도윤", "스터디 일정, 과제, 출석을 한 번에 관리하는 서비스입니다.",
                        "study-mate.coala.dev", "https://github.com/JBNU-COALA/study-mate",
                        "",
                        List.of("스터디", "일정", "과제"), "운영중", "스터디 운영자", "Public", "2026.02 ~ 운영 중",
                        "스터디 일정과 과제, 출석 체크를 한 화면에서 관리하기 위한 서비스입니다.",
                        List.of("스터디별 일정표", "과제 제출 체크", "출석 기록 관리"),
                        List.of("React", "Django", "SQLite", "Calendar")),
                memberService("deploy-note", "Deploy Note", "productivity", "박세연", "팀 배포 체크리스트와 릴리즈 노트",
                        "deploy-note.coala.dev", "https://github.com/JBNU-COALA/deploy-note",
                        "",
                        List.of("배포", "문서", "팀"), "운영중", "프로젝트 팀", "Public", "2026.01 ~ 운영 중",
                        "배포 전 확인해야 할 항목과 릴리즈 노트를 서비스 단위로 남기는 도구입니다.",
                        List.of("배포 체크리스트", "릴리즈 노트 템플릿", "팀별 운영 기록"),
                        List.of("React", "Express", "Markdown", "GitHub Actions")),
                memberService("algo-room", "Algo Room", "learning", "정하윤", "알고리즘 문제 풀이 기록과 스터디 과제를 모아보는 서비스입니다.",
                        "algo-room.coala.dev", "https://github.com/JBNU-COALA/algo-room",
                        "",
                        List.of("알고리즘", "스터디", "기록"), "운영중", "알고리즘 스터디", "Public", "2025.12 ~ 운영 중",
                        "문제 풀이 기록과 스터디 과제를 모아보고, 회차별 진행 상황을 확인합니다.",
                        List.of("회차별 문제 묶음", "풀이 기록", "스터디 과제 상태"),
                        List.of("React", "Spring Boot", "MySQL", "Baekjoon")),
                memberService("lab-board", "Lab Board", "community", "서지우", "연구실 모집, 세미나, 인턴 정보를 정리하는 게시판형 서비스입니다.",
                        "lab-board.coala.dev", "https://github.com/JBNU-COALA/lab-board",
                        "",
                        List.of("연구실", "세미나", "정보"), "운영중지", "연구실 정보 공유", "Public", "2025.11 ~ 운영 중지",
                        "연구실 모집, 세미나, 학부생 인턴 정보를 게시판 형태로 정리한 서비스입니다.",
                        List.of("연구실 공고 목록", "세미나 일정 정리", "관심 연구실 저장"),
                        List.of("Vue", "Firebase", "Markdown")),
                memberService("resume-kit", "Resume Kit", "productivity", "강민재", "포트폴리오와 이력서 초안을 팀원끼리 리뷰할 수 있게 만든 도구입니다.",
                        "resume-kit.coala.dev", "https://github.com/JBNU-COALA/resume-kit",
                        "",
                        List.of("포트폴리오", "리뷰", "문서"), "운영종료", "취업·포트폴리오 준비", "Public", "2025.09 ~ 운영 종료",
                        "포트폴리오와 이력서 초안을 팀원끼리 리뷰하고 개선 기록을 남기는 도구입니다.",
                        List.of("리뷰 요청", "체크리스트", "수정 이력"),
                        List.of("React", "NestJS", "PostgreSQL")),
                memberService("prompt-vault", "Prompt Vault", "ai", "오유진", "프로젝트에서 사용한 프롬프트와 실험 결과를 정리하는 아카이브입니다.",
                        "prompt-vault.coala.dev", "https://github.com/JBNU-COALA/prompt-vault",
                        "",
                        List.of("AI", "프롬프트", "실험"), "운영중", "AI 프로젝트 팀", "Public", "2026.04 ~ 운영 중",
                        "프로젝트에서 사용한 프롬프트와 실험 결과를 재사용 가능한 형태로 모읍니다.",
                        List.of("프롬프트 버전 관리", "실험 결과 기록", "태그 기반 검색"),
                        List.of("React", "FastAPI", "Vector DB", "LLM API")),
                memberService("team-clock", "Team Clock", "productivity", "윤태현", "팀별 개발 시간, 회의 기록, 마감 일정을 가볍게 관리합니다.",
                        "team-clock.coala.dev", "https://github.com/JBNU-COALA/team-clock",
                        "",
                        List.of("팀", "일정", "생산성"), "운영중지", "팀 프로젝트", "Public", "2025.10 ~ 운영 중지",
                        "개발 시간, 회의 기록, 마감 일정을 가볍게 정리하는 팀 운영 도구입니다.",
                        List.of("회의 로그", "마감 일정", "팀별 활동 시간"),
                        List.of("Svelte", "Node.js", "SQLite"))
        ));
    }

    private MemberService memberService(String id, String title, String category, String owner, String summary,
                                        String url, String githubUrl, String imageUrl, List<String> tags,
                                        String status, String audience, String visibility, String period,
                                        String description, List<String> features, List<String> stack) {
        return MemberService.builder()
                .id(id)
                .title(title)
                .category(category)
                .owner(owner)
                .summary(summary)
                .url(url)
                .githubUrl(githubUrl)
                .imageUrl(imageUrl)
                .tags(tags)
                .status(status)
                .audience(audience)
                .visibility(visibility)
                .period(period)
                .description(description)
                .features(features)
                .stack(stack)
                .build();
    }

    private void seedInstanceApplications() {
        if (instanceApplicationRepository.count() == 0) {
            instanceApplicationRepository.save(InstanceApplication.builder()
                    .id("jc-001")
                    .applicantName("김코알라")
                    .studentId("20211234")
                    .keyEmail("coala.member@example.com")
                    .instanceType("medium")
                    .purpose("캡스톤 프로젝트 API 서버 배포")
                    .duration("6개월")
                    .requestedAt(LocalDate.of(2026, 3, 10))
                    .approvedAt(LocalDate.of(2026, 3, 12))
                    .status("approved")
                    .adminNote("승인되었습니다. 접속 키와 안내 메일을 확인하세요.")
                    .attachedFiles(List.of(InstanceAttachedFile.builder()
                            .name("jc-001-connection-info.pdf")
                            .size("128 KB")
                            .uploadedAt("2026-03-12")
                            .build()))
                    .specs(InstanceSpec.forType("medium"))
                    .build());
            instanceApplicationRepository.save(InstanceApplication.builder()
                    .id("jc-002")
                    .applicantName("박알고")
                    .studentId("20220987")
                    .keyEmail("algorithm@example.com")
                    .instanceType("micro")
                    .purpose("동아리 스터디 실습 서버")
                    .duration("1년")
                    .requestedAt(LocalDate.of(2026, 3, 28))
                    .status("pending")
                    .attachedFiles(List.of())
                    .specs(InstanceSpec.forType("micro"))
                    .build());
        }
        if (serviceInquiryRepository.count() == 0) {
            serviceInquiryRepository.save(ServiceInquiry.builder()
                    .id("inq-001")
                    .title("인스턴스 기간 연장 문의")
                    .summary("프로젝트 일정 변경으로 사용 기간 연장이 가능한지 문의합니다.")
                    .content("프로젝트 일정 변경으로 사용 기간 연장이 가능한지 문의합니다.")
                    .author("김코알라")
                    .createdDate(LocalDate.of(2026, 4, 28))
                    .status("답변 완료")
                    .statusClass("status--approved")
                    .build());
            serviceInquiryRepository.save(ServiceInquiry.builder()
                    .id("inq-002")
                    .title("포트 개방 문의")
                    .summary("API 서버 테스트를 위해 외부 접속 포트 설정이 필요한지 확인하고 싶습니다.")
                    .content("API 서버 테스트를 위해 외부 접속 포트 설정이 필요한지 확인하고 싶습니다.")
                    .author("박알고")
                    .createdDate(LocalDate.of(2026, 4, 25))
                    .status("검토 중")
                    .statusClass("status--pending")
                    .build());
        }
    }

    private void seedInfoArticles() {
        if (infoArticleRepository.count() > 0) {
            return;
        }
        infoArticleRepository.saveAll(List.of(
                info(InfoCategory.NEWS, "소식", "5월 코알라 운영진 공지", "공지", "운영진", "2026-05-02",
                        "이번 달 운영 일정입니다.\n\n- 정기 모임: 매주 목요일\n- 프로젝트 점검: 5월 둘째 주\n- 서비스 배포 신청: 상시",
                        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80"),
                info(InfoCategory.CONTEST, "대회", "2026 AI 해커톤 참가팀 모집", "외부 대회", "대회팀", "2026-05-01",
                        "AI 해커톤 참가팀을 모집합니다.\n\n- 주제: 서비스 기획과 AI 기능 구현\n- 팀 구성: 2명 이상 5명 이하\n- 신청 마감: 2026년 5월 10일",
                        "https://images.unsplash.com/photo-1517048676732-d65bc937f952?auto=format&fit=crop&w=1200&q=80"),
                info(InfoCategory.LAB, "연구실", "지능형소프트웨어 연구실 학부생 인턴 안내", "연구실", "연구연계팀", "2026-04-29",
                        "지능형소프트웨어 연구실 학부생 인턴 안내입니다.\n\n- 대상: 2학년 이상\n- 분야: 웹 서비스, 데이터 처리, 모델 활용",
                        "https://images.unsplash.com/photo-1581093458791-9d15482442f6?auto=format&fit=crop&w=1200&q=80"),
                info(InfoCategory.RESOURCE, "자료", "React 상태관리 패턴 정리", "3.4 MB", "김예린", "2026-04-27",
                        "React 상태관리 패턴을 정리한 자료입니다.\n\n## 포함 내용\n\n- Context 사용 기준\n- Zustand 상태 분리\n- TanStack Query 캐시 전략",
                        "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1200&q=80"),
                info(InfoCategory.CONTEST, "대회", "교내 캡스톤 경진대회 일정", "교내 대회", "박세연", "2026-04-25",
                        "교내 캡스톤 경진대회 일정입니다.\n\n- 예선 접수: 5월 20일\n- 발표 자료 제출: 5월 27일\n- 본선 발표: 6월 3일",
                        "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80"),
                info(InfoCategory.RESOURCE, "자료", "백엔드 보안 체크리스트", "외부 링크", "최민호", "2026-04-23",
                        "백엔드 보안 점검용 체크리스트입니다.\n\n- 인증 토큰 만료와 재발급\n- 권한별 API 접근 제어\n- 파일 업로드 검증",
                        "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=1200&q=80")
        ));
    }

    private InfoArticle info(InfoCategory category, String tag, String title, String meta, String sourceName,
                             String sourceDate, String content, String imageUrl) {
        return InfoArticle.builder()
                .category(category)
                .tag(tag)
                .title(title)
                .meta(meta)
                .sourceName(sourceName)
                .sourceDate(LocalDate.parse(sourceDate))
                .content(content)
                .imageUrl(imageUrl)
                .viewCount(0)
                .bookmarkCount(0)
                .build();
    }

    private void seedRecruits() {
        if (recruitPostRepository.count() > 0) {
            return;
        }
        RecruitPost react = recruit("react-study", "React 19 + TypeScript 심화 스터디 2기 모집",
                "실무형 컴포넌트 설계, 상태관리 패턴, 테스트 전략까지 한 번에 다루는 집중 스터디입니다.",
                "study", "open", 4, 6, "김민지", "김", "sky", "프론트엔드 파트장", 96.2,
                List.of("#React", "#TypeScript", "#Frontend"),
                List.of("React", "TypeScript", "Vite", "Vitest"),
                List.of(role("스터디원", 4, 6, 0)),
                "온라인 (Discord) + 월 1회 오프라인", "8주",
                List.of("이번 스터디는 단순 문법 학습이 아니라 실제 서비스 코드 구조를 설계하는 방식에 집중합니다.",
                        "주차별 과제는 커스텀 훅, 비동기 상태 흐름, 폼 아키텍처, 테스트 자동화를 포함합니다.",
                        "참여자는 매주 코드 리뷰를 받고, 마지막 주에는 팀 단위 미니 프로젝트를 발표합니다."),
                List.of("주 2회 온라인 스터디 진행 (화/목 저녁)", "GitHub PR 기반 코드 리뷰", "주간 회고 문서 제출 및 피드백"),
                1240, 48);
        recruitPostRepository.save(react);
        recruitCommentRepository.save(RecruitComment.builder()
                .recruitPost(react)
                .author("이준호")
                .authorInitials("이")
                .authorTone("mint")
                .content("비전공자도 지원 가능한지 궁금합니다. 선수 지식 기준이 있을까요?")
                .build());
        recruitCommentRepository.save(RecruitComment.builder()
                .recruitPost(react)
                .author("박소연")
                .authorInitials("박")
                .authorTone("rose")
                .content("기초 JS와 React 훅 경험이 있으면 충분히 따라올 수 있어요.")
                .build());

        recruitPostRepository.save(recruit("ai-project", "AI 기반 일정 어시스턴트 사이드 프로젝트",
                "기획, FE, BE가 함께 8주 동안 MVP를 개발하는 협업형 프로젝트 팀을 모집합니다.",
                "project", "open", 3, 5, "정우석", "정", "slate", "프로젝트 리드", 92.4,
                List.of("#AI", "#MVP", "#협업"), List.of("Next.js", "FastAPI", "PostgreSQL"),
                List.of(role("프론트엔드", 1, 2, 0), role("백엔드", 1, 2, 1), role("기획", 1, 1, 2)),
                "오프라인 주 1회 + 상시 온라인", "8주",
                List.of("일정 추천과 회의 기록 요약을 제공하는 웹 서비스 MVP를 목표로 합니다.",
                        "팀 단위 스프린트로 운영하며, 기능별 책임 영역을 명확히 나눠 진행합니다."),
                List.of("격주 스프린트 계획/회고", "Issue 템플릿 기반 태스크 관리", "기능 데모와 기술 공유 세션"),
                980, 35));
        recruitPostRepository.save(recruit("portfolio-mentoring", "취업 포트폴리오 리뷰 멘토링 2기",
                "졸업생 멘토와 함께 포트폴리오 메시지, 구조, 전달력까지 단계별로 점검하는 프로그램입니다.",
                "tutoring", "closing-soon", 7, 8, "최예린", "최", "sand", "졸업생 멘토", 97.0,
                List.of("#취업", "#포트폴리오", "#멘토링"), List.of("Notion", "Figma"),
                List.of(role("멘티", 7, 8, 0)), "온라인", "4주",
                List.of("문서 완성도보다 문제 해결 방식과 의사결정 근거를 어떻게 보여줄지에 집중합니다.",
                        "개인별 1:1 피드백과 그룹 리뷰를 병행해 빠르게 개선 포인트를 찾습니다."),
                List.of("주 1회 그룹 세션", "개인 문서 리뷰 코멘트 제공"), 760, 41));
        recruitPostRepository.save(recruit("backend-study", "백엔드 아키텍처 스터디 (Spring & Node 비교)",
                "실제 API 설계 사례를 바탕으로 백엔드 구조를 비교 학습하고, 설계 문서 작성까지 진행합니다.",
                "study", "open", 5, 8, "윤지수", "윤", "amber", "백엔드 운영진", 89.6,
                List.of("#Backend", "#Architecture", "#API"), List.of("Spring Boot", "Node.js", "Redis"),
                List.of(role("스터디원", 5, 8, 0)), "온라인", "6주",
                List.of("요구사항을 데이터 모델과 API로 변환하는 과정을 반복 학습합니다.",
                        "팀별로 아키텍처 결정을 문서화하고 리뷰합니다."),
                List.of("주 1회 발표", "주간 과제 PR 제출"), 612, 20));
        recruitPostRepository.save(recruit("design-system-project", "디자인 시스템 구축 프로젝트",
                "컴포넌트 기준, 토큰 체계, 문서 자동화까지 포함한 실전형 디자인 시스템 프로젝트입니다.",
                "project", "closed", 4, 4, "한서율", "한", "rose", "디자인 리드", 94.1,
                List.of("#DesignSystem", "#Storybook", "#협업"), List.of("React", "Storybook", "Figma"),
                List.of(role("프론트엔드", 2, 2, 0), role("디자이너", 2, 2, 1)), "오프라인", "10주",
                List.of("현재 모집이 마감되어 대기자 등록만 가능합니다."),
                List.of("월간 리뷰 세션"), 1320, 67));
    }

    private RecruitPost recruit(String id, String title, String shortDesc, String category, String status,
                                int currentMembers, int maxMembers, String host, String initials, String tone,
                                String hostRole, double trustScore, List<String> tags, List<String> techStack,
                                List<RecruitRole> roles, String meetingType, String expectedDuration,
                                List<String> detail, List<String> process, long views, long bookmarks) {
        RecruitPost recruit = RecruitPost.builder()
                .id(id)
                .title(title)
                .shortDesc(shortDesc)
                .category(category)
                .status(status)
                .currentMembers(currentMembers)
                .maxMembers(maxMembers)
                .host(host)
                .hostInitials(initials)
                .hostTone(tone)
                .hostRole(hostRole)
                .trustScore(trustScore)
                .tags(tags)
                .techStack(techStack)
                .meetingType(meetingType)
                .expectedDuration(expectedDuration)
                .detailContent(detail)
                .processList(process)
                .views(views)
                .bookmarks(bookmarks)
                .build();
        roles.forEach(recruit::addRole);
        return recruit;
    }

    private RecruitRole role(String label, int current, int max, int sortOrder) {
        return RecruitRole.builder()
                .label(label)
                .current(current)
                .max(max)
                .sortOrder(sortOrder)
                .build();
    }
}
