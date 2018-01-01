package orders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

  class ChoiceProduct {
      List choiceProductFromGoods(Connection conn, Scanner scanner, List tmpList) throws SQLException {
        boolean b;
       Integer prodID;
       Double price =null;
        do {
            b=false;
            tmpList.clear();
            System.out.println("enter product_id: ");
            new DatabaseJdbcConnection().selectAll("goods");
            String product_id = scanner.nextLine();
            try {
                  prodID = Integer.parseInt(product_id);
                  tmpList.add(prodID);
//7-8  иду в табл goods и достаю цену что бы установить значение price-basket
                try (PreparedStatement statement = conn.prepareStatement("select goods.price FROM goods WHERE  goods.product_id=" + prodID)) {
                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        price = rs.getDouble(1);
                        tmpList.add(price);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("!!!Enter please numeric format !!!");
            }
            if(price==null || tmpList.get(0)==null){
                b=true;
                System.out.println("Attention!!! It is not correct product code");
            }
        }while (b);

          return tmpList;
      }
}
