package hello.jdbc.service;

import static hello.jdbc.connection.ConnectionConst.PASSWORD;
import static hello.jdbc.connection.ConnectionConst.URL;
import static hello.jdbc.connection.ConnectionConst.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.SQLException;
import javax.sql.DataSource;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {

  public static final String MEMBER_A = "memberA";
  public static final String MEMBER_B = "memberB";
  public static final String MEMBER_EX = "ex";

  @Autowired
  private MemberRepositoryV3 memberRepository;
  @Autowired
  private MemberServiceV3_3 memberService;


  @TestConfiguration
  static class TestConfig {

    @Bean
    DataSource dataSource() {
      return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Bean
    PlatformTransactionManager transactionManager() {
      return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    MemberRepositoryV3 MemberRepository() {
      return new MemberRepositoryV3(dataSource());
    }

    @Bean
    MemberServiceV3_3 MemberService() {
      return new MemberServiceV3_3(MemberRepository());
    }
  }

  @AfterEach
  void after() throws SQLException {
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