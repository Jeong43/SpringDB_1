package hello.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {

  public static final String MEMBER_A = "memberA";
  public static final String MEMBER_B = "memberB";
  public static final String MEMBER_EX = "ex";

  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private MemberServiceV4 memberService;


  @TestConfiguration
  @RequiredArgsConstructor
  static class TestConfig {

    @Autowired
    private final DataSource dataSource;

    @Bean
    MemberRepository MemberRepository() {
      //return new MemberRepositoryV4_1(dataSource);
      return new MemberRepositoryV4_2(dataSource);
    }

    @Bean
    MemberServiceV4 MemberService() {
      return new MemberServiceV4(MemberRepository());
    }
  }

  @AfterEach
  void after() {
    memberRepository.delete(MEMBER_A);
    memberRepository.delete(MEMBER_B);
    memberRepository.delete(MEMBER_EX);
  }

  @Test
  void AopCheck() {
    log.info("MemberService class={}", memberService.getClass());
    log.info("MemberRepository class={}", memberRepository.getClass());
    Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
    Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
  }

  @Test
  @DisplayName("정상 이체")
  void accountTransfer() throws SQLException {
    //given
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberB = new Member(MEMBER_B, 10000);
    memberRepository.save(memberA);
    memberRepository.save(memberB);

    //when
    log.info("START TX");
    memberService.accountTransfer(MEMBER_A, MEMBER_B, 2000);
    log.info("END TX");

    //then
    assertThat(memberRepository.findById(MEMBER_A).getMoney()).isEqualTo(8000);
    assertThat(memberRepository.findById(MEMBER_B).getMoney()).isEqualTo(12000);
  }

  @Test
  @DisplayName("이체 중 예외")
  void accountTransferEx() throws SQLException {
    //given
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberEx = new Member(MEMBER_EX, 10000);
    memberRepository.save(memberA);
    memberRepository.save(memberEx);

    //when
    assertThatThrownBy(() -> memberService.accountTransfer(MEMBER_A, MEMBER_EX, 2000))
        .isInstanceOf(IllegalStateException.class);

    //then
    assertThat(memberRepository.findById(MEMBER_A).getMoney()).isEqualTo(10000);
    assertThat(memberRepository.findById(MEMBER_EX).getMoney()).isEqualTo(10000);
  }
}