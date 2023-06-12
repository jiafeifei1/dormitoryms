
import java.sql.*;
import java.util.Scanner;

public class Room {
    /*private int roomId;
    private String roomNumber;
    private int buildingId;
    private int people;**/

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/dormitory?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "123456";

    /*public Room(int roomId, String roomNumber, int buildingId, int people) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.buildingId = buildingId;
        this.people = people;
    }

    // 获取所有房间
    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<Room>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            String sql = "SELECT * FROM room";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Room room = new Room(rs.getInt("id"), rs.getString("room_number"), rs.getInt("building_id"), rs.getInt("people"));
                rooms.add(room);
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return rooms;
    }

    // 根据 ID 获取房间
    public static Room getRoomById(int id) {
        Room room = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            String sql = "SELECT * FROM room WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                room = new Room(rs.getInt("id"), rs.getString("room_number"), rs.getInt("building_id"), rs.getInt("people"));
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return room;
    }**/

    // 添加房间
    public static void addRoom(Statement stmt, Scanner scanner) throws SQLException {
        System.out.println("请输入宿舍楼编号：");
        String dormitoryBuildingId = scanner.next();
        System.out.println("请输入房间号：");
        String roomNumber = scanner.next();
        String roomid = dormitoryBuildingId+roomNumber;
        int room_people=0;
        if(searchRoom(stmt, roomid))
        {
            System.out.println("该宿舍房间已存在，请勿重复添加！");
        }
        else{
            String sql = "INSERT INTO room (dormitory_building_id,room_number,room_id,people) VALUES (?, ?, ?, ?)";
            Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dormitoryBuildingId);
            pstmt.setString(2, roomNumber);
            pstmt.setString(3, roomid);
            pstmt.setInt(4, room_people);
            int result = pstmt.executeUpdate();

            if (result == 1) {
                System.out.println("新增房间成功！");
            } else {
                System.out.println("新增房间失败！");
            }
        }
    }

    // 删除房间（该房间的学生的宿舍楼和房间号改为空）
    static void deleteRoom(Statement stmt, Scanner scanner) throws SQLException {
        System.out.println("请输入要删除的房间编号(宿舍楼+房间号)：");
        String roomId = scanner.next();
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        // 查询该房间是否存在
        if(searchRoom(stmt, roomId)) {
            // 获取该房间的学生
            String queryStudentsSql = "SELECT id FROM student WHERE room_ids=?";
            PreparedStatement queryStudentsStmt = conn.prepareStatement(queryStudentsSql);
            queryStudentsStmt.setString(1, roomId);
            ResultSet studentsResult = queryStudentsStmt.executeQuery();

            // 更新这些学生的dormitory_building和room_number字段
            String updateStudentSql = "UPDATE student SET dormitory_building = NULL, room_number = NULL, room_ids = NULL WHERE id = ?";
            PreparedStatement updateStudentStmt = conn.prepareStatement(updateStudentSql);
            while(studentsResult.next()) {
                String studentId = studentsResult.getString("id");
                updateStudentStmt.setString(1, studentId);
                updateStudentStmt.executeUpdate();
            }

            //删除该房间
            String deleteRoomSql = "DELETE FROM room WHERE room_id = ?";
            PreparedStatement deleteRoomStmt = conn.prepareStatement(deleteRoomSql);
            deleteRoomStmt.setString(1, roomId);
            int result = deleteRoomStmt.executeUpdate();
            if(result == 1) {
                System.out.println("删除房间成功！");
            } else {
                System.out.println("删除房间失败！");
            }
        } else {
            System.out.println("该房间不存在！");
        }
    }

    // 修改房间
    static void updateRoom(Statement stmt, Scanner scanner) throws SQLException {
        System.out.println("请输入要修改的房间编号：");
        String id = scanner.next();
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
        if (searchRoom(stmt,id)) {
            System.out.println("请输入新的宿舍楼编号：");
            String dormitoryBuildingId = scanner.next();
            System.out.println("请输入新的房间号：");
            String roomNumber = scanner.next();

            if(Dormitory.searchBuilding(stmt,dormitoryBuildingId)&&!searchRoom(stmt,roomNumber)) {
                // 获取该房间的学生
                String queryStudentsSql = "SELECT id FROM student WHERE room_ids=?";
                PreparedStatement queryStudentsStmt = conn.prepareStatement(queryStudentsSql);
                queryStudentsStmt.setString(1, id);
                ResultSet studentsResult = queryStudentsStmt.executeQuery();

                // 更新这些学生的dormitory_building和room_number字段
                String updateStudentSql = "UPDATE student SET dormitory_building = NULL, room_number = NULL, room_ids = NULL WHERE id = ?";
                PreparedStatement updateStudentStmt = conn.prepareStatement(updateStudentSql);
                while(studentsResult.next()) {
                    String studentId = studentsResult.getString("id");
                    updateStudentStmt.setString(1, studentId);
                    updateStudentStmt.executeUpdate();
                }

                String sql = "UPDATE room SET room_number = ?, dormitory_building_id = ?,room_id = ? WHERE room_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, roomNumber);
                    pstmt.setString(2, dormitoryBuildingId);
                    pstmt.setString(3,  dormitoryBuildingId + roomNumber);
                    pstmt.setString(4, id);

                    int result = pstmt.executeUpdate();
                    if (result == 1) {
                        System.out.println("修改房间成功！");
                    } else {
                        System.out.println("修改房间失败！");
                    }
                } catch (SQLException e) {
                    System.out.println("修改房间信息时发生异常！");
                    e.printStackTrace();
                }
            }
            else if(!Dormitory.searchBuilding(stmt,dormitoryBuildingId)&&!searchRoom(stmt,roomNumber)) {
                System.out.println("该宿舍楼不存在！");
            }
            else
                System.out.println("该宿舍房间已存在！");
        }
        else{
            System.out.println("该房间不存在！");
        }
    }
    // 浏览房间
    static void queryRoom(Statement stmt) throws SQLException {
        String sql = "SELECT r.room_id,r.room_number,b.name,r.people FROM room r,dormitory_building b WHERE r.dormitory_building_id=b.db_id";
        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("编号\t" +"    房间\t" +"  宿舍楼\t" +"入住人数\t");
        while(rs.next()){
            System.out.print( rs.getString("room_id")+"\t");
            System.out.print( rs.getString("room_number")+"  \t");
            System.out.print( rs.getString("name")+"\t");
            System.out.println( rs.getInt("people"));
        }
        rs.close();
    }
    // 查询房间是否存在，返回布尔值
    static boolean searchRoom(Statement stmt, String roomid) throws SQLException {
        String sql = "SELECT * FROM room";
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()) {
            if(rs.getString("room_id").equals(roomid)) {
                return true;
            }
        }
        return false;
    }
    // 更新房间入住人数
    static void updateroom_number(String flag, String roomId) {
        String sql = "SELECT people FROM room WHERE room_id = ?";
        int number=0;
        Connection conn;
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                number = rs.getInt("people");
            }
            if (flag.equals("increase")) {
                String increase_sql = "UPDATE room SET people = ? WHERE room_id = ?";
                PreparedStatement pstmt1 = conn.prepareStatement(increase_sql);
                pstmt1.setInt(1, number + 1);
                pstmt1.setString(2, roomId);
                pstmt1.executeUpdate();
            } else if (flag.equals("reduce")) {
                String reduce_sql = "UPDATE room SET people = ? WHERE room_id = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(reduce_sql);
                pstmt2.setInt(1, number - 1);
                pstmt2.setString(2, roomId);
                pstmt2.executeUpdate();
            } else {
                System.out.println("请检查传入的flag");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

/*
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }**/
}
