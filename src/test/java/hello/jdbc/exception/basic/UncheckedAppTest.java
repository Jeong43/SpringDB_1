package hello.jdbc.exception.basic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class UncheckedAppTest {

  @Test
  void unchecked() {
    Controller controller = new Controller();
    assertThatThrownBy(controller::request)
        .isInstanceOf(Exception.class);
  }

  static class Controller {

    Service service = new Service();

    public void request() {
      service.logic();
    }
  }

  /**
   * Checked 예외는 예외를 잡아서 처리하거나 던지거나 둘 중 하나를 필수로 선택해야 한다.
   */
  static class Service {

    NetworkClient networkClient = new NetworkClient();
    Repository repository = new Repository();

    public void logic() {
      repository.call();
      networkClient.call();
    }
  }

  static class NetworkClient {

    public void call() {
      throw new RuntimeConnectException("연결 실패");
    }

  }

  static class Repository {

    public void call() {
      try {
        runSQL();
      } catch (SQLException e) {
        throw new RuntimeSQLException(e);
      }
    }

    public void runSQL() throws SQLException {
      throw new SQLException("ex");
    }

  }

  static class RuntimeConnectException extends RuntimeException {

    public RuntimeConnectException(String message) {
      super(message);
    }
  }

  static class RuntimeSQLException extends RuntimeException {

    public RuntimeSQLException(Throwable cause) {
      super(cause);
    }
  }

}
