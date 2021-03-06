/*
 * copyright 2013-2020
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 14/10/2020 (georgemoralis) - using jinq
 * 13/10/2020 (georgemoralis) - Initial commit
 */
package gr.codebb.protoerp.settings;

import gr.codebb.lib.database.GenericDao;
import gr.codebb.lib.database.PersistenceManager;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.jinq.jpa.JinqJPAStreamProvider;

public class SettingsHelper {

  public static void addStringSetting(String settingName, String settingValue) {
    GenericDao gdaoi1 = new GenericDao(SettingsEntity.class, PersistenceManager.getEmf());
    SettingsEntity t = new SettingsEntity();
    t.setSettingName(settingName);
    t.setSettingValue(settingValue);
    gdaoi1.createEntity(t);
  }

  public static void addIntegerSetting(String settingName, int settingValue) {
    GenericDao gdaoi1 = new GenericDao(SettingsEntity.class, PersistenceManager.getEmf());
    SettingsEntity t = new SettingsEntity();
    t.setSettingName(settingName);
    t.setSettingValue(Integer.toString(settingValue));
    gdaoi1.createEntity(t);
  }

  public static void updateStringSetting(String settingName, String settingValue) {
    GenericDao gdaoi1 = new GenericDao(SettingsEntity.class, PersistenceManager.getEmf());
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    Optional<SettingsEntity> result =
        streams
            .streamAll(em, SettingsEntity.class)
            .where(c -> c.getSettingName().equals(settingName))
            .findFirst();
    em.close();

    if (result.isPresent()) {
      SettingsEntity tmp3 = (SettingsEntity) result.get();
      tmp3.setSettingValue(settingValue);
      gdaoi1.updateEntity(tmp3);
    }
  }

  public static String loadStringSetting(String settingName) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    Optional<SettingsEntity> result =
        streams
            .streamAll(em, SettingsEntity.class)
            .where(c -> c.getSettingName().equals(settingName))
            .findFirst();
    em.close();
    if (result.isPresent()) {
      return result.get().getSettingValue();
    } else {
      return null;
    }
  }

  public static int loadIntegerSetting(String settingName) {
    String stringsetting = loadStringSetting(settingName);
    if (stringsetting != null) {
      int setting = Integer.parseInt(stringsetting);
      return setting;
    } else {
      return -1;
    }
  }

  public static void updateIntegerSetting(String settingName, int settingValue) {
    updateStringSetting(settingName, Integer.toString(settingValue));
  }

  public static long loadLongSetting(String settingName) {
    String stringsetting = loadStringSetting(settingName);
    if (stringsetting != null) {
      if (stringsetting.length() == 0) {
        return -1;
      }
      long setting = Long.parseLong(stringsetting);
      return setting;
    } else {
      return -1;
    }
  }

  public static void updateLongSetting(String settingName, long settingValue) {
    updateStringSetting(settingName, Long.toString(settingValue));
  }

  public static boolean loadBooleanSetting(String settingName) {
    String stringsetting = loadStringSetting(settingName);
    if (stringsetting != null) {
      int setting = Integer.parseInt(stringsetting);
      return setting != 0;
    } else {
      return false;
    }
  }

  public static void updateBooleanSetting(String settingName, boolean settingValue) {
    updateStringSetting(settingName, settingValue ? "1" : "0");
  }
}
