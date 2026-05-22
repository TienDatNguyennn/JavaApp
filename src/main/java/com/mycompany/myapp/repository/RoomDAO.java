package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng ROOM.
 * Caller phải gọi DBConnection.commitTransaction() sau mỗi write.
 */
public class RoomDAO {

    public List<Room> findAllActive() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT room_id, room_name, capacity, floor, room_type, " +
                     "created_at, updated_at, is_deleted " +
                     "FROM ROOM WHERE is_deleted = 0 ORDER BY floor, room_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT room_id, room_name, capacity, floor, room_type, " +
                     "created_at, updated_at, is_deleted " +
                     "FROM ROOM WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean insert(Room r) throws SQLException {
        String sql = "INSERT INTO ROOM (room_id, room_name, capacity, floor, room_type) " +
                     "VALUES ((SELECT NVL(MAX(room_id), 0) + 1 FROM ROOM), ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, r.getRoomName());
            ps.setInt(2, r.getCapacity());
            ps.setInt(3, r.getFloor());
            ps.setNString(4, r.getRoomType());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Room r) throws SQLException {
        String sql = "UPDATE ROOM SET room_name = ?, capacity = ?, floor = ?, " +
                     "room_type = ?, updated_at = SYSDATE " +
                     "WHERE room_id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, r.getRoomName());
            ps.setInt(2, r.getCapacity());
            ps.setInt(3, r.getFloor());
            ps.setNString(4, r.getRoomType());
            ps.setInt(5, r.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int roomId) throws SQLException {
        String sql = "UPDATE ROOM SET is_deleted = 1, updated_at = SYSDATE WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomName(rs.getString("room_name"));
        r.setCapacity(rs.getInt("capacity"));
        r.setFloor(rs.getInt("floor"));
        r.setRoomType(rs.getString("room_type"));
        r.setCreatedAt(rs.getDate("created_at"));
        r.setUpdatedAt(rs.getDate("updated_at"));
        r.setDeleted(rs.getInt("is_deleted") == 1);
        return r;
    }
}
