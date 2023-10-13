package hello.jdbc.exception.translator;

import static hello.jdbc.connection.ConnectionConst.PASSWORD;
import static hello.jdbc.connection.ConnectionConst.URL;
import static hello.jdbc.connection.ConnectionConst.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

@Slf4j
class SpringExceptionTranslatorTest {

  DriverManagerDataSource dataSource;

  @BeforeEach
  void init() {
    dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
  }

  @Test
  void sqlExceptionErrorCode() {
    String sql = "select bad grammar";

    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = dataSource.getConnection();
      pstmt = conn.prepareStatement(sql);
      pstmt.executeQuery();

    } catch (SQLException e) {
      log.info("error", e);
      assertThat(e.getErrorCode()).isEqualTo(42122);

    } finally {
      JdbcUtils.closeStatement(pstmt);
      JdbcUtils.closeConnection(conn);
    }
  }

  @Test
  void exceptionTranslator() {
    String sql = "select bad grammar";

    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = dataSource.getConnection();
      pstmt = conn.prepareStatement(sql);
      pstmt.executeQuery();

    } catch (SQLException e) {
      assertThat(e.getErrorCode()).isEqualTo(42122);
      //org.springframework.jdbc.support.sql-error-codes.xml

      SQLErrorCodeSQLExceptionTranslator exTranslator
          = new SQLErrorCodeSQLExceptionTranslator(dataSource);

      //BadSqlGrammarException
      DataAccessException resultEx = exTranslator.translate("select", sql, e);
      log.info("resultEx", resultEx);
      assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);

    } finally {
      JdbcUtils.closeStatement(pstmt);
      JdbcUtils.closeConnection(conn);
    }

  }


}
