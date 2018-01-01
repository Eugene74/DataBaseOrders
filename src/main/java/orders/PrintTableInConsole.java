package orders;

/*
* класс для печати таблицы чтобы хоть как то ровненько :)
* */
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

class PrintTableInConsole {
    void printTable(ResultSet rs, String nameTable) throws SQLException {
      ResultSetMetaData md = rs.getMetaData();
      String space = "          ";
      String vs="";

        System.out.println(nameTable+"---------------------------------------------------------------------------------------------------------");

      for (int i = 1; i <= md.getColumnCount(); i++) {
// чтобы столбец с адресом пошире был
          if (i==3 | i==5) {
              vs = space;
          }
// а этот столбец  с ценой наоборот  поближе к площади  и район тоже
          if (i != 3 && i!=5) {
              vs = "    ";
          }
          System.out.print(md.getColumnName(i) + "   " + vs);
          vs = "";
      }
      System.out.println();
      while (rs.next()) {
          for (int i = 1; i <= md.getColumnCount(); i++) {
              System.out.print("      "+rs.getString(i) + "\t\t");
          }
          System.out.println();
      }
        System.out.println(nameTable+"---------------------------------------------------------------------------------------------------------");
        System.out.println();
  }
}
