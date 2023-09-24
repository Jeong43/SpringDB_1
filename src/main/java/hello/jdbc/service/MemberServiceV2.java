package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

  private final DataSource dataSource;
  private final MemberRepositoryV2 memberRepository;

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    Connection conn = dataSource.getConnection();
    try {
      conn.setAutoCommit(false); //트랜잭션 시작

      //비즈니스 로직
      bizLogic(conn, fromId, toId, money);

      conn.commit(); //성공 시 커밋
    } catch (Exception e) {
      conn.rollback(); //실패 시 롤백
      throw new IllegalStateException(e);
    } finally {
      release(conn);
    }
  }

  private void bizLogic(Connection conn, String fromId, String toId, int money)
      throws SQLException {
    Member fromMember = memberRepository.findById(conn, fromId);
    Member toMember = memberRepository.findById(conn, toId);

    memberRepository.update(conn, fromId, fromMember.getMoney() - money);
    validation(toMember);
    memberRepository.update(conn, toId, toMember.getMoney() + money);
  }

  private void release(Connection conn) {
    if (conn != null) {
      try {
        conn.setAutoCommit(true); //커넥션 풀 고려
        conn.close();
      } catch (Exception e) {
        log.info("error", e);
      }
    }
  }

  private void validation(Member toMember) {
    if (toMember.getMemberId().equals("ex")) {
      throw new IllegalStateException("이체 중 예외 발생");
    }
  }
}
