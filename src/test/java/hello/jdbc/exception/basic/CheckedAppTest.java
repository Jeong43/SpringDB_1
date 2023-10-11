package hello.jdbc.exception.basic;

import static org.assertj.core.api.Assertions.*;

import java.net.ConnectException;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class CheckedAppTest {

  @Test
  void checked_controller() {
    Controller controller = new Controller();
    assertThatThrownBy(controller::request)
        .isInstanceOf(SQLException.class);
  }

  static class Controller {

    Service service = new Service();

    public void request() throws SQLException, ConnectException {
      service.logic();
    }
  }

  /**
   * Checked 예외는 예외를 잡아서 처리하거나 던지거나 둘 중 하나를 필수로 선택해야 한다.
   */
  static class Service {

    NetworkClient networkClient = new NetworkClient();
    Repository repository = new Repository();

    public void logic() throws SQLException, ConnectException {
      repository.call();
      networkClient.call();
    }
  }

  static class NetworkClient {

    public void call() throws ConnectException {
      throw new ConnectException("ex");
    }

  }

  static class Repository {

    public void call() throws SQLException {
      throw new SQLException("ex");
    }

  }


}
