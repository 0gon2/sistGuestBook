package guestbook.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import guestbook.model.Message;
import jdbc.JdbcUtil;

public class MessageDao {
	private static MessageDao messageDao = new MessageDao();
	public static MessageDao getInstance() {
		return messageDao;
	}
	
	private MessageDao() {}
	
	public int insert(Connection conn, Message message) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try {
			pstmt = conn.prepareStatement("select nvl(max(id),0) from guestbook_message ");
			rs=pstmt.executeQuery();	
			int max=0;
			if(rs.next()) {
				max=rs.getInt(1);
			}
			
			pstmt = conn.prepareStatement(
					"insert into guestbook_message " + 
					"(id, guestname, password, message) values (?, ?, ?, ?)");
			pstmt.setInt(1, max+1);
			pstmt.setString(2, message.getGuestName());
			pstmt.setString(3, message.getPassword());
			pstmt.setString(4, message.getMessage());
			return pstmt.executeUpdate();
		} finally {
			JdbcUtil.close(pstmt);
		}
	}

	public Message select(Connection conn) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(
					"select * from guestbook_message");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return makeMessageFromResultSet(rs);
			} else {
				return null;
			}
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
	}

	private Message makeMessageFromResultSet(ResultSet rs) throws SQLException {
		Message message = new Message();
		message.setId(rs.getInt("id"));
		message.setGuestName(rs.getString("guestname"));
		message.setPassword(rs.getString("password"));
		message.setMessage(rs.getString("message"));
		return message;
	}

	public int selectCount(Connection conn) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from guestbook_message");
			rs.next();
			return rs.getInt(1);
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(stmt);
		}
	}

	public List<Message> selectList(Connection conn, int firstRow, int endRow) 
			throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql=null;
		try {
			sql = " select * from (select rownum rnum ,a.* from "
					+ "(select * from guestbook_message "
					+ "order by id desc) "
					+ "	a ) where rnum  between ? and ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, firstRow - 1);
			pstmt.setInt(2, endRow - firstRow + 1);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				List<Message> messageList = new ArrayList<Message>();
				do {
					messageList.add(makeMessageFromResultSet(rs));
				} while (rs.next());
				return messageList;
			} else {
				return Collections.emptyList();
			}
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
	}

	public int delete(Connection conn, int id) throws SQLException {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(
					"delete from guestbook_message where id = ?");
			pstmt.setInt(1, id);
			return pstmt.executeUpdate();
		} finally {
			JdbcUtil.close(pstmt);
		}
	}

}
