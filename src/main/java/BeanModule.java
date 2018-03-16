import com.google.inject.Binder;
import com.google.inject.Module;


import org.apache.commons.dbcp2.BasicDataSourceFactory;

import javax.sql.DataSource;

import dao.RaftDao;
import util.ConfigUtil;

public class BeanModule implements Module {

  public void configure(Binder binder) {
    try {
      DataSource dataSource = BasicDataSourceFactory.createDataSource(
          ConfigUtil.loadProperties("data-source.properties"));
      binder.bind(RaftDao.class).toInstance(new RaftDao(dataSource));
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
