import java.sql.*;
import java.util.Scanner;

public class Student {
    /*private int id;
    private String name;
    private int roomId;**/
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/dormitory?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "123456";

   /* public Student(int id, String name, int roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
    }**/
// 根据学生的性别男、女列出可入住的宿舍楼
   private static void listbuildings(Statement stmt, String sex ) throws SQLException {
       String sql = "SELECT * FROM dormitory_building WHERE sex='男'";
       ResultSet rs = stmt.executeQuery(sql);
       int num=1;
       System.out.printf("%-8s%-18s%-19s\n", "序号", "名称", "地址");
       while(rs.next()){
           System.out.printf("%-10s%-20s%-20s\n", num++, rs.getString("name"), rs.getString("address"));
       }

       rs.close();
   }
    // 根据宿舍楼列出可入住的房间
    private static void listroom(Statement stmt, String building ) throws SQLException {
        String sql = "SELECT * FROM room WHERE dormitory_building_id=?";
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, building);

        ResultSet rs = pstmt.executeQuery();
        System.out.printf("可选择宿舍房间：\n");
        while (rs.next()) {
            if(rs.getInt("people")<6)
                System.out.printf("%-10s%-5d\n", rs.getString("room_id"), rs.getInt("people"));
        }
        rs.close();
    }
    // 判断该宿舍楼是否满足条件
    private static boolean judge_building( String building, String sex ) throws  SQLException{
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        String sql = "SELECT * FROM dormitory_building WHERE name=? AND sex=?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, building);
        pstmt.setString(2, sex);

        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    // 判断该宿舍房间人数是否已满
    private static boolean judge_room_people( String roomid ) throws SQLException {
        String sql = "SELECT * FROM room WHERE room_id=?";
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, roomid);

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            if (rs.getInt("people") < 6)
                return true;
        }
        return false;
    }



    // 添加学生信息、入住
     static void addStudent(Statement stmt, Scanner scanner) throws SQLException {
        String id;
        while (true) {
            System.out.println("请输入学生学号：");
            id = scanner.next();
            if(searchStudentByid(stmt,id))
                System.out.println("该学号已被占用！");
            else
                break;
        }

        System.out.println("请输入学生姓名：");
        String name = scanner.next();
        System.out.println("请输入学生年龄：");
        String age = scanner.next();
        System.out.println("请选择学生性别：");
        String gender;
        while (true) {
            System.out.println("1.女 2.男");
            int ch = scanner.nextInt();
            if(ch == 1) {
                gender = "女";
                break;
            }
            else if (ch == 2) {
                gender = "男";
                break;
            }
            else
                System.out.println("请选择已有选项！");
        }

        listbuildings( stmt, gender);

        System.out.println("请输入学生所在宿舍楼名称：");
        String dormitoryBuildingId;
        while( true ) {
            dormitoryBuildingId = scanner.next();
            if(judge_building(dormitoryBuildingId,gender))
                break;
            else
                System.out.println("该宿舍楼不符合要求，请选择合适的宿舍楼！");
        }
        listroom(stmt,dormitoryBuildingId);

        System.out.println("请输入学生所在房间号：");
        String room_num ;
        while( true ) {
            room_num = scanner.next();
            if(judge_room_people( dormitoryBuildingId+room_num ))
                break;
            else
                System.out.println("该房间不符合要求，请选择合适的宿舍房间！");
        }
        if(Room.searchRoom(stmt, dormitoryBuildingId+room_num))
        {
            String sql = "INSERT INTO student (id,name,gender,age,dormitory_building,room_number,room_ids) VALUES (?, ?, ?, ?, ?, ?, ?) " ;
            Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, gender);
            pstmt.setString(4, age);
            pstmt.setString(5, dormitoryBuildingId);
            pstmt.setString(6, room_num);
            pstmt.setString(7, dormitoryBuildingId+room_num);
            int result = pstmt.executeUpdate();
            if (result == 1) {
                System.out.println("新增学生成功！");
                Room.updateroom_number( "increase", dormitoryBuildingId+room_num);
            } else {
                System.out.println("新增学生失败！");
            }
        }
        else{
            System.out.println("该房间不存在！");
        }
    }

    static void quitDorm(Scanner scanner) throws SQLException {
        System.out.println("请输入要退宿的学生编号:");
        String studentId = scanner.next();
        String sql = "UPDATE student SET dormitory_building=NULL, room_number=NULL, room_ids=NULL WHERE id=?";
        String roomid = "";
        String query = "SELECT room_ids FROM student WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement psQuery = conn.prepareStatement(query);
             PreparedStatement psUpdate = conn.prepareStatement(sql)) {
            psQuery.setString(1, studentId);
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                roomid = rs.getString("room_id");
            }
            psUpdate.setString(1, studentId);
            System.out.println("roomid="+roomid);
            int result = psUpdate.executeUpdate();
            if (result == 1) {
                System.out.println("学生退宿成功！");
                Room.updateroom_number("reduce", roomid);
            } else {
                System.out.println("学生退宿失败！");
            }
        }
    }
    static void switchRoom(Statement stmt, Scanner scanner) throws SQLException {
        System.out.println("请输入要调换宿舍的学生学号：");
        String studentId = scanner.next();
        if(searchStudentByid(stmt,studentId)) {
            Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
            String selectSql = "SELECT * FROM student WHERE id=?";
            PreparedStatement selectPstmt ;
            selectPstmt = conn.prepareStatement(selectSql);
            selectPstmt.setString(1, studentId);
            ResultSet resultSet = selectPstmt.executeQuery();
            String beforeroomId = "";
            String stu_sex="";
            if (resultSet.next()) {
                beforeroomId = resultSet.getString("room_ids");
                stu_sex = resultSet.getString("gender");
            }

            listbuildings( stmt, stu_sex);

            System.out.println("请输入学生要调换的宿舍楼名称：");
            String building_id;
            while( true ) {
                building_id = scanner.next();
                if(judge_building(building_id,stu_sex))
                    break;
                else
                    System.out.println("该宿舍楼不符合要求，请选择合适的宿舍楼！");
            }
            listroom(stmt,building_id);

            System.out.println("请输入学生要调换的房间号：");
            String room_num ;
            while( true ) {
                room_num = scanner.next();
                if(judge_room_people( building_id+room_num ))
                    break;
                else
                    System.out.println("该房间不符合要求，请选择合适的宿舍房间！");
            }

            if(Room.searchRoom(stmt, building_id+room_num)) {
                String sql = "UPDATE student SET dormitory_building=?,room_number=?,room_ids=? WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, building_id);
                pstmt.setString(2, room_num);
                pstmt.setString(3, building_id + room_num);
                pstmt.setString(4, studentId);
                int result = pstmt.executeUpdate();
                if (result == 1) {
                    System.out.println("学生调换宿舍成功！");
                    Room.updateroom_number("reduce", beforeroomId);
                    Room.updateroom_number("increase", building_id + room_num);
                } else {
                    System.out.println("学生调换宿舍失败！");
                }
                pstmt.close();
            }

            selectPstmt.close();

            conn.close();
        }
        else
            System.out.println("该学生不存在！");
    }
    static void queryStudent(Statement stmt) throws SQLException {
        String sql = "SELECT * FROM student";
        ResultSet rs = stmt.executeQuery(sql);
        System.out.printf("%-9s %-9s %-6s %-5s %-10s%n", "学号", "姓名", "性别", "年龄", "宿舍房间");
        while (rs.next()) {
            System.out.printf("%-10s %-10s %-6s %-6s %-10s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getInt("age"),
                    rs.getString("room_ids"));
        }

        rs.close();
    }
    //按照宿舍楼房间查找学生
    public static void getStudentsByBuilding( Scanner scan) throws SQLException {
        System.out.println("输入宿舍房间:");
        String roomID = scan.next();
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        String sql ="SELECT * FROM student WHERE room_ids=?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, roomID);
        ResultSet rs = pstmt.executeQuery();
        System.out.printf("%-9s %-9s %-6s %-5s %-10s%n", "学号", "姓名", "性别", "年龄", "宿舍房间");
        while (rs.next()) {
            System.out.printf("%-10s %-10s %-6s %-6s %-10s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getInt("age"),
                    rs.getString("room_ids"));
        }
    }

    // 按照学号查找学生
    public static boolean searchStudentByid( Statement stmt, String stu_id) throws SQLException {

        String sql = "SELECT * FROM student";
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()) {
            if(rs.getString("id").equals(stu_id)) {
                return true;
            }
        }
        return false;
    }
}

