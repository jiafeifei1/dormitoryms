import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Dormitory {
    /*private int id;
    private String name;
    private ArrayList<Room> rooms;**/
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/dormitory?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "123456";

    /*public Dormitory(int id, String name) {
        this.id = id;
        this.name = name;
        this.rooms = new ArrayList<Room>();
    }**/
    //添加宿舍楼
    static void addDormitoryBuilding(Statement stmt, Scanner scanner) throws SQLException {
        String Sex;
        while(true){
            System.out.println("请输入宿舍楼名称：");
            String dormitoryBuildingName = scanner.next();
            if(searchBuilding(stmt, dormitoryBuildingName)){
                System.out.println("该宿舍楼已存在，请勿重复添加！");
            }
            else{
                System.out.println("请输入宿舍楼地址：");
                String dormitoryBuildingAddress = scanner.next();
                while (true) {
                    System.out.println("请选择宿舍入住性别：1.女 2.男");
                    int ch = scanner.nextInt();
                    if(ch == 1) {
                        Sex = "女";
                        break;
                    }
                    else if (ch == 2) {
                        Sex = "男";
                        break;
                    }
                    else
                        System.out.println("请选择已有选项！");
                }
                String sql = "INSERT INTO dormitory_building (name, address, db_id,sex) VALUES (?, ?, ?, ?)";
                Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dormitoryBuildingName);
                pstmt.setString(2, dormitoryBuildingAddress);
                pstmt.setString(3, dormitoryBuildingName);
                pstmt.setString(4, Sex);
                int result = pstmt.executeUpdate();
                if (result == 1) {
                    System.out.println("新增宿舍楼成功！");
                } else {
                    System.out.println("新增宿舍楼失败！");
                }
                break;
            }

        }
    }
    //删除宿舍楼
    static void deleteDormitoryBuilding(Statement stmt, Scanner scanner) throws SQLException {
        System.out.println("请输入要删除的宿舍楼编号：");
        String id = scanner.next();
        if(searchBuilding(stmt, id)){
            String sql = "DELETE dormitory_building, room FROM dormitory_building LEFT JOIN room ON dormitory_building.db_id = room.dormitory_building_id WHERE dormitory_building.db_id = ?";
            Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("成功删除宿舍楼及其房间！");
            } else {
                System.out.println("删除宿舍楼失败！");
            }
        }
        else {
            System.out.println("该宿舍不存在！");
        }

    }

    // 修改宿舍楼
    static void updateDormitoryBuilding(Statement stmt, Scanner scanner) throws SQLException {
        System.out.println("请输入要修改的宿舍楼名称：");
        String id = scanner.next();
        if(searchBuilding(stmt, id)){
            int choice;
            do {
                System.out.println("请选择要修改的宿舍楼信息:");
                System.out.println("1. 宿舍楼名称");
                System.out.println("2. 宿舍楼地址");
                System.out.println("3. 宿舍楼入住性别");
                System.out.println("4. 全部修改");
                System.out.print("您的选择是：");
                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        updateDormitoryBuildingName(stmt, scanner, id);
                        break;
                    case 2:
                        updateDormitoryBuildingAddress( scanner, id);
                        break;
                    case 3:
                        updateDormitoryBuildingSex(stmt, scanner, id);
                        break;
                    case 4:
                        updateDormitoryBuildingAddress( scanner, id);
                        updateDormitoryBuildingSex(stmt, scanner, id);
                        updateDormitoryBuildingName(stmt, scanner, id);
                        System.out.println("浏览全部信息");
                        queryDormitoryBuilding( stmt);
                        System.out.println("按任意键继续...");
                        try {
                            System.in.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("请输入已有选项！");
                        break;
                }
            } while (choice < 1 || choice > 4);
        }
        else{
            System.out.println("该宿舍楼不存在！");
        }
    }

    private static void updateDormitoryBuildingName(Statement stmt, Scanner scanner, String id) throws SQLException {
        while(true) {
            System.out.println("请输入新的宿舍楼名称：");
            String dormitoryBuildingName = scanner.next();
            if(searchBuilding(stmt, dormitoryBuildingName)){
                System.out.println("该宿舍楼已存在，请重新输入！");
            }
            else {
                String sql = "UPDATE dormitory_building SET name = ? ,db_id = ? WHERE db_id = ?";
                try (Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, dormitoryBuildingName);
                    pstmt.setString(2, dormitoryBuildingName);
                    pstmt.setString(3, id);
                    int result = pstmt.executeUpdate();
                    if (result == 1) {
                        System.out.println("修改宿舍楼名称成功！");
                    } else {
                        System.out.println("修改宿舍楼名称失败！");
                    }
                } catch (SQLException e) {
                    System.out.println("修改宿舍楼名称时发生异常！");
                    e.printStackTrace();
                }

                break;
            }
        }

    }

    private static void updateDormitoryBuildingAddress( Scanner scanner, String id) throws SQLException {
        System.out.println("请输入新的宿舍楼地址：");
        String dormitoryBuildingAddress = scanner.next();

        String sql = "UPDATE dormitory_building SET address = ? WHERE db_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dormitoryBuildingAddress);
            pstmt.setString(2, id);
            int result = pstmt.executeUpdate();
            if (result == 1) {
                System.out.println("修改宿舍楼地址成功！");
            } else {
                System.out.println("修改宿舍楼地址失败！");
            }
        } catch (SQLException e) {
            System.out.println("修改宿舍楼地址时发生异常！");
            e.printStackTrace();
        }
    }
    private static void updateDormitoryBuildingSex(Statement stmt, Scanner scanner, String id) throws SQLException {
        String dormitoryBuildingSex;
        while (true) {
            System.out.println("请选择宿舍入住性别：1.女 2.男");
            int ch = scanner.nextInt();
            if(ch == 1) {
                dormitoryBuildingSex = "女";
                break;
            }
            else if (ch == 2) {
                dormitoryBuildingSex = "男";
                break;
            }
            else
                System.out.println("请选择已有选项！");
        }
        String sql = "UPDATE dormitory_building SET sex = ? WHERE db_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dormitoryBuildingSex);
            pstmt.setString(2, id);
            int result = pstmt.executeUpdate();
            if (result == 1) {
                System.out.println("修改宿舍楼入住性别成功！");
                System.out.println("浏览全部信息");
                queryDormitoryBuilding( stmt);
            } else {
                System.out.println("修改宿舍楼入住性别失败！");
            }
        } catch (SQLException e) {
            System.out.println("修改宿舍楼入住性别时发生异常！");
            e.printStackTrace();
        }
        System.out.println("按任意键继续...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 浏览宿舍楼
    static void queryDormitoryBuilding(Statement stmt) throws SQLException {
        String sql = "SELECT * FROM dormitory_building";
        ResultSet rs = stmt.executeQuery(sql);
        int num=1;
        System.out.printf("%-8s%-18s%-19s%-19s\n", "序号", "名称", "入住性别", "地址");
        while(rs.next()){
            System.out.printf("%-10s%-20s%-19s%-20s\n", num++, rs.getString("name"), rs.getString("sex"), rs.getString("address"));
        }

        rs.close();
    }
    // 查询宿舍楼是否存在，返回布尔值
    static boolean searchBuilding(Statement stmt, String buildingName) throws SQLException {
        String sql = "SELECT * FROM dormitory_building";
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()) {
            if(rs.getString("name").equals(buildingName)) {
                return true;
            }
        }

        return false;
    }

}
