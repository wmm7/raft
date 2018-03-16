package dao;

import com.google.inject.Inject;

import javax.sql.DataSource;

public class RaftDao {

  private DataSource dataSource;

  @Inject
  public RaftDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }


}
