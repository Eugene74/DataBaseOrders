package orders;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class DatabaseJdbcConnection {
    private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/orders?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "7RootPEugene47";
    private static Connection conn;
    static {
        try {
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void initDB() throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS clients(" +
                    "client_id INT(10) NOT NULL AUTO_INCREMENT," +
                    "name_client VARCHAR(45) NOT NULL, " +
                    "delivery VARCHAR (45)," +
                    "phone_client VARCHAR(45)," +
                    "PRIMARY KEY (client_id))  ENGINE = InnoDB  DEFAULT CHARACTER SET = utf8");
            statement.execute("CREATE TABLE IF NOT EXISTS goods(" +
                    "product_id INT NOT NULL AUTO_INCREMENT," +
                    "name_product  VARCHAR(45) NOT NULL ," +
                    "price DOUBLE NOT NULL," +
                    "UNIQUE (name_product)," +
                    "PRIMARY KEY (product_id))  ENGINE = InnoDB  DEFAULT CHARACTER SET = utf8");
            statement.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id INT NOT NULL AUTO_INCREMENT," +
                    "date_delivery DATE NOT NULL," +
                    "client_id INT(10) NOT NULL," +
                    "total_sum DOUBLE," +
                    "FOREIGN KEY (client_id) REFERENCES orders.clients(client_id)," +
                    "PRIMARY KEY (order_id))  ENGINE = InnoDB  DEFAULT CHARACTER SET = utf8");
            statement.execute("CREATE TABLE IF NOT EXISTS basket(" +
                    "id_basket INT(10) NOT NULL AUTO_INCREMENT," +
                    "product_id INT(10) NOT NULL," +
                    "order_id INT(11) NOT NULL," +
                    "client_id INT(10) NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "PRIMARY KEY (id_basket),"+
                    "FOREIGN KEY (client_id) REFERENCES orders.clients(client_id)," +
                    "FOREIGN KEY (product_id) REFERENCES orders.goods(product_id)," +
                    "FOREIGN KEY (order_id) REFERENCES orders.orders(order_id))" +
                    " ENGINE = InnoDB  DEFAULT CHARACTER SET = utf8");
        }
    }
    void fillClientsTable(String nameClient, String delivery, String phone) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO  orders.clients (name_client, delivery, phone_client ) VALUES (?,?,?)")) {
            ps.setString(1, nameClient);
            ps.setString(2, delivery);
            ps.setString(3, phone);
            ps.executeUpdate();
        }
    }
    boolean fillGoodsTable(String nameProd, double priceD)throws SQLException{
        boolean b = false;
        try {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO  orders.goods (name_product, price) VALUES (?,?)")) {
                ps.setString(1, nameProd);
                ps.setDouble(2, priceD);
                ps.executeUpdate();
            }
        } catch (MySQLIntegrityConstraintViolationException exception) {
            System.out.println("This Product already exist");
            b= true;
        }
        return b;
    }
    void selectAll(String table) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("select * FROM "+table)) {
            try (ResultSet rs = ps.executeQuery()) {
                new PrintTableInConsole().printTable(rs, table);
            }
        }
    }
    void createOrder(Date tmpDate, int client_id, double price) throws SQLException{
        try(PreparedStatement ps= conn.prepareStatement("INSERT  INTO  orders(  date_delivery, client_id, total_sum) VALUES (?,?,? )")){
            ps.setDate(1, tmpDate );
            ps.setInt(2,client_id);
            ps.setDouble(3,  price);  // 0  at first
            ps.executeUpdate();
        }
    }
    int getOrderId(int client_id, String date_delivery, int total_sum) throws SQLException{
        int order_id = 0;
        try (PreparedStatement ps = conn.prepareStatement("select orders.order_id FROM orders" +
                " WHERE  orders.client_id="+client_id+" AND     DATE (orders.date_delivery)='" +  date_delivery +"'" +" AND orders.total_sum="+total_sum )  ) { // total_sum = 0 - для случая №6, а вообще в перспективе может понадобиться по дате и по сумме доставать ID
            ResultSet rs=ps.executeQuery();
            while (rs.next()) {
                order_id=rs.getInt(1);
            }
        }
        return order_id;
    }
    void selectProductsInsertOrder(Date tmpDate, int client_id, String date_delivery, Scanner scanner) throws SQLException {
        boolean b ;
        conn.setAutoCommit(false);
        try {
            try {
//5 создаю   заказ с Нулём  в total_sum, чтобы зарезервировать order_id
                      createOrder(tmpDate, client_id, 0);

//6 достаю order_id из таблицы orders
                int order_id = getOrderId(client_id, date_delivery, 0);
//7 заполняю basket
                try (PreparedStatement prepStatement = conn.prepareStatement("INSERT INTO basket(product_id, order_id, client_id, price ) VALUES (?,?,?,?)")) {
                    do {
                        b = true;

                        List tmpList=new ArrayList();
                        tmpList = new ChoiceProduct().choiceProductFromGoods(conn,scanner, tmpList);
                        int prodID = (Integer) tmpList.get(0);
                        Double price = (Double) tmpList.get(1);


//7-continue получив price заканчиваю заполнять basket
                        prepStatement.setInt(1, prodID);
                        prepStatement.setInt(2, order_id);
                        prepStatement.setInt(3, client_id);
                        prepStatement.setDouble(4, price);
                        prepStatement.executeUpdate();

                        System.out.println("select more? if -yes - 1, No-2, cancel an order at all -3 or anything ");
                        String more = scanner.nextLine();
                        switch (more) {
                            case "1":
                                break;
                            case "2":
//9 получаю сумму (total_sum) заказа, выбирая цены из basket таблицы
                                double total_sum = 0;
                                try (PreparedStatement statement = conn.prepareStatement("SELECT price FROM basket WHERE order_id=" + order_id + " AND client_id=" + client_id)) {
                                    ResultSet rs = statement.executeQuery();
                                    while (rs.next()) {
                                        total_sum = total_sum + rs.getDouble(1);
                                    }
                                }
// 10 устанавливаю сумму заказа(total_sum) - заказ дооформляю полностью
                                try (PreparedStatement statement = conn.prepareStatement("UPDATE orders set total_sum=" + total_sum +
                                        " WHERE orders.order_id=" + order_id + " AND client_id=" + client_id)) {
                                    statement.executeUpdate();
                                }
                                b = false;
                                conn.commit();// если заполнил и согласился то в базу
                                break;
                            case "3":
                                b = false;
                                System.out.println("Goodbye, your order has not been issued ");
                                conn.rollback();
                                break;
                            default:
                                b = false;
                                System.out.println("Goodbye, your order has not been issued ");
                                conn.rollback();
                                break;
                        }
                    } while (b);
                }
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }
        } finally {
            conn.setAutoCommit(true);
        }
    }
    void deleteOrder(int order_id)throws SQLException{
        try (PreparedStatement ps = conn.prepareStatement("DELETE  FROM basket WHERE  basket.order_id="+order_id )  ) {
            ps.executeUpdate();        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE  FROM orders WHERE  orders.order_id="+order_id )  ) {
            ps.executeUpdate();
        }
    }
    void detailedOrderInformation(int order_id) throws SQLException{
        try (PreparedStatement ps = conn.prepareStatement("select orders.order_id, clients.name_client, clients.delivery," +
                " clients.phone_client, orders.total_sum, orders.date_delivery FROM orders, clients  WHERE  orders.client_id =clients.client_id  AND orders.order_id="+order_id )  ) {
            try (ResultSet rs = ps.executeQuery()) {
                new PrintTableInConsole().printTable(rs, "Detailed Table");
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("select basket.product_id, goods.name_product," +
                " goods.price FROM basket, goods  WHERE   basket.product_id = goods.product_id     AND basket.order_id="+order_id )  ) {
            try (ResultSet rs = ps.executeQuery()) {
                new PrintTableInConsole().printTable(rs, "Basket order ID - "+ order_id);
            }
        }
    }
    void joinSelect()throws  SQLException{
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM  clients INNER JOIN orders ON orders.client_id=clients.client_id " )  ) {
            try (ResultSet rs = ps.executeQuery()) {
                new PrintTableInConsole().printTable(rs, "Join Table Clients And Orders");
            }
        }
    }
    void closeConnection() throws SQLException {
        if (conn != null) conn.close();
    }
    List getListColumnValuesFromTable(String columnName, String table) throws SQLException {
        List  tList = new ArrayList ();
        try (PreparedStatement statement=conn.prepareStatement("SELECT "+ columnName +" FROM " + table)){
            try (ResultSet  resultSet= statement.executeQuery()){
                while (resultSet.next()) {
                    if(columnName.equals("client_id")||columnName.equals("order_id")||columnName.equals("product_id")||columnName.equals("id_basked")) {
                        tList.add(resultSet.getInt(1));
                    }else{
                        if(columnName.equals("price")||columnName.equals("total_sum")) {
                            tList.add(resultSet.getDouble(1));
                        }else {
                            if(columnName.equals("date_delivery") ) {
                                tList.add(resultSet.getDate(1));
                            }else {
                                tList.add(resultSet.getString(1));
                            }
                        }
                    }
                }
            }
        }
         return tList;
     }
}

