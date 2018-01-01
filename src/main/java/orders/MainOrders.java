package orders;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class MainOrders {
private static DatabaseJdbcConnection databaseJdbcConnection;
    public static void main(String[] args) {
        try {
            databaseJdbcConnection =new DatabaseJdbcConnection();
             // create table if they not exists
            databaseJdbcConnection.initDB();
            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    System.out.println("1: Add Client");
                    System.out.println("2: Add Product");
                    System.out.println("3: Make an order");
                    System.out.println("4: Order information (detailed)- enter ID: ");
                    System.out.println("5: Delete  an order by ID: ");
                    System.out.println("6: Selection of all orders and related clients: ");
                    System.out.print("-> ");
                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addClient(sc);
                            break;
                        case "2":
                            addProduct(sc);
                            break;
                        case "3":
                           makeOrder(sc);
                            break;
                        case "4":
                              searchOrder(sc);
                            break;
                        case "5":
                            deleteOrder(sc);
                            break;
                        case "6":
                            joinSelect( );
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                databaseJdbcConnection.closeConnection();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private static void addClient(Scanner sc) throws SQLException {
        String nameClient;
        String delivery = null;
        String phone =null;
        boolean b=false;
        do {
            System.out.print("Enter Name: ");
            nameClient = sc.nextLine();
            if(!nameClient.equals("")){

                System.out.print("Enter Delivery address: ");
                  delivery = sc.nextLine();

                if(!delivery.equals("")){
                    System.out.print("Enter phone number: ");
                      phone = sc.nextLine();
                    if(phone.equals("")){
                        System.out.println("Wrong phone...");
                        b=true;
                    }
                }else {
                    System.out.println("Wrong Delivery address...");
                    b=true;
                }
             }else {
                System.out.println("Wrong name...");
                b=true;
            }
            if(b==false)  databaseJdbcConnection.fillClientsTable(nameClient, delivery, phone);
        }while (b);
    }
    private static void addProduct(Scanner sc) throws SQLException {
        boolean b=false;
        Double priceD=null;
        do {    System.out.print("Enter Product name: ");
                String nameProd = sc.nextLine();
                if(!nameProd.equals("")){
                    try {
                        System.out.print("Enter price(use point (.) not comma (,) ): ");
                        String price = sc.nextLine();
                        priceD = Double.parseDouble(price);
                    } catch (NumberFormatException e) {
                        System.out.println("Wrong price...");
                        b=true;
                    }
                }else {
                    System.out.println("Wrong name...");
                    b=true;
                }

               if(!(priceD==null))
                    b = databaseJdbcConnection.fillGoodsTable(nameProd, priceD);
        }while (b);
    }
    private static synchronized void makeOrder(Scanner scanner) throws SQLException {
        System.out.print("Enter  client ID: ");
        System.out.println("Select client from list: ");
        databaseJdbcConnection.selectAll("clients");
        int client_id=0;
        boolean b;
        do {
            b=false;
            try {
                String  id = scanner.nextLine();
                client_id = Integer.parseInt(id);
                List list=databaseJdbcConnection.getListColumnValuesFromTable("client_id", "clients");
              if(b=list.contains(client_id)){
                  b=false;
              }else {
                  System.out.println("Attention!!! It is not correct client_id code, please Select client from list, again: ");
                  b=true;
              }
            } catch (NumberFormatException e) {
                System.out.println("!!!Enter please numeric format !!!");
                b=true;
            }
        } while (b);
        System.out.print("Enter date delivery: ");
        System.out.print("Enter day (for example-01)delivery: ");
        String  day = scanner.nextLine();
        System.out.print("Enter month (for example-07)delivery: ");
        String  month = scanner.nextLine();
        System.out.print("Enter year (for example-2017) delivery: ");
        String  year = scanner.nextLine();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date tmpDate;
        String date_delivery = year+"-"+month +"-"+day ;
        try {
            tmpDate = dateFormat.parse(date_delivery);
            String inverseTransformDate= dateFormat.format(tmpDate);
            if(!date_delivery.equals(inverseTransformDate)){
                System.out.println("Something wrong with your date.... Enter correct date of delivery");
                return;
            }
        } catch (ParseException e) {
            System.out.println("Something wrong with your date.... Enter correct date of delivery");
            return;
        }
        System.out.println("Select goods from list: ");
        System.out.println("selecting...");
// fill basket, fill order
        databaseJdbcConnection.selectProductsInsertOrder(new java.sql.Date (tmpDate.getTime()),client_id,date_delivery, scanner);
    }
    private static void searchOrder(Scanner sc) throws SQLException {
        System.out.println("Enter ID Order from list:");

        String orders = "orders";
        databaseJdbcConnection.selectAll(orders);

        String  order  = sc.nextLine();
        Integer order_id = Integer.parseInt(order);
        databaseJdbcConnection.detailedOrderInformation(order_id);
        System.out.println("this is the basket of this order:");
    }
//trick in that that to remove not simply so, all over again it is necessary to remove from basket corresponding lines because of a foreign key
    private static void deleteOrder(Scanner sc) throws SQLException {
        System.out.println("Enter ID Order from list:");
        String orders = "orders";
        databaseJdbcConnection.selectAll(orders);

        String  order  = sc.nextLine();
        Integer order_id;

        try {
            order_id = Integer.parseInt(order );
            databaseJdbcConnection.deleteOrder(order_id);
        } catch (NumberFormatException e) {
            System.out.println("Something wrong with your choice.... Enter correct order ID");
        }
    }
    private  static void joinSelect()throws  SQLException{
        databaseJdbcConnection.joinSelect();
    }
}
