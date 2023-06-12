import java.sql.*;
import java.util.Scanner;

class DormitoryManagement {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/dormitory?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    static final String USER = "root";
    static final String PASS = "123456";
    public static void main(String[] args) throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        login();

    }

    private static void login() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入用户名：");
            String username = scanner.nextLine();
            System.out.println("请输入密码：");
            String password = scanner.nextLine();
            if (checkLogin(username, password)) {
                System.out.println("登录成功！");

                updateRoomTable(); // 更新房间表
                updateStudentTable(); // 更新学生表
                break;
            } else {
                System.out.println("用户名或密码错误，请重新输入！");
            }
        }
        showMenu();
    }

    private static boolean checkLogin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private static void showMenu() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        Statement stmt = conn.createStatement();
        int option = -1;
        while (option != 0) {
            System.out.println("欢迎来到学生宿舍管理系统！\n请选择操作：");
            System.out.println("1. 宿舍楼管理");
            System.out.println("2. 房间管理");
            System.out.println("3. 学生管理");
            System.out.println("0. 退出");
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    dormitoryBuildingManagement(stmt, scanner);
                    break;
                case 2:
                    roomManagement(stmt, scanner);
                    break;
                case 3:
                    studentManagement(stmt, scanner);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("请输入有效的操作编号！");
                    break;
            }
        }
    }

    // 宿舍楼管理菜单
    private static void dormitoryBuildingManagement(Statement stmt, Scanner scanner) throws SQLException {
        int option = -1;
        while (option != 0) {
            System.out.println("请选择宿舍楼管理操作：");
            System.out.println("1. 增加宿舍楼");
            System.out.println("2. 删除宿舍楼");
            System.out.println("3. 修改宿舍楼");
            System.out.println("4. 查询宿舍楼");
            System.out.println("0. 返回上一级");
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    Dormitory.addDormitoryBuilding(stmt, scanner);
                    break;
                case 2:
                    Dormitory.deleteDormitoryBuilding(stmt,scanner);
                    break;
                case 3:
                    Dormitory.updateDormitoryBuilding(stmt, scanner);
                    break;
                case 4:
                    Dormitory.queryDormitoryBuilding(stmt);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("请输入有效的操作编号！");
                    break;
            }
        }
    }

    // 房间管理（添加，删除，修改，浏览）
    // 房间管理菜单
    private static void roomManagement(Statement stmt, Scanner scanner) throws SQLException {
        int option = -1;
        while (option != 0) {
            System.out.println("请选择房间管理操作：");
            System.out.println("1. 增加房间");
            System.out.println("2. 删除房间");
            System.out.println("3. 修改房间");
            System.out.println("4. 查询房间");
            System.out.println("0. 返回上一级");
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    Room.addRoom(stmt, scanner);
                    break;
                case 2:
                    Room.deleteRoom(stmt, scanner);
                    break;
                case 3:
                    Room.updateRoom(stmt, scanner);
                    break;
                case 4:
                    Room.queryRoom(stmt);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("请输入有效的操作编号！");
                    break;
            }
        }
    }

    private static void studentManagement(Statement stmt, Scanner scanner) throws SQLException {
        int option = -1;
        while (option != 0) {
            System.out.println("请选择学生管理操作：");
            System.out.println("1. 新增学生");
            System.out.println("2. 学生退宿");
            System.out.println("3. 学生转宿");
            System.out.println("4. 查询学生");
            System.out.println("5. 按照宿舍房间查找");
            System.out.println("0. 返回上一级");
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    Student.addStudent(stmt, scanner);
                    break;
                case 2:
                    Student.quitDorm(scanner);
                    break;
                case 3:
                    Student.switchRoom(stmt, scanner);
                    break;
                case 4:
                    Student.queryStudent(stmt);
                    break;
                case 5:
                    Student.getStudentsByBuilding(scanner);
                case 0:
                    break;
                default:
                    System.out.println("请输入有效的操作编号！");
                    break;
            }
        }
    }
    // 更新房间表格
    private static void updateRoomTable(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String query = "SELECT * FROM dormitory_building WHERE db_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query);
                Statement updateStmt = conn.createStatement()
        ) {
            // 查询所有的room记录
            String selectAllQuery = "SELECT * FROM room";
            ResultSet rs = updateStmt.executeQuery(selectAllQuery);

            // 对每条记录判断所属宿舍楼是否存在
            while (rs.next()) {
                String roomId = rs.getString("room_id");
                String buildingId = rs.getString("dormitory_building_id");
                //String roomNumber = rs.getString("room_number");

                stmt.setString(1, buildingId);
                ResultSet buildingRs = stmt.executeQuery();
                if (buildingRs.next()) {
                    System.out.print("");// 宿舍楼存在，不做处理
                } else {
                    // 宿舍楼不存在，删除该房间
                    String deleteQuery = "DELETE FROM room WHERE dormitory_building_id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setString(1, buildingId);
                    deleteStmt.executeUpdate();
                    System.out.println("Room " + roomId + " belongs to a non-existent building, deleted.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    // 更新学生表格以及房间人数
    private static void updateStudentTable() {
        String roomQuery = "SELECT * FROM room WHERE room_id = ?";
        String updateStudentQuery = "UPDATE student SET dormitory_building = NULL, room_number = NULL, room_ids = NULL WHERE room_ids = ?";
        String updateRoomQuery = "UPDATE room SET people = people + 1 WHERE room_id = ?";
        String sql = "UPDATE room SET people = 0";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try (PreparedStatement roomStmt = conn.prepareStatement(roomQuery);
             PreparedStatement updateStudentStmt = conn.prepareStatement(updateStudentQuery);
             PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoomQuery);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement selectAllStmt = conn.createStatement()) {

            pstmt.executeUpdate();// 初始化房间入住人数
            ResultSet rs = selectAllStmt.executeQuery("SELECT * FROM student");

            // 对每条记录判断所属房间是否存在
            while (rs.next()) {
                String roomID = rs.getString("room_ids");
                String studentName = rs.getString("name");

                roomStmt.setString(1, roomID);
                ResultSet roomRs = roomStmt.executeQuery();
                if (roomRs.next()) {
                    // 房间存在，更新房间人数
                    updateRoomStmt.setString(1, roomID);
                    updateRoomStmt.executeUpdate();
                } else {
                    // 房间不存在，更新该学生
                    updateStudentStmt.setString(1, roomID);
                    updateStudentStmt.executeUpdate();
                    System.out.println("Dormitory " + studentName + " belongs to a non-existent room.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}