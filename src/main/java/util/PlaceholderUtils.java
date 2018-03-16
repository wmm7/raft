package util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderUtils {
  private static final Logger logger = LoggerFactory.getLogger(PlaceholderUtils.class);

  private static Pattern                 pattern       = Pattern.compile("\\$\\{(\\w+)\\}");
  private static Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

  private PlaceholderUtils() {
  }

  private static String getPlaceholderProperties(String key, String filePath) {

    // 判断文件是否存在, 不存在返回 null
    File f = new File(filePath);
    if (!f.exists()) {
      return null;
    }

    Properties p = new Properties();

    // 防止重复加载, 进行缓存
    if (propertiesMap.containsKey(filePath)) {
      p = propertiesMap.get(filePath);
      if (p == null) {
        logger.error("getPlaceholderProperties error for properties in map is null, filePath:{}, properties:{}", filePath, p);
      }
    } else {
      try {
        p.load(new FileInputStream(new File(filePath)));
      } catch (IOException e) {
        logger.error("getPlaceholderProperties error for IOException, filePath:{}", filePath);
      }
      propertiesMap.put(filePath, p);
    }

    String value = p.getProperty(key);
    if (value == null) {
      throw new NullPointerException("value of properties cannot be null for filePath:" + filePath + ", key:" + key);
    }
    return value;
  }

  /**
   * 用实际值替换占位符
   *
   * @param p
   * @param actualValuePropertiesFilePath
   * @return
   */
  public static synchronized boolean replacePlaceholderWithActualValue(Properties p, String actualValuePropertiesFilePath) {

    Iterator<Map.Entry<Object, Object>> it = p.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Object, Object> entry = it.next();
      String key = (String) entry.getKey();
      String oldValue = (String) entry.getValue();

      if (oldValue == null) {
        logger.error("replacePlaceholderWithActualValue error for oldValue is null, key={}, oldValue={}, "
                     + "actualValuePropertiesFilePath={}", key, oldValue, actualValuePropertiesFilePath);
        return false;
      }

      Matcher matcher = pattern.matcher(oldValue);
      String newValue = oldValue;
      while (matcher.find()) {
        String group = matcher.group(1);

        String replace = getPlaceholderProperties(group, actualValuePropertiesFilePath);
        if (replace != null) {
          logger.info("replacePlaceholderWithActualValue before replace placeholder, key={}, oldValue={}", key, oldValue);
          newValue = matcher.replaceFirst(replace);
          matcher.reset(newValue);
          p.setProperty(key, newValue);
          logger.info("replacePlaceholderWithActualValue set property success, key={}, oldValue={}, newValue={}, "
                      + "actualValuePropertiesFilePath={}", key, oldValue, p.get(key), actualValuePropertiesFilePath);
        } else {
          logger.error("replacePlaceholderWithActualValue error, key={}, oldValue={}, "
                       + "actualValuePropertiesFilePath={}", key, oldValue, actualValuePropertiesFilePath);
        }
      }
    }

    return true;
  }

  public static synchronized boolean replacePlaceholderWithActualValue(LinkedHashMap m,
                                                                       String actualValuePropertiesFilePath) {

    Iterator<Map.Entry<Object, Object>> it = m.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Object, Object> entry = it.next();
      String key = (String) entry.getKey();
      Object objValue = entry.getValue();

      if (objValue == null){
        return false;
      }

      if (objValue.getClass().equals(String.class)) {
        String oldValue = objValue.toString();
        if (oldValue == null) {
          return false;
        }

        Matcher matcher = pattern.matcher(oldValue);
        String newValue = oldValue;
        while (matcher.find()) {
          String group = matcher.group(1);

          String replace = getPlaceholderProperties(group, actualValuePropertiesFilePath);
          if (replace != null) {
            logger.info("replacePlaceholderWithActualValue before replace placeholder, key={}, oldValue={}", key, oldValue);
            newValue = matcher.replaceFirst(replace);
            matcher.reset(newValue);
            m.put(key, newValue);
            logger.info("replacePlaceholderWithActualValue set property success, key={}, oldValue={}, newValue={}, "
                        + "actualValuePropertiesFilePath={}", key, oldValue, m.get(key), actualValuePropertiesFilePath);
          } else {
            logger.error("replacePlaceholderWithActualValue error, key={}, oldValue={}, "
                         + "actualValuePropertiesFilePath={}", key, oldValue, actualValuePropertiesFilePath);
          }
        }
      }
    }

    return true;
  }

  // 获取加密文件的绝对路径
  public static String getEncryptPropertiesRealPath(String filePath) {
    if (StringUtils.isBlank(filePath)) {
      logger.error("getEncryptPropertiesRealPath error, filePath:{}", filePath);
    }
    return filePath.substring(0, filePath.lastIndexOf(".")) + "_encrypt.properties";
  }
}
